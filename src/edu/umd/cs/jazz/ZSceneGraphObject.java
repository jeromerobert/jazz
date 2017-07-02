/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

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
 * All Jazz operations occur in local coordinates.  For instance, coordinates of rectangles
 * are specified in local coordinates.  In addition, objects maintain a bounding box which is
 * stored in local coordinates. 
 * <P> See the Jazz Tutorial for a more complete description of the scene graph.
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Ben Bederson
 * @see     ZNode
 * @see     ZVisualComponent
 */
public abstract class ZSceneGraphObject implements ZSerializable, Serializable, Cloneable {
				// Default values
    static public final boolean volatileBounds_DEFAULT = false;    // True if this node has volatile bounds (shouldn't be cached)

    /**
     * The single instance of the object reference table used for cloning scenegraph trees.
     */
    private static ZObjectReferenceTable objRefTable = ZObjectReferenceTable.getInstance();
    
    /**
     * Used to detect recursive calls to clone.
     */
    private static boolean inClone;

    /**
     * The bounding rectangle occupied by this object in its own local coordinate system.
     * Conceptually, the bounding rectangle is defined as the minimum rectangle that
     * would surround all of the geometry drawn by the node and its children. The bounding
     * rectangle's coordinates are in the node's local coordinates. That is, they are 
     * independant of any viewing transforms, or of transforms performed by parents 
     * of the node.
     */
    protected ZBounds bounds;

    /**
     *  True if this node is specifically set to have volatile bounds
     */
    protected boolean volatileBounds = volatileBounds_DEFAULT;

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
     * Return a copy of the bounds of the subtree rooted at this node in local coordinates.
     * If a valid cached value is available, this method returns it.  If a
     * valid cache is not available (i.e. the object is volatile) then the bounds are
     * recomputed, cached and then returned to the caller.
     * <P>
     * If the object is a context-sensitive object, then it may compute the bounds
     * based on the current render context.
     * @return The bounds of the subtree rooted at this in local coordinates.
     * @see ZRoot#getCurrentRenderContext
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
    protected ZBounds getBoundsReference() {
	if (getVolatileBounds()) {
	    computeBounds();
	}
	return bounds;
    }

    /**
     * Internal method to specify the bounds of this object.
     */
    protected void setBounds(ZBounds newBounds) {
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
    public boolean getVolatileBounds() {
	return volatileBounds;
    }

    /**
     * Specifies whether or not this node is volatile.
     * All parents of this node are also volatile when this is volatile.
     * <p>
     * Volatile objects are those objects that change regularly, such as an object
     * that is animated, or one whose rendering depends on its context.
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
     * Creates a copy of this scene graph object and all its children.<p>
     *
     * ZSceneGraphObject.duplicateObject() calls Object.clone() on this object, and
     * returns the newly cloned object. This results in a shallow copy of the object.<p>
     *
     * Subclasses override this method to modify the cloning behavior
     * for nodes in the scene graph. Typically, subclasses first invoke super.duplicateObject()
     * to get the default cloning behavior, and then take additional actions after this.
     * In particular, ZGroup.duplicateObject() first invokes super.duplicateObject(), 
     * and then calls duplicateObject() on all of the children in the group, so that the whole
     * tree beneath the group is cloned.
     *
     * Applications do not call duplicateObject directly. Instead, 
     * ZSceneGraphObject.clone() is used clone a scene graph object.
     */
    protected Object duplicateObject() {
	ZSceneGraphObject newObject;
	try {
	    newObject = (ZSceneGraphObject)super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException("Object.clone() failed: " + e);
	}

	if (bounds != null) {
	    newObject.bounds = (ZBounds)(bounds.clone());
	}

	objRefTable.addObject(this, newObject);

	return newObject;
    }

    /**
     * Updates references to scene graph nodes after a clone operation.<p>
     *
     * This method is invoked on cloned objects after the clone operation has been
     * completed. The objRefTable parameter is a table mapping from the original
     * uncloned objects to their newly cloned versions. Subclasses override this
     * method to update any internal references they have to scene graph nodes.
     * For example, ZNode's updateObjectReferences does:
     * 
     * <pre>
     *	    super.updateObjectReferences(objRefTable);
     *
     *	    // Set parent to point to the newly cloned parent, or to 
     *	    // null if the parent object was not cloned.
     *      parent = (ZNode)objRefTable.getNewObjectReference(parent);
     * </pre>
     *
     * @param objRefTable Table mapping from uncloned objects to their cloned versions.
     */
    protected void updateObjectReferences(ZObjectReferenceTable objRefTable) {

	if (!inClone) {
	    throw new RuntimeException("ZSceneGraphObject.updateObjectReferences: Called outside of a clone");
	}

    }


    /**
     * Clones this scene graph object and all its children and returns the newly 
     * cloned sub-tree. Applications must then add the sub-tree to the scene graph
     * for it to become visible.
     *
     * @return A cloned copy of this object.
     */
    public Object clone() {
	Object newObject;

	if (inClone) {

	    // Recursive call of clone (e.g. by a group to copy its children)
	    newObject = duplicateObject();

	} else {
	    try {
		inClone = true;
		objRefTable.reset();
		newObject = duplicateObject();
		
		// Updates cloned objects. This iterates through all the cloned 
		// objects in the reference table, notifying them to update their
		// internal references, passing in a reference
		// to objRefTable so it can be queried for original/new object mappings.

		for (Iterator iter = objRefTable.iterator() ; iter.hasNext() ;) {
		    ZSceneGraphObject clonedObject = (ZSceneGraphObject) iter.next();
		    clonedObject.updateObjectReferences(objRefTable);
		}

	    } finally {
		inClone = false;
	    }
	}
	return newObject;
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
	    str += ": Bounds = [x=" + b.getX() + ", y=" + b.getY() +
		", w=" + b.getWidth() + ", h=" + b.getHeight() + "]";
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
