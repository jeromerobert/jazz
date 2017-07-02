/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.awt.image.renderable.RenderContext;
import java.util.*;
import java.io.*;
import javax.swing.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ZCanvas</b> is a simple Swing component that can be used to render
 * onto for Jazz.  It extends JComponent, and overrides the appropriate
 * methods so that whenever Java requests that this widget gets redrawn,
 * the requests are forwarded on to Jazz to render appropriately.  It also
 * defines a very simple Jazz scenegraph consisting of a root, a camera,
 * and one node.  Finally, it supports capturing the current camera view
 * onto an Image (i.e., a screengrab).  It also supports the use of
 * Swing components within Jazz by forwarding mouse, repaint, and revalidate
 * events.
 * <p>
 * ZCanvas defines basic event handlers for panning and zooming with the keyboard and mouse
 * which can be disabled with @link{#disableEventHandlers}.
 *
 * @author Benjamin B. Bederson
 * @author Lance E. Good
 */
public class ZCanvas extends JComponent implements Serializable {
    /**
     * Used as a hashtable key to indicate that a JComponent is
     * functioning as a place holder for Swing components displayed in
     * a ZCanvas
     */
    static final String SWING_WRAPPER_KEY = "Swing Wrapper";

				// The root of the scenegraph
    private ZRoot	    root;
				// The camera in the scenegraph
    private ZCamera         camera;
				// The camera node in the scenegraph
    private ZVisualLeaf     cameraNode;
				// The surface associated with the component
    private ZDrawingSurface surface;
				// The single node that camera looks onto.  It is considered to
				// be the "layer" because many applications will put content
				// under this node which can then be hidden or revealed like a layer.
    private ZLayerGroup            layer;

    private Cursor cursor = getCursor();
				// A visible though not rendered JComponent to which Swing components are
				// added to function properly in the Jazz Scenegraph
    private JComponent swingWrapper;
				// True if node events are processed
    private boolean enableNodeEvents = true;
				// The current node under the pointer
    private ZNode currentNode = null;
				// The current path to the current node for the current event
    private ZSceneGraphPath currentPath = null;
				// The path grabbed for a press/drag/release sequence
    private ZSceneGraphPath grabPath = null;
				// Listeners used to be sure there is at least one listener of each type
    private MouseAdapter emptyMouseListener = null;
    private MouseMotionAdapter emptyMouseMotionListener = null;


    /**
     * Mouse Listener for ZNodes that have visual components
     */
    ZNodeEventHandler nodeListener;

    /**
     * The event handler that supports events for Swing Visual Components
     */
    protected ZSwingEventHandler          swingEventHandler;
    
    /**
     * The event handler that supports panning
     */
    protected ZEventHandler          panEventHandler;

    /**
     * The event handler that supports zooming
     */
    protected ZEventHandler          zoomEventHandler;

    /**
     * The event handler that supports key events
     */
    protected ZEventHandler          keyEventHandler;

    /**
     * The default constructor for a ZCanvas.  This creates a simple
     * scenegraph with a root, camera, surface, and layer.  These 4 scenegraph
     * elements are accessible to the application through get methods.
     * Also adds the necessary structure to facilitate the focus, repaint,
     * and event handling for Swing components within Jazz
     * @see #getRoot()
     * @see #getDrawingSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZCanvas() {
	root = new ZRoot();
	camera = new ZCamera();
	cameraNode = new ZVisualLeaf(camera);
	surface = new ZDrawingSurface(camera, cameraNode, this);
	layer = new ZLayerGroup();
	root.addChild(layer);
	root.addChild(cameraNode);
	camera.addLayer(layer);

	init();
    }

    /**
     * A constructor for a ZCanvas that uses an existing scenegraph.
     * This creates a new camera and surface.  The camera is inserted into
     * the scenegraph under the root, and the specified layer is added to
     * the camera's paint start point list.  The scenegraph
     * elements are accessible to the application through get methods.
     * Also adds the necessary structure to facilitate the focus, repaint,
     * and event handling for Swing components within Jazz
     * @param aRoot The existing root of the scenegraph this component is attached to
     * @param layer The existing layer node of the scenegraph that this component's camera looks onto
     * @see #getRoot()
     * @see #getDrawingSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZCanvas(ZRoot aRoot, ZLayerGroup layer) {
	root = aRoot;
	camera = new ZCamera();
	cameraNode = new ZVisualLeaf(camera);
	surface = new ZDrawingSurface(camera, cameraNode, this);
	root.addChild(cameraNode);
	camera.addLayer(layer);

	init();
    }

    /**
     * Internal method to support initialization of a ZCanvas.
     */
    protected void init() {
				// Add support for Swing widgets
	swingWrapper = new JComponent() {
	    public boolean isValidateRoot() {
		return true;
	    }
	};
	swingWrapper.putClientProperty(SWING_WRAPPER_KEY, new Object());
	swingWrapper.setSize(0, 0);
	swingWrapper.setVisible(true);
	add(swingWrapper);

	nodeListener = new ZNodeEventHandler(this);

	setEnableNodeEvents(true);
	
	if (!(RepaintManager.currentManager(this) instanceof ZBasicRepaintManager)) {
	    RepaintManager.setCurrentManager(new ZBasicRepaintManager());
	}

	setNavEventHandlersActive(true);    // Create some basic event handlers for panning and zooming
	setSwingEventHandlersActive(true);  // Create the event handler for swing events
       	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));   // Create the Swing event handler

    }

    /**
     * This renders the Jazz scene attached to this component by passing on the Swing paint request
     * to the underlying Jazz surface.
     * @param g The graphics to be painted onto
     */
    public void paintComponent(Graphics g) {
	surface.paint(g);
    }

    /**
     * This captures changes in the component's bounds so the underlying Jazz camera can
     * be updated to mirror bounds change.
     * @param x The X-coord of the top-left corner of the component
     * @param y The Y-coord of the top-left corner of the component
     * @param width The width of the component
     * @param height The Height of the component
     */
    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(x, y, w, h);
	Rectangle bounds = getBounds();
	camera.setBounds(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
    }

    /**
     * Sets the background color of this component.
     * Actually - this is implemented by changing the fill color of the
     * camera associated with this component since the camera controls
     * the rendering onto this component.
     * @param background The new color to use for this component's background
     */
    public void setBackground(Color background) {
	super.setBackground(background);
	camera.setFillColor(background);
    }

    /**
     * Sets the surface.
     * @param surface the surface
     */
    public void setDrawingSurface(ZDrawingSurface aSurface) {
	surface = aSurface;
    }

    /**
     * Return the surface.
     * @return the surface
     */
    public ZDrawingSurface getDrawingSurface() {
	return surface;
    }

    /**
     * Sets the camera.
     * @param camera the camera
     */
    public void setCamera(ZCamera aCamera) {
	camera = aCamera;
	Rectangle bounds = getBounds();
	camera.setBounds(0, 0, (int)bounds.getWidth(), (int)bounds.getHeight());
    }

    /**
     * Return the camera associated with the primary surface.
     * @return the camera
     */
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Return the camera's node associated with the primary surface.
     * @return the camera's node
     */
    public ZNode getCameraNode() {
	return cameraNode;
    }

    /**
     * Sets the root.
     * @param root the root
     */
    public void setRoot(ZRoot aRoot) {
	root = aRoot;
    }

    /**
     * Return the root of the scenegraph.
     * @return the root
     */
    public ZRoot getRoot() {
	return root;
    }

    /**
     * Sets the layer.
     * @param layer the layer
     */
    public void setLayer(ZLayerGroup aLayer) {
	layer = aLayer;
    }

    /**
     * Return the "layer".  That is, the single node that
     * the camera looks onto to start.
     * @return the node
     */
    public ZLayerGroup getLayer() {
	return layer;
    }

    public boolean isFocusTraversable() {
	return true;
    }

    /**
     * Generate a copy of the view in the current camera scaled so that the aspect ratio
     * of the screen is maintained, and the larger dimension is scaled to
     * match the specified parameter.
     * @return An image of the camera
     */
    public Image getScreenImage(int maxDim) {
	int w, h;

	if (getSize().getWidth() > getSize().getHeight()) {
	    w = maxDim;
	    h = (int)(maxDim * getSize().getHeight() / getSize().getWidth());
	} else {
	    h = maxDim;
	    w = (int)(maxDim * getSize().getWidth() / getSize().getHeight());
	}
	return getScreenImage(w, h);
    }

    /**
     * Generate a copy of the current camera scaled to the specified dimensions.
     * @param w  Width of the image
     * @param h  Height of the image
     * @return An image of the camera
     */
    public Image getScreenImage(int w, int h) {
				// We create an image of the right size and get its graphics
	Image screenImage = createImage(w, h);
	Graphics2D g2 = (Graphics2D)screenImage.getGraphics();
				// Then, we compute the transform that will map the component into the image
	float dsx = (float)(w / getSize().getWidth());
	float dsy = (float)(h / getSize().getHeight());
	AffineTransform at = AffineTransform.getScaleInstance(dsx, dsy);
	g2.setTransform(at);
				// Finally, we paint onto the image
	surface.paint(g2);
				// And we're done
	return screenImage;
    }

    /**
     * Return the pan event handler.
     * @return the pan event handler.
     */
    public ZEventHandler getPanEventHandler() {
	return panEventHandler;
    }

    /**
     * Return the zoom event handler.
     * *@eturn the zoom event handler.
     */
    public ZEventHandler getZoomEventHandler() {
	return zoomEventHandler;
    }


    public void setSwingEventHandlersActive(boolean active) {
	if (active) {
	    if (swingEventHandler == null) {
		swingEventHandler = new ZSwingEventHandler(this, cameraNode);
	    }
	    swingEventHandler.setActive(true);
	}
	else {
	    if (swingEventHandler != null) {
		swingEventHandler.setActive(false);
	    }
	}
    }
    
    /**
     * Control whether event handlers are active or not for this ZCanvas.
     * This controls basic panning and zooming event handlers for the mouse,
     * so that the left button pans, and the right button zooms.
     */
    public void setNavEventHandlersActive(boolean active) {
	if (active) {
	    boolean swingActive = false;

	    // Activate event handlers
	    if (panEventHandler == null) {
		panEventHandler = new ZPanEventHandler(cameraNode);
	    }
	    if (zoomEventHandler == null) {
		zoomEventHandler = new ZoomEventHandler(cameraNode);
	    }
	    
	    if (swingEventHandler != null && swingEventHandler.isActive()) {
		swingEventHandler.setActive(false);
		swingActive = true;
	    }
	    
	    panEventHandler.setActive(true);
	    zoomEventHandler.setActive(true);

	    if (swingEventHandler != null && swingActive) {
		swingEventHandler.setActive(true);
	    }
	    
	} else {
				// Deactivate event handlers
	    if (panEventHandler != null) {
		panEventHandler.setActive(false);
	    }
	    if (zoomEventHandler != null) {
		zoomEventHandler.setActive(false);
	    }
	}
    }
    
    /**
     * Returns the component to which Swing components are added to function
     * properly in Jazz.  Only public to give access to ZSwing.
     * Should not be used otherwise.
     * @return The component to which Swing components are added to function in Jazz
     */
    public JComponent getSwingWrapper() {
	return swingWrapper;
    }

    /**
     * Specify if Jazz node event handlers should be invoked.
     * NOTE:  This should only be called if Jazz Events are not needed
     * @param enable True if node event handlers should be invoked.
     */
    public void setEnableNodeEvents(boolean enable) {
	enableNodeEvents = enable;
				// Need to add empty listeners so that events get processed
				// We define our event handlers by over-riding processMouseEvent 
				// without actually adding listeners.  But, processMouseEvent
				// doesn't get called unless there is at least one listener
	if (enable) {
	    if (emptyMouseListener == null) {
		emptyMouseListener = new MouseAdapter() {};
		emptyMouseMotionListener = new MouseMotionAdapter() {};
		addMouseListener(emptyMouseListener);
		addMouseMotionListener(emptyMouseMotionListener);
	    }
	} else {
	    if (emptyMouseListener != null) {
		removeMouseListener(emptyMouseListener);
		removeMouseMotionListener(emptyMouseMotionListener);
		emptyMouseListener = null;
		emptyMouseMotionListener = null;
	    }
	}
    }

    /**
     * Determine if Jazz node event handlers should be invoked.
     * @return True if Node event handlers should be invoked.
     */
    public final boolean getEnableNodeEvents() {
	return enableNodeEvents;
    }

    /**
     * Internal method that overrides java.awt.Component.processMouseEvent
     * to pass the mouse events to our listeners first, and
     * then on to the other listeners.  This allows Jazz to support consuming
     * the events, and only passing the events on if they are not consumed.
     * Mouse events get dispatched with the following priority (assuming
     * swing and node events are enabled.)
     * <ol>
     * <li> If there is a Swing widget, then that gets the mouse event
     * <li> If the event is not consumed, and there is a node event listener, then that gets the event
     * <li> If the event is not consumed, then any other component event listeners are processed.
     * </ol>
     * @param e The MouseEvent to process
     * @see #setEnableNodeEvents
     */
    public void processMouseEvent(MouseEvent e) {
	int id = e.getID();
				// Determine the node under the pointer
	if (enableNodeEvents) {
	    currentPath = getDrawingSurface().pick(e.getX(), e.getY());
	    currentNode = currentPath.getNode();
	}

				// First, check to see if it should go to a specific node
	if (enableNodeEvents) {
	    if (!e.isConsumed()) {
		switch (id) {
		case MouseEvent.MOUSE_PRESSED:
		    nodeListener.mousePressed(e);
		    break;
		case MouseEvent.MOUSE_RELEASED:
		    nodeListener.mouseReleased(e);
		    break;
		case MouseEvent.MOUSE_CLICKED:
		    nodeListener.mouseClicked(e);
		    break;
		}
	    }
	}
				// Else, pass it on to other event handlers
	if (!e.isConsumed()) {
	    super.processMouseEvent(e);
	}
    }

    /**
     * Internal method that overrides java.awt.Component.processMouseEvent
     * to pass the mouse events to our listeners first, and
     * then on to the other listeners.  This allows Jazz to support consuming
     * the events, and only passing the events on if they are not consumed.
     * Mouse events get dispatched with the following priority (assuming
     * swing and node events are enabled.)
     * <ol>
     * <li> If there is a Swing widget, then that gets the mouse event
     * <li> If the event is not consumed, and there is a node event listener, then that gets the event
     * <li> If the event is not consumed, then any other component event listeners are processed.
     * </ol>
     * @param e The MouseEvent to process
     * @see #setEnableNodeEvents
     */
    protected void processMouseMotionEvent(MouseEvent e) {
	int id = e.getID();

	if (enableNodeEvents) {
	    currentPath = getDrawingSurface().pick(e.getX(), e.getY());
	    currentNode = currentPath.getNode();
	}

				// First, check to see if it should go to a specific node
	if (enableNodeEvents) {
	    if (!e.isConsumed()) {
		switch (id) {
		case MouseEvent.MOUSE_MOVED:
		    nodeListener.mouseMoved(e);
		    break;
		case MouseEvent.MOUSE_DRAGGED:
		    nodeListener.mouseDragged(e);
		    break;
		}
	    }
	}
				// Else, pass it on to other event handlers
	if (!e.isConsumed()) {
	    super.processMouseMotionEvent(e);
	}
    }

    /**
     * Sets the cursor for this ZCanvas
     * @param c The new cursor
     */
    public void setCursor(Cursor c) {
	setCursor(c,true);
    }

    /**
     * Sets the cursor for this ZCanvas.  If realSet is
     * true then the cursor that displays when the mouse is over the
     * ZCanvas is set as well as the currently displayed cursor.
     * If realSet is false then only the currently displayed cursor is changed
     * to indicate that the mouse is over a deeper component within the
     * ZCanvas.
     * @param c The new cursor
     * @param realSet true - The ZCanvas cursor and current cursor set
     *                false - Only the current cursor set
     */
    public void setCursor(Cursor c, boolean realSet) {
	if (realSet) {
	    cursor = c;
	}
	super.setCursor(c);
    }

    /**
     * Sets the current cursor to the ZCanvas's cursor
     */
    public void resetCursor() {
	setCursor(cursor, false);
    }


    /**
     * Event handler to capture MousePressed, MouseReleased, MouseMoved,
     * MouseClicked, and MouseDragged events on Jazz nodes, and pass them
     * on to the node.
     */
    class ZNodeEventHandler {
	int grabIndex = 0;
	ZNode grabNode = null;
	ZNode prevNode = null;
	AffineTransform tmpTransform = new AffineTransform();

	// Constructor that adds the mouse listeners to the ZCanvas
	ZNodeEventHandler(ZCanvas zbc) {
	}

	// Internal method to generate exit/enter events when the current node may have changed
	protected void updateCurrentNode(MouseEvent e) {
	    if (currentNode != prevNode) {
		if (prevNode != null) {
		    try {
			prevNode.fireMouseEvent(new ZMouseEvent(MouseEvent.MOUSE_EXITED, prevNode, e, currentPath));
		    } catch (ZNodeNotFoundException exc) {
				// The current node was probably deleted in an event handler,
				// so we won't give it any more events
		    }
		}
		if (currentNode != null) {
		    currentNode.fireMouseEvent(new ZMouseEvent(MouseEvent.MOUSE_ENTERED, currentNode, e, currentPath));
		}
		prevNode = currentNode;
	    }
	}

	// Send the event to the grabbed node, and percolate the event up the path
	// as long as the event hasn't been consumed
	private void fireEvent(int id, MouseEvent e, ZNode fireNode, int fireIndex, ZSceneGraphPath firePath) {
	    ZNode node = fireNode;
	    ZSceneGraphObject obj;
	    int i = fireIndex - 1;
	    boolean cameraNodeFired = false;

	    do {
				// If the node has a listener, then fire the event
                ZMouseEvent zme = new ZMouseEvent(id, node, e, firePath);
		node.fireMouseEvent(zme);
		if (zme.isConsumed()) {
				// If the event is consumed, then we are done here
		    e.consume();
		    break;
		}
		if (node == firePath.getTopCameraNode()) {
		    cameraNodeFired = true;
		}
				// Else, percolate up the path, looking for other nodes
		node = null;
		while (i >= 0) {
		    obj = firePath.getParent(i);
		    i--;
		    if (obj instanceof ZNode) {
			node = (ZNode)obj;
		        if (node.hasMouseListener()) {
			    break;
			} else {
			    node = null;
			}
		    }
		}
		if (!cameraNodeFired && (node == null)) {
		    cameraNodeFired = true;
		    if (firePath.getObject() != null) {
			node = firePath.getTopCameraNode();
		    }
		}
	    } while (node != null);
	}

	// Forwards mouseMoved events to nodes in Jazz,
	// if any should receive the event
	public void mouseMoved(MouseEvent e) {
	    updateCurrentNode(e);

	    if ((currentNode == null) || !currentNode.hasMouseListener()) {
		currentNode = currentPath.getTopCameraNode();
	    }

	    fireEvent(MouseEvent.MOUSE_MOVED, e, currentNode, currentPath.getNumParents()-1, currentPath);
	}

	// Forwards mousePressed events to nodes in Jazz,
	// if any should receive the event
	public void mousePressed(MouseEvent e) {
	    updateCurrentNode(e);   // Event handlers could have changed the scenegraph, so check for current node
	    grabNode = currentNode;
	    grabPath = currentPath;
				// If the clicked on node doesn't have a mouse or mousemotion listener, then percolate
				// the event up to its parent.
            int i = grabPath.getNumParents() - 1;
	    ZSceneGraphObject obj;
	    while ((i >= 0) && (grabNode != null) && !grabNode.hasMouseListener()) {
		obj = grabPath.getParent(i);
		if (obj instanceof ZNode) {
		    grabNode = (ZNode)obj;
		}
		i--;
	    }
	    grabIndex = i;
	    if ((grabNode == null) || !grabNode.hasMouseListener()) {
		grabNode = grabPath.getTopCameraNode();
	    }

	    fireEvent(MouseEvent.MOUSE_PRESSED, e, grabNode, grabIndex, grabPath);
	}

	// Forwards mouseDragged events nodes in Jazz,
	// if any should receive the event
	public void mouseDragged(MouseEvent e) {
				// We need to set the transform using the objects on the grab path.
				// We can't use the current transform from the current path because the path may be different.
				// So instead, we recompute the transform using the objects along the path.
	    if (grabPath.getObject() == null) {
				// Path empty, so just use camera transform
		tmpTransform = camera.getViewTransform();
	    } else {
		tmpTransform.setToIdentity();
		int n = grabPath.getNumParents();
		ZSceneGraphObject obj;
		for (int i=0; i<n; i++) {
		    obj = grabPath.getParent(i);
		    if (obj instanceof ZCamera) {
			tmpTransform.concatenate(((ZCamera)obj).getViewTransform());
		    } else if (obj instanceof ZTransformGroup) {
			tmpTransform.concatenate(((ZTransformGroup)obj).getTransform());
		    }
		}
	    }
	    grabPath.setTransform(tmpTransform);

	    fireEvent(MouseEvent.MOUSE_DRAGGED, e, grabNode, grabIndex, grabPath);
	}

	// Forwards mouseReleased events to nodes in Jazz,
	// if any should receive the event
	public void mouseReleased(MouseEvent e) {
	    updateCurrentNode(e);   // Event handlers could have changed scenegraph, check for current node
	    grabPath.setTransform(currentPath.getTransform());

	    fireEvent(MouseEvent.MOUSE_RELEASED, e, grabNode, grabIndex, grabPath);
	}

	// Forwards mouseClicked events to nodes in Jazz,
	// if any should receive the event
	public void mouseClicked(MouseEvent e) {
	    if (grabNode != null) {
		ZMouseEvent zme = new ZMouseEvent(MouseEvent.MOUSE_CLICKED, grabNode, e, currentPath);
		grabNode.fireMouseEvent(zme);
		if (zme.isConsumed()) {
		    e.consume();
		}
	    }
	}

	// Implemented within mouseMoved
	public void mouseExited(MouseEvent e) {
	}

	// Implemented within mouseMoved
	public void mouseEntered(MouseEvent e) {
	}
    }

    /**
     * This is an internal class used by Jazz to support Swing components
     * in Jazz.  This should not be instantiated, though all the public
     * methods of javax.swing.RepaintManager may still be called and
     * perform in the expected manner.
     *
     * ZBasicRepaint Manager is an extension of RepaintManager that traps
     * those repaints called by the Swing components that have been added
     * to the ZCanvas and passes these repaints to the
     * SwingVisualComponent rather than up the component hierarchy as
     * usually happens.
     *
     * Also traps revalidate calls made by the Swing components added
     * to the ZCanvas to reshape the applicable Visual Component.
     *
     * Also keeps a list of ZSwings that are painting.  This
     * disables repaint until the component has finished painting.  This is
     * to address a problem introduced by Swing's CellRendererPane which is
     * itself a work-around.  The problem is that JTable's, JTree's, and
     * JList's cell renderers need to be validated before repaint.  Since
     * we have to repaint the entire Swing component hierarchy (in the case
     * of a Swing component group used as a Jazz visual component).  This
     * causes an infinite loop.  So we introduce the restriction that no
     * repaints can be triggered by a call to paint.
     */
    public class ZBasicRepaintManager extends RepaintManager {
	// The components that are currently painting
	// This needs to be a vector for thread safety
	Vector paintingComponents = new Vector();

	/**
	 * Locks repaint for a particular (Swing) component displayed by
	 * ZCanvas
	 * @param c The component for which the repaint is to be locked
	 */
	public void lockRepaint(JComponent c) {
	    paintingComponents.addElement(c);
	}

	/**
	 * Unlocks repaint for a particular (Swing) component displayed by
	 * ZCanvas
	 * @param c The component for which the repaint is to be unlocked
	 */
	public void unlockRepaint(JComponent c) {
	    synchronized (paintingComponents) {
		paintingComponents.removeElementAt(paintingComponents.lastIndexOf(c));
	    }
	}

	/**
	 * Returns true if repaint is currently locked for a component and
	 * false otherwise
	 * @param c The component for which the repaint status is desired
	 * @return Whether the component is currently painting
	 */
	public boolean isPainting(JComponent c) {
	    return paintingComponents.contains(c);
	}

	/**
	 * This is the method "repaint" now calls in the Swing components.
	 * Overridden to capture repaint calls from those Swing components
	 * which are being used as Jazz visual components and to call the Jazz
	 * repaint mechanism rather than the traditional Component hierarchy
	 * repaint mechanism.  Otherwise, behaves like the superclass.
	 * @param c Component to be repainted
	 * @param x X coordinate of the dirty region in the component
	 * @param y Y coordinate of the dirty region in the component
	 * @param w Width of the dirty region in the component
	 * @param h Height of the dirty region in the component
	 */
	public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
	    boolean captureRepaint = false;
	    JComponent capturedComponent = null;
	    int captureX = x, captureY = y;

	    // We have to check to see if the ZCanvas
	    // (ie. the SwingWrapper) is in the components ancestry.  If so,
	    // we will want to capture that repaint.  However, we also will
	    // need to translate the repaint request since the component may
	    // be offset inside another component.
	    for(Component comp = c; comp != null && comp.isLightweight() && !captureRepaint; comp = comp.getParent()) {

		if (comp.getParent() != null &&
		    comp.getParent() instanceof JComponent &&
		    ((JComponent)comp.getParent()).getClientProperty(SWING_WRAPPER_KEY) != null) {
		    if (comp instanceof JComponent) {
			captureRepaint = true;
			capturedComponent = (JComponent)comp;
		    }
		}
		else {
		    // Adds to the offset since the component is nested
		    captureX += comp.getLocation().getX();
		    captureY += comp.getLocation().getY();
		}

	    }

	    // Now we check to see if we should capture the repaint and act
	    // accordingly
	    if (captureRepaint) {
		if (!isPainting(capturedComponent)) {

		    ZSwing vis = (ZSwing)capturedComponent.getClientProperty(ZSwing.VISUAL_COMPONENT_KEY);

		    if (vis != null) {
			vis.repaint(new ZBounds((float)captureX,(float)captureY,(float)w,(float)h));
		    }

		}
	    }
	    else {
		super.addDirtyRegion(c,x,y,w,h);
	    }
	}

	/**
	 * This is the method "revalidate" calls in the Swing components.
	 * Overridden to capture revalidate calls from those Swing components
	 * being used as Jazz visual components and to update Jazz's visual
	 * component wrapper bounds (these are stored separately from the
	 * Swing component). Otherwise, behaves like the superclass.
	 * @param invalidComponent The Swing component that needs validation
	 */
	public synchronized void addInvalidComponent(JComponent invalidComponent) {
	    final JComponent capturedComponent = invalidComponent;

	    if (capturedComponent.getParent() != null &&
		capturedComponent.getParent() instanceof JComponent &&
		((JComponent)capturedComponent.getParent()).getClientProperty(SWING_WRAPPER_KEY) != null) {

		Runnable validater = new Runnable() {
		    public void run() {
			capturedComponent.validate();
			ZSwing swing = (ZSwing)capturedComponent.getClientProperty(ZSwing.VISUAL_COMPONENT_KEY);
			swing.reshape();
		    }
		};
		SwingUtilities.invokeLater(validater);
	    }
	    else {
		super.addInvalidComponent(invalidComponent);
	    }
	}
    }

}
