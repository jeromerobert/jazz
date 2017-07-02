/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.util.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZLayerGroup</b> is used exclusively to specify the portion of the scenegraph
 * that a camera can see. It has no other function.
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @see ZCamera
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
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZLayerGroup newLayer = (ZLayerGroup)super.duplicateObject();

	    // Shallow-Copy the cameras array.
	    // Note that updateObjectReferences modifies this array. See below.
	if (cameras != null) {
	    newLayer.cameras = (ZCamera[])cameras.clone();
	}
	return newLayer;
    }

    /**
     * Called to update internal object references after a clone operation 
     * by {@link ZSceneGraphObject#clone}.
     *
     * @see ZSceneGraphObject#updateObjectReferences
     */
     protected void updateObjectReferences(ZObjectReferenceTable objRefTable) {
	super.updateObjectReferences(objRefTable);

	if (numCameras != 0) {
	    int n = 0;

	    for (int i = 0; i < numCameras; i++) {
		ZCamera newCamera = (ZCamera)objRefTable.getNewObjectReference(cameras[i]);
		if (newCamera == null) {
		    // Cloned a ZLayerGroup, but did not clone a camera looking at the layer.
		    // Drop the camera from the list of cameras.
		} else {
		    // Cloned a ZLayerGroup and also the camera. Use the new camera.
		    cameras[n++] = newCamera;
		}
	    }
	    numCameras = n;
	}
    }


    /**
     * Trims the capacity of the array that stores the cameras list points to
     * the actual number of points.  Normally, the cameras list arrays can be
     * slightly larger than the number of points in the cameras list.
     * An application can use this operation to minimize the storage of a
     * cameras list.
     */
    public void trimToSize() {
	ZCamera[] newCameras;

	if (numCameras == 0) {
	    newCameras = new ZCamera[1];
	} else {
	    newCameras = new ZCamera[numCameras];
	    for (int i=0; i<numCameras; i++) {
		newCameras[i] = cameras[i];
	    }
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
     * Return a copy of the array of cameras looking onto this layer.
     * This method always returns an array, even when there
     * are no cameras.
     * @return the cameras looking onto this layer
     */
    public ZCamera[] getCameras() {
	ZCamera[] copyCameras = new ZCamera[numCameras];
	System.arraycopy(cameras, 0, copyCameras, 0, numCameras);

	return copyCameras;
    }

    /**
     * Internal method to return a reference to the actual cameras looking onto this layer.
     * It should not be modified by the caller.  Note that the actual number
     * of cameras could be less than the size of the array.  Determine
     * the actual number of cameras with @link{#getNumCameras}.
     * @return the cameras looking onto this layer.
     */
    protected ZCamera[] getCamerasReference() {
	return cameras;
    }

    /**
     * Return the number of cameras of this group node.
     * @return the number of cameras.
     */
    public int getNumCameras() {
	return numCameras;
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

    /**
     * Method to pass repaint methods up the tree,
     * and to any cameras looking here.  Repaints only the sub-portion of
     * this object specified by the given ZBounds.
     * Note that the transform and clipBounds parameters may be modified as a result of this call.
     * @param obj The object to repaint
     * @param at  The affine transform
     * @param clipBounds The bounds to clip to when repainting
     */
    public void repaint(ZSceneGraphObject obj, AffineTransform at, ZBounds clipBounds) {
	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println("ZLayerGroup.repaint(obj, at, bounds): this = " + this);
	    System.out.println("ZLayerGroup.repaint(obj, at, bounds): obj = " + obj);
	    System.out.println("ZLayerGroup.repaint(obj, at, bounds): at = " + at);
	}

	super.repaint(obj, at, clipBounds);

				// The camera could modify the transform and clip bounds,
				// so if there is more than one camera, make a copy of them,
				// and use the copies for each other camera.
	AffineTransform origAT = null;
	ZBounds origClipBounds = null;

	if (numCameras > 1) {
	    origAT = (AffineTransform)at.clone();
	    if (clipBounds != null) {
		origClipBounds = (ZBounds)clipBounds.clone();
	    }
	}
	for (int i=0; i<numCameras; i++) {
	    if (i >= 1) {
		at.setTransform(origAT);
		if (origClipBounds != null) {
		    clipBounds.setRect(origClipBounds);
		}
	    }
	    cameras[i].repaint(obj, at, clipBounds);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();
	if (cameras == null) {
	    cameras = new ZCamera[1];
	}
    }
}
