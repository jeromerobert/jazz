/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ZPanEventHandler</b> provides event handlers for basic panning
 * of a Jazz camera with the left mouse.  The interaction is that
 * clicking and dragging the mouse translates the camera so that
 * the point on the surface stays under the mouse.
 *
 * @author  Benjamin B. Bederson
 */

public class ZPanEventHandler extends ZEventHandler {
    protected int minMovement = 5;  // Min # of pixels to count as a movement
    protected Point2D pressObjPt;	// Event coords of mouse press (in object space)
    protected boolean moved = false;
    protected Point2D pressScreenPt;
    
    /**
     * Constructs a new ZPanEventHandler.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The surface that is panned
     */
    public ZPanEventHandler(Component c, ZSurface v) {
	super(c, v);
	pressObjPt = new Point2D.Float();
	pressScreenPt = new Point2D.Float();
    }
    
    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */ 
    public void mousePressed(MouseEvent e) {
	ZCamera camera = getCamera();

	moved = false;
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getComponent().requestFocus();
	    getSurface().startInteraction();
	    
	    pressScreenPt.setLocation(e.getX(), e.getY());
	    pressObjPt.setLocation(e.getX(), e.getY());
	    camera.cameraToScene(pressObjPt);
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(MouseEvent e) {
	ZCamera camera = getCamera();

	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (!moved) {
		if ((Math.abs(e.getX() - pressScreenPt.getX()) > minMovement) ||
		    (Math.abs(e.getY() - pressScreenPt.getY()) > minMovement)) {
		    moved = true;
		}
	    }
	    if (moved) {
		Point2D currObjPt = new Point2D.Float(e.getX(), e.getY());
		camera.cameraToScene(currObjPt);
		
		float dx = (float)(currObjPt.getX() - pressObjPt.getX());
		float dy = (float)(currObjPt.getY() - pressObjPt.getY());
		
		camera.getViewTransform().translate(dx, dy);
		getSurface().restore();
	    }
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(MouseEvent e) {
	ZCamera camera = getCamera();

	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (moved) {
		getSurface().endInteraction();
	    }
	}
    }

    /**
     * Returns true if the most recent button press/drag/release resulted in a pan movement.
     * @return moved
     */
    public boolean moved() {
	return moved;
    }
}
