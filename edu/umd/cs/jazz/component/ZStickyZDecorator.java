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
 * <b>ZStickyZDecorator</b> is a decorator that forces the child to always be rendered
 * at the same scale.  Thus, as a particular camera that looks at the component changes its
 * magnification, the component changes its size by the inverse of the camera's magnification
 * so it's magnification is not changed.
 * <p>
 * A basic way to use this is to create a constraint decorator that wraps a 
 * simple visual component.  The following code creates a rectangle, decorates
 * it with a constraint, and then creates a node with the decorator chain.
 * The result is that the rectangle will pan with the camera, but won't
 * change size as the camera changes its magnification.
 * <pre>
 *	ZRectangle rect;
 *	ZNode node;
 *	ZStickyZDecorator sticky;

 *	rect = new ZRectangle(0, 0, 50, 50);
 *	sticky = new ZStickyZDecorator(camera, rect);
 *	node = new ZNode(sticky);
 *	layer.addChild(node);
 *      surface.restore();
 * </pre>
 *
 * @author  Benjamin B. Bederson
 */
public class ZStickyZDecorator extends ZConstraintDecorator {
    /**
     * Defaults for the point of the child that will be fixed.
     */
    static public float stickyPointX_DEFAULT = 0.5f;
    static public float stickyPointY_DEFAULT = 0.5f;

    /**
     * The point of the child that will be fixed.
     */
    protected float stickyPointX = stickyPointX_DEFAULT;
    protected float stickyPointY = stickyPointY_DEFAULT;

    /**
     * Internal point used for temporary storage.
     */
    protected Point2D pt = new Point2D.Float();

    /**
     * Constructs a new sticky z decorator.
     */
    public ZStickyZDecorator() {
	super();
    }
    
    /**
     * Constructs a new sticky z decorator with a specified camera.
     * @param camera The camera the component is related to.
     */
    public ZStickyZDecorator(ZCamera camera) {
	super(camera);
    }
    
    /**
     * Constructs a new sticky z decorator with a specified camera that decorates the specified child.
     * @param camera The camera the component is related to.
     * @param child The child that should go directly below this decorator.
     */
    public ZStickyZDecorator(ZCamera camera, ZVisualComponent child) {
	super(camera, child);
    }

    /**
     * Constructs a new ZStickyZDecorator that is a duplicate of the reference one, i.e., a "copy constructor"
     * @param <code>stickyz</code> Reference stickyz decorator
     */
    public ZStickyZDecorator(ZStickyZDecorator stickyz) {
	super(stickyz);
    }

    /**
     * Duplicates the current ZStickyZDecorator by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZStickyZDecorator(ZStickyZDecorator)
     */
    public Object clone() {
	return new ZStickyZDecorator(this);
    }

    /**
     * Specifies a point on the unit square of the
     * sticky object that will remain fixed when the scene is zoomed.
     * The coordinates range from upper left hand corner (0,0) of
     * the sticky object, to bottom right hand corner (1,1).
     * @param x X coordinate of the sticky point of the sticky object.
     * @param y Y coordinate of the sticky point of the sticky object.
     */
    public void setStickyPoint(float x, float y) {
	stickyPointX = x;
	stickyPointY = y;
    }
    
    /**
     * Returns a Dimension specifying a point on the sticky object
     * that remains fixed as the scene is zoomed.
     * @return the coordinates of the fixed point of the sticky object.
     */
    public Dimension getStickyPoint() {
	Dimension d = new Dimension();
	d.setSize(stickyPointX, stickyPointY);
	return d;
    }
    
    /**
     * Computes the constraint that defines the child to keep a constant magnification
     * even as the camera magnification changes.
     * @return the affine transform the defines the constraint.
     */
    protected AffineTransform computeTransform() {
	AffineTransform at = new AffineTransform();

	if ((camera != null) && (child != null)) {
	    Rectangle2D childBounds = child.getLocalBounds();
	    float iscale = 1.0f / camera.getMagnification();

				// Compute the position of the child so that its center point
				// stays fixed, and it is scaled by the inverse of the camera
				// magnification.
	    
				// This is computed by getting the "fixed" point that is not
				// supposed to move, and creating a transform that scales
				// around that point by the inverse of the current camera magnification.
	    pt.setLocation((float)(childBounds.getX() + (stickyPointX * childBounds.getWidth())),
			    (float)(childBounds.getY() + (stickyPointY * childBounds.getHeight())));

	    at.translate(pt.getX(), pt.getY());
	    at.scale(iscale, iscale);
	    at.translate(-pt.getX(), -pt.getY());
	}

	return at;
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

	if (fieldName.compareTo("stickyPointX") == 0) {
	    stickyPointX = ((Float)fieldValue).floatValue();
	}
	if (fieldName.compareTo("stickyPointY") == 0) {
	    stickyPointY = ((Float)fieldValue).floatValue();
	}
    }

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out); 

	if (stickyPointX != stickyPointX_DEFAULT) {
	    out.writeState("float", "stickyPointX", stickyPointX);
	}
	if (stickyPointY != stickyPointY_DEFAULT) {
	    out.writeState("float", "stickyPointY", stickyPointY);
	}
    }
}
