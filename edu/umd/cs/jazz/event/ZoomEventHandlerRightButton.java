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
	    camera.getInverseViewTransform().transform(pressObjPt, pressObjPt);

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
