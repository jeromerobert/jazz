/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * A simple event handler to connect two nodes with a link
 * @author Lance Good
 */
public class LinkConnector implements ZEventHandler, ZMouseListener, ZMouseMotionListener {

    // The highlight color
    static final Color HIGHLIGHT_COLOR = GraphItApplet.NODE_HIGHLIGHT_COLOR;

    // The pen color
    static final Color PEN_COLOR = GraphItApplet.NODE_PEN_COLOR;

    // The fill color
    static final Color FILL_COLOR = GraphItApplet.NODE_FILL_COLOR;

    // The pen width
    static final double PEN_WIDTH = 2.0;

    // The canvas on which the event handler is acting
    ZCanvas canvas = null;

    // The camera node on which this event handler listens
    ZNode node = null;

    // The layer to which new links are added
    ZLayerGroup linkLayer;

    // The current link
    Link currentLink = null;

    // Is this event handler active?
    boolean active = false;

    // A temporary point variable to avoid allocating one when needed
    Point pt = null;

    // The "picked" source node
    ZNode pickSrcNode = null;

    // The "picked" destination node
    ZNode pickDestNode = null;

    /**
     * The default constructor
     */
    public LinkConnector(ZCanvas canvas, ZLayerGroup layer) {
	this.canvas = null;
	this.node = canvas.getCameraNode();	
	this.linkLayer = layer;
	this.active = false;
	this.pt = new Point();
    }

    /**
     * Set this event handler active
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

    ////////////////////////////////////////////////////////////////////////
    // ZMouse Listener Implementation
    ////////////////////////////////////////////////////////////////////////   
    
    /**
     * Listens for mousePressed events
     */
    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
	    // Check to see if the node is a node and not a group
	    ZNode node = e.getPath().getNode();	    
	    if (node != null && 
		node instanceof ZVisualLeaf) {

		// A link hasn't yet been created
		if (currentLink == null) {
		    e.getPath().getTopCamera().getDrawingSurface().setInteracting(true);
		    currentLink = new Link(node);
		    currentLink.setPenWidth(PEN_WIDTH / e.getPath().getCamera().getMagnification());
		    ZVisualLeaf linkLeaf = new ZVisualLeaf(currentLink);

		    // Make sure the links are not pickable
		    linkLeaf.setPickable(false);
		    linkLeaf.setFindable(false);
		    
		    linkLayer.addChild(linkLeaf);

		    // Reset the color of the highlighted node
		    if (pickSrcNode instanceof ZVisualLeaf) {
			ZVisualComponent vis = ((ZVisualLeaf)pickSrcNode).getFirstVisualComponent();
			if (vis instanceof ZEllipse) {
			    ZEllipse ellipse = (ZEllipse)vis;
			    ellipse.setFillColor(FILL_COLOR);
			}
		    }

		    pickSrcNode = null;
		}
		// A link has been created
		else {
		    // Don't let the link connect to itself
		    if (node != currentLink.getSourceNode()) {
			e.getPath().getTopCamera().getDrawingSurface().setInteracting(false);				    
			currentLink.setDestination(node);
			currentLink = null;		   

			pickSrcNode = pickDestNode;
			pickDestNode = null;			
		    }
		}
	    }
	}	
    }

    /**
     * Listens for mouseReleased events
     */
    public void mouseReleased(ZMouseEvent e) {
    }

    /**
     * Listens for mouseClicked events
     */
    public void mouseClicked(ZMouseEvent e) {
    }

    /**
     * Listens for mouseEntered events
     */
    public void mouseEntered(ZMouseEvent e) {
    }

    /**
     * Listens for mouseExited events
     */
    public void mouseExited(ZMouseEvent e) {
    }

    ////////////////////////////////////////////////////////////////////////
    // ZMouseMotion Listener Implementation
    ////////////////////////////////////////////////////////////////////////   
    
    /**
     * Listens for mouseDragged events
     */
    public void mouseDragged(ZMouseEvent e) {
    }

    /**
     * Listens for mouseDragged events     
     */
    public void mouseMoved(ZMouseEvent e) {
	// Check to see if a link has been created to highlight possible
	// destinations
	if (currentLink != null) {

	    pt.setLocation(e.getX(),e.getY());
	    e.getPath().getCamera().cameraToLocal(pt,null);
	    currentLink.setDestination(pt);

	    ZNode pickNode = e.getPath().getNode();	    

	    if (pickDestNode != pickNode) {
		if (pickDestNode instanceof ZVisualLeaf) {
		    ZVisualComponent vis = ((ZVisualLeaf)pickDestNode).getFirstVisualComponent();
		    if (vis instanceof ZEllipse) {
			ZEllipse ellipse = (ZEllipse)vis;
			ellipse.setFillColor(FILL_COLOR);
		    }
		}		
		pickDestNode = pickNode;


		if (pickDestNode instanceof ZVisualLeaf &&
		    pickDestNode != currentLink.getSourceNode()) {
		    ZVisualComponent vis = ((ZVisualLeaf)pickDestNode).getFirstVisualComponent();
		    if (vis instanceof ZEllipse) {
			ZEllipse ellipse = (ZEllipse)vis;
			ellipse.setFillColor(HIGHLIGHT_COLOR);
		    }		
		    
		}
	    }
	}
	// Check to see that a link has not yet been created to highlight
	// possible sources
	else {
	    ZNode pickNode = e.getPath().getNode();

	    if (pickSrcNode != pickNode) {
		if (pickSrcNode instanceof ZVisualLeaf) {
		    ZVisualComponent vis = ((ZVisualLeaf)pickSrcNode).getFirstVisualComponent();
		    if (vis instanceof ZEllipse) {
			ZEllipse ellipse = (ZEllipse)vis;
			ellipse.setFillColor(FILL_COLOR);
		    }
		}		
		pickSrcNode = pickNode;	 


		if (pickSrcNode instanceof ZVisualLeaf) {
		    ZVisualComponent vis = ((ZVisualLeaf)pickSrcNode).getFirstVisualComponent();
		    if (vis instanceof ZEllipse) {
			ZEllipse ellipse = (ZEllipse)vis;
			ellipse.setFillColor(HIGHLIGHT_COLOR);
		    }		
		    
		}
	    }
	}
    }
}


