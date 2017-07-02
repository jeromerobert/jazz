/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.io.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.*;

/**
 * <b>ZSceneGraphPath</b> represents a unique path in a scene graph from a top-level camera to a terminal
 * node. The class also encapsulates a transform, indicating
 * the composited transform from the top-level camera to the leaf, possibly going through zero
 * or more internal cameras.
 *
 * ZSceneGraphPath objects are generated by the Jazz pick methods.
 * @see ZDrawingSurface#pick
 *
 * @author Jonathan Meyer, Aug 99
 * @author Ben Bederson
 */
public final class ZSceneGraphPath implements Serializable {
				// Better to overallocate a bit, and reduce the possibility of dynamic re-allocation
    static private final int INITIAL_PATH_LENGTH = 10;

    private ZRoot root = null;	               // Root of the scenegraph
    private ZCamera topCamera = null;          // The top-level camera.  Set even if the path is empty.
    private ZNode topCameraNode = null;        // The node of the top camera.  Set even if the path is empty.
    private ZSceneGraphObject parents[];       // List of objects in path excluding the terminal object
    private int numParents = 0;                // Number of objects in path list
    private ZSceneGraphObject terminal = null; // The terminal object
    private AffineTransform transform = null;  // The cumulative transform of the path, including internal cameras

    public ZSceneGraphPath() {
    	parents = new ZSceneGraphObject[INITIAL_PATH_LENGTH];
	transform = new AffineTransform();
    }

    /**
     * Returns the terminal object in the path. This is
     * either a ZVisualComponent or a ZNode.
     */
    public ZSceneGraphObject getObject() {
        return terminal;
    }

    /**
     * Sets the terminal object in the path.
     */
    public void setObject(ZSceneGraphObject object) {
        terminal = object;
    }

    /**
     * Returns the node associated with the top-level camera in the path.
     * Even if the path is empty, this node is guaranteed to be st.
     */
    public ZNode getTopCameraNode() {
        return topCameraNode;
    }

    /**
     * Sets the node associated with the top-level camera in the path.
     */
    public void setTopCameraNode(ZNode node) {
	topCameraNode = node;
    }

    /**
     * Returns the top-level camera in the path.  This is the first camera that
     * the object was picked within.
     * Even if the path is empty, this camera is guaranteed to be set.
     */
    public ZCamera getTopCamera() {
        return topCamera;
    }

    /**
     * Sets the top-level camera in the path.
     */
    public void setTopCamera(ZCamera camera) {
	topCamera = camera;
    }

    /**
     * Returns the nearest ZNode to the picked object. If the
     * picked object is a ZNode, this simply returns that
     * object. If the picked object is a ZVisualComponent, this
     * returns the ZNode parent of the component.
     * @return the node.
     */
    public ZNode getNode() {
    	if (terminal instanceof ZVisualComponent) {
    	    for (int i = numParents - 1; i >= 0; i--) {
    	        Object p = parents[i];
    	        if (p instanceof ZNode) return (ZNode)p;
    	    }
    	}
    	        // terminal is either null or a ZNode - safe
    	        // to simply cast it and return it.
        return (ZNode)terminal;
    }

    /**
     * Returns the nearest ZCamera to the picked object.
     * That is, this returns the last camera on the path.
     * If the path is empty, it returns the top-level camera.
     * @return the camera.
     */
    public ZCamera getCamera() {
    	if (terminal instanceof ZCamera) {
	    return (ZCamera)terminal;
	}
	for (int i = numParents - 1; i >= 0; i--) {
	    Object p = parents[i];
	    if (p instanceof ZCamera) return (ZCamera)p;
    	}

	return topCamera;
    }

    /**
     * Returns the root node for this path.
     */
    public ZRoot getRoot() { return root; }

    /**
     * Sets the root node for this path.
     */
    public void setRoot(ZRoot root) { this.root = root; }

    /**
     * Returns the transform for this path. This is formed by compositing
     * the all the transforms of the scene graph objects in this path.
     * This resulting transform represents the local coordinate system
     * of the terminal element of this path.  If the path is empty,
     * then the transform just contains the top-level camera view transform
     */
    public AffineTransform getTransform() { return transform; }

    /**
     * Sets the transform for this path.
     */
    public void setTransform(AffineTransform tm) { this.transform = tm; }

    /**
     * Converts the specified point from screen coordinates to 
     * global coordinates through the top-level camera of this path.
     * @param pt The pt to be transformed
     */
    public void screenToGlobal(Point2D pt) {
	AffineTransform inverseTransform = topCamera.getInverseViewTransform();
	inverseTransform.transform(pt, pt);
    }

    /**
     * Converts the specified rectangle from screen coordinates to 
     * global coordinates through the top-level camera of this path.
     * @param rect The rect to be transformed
     */
    public void screenToGlobal(Rectangle2D rect) {
	AffineTransform inverseTransform = topCamera.getInverseViewTransform();
	ZTransformGroup.transform(rect, inverseTransform);
    }

    /**
     * Converts the specified point from screen coordinates to the
     * local coordinate system of the terminal scene graph object in
     * this path.
     * @param pt The pt to be transformed
     */
    public void screenToLocal(Point2D pt) {
	try {
	    AffineTransform inverseTransform = transform.createInverse();
	    inverseTransform.transform(pt, pt);
	} catch (NoninvertibleTransformException e) {
	    System.out.println(e);
	}
    }

    /**
     * Converts the specified rectangle from screen coordinates to the
     * local coordinate system of the terminal scene graph object in
     * this path.
     * @param rect The rect to be transformed
     */
    public void screenToLocal(Rectangle2D rect) {
	try {
	    AffineTransform inverseTransform = transform.createInverse();
	    ZTransformGroup.transform(rect, inverseTransform);
	} catch (NoninvertibleTransformException e) {
	    System.out.println(e);
	}
    }

    /**
     * Returns the number of internal ZSceneGraphObjects between the root and the terminal object.
     */
    public int getNumParents() { return numParents; }

    /**
     * Returns the i'th scene graph object between the root and the terminal object. The parent
     * at position to 0 is closest to the root, and the parent at the last
     * position is closest to the terminal object.
     * @param i The index of the path element to return
     * @return The scene graph object
     */
    public ZSceneGraphObject getParent(int i) {
        if (i >= numParents || i < 0)
	    throw new IndexOutOfBoundsException(
		"Index: "+i+", Size: "+numParents);
	return parents[i];
    }

    /**
     * Adds a node to the end of the list of parent nodes.
     * This is used during picking by ZNodes and ZVisualComponents
     * to construct a path from the root to the picked object.
     * @param sgo The scene graph object to be added to the path
     */
    public void push(ZSceneGraphObject sgo) {
	try {
	    parents[numParents] = sgo;
	} catch (ArrayIndexOutOfBoundsException e) {
    	    int newLen = parents.length * 2;
    	    ZSceneGraphObject tmp[] = new ZSceneGraphObject[newLen];
    	    System.arraycopy(parents, 0, tmp, 0, parents.length);
	    parents = tmp;
	    parents[numParents] = sgo;
    	}
	numParents++;
    }

    /**
     * Removes a node (and any nodes after it) from the list of parent nodes.
     * This is used during picking by ZNodes and ZVisualComponents
     * to construct a path from the root to the picked object.
     * @param sgo The scene graph object to be removed from the path
     */
    public void pop(ZSceneGraphObject sgo) {
    	for (int i = numParents - 1; i >= 0; i--) {
	    if (parents[i] == sgo) {
	        numParents = i;
	        return;
	    }
	}
        throw new IllegalArgumentException(sgo + " is not on path.");
    }

    /**
     * Trims the capacity of the array that stores the parents list points to
     * the actual number of points.  Normally, the parents list arrays can be
     * slightly larger than the number of points in the parents list.
     * An application can use this operation to minimize the storage of a
     * parents list.
     */
    public void trimToSize() {
	ZSceneGraphObject[] newParents = new ZSceneGraphObject[numParents];
	for (int i=0; i<numParents; i++) {
	    newParents[i] = parents[i];
	}
	parents = newParents;
    }

    /**
     * Returns a string description of this path useful for debugging.
     * @return the string
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("ZSceneGraphPath[transform=" + transform
    	    + ", root=" + getRoot() + ", object=" + getObject() + ", parents=");
    	for (int i = 0; i < numParents; i++) {
    	    sb.append(parents[i]);
    	    if (i < numParents - 1) sb.append(", ");
    	}
    	sb.append("]");
    	return sb.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }
}