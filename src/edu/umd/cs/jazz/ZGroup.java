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
import javax.swing.event.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>ZGroup</b> is a node with children. Applications may use ZGroup to "group" children.
 * By inserting a group node above several children, the group node can then be
 * manipulated which will affect all of its children. Groups are typically used when
 * several objects should be treated as a semantic unit.  
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
public class ZGroup extends ZNode implements ZSerializable, Serializable {
				// Default values
    static public final boolean childrenPickable_DEFAULT = true;     // True if this group should pick its children
    static public final boolean childrenFindable_DEFAULT = true;     // True if this group should find its children
    static public final boolean hasOneChild_DEFAULT = false;     // True if this group can have no more than one child

    /**
     * The array of children under this group node.
     * This is guaranteed to point to a valid array,
     * even if this group does not have any children.
     */
    ZNode[] children = null;

    /**
     * The actual number of children of this group.
     */
    int numChildren = 0;

    /**
     * Cached volatility computation.
     */
    transient boolean cacheVolatile = false;

    /**
     *  True if pick should pick children.
     */
    private boolean childrenPickable = childrenPickable_DEFAULT;

    /**
     *  True if find should find children.
     */
    private boolean childrenFindable = childrenFindable_DEFAULT;

    /**
     *  True if this group can have no more than one child
     */
    private boolean hasOneChild = hasOneChild_DEFAULT;

    //****************************************************************************
    //
    //                Constructors
    //
    //****************************************************************************

    /**
     * Constructs an empty ZGroup.
     */
    public ZGroup() {
	children = new ZNode[1];
    }

    /**
     * Constructs a new group node with the specified node as a child of the
     * new group.  If the specified child was already a member of a tree (i.e., had a parent),
     * then this new node is inserted in the tree above the child so that the original
     * child is still in that tree, but with this node inserted in the middle of the tree.
     * If the specified child does not have a parent, then it is just made a child of this node.
     * @param child Child of the new group node.
     */
    public ZGroup(ZNode child) {
	this();
	insertAbove(child);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZGroup newGroup = (ZGroup)super.duplicateObject();

				// Do a deep copy of children
	if (children != null) {
	    newGroup.children = new ZNode[children.length];
	    for (int i = 0; i < numChildren; i++) {
		ZNode newChild = (ZNode)children[i].clone();
		newGroup.children[i] = newChild;   
		newChild.parent = newGroup;
	    }
	}

	return newGroup;
    }

    /**
     * Trims the capacity of the array that stores the children list points to
     * the actual number of points.  Normally, the children list arrays can be
     * slightly larger than the number of points in the children list.
     * An application can use this operation to minimize the storage of a
     * children list.
     */
    public void trimToSize() {
	ZNode[] newChildren = new ZNode[numChildren];
	for (int i=0; i<numChildren; i++) {
	    newChildren[i] = children[i];
	}
	children = newChildren;
    }

    /**
     * Insert this group node above the specified node.
     * If the child node already had a parent, then this node will be
     * properly inserted in between the original parent and the child.
     * If this node already had a parent, then it will be removed
     * before being inserted.  If this node already had children, then
     * they will be left alone, and the new child will be the last
     * child of this node.  
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @param child the child node that this node should go above.
     * @see ZGroupEvent
     */
    public void insertAbove(ZNode child) {
				// If this node already has a parent, then remove it.
	if (parent != null) {
	    parent.removeChild(this,false);
	}

				// Now, go ahead and insert this above the specified child.
	ZGroup prevParent = parent;
	ZGroup p = child.getParent();
	if (p != null) {
				// First, find out the position of the child in its parent child list,
				// and put this in its place.
	    ZNode[] cousins = p.children;
	    for (int i=0; i<cousins.length; i++) {
		if (cousins[i] == child) {
		    cousins[i] = this;
		    child.parent = null;
		    break;
		}
	    }
	    parent = p;
	}	
	addChildImpl(child,false);     // Then, add the child below this.

	
	// Fire the necessary events - after we've updated the tree
	
	// If the new parent is null - then this was just a normal
	// removeChild and not a modification 
	if (prevParent != null &&
	    p == null) {
	    prevParent.fireGroupEvent(ZGroupEvent.NODE_REMOVED,this,false);
	}
	else if (prevParent != null) {
	    prevParent.fireGroupEvent(ZGroupEvent.NODE_REMOVED,this,true);
	}

	// If the child's previous parent is null - then this was just a normal
	// addChild and not a modification	
	if (p != null) {
	    p.fireGroupEvent(ZGroupEvent.NODE_REMOVED,child,true);

	    // If this' previous parent is null - then this was just a
	    // normal addChild and not a modification
	    if (prevParent == null) {
		p.fireGroupEvent(ZGroupEvent.NODE_ADDED,this,false);
	    }
	    else {
		p.fireGroupEvent(ZGroupEvent.NODE_ADDED,this,true);	
	    }
	    
	    this.fireGroupEvent(ZGroupEvent.NODE_ADDED,child,true);
	}
	else {
	    this.fireGroupEvent(ZGroupEvent.NODE_ADDED,child,false);
	}
	
        reshape();
    }
    
    /**
     * Extract this node from the tree, merging the nodes above and below it.
     * If this node has a parent, then this node's children will be added
     * to that parent's children in the same position this node was in.
     * If this node doesn't have a parent, then its children will just
     * be removed from it.  Note that this method delays firing any necessary
     * group events until the tree has been completely updated.
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @see ZGroupEvent
     */
    public void extract() {
	ZNode child;
	ZGroup origParent = parent;

				// Note: We must remove this from its parent before
				// we move up the children because the parent could
				// have 'hasOneChild' set, and thus only be able to
				// have one child at a time.

	ZNode beforeNode = null;
	if (parent != null) {
				// First, find the cousin of this node that is just after
				// this in its parent children list.  We'll use that to
				// move the children in front of after this node is removed
				// from the list.
	    int numChildren = parent.getNumChildren();
	    ZNode[] parentChildren = parent.getChildrenReference();
	    for (int i=0; i<numChildren; i++) {
		if (this == parentChildren[i]) {
		    if (i < (numChildren - 1)) {
			beforeNode = parentChildren[i + 1];
		    }
		    break;
		}
	    }

				// Now remove this from its parent
	    parent.removeChild(this,false);
	}

	                        // Store reference to the first child to fire events later
	int prevNumChildren = numChildren;
	ZNode firstChild = children[0];

	
				// Then, move this node's children to its original parent				
	while (numChildren > 0) {
	    child = children[0];

	    if (origParent != null) {
		
		origParent.addChildImpl(child,false);
		
		if (beforeNode == null) {
		    origParent.raise(child);
		} else {
		    origParent.lowerTo(child, beforeNode);
		}
	    }
	    else {
	      // Its okay to fire the events here because
	      // the parent is null - which means *this* wasn't
	      // removed from its parent and the children
	      // won't be added again anywhere in this method
	      this.removeChild(child,true);
	    }
	}


	// Now fire the necessary events - after we've updated the tree
	if (origParent != null) {
	    origParent.fireGroupEvent(ZGroupEvent.NODE_REMOVED,this,false);
	    
	    int index = origParent.indexOf(firstChild);
	    for(int i=0; i<prevNumChildren; i++) {
		this.fireGroupEvent(ZGroupEvent.NODE_REMOVED,origParent.getChild(index+i),true);
	    }	
	    for(int i=0; i<prevNumChildren; i++) {
		origParent.fireGroupEvent(ZGroupEvent.NODE_ADDED,origParent.getChild(index+i),true);	    
	    }	
	}
    }

    /**
     * Add a node to be a new child of this group node.  The new node
     * is added to the end of the list of this node's children.
     * If child was previously a child of another node, it is removed from that first.
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * <P>
     * If this group has 'hasOneChild' set, and if adding this child
     * would result in there being more than one child,
     * then a ZTooManyChildrenException is thrown.
     * @param child The new child node.
     * @see ZGroupEvent
     */
    public void addChild(ZNode child) {	
	addChildImpl(child,true);
    }

    /**
     * The implementation of the addChild method
     * @param child The child to be added
     * @param fireEvent Should the group event be fired
     */   
    protected void addChildImpl(ZNode child, boolean fireEvent) {
				// Check if can't have more than one child
	if (hasOneChild() && (numChildren >= 1)) {
	    throw new ZTooManyChildrenException(this, "Can't have more than one child when hasOneChild flag set");
	}

				// First remove child from existing parent if one
	if (child.parent != null) {
	    child.parent.removeChild(child,fireEvent);
	}

				// Then, allocate space for new child and copy it
	try {
	    children[numChildren] = child;
	} catch (ArrayIndexOutOfBoundsException e) {
	    ZNode[] newChildren = new ZNode[(numChildren == 0) ? 1 : (2 * numChildren)];
	    System.arraycopy(children, 0, newChildren, 0, numChildren);
	    children = newChildren;
	    children[numChildren] = child;
	}
				// Finally, update parent pointer, and number of children
	child.parent = this;
	numChildren++;
	updateVolatility();	// Need to update volatility since new child could be volatile
	updateHasNodeListener(); // Need to update hasNodeListener since new child could have a node listener

				// Manually update bounds and repaint since reshape would result
				// in entire group being painted twice.  Since we're adding a new
				// item, we only have to repaint the new part.
	updateBounds();
	child.repaint();

	if (fireEvent) {
	    fireGroupEvent(ZGroupEvent.NODE_ADDED,child,false);
	}
    }
    
    /**
     * Remove all chidren from this group node.
     * If the node has no children, then nothing happens.
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @param child The child to be removed.
     * @see ZGroupEvent
     */

    public void removeAllChildren() {
	for (int i=numChildren-1; i >= 0; i--) {
	    removeChildImpl(i, true);
	}
    }

    /**
     * Remove the child at the specified position of this group node's children.
     * Any subsequent children are shifted to the left (one is subtracted from 
     * their indices).
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @param index The position of the child node to be removed.
     * @see ZGroupEvent
     */
    public void removeChild(int index) {
	removeChildImpl(index, true);
    }

    /**
     * Remove the specified child node from this group node.
     * If the specified node wasn't a child of this node,
     * then nothing happens.
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @param child The child to be removed.
     * @param fireEvent Should the group event be fired.
     * @see ZGroupEvent
     */
    protected void removeChild(ZNode child, boolean fireEvent) {
	for (int i=0; i<numChildren; i++) {
				// Find child within children list
	    if (child == children[i]) {
		removeChildImpl(i, fireEvent);
		break;
	    }
	}
    }

    /**
     * Remove the specified child node from this group node.
     * If the specified node wasn't a child of this node,
     * then nothing happens.
     * <P>
     * This method may fire NODE_ADDED or NODE_REMOVED ZGroupEvents.
     * ZGroupEvents now contains a method <code>isModificationEvent()</code> to
     * distinguish a modification event from a <bold>true</bold> node addition
     * or removal.  A modification event is one in which a node changes
     * position in a single scenegraph or between two different scenegraphs.
     * A true addition or removal event is one in which a node is first
     * added to or removed from a scenegraph.
     * @param child The child to be removed.
     * @see ZGroupEvent
     */
    public void removeChild(ZNode child) {
	removeChild(child, true);
    }

    /**
     * The implementation of the removeChild method @param index The
     * position of the child to be removed @param fireEvent Should the group event be
     * fired */
    protected void removeChildImpl(int index, boolean fireEvent) {
	if ((index >= 0) && (index < numChildren)) {
	    ZNode child = children[index];
	    child.repaint(); // Repaint area that child was in before removing it so it damages the proper area
				// Then, slide down other children, effectively removing this one
	    for (int j=index; j < (numChildren - 1); j++) {
		children[j] = children[j+1];
	    }
	    children[numChildren - 1] = null;
	    numChildren--;
	    child.parent = null;
	    updateVolatility();	  // Need to update volatility since previous child could have be volatile
	    updateHasNodeListener();  // Need to update node listener since previous child could have been the last node listener
	    
				// Manually update bounds and repaint since reshape would result
				// in entire group being painted twice.  Since we're removing a single
				// we only have to repaint that area.
	    updateBounds();
	    
	    if (fireEvent) {
		fireGroupEvent(ZGroupEvent.NODE_REMOVED,child,false);
	    }
	}
    }

    /**
     * Searches for the first occurrence of the given child in the children of this node.
     * @param child The child to search for
     * @return The index of the child, or -1 if not found
     */
    public int indexOf(ZNode child) {
	int index = -1;

	int numChildren = getNumChildren();
	ZNode[] parentChildren = getChildrenReference();
	for (int i=0; i<numChildren; i++) {
	    if (child == parentChildren[i]) {
		index = i;
		break;
	    }
	}

	return index;
    }
    
    /**
     * Return a copy of the array of children of this node.
     * This method always returns an array, even when there
     * are no children.
     * @return the children of this node.
     */
    public ZNode[] getChildren() {
	ZNode[] copyChildren = new ZNode[numChildren];
	System.arraycopy(children, 0, copyChildren, 0, numChildren);

	return copyChildren;
    }

    /**
     * Return an iterator over the children of this group in the proper order.
     * @return the iterator 
     */
    public Iterator getChildrenIterator() {
	return new ZNodeIterator(children, numChildren);
    }

    /**
     * Return an iterator over the children of this group in the proper order.
     * @return the iterator 
     */
    public Iterator iterator() {
	return new ZNodeIterator(children, numChildren);
    }



    /**
     * Returns the i'th child of this node.
     * @return the i'th child of this node.
     */
    public ZNode getChild(int i) {
        if (i >= numChildren || i < 0)
	    throw new IndexOutOfBoundsException(
		"Index: "+i+", Size: "+numChildren);
        return children[i];
    }

    /**
     * Internal method to return a reference to the actual children of this node.
     * It should not be modified by the caller.  Note that the actual number
     * of children could be less than the size of the array.  Determine
     * the actual number of children with {@link #getNumChildren}.
     * @return the children of this node.
     */
    protected ZNode[] getChildrenReference() {
	return children;
    }

    /**
     * Return the number of children of this group node.
     * @return the number of children.
     */
    public int getNumChildren() {
	return numChildren;
    }

    /**
     * Raises the specified child node within the drawing order of this node's children,
     * so it gets rendered above (after) all of its siblings.
     * This is done by moving the child node to the end of this node's children list.
     * @param child the child node to be raised.
     */
    public void raise(ZNode child) {
	for (int i=0; i<numChildren; i++) {
				// Find child within children list
	    if (child == children[i]) {
				// Then, slide down other children, and add this child to the end
		for (int j=i; j < (numChildren - 1); j++) {
		    children[j] = children[j+1];
		}
		children[numChildren - 1] = child;
		child.repaint();
		break;
	    }
	}
    }

    /**
     * Raises the specified child node within the drawing order of this node's siblings,
     * so it gets rendered above (after) the specified node.
     * This is done by moving this node just after the specified node in its parent's child list.
     * If the specified node is not a child of this, then this call does nothing.
     * <p>
     * If the specified reference node is null, then this node is raised to be the
     * last node rendered of its siblings (i.e., equivalent to calling {@link #raise}
     *
     * @param child the child node to be raised.
     * @param afterNode The node to raise this node after.
     */
    public void raiseTo(ZNode child, ZNode afterNode) {
	if (afterNode == null) {
	    raise(child);
	} else {
	    int i;
	    int childIndex = -1;
	    int afterIndex = -1;
				// Find nodes within children list
	    for (i=0; i<numChildren; i++) {
		if (child == children[i]) {
		    childIndex = i;
		}
		if (afterNode == children[i]) {
		    afterIndex = i;
		}
		if ((childIndex >= 0) && (afterIndex >= 0)) {
		    break;
		}
	    }
				// If child and ref node found then continue
	    if ((childIndex >= 0) && (afterIndex >= 0)) {
				// If child before ref node
		if (childIndex < afterIndex) {
				// Slide down other children, and add this child after the ref node
		    for (i=childIndex; i<afterIndex; i++) {
			children[i] = children[i+1];
		    }
		    children[afterIndex] = child;
				// Else, child after ref node
		} else {
		    for (i=childIndex; i>(afterIndex + 1); i--) {
			children[i] = children[i-1];
		    }
		    children[afterIndex + 1] = child;
		}
	    }
	    child.repaint();
	}
    }

    /**
     * Lowers the specified child node within the drawing order of this node's children,
     * so it gets rendered below (before) all of its siblings.
     * This is done by moving the child node to the end of this node's children list.
     * @param child the child to be lowered.
     */
    public void lower(ZNode child) {
	for (int i=0; i<numChildren; i++) {
				// Find child within children list
	    if (child == children[i]) {
				// Then, slide up other children, and add this child to the beginning
		for (int j=i; j > 0; j--) {
		    children[j] = children[j-1];
		}
		children[0] = child;
		child.repaint();
		break;
	    }
	}
    }

    /**
     * Lowers the specified child node within the drawing order of this node's siblings,
     * so it gets rendered below (before) the specified node.
     * This is done by moving this node just before the specified node in its parent's child list.
     * If the specified node is not a child of this, then this call does nothing.
     * <p>
     * If the specified reference node is null, then this node is lowered to be the
     * first node rendered of its siblings (i.e., equivalent to calling {@link #lower}
     *
     * @param child the child to be lowered.
     * @param beforeNode The node to lower this node before.
     */
    public void lowerTo(ZNode child, ZNode beforeNode) {
	if (beforeNode == null) {
	    lower(child);
	} else {
	    int i;
	    int childIndex = -1;
	    int beforeIndex = -1;
				// Find nodes within children list
	    for (i=0; i<numChildren; i++) {
		if (child == children[i]) {
		    childIndex = i;
		}
		if (beforeNode == children[i]) {
		    beforeIndex = i;
		}
		if ((childIndex >= 0) && (beforeIndex >= 0)) {
		    break;
		}
	    }
				// If child and ref node found then continue
	    if ((childIndex >= 0) && (beforeIndex >= 0)) {
				// If child before ref node
		if (childIndex < beforeIndex) {
				// Slide down other children, and add this child before the ref node
		    for (i=childIndex; i<(beforeIndex - 1); i++) {
			children[i] = children[i+1];
		    }
		    children[beforeIndex - 1] = child;
				// Else, child before ref node
		} else {
		    for (i=childIndex; i>beforeIndex; i--) {
			children[i] = children[i-1];
		    }
		    children[beforeIndex] = child;
		}
	    }
	    child.repaint();
	}
    }

    /**
     * Internal method to compute and cache the volatility of a node,
     * to recursively call the parents to compute volatility.
     * A node is considered to be volatile if it is set to be volatile,
     * or any of its descendants are volatile.
     * All parents of this node are also volatile when this is volatile.
     * @see #setVolatileBounds(boolean)
     * @see #getVolatileBounds()
     */
    protected void updateVolatility() {
				// If this node set to volatile, then it is volatile
	cacheVolatile = volatileBounds;
	if (!cacheVolatile) {
				// Else, if any of its children are volatile, then it is volatile
	    for (int i=0; i<numChildren; i++) {
		if (children[i].getVolatileBounds()) {
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
     * to be volatile with {@link ZNode#setVolatileBounds}, or if any
     * of its children are volatile.
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
    // Get/Set pairs
    //
    //***************************************************************************

    /**
     * Specifies whether this node should pick its children.
     * If this is true, then the pick() method will descend to
     * group's children and will first pick them.  If false,
     * then the pick method will not pick children, and will
     * only pick this node.
     * @param childrenPickable True if this node should pick its children
     * @see ZDrawingSurface#pick
     */
    public void setChildrenPickable(boolean childrenPickable) {
	this.childrenPickable = childrenPickable;
    }

    /**
     * Determines if this node picks its children.
     * @return True if this node picks its children.
     */
    public final boolean getChildrenPickable() {
	return childrenPickable;
    }

    /**
     * Specifies whether this node should find its children.
     * If this is true, then the findNodes() method will descend to
     * its children and will first find them.  If false,
     * then the find method will not find this node's children, and will
     * only find this node.  In any case, this or any other node is only
     * findable if it satisfies the accept criteria of the find filter
     * used in a find operation.
     * @see ZDrawingSurface#findNodes
     * @param childrenFindable True if this node should find its children
     */
    public void setChildrenFindable(boolean childrenFindable) {
	this.childrenFindable = childrenFindable;
    }

    /**
     * Determines if this node finds its children.
     * @return True if the children of this node are findable.
     */
    public final boolean getChildrenFindable() {
	return childrenFindable;
    }

    /**
     * Specifies if this group is only allowed to have a maxium of one child.  If it is true
     * then a ZTooManyChildrenException will be thrown if an attempt to make this group
     * have more than one child is made.  This is typically used when an application wants
     * to define a "decorator chain" which relates one group node to its child.
     * <P>
     * Jazz provides ZSceneGraphEditor to manage these decorator chains, and that
     * class should be examined for more information.
     * @param oneChild True if this node can have no more than one child
     * @see ZSceneGraphEditor
     */
    public void setHasOneChild(boolean oneChild) {
	if (oneChild && numChildren > 1) {
	    throw new ZTooManyChildrenException(this, "Can't have more than one child when hasOneChild flag set");
	}
	hasOneChild = oneChild;
    }

    /**
     * Determines if this group node can have no more than one child
     * @return True if this node can have no more than one child
     * @see ZSceneGraphEditor
     */
    public final boolean hasOneChild() {
	return hasOneChild;
    }

    //****************************************************************************
    //
    // Painting and Bounds related methods
    //
    //***************************************************************************

    /**
     * Renders this node which results in its children getting painted.
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
	if (numChildren == 0) {
	    return;
	}

	ZBounds visibleBounds = renderContext.getVisibleBounds();

				// Paint children
	//System.out.println("ZGroup painting children: "+numChildren);
	for (int i=0; i<numChildren; i++) {
	    if (visibleBounds.intersects(children[i].getBoundsReference())) {
		children[i].render(renderContext);
	    }
	}
    }

    /**
     * Recomputes and caches the bounds for this node.  Generally this method is
     * called by reshape when the bounds have changed, and it should rarely
     * directly elsewhere.  A ZGroup bounds is the union
     * of its children's bounds
     */
    protected void computeBounds() {
	bounds.reset();
	for (int i=0; i<numChildren; i++) {
	    bounds.add(children[i].getBoundsReference());
	}
    }

    // *********************************************************************
    //
    // Event methods
    //
    // *********************************************************************
    
    /**
     * This method overriddes ZNode.removeNodeListener so as to properly update
     * the hasNodeListener bit taking into consideration the ZGroup's children.
     * @param l The node listener to be removed
     */
    public void removeNodeListener(ZNodeListener l) {
        listenerList.remove(ZNodeListener.class, l);
	if (listenerList.getListenerCount() == 0) {
	    listenerList = null;
	}

	updateHasNodeListener();
    }

    /**
     * Updates the hasNodeListener bit taking into consideration the
     * ZGroup's children.  If the bit for this group changes, it
     * notifies its parent.
     */
    public void updateHasNodeListener() {
	boolean hadListener = hasNodeListener;

	hasNodeListener = false;       
	if (listenerList != null &&
	    listenerList.getListenerCount(ZNodeListener.class) > 0) {
	    hasNodeListener = true;
	}
	else {
	    for(int i=0; i<numChildren; i++) {
		if (children[i].hasNodeListener()) {
		    hasNodeListener = true;
		    break;
		}
	    }
	}

	if (parent != null &&
	    hasNodeListener != hadListener) {
	    parent.updateHasNodeListener();
	}
    }
    
    /**
     * Adds the specified group listener to receive group events from this node.
     *
     * @param l the group listener.
     */
    public void addGroupListener(ZGroupListener l) {
	if (listenerList == null) {
	    listenerList = new EventListenerList();
	}
        listenerList.add(ZGroupListener.class, l);
    }

    /**
     * Removes the specified group listener so that it no longer
     * receives group events from this group.
     *
     * @param l the group listener.
     */
    public void removeGroupListener(ZGroupListener l) {
        listenerList.remove(ZGroupListener.class, l);
	if (listenerList.getListenerCount() == 0) {
	    listenerList = null;
	}
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type, then percolates the event up scenegraph,
     * notifying any listeners on any higher ZGroup nodes.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.  The listener list is processed in last to
     * first order.
     * @param id The event id (NODE_ADDED, NODE_REMOVED)
     * @param child The child being added or removed from this node
     * @param isModification true if this is a modification event.
     * @see EventListenerList
     * @see ZGroupEvent
     */
    protected void fireGroupEvent(int id, ZNode child, boolean isModification) {
	ZNode node = this;
	ZGroupEvent e = new ZGroupEvent((ZGroup)node, id, child, isModification);
	do {
	    if (e.isConsumed()) {
		break;
	    }

	    if (node instanceof ZGroup) {
				// Guaranteed to return a non-null array
		if (node.listenerList != null) {
		    Object[] listeners = node.listenerList.getListenerList();

				// Process the listeners last to first, notifying
				// those that are interested in this event
		    for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ZGroupListener.class) {
			    switch (id) {
			    case ZGroupEvent.NODE_ADDED:
				((ZGroupListener)listeners[i+1]).nodeAdded(e);
				break;
			    case ZGroupEvent.NODE_REMOVED:
				((ZGroupListener)listeners[i+1]).nodeRemoved(e);
				break;
			    }
			    if (e.isConsumed()) {
				break;
			    }
			}
		    }
		}
	    }
	    node = node.getParent();
	} while (node != null);
    }

    //****************************************************************************
    //
    //			Other Methods
    //
    //****************************************************************************

    /**
     * Returns the first object under the specified rectangle (if there is one)
     * in the subtree rooted with this as searched in reverse (front-to-back) order.
     * This performs a depth-first search, first picking children.
     * Only returns a node if this is "pickable".
     * <P>
     * If childrenPickable is false, then this will never return a child as the picked node.
     * Instead, this node will be returned if any children are picked.  If no children
     * are picked, then this will return null.
     * @param rect Coordinates of pick rectangle in local coordinates
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return The picked node, or null if none
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	boolean picked = false;

	if (isPickable()) {
	    path.push(this);

	    for (int i=(numChildren - 1); i>=0; i--) {
		if (children[i].pick(rect, path)) {
		    if (!getChildrenPickable()) {
		    	// Can't pick children - set the picked object to the group
		    	path.pop(this);
		        path.setObject(this);
		    }
		    return true;
		}
	    }

	    // Remove me from the path if the pick failed
	    path.pop(this);
	}

	return false;
    }

    /**
     * Internal method to return the list of nodes that are accepted by the specified filter in the
     * subtree rooted with this.  If this node is not "findable", then neither
     * this node, nor any of its descendants will be included.
     * The filter specifies whether or not this node should be accepted by the
     * search, and whether the node's children should be searched.
     * <P>
     * If findChildren is false, then the children of this node are are not checked for being found,
     * and only this node (possibly) is found.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @param nodes the accumulation list (results will be place here).
     * @return the number of nodes searched
     * @see #isFindable()
     * @see ZFindFilter
     */
    protected int findNodes(ZFindFilter filter, ArrayList nodes) {
	int nodesSearched = 1;

				// Only search if node is findable.
	if (isFindable()) {
				// Check if this node is accepted by the filter
	    if (filter.accept(this)) {
		nodes.add(this);
	    }

				// Check node's children
	    if (getChildrenFindable() && filter.childrenFindable(this)) {
		for (int i=0; i<numChildren; i++) {
		    nodesSearched += children[i].findNodes(filter, nodes);
		}
	    }
	}

	return nodesSearched;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = super.dump();

	if (hasOneChild()) {
	    str += "\n HasOneChild";
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

	if (numChildren > 0) {
	    ZNode[] copyChildren = getChildren();  // Array can have some empty slots
	    out.writeState("List", "children", Arrays.asList(copyChildren));
	}
	if (childrenPickable != childrenPickable_DEFAULT) {
	    out.writeState("boolean", "childrenPickable", childrenPickable);
	}
	if (childrenFindable != childrenFindable_DEFAULT) {
	    out.writeState("boolean", "childrenFindable", childrenFindable);
	}
	if (hasOneChild != hasOneChild_DEFAULT) {
	    out.writeState("boolean", "hasOneChild", hasOneChild);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add children
	for (int i=0; i<numChildren; i++) {
	    out.addObject(children[i]);
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

	if (fieldName.compareTo("children") == 0) {
	    ZNode child;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		child = (ZNode)i.next();
		addChild(child);
	    }
 	} else if (fieldName.compareTo("childrenPickable") == 0) {
	    setChildrenPickable(((Boolean)fieldValue).booleanValue());
 	} else if (fieldName.compareTo("childrenFindable") == 0) {
	    setChildrenFindable(((Boolean)fieldValue).booleanValue());
 	} else if (fieldName.compareTo("hasOneChild") == 0) {
	    setHasOneChild(((Boolean)fieldValue).booleanValue());
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }

    /**
     * An iterator class to support iterating over the children of a group.
     */
    class ZNodeIterator implements Iterator {
	private ZNode[] array;
	private int len;
	private int index;

	public ZNodeIterator(ZNode[] array, int len) {
	    this.array = array;
	    this.len = len;
	    index = 0;
	}
	
	public boolean hasNext() {
	    return (index < len);
	}
	
	public Object next() {
	    Object obj = null;

	    if (index >= len) {
		throw new NoSuchElementException();
	    } else {
		obj = array[index];
		index++;
	    }
	    
	    return obj;
	}
	
	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}
