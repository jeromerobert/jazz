/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * <b>ZoomEventhandlerRightButton</b> provides event handlers for basic zooming
 * of a Jazz camera with the right button.  The interaction is that
 * the initial mouse press defines the zoom anchor point, and then
 * moving the mouse to the right zooms with a speed proportional 
 * to the amount the mouse is moved to the right of the anchor point.
 * Similarly, if the mouse is moved to the left, the the camera is
 * zoomed out.
 *
 * @author  Benjamin B. Bederson
 */

public class ZoomEventHandlerRightButton extends ZEventHandler {
    protected float   scaleDelta = 1.0f;    // Amount to zoom by
    protected float   pressWinX;            // Event x-coord of mouse press (in window space)
    protected Point2D pressObjPt;           // Event coords of mouse press (in object space)
    protected boolean zooming = false;      // True while zooming 
    protected float   minMag = 0.0f;        // The minimum allowed magnification
    protected float   maxMag = -1.0f;       // The maximum allowed magnification (or disabled if less than 0)

    /**
     * Constructs a new ZoomEventHandlerRightButton.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The camera that is panned
     */
    public ZoomEventHandlerRightButton(Component c, ZSurface v) {
	super(c, v);
	pressObjPt = new Point2D.Float();
    }
    
    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(MouseEvent e) {
	ZCamera camera = getCamera();

	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    getComponent().requestFocus();
	    getSurface().startInteraction();
	    
	    pressWinX = e.getX();
	    pressObjPt.setLocation(e.getX(), e.getY());
	    camera.cameraToScene(pressObjPt);

	    scaleDelta = 1.0f;	               // No zooming until the mouse is moved
	    startZooming();
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(MouseEvent e) {
	ZCamera camera = getCamera();
	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    float dx = (float)(e.getX() - pressWinX);
	    scaleDelta = (float)(1.0f + (0.001f * dx));
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(MouseEvent e) {
	ZCamera camera = getCamera();

	if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    stopZooming();

	    getSurface().endInteraction();
	}
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
	    ZCamera camera = getCamera();

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
	    camera.getViewTransform().scale(scaleDelta, (float)pressObjPt.getX(), (float)pressObjPt.getY());
	    getSurface().restore();
	    
	    try {
				// The sleep here is necessary.  Otherwise, there won't be
				// time for the primary event thread to get and respond to
				// input events.
		Thread.sleep(20);

				// If the sleep was interrupted, then cancel the zooming,
				// so don't do the next zooming step
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ZoomEventHandlerRightButton.this.zoomOneStep();
		    }
		});
	    } catch (InterruptedException e) {
		zooming = false;
	    }
	}
    }
}
