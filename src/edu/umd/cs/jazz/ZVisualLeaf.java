/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.lang.reflect.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZVisualLeaf</b> is a leaf node that has one or more visual components
 * that can be rendered. Many applications will attach all of their visual
 * components to ZVisualLeafs.  
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author Ben Bederson
 */
public class ZVisualLeaf extends ZLeaf implements ZSerializable, Serializable {
    /**
     * The visual components associated with this leaf.
     */
    private ZVisualComponent[] visualComponents = null;

    /**
     * The actual number of visual components.
     */
    private int numVisualComponents = 0;

    /**
     * Cached volatility computation.
     */
    private transient boolean cacheVolatile = false;

    //****************************************************************************
    //
    //                Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new empty visual leaf node.
     */
    public ZVisualLeaf() {
	numVisualComponents = 0;
	visualComponents = new ZVisualComponent[1];
    }

    /**
     * Constructs a new visual leaf node with the specified visual component.
     * @param visualComponent The new visual component that this leaf displays.
     */
    public ZVisualLeaf(ZVisualComponent visualComponent) {
	numVisualComponents = 0;
	visualComponents = new ZVisualComponent[1];
	setVisualComponent(visualComponent);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZVisualLeaf newObject = (ZVisualLeaf)super.duplicateObject();

	if (visualComponents != null) {
	    newObject.visualComponents = (ZVisualComponent[])visualComponents.clone();
	}

	return newObject;	
    }
    
    /**
     * Trims the capacity of the array that stores the visual components list to
     * the actual number of points.  Normally, the visual components list array can be
     * slightly larger than the number of points in the visual components list.
     * An application can use this operation to minimize the storage of a
     * visual components list.
     */
    public void trimToSize() {
	ZVisualComponent[] newVisualComponents = new ZVisualComponent[numVisualComponents];
	for (int i=0; i<numVisualComponents; i++) {
	    newVisualComponents[i] = visualComponents[i];
	}
	visualComponents = newVisualComponents;
    }

    /**
     * Add a new visual component to this leaf node.
     * If this leaf already contains this component, then nothing happens.
     * @param visualComponent The visual component to be added.
     */
    public void addVisualComponent(ZVisualComponent visualComponent) {
	if (visualComponent == null) {
	    return;
	}
				// Check if visualComponent already exists
	for (int i=0; i<numVisualComponents; i++) {
	    if (visualComponents[i] == visualComponent) {
		return;
	    }
	}

				// If not, add it - growing array if necessary
	try {
	    visualComponents[numVisualComponents] = visualComponent;
	} catch (ArrayIndexOutOfBoundsException e) {
	    ZVisualComponent[] newVisualComponents = new ZVisualComponent[(numVisualComponents == 0) ? 1 : (2 * numVisualComponents)];
	    System.arraycopy(visualComponents, 0, newVisualComponents, 0, numVisualComponents);
	    visualComponents = newVisualComponents;
	    visualComponents[numVisualComponents] = visualComponent;
	}
	numVisualComponents++;
	visualComponent.addParent(this);
	updateVolatility();
	reshape();
    }

    /**
     * Remove a visual component from this leaf node.
     * If this leaf didn't already contains this component, then nothing happens.
     * @param visualComponent The visual component to be removed.
     */
    public void removeVisualComponent(ZVisualComponent visualComponent) {
	if (visualComponent == null) {
	    return;
	}
				// Check if visualComponent already exists
	int i, j;
	boolean found = false;
	for (i=0; i<numVisualComponents; i++) {
	    if (visualComponents[i] == visualComponent) {
		found = true;
		break;
	    }
	}

	if (found) {
	    for (j=i; j<(numVisualComponents-1); j++) {
		visualComponents[j] = visualComponents[j+1];
	    }
	    numVisualComponents--;
	    visualComponent.removeParent(this);
	    updateVolatility();
	    reshape();
	}
    }

    /**
     * Set the visual component associated with this leaf node.
     * If this node previously had any visual components associated with it,
     * then those components will be replaced with the new one.
     * @param visualComponent The new visual component for this node.
     */
    public void setVisualComponent(ZVisualComponent visualComponent) {
	clearVisualComponents();
	addVisualComponent(visualComponent);
    }

    /**
     * Return the visual components associated with this visual leaf.
     */
    public final ZVisualComponent[] getVisualComponents() {
	if (numVisualComponents == 0) {
	    return(null);
	}

	return(visualComponents);
    }

    /**
     * Return the first visual component associated with this leaf,
     * or null if there are none.
     */
    public final ZVisualComponent getFirstVisualComponent() {
	if (numVisualComponents == 0) {
	    return(null);
	}

	return visualComponents[0];
    }

    /**
     * Remove all visual components from this visual leaf.
     */
    public void clearVisualComponents() {
	for (int i=0; i<numVisualComponents; i++) {
	    repaint();
	    visualComponents[i].removeParent(this);
	}
	numVisualComponents = 0;
	updateVolatility();
    }

    /**
     * Internal method to compute and cache the volatility of a node,
     * to recursively call the parents to compute volatility.
     * All parents of this node are also volatile when this is volatile.
     * A leaf is volatile if either the node or any of its visual components
     * are volatile.
     * @see #setVolatileBounds(boolean)
     * @see #getVolatileBounds()
     */
    protected void updateVolatility() {
				// If this node set to volatile, then it is volatile
	cacheVolatile = volatileBounds;
	if (!cacheVolatile) {
				// Else, if any of its visual components are volatile, then it is volatile
	    for (int i=0; i<numVisualComponents; i++) {
		if (visualComponents[i].getVolatileBounds()) {
		    cacheVolatile = true;
		    break;
		}
	    }
	}
				// Update parent's volatility
	if (parent != null) {
	    parent.updateVolatility();
	}
    }

    /**
     * Determines if this node is volatile.
     * A node is considered to be volatile if it is specifically set
     * to be volatile with {@link ZNode#setVolatileBounds}.
     * All parents of this node are also volatile when this is volatile.
     * <p>
     * Volatile objects are those objects that change regularly, such as an object
     * that is animated, or one whose rendering depends on its context.
     * @return true if this node is volatile
     * @see #setVolatileBounds(boolean)
     */
    public boolean getVolatileBounds() {
	return cacheVolatile;
    }

    //****************************************************************************
    //
    // Painting related methods
    //
    //***************************************************************************

    /**
     * Renders this node which results its visual components getting painted.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param renderContext The graphics context to use for rendering.
     */
    public void render(ZRenderContext renderContext) {
				// Paint all visual components
	for (int i=0; i<numVisualComponents; i++) {
	    visualComponents[i].render(renderContext);
	}

	if (ZDebug.debug) {
	    ZDebug.incPaintCount();	// Keep a count of how many things have been rendered
				        // Draw bounding box if requested for debugging
	    if (ZDebug.showBounds) {
		Graphics2D g2 = renderContext.getGraphics2D();
		g2.setColor(new Color(60, 60, 60));
		g2.setStroke(new BasicStroke((float)(1.0 / renderContext.getCompositeMagnification()),
					     BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(getBoundsReference());
	    }
	}
    }

    /**
     * Recomputes and caches the bounds for this node.  Generally this method is
     * called by reshape when the bounds have changed, and it should rarely
     * directly elsewhere.  A ZVisualLeaf bounds is the bounds of the union 
     * of its visual components.
     */
    protected void computeBounds() {
	bounds.reset();

	if (numVisualComponents > 0) {
	    for (int i=0; i<numVisualComponents; i++) {
		bounds.add((Rectangle2D)visualComponents[i].getBoundsReference());
	    }
	}
    }

    //****************************************************************************
    //
    //			Other Methods
    //
    //****************************************************************************

    /**
     * Returns true if any of this node's visual components
     * are under the specified rectangle, and builds a ZSceneGraphPath to the node.
     * Only returns "pickable" nodes.
     * @param rect Coordinates of pick rectangle in local coordinates
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return The picked node, or null if none
     * @see ZDrawingSurface#pick(int, int)
     */
     public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	 ZVisualComponent vc;

	if (isPickable() && (numVisualComponents > 0)) {
	    path.push(this);

	    for (int i=0; i<numVisualComponents; i++) {
		vc = visualComponents[i];
		if (vc.pick(rect, path)) {
		    if (!(vc instanceof ZCamera)) {
				// Set object here rather than in component so components don't
				// have to worry about implementation of paths.
			path.setObject(vc);
		    }
		    return true;
		}
	    }
	    path.pop(this);
	}

	return false;
    }

    /**
     * Return a copy of the bounds of this node's visual components in local coordinates.
     * If this node does not have any visual components, then this returns null.
     * @return The union of this node's visual component's bounds in local coordinates
     * (or null if there are no visual components).
     */
    public ZBounds getVisualComponentBounds() {
	ZBounds bounds = null;

	if (numVisualComponents > 0) {
	    bounds = new ZBounds();
	    for (int i=0; i<numVisualComponents; i++) {
		bounds.add((Rectangle2D)visualComponents[i].getBounds());
	    }
	}
	return bounds;
    }

    /**
     * Return a copy of the bounds of this node's visual components in global coordinates.
     * If this node does not have any visual components, then this returns null.
     * Note that global bounds are not cached, and this method involves some computation.
     * @return The visual component's bounds in global coordinates 
     * (or null if there are no visual components).
     */
    public ZBounds getVisualComponentGlobalBounds() {
	ZBounds globalBounds = null;
	if (numVisualComponents > 0) {
	    globalBounds = getVisualComponentBounds();
	    AffineTransform at = getLocalToGlobalTransform();
	    globalBounds.transform(at);
	}

	return globalBounds;
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

	if (numVisualComponents > 0) {
	    ZVisualComponent[] copyVisualComponents = getVisualComponents();
	    out.writeState("List", "visualComponents", Arrays.asList(copyVisualComponents));
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

	for (int i=0; i<numVisualComponents; i++) {
	    out.addObject(visualComponents[i]);
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

	if (fieldName.compareTo("visualComponents") == 0) {
	    ZVisualComponent visualComponent;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		visualComponent = (ZVisualComponent)i.next();
		addVisualComponent(visualComponent);
	    }
				// For backwards compatability, we read in this value
				// for a single visual component
	} else if (fieldName.compareTo("visualComponent") == 0) {
	    ZVisualComponent visualComponent = (ZVisualComponent)fieldValue;
	    setVisualComponent(visualComponent);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }
}
