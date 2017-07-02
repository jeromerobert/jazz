/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZoomEventhandler</b> provides event handlers for basic zooming
 * of a Jazz camera with the right button.  The interaction is that
 * the initial mouse press defines the zoom anchor point, and then
 * moving the mouse to the right zooms with a speed proportional 
 * to the amount the mouse is moved to the right of the anchor point.
 * Similarly, if the mouse is moved to the left, the the camera is
 * zoomed out.
 *
 * @author  Benjamin B. Bederson
 */

public class ZoomEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private boolean   active = false;     // True when event handlers are attached to a node
    private ZNode     node = null;        // The node the event handlers are attached to

    private ZCamera camera = null;        // The camera we are zooming within
    private float   scaleDelta = 1.0f;    // Amount to zoom by
    private float   pressScreenX;         // Event x-coord of mouse press (in screen space)
    private Point2D pressObjPt;           // Event coords of mouse press (in object space)
    private boolean zooming = false;      // True while zooming 
    private float   minMag = 0.0f;        // The minimum allowed magnification
    private float   maxMag = -1.0f;       // The maximum allowed magnification (or disabled if less than 0)

    /**
     * Constructs a new ZoomEventHandler.
     * @param <code>node</code> The node this event handler attaches to.
     */
    public ZoomEventHandler(ZNode node) {
	this.node = node;
	pressObjPt = new Point2D.Float();
    }
    
    /**
     * Specifies whether this event handler is active or not.
     * @param active True to make this event handler active
     */
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
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    ZSceneGraphPath path = e.getPath();
	    camera = path.getTopCamera();
	    camera.getDrawingSurface().setInteracting(true);
	    
	    pressScreenX = e.getX();
	    pressObjPt.setLocation(e.getX(), e.getY());
	    path.screenToGlobal(pressObjPt);

	    scaleDelta = 1.0f;	               // No zooming until the mouse is moved
	    startZooming();
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    float dx = (float)(e.getX() - pressScreenX);
	    scaleDelta = (float)(1.0f + (0.001f * dx));
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    stopZooming();
	    camera = null;
	    e.getPath().getTopCamera().getDrawingSurface().setInteracting(false);
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
    public void setMinMagnification(float newMinMag) {
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
    public void setMaxMagnification(float newMaxMag) {
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
	    float currentMag = camera.getMagnification();
	    float newMag = currentMag * scaleDelta;
	    if (newMag < minMag) {
		scaleDelta = minMag / currentMag;
	    }
	    if ((maxMag > 0) && (newMag > maxMag)) {
		scaleDelta = maxMag / currentMag;
	    }

				// Now, go ahead and zoom one step
	    camera.scale(scaleDelta, (float)pressObjPt.getX(), (float)pressObjPt.getY());
	    
	    try {
				// The sleep here is necessary.  Otherwise, there won't be
				// time for the primary event thread to get and respond to
				// input events.
		Thread.sleep(20);

				// If the sleep was interrupted, then cancel the zooming,
				// so don't do the next zooming step
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ZoomEventHandler.this.zoomOneStep();
		    }
		});
	    } catch (InterruptedException e) {
		zooming = false;
	    }
	}
    }
}
