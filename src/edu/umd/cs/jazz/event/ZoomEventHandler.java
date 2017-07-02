/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
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
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Benjamin B. Bederson
 */

public class ZoomEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener, Serializable {
    private boolean   active = false;     // True when event handlers are attached to a node
    private ZNode     node = null;        // The node the event handlers are attached to

    private ZCamera camera = null;        // The camera we are zooming within
    private double   scaleDelta = 1.0;    // Amount to zoom by
    private double   pressScreenX;         // Event x-coord of mouse press (in screen space)
    private Point2D pressObjPt;           // Event coords of mouse press (in object space)
    private boolean zooming = false;      // True while zooming 
    private double   minMag = 0.0;        // The minimum allowed magnification
    private double   maxMag = -1.0;       // The maximum allowed magnification (or disabled if less than 0)
				                                  // Mask out mouse and mouse/key chords
    private int     all_button_mask   = (MouseEvent.BUTTON1_MASK | 
					 MouseEvent.BUTTON2_MASK | 
					 MouseEvent.BUTTON3_MASK | 
					 MouseEvent.ALT_GRAPH_MASK | 
					 MouseEvent.CTRL_MASK | 
					 MouseEvent.META_MASK | 
					 MouseEvent.SHIFT_MASK | 
					 MouseEvent.ALT_MASK);

    /**
     * Constructs a new ZoomEventHandler.
     * @param <code>node</code> The node this event handler attaches to.
     */
    public ZoomEventHandler(ZNode node) {
	this.node = node;
	pressObjPt = new Point2D.Double();
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
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
	return active;
    }

    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    ZSceneGraphPath path = e.getPath();
	    camera = path.getTopCamera();
	    camera.getDrawingSurface().setInteracting(true);
	    
	    pressScreenX = e.getX();
	    pressObjPt.setLocation(e.getX(), e.getY());
	    path.screenToGlobal(pressObjPt);

	    scaleDelta = 1.0;	               // No zooming until the mouse is moved
	    startZooming();
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    double dx = (double)(e.getX() - pressScreenX);
	    scaleDelta = (1.0 + (0.001 * dx));
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON3_MASK) {   // Right button only
	    stopZooming();
	    camera = null;
	    ZCamera topCamera = e.getPath().getTopCamera();
	    topCamera.getDrawingSurface().setInteracting(false);

				// do this to generate side effect camera event
	    topCamera.setViewTransform(topCamera.getViewTransform());
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
	    double currentMag = camera.getMagnification();
	    double newMag = currentMag * scaleDelta;
	    if (newMag < minMag) {
		scaleDelta = minMag / currentMag;
	    }
	    if ((maxMag > 0) && (newMag > maxMag)) {
		scaleDelta = maxMag / currentMag;
	    }

				// Now, go ahead and zoom one step
	    camera.scale(scaleDelta, pressObjPt.getX(), pressObjPt.getY());
	    
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
    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

				// write Point2D.double pressObjPt
	out.writeDouble(pressObjPt.getX());
	out.writeDouble(pressObjPt.getY());
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	double x, y;

				// read pressObjPt
	x = in.readDouble();
	y = in.readDouble();
	pressObjPt = new Point2D.Double(x, y);
    }
}
