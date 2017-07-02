/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZVisualComponent</b> is the base class for objects that actually get rendered.
 * A visual component primarily implements three methods: paint(), pick(), and computeBounds().
 * New sub-classes must override at least paint() and computeBounds(), and will often
 * choose to override pick() as well.
 *
 * @author Ben Bederson
 * @author Britt McAlister
 */
public class ZVisualComponent extends ZSceneGraphObject implements ZSerializable, Serializable {
    /**
     * The parents of this visual component.
     * This is guaranteed to point to a valid array,
     * even if this group does not have any parents.
     */
    ZNode[] parents = null;

    /**
     * The actual number of parents of this component
     */
    int numParents = 0;

    //****************************************************************************
    //
    //               Constructors
    //
    //***************************************************************************

    /**
     * Default constructor for visual component.
     */
    public ZVisualComponent() {
	parents = new ZNode[1];
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refVC The reference visual component to copy
     */
    public void duplicateObject(ZVisualComponent refVC) {
	super.duplicateObject(refVC);

	parents = new ZNode[1];
    }

    /**
     * Duplicates the current object by using the copy constructor.
     * The portion of the reference object that is duplicated is that necessary to reuse the object
     * in a new place within the scenegraph, but the new object is not inserted into any scenegraph.
     * The object must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     *
     * @return A copy of this visual component.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZVisualComponent copy;

	objRefTable.reset();
	copy = new ZVisualComponent();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Trims the capacity of the array that stores the parents list points to
     * the actual number of points.  Normally, the parents list arrays can be
     * slightly larger than the number of points in the parents list.
     * An application can use this operation to minimize the storage of a
     * parents list.
     */
    public void trimToSize() {
	ZNode[] newParents = new ZNode[numParents];
	for (int i=0; i<numParents; i++) {
	    newParents[i] = parents[i];
	}
	parents = newParents;
    }

    //****************************************************************************
    //
    //			Get/Set  pairs
    //
    //***************************************************************************

    /**
     * Internal method to compute and cache the volatility of a component,
     * to recursively call the parents to compute volatility.
     * All parents of this component are also volatile when this is volatile.
     * @see #setVolatileBounds(boolean)
     * @see #getVolatileBounds()
     */
    protected void updateVolatility() {
				// Update parent's volatility
	if (parents != null) {
	    for (int i=0; i<numParents; i++) {
		parents[i].updateVolatility();
	    }
	}
    }

    /**
     * Return a copy of the array of parents of this node.
     * This method always returns an array, even when there
     * are no children.
     * @return the parents of this node.
     */
    public ZNode[] getParents() {
	ZNode[] copyParents = new ZNode[numParents];
	System.arraycopy(parents, 0, copyParents, 0, numParents);

	return copyParents;
    }

    /**
     * Internal method to add a node to be a new parent of this component.  The new node
     * is added to the end of the list of this node's parents;
     * @param parent The new parent node.
     */
    void addParent(ZNode parent) {
	try {
	    parents[numParents] = parent;
	} catch (ArrayIndexOutOfBoundsException e) {
	    ZNode[] newParents = new ZNode[parents.length * 2];
	    System.arraycopy(parents, 0, newParents, 0, numParents);
	    parents = newParents;
	    parents[numParents] = parent;
	}
	numParents++;
    }

    /**
     * Internal method to remove the specified parent node from this visual component.
     * If the specified node wasn't a parent of this node,
     * then nothing happens.
     * @param parent The parent to be removed.
     */
    void removeParent(ZNode parent) {
	for (int i=0; i<numParents; i++) {
				// Find parent within parent list
	    if (parent == parents[i]) {
				// Then, slide down other parents, effectively removing this one
		for (int j=i; j < (numParents - 1); j++) {
		    parents[j] = parents[j+1];
		}
		parents[numParents - 1] = null;
		numParents--;
		break;
	    }
	}
    }

    /**
     * This is a utility function to determine if the specified rectangle
     * intersects the bounds of this visual component.
     * @param rect the rectangle that this method tests for intersection with
     * @return true if this component's local bounds intersects the specified rectangle
     */
    public boolean pickBounds(Rectangle2D rect) {
	if (getBoundsReference().intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())) {
	    return true;
	}
	return false;
    }

    /**
     * Determines whether the specified rectangle (in local coordinates) should "pick" this visual component.
     * Picking is typically used to determine if a pointer is over an object, and
     * thus pick should be implemented to retur true if the rectangle intersects the object.
     * <p>
     * The implementation of this pick method for the base visual component returns
     * true if the rectangle intersects the bounds of the component.  If a sub-class
     * wants more detailed picking, then it must extend this class.  For example,
     * a circle may only consider itself to be picked if the pointer is within
     * the circle - rather than within the rectangular bounds.
     * @param rect The rectangle that is picking this visual component in local coordinates.
     * @return true if the rectangle picks this visual component
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	return pickBounds(rect);
    }

    /**
     * Renders this visual component.  The rendercontext contains various
     * aspects of the rendering state including the Graphics2D and the
     * camera being rendered within.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     * <P>
     * Note that a subclass can either extend this directly, or can
     * just extend paint if it does not access to the render context.
     * @param renderContext The graphics context to use for rendering.
     * @see #paint(Graphics2D)
     */
    public void render(ZRenderContext renderContext) {
	paint(renderContext.getGraphics2D());
    }

    /**
     * Renders this visual component.
     * <p>
     * It is guaranteed that
     * the transform, clip, and composite of the Graphics2D will be set properly
     * for this component.
     * However, the color, font, and stroke are unset, and the visual component
     * must set those things as needed.  The visual component is
     * not obligated to restore any aspect of the Graphics2D state.
     * <P>
     * Note that a subclass can either extend this directly, or can
     * just render if it needs access to the render context for a more complex object.
     * @param Graphics2D The graphics to render with
     * @see #render(ZRenderContext)
     */
    public void paint(Graphics2D g2) {
    }

    /*
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
	for (int i=0; i<numParents; i++) {
	    parents[i].repaint();
	}
    }

    /**
     * This causes just the specified bounds of this visual component to be repainted.
     * Note that the input parameter may be modified as a result of this call.
     * @param repaintBounds The bounds to repaint
     * @see #repaint()
     */
    public void repaint(ZBounds repaintBounds) {
	for (int i=0; i<numParents; i++) {
	    parents[i].repaint(repaintBounds);
	}
    }

    /**
     * Internal method that causes this node and all of its ancestors
     * to recompute their bounds.
     */
    protected void updateBounds() {
	computeBounds();
	for (int i=0; i<numParents; i++) {
	    parents[i].updateBounds();
	}
    }

    /**
     * Internal method to notify the visual component that its bounds have
     * been updated, and its parents should now update their bounds
     */
    protected void boundsUpdated() {
	for (int i=0; i<numParents; i++) {
	    parents[i].updateBounds();
	}
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
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);
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
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }
}
