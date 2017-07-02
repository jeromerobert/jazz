/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.awt.image.ImageObserver;
import java.util.*;
import java.io.*;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZDrawingSurface</b> represents the surface on which a camera can project.
 * A surface can be mapped to a Java Component (AWT or Swing), a back
 * buffer or image, or a printer - actually, anything that can generate
 * a Graphics2D - the Java2D render context.
 *
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 * @author  Maria E. Jump
 * @see     ZNode
 */

public class ZDrawingSurface implements Printable, Serializable {
				// Default values
    static public final int RENDER_QUALITY_LOW = 1;
    static public final int RENDER_QUALITY_MEDIUM = 2;
    static public final int RENDER_QUALITY_HIGH = 3;

    static public final int DEFAULT_HALO = 2;
    static public final int DEFAULT_RENDER_QUALITY = RENDER_QUALITY_LOW;

				// The camera this surface is associated with.
    private transient ZCamera camera = null;

				// The camera this surface is associated with.
    private transient ZNode   cameraNode = null;

				// The optional component this surface is associated with.
    private JComponent            component = null;

				// True when user interacting with surface
    private boolean               interacting;

				// The render quality to use when painting this surface.
    private int                   renderQuality = DEFAULT_RENDER_QUALITY;

				// The render quality when not interacting with surface.
    private int                   nonInteractingRenderQuality;

				// Rectangle used for calculating repaint region.
				// Defined once per surface, and reused for efficiency.
    private Rectangle             tmpRepaintRect = null;

    //****************************************************************************
    //
    //                 Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new Surface. Surfaces are associated with a top-level camera and serve
     * as the surface onto which the camera projects.
     */
    public ZDrawingSurface() {
	this(null, null);
    }

    /**
     * Constructs a new Surface.  Surfaces are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * If this surface is attached to a window, then its component must be set
     * with {@link #setComponent}
     * @param node The part of the scenegraph this camera sees.
     * @param cameraNode The node the camera is attached to
     */
    public ZDrawingSurface(ZCamera camera, ZNode cameraNode) {
	tmpRepaintRect = new Rectangle();
	setCamera(camera, cameraNode);
	setRenderQuality(DEFAULT_RENDER_QUALITY);
    }

    /**
     * Constructs a new Surface.  Surfaces are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * @param node The part of the scenegraph this camera sees.
     * @param cameraNode The node the camera is attached to
     * @param aComponent The component this surface is connected to
     */
    public ZDrawingSurface(ZCamera camera, ZNode cameraNode, JComponent aComponent) {
	tmpRepaintRect = new Rectangle();
	component = aComponent;
	setCamera(camera, cameraNode);
	setRenderQuality(DEFAULT_RENDER_QUALITY);
    }

    /**
     * Specify that future rendering should occur at the specified quality.  Normally, an application
     * should not have to call this method directly, although it can.
     * @param qualityRequested Can be <code>RENDER_QUALITY_LOW</code>,
     * <code>RENDER_QUALITY_MEDIUM</code> or <code>RENDER_QUALITY_HIGH</code>.
     */
    public void setRenderQuality(int qualityRequested) {
	if (qualityRequested > renderQuality) {
	    renderQuality = qualityRequested;
	    repaint();
	} else {
	    renderQuality = qualityRequested;
	}
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
     * Sets the camera that this surface is rendered with.
     * @param cam The new camera
     * @param camNode The camera's node
     */
    public void setCamera(ZCamera cam, ZNode camNode) {
	if (camera != null) {
	    camera.setDrawingSurface(null);
	}
	camera = cam;
	cameraNode = camNode;
	camera.setDrawingSurface(this);
    }

    /**
     * Get the camera this surface is associated with.
     * @return the camera this surface is associated with.
     */
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Get the camera node this surface is associated with.
     * @return the camera node this surface is associated with.
     */
    public ZNode getCameraNode() {
	return cameraNode;
    }

    /**
     * Determine if the user interacting with the surface
     * @return Value of interacting.
     */
    public boolean isInteracting() {
	return interacting;
    }

    /**
     * Specify if the user is interacting with the surface or not.
     * Typically, event handlers will set this to true on a button press event,
     * and to false on a button release event.  This allows Jazz to change the
     * render quality to favor speed during interaction, and quality when not
     * interacting.
     *
     * @param v  Value to assign to interacting.
     */
    public void setInteracting(boolean v) {
	if (v && !interacting) {
	    interacting = true;
	    nonInteractingRenderQuality = renderQuality;
	    setRenderQuality(RENDER_QUALITY_LOW);
	} else if (!v && interacting) {
	    interacting = false;
	    setRenderQuality(nonInteractingRenderQuality);
	    repaint();
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
	    camera.repaint();
	}
    }

    /**
     * Internal method to notify the surface that the specified bounds should be repainted.
     * This queues an event requesting the repaint to happen.
     * Note that the input parameter may be modified as a result of this call.
     * @param repaintBounds The bounds that need to be redrawn (in global coordinates).
     */
    void repaint(ZBounds repaintBounds) {
	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println("ZDrawingSurface.repaint: repaintBounds = " + repaintBounds);
	}

	if (component != null) {
				// We need to round conservatively so the repainted area is big enough
	    tmpRepaintRect.setRect((int)(repaintBounds.getX() - 1.0f),
				   (int)(repaintBounds.getY() - 1.0f),
				   (int)(repaintBounds.getWidth() + 3.0f),
				   (int)(repaintBounds.getHeight() + 3.0f));
	    component.repaint(tmpRepaintRect);
	}

	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println();
	}
    }

    /**
     * Paints the camera this surface sees.
     * @param Graphics The graphics to use for rendering.
     */
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;

	Rectangle rectSrc = g2.getClipBounds();
	ZBounds paintBounds;
	if (rectSrc == null) {
	    paintBounds = camera.getBounds();
	} else {
	    paintBounds = new ZBounds(rectSrc);
	}

	if (ZDebug.debug && ZDebug.debugRender) {
	    System.out.println("ZDrawingSurface.paint(ZRenderContext): transform   = " + g2.getTransform());
	    System.out.println("ZDrawingSurface.paint(ZRenderContext): clip bounds = " + g2.getClipBounds());
	    System.out.println("ZDrawingSurface.paint(ZRenderContext): paint bounds = " + paintBounds);
	    ZDebug.clearPaintCount();
	}

	ZRenderContext rc = new ZRenderContext(g2, paintBounds, this, renderQuality);
	camera.render(rc);

	if (ZDebug.debug && ZDebug.debugRender) {
	    System.out.println("ZDrawingSurface.paint: Rendered " + ZDebug.getPaintCount() + " objects this pass");
	    System.out.println("");
	}
    }

    /**
     * Force this surface to immediately paint any regions that are out of date
     * and marked for future repainting.
     */
    public void paintImmediately() {
	if (component != null) {
	    RepaintManager.currentManager(component).paintDirtyRegions();
	}
    }

    /**
     * Returns the path to the first object intersecting the specified rectangle within DEFAULT_HALO pixels
     * as searched in reverse (front-to-back) order, or null if no objects satisfy criteria.
     * If no object is picked, then the path contains just the top-level camera as a terminal object.
     * @param x X-coord of pick point in window coordinates.
     * @param y Y-coord of pick point in window coordinates.
     * @return The ZSceneGraphPath to the picked object.
     */
    public ZSceneGraphPath pick(int x, int y) {
	return pick(x, y, DEFAULT_HALO);
    }

    /**
     * Returns the path to the first object intersecting the specified rectangle within halo pixels
     * as searched in reverse (front-to-back) order, or null if no objects satisfy criteria.
     * If no object is picked, then the path contains just the top-level camera as a terminal object.
     * @param x X-coord of pick point in window coordinates.
     * @param y Y-coord of pick point in window coordinates.
     * @param halo The amount the point can miss an object and still pick it
     * @return The ZSceneGraphPath to the picked object.
     */
    public ZSceneGraphPath pick(int x, int y, int halo) {
	ZSceneGraphPath path = new ZSceneGraphPath();

	if (camera != null) {
	    Date startTime = null, pickTime = null;
	    if (ZDebug.debug && ZDebug.debugTiming) {
		startTime = new Date();
	    }

	    ZBounds rect = new ZBounds(x-halo, y-halo, halo+halo, halo+halo);
	    path.setRoot(camera.getRoot());
	    path.setTopCamera(camera);
	    path.setTopCameraNode(cameraNode);
	    camera.pick(rect, path);
				// If we didn't pick an object, then we still set the transform
	    if (path.getObject() == null) {
		path.setTransform(camera.getViewTransform());
	    }

	    if (ZDebug.debug && ZDebug.debugTiming) {
		pickTime = new Date();
		System.out.println("ZDrawingSurface.pick: pickTime = " + (pickTime.getTime() - startTime.getTime()));
	    }
	}
	if (ZDebug.debug && ZDebug.debugPick) {
	    System.out.println("ZDrawingSurface.pick: " + path);
	}

	return path;
    }

    /**
     * Return the list of nodes that are accepted by the specified filter in the
     * portion of the scenegraph visible within the camera attached to this surface.
     * If a node is not "findable", then neither
     * that node, nor any of its descendants will be included.
     * The filter specifies whether or not a given node should be accepted by this
     * search, and whether the children of a node should be searched.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @return The nodes found
     * @see ZNode#isFindable()
     * @see ZFindFilter
     */
    public ArrayList findNodes(ZFindFilter filter) {
	ArrayList nodes = new ArrayList();
	if (camera != null) {
	    camera.findNodes(filter, nodes);
	}
	return nodes;
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

				// translate the graphics to the printable bounds on the page
	g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				// scale the graphics to the printable bounds on the page
	ZBounds cameraBounds = camera.getBounds();
	double scaleFactor = pageFormat.getImageableWidth()/cameraBounds.getWidth();
	if (pageFormat.getImageableHeight()/cameraBounds.getHeight() < scaleFactor) {
	    scaleFactor = pageFormat.getImageableHeight()/cameraBounds.getHeight();
	}
	g2.scale(scaleFactor, scaleFactor);

				// paint onto the printer graphics
	ZRenderContext rc = new ZRenderContext(g2, new ZBounds(cameraBounds), this, RENDER_QUALITY_HIGH);
	camera.render(rc);

	return PAGE_EXISTS;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = toString();
	str += "\n Camera: " + camera;
	return str;
    }

}
