/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import edu.umd.cs.jazz.*;

/**
 * <b>ZSceneGraphEditor</b> provides a convenience mechanism used to locate
 * and create instances of the following types of group nodes:
 * <p>
 * <ul>
 * <li>Custom (i.e., any other group that has hasOneChild set)
 * <li>ZNameGroup
 * <li>ZInvisibleGroup
 * <li>ZLayoutGroup
 * <li>ZAnchorGroup
 * <li>ZTransformGroup
 * <li>ZStickyGroup
 * <li>ZSelectionGroup
 * <li>ZFadeGroup
 * <li>ZSpatialIndexGroup
 * </ul>
 *
 * ZSceneGraphEditor uses lazy evaluation to automatically create these
 * group nodes in a scene graph as they are needed.<p>
 *
 * For example, you can use a graph editor to obtain a ZTransformGroup
 * for a leaf node. The first time you do this, a new ZTransformGroup
 * is inserted above the leaf. Subsequently, that ZTransformGroup
 * is reused. e.g.<p>
 *
 * <pre>
 *    ZTransformGroup t = node.editor().getTransformGroup();
 *    t.translate(1, 0);
 * </pre>
 *
 * will translate <i>node</i> by 1, 0. Repeatedly executing
 * this code will cause the node to move horizontally across the
 * screen.<p>
 *
 * If multiple group types are created for a given node in the scene graph,
 * they are ordered as shown in the list above - so custom groups
 * will be inserted at the top (closest to the root), whereas spatialIndex groups will be
 * inserted at the bottom (closest to the node being edited.)<p>
 *
 * Group nodes inserted into the scene graph by ZSceneGraphEditor have
 * their hasOneChild flag set to true - such group nodes are referred to
 * as "edit groups", and can only act on the single node immediately
 * beneath them in the scene graph. This guarantees that
 * translations or scalings applied to a ZTransformGroup
 * node created by ZSceneGraphEditor effect only the node being edited.
 * ZSceneGraphEditor uses the hasOneChild flag to identify edit
 * groups that it has created in the scene graph, to avoid creating the
 * same group nodes twice.<p>
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @see edu.umd.cs.jazz.ZNode#editor
 * @author Jonathan Meyer, 25 Aug 99
 * @author Ben Bederson
 */

public class ZSceneGraphEditor implements Serializable {
    // The ordering of edit groups is defined here

    /**
     * A bit representing an application-defined custom node type and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int CUSTOM = 512;

    /**
     * A bit representing an name node type (ZNameGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int NAME = 256;

    /**
     * A bit representing an invisible node type (ZInvisibleGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int INVISIBLE = 128;

    /**
     * A bit representing a layout node type (ZLayoutGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int LAYOUT = 64;

    /**
     * A bit representing an anchor node type (ZAnchorGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int ANCHOR = 32;

    /**
     * A bit representing a transform node type (ZTransformGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int TRANSFORM = 16;

    /**
     * A bit representing a sticky node type (ZStickyGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int STICKY = 8;

    /**
     * A bit representing a selection node type (ZSelectionGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int SELECTION = 4;

    /**
     * A bit representing a fade node type (ZFadeGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int FADE = 2;

    /**
     * A bit representing an index node type (ZSpatialIndexGroup) and order.  Bigger numbers appear
     * higher in the editor chain.  This is the value returned by #editGroupType.
     */
    static protected final int INDEX = 1;

    /**
     * A value representing an unknown node type.
     * This is the value returned by #editGroupType.
     */
    static protected final int UNKNOWN = -1;

    protected ArrayList   editGroups;     // List of edit groups above node (in bottom-top order)
    protected ZNode       editNode;       // Node being edited
    protected int         groupTypes;     // What types of edit groups this node has

    /**
     * Returns the type number for a given instance.
     * @param node The node being checked
     * @return The node type 
     */
    static protected int editGroupType(ZNode node) {
        if (node instanceof ZSelectionGroup) return SELECTION;
        else if (node instanceof ZFadeGroup) return FADE;
        else if (node instanceof ZStickyGroup) return STICKY; // Check sticky before transform since sticky *is* a transform
        else if (node instanceof ZAnchorGroup) return ANCHOR;
        else if (node instanceof ZLayoutGroup) return LAYOUT;
        else if (node instanceof ZSpatialIndexGroup) return INDEX;
        else if (node instanceof ZNameGroup) return NAME;
        else if (node instanceof ZInvisibleGroup) return INVISIBLE;
        else if (node instanceof ZTransformGroup) return TRANSFORM;
        else if (node instanceof ZGroup && ((ZGroup)node).hasOneChild()) return CUSTOM;
        return UNKNOWN;
    }

    /**
     * Determines if node is an edit group node.
     * @return true if node is an edit group node, false otherwise.
     */
    static protected boolean isEditGroup(ZNode node) {
        return ((node != null) && (editGroupType(node) > 0) && ((ZGroup)node).hasOneChild());
    }

    /**
     * Adds a ZGroup as an "editGroup", preserving the
     * order listed by the numbering for the various editGroup types.
     * If group already in list, then do nothing.
     * @param type The type of the new group
     * @param editGroup The new group
     */
    protected void setEditGroup(int type, ZGroup editGroup) {
	if (hasEditGroup(type)) {
	    return;
	}

        groupTypes |= type;

        boolean done = false;
	ZNode child = null;
	ZGroup g = null;
	int size = editGroups.size();
	int i = 0;
        for (i = 0; i < size; i++) {
	    g = (ZGroup)editGroups.get(i);
	    if (editGroupType(g) > type) {
		break;
	    }
	    child = g;
	}
	if (child == null) {
	    child = editNode;
	}
	editGroups.add(i, editGroup);
	editGroup.insertAbove(child);
	editGroup.setHasOneChild(true);
    }

    /**
     * Determines if this editor has an edit group of the specified type.
     * @return True if this editor has the specified edit group.
     */
    protected boolean hasEditGroup(int type) {
        return (groupTypes & type) != 0;
    }

    /**
     * Returns the specified edit group type for this editor, if there is one, 
     * or null otherwise.
     * @param type The type of edit group to return
     * @return The edit group
     */
    protected ZGroup getEditGroup(int type) {
        if (hasEditGroup(type)) {
            for (int i = 0; i < editGroups.size(); i++) {
           	ZGroup d = (ZGroup)editGroups.get(i);
                if (editGroupType(d) == type) {
                    return d;
                }
            }
        }
        return null;
    }

    /**
     * Removes the specified edit group from this editor if there is one,
     * or does nothing otherwise.
     * @param type The type of edit group to return
     * @return True if the edit group existed and was removed
     */
    protected boolean removeEditGroup(int type) {
        if (hasEditGroup(type)) {
	    ZGroup g = null;
            for (Iterator i = editGroups.iterator(); i.hasNext(); ) {
                g = (ZGroup)i.next();
                if (editGroupType(g) == type) {
                    i.remove();
                    g.extract();
                    groupTypes &= ~type;
                    return true;
                }
            }
            // should not get here
            throw new RuntimeException("ZSceneGraphEditor: Attempt to remove an edit group " +
                                       "that doesn't exist (two editors in use at once?)");
        }
        return false;
    }

    //
    // PUBLIC API
    //

    /**
    * Given a node, constructs an "editor" for the node.
    * Editors are used to locate and create parent groups
    * above the node.
    */
    public ZSceneGraphEditor(ZNode node) {
        // Search downwards, skipping over edit groups to identify the
        // lowest "editNode"
        //
        while (isEditGroup(node)) {
            node = ((ZGroup)node).getChild(0);
        }

        // Now search upwards to identify edit groups
        ArrayList groups = new ArrayList();
        ZNode parent = node.getParent();
        while (parent != null && isEditGroup(parent)) {
            groups.add(parent);
            groupTypes |= editGroupType(parent);
            parent = parent.getParent();
        }

        editNode = node;
        editGroups = groups;
    }

    /**
     * If ZSceneGraphEditor has inserted groups above a node, this
     * returns the topmost of those groups (the group nearest the root of the
     * scene graph). If no edit groups have been inserted above a node,
     * this returns the node itself. This method is useful if you want
     * to remove a node and also its associated edit groups
     * from a scene graph.
     */
    public ZNode getTop() {
	int size = editGroups.size();
	if (size == 0) {
	    return editNode;
	} else {
	    return (ZNode)editGroups.get(size - 1);
	}
    }

    /**
     * Returns the node being edited.  This is the node that is the bottom-most
     * node of an edit group.  It is defined as being this node, or the first
     * descendant that does not have 'hasOneChild' set.
     */
    public ZNode getNode() {
        return editNode;
    }

    /**
     * Returns an array representing the list of "edit" groups
     * above the node - groups above the node whose hasOneChild
     * flag is set to true.  The groups are listed in bottom-top order.
     */
    public ArrayList getGroups() { return editGroups; }


    /**
     * Returns a ZTransformGroup to use for a node,
     * inserting one above the edited node if none exists.
     */
    public ZTransformGroup getTransformGroup() {
        ZTransformGroup tg = (ZTransformGroup)getEditGroup(TRANSFORM);
        if (tg == null) {
	    setEditGroup(TRANSFORM, tg = new ZTransformGroup());

				// If the node getting this transformGroup is spatial-indexed,
				// add a node listener so it will be re-indexed when it's
				// bounds change.
	    ZNode top = tg.editor().getTop().getParent();
	    if ((top != null) && (top.editor().hasSpatialIndexGroup())) {
		ZSpatialIndexGroup indexGroup = top.editor().getSpatialIndexGroup();
		indexGroup.addListener(tg);
	    }
	}
        return tg;
    }

    /**
     * Returns true if this node has a ZTransformGroup above it as
     * an edit group, false otherwise.
     */
    public boolean hasTransformGroup() { return hasEditGroup(TRANSFORM); }

    /**
     * Removes a TransformGroup edit group from above a node. Returns true
     * if the ZTransformGroup could be removed, false otherwise.
     */
    public boolean removeTransformGroup() { return removeEditGroup(TRANSFORM); }


    /**
     * Returns a ZFadeGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZFadeGroup getFadeGroup() {
        ZFadeGroup fg = (ZFadeGroup)getEditGroup(FADE);
        if (fg == null) { setEditGroup(FADE, fg = new ZFadeGroup()); }
        return fg;
    }
    /**
     * Returns true if this node has a ZFadeGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasFadeGroup() { return hasEditGroup(FADE); }
    /**
     * Removes a ZFadeGroup edit group for above a node. Returns true
     * if the ZFadeGroup could be removed, false otherwise.
     */
    public boolean removeFadeGroup() { return removeEditGroup(FADE); }

    /**
     * Returns a ZStickyGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZStickyGroup getStickyGroup() {
        ZStickyGroup g = (ZStickyGroup)getEditGroup(STICKY);
        if (g == null) { setEditGroup(STICKY, g = new ZStickyGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZStickyGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasStickyGroup() { return hasEditGroup(STICKY); }
    /**
     * Removes a ZStickyGroup edit group for above a node. Returns true
     * if the ZStickyGroup could be removed, false otherwise.
     */
    public boolean removeStickyGroup() { return removeEditGroup(STICKY); }

    /**
     * Returns a ZSelectionGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZSelectionGroup getSelectionGroup() {
        ZSelectionGroup g = (ZSelectionGroup)getEditGroup(SELECTION);
        if (g == null) { setEditGroup(SELECTION, g = new ZSelectionGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZSelectionGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasSelectionGroup() { return hasEditGroup(SELECTION); }
    /**
     * Removes a ZSelectionGroup edit group for above a node. Returns true
     * if the ZSelectionGroup could be removed, false otherwise.
     */
    public boolean removeSelectionGroup() { return removeEditGroup(SELECTION); }


    /**
     * Returns a ZAnchorGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZAnchorGroup getAnchorGroup() {
        ZAnchorGroup g = (ZAnchorGroup)getEditGroup(ANCHOR);
        if (g == null) { setEditGroup(ANCHOR, g = new ZAnchorGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZAnchorGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasAnchorGroup() { return hasEditGroup(ANCHOR); }
    /**
     * Removes a ZAnchorGroup edit group for above a node. Returns true
     * if the ZAnchorGroup could be removed, false otherwise.
     */
    public boolean removeAnchorGroup() { return removeEditGroup(ANCHOR); }


    /**
     * Returns a ZLayoutGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZLayoutGroup getLayoutGroup() {
        ZLayoutGroup g = (ZLayoutGroup)getEditGroup(LAYOUT);
        if (g == null) { setEditGroup(LAYOUT, g = new ZLayoutGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZLayoutGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasLayoutGroup() { return hasEditGroup(LAYOUT); }
    /**
     * Removes a ZLayoutGroup edit group for above a node. Returns true
     * if the ZLayoutGroup could be removed, false otherwise.
     */
    public boolean removeLayoutGroup() { return removeEditGroup(LAYOUT); }

    /**
     * Returns a ZNameGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZNameGroup getNameGroup() {
        ZNameGroup g = (ZNameGroup)getEditGroup(NAME);
        if (g == null) { setEditGroup(NAME, g = new ZNameGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZNameGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasNameGroup() { return hasEditGroup(NAME); }

    /**
     * Removes a ZNameGroup edit group for above a node. Returns true
     * if the ZNameGroup could be removed, false otherwise.
     */
    public boolean removeNameGroup() { return removeEditGroup(NAME); }

    /**
     * Returns a ZInvisibleGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZInvisibleGroup getInvisibleGroup() {
        ZInvisibleGroup g = (ZInvisibleGroup)getEditGroup(INVISIBLE);
        if (g == null) { setEditGroup(INVISIBLE, g = new ZInvisibleGroup()); }
        return g;
    }
    /**
     * Returns true if this node has a ZInvisibleGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasInvisibleGroup() { return hasEditGroup(INVISIBLE); }
    /**
     * Removes a ZInvisibleGroup edit group for above a node. Returns true
     * if the ZInvisibleGroup could be removed, false otherwise.
     */
    public boolean removeInvisibleGroup() { return removeEditGroup(INVISIBLE); }

    /**
     * Returns a ZSpatialIndexGroup to use for a node, or
     * inserts one above the node if none exists.
     */
    public ZSpatialIndexGroup getSpatialIndexGroup() {
        ZSpatialIndexGroup g = (ZSpatialIndexGroup)getEditGroup(INDEX);
        if (g == null) { setEditGroup(INDEX, g = new ZSpatialIndexGroup()); }
        return g;
    }

    /**
     * Returns true if this node has a ZSpatialIndexGroup above it as an edit group,
     * false otherwise.
     */
    public boolean hasSpatialIndexGroup() { return hasEditGroup(INDEX); }

    /**
     * Removes a ZSpatialIndexGroup edit group for above a node. Returns true
     * if the ZSpatialIndexGroup could be removed, false otherwise. Removes all
     * nodeListers registered on the indexed nodes.
     */
    public boolean removeSpatialIndexGroup() { 
	if (hasEditGroup(INDEX)) {
	    ZGroup g = null;
            for (Iterator i = editGroups.iterator(); i.hasNext(); ) {
                g = (ZGroup)i.next();
                if (editGroupType(g) == INDEX) {
		    ZSpatialIndexGroup spatialIndexGroup = (ZSpatialIndexGroup)g;
		    spatialIndexGroup.unregisterAllListeners();
		}
	    }
 	}
	return removeEditGroup(INDEX);
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String str = "Editor(" + editNode + ") = [";
        for (int i = 0; i < editGroups.size(); i++) {
	    ZGroup g = (ZGroup)editGroups.get(i);
	    str += editGroupType(g) + ":" + g + ", ";
	}
	str += "]";

	return str;
    }
}
