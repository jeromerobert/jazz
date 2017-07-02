/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.scenegraph.*;

/**
 * <b>ZConstraintDecorator</b> is a ZVisualComponent decorator that applies a simple
 * constraint to the child that modifies the transform, effectively controlling where
 * it is in space.  The constraint can be
 * dependent on the camera so that the child moves whenever the camera view changes.  
 * A subclass must define the constraint that specifies the relationship between the 
 * child and the camera.
 * @author  Benjamin B. Bederson
 * @see     #computeTransform
 */
public class ZConstraintDecorator extends ZVisualComponentDecorator {
    /**
     * The camera the child is related to
     */
    protected ZCamera camera = null;

    /**
     * Constructs a new constraint decorator
     * This constructor does not specify a camera to be used in calculating the constraint.
     * Set the camera separately to make the component behave properly.
     * @see #setCamera
     */
    public ZConstraintDecorator() {
    }
    
    /**
     * Constructs a new constraint decorator with a specified camera.
     * @param camera The camera the component is related to.
     */
    public ZConstraintDecorator(ZCamera camera) {
	this.camera = camera;
    }
    
    /**
     * Constructs a new constraint decorator with a specified camera that decorates the specified child.
     * @param camera The camera the component is related to.
     * @param child The child that should go directly below this decorator.
     */
    public ZConstraintDecorator(ZCamera camera, ZVisualComponent child) {
	super(child);
	this.camera = camera;
	updateVolatility();
    }
    
    /**
     * Constructs a new ZConstraintDecorator that is a duplicate of the reference one, i.e., a "copy constructor"
     * @param <code>constraint</code> Reference constraint decorator
     */
    public ZConstraintDecorator(ZConstraintDecorator constraint) {
	super(constraint);
	camera = constraint.camera;
    }

    /**
     * Duplicates the current ZConstraintDecorator by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZConstraintDecorator(ZConstraintDecorator)
     */
    public Object clone() {
	return new ZConstraintDecorator(this);
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************
        
    /**
     * Get the camera that this component is related to.
     * @return the camera
     */   
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Get the camera that this component is related to
     * @param camera The new camera
     */
    public void setCamera(ZCamera camera) {
	this.camera = camera;
	damage();
    }
    
    /**
     * All constrained objects are volatile - their bounds change
     * depending on the particular constraint, and thus must
     * not be cached.
     */
    public boolean isVolatile() {
	return true;
    }

    /**
     * Overrides the method to insert this decorator into the decorator chain
     * so that the volatility of the component will be updated.
     */
    public void insertAbove(ZVisualComponent c) {
	super.insertAbove(c);
	updateVolatility();
    }

    /**
     * Overrides the method to remove this decorator from the decorator chain
     * so that the volatility of the component will be updated.
     */
    public void remove() {
	super.remove();
	updateVolatility();
    }
        
    /**
     * Overrides the method to remove this decorator from the decorator chain
     * so that the volatility of the component will be updated.
     */
    public void setParent(ZScenegraphObject aParent) {
	super.setParent(aParent);
	updateVolatility();
    }
        
    /**
     * Returns true if the child is picked.  Does so by applying the constraint so that
     * the pick rectangle is mapped in the same way that the child is painted.
     * @param <code>renderContext</code> The render context to paint into.
     */
    public boolean pick(Rectangle2D rect) {
	Rectangle2D transformedRect = (Rectangle2D)rect.clone();
	
	try {
	    AffineTransform invTransform = computeTransform().createInverse();
	    ZUtil.transform(transformedRect, invTransform);
	} catch (NoninvertibleTransformException e) {
				// Couldn't invert transform - not much we can do here.
	    System.out.println("ZConstraintDecorator.pick: Can't compute transform inverse");
	}
	return super.pick(transformedRect);
    }

    /**
     * Paints the child, but applies the constraint so that it appears in the right place.
     * @param <code>renderContext</code> The render context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();

        if (child != null) {
	    AffineTransform saveTransform = g2.getTransform();      // Store the original transform

				// Change the transform depending on the constraint so that
				// the child appears in the appropriate place.
	    g2.transform(computeTransform());
	    child.paint(renderContext);

	    g2.setTransform(saveTransform);       // Restore the transform state
	}
    }

    /**
     * Compute the bounds based on the child's bounds - but modified according
     * to the constraint.
     */
    protected void computeLocalBounds() {
	localBounds.reset();
	if (child != null) {
	    localBounds.add(child.getLocalBounds());
	    localBounds.transform(computeTransform());
	}
    }

    /**
     * This defines the constraint that specifies where the child gets rendered.
     * By default, it returns an identity transform, but a sub-class can override this method to
     * compute the transform based on various kinds of state, such as the current camera view.
     * This transform gets applied to the graphics context before the child gets rendered,
     * and is used to compute the bounds.
     * <p>
     * When sub-classes define this, they must carefully consider the chain of
     * transforms that may have been applied to the current graphics.  The transform
     * that this method returns is concatenated to the current graphics transform.
     * The current graphics transform typically contains a sequence of transforms
     * (camera transform, node 1 transform, node 2 transform, ...).
     * <p>
     * For example, if the current transform is: [C T1 T2], and you want to 
     * create a sticky object by undoing the camera transform, you must concatenate
     * [T2inv T1inv Cinv T1 T2] because concatenating that onto the current
     * transform results in [T1 T2].
     * @return The transform that specifies the constraint.
     */
    protected AffineTransform computeTransform() {
	return new AffineTransform();
    }

    /**
     * Applies the inverse of the constraint transform to the associated node of this decorator.
     * This is designed to be applied when the constraint is added so the object won't move
     * as a result of this decorator being added.  This method must be called by the
     * application after the constraint decorator is added.
     */
    public void applyInverseTransform() {
	ZNode node = findNode();
	if ((node != null) && (camera != null)) {
	    try {
		AffineTransform invTransform = computeTransform().createInverse();
		node.getTransform().concatenate(invTransform);
	    } catch (NoninvertibleTransformException e) {
				// Couldn't invert transform - not much we can do here.
		System.out.println("ZConstraintDecorator.applyInverseTransform: Can't compute transform inverse");
	    }
	}
    }

    /**
     * Applies the constraint transform to the associated node of this decorator.
     * This is designed to be applied when the constraint is removed so the object won't move
     * as a result of this decorator being removed.  This method must be called by the
     * application just before the constraint decorator is removed.
     */
    public void applyTransform() {
	ZNode node = findNode();
	if ((node != null) && (camera != null)) {
	    node.getTransform().concatenate(computeTransform());
	}
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

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
}
