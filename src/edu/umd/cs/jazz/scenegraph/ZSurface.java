/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.awt.image.ImageObserver;
import java.util.*;
import java.io.*;
import javax.swing.JComponent;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZSurface</b> represents the surface on which a camera can project.
 * A surface can be mapped to a Java Component (AWT or Swing), a back
 * buffer or image, or a printer - actually, anything that can generate
 * a Graphics2D - the Java2D render context.
 * The surface is primarily responsible for performing region management.
 * It manages the "dirty" areas of the scenegraph that have to be repainted.
 *
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 * @author  Maria E. Jump
 * @see     ZNode
 */

public class ZSurface implements Printable {
    static public final int RENDER_QUALITY_LOW = 1;
    static public final int RENDER_QUALITY_MEDIUM = 2;
    static public final int RENDER_QUALITY_HIGH = 3;

    static public final int DEFAULT_HALO = 2;
    static public final int DEFAULT_RENDER_QUALITY = RENDER_QUALITY_LOW;

    protected transient ZArea       damagedArea;
    protected boolean               repaintRequest = false;
    protected int                   nonInteractingRenderQuality;
    protected transient ZCamera     camera = null;
    protected boolean               interacting;           // True when user interacting with surface
    protected JComponent            component = null;
    protected int                   renderQuality = DEFAULT_RENDER_QUALITY;
    
    /**
     * Constructs a new Surface. Surfaces are associated with a top-level camera and serves
     * as the surface onto which the camera projects. 
     */
    public ZSurface() {
	this(null);
    }

    /**
     * Constructs a new Surface.  Surfaces are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * If this surface is attached to a window, then its component must be set
     * with {@link #setComponent}
     * @param node The part of the scenegraph this camera sees.
     */
    public ZSurface(ZCamera camera) {
	setCamera(camera);
	damagedArea = new ZArea();
	setRenderQuality(DEFAULT_RENDER_QUALITY);
    }

    /**
     * Constructs a new Surface.  Surfaces are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * @param node The part of the scenegraph this camera sees.
     * @param aComponent The component this surface is connected to
     */
    public ZSurface(ZCamera camera, JComponent aComponent) {
	component = aComponent;
	setCamera(camera);
	damagedArea = new ZArea();
	setRenderQuality(DEFAULT_RENDER_QUALITY);
    }

    /**
     * Specify that future rendering should occur at the specified quality.  Normally, an application
     * should not have to call this method directly, although it can.
     * @param qualityRequested Can be <code>RENDER_QUALITY_LOW</code>,
     * <code>RENDER_QUALITY_MEDIUM</code> or <code>RENDER_QUALITY_HIGH</code>.
     */
    public void setRenderQuality(int qualityRequested) {
	renderQuality = qualityRequested;
    }

    /**
     * Determine the current render quality.
     */
    public int getRenderQuality() {
	return renderQuality;
    }
    
    /**
     * Set the component that this surface is attached to, or null if none.
     * @param aComponent The component this surface is attached to, or null if none.
     */
    public void setComponent(JComponent aComponent) {
	component = aComponent;
    }

    /**
     * Sets the component (i.e., window) that this camera is rendered within.
     */
    public void setCamera(ZCamera cam) {
	if (camera != null) {
	    camera.setSurface(null);
	}
	camera = cam;
	camera.setSurface(this);
    }
    
    /**
       * Get the camera this surface is associated with.
       * @return the camera this surface is associated with.
       */
    public ZCamera getCamera() {return camera;}
    
    /**
     * Applications should call <code>startInteraction</code> when the user is starting a graphical
     * interaction, so Jazz can properly monitor rendering speed.  If the system slows
     * down during an interaction, rendering quality may be reduced to try and improve speed.
     * Note that it is necessary for the application to notify the system when interaction is occurring.
     * If Jazz tried to balance load completely on its own, it might decide that an interaction had
     * finished and that it was ok to re-render in full high-quality while the user was still interacting
     * in which case the user may well have to wait for the render to reach a point where it could
     * be interrupted.
     *
     * @see #endInteraction()
     */
    public void startInteraction() {
	if (!interacting) {
	    interacting = true;
	    nonInteractingRenderQuality = renderQuality;
	    setRenderQuality(RENDER_QUALITY_LOW);
	}
    }
    
    /**
     * Applications should call <code>endInteraction</code> when the user finishes a graphical interaction.
     * This is necessary as Jazz may have rendered some things at low quality in which case they
     * will have to be re-rendered at full quality.
     *
     * @see #startInteraction()
     */
    public void endInteraction() {
	interacting = false;
	setRenderQuality(nonInteractingRenderQuality);
	repaint();
    }
    
    /**
       * Determine if the user interacting with the surface
       * @return Value of interacting.
       */
    public boolean isInteracting() {return interacting;}
    
    /**
       * Specifies that the user is interacting with the surface.
       * @param v  Value to assign to interacting.
       */
    public void setInteracting(boolean  v) {this.interacting = v;}

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	return super.toString() + "\nCamera's " + camera;
    }
   
    /**
     * Notifies this surface that the specified bounds have changed, and that portion
     * of the surface should be repainted at the next restore.
     * Damaged area is accumulated in global coordinates.
     * @param bounds The bounds that need to be redrawn (in global coordinates).
     */
    public void damage(ZBounds bounds) {
	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZSurface.damage: bounds = " + bounds);
	}
	
	damagedArea.add(bounds);
	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZSurface.damage: damagedArea = " + damagedArea.getBounds());
	}
    }

    /**
     * A utility function to repaint the entire component.
     * This results in request being queued for the entire component to be painted.
     * The paint does not happen immediately, but instead occurs when the queued
     * request comes to the head of the Swing event queue and is fired.
     */
    public void repaint() {
	if (camera != null) {
	    camera.damage();
	    restore();
	}
    }

    /**
     * This queues a request to restore (i.e. repaint) the damaged area of the scenegraph.
     * Only the portion of the scene that has changed (as specified by calls to damage)
     * will be repainted.
     * The paint does not happen immediately, but instead occurs when the queued
     * request comes to the head of the Swing event queue and is fired.
     * In this case, multiple queued restore commands are automatically combined
     * and result in a single paint call.
     * @see #damage(ZBounds)
     */
    public void restore() {
	restore(false);
    }

    /**
     * This causes a restore (i.e. repaint) of the damaged area of the scenegraph.
     * Only the portion of the scene that has changed (as specified by calls to damage)
     * will be repainted.  The caller can specify whether the restoration happens
     * immediately, or if a request is queued.  If the request is queued, then
     * the paint does not happen immediately, but instead occurs when the queued
     * request comes to the head of the Swing event queue and is fired.  
     * In this case, multiple queued restore commands are automatically combined
     * and result in a single paint call.
     * @param restoreImmediately True to specify that the restoration happens immediately, or false to queue a request.
     * @see #damage(ZBounds)
     */
    public void restore(boolean restoreImmediately) {
	if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
	    System.out.println("ZSurface restoring");
	}
	
	if (damagedArea.isEmpty()) {
	    if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
		System.out.println("ZSurface.restore: Empty Area - nothing painted");
	    }
	    return;
	}

	if (component != null) {
				// Transform global scene coords to window coordinates
	    ZTransform viewTransform = camera.getViewTransform();
	    Rectangle2D rect2D = new Rectangle2D.Float();
	    viewTransform.transform(damagedArea.getBounds2D(), rect2D);
				// We need to round conservatively so the damaged area is big enough
	    Rectangle rect = new Rectangle((int)(rect2D.getX() - 1.0f), 
					   (int)(rect2D.getY() - 1.0f), 
					   (int)(rect2D.getWidth() + 3.0f), 
					   (int)(rect2D.getHeight() + 3.0f));
	    if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
		System.out.println("ZSurface.restore: paint area bounds: " + damagedArea.getBounds());
		System.out.println("ZSurface.restore: window area bounds: " + rect);
	    }

	    if (restoreImmediately) {
		component.paintImmediately(rect);
	    } else {
		component.repaint(rect);
	    }
	    damagedArea.reset();
	}
	
	if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
	    System.out.println();
	}
    }

     /**
     * Paints the camera this surface sees.
     * @param renderContext The graphics context to use for rendering.
     */
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;

				// Transform window coordinates clip region to global scene coords
	ZTransform inverseViewTransform = camera.getInverseViewTransform();
	Rectangle2D rect2D = new Rectangle2D.Float();
	Rectangle rectSrc = g2.getClipBounds();
	if (rectSrc == null) {
	    rectSrc = camera.getGlobalBounds().getBounds();
	}
	inverseViewTransform.transform(rectSrc, rect2D);
	ZArea paintArea = new ZArea(rect2D);

	if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
	    System.out.println("ZSurface.paint(ZRenderContext): clip bounds = " + g2.getClipBounds());
	    System.out.println("ZSurface.paint(ZRenderContext): paint area = " + paintArea);
	}
       
	ZDebug.clearPaintCount(g2);

	ZRenderContext rc = new ZRenderContext(g2, paintArea, this, renderQuality);
	camera.paint(rc);

	if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
	    System.out.println("ZSurface.paint: Rendered " + ZDebug.getPaintCount(g2) + " objects this pass");
	}
    }

    /**
     * Returns the first object intersecting the specified rectangle within DEFAULT_HALO pixels
     * as searched in reverse (front-to-back) order, or null if no objects satisfy criteria.
     * @param x X-coord of pick point in window coordinates.
     * @param y Y-coord of pick point in window coordinates.
     * @return The picked object.
     */
    public ZNode pick(int x, int y) {
	return pick(x, y, DEFAULT_HALO);
    }

    /**
     * Returns the first object intersecting the specified rectangle within halo pixels
     * as searched in reverse (front-to-back) order, or null if no objects satisfy criteria.
     * @param x X-coord of pick point in window coordinates.
     * @param y Y-coord of pick point in window coordinates.
     * @param halo The amount the point can miss an object and still pick it
     * @return The picked object.
     */
    public ZNode pick(int x, int y, int halo) {
	Date startTime = null, pickTime = null;
	if (ZDebug.getDebug() == ZDebug.DEBUG_TIME) {
	    startTime = new Date();
	}

	ZBounds rect = new ZBounds(x-halo, y-halo, halo+halo, halo+halo);
	camera.getTransform().inverseTransform(rect, rect);
	ZNode pickNode = camera.pick(rect);

	if (ZDebug.getDebug() == ZDebug.DEBUG_TIME) {
	    pickTime = new Date();
	    System.out.println("ZSurface.pick: pickTime = " + (pickTime.getTime() - startTime.getTime()));
	}
	return pickNode;
    }
    
    /**
     * Constructs a new PrinterJob, allows the user to select which printer
     * to print to, and prints the surface.
     */
    public void printSurface() {
	PrinterJob printJob = PrinterJob.getPrinterJob();

				// Set up a new book so we can specify the numbe of pages (1)
	PageFormat pageFormat = printJob.defaultPage();
	Book book = new Book();
	book.append(this, pageFormat);
	printJob.setPageable(book);

	if (printJob.printDialog()) {   // Open up a print dialog
	    try {
		printJob.print();	// Then, start the print
	    } catch (Exception e) {
		System.out.println("Error Printing");
		e.printStackTrace();
	    }
	}
    }

    /**
     * Prints the surface into the specified Graphics context in the specified format. A PrinterJob
     * calls the printable interface to request that a surface be rendered into the context specified
     * by the graphics. The format of the page to be drawn is specified by PageFormat. The zero based
     * index of the requested page is specified by pageIndex. If the requested page does not exist then
     * this method returns NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned. If the printable object aborts
     * the print job then it throws a PrinterException
     * @param graphics    the context into which the page is drawn
     * @param pageFormat  the size and orientation of the page being drawn
     * @param pageIndex   the zero based index of the page to be drawn
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
	if (pageIndex != 0) {
	    return NO_SUCH_PAGE;
	}
	Graphics2D g2 = (Graphics2D)graphics;

				// translate the graphics to the printable area on the page
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				// scale the graphics to the printable area on the page
	double scaleFactor = pageFormat.getImageableWidth()/camera.getSize().getWidth();
	if (pageFormat.getImageableHeight()/camera.getSize().getHeight() < scaleFactor) {
	    scaleFactor = pageFormat.getImageableHeight()/camera.getSize().getHeight();
	}
	g2.scale(scaleFactor, scaleFactor);

				// paint onto the printer graphics
	ZRenderContext rc = new ZRenderContext(g2, new ZArea(camera.getViewBounds()), this, RENDER_QUALITY_HIGH);
	camera.paint(rc);

	return PAGE_EXISTS;
    }    
}
