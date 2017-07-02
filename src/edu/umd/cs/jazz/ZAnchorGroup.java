/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ZAnchorGroup</b> is a group that manages a hyperlink from the edit node below it.
 * It references another node, or an area in space.  It has
 * methods to visually show that there is a link (typically used by an application when the user
 * mouses over the visual component), and to follow a link.
 * <p>
 * ZAnchorGroup indicates the link with a visual component that can be defined by extending
 * this class and overriding createLinkComponent.  By default, it creates an arrow pointing
 * to the destination of the link.
 *
 * @author  Benjamin B. Bederson
 */
public class ZAnchorGroup extends ZVisualGroup implements ZSerializable, Serializable {
				// Default values
    static private final int  LINK_ANIMATION_TIME = 750;
    static private final int  DEFAULT_CONNECTOR_WIDTH = 5;
    static private Color      nodeColor = new Color(150, 150, 0);
    static private Color      boundsColor = new Color(150, 150, 150);

    /**
     * The destination node of this link (for link traversal), if there is one.
     */
    private ZNode             destNode = null;

    /**
     * The destination bounds of this link (for link traversal), if there is one.
     */
    private transient Rectangle2D       destBounds = null;

    /**
     * The source point of the link (for link display)
     */
    private transient Point2D srcPt = null;

    /**
     * The destination point of the link (for link display)
     */
    private transient Point2D destPt = null;

    /**
     * The node representing the visual link
     */
    private transient ZVisualLeaf linkNode = null;

    /**
     * The listener used to if the destination node changes.
     */
    private transient ZTransformListener destNodeListener = null;

    //****************************************************************************
    //
    // Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new ZAnchorGroup
     */
    public ZAnchorGroup() {
    }

    /**
     * Constructs a new link group node with the specified node as a child of the
     * new group.
     * @param child Child of the new group node.
     */
    public ZAnchorGroup(ZNode child) {
	super(child);
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refNode The reference node to copy
     */
    public void duplicateObject(ZAnchorGroup refNode) {
	super.duplicateObject(refNode);

	destNode = refNode.destNode;
	if (refNode.destBounds != null) {
	    destBounds = (Rectangle2D)refNode.destBounds.clone();
	}
    }

    /**
     * Duplicates the current node by using the copy constructor.
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     *
     * @return A copy of this node.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZAnchorGroup copy;

	objRefTable.reset();
	copy = new ZAnchorGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Disposes of this anchor group when it is no longer used.
     */
    public void finalize() {
				// Remove the destination transform listener if there is one
	if ((destNode != null) && (destNodeListener != null)) {
	    destNode.editor().getTransformGroup().removeTransformListener(destNodeListener);
	    destNodeListener = null;
	}
    }

    //****************************************************************************
    //
    // Get/Set pairs
    //
    //***************************************************************************

    /**
     * Determine the node that is the destination of this link.
     * @return The destination node, or null if none.
     */
    public ZNode getDestNode() {
	return destNode;
    }

    /**
     * Set the node destination of this link.  Setting the link destination
     * to be a node clears the bounds destination if there was one.
     * @param node The node that is the destination of this link.
     * @param camera The camera the link is going to be primarily viewed within.  This is used to determine the appropriate visual display of the link.
     * @see #setDestBounds
     */
    public void setDestNode(ZNode node, ZCamera camera) {
				// First, remove the old listener if there is one
	if ((destNode != null) && (destNodeListener != null)) {
	    destNode.editor().getTransformGroup().removeTransformListener(destNodeListener);
	    destNodeListener = null;
	}

				// Then, set up the new destination node
	destNode = node;
	destBounds = null;
	updateLinkComponent(camera);

				// Finally, create a new transform listener so we get
				// notified whenever the destination node transform changes
	if (destNode != null) {
	    destNodeListener = new ZTransformListener() {
		public void transformChanged(ZTransformEvent e) {
		    destPt = null;
		    updateLinkComponent(null);
		}
	    };
	    destNode.editor().getTransformGroup().addTransformListener(destNodeListener);
	}
    }

    /**
     * Determine the bounds that are the destination of this link.
     * @return The destination bounds, or null if none.
     */
    public Rectangle2D getDestBounds() {
	return destBounds;
    }

    /**
     * Set the bounds destination of this link.  Setting the bounds destination
     * to be a node clears the link destination if there was one.
     * @param bounds The bounds (in global coordinates) that is the destination of this link.
     * @param camera The camera the link is going to be primarily viewed within.  This is used to determine the appropriate visual display of the link.
     * @see #setDestNode
     */
    public void setDestBounds(Rectangle2D bounds, ZCamera camera) {
				// First, remove the old node listener if there is one
	if ((destNode != null) && (destNodeListener != null)) {
	    destNode.editor().getTransformGroup().removeTransformListener(destNodeListener);
	    destNodeListener = null;
	}

	destNode = null;
	destBounds = bounds;
	destPt = null;
	updateLinkComponent(camera);

	if (linkNode != null) {
	    ZVisualComponent linkComponent = linkNode.getVisualComponent();
	    if (linkComponent instanceof ZPenColor) {
		((ZPenColor)linkComponent).setPenColor(boundsColor);
	    }
	}
    }

    //****************************************************************************
    //
    // Other methods
    //
    //***************************************************************************

    /**
     * Trap computeBounds requests as it indicates that the anchor source has changed,
     * and the visual link needs to be updated.
     */
    public void computeBounds() {
	super.computeBounds();

	srcPt = null;
	updateLinkComponent(null);
    }

    /**
     * Internal method to create the visual component
     * that represents the link.  Applications can
     * change visual representation of a selected object
     * by extending this class, and overriding this method.
     * @return the visual component that represents the selection.
     */
    protected ZVisualComponent createLinkComponent() {
	ZPolyline linkComponent;

	linkComponent = new ZPolyline();
	linkComponent.setArrowHead(ZPolyline.ARROW_LAST);

	return linkComponent;
    }

    /**
     * Set the source point of the link for purposes of visually indicating the link.
     * @param pt The source point in global coordinates
     */
    public void setSrcPt(Point2D pt) {
	if (srcPt == null) {
	    srcPt = new Point2D.Float();
	}
	srcPt.setLocation(pt);

	if ((destPt != null) && (linkNode != null)) {
	    ZVisualComponent linkComponent = linkNode.getVisualComponent();
	    if (linkComponent instanceof ZPolyline) {
		((ZPolyline)linkComponent).setCoords(srcPt, destPt);
	    }
	}
    }

    /**
     * Set the destination point of the link for purposes of visually indicating the link.
     * @param pt The destination point in global coordinates
     */
    public void setDestPt(Point2D pt) {
	if (destPt == null) {
	    destPt = new Point2D.Float();
	}
	destPt.setLocation(pt);

	if ((srcPt != null) && (linkNode != null)) {
	    ZVisualComponent linkComponent = linkNode.getVisualComponent();
	    if (linkComponent instanceof ZPolyline) {
		((ZPolyline)linkComponent).setCoords(srcPt, destPt);
	    }
	}
    }

    /**
     * Update the visual component that represents the link.
     * This should be called whenever any internal state has changed that the visual link
     * depends on.
     * @param camera The camera the link is going to be primarily viewed within.  This is used to determine the appropriate visual display of the link.
     */
    public void updateLinkComponent(ZCamera camera) {
	ZBounds bounds;
	Point2D pt;

				// Nothing to do if no visual link
	if (linkNode == null) {
	    return;
	}

				// First set the pen width and color
	ZVisualComponent linkComponent = linkNode.getVisualComponent();
	if (linkComponent instanceof ZPenColor) {
	    if (destBounds != null) {
		((ZPenColor)linkComponent).setPenColor(boundsColor);
	    } else {
		((ZPenColor)linkComponent).setPenColor(nodeColor);
	    }
	}
	if (camera != null) {
	    if (linkComponent instanceof ZStroke) {
		((ZStroke)linkComponent).setPenWidth(DEFAULT_CONNECTOR_WIDTH / camera.getMagnification());
	    }
	}

				// Then set the source bounds
	pt = srcPt;
	if (pt == null) {
	    bounds = new ZBounds();
	    for (int i=0; i<numChildren; i++) {
		bounds.add(children[i].getBounds());
	    }
	    if (!bounds.isEmpty()) {
		pt = new Point2D.Float((float)(bounds.getX() + 0.5*bounds.getWidth()), (float)(bounds.getY() + 0.5*bounds.getHeight()));
		localToGlobal(pt);
	    }
	}
	if (pt != null) {
	    setSrcPt(pt);
	}

				// Finally set the destination bounds
	pt = destPt;
	if (pt == null) {
	    bounds = null;
	    if (destNode != null) {
				// Set destination point to center of destination object
		bounds = destNode.getGlobalBounds();
	    } else if (destBounds != null) {
				// Set destination point to center of destination bounds
		bounds = new ZBounds(destBounds);
	    }
	    if ((bounds != null) && !bounds.isEmpty()) {
		globalToLocal(bounds);
		pt = new Point2D.Float((float)(bounds.getX() + 0.5*bounds.getWidth()), (float)(bounds.getY() + 0.5*bounds.getHeight()));
		localToGlobal(pt);
	    }
	}
	if (pt != null) {
	    setDestPt(pt);
	}
    }

    /**
     * Specify whether the visual depiction of this link should be visible or not.
     * @param visible True if the link should be shown, or false otherwise.
     * @param camera The camera the link is going to be primarily viewed within.  This is used to determine the appropriate visual display of the link.
     */
    public void setVisible(boolean visible, ZCamera camera) {
	if (visible) {
				// Make link visible, so create a new one if there isn't one already (and this has a parent & camera)
	    ZGroup parent = getParent();
	    if ((linkNode == null) && (parent != null) && (camera != null)) {
		ZVisualComponent linkComponent = createLinkComponent();
		linkNode = new ZVisualLeaf(linkComponent);
		linkNode.setPickable(false);
		linkNode.setFindable(false);
				// Add the link above other things visible from the camera
		ZLayerGroup[] layers = camera.getLayersReference();
		layers[camera.getNumLayers() - 1].addChild(linkNode);
	    }
	    updateLinkComponent(camera);    // And, then update the visual link
	} else {
				// Make link invisible, so remove it
	    if (linkNode != null) {
		linkNode.getParent().removeChild(linkNode);
		linkNode = null;
	    }
	}
    }

    /**
     * Determine if the visual depiction of this link is currently visible.
     * @return True if the link is visible.
     */
    public boolean isVisible() {
	if (linkNode != null) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Follow the link, animating the viewpoint in the specified camera to the link destination.
     * @param camera The camera to animate
     */
    public void follow(ZCamera camera) {
	Rectangle2D bounds = null;

				// First determine the endpoint bounds
	if (destNode != null) {
	    bounds = destNode.getGlobalBounds();
	} else if (destBounds != null) {
	    bounds = destBounds;
	}

				// Then animate to the destination
	if (bounds != null) {
	    ZDrawingSurface surface = camera.getDrawingSurface();
	    if (surface != null) {
		surface.setInteracting(true);
	    }
	    camera.center(bounds, LINK_ANIMATION_TIME, surface);
	    if (surface != null) {
		surface.setInteracting(false);
	    }
	}
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = super.dump();

	if (destNode != null) {
	    str += "\n Destination node: " + destNode;
	}
	if (destBounds != null) {
	    str += "\n Destination bounds: " + destBounds;
	}

	return str;
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out);

	if (destNode != null) {
	    out.writeState("ZNode", "destNode", destNode);
	}
	if (destBounds != null) {
	    out.writeState("java.awt.geom.Rectangle2D", "destBounds", destBounds);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

	if (destNode != null) {
	    out.addObject(destNode);
	}
    }

    /**
     * Set some state of this object as it gets read back in.
     * After the object is created with its default no-arg constructor,
     * this method will be called on the object once for each bit of state
     * that was written out through calls to ZObjectOutputStream.writeState()
     * within the writeObject method.
     * @param fieldType The fully qualified type of the field
     * @param fieldName The name of the field
     * @param fieldValue The value of the field
     */
    public void setState(String fieldType, String fieldName, Object fieldValue) {
	super.setState(fieldType, fieldName, fieldValue);

	if (fieldName.compareTo("destNode") == 0) {
	    setDestNode((ZNode)fieldValue, null);
	} else if (fieldName.compareTo("destBounds") == 0) {
	    setDestBounds((Rectangle2D)fieldValue, null);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
				// write local class
	out.defaultWriteObject();

				// write Rectangle2D destBounds
	if (destBounds == null) {
	    out.writeBoolean(false);
	} else {
	    out.writeBoolean(true);
	    out.writeDouble(destBounds.getX());
	    out.writeDouble(destBounds.getY());
	    out.writeDouble(destBounds.getWidth());
	    out.writeDouble(destBounds.getHeight());
	}
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
				// read local class
	in.defaultReadObject();

				// read Rectangle2D destBounds
	if (in.readBoolean()) {
	    double x, y, w, h;
	    x = in.readDouble();
	    y = in.readDouble();
	    w = in.readDouble();
	    h = in.readDouble();
	    destBounds = new Rectangle2D.Float((float)x, (float)y, (float)w, (float)h);
	}
    }
}
