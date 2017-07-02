/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.io.IOException;
import java.awt.geom.AffineTransform;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZSceneGraphObject</b> is the base class for all objects in the Jazz scenegraph.
 * It provides support for the basic shared methods between all nodes and visual components.
 * <P>
 * <B> Coordinate Systems </B><br>
 * Application developers must understand the basic coordinate systems used in Jazz.
 * The basic coordinate system has its origin at the upper-left.  The X-axis increases positively
 * to the right, and the Y-axis increase positively down.
 * <P>
 * Because certain node types define transforms which define a new relative coordinate system,
 * it is important to realize that typically, objects are not placed in "global" coordinates.
 * Rather, every object is defined in their own "local" coordinate system.  The relationship
 * of the local coordinate system to the global coordinate system is determined by the
 * series of transforms between that object, and the root of the scenegraph.
 * <P>
 * All Jazz operations occur in local coordinates.  For instance, coordinates and rectangles
 * object receiving those parameters.  In addition, objects cache their bounds within in
 * their local coordinates.  This is efficient because it means that if an object changes
 * itself, none of its ancestors need to be notified in any way.
 *
 * @author  Ben Bederson
 * @see     ZNode
 * @see     ZVisualComponent
 */
public abstract class ZSceneGraphObject implements ZSerializable, Serializable {
				// Default values
    static public final boolean volatileBounds_DEFAULT = false;    // True if this node has volatile bounds (shouldn't be cached)

    /**
     * The single instance of the object reference table used for cloning scenegraph trees.
     */
    static protected ZObjectReferenceTable objRefTable = ZObjectReferenceTable.getInstance();

    /**
     * The bounds occupied by this object in its own local coordinate system.
     * These bounds are not affected by this node's parents.
     * These bounds represent any content that this node contains
     * (including any elements stored by subtypes).
     */
    protected ZBounds bounds;

    /**
     *  True if this node is specifically set to have volatile bounds
     */
    private boolean volatileBounds = volatileBounds_DEFAULT;

    //****************************************************************************
    //
    //                 Constructors
    //
    //***************************************************************************

    /**
     * Constructs an empty scenegraph object.
     * <P>
     * Most objects will want to store their
     * bounds, and so we allocate bounds here.
     * However, if a particular object is implemented by
     * computing its bounds every time it is asked instead of allocating
     * it, then it can free up the bounds allocated here.
     */
    protected ZSceneGraphObject() {
	bounds = new ZBounds();
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refObj The reference object to copy
     */
    public void duplicateObject(ZSceneGraphObject refObj) {
	if (refObj.bounds != null) {
	    bounds = (ZBounds)(refObj.bounds.clone());
	}
	volatileBounds = refObj.volatileBounds;
    }

   /**
     * Return a copy of the bounds of the subtree rooted at this node in local coordinates.
     * If a valid cached value is available, this method returns it.  If a
     * valid cache is not available (i.e. the object is volatile) then the bounds are
     * recomputed, cached and then returned to the caller.
     * @return The bounds of the subtree rooted at this in local coordinates.
     */
    public ZBounds getBounds() {
	if (getVolatileBounds()) {
	    computeBounds();
	}
	return (ZBounds)(bounds.clone());
    }

   /**
     * Internal method to return the original bounds of the subtree rooted at this node in local coordinates.
     * If a valid cached value is available, this method returns it.  If a
     * valid cache is not available (i.e. the object is volatile) then the bounds are
     * recomputed, cached and then returned to the caller.
     * @return The bounds of the subtree rooted at this in local coordinates.
     */
    ZBounds getBoundsReference() {
	if (getVolatileBounds()) {
	    computeBounds();
	}
	return bounds;
    }

    /**
     * Internal method to specify the bounds of this object.
     */
    void setBounds(ZBounds newBounds) {
	bounds = newBounds;
    }

    /**
     * Recomputes and caches the bounds for this node.  Generally this method is
     * called by reshape when the bounds have changed, and it should rarely
     * directly elsewhere.
     */
    protected void computeBounds() {
    }

    /**
     * Repaint causes the portions of the surfaces that this object
     * appears in to be marked as needing painting, and queues events to cause
     * those areas to be painted. The painting does not actually
     * occur until those events are handled.
     * If this object is visible in multiple places because more than one
     * camera can see this object, then all of those places are marked as needing
     * painting.
     * <p>
     * Scenegraph objects should call repaint when their internal
     * state has changed and they need to be redrawn on the screen.
     * <p>
     * Important note : Scenegraph objects should call reshape() instead
     * of repaint() if the internal state change effects the bounds of the
     * shape in any way (e.g. changing penwidth, selection, transform, adding
     * points to a line, etc.)
     *
     * @see #reshape()
     */
    public void repaint() {
	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println("ZSceneGraphObject.repaint: this = " + this);
	}
    }

    /**
     * Reshape causes the portion of the surface that this object
     * appears in before the bounds are changed to be marked as needing
     * painting, and queues events to cause those areas to be painted.
     * Then, the bounds are updated, and finally, the portion of the
     * screen corresponding to the newly computed bounds are marked
     * for repainting.
     * If this object is visible in multiple places because more than one
     * camera can see this object, then all of those places are marked as needing
     * painting.
     * <p>
     * Scenegraph objects should call reshape when their internal
     * state has changed in such a way that their bounds have changed.
     * <p>
     * Important note : Scenegraph objects should call repaint() instead
     * of reshape() if the bounds of the shape have not changed.
     *
     * @see #repaint()
     */
    public void reshape() {
        repaint();
        updateBounds();
        repaint();
    }

    /**
     * Internal method that causes this node and all of its ancestors
     * to recompute their bounds.
     */
    protected void updateBounds() {
	computeBounds();
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
    public final boolean getVolatileBounds() {
	return volatileBounds;
    }

    /**
     * Specifies whether or not this node is volatile.
     * All parents of this node are also volatile when this is volatile.
     * @param v the new specification of whether this node is volatile.
     * @see #getVolatileBounds()
     */
    public void setVolatileBounds(boolean v) {
	volatileBounds = v;
	updateVolatility();
    }

    /**
     * Internal method to compute and cache the volatility of a node,
     * to recursively call the parents to compute volatility.
     * All parents of this node are also volatile when this is volatile.
     * @see #setVolatileBounds(boolean)
     * @see #getVolatileBounds()
     */
    protected void updateVolatility() {
    }

    /**
     * Manage dangling references when scenegraph objects are cloned.
     * This gets called after a sub-graph of the scenegraph has been cloned,
     * and it is this method's responsibility to update any internal references
     * to the original portions of the scenegraph.  Subtypes that define new
     * references should override this method to manage their references.
     * @param objRefTable The table that maintains the relationships between cloned objects.
     */
    public void updateObjectReferences(ZObjectReferenceTable objRefTable) {
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = toString();
	ZBounds b = getBounds();
	if (b.isEmpty()) {
	    str += ": Bounds = [Empty]";
	} else {
	    str += ": Bounds = [x=" + (float)b.getX() + ", y=" + (float)b.getY() +
		", w=" + (float)b.getWidth() + ", h=" + (float)b.getHeight() + "]";
	}
	if (getVolatileBounds()) {
	    str += "\n Volatile";
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
	if (volatileBounds != volatileBounds_DEFAULT) {
	    out.writeState("boolean", "volatileBounds", volatileBounds);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
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
	if (fieldName.compareTo("volatileBounds") == 0) {
	    setVolatileBounds(((Boolean)fieldValue).booleanValue());
	}
    }
}
