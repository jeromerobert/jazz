/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * This code creates a simple scene and then adds a internal camera
 * (or "portal") that looks at the scene also.  The scene can be
 * panned and zoomed as a whole, and the view within the portal
 * can be panned and zoomed as well.
 *
 * @author Ben Bederson
 */ 
public class PortalTest extends JFrame {
    public static void main(String[] args) {
	JFrame frame;
	ZCanvas canvas;

				// Set up basic frame
	frame = new JFrame();
	frame.setBounds(100, 100, 400, 400);
	frame.setResizable(true);
	frame.setBackground(null);
	frame.setVisible(true);
	canvas = new ZCanvas();
	frame.getContentPane().add(canvas);
	frame.validate();

				// Support exiting application
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

				// Make some rectangles on the surface so we can see where we are
	ZRectangle rect;
	for (int x=0; x<5; x++) {
	    for (int y=0; y<5; y++) {
		rect = new ZRectangle(50*x, 50*y, 40, 40);   
		rect.setFillColor(Color.blue);
		rect.setPenColor(Color.black);
		canvas.getLayer().addChild(new ZVisualLeaf(rect));
	    }
	}

				// Now, create a portal (i.e., internal camera) on the surface
				// that looks at all of the rectangles.
	Portal portal = new Portal(canvas, 100, 100, 200, 200);
	canvas.getRoot().addChild(portal);
	canvas.getCamera().addLayer(portal);
    }
}

/**
 * The portal itself actually consists of a few elements.  There is a layer which
 * can be looked at by the application camera.  The layer has a ZVisualGroup which
 * acts as a border around the camera (it contains a rectangle).  Then, the border
 * contains a ZVisualLeaf which contains the internal camera.  The internal camera
 * is the actual element that acts like a portal - looking onto the scenegraph.
 */
class Portal extends ZLayerGroup {
    public Portal(ZCanvas canvas, int x, int y, int w, int h) {
				// Create the internal camera.  There is a bug in the Jazz-0.6 pick
				// method.  So, we override the pick method here to fix the bug.  It
				// will be fixed in the next release of Jazz.
	ZCamera camera = new ZCamera(canvas.getLayer(), canvas.getDrawingSurface()) {
	    public boolean pick(java.awt.geom.Rectangle2D rect, ZSceneGraphPath path) {
		if (super.pick(rect, path)) {
		    return true;
		} else if (rect.intersects(getBounds())) {
		    path.push(this); // Add this object to the path
			  	     // Concatenate the camera's transform with the one stored in the path
		    AffineTransform origTm = path.getTransform();
		    AffineTransform tm = new AffineTransform(origTm);
		    tm.concatenate(getViewTransform());
		    path.setTransform(tm);
		    return true;
		} else {
		    return false;
		}
	    }
	};

				// Fill in the details of the camera - its bounds, color, border
	camera.setBounds(x, y, w, h);
	camera.setFillColor(Color.gray);
	ZVisualLeaf leaf = new ZVisualLeaf(camera);
	ZVisualGroup border = new ZVisualGroup(leaf);
	ZRectangle borderRect = new ZRectangle(x, y, w, h);
	borderRect.setPenColor(Color.black);
	borderRect.setFillColor(null);
	borderRect.setPenWidth(5.0);
	border.setFrontVisualComponent(borderRect);

				// Now, build up the portal connections.  The camera gets added to the
				// border, and the border gets added to this layer.
	this.addChild(border);
	border.addChild(leaf);

				// Finally, create event handlers to pan and zoom the internal camera
	new PanCameraEventHandler(this, camera).setActive(true);
	new ZoomCameraEventHandler(this, camera).setActive(true);
    }
}

class PanCameraEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private static final int MIN_MOVEMENT = 5;      // Min # of pixels to count as a movement
    private boolean active = false;                 // True when event handlers are attached to a node
    private ZNode   node = null;
    private ZCamera camera = null;
	
    private Point2D pressScreenPt = new Point2D.Double();  // Event coords of mouse press (in screen space)
    private Point2D pressObjPt = new Point2D.Double();	  // Event coords of mouse press (in object space)
    private boolean moved = false;                        // True if the camera was panned on the most recent interaction

    public PanCameraEventHandler(ZNode node, ZCamera camera) {
	this.node = node;
	this.camera = camera;
    }
	
    public void setActive(boolean active) {
	if (this.active && !active) {
				// Turn off event handlers
	    this.active = false;
	    node.removeMouseListener(this);
	    node.removeMouseMotionListener(this);
	} else if (!this.active && active) {
				// Turn on event handlers
	    this.active = true;
	    node.addMouseListener(this);
	    node.addMouseMotionListener(this);
	}
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
	return active;
    }

    public void mousePressed(ZMouseEvent e) {
	moved = false;
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();
	    path.getTopCamera().getDrawingSurface().setInteracting(true);
	    pressScreenPt.setLocation(e.getX(), e.getY());
	    pressObjPt.setLocation(e.getX(), e.getY());
	    path.screenToLocal(pressScreenPt);
	    path.screenToLocal(pressObjPt);
	    e.consume();
	}
    }

    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (!moved) {
		if ((Math.abs(e.getX() - pressScreenPt.getX()) > MIN_MOVEMENT) ||
		    (Math.abs(e.getY() - pressScreenPt.getY()) > MIN_MOVEMENT)) {
		    moved = true;
		}
	    }
	    if (moved) {
		ZSceneGraphPath path = e.getPath();
		Point2D currObjPt = new Point2D.Double(e.getX(), e.getY());
		path.screenToLocal(currObjPt);
		double dx = (currObjPt.getX() - pressObjPt.getX());
		double dy = (currObjPt.getY() - pressObjPt.getY());
		camera.translate(dx, dy);
	    }
	    e.consume();
	}
    }

 
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    e.getPath().getTopCamera().getDrawingSurface().setInteracting(false);
	    e.consume();
	}
    }

    public void mouseEntered(ZMouseEvent e) {
    }

    public void mouseExited(ZMouseEvent e) {
    }

    public void mouseClicked(ZMouseEvent e) {
    }

    public void mouseMoved(ZMouseEvent e) {
    }
}

/**
 * Zoom within the portal.
 * right button zooms in
 * shift-right button zooms out
 */
class ZoomCameraEventHandler  implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private boolean active = false;       // True when event handlers are attached to a node
    private ZNode   node = null;          // The node the event handlers are attached to
    private ZCamera camera = null;        // The camera we are zooming within
    private double   scaleDelta = 1.0;    // Amount to zoom by
    private double   pressScreenX;         // Event x-coord of mouse press (in screen space)
    private Point2D pressScreenPt = new Point2D.Double();  // Event coords of mouse press (in screen space)
    private Point2D pressObjPt;           // Event coords of mouse press (in object space)
    private boolean zooming = false;      // True while zooming 
    private double   minMag = 0.0;        // The minimum allowed magnification
    private double   maxMag = -1.0;       // The maximum allowed magnification (or disabled if less than 0)
    private boolean moved = false;        // True if the camera was panned on the most recent interaction
    private static final int MIN_MOVEMENT = 5;      // Min # of pixels to count as a movement	
    

    public ZoomCameraEventHandler(ZNode node, ZCamera camera) {
	this.node = node;
	this.camera = camera;
	pressObjPt = new Point2D.Double();
    }
    
    public void setActive(boolean active) {
	if (this.active && !active) {
				// Turn off event handlers
	    this.active = false;
	    node.removeMouseListener(this);
	    node.removeMouseMotionListener(this);
	} else if (!this.active && active) {
				// Turn on event handlers
	    this.active = true;
	    node.addMouseListener(this);
	    node.addMouseMotionListener(this);
	}
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
	return active;
    }

    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) { // Right button only
	    ZSceneGraphPath path = e.getPath();
	    pressScreenX = e.getX();
	    pressObjPt.setLocation(e.getX(), e.getY());
	    path.screenToLocal(pressObjPt);

	    if (e.isShiftDown()) {
		scaleDelta = 1.01;
	    } else {
		scaleDelta = 0.99;
	    }
	    startZooming(); 
	    e.consume();
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) { // Right button only
	    double dx = (e.getX() - pressScreenX);
	    scaleDelta = (1.0 + (0.001 * dx));
	}
    }


    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    stopZooming();
	    e.consume();	
	}
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been moved on a node
     * (with no buttons no down).
     */
    public void mouseMoved(ZMouseEvent e) {
    }
    
    /**
     * Start animated zooming.
     */
    public void startZooming() {
	zooming = true;
	zoomOneStep();
    }
    /**
     * Set the minimum magnification that the camera can be set to
     * with this event handler.  Setting the min mag to <= 0 disables
     * this feature.  If the min mag if set to a value which is greater
     * than the current camera magnification, then the camera is left
     * at its current magnification.
     * @param newMinMag the new minimum magnification
     */
    public void setMinMagnification(double newMinMag) {
	minMag = newMinMag;
    }

    /**
     * Set the maximum magnification that the camera can be set to
     * with this event handler.  Setting the max mag to <= 0 disables
     * this feature.  If the max mag if set to a value which is less
     * than the current camera magnification, then the camera is left
     * at its current magnification.
     * @param newMaxMag the new maximum magnification
     */
    public void setMaxMagnification(double newMaxMag) {
	maxMag = newMaxMag;
    }

    /**
     * Stop animated zooming.
     */
    public void stopZooming() {
	zooming = false;
    }

    /**
     * Do one zooming step, sleep a short amount, and schedule the next zooming step.
     * This effectively continuously zooms while still accepting input events so
     * that the zoom center point can be changed, and zooming can be stopped.
     */
    public void zoomOneStep() {
	if (zooming) {
				// Check for magnification bounds
	    double currentMag = this.camera.getMagnification();
	    double newMag = currentMag * scaleDelta;
	    if (newMag < minMag) {
		scaleDelta = minMag / currentMag;
	    }
	    if ((maxMag > 0) && (newMag > maxMag)) {
		scaleDelta = maxMag / currentMag;
	    }

				// Now, go ahead and zoom one step
	    this.camera.scale(scaleDelta, (double)pressObjPt.getX(), (double)pressObjPt.getY());
	    
	    try {
		// The sleep here is necessary.  Otherwise, there won't be
		// time for the primary event thread to get and respond to
		// input events.
		Thread.sleep(10);

				// If the sleep was interrupted, then cancel the zooming,
				// so don't do the next zooming step
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ZoomCameraEventHandler.this.zoomOneStep();
		    }
		});
	    } catch (InterruptedException e) { zooming = false; }
	}
    }
}
