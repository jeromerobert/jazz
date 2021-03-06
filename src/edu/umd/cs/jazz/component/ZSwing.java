/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.beans.*;
import java.util.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;

/*
  This message was sent to Sun on August 27, 1999

  -----------------------------------------------

  We are currently developing Jazz, a "scenegraph" for use in 2D graphics.
  One of our ultimate goals is to support Swing lightweight components
  within Jazz, whose graphical space supports arbitray affine transforms.
  The challenge in this pursuit is getting the components to respond and
  render properly though not actually displayed in a standard Java component
  hierarchy.


  The first issues involved making the Swing components focusable and
  showing.  This was accomplished by adding the Swing components to a 0x0
  JComponent which was in turn added to our main Jazz application component.
  To our good fortune, a Java component is showing merely if it and its
  ancestors are showing and not based on whether it is ACTUALLY visible.
  Likewise, focus in a JComponent depends merely on the component's
  containing window having focus.


  The second issue involved capturing the repaint calls on a Swing
  component.  Normally, for a repaint and the consequent call to
  paintImmediately, a Swing component obtains the Graphics object necessary
  to render itself through the Java component heirarchy.  However, for Jazz
  we would like the component to render using a Graphics object that Jazz
  may have arbitrarily transformed in some way.  By capturing in the
  RepaintManager the repaint calls made on our special Swing components, we
  are able to redirect the repaint requests through the Jazz architecture to
  put the Graphics in its proper context.  Unfortunately, this means that
  if the Swing component contains other Swing components, then any repaint
  requests made by one of these nested components must go through
  the Jazz architecture then through the top level Swing component
  down to the nested Swing component.  This normally doesn't cause a
  problem.  However, if calling paint on one of these nested
  children causes a call to repaint then an infinite loop ensues.  This does
  in fact happen in the Swing components that use cell renderers.  Before
  the cell renderer is painted, it is invalidated and consequently
  repainted.  We solved this problem by putting a lock on repaint calls for
  a component while that component is painting.  (A similar problem faced
  the Swing team over this same issue.  They solved it by inserting a
  CellRendererPane to capture the renderer's invalidate calls.)


  Another issue arose over the forwarding of mouse events to the Swing
  components.  Since our Swing components are not actually displayed on
  screen in the standard manner, we must manually dispatch any MouseEvents
  we want the component to receive.  Hence, we needed to find the deepest
  visible component at a particular location that accepts MouseEvents.
  Finding the deepest visible component at a point was achieved with the
  "findComponentAt" method in java.awt.Container.  With the
  "getListeners(Class listenerType)" method added in JDK1.3 Beta we are able
  to determine if the component has any Mouse Listeners. However, we haven't
  yet found a way to determine if MouseEvents have been specifically enabled
  for a component. The package private method "eventEnabled" in
  java.awt.Component does exactly what we want but is, of course,
  inaccessible.  In order to dispatch events correctly we would need a
  public accessor to the method "boolean eventEnabled(AWTEvent)" in
  java.awt.Component.


  Still another issue involves the management of cursors when the mouse is
  over a Swing component in our application.  To the Java mechanisms, the
  mouse never appears to enter the bounds of the Swing components since they
  are contained by a 0x0 JComponent.  Hence, we must manually change the
  cursor when the mouse enters one of the Swing components in our
  application. This generally works but becomes a problem if the Swing
  component's cursor changes while we are over that Swing component (for
  instance, if you resize a Table Column).  In order to manage cursors
  properly, we would need setCursor to fire property change events.


  With the above fixes, most Swing components work.  The only Swing
  components that are definitely broken are ToolTips and those that rely on
  JPopupMenu. In order to implement ToolTips properly, we would need to have
  a method in ToolTipManager that allows us to set the current manager, as
  is possible with RepaintManager.  In order to implement JPopupMenu, we
  will likely need to reimplement JPopupMenu to function in Jazz with
  a transformed Graphics and to insert itself in the proper place in the
  Jazz scenegraph.

*/


/**
 * <b>ZSwing</b> is a Visual Component wrapper used to add
 * Swing Components to a Jazz ZCanvas.
 * <P>
 * Example: adding a swing JButton to a ZCanvas:
 * <pre>
 *     ZCanvas canvas = new ZCanvas();
 *     JButton button = new JButton("Button");
 *     swing = new ZSwing(canvas, button);
 *     leaf = new ZVisualLeaf(swing);
 *     canvas.getLayer().addChild(leaf);
 * <pre>
 *
 * NOTE: ZSwing has the current limitation that it does not listen for
 *       Container events.  This is only an issue if you create a ZSwing
 *       and later add Swing components to the ZSwing's component hierarchy
 *       that do not have double buffering turned off or have a smaller font
 *       size than the minimum font size of the original ZSwing's component
 *       hierarchy.
 *
 *       For instance, the following bit of code will give unexpected
 *       results:
 *       <pre>
 *            JPanel panel = new JPanel();
 *            ZSwing swing = new ZSwing(panel);
 *            JPanel newChild = new JPanel();
 *            newChild.setDoubleBuffered(true);
 *            panel.add(newChild);
 *       </pre>
 * <p>
 * NOTE: ZSwing is not properly ZSerializable, but it is java.io.Serializable.
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author Benjamin B. Bederson
 * @author Lance E. Good
 */
public class ZSwing extends ZVisualComponent implements Serializable, PropertyChangeListener {

    /**
     * The cutoff at which the Swing component is rendered greek
     */
    protected double renderCutoff = 0.3;

    /**
     * The Swing component that this Visual Component wraps
     */
    protected JComponent component = null;

    /**
     * The minimum font size in the Swing hierarchy rooted at the component
     */
    protected double minFontSize = Double.MAX_VALUE;

    /**
     * The default stroke
     */
    protected transient Stroke defaultStroke = new BasicStroke();

    /**
     * The default font
     */
    protected Font defaultFont = new Font("Serif",Font.PLAIN,12);

    /**
     * Used as a hashtable key for this object in the Swing component's
     * client properties.
     */
    public static final String VISUAL_COMPONENT_KEY = "ZSwing";

    /**
     * Constructs a new visual component wrapper for the Swing component
     * and adds the Swing component to the SwingWrapper component of
     * the ZCanvas
     * @param zbc The ZCanvas to which the Swing component will
     *            be added
     * @param component The swing component to be wrapped
     */
    public ZSwing(ZCanvas zbc,JComponent component) {

        this.component = component;
        component.putClientProperty(VISUAL_COMPONENT_KEY, this);
        init(component);
        zbc.getSwingWrapper().add(component);
        component.revalidate();
        reshape();
    }

    /**
     * Determines if the Swing component should be rendered normally or
     * as a filled rectangle.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param renderContext Contains information about current render.
     */
    public void render(ZRenderContext renderContext) {
        Graphics2D g2 = renderContext.getGraphics2D();

        if (defaultStroke == null) {
            defaultStroke = new BasicStroke();
        }
        g2.setStroke(defaultStroke);

        if (defaultFont == null) {
            defaultFont = new Font("Serif",Font.PLAIN,12);
        }

        g2.setFont(defaultFont);

        if (component.getParent() == null && 
	    renderContext.getDrawingSurface() != null) {
	    ZCanvas canvas = (ZCanvas) renderContext.getDrawingSurface().getComponent();
	    canvas.getSwingWrapper().add(component);
	    component.revalidate();
        }

	if ((renderContext.getCompositeMagnification() < renderCutoff &&
	     (renderContext.getDrawingSurface() != null) && 
	     renderContext.getDrawingSurface().isInteracting()) ||
            (minFontSize*renderContext.getCompositeMagnification() < 0.5)) {
            paintAsGreek(g2);
        }
        else {
            paint(g2);
        }

    }

    /**
     * Paints the Swing component as greek.
     * @param g2 The graphics used to render the filled rectangle
     */
    public void paintAsGreek(Graphics2D g2) {
        Color background = component.getBackground();
        Color foreground = component.getForeground();
        Rectangle2D rect = new Rectangle2D.Double(bounds.getX(),
                                                 bounds.getY(),
                                                 bounds.getWidth(),
                                                 bounds.getHeight());

        if (background != null) {
            g2.setColor(background);
        }
        g2.fill(rect);

        if (foreground != null) {
            g2.setColor(foreground);
        }
        g2.draw(rect);

    }

    /**
     * Forwards the paint request to the Swing component to paint normally
     * @param g2 The graphics this visual component should pass to the Swing
     *           component
     */
    public void paint(Graphics2D g2) {
        ZCanvas.ZBasicRepaintManager manager = (ZCanvas.ZBasicRepaintManager)RepaintManager.currentManager(component);
        manager.lockRepaint(component);
        component.paint(g2);
        manager.unlockRepaint(component);
    }

    /**
     * Repaint's the specified portion of this visual component
     * Note that the input parameter may be modified as a result of this call.
     * @param repaintBounds The bounding box to repaint within this component
     */
    public void repaint(ZBounds repaintBounds) {
        ZNode[] parentsRef = getParentsReference();
        int numParents = getNumParents();

        for(int i=0; i<numParents; i++) {
            if (i == numParents - 1) {
                parentsRef[i].repaint(repaintBounds);
            }
            else {
                parentsRef[i].repaint((ZBounds)repaintBounds.clone());
            }
        }
    }

    /**
     * Sets the Swing component's bounds to its preferred bounds
     * unless it already is set to its preferred size.  Also
     * updates the visual components copy of these bounds
     */
    public void computeBounds() {
        Dimension d = component.getPreferredSize();
        bounds.setRect(0, 0, d.getWidth(), d.getHeight());
        if (!component.getSize().equals(d)) {
            component.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());
        }
    }

    /**
     * Returns the Swing component that this visual component wraps
     * @return The Swing component that this visual component wraps
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * We need to turn off double buffering of Swing components within
     * Jazz since all components contained within a native container
     * use the same buffer for double buffering.  With normal Swing
     * widgets this is fine, but for Swing components within Jazz this
     * causes problems.  This function recurses the component tree
     * rooted at c, and turns off any double buffering in use.  It also
     * updates the minimum font size based on the font size of c and adds
     * a property change listener to listen for changes to the font.
     * @param c The Component to be recursively unDoubleBuffered
     */
    void init(Component c) {
        Component[] children = null;
        if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }

        if (c.getFont() != null) {
            minFontSize = Math.min(minFontSize,c.getFont().getSize());
        }

        if (children != null) {
            for (int j=0; j<children.length; j++) {
                init(children[j]);
            }
        }

        if (c instanceof JComponent) {
            ((JComponent)c).setDoubleBuffered(false);
            c.addPropertyChangeListener("font",this);
        }
    }

    /**
     * Listens for changes in font on components rooted at this ZSwing
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (component.isAncestorOf((Component)evt.getSource()) &&
            ((Component)evt.getSource()).getFont() != null) {
            minFontSize = Math.min(minFontSize,((Component)evt.getSource()).getFont().getSize());
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(component);
    }
}
