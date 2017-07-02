/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
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
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Benjamin B. Bederson
 */

public class ZPanEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener, Serializable {
    private static final int MIN_MOVEMENT = 5;      // Min # of pixels to count as a movement

    private boolean active = false;        // True when event handlers are attached to a node
    private ZNode   node = null;           // The node the event handlers are attached to

    private transient Point2D pressScreenPt = new Point2D.Double();  // Event coords of mouse press (in screen space)
    private transient Point2D pressObjPt = new Point2D.Double();	  // Event coords of mouse press (in object space)
    private boolean moved = false;                        // True if the camera was panned on the most recent interaction
				                    // Mask out mouse and mouse/key chords
    private int            all_button_mask   = (MouseEvent.BUTTON1_MASK | 
						MouseEvent.BUTTON2_MASK | 
						MouseEvent.BUTTON3_MASK | 
						MouseEvent.ALT_GRAPH_MASK | 
						MouseEvent.CTRL_MASK | 
						MouseEvent.META_MASK | 
						MouseEvent.SHIFT_MASK | 
						MouseEvent.ALT_MASK);

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
     * @return Is this event handler active?
     */
    public boolean isActive() {
	return active;
    }
    
    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */ 
    public void mousePressed(ZMouseEvent e) {
	moved = false;
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
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
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (!moved) {
		if ((Math.abs(e.getX() - pressScreenPt.getX()) > MIN_MOVEMENT) ||
		    (Math.abs(e.getY() - pressScreenPt.getY()) > MIN_MOVEMENT)) {
		    moved = true;
		}
	    }
	    if (moved) {
		ZSceneGraphPath path = e.getPath();
		Point2D currObjPt = new Point2D.Double(e.getX(), e.getY());
		path.screenToGlobal(currObjPt);
		
		double dx = (currObjPt.getX() - pressObjPt.getX());
		double dy = (currObjPt.getY() - pressObjPt.getY());

		path.getTopCamera().translate(dx, dy);
	    }
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
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
     * Returns true if the most recent button press/drag/release resulted in a pan movement.
     * @return moved
     */
    public boolean isMoved() {
	return moved;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

				// write Point2D.double pressScreenPt
	out.writeDouble(pressScreenPt.getX());
	out.writeDouble(pressScreenPt.getY());

				// write Point2D.double pressObjPt
	out.writeDouble(pressObjPt.getX());
	out.writeDouble(pressObjPt.getY());
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	double x, y;

				// read pressScreenPt
	x = in.readDouble();
	y = in.readDouble();
	pressScreenPt = new Point2D.Double(x, y);

				// read pressObjPt
	x = in.readDouble();
	y = in.readDouble();
	pressObjPt = new Point2D.Double(x, y);
    }
}



