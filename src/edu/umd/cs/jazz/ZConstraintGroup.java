/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>ZConstraintGroup</b> is a transform group node that supports the application of a simple
 * constraint to its children.  Sub-classes must override the @link{#getTransform} 
 * and @link{#getTransformField} methods of @link{ZTransformGroup} to define the new transform.
 * This class stores a reference
 * to a camera so, for example, a sub-class could define a constraint
 * dependent on the camera so that the children move whenever the camera view changes.  
 *
 * @author  Benjamin B. Bederson
 */
public class ZConstraintGroup extends ZTransformGroup implements Serializable {
				// Note that while conceptually, a constraint group access the
				// camera view transform whenever the transform is needed, that approach
				// is inefficient because the constraint has to be recomputed on every
				// render, even when the camera hasn't changed.  
				// 
				// So, instead, this node is implemented by adding a camera event listener
				// that gets called back whenever the camera view is changed, and then
				// the constraint is computed and cached for future use.
    /**
     * The camera the constraint is related to
     */
    protected ZCamera camera = null;

    /**
     * The listener used to know when the camera view changes.
     */
    private transient ZCameraListener cameraListener = null;

    /**
     * Constructs a new constraint group.
     * This constructor does not specify a camera to be used in calculating the constraint.
     * Set the camera separately to make the node behave properly.
     * @see #setCamera
     */
    public ZConstraintGroup() {
	init(null);
    }
    
    /**
     * Constructs a new constraint group with a specified camera.
     * @param camera The camera the node is related to.
     */
    public ZConstraintGroup(ZCamera camera) {
	init(camera);
    }
    
    /**
     * Constructs a new constraint group that decorates the specified child.
     * @param child The child that should go directly below this node.
     */
    public ZConstraintGroup(ZNode child) {
	super(child);
    }

    /**
     * Constructs a new constraint group with a specified camera that decorates the specified child.
     * @param camera The camera the node is related to.
     * @param child The child that should go directly below this node.
     */
    public ZConstraintGroup(ZCamera camera, ZNode child) {
	super(child);

	init(camera);
    }

    /**
     * Disposes of this constraint group when it is no longer used.
     */
    public void finalize() {
				// Remove the camera listener if there is one
	if ((camera != null) && (cameraListener != null)) {
	    camera.removeCameraListener(cameraListener);
	}
    }

    /**
     * Internal method to help node construction.
     */
    protected void init(ZCamera camera) {
	setCamera(camera);
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
    public void duplicateObject(ZConstraintGroup refNode) {
	super.duplicateObject(refNode);

	setCamera(refNode.camera);
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
	ZConstraintGroup copy;

	objRefTable.reset();
	copy = new ZConstraintGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Computes the constraint that defines the child to not move
     * even as the camera view changes.  This is called whenever
     * the camera view changes.  This should be overrided
     * by sub-classes defining constraints.  
     * @return The new transform
     */
    public AffineTransform computeTransform() {
	return new AffineTransform();
    }

    /**
     * Internal method to recompute the constraint transform.
     * This should be called whenever some internal state that can
     * affect the constraint changes.
     */
    protected void updateTransform() {
	setTransform(computeTransform());
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************
        
    /**
     * Get the camera that this node is related to.
     * @return the camera
     */   
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Set the camera that this node is related to
     * @param camera The new camera
     */
    public void setCamera(ZCamera camera) {
				// First, remove old camera listener
	if ((camera != null) && (cameraListener != null)) {
	    camera.removeCameraListener(cameraListener);
	}

				// Then, set new camera
	this.camera = camera;

				// Finally, create a new camera listener so we get
				// notified whenever the camera view changes
	if (camera != null) {
	    createCameraListener();
	    updateTransform();
	}
    }

    // Create the camera listener that watches for camera view changes.
    private void createCameraListener() {
	cameraListener = new ZCameraListener() {
	    public void viewChanged(ZCameraEvent e) {
		updateTransform();
	    }
	};
	camera.addCameraListener(cameraListener);
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

	if (camera != null) {
	    out.writeState("ZCamera", "camera", camera);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add camera visual component
	if (camera != null) {
	    out.addObject(camera);
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

	if (fieldName.compareTo("camera") == 0) {
	    setCamera((ZCamera)fieldValue);
	}
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	System.out.println("unserializing ZConstraintGroup");
	in.defaultReadObject();
	createCameraListener();
    }
}
