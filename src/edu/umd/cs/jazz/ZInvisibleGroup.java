/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZInvisibleGroup</b> is a group node that completely hides its descendents.
 * It does not render anything, nor does it pick or find any children.  In addition,
 * An invisible group always has empty bounds.  An invisible group can be inserted
 * into the tree when an application wants to temporarily act as if a portion of
 * the scenegraph doesn't exist.
 *
 * @author Ben Bederson
 */
public class ZInvisibleGroup extends ZGroup implements ZSerializable {
    //****************************************************************************
    //
    //                  Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new empty invisible group node.
     */
    public ZInvisibleGroup() { }

    /**
     * Constructs a new invisible group node with the specified node as a child of the
     * new group.
     * @param child Child of the new group node.
     */
    public ZInvisibleGroup(ZNode child) {
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
    public void duplicateObject(ZInvisibleGroup refNode) {
	super.duplicateObject(refNode);
    }

    /**
     * Duplicates the current node by using the copy constructor.
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     * <P>
     * In particular, the visual component associated with this group gets duplicated
     * along with the subtree.
     *
     * @return A copy of this node.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZInvisibleGroup copy;

	objRefTable.reset();
	copy = new ZInvisibleGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    //****************************************************************************
    //
    // Static convenience methods to make a sub-tree in/visible
    //
    //***************************************************************************

    /**
     * Make the sub-tree rooted at the specified node invisible or visible.
     * If the node is being made invisible, this inserts an invisible node above that node
     * (if there isn't already one there.)
     * If it is being made visible, then an invisible nodes directly above
     * the specified node is removed if there is one.
     * @param node the node to make invisible
     */
    static public void setVisible(ZNode node, boolean visible) {
	if (visible) {
	    node.editor().removeInvisibleGroup();
	} else {
	    node.editor().getInvisibleGroup();
	}
    }

    //****************************************************************************
    //
    // Painting related methods
    //
    //***************************************************************************

    /**
     * An invisible node does not get rendered at all, nor do any of its children.
     * @param renderContext The graphics context to use for rendering.
     */
    public void render(ZRenderContext renderContext) {
    }

    /**
     * An invisible group always has empty bounds.
     */
    protected void computeBounds() {
	bounds.reset();
    }

    //****************************************************************************
    //
    //			Other Methods
    //
    //****************************************************************************

    /**
     * An invisible node never gets picked, nor does it pick any of its children.
     *
     * @param rect Coordinates of pick rectangle in local coordinates
     * @param mag The magnification of the camera being picked within.
     * @return The picked node, or null if none
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	 return false;
    }

    /**
     * In invisible node never is found, nor are any of its children.
     *
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @param nodes the accumulation list (results will be place here).
     * @return the number of nodes searched
     */
    int findNodes(ZFindFilter filter, ArrayList nodes) {
	return 0;
    }
}
