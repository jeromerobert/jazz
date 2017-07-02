/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZPanEventHandler</b> provides event handlers for basic panning
 * of a Jazz camera with the left mouse.  The interaction is that
 * clicking and dragging the mouse translates the camera so that
 * the point on the surface stays under the mouse.
 *
 * @author  Benjamin B. Bederson
 */

public class ZPanEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private static final int MIN_MOVEMENT = 5;      // Min # of pixels to count as a movement

    private boolean active = false;        // True when event handlers are attached to a node
    private ZNode   node = null;           // The node the event handlers are attached to

    private Point2D pressScreenPt = new Point2D.Float();  // Event coords of mouse press (in screen space)
    private Point2D pressObjPt = new Point2D.Float();	  // Event coords of mouse press (in object space)
    private boolean moved = false;                        // True if the camera was panned on the most recent interaction

    /**
     * Constructs a new ZPanEventHandler.
     * @param <code>node</code> The node this event handler attaches to.
     */
    public ZPanEventHandler(ZNode node) {
	this.node = node;
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
	moved = false;
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();
	    path.getTopCamera().getDrawingSurface().setInteracting(true);
	    
	    pressScreenPt.setLocation(e.getX(), e.getY());
	    pressObjPt.setLocation(e.getX(), e.getY());
	    path.screenToGlobal(pressObjPt);
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
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
		Point2D currObjPt = new Point2D.Float(e.getX(), e.getY());
		path.screenToGlobal(currObjPt);
		
		float dx = (float)(currObjPt.getX() - pressObjPt.getX());
		float dy = (float)(currObjPt.getY() - pressObjPt.getY());

		path.getTopCamera().translate(dx, dy);
	    }
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
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
     * Returns true if the most recent button press/drag/release resulted in a pan movement.
     * @return moved
     */
    public boolean isMoved() {
	return moved;
    }
}
