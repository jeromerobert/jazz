/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.event.*;

/**
 * An extension of polyline that keeps track of a source and destination
 * node's global bounds to know when to update its coordinates.  The link
 * also listen for group events to know whether either the source or
 * destination nodes have been deleted (or any of their ancestors) to
 * determine when to delete itself.
 * @author Lance Good
 */
public class Link extends ZPolyline implements ZNodeListener, ZGroupListener {

    // The current source connection point
    Point2D srcPt;

    // The current destination connection point
    Point2D destPt;

    // The current source node
    ZNode srcNode;

    // The current destination node    
    ZNode destNode;    

    // The current root of the subtree containing the source 
    ZNode srcAncestor;
    
    // The current root of the subtree containing the destination
    ZNode destAncestor;

    // The current source and destination bounds
    ZBounds srcBounds;

    // The current source and destination bounds    
    ZBounds destBounds;

    /**
     * The default constructor
     */
    public Link(ZNode src) {
	super();
	srcPt = new Point2D.Double(0.0,0.0);
	destPt = new Point2D.Double(0.0,0.0);
	setSource(src);
	setArrowHead(ARROW_LAST);
	setArrowHeadType(ARROW_CLOSED);
    }

    /**
     * Get the source Node
     * @return The source node
     */
    public ZNode getSourceNode() {
	return srcNode;
    }
    
    /**
     * Set the source of this link
     * @param src The new source
     */
    public void setSource(ZNode src) {
	srcNode = src;

	// Add a listener for the global bounds
	srcNode.addNodeListener(this);

	// Get the current global bounds
	srcBounds = srcNode.getGlobalBounds();

	// Find the root of the subtree containing the source
	srcAncestor = srcNode;
	while(srcAncestor.getParent() != null) {
	    srcAncestor = srcAncestor.getParent();
	}
	((ZGroup)srcAncestor).addGroupListener(this);
	
	// If the destination node is null then we have to initialize
	// the destination point 
	if (destNode == null) {
	    destPt.setLocation((srcBounds.getX()+srcBounds.getWidth()/2.0),
			       (srcBounds.getY()+srcBounds.getHeight()/2.0));

	    srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));

	    updateSourcePoint();
	}
	// If the destination point is not null then we just update the
	// bounds of this point
	else {
	    srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));
	    destPt.setLocation(destBounds.getX()+(destBounds.getWidth()/2.0),destBounds.getY()+(destBounds.getHeight()/2.0));

	    updateSourcePoint();
	    updateDestinationPoint();
	}

	// Set the coords to the updated points and repaint
	setCoords(srcPt,destPt);	
	repaint();
    }

    /**
     * Set the destination node
     * @param dest The destination node
     */
    public void setDestination(ZNode dest) {
	destNode = dest;

	// Listen for global bounds changes
	destNode.addNodeListener(this);	

	// Get the current global bounds
	destBounds = destNode.getGlobalBounds();

	// Find the root of the subtree containing the destination
	destAncestor = destNode;
	while(destAncestor.getParent() != null) {
	    destAncestor = destAncestor.getParent();
	}
	((ZGroup)destAncestor).addGroupListener(this);

	// Compute the initial positions for the source and destination
	// points.  We know the resulting connection points will lie on
	// the line connecting these two points so this is a good first
	// guess
	srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));
	destPt.setLocation(destBounds.getX()+(destBounds.getWidth()/2.0),destBounds.getY()+(destBounds.getHeight()/2.0));

	// Now really compute these points
	updateSourcePoint();
	updateDestinationPoint();

	// Set the coords to the updated points and repaint	
	setCoords(srcPt,destPt);
	repaint();	
    }

    /**
     * Set the destination point (used for rubberbanding)
     * @param pt The new rubber band point
     */
    public void setDestination(Point2D pt) {
	destPt = pt;
	srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));
	
	updateSourcePoint();
	setCoords(srcPt,destPt);
	repaint();
    }

    /**
     * Update the source connection point - this method does a little math
     * to find a point on the radius of the node's circle on the line
     * connecting the source node's center to the destination node's center
     */
    public void updateSourcePoint() {
	if (destPt.getX() != srcPt.getX() ||
	    destPt.getY() != srcPt.getY()) {

	    double xComp,yComp,dist;

	    xComp = destPt.getX()-srcPt.getX();
	    yComp = destPt.getY()-srcPt.getY();

	    dist = Math.sqrt(xComp*xComp+yComp*yComp);

	    xComp = xComp/dist;
	    yComp = yComp/dist;

	    xComp = xComp*(srcBounds.getWidth()/2.0);
	    yComp = yComp*(srcBounds.getWidth()/2.0);

	    srcPt.setLocation(srcPt.getX()+xComp,srcPt.getY()+yComp);
	}
    }

    /**
     * Update the destination connection point - this method does a little math
     * to find a point on the radius of the node's circle on the line
     * connecting the source node's center to the destination node's center    
     */
    public void updateDestinationPoint() {
	if (destPt.getX() != srcPt.getX() ||
	    destPt.getY() != srcPt.getY()) {

	    double xComp,yComp,dist;

	    xComp = srcPt.getX()-destPt.getX();
	    yComp = srcPt.getY()-destPt.getY();

	    dist = Math.sqrt(xComp*xComp+yComp*yComp);

	    xComp = xComp/dist;
	    yComp = yComp/dist;

	    xComp = xComp*(destBounds.getWidth()/2.0);
	    yComp = yComp*(destBounds.getWidth()/2.0);

	    destPt.setLocation(destPt.getX()+xComp,destPt.getY()+yComp);
	}
    }

    ////////////////////////////////////////////////////////////////////////
    // Node Listener Implementation
    ////////////////////////////////////////////////////////////////////////   
    
    /**
     * Local bounds changed event
     */
    public void boundsChanged(ZNodeEvent e) {
    }

    /**
     * Global bounds changed event
     */
    public void globalBoundsChanged(ZNodeEvent e) {
	if (e.getNode() == srcNode) {
	    srcBounds = srcNode.getGlobalBounds();

	    srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));
	    destPt.setLocation(destBounds.getX()+(destBounds.getWidth()/2.0),destBounds.getY()+(destBounds.getHeight()/2.0));


	    updateSourcePoint();
	    updateDestinationPoint();
	    setCoords(srcPt,destPt);
	    repaint();	    
	}
	else if (e.getNode() == destNode) {
	    destBounds = destNode.getGlobalBounds();

	    srcPt.setLocation(srcBounds.getX()+(srcBounds.getWidth()/2.0),srcBounds.getY()+(srcBounds.getHeight()/2.0));
	    destPt.setLocation(destBounds.getX()+(destBounds.getWidth()/2.0),destBounds.getY()+(destBounds.getHeight()/2.0));

	    updateSourcePoint();
	    updateDestinationPoint();
	    setCoords(srcPt,destPt);
	    repaint();	    
	}
    }

    ////////////////////////////////////////////////////////////////////////
    // Group Listener Implementation
    ////////////////////////////////////////////////////////////////////////   
    
    /**
     * Node added event
     */
    public void nodeAdded(ZGroupEvent e) {
    }

    /**
     * Node removed event, used to determine when we should delete the
     * link.
     */
    public void nodeRemoved(ZGroupEvent e) {
	ZNode node = e.getChild();

	// We only need to check non-modification events
	if (!e.isModificationEvent()) {

	    // It isn't possible to delete a link in our interface
	    // unless the source and destination nodes have been specified
	    // so we only need to check the events if these are non-null
	    if (srcNode != null &&
		destNode != null) {

		// Now check to see if the node is equal to the source
		// or destination OR if the node is an ancestor of the
		// source or destination
		if (node == srcNode ||
		    node == destNode ||
		    srcNode.isDescendentOf(node) ||
		    destNode.isDescendentOf(node)) {
		    ((ZGroup)srcAncestor).removeGroupListener(this);
		    ((ZGroup)destAncestor).removeGroupListener(this);
		    ZNode top = getParents()[0].editor().getTop();
		    top.getParent().removeChild(top);
		    srcNode = null;
		    destNode = null;
		}
	    }
	}
    }
}



