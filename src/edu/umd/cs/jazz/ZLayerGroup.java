/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.util.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZLayerGroup</b> is a group node that can be a paint start point of a camera.
 *
 * @author Ben Bederson
 */
public class ZLayerGroup extends ZGroup implements ZSerializable, Serializable {
    /**
     * All the cameras explicitly looking onto the scene graph at this node.  Other cameras
     * may actually see this node *indirectly* (some ancestor may have a camera looking at it.)
     */
    private ZCamera[] cameras = new ZCamera[1];

    /**
     * The actual number of cameras looking at this node.
     */
    private int numCameras = 0;

    //****************************************************************************
    //
    //                 Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new ZLayerGroup.  The node must be attached to a live scenegraph (a scenegraph that is
     * currently visible) order for it to be visible.
     */
    public ZLayerGroup () {
    }

    /**
     * Constructs a new layer group node with the specified node as a child of the
     * new group.
     * @param child Child of the new group node.
     */
    public ZLayerGroup(ZNode child) {
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
    public void duplicateObject(ZLayerGroup refNode) {
	super.duplicateObject(refNode);
    }

    /**
     * Duplicates the current node by using the copy constructor.
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     * <P>
     * In particular, for a layer group, the cameras pointing at this node are not copied.
     * Rather, the layer must be manually added to a specific camera.
     *
     * @return A copy of this node.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZLayerGroup copy;

	objRefTable.reset();
	copy = new ZLayerGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Trims the capacity of the array that stores the cameras list points to
     * the actual number of points.  Normally, the cameras list arrays can be
     * slightly larger than the number of points in the cameras list.
     * An application can use this operation to minimize the storage of a
     * cameras list.
     */
    public void trimToSize() {
	ZCamera[] newCameras = new ZCamera[numCameras];
	for (int i=0; i<numCameras; i++) {
	    newCameras[i] = cameras[i];
	}
	cameras = newCameras;
    }

    /**
     * Internal method to add the camera to the list of cameras that this node is visible within.
     * If camera is already listed, it will not be added again.
     *
     * @param camera The camera this node should be visible within
     */
    void addCamera(ZCamera camera) {
				// First check to see if the camera is already on the list
	for (int i=0; i<numCameras; i++) {
	    if (camera == cameras[i]) {
		return;
	    }
	}
				// Else, add it
	try {
	    cameras[numCameras] = camera;
	} catch (ArrayIndexOutOfBoundsException e) {
	    ZCamera[] newCameras = new ZCamera[cameras.length * 2];
	    System.arraycopy(cameras, 0, newCameras, 0, numCameras);
	    cameras = newCameras;
	    cameras[numCameras] = camera;
	}
	numCameras++;
	repaint();
    }

    /**
     * Internal method to remove camera from the list of cameras that this node is visible within.
     *
     * @param camera The camera this node is no longer visible within
     */
    void removeCamera(ZCamera camera) {
	repaint();
	for (int i=0; i<numCameras; i++) {
				// Find camera within cameras list
	    if (camera == cameras[i]) {
				// Then, slide down other cameras, effectively removing this one
		for (int j=i; j < (numCameras - 1); j++) {
		    cameras[j] = cameras[j+1];
		}
		cameras[numCameras - 1] = null;
		numCameras--;
		break;
	    }
	}
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
	    System.out.println("ZNode.repaint: this = " + this);
	}

				// ZLayerGroup needs to override the base repaint method
				// so it can pass on the repaint call to the cameras that
				// look at this layer.
	repaint(getBounds());
    }

    /**
     * Method to pass repaint methods up the tree,
     * and to any cameras looking here.  Repaints only the sub-portion of
     * this object specified by the given ZBounds.
     * Note that the input parameter may be modified as a result of this call.
     * @param repaintBounds The bounds to repaint
     */
    public void repaint(ZBounds repaintBounds) {
	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println("ZLayerGroup.repaint(ZBounds): this = " + this);
	    System.out.println("ZLayerGroup.repaint(ZBounds): repaintBounds = " + repaintBounds);
	}

	super.repaint(repaintBounds);

	for (int i=0; i<numCameras; i++) {
	    cameras[i].repaint(repaintBounds);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }
}
