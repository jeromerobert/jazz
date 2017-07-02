/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;

import edu.umd.cs.jazz.io.*;

/** 
 * <b>ZLeaf</b> is a basic leaf node that doesn't have any children.
 * 
 * @author Ben Bederson
 */
public class ZLeaf extends ZNode implements ZSerializable, Serializable {

    //****************************************************************************
    //
    //              Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new empty leaf node.
     */
    public ZLeaf() {
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
    public void duplicateObject(ZLeaf refNode) {
	super.duplicateObject(refNode);
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
	ZLeaf copy;

	objRefTable.reset();
	copy = new ZLeaf();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }
}
