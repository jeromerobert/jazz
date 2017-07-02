/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * A simple event handler to create nodes whose radius is based on
 * the current camera magnification.
 * @author Lance Good
 */
public class NodeDropper implements ZEventHandler, ZMouseListener {

    // The node size
    static final double NODE_SIZE = 15.0;

    // The pen width
    static final double PEN_WIDTH = 1.0;

    // The pen color
    static final Color PEN_COLOR = GraphItApplet.NODE_PEN_COLOR;

    // The fill color
    static final Color FILL_COLOR = GraphItApplet.NODE_FILL_COLOR;    

    // The canvas on which this event handler is active
    ZCanvas canvas;

    // The camera node on which this event handler is active
    ZNode node;

    // The layer on which the nodes are dropped
    ZLayerGroup layer;

    // This event handler wasn't active
    boolean active;

    // A point variable to avoid allocating one when needed
    Point pt;

    /**
     * The default constructor
     */
    public NodeDropper(ZCanvas canvas) {	
	this.canvas = canvas;
	this.node = canvas.getCameraNode();
	this.layer = canvas.getLayer();
	this.pt = new Point();	
	this.active = false;
    }

    /**
     * Set this event handler active
     */
    public void setActive(boolean active) {
	if (this.active && !active) {
				// Turn off event handlers
	    this.active = false;
	    node.removeMouseListener(this);
	} else if (!this.active && active) {
				// Turn on event handlers
	    this.active = true;
	    node.addMouseListener(this);
	}	
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
	return active;
    }

    ////////////////////////////////////////////////////////////////////////
    // ZMouse Listener Implementation
    ////////////////////////////////////////////////////////////////////////   

    /**
     * Listens for mouseEntered events
     */
    public void mouseEntered(ZMouseEvent zme) {
    }

    /**
     * Listens for mouseExited events
     */
    public void mouseExited(ZMouseEvent zme) {
    }

    /**
     * Listens for mousePressed events to create the nodes whose size is
     * based on the current camera magnification
     */
    public void mousePressed(ZMouseEvent zme) {
	if ((zme.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
	    ZSceneGraphPath path = zme.getPath();
	    ZCamera camera = path.getTopCamera();

	    pt.setLocation(zme.getX()-(NODE_SIZE/2.0),zme.getY()-(NODE_SIZE/2.0));
	    path.screenToGlobal(pt);

	    ZEllipse ellipse = new ZEllipse(pt.getX(),
					    pt.getY(),
					    NODE_SIZE / camera.getMagnification(),
					    NODE_SIZE / camera.getMagnification());

	    ZVisualLeaf leaf = new ZVisualLeaf(ellipse);
	    ellipse.setPenWidth(PEN_WIDTH / camera.getMagnification());
	    ellipse.setPenColor(PEN_COLOR);
	    ellipse.setFillColor(FILL_COLOR);
	    layer.addChild(leaf);
	}
    }

    /**
     * Listens for mouseReleased events
     */
    public void mouseReleased(ZMouseEvent zme) {	
    }

    /**
     * Listens for mouseClicked events
     */
    public void mouseClicked(ZMouseEvent zme) {
    }
}



