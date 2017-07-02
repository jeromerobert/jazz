/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.lang.reflect.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZVisualLeaf</b> is a leaf node that has a visual component that can be rendered.
 *
 * @author Ben Bederson
 */
public class ZVisualLeaf extends ZLeaf implements ZSerializable, Serializable {
    /**
     * The visual component associated with this leaf.
     */
    private ZVisualComponent visualComponent = null;

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
    }

    /**
     * Constructs a new visual leaf node with the specified visual component.
     * @param newVisualComponent The new visual component that this leaf displays.
     */
    public ZVisualLeaf(ZVisualComponent newVisualComponent) {
	setVisualComponent(newVisualComponent);
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
    public void duplicateObject(ZVisualLeaf refNode) {
	super.duplicateObject(refNode);

	cacheVolatile = refNode.cacheVolatile;

	try {
				// Need to use reflection to call 'duplicateObject' method
				// in order to get the method called on the right object type.
	    Class cl = refNode.visualComponent.getClass();
	    visualComponent = (ZVisualComponent)cl.newInstance();
	    Class[] parameterTypes = new Class[1];
	    parameterTypes[0] = cl;
	    Method method = cl.getMethod("duplicateObject", parameterTypes);
	    Object[] args = new Object[1];
	    args[0] = refNode.visualComponent;
	    method.invoke(visualComponent, args);

	    objRefTable.addObject(refNode.visualComponent, visualComponent);
	}
	catch (IllegalAccessException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (IllegalArgumentException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (InvocationTargetException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (NullPointerException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (InstantiationException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (ExceptionInInitializerError e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (SecurityException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
	}
	catch (NoSuchMethodException e) {
	    System.out.println("ZVisualLeaf.duplicateObject: " + e);
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
	ZVisualLeaf copy;

	objRefTable.reset();
	copy = new ZVisualLeaf();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Set the visual component associated with this leaf node.
     * If this node previously had a visual component associated with it,
     * than that component will be replaced with the new one.
     * @param newVisualComponent The new visual component for this node.
     */
    public void setVisualComponent(ZVisualComponent newVisualComponent) {
				// First remove old visual component if there was one
	if (visualComponent != null) {
	    repaint();
	    visualComponent.removeParent(this);
	}
				// Now, add new visual component
	visualComponent = newVisualComponent;
	if (visualComponent != null) {
	    visualComponent.addParent(this);
	}
	reshape();
    }

    /**
     * Return the visual component associated with this leaf,
     * or null if none.
     */
    public final ZVisualComponent getVisualComponent() {
	return visualComponent;
    }

    /**
     * Internal method to compute and cache the volatility of a node,
     * to recursively call the parents to compute volatility.
     * All parents of this node are also volatile when this is volatile.
     * A leaf is volatile if either the node or it's visual component
     * is volatile.
     * @see #setVolatileBounds(boolean)
     * @see #getVolatileBounds()
     */
    protected void updateVolatility() {
				// If this node set to volatile, then it is volatile
	cacheVolatile = getVolatileBounds();
	if (!cacheVolatile) {
				// Else, if its visual component is volatile, then it is volatile
	    ZVisualComponent vc = getVisualComponent();
	    if (vc != null) {
		cacheVolatile = vc.getVolatileBounds();
	    }
	}
				// Update parent's volatility
	if (parent != null) {
	    parent.updateVolatility();
	}
    }

    //****************************************************************************
    //
    // Painting related methods
    //
    //***************************************************************************

    /**
     * Renders this node which results in the node's visual component getting painted.
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
	if (visualComponent == null) {
	    return;
	}

				// Paint visual component
	visualComponent.render(renderContext);

	if (ZDebug.debug) {
	    ZDebug.incPaintCount();	// Keep a count of how many things have been rendered
				        // Draw bounding box if requested for debugging
	    if (ZDebug.showBounds) {
		Graphics2D g2 = renderContext.getGraphics2D();
		g2.setColor(new Color(60, 60, 60));
		g2.setStroke(new BasicStroke(1.0f / renderContext.getCompositeMagnification(),
					     BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(getBoundsReference());
	    }
	}
    }

    /**
     * Recomputes and caches the bounds for this node.  Generally this method is
     * called by reshape when the bounds have changed, and it should rarely
     * directly elsewhere.  A ZVisualLeaf bounds is the bounds of its visual component
     */
    protected void computeBounds() {
	bounds.reset();
	bounds.add(visualComponent.getBoundsReference());
    }

    //****************************************************************************
    //
    //			Other Methods
    //
    //****************************************************************************

    /**
     * Returns this node if the visual component referenced by this node
     * is under the specified rectangle.
     * Only returns "pickable" nodes.
     * @param rect Coordinates of pick rectangle in local coordinates
     * @param mag The magnification of the camera being picked within.
     * @return The picked node, or null if none
     * @see ZDrawingSurface#pick(int, int);
     */
     public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	if (isPickable() && (visualComponent != null)) {
	    path.push(this);
	    if (visualComponent.pick(rect, path)) {
	    	path.setObject(visualComponent);
	    	return true;
	    }
	    path.pop(this);
	}

	return false;
    }

    /**
     * Return a copy of the bounds of this node's visual component in local coordinates.
     * If this node does not have a visual component, then this returns null.
     * @return The visual component's bounds in local coordinates (or null if no visual component).
     */
    public ZBounds getVisualComponentBounds() {
	ZBounds bounds = null;
	if (visualComponent != null) {
	    bounds = visualComponent.getBounds();
	}

	return bounds;
    }

    /**
     * Return a copy of the bounds of this node's visual component in global coordinates.
     * If this node does not have a visual component, then this returns null.
     * Note that global bounds are not cached, and this method involves some computation.
     * @return The visual component's bounds in global coordinates (or null if no visual component).
     */
    public ZBounds getVisualComponentGlobalBounds() {
	ZBounds globalBounds = null;
	if (visualComponent != null) {
	    globalBounds = visualComponent.getBounds();
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

	if (visualComponent != null) {
	    out.writeState("ZVisualComponent", "visualComponent", visualComponent);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add visual component if there is one
	if (visualComponent != null) {
	    out.addObject(visualComponent);
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

	if (fieldName.compareTo("visualComponent") == 0) {
	    setVisualComponent((ZVisualComponent)fieldValue);
	}
    }
}
