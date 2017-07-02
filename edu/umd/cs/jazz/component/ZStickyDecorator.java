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
 * <b>ZStickyDecorator</b> is a decorator that forces the child to always be rendered
 * at the same place and size, independent of the camera view.  Thus, as a particular
 * camera that looks at the component changes its magnification, the component changes 
 * its transform by the inverse of the camera's transform  so it's position is not changed.
 * <p>
 * A basic way to use this is to create a constraint decorator that wraps a 
 * simple visual component.  The following code creates a rectangle, decorates
 * it with a constraint, and then creates a node with the decorator chain.
 * The result is that the rectangle will not move as the camera changes its view.
 * <pre>
 *	ZRectangle rect;
 *	ZNode node;
 *	ZStickyDecorator sticky;

 *	rect = new ZRectangle(0, 0, 50, 50);
 *	sticky = new ZStickyDecorator(camera, rect);
 *	node = new ZNode(sticky);
 *	layer.addChild(node);
 *      surface.restore();
 * </pre>
 *
 * @author  Benjamin B. Bederson
 */
public class ZStickyDecorator extends ZConstraintDecorator {
    /**
     * Constructs a new sticky decorator.
     */
    public ZStickyDecorator() {
	super();
    }
    

    /**
     * Constructs a new sticky decorator with a specified camera.
     * @param camera The camera the component is related to.
     */
    public ZStickyDecorator(ZCamera camera) {
	super(camera);
    }
    
    /**
     * Constructs a new sticky decorator with a specified camera that decorates the specified child.
     * @param camera The camera the component is related to.
     * @param child The child that should go directly below this decorator.
     */
    public ZStickyDecorator(ZCamera camera, ZVisualComponent child) {
	super(camera, child);
    }

    /**
     * Constructs a new ZStickyDecorator that is a duplicate of the reference one, i.e., a "copy constructor"
     * @param <code>sticky</code> Reference sticky decorator
     */
    public ZStickyDecorator(ZStickyDecorator sticky) {
	super(sticky);
    }

    /**
     * Duplicates the current ZStickyDecorator by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZStickyDecorator(ZStickyDecorator)
     */
    public Object clone() {
	return new ZStickyDecorator(this);
    }

    /**
     * Paints the child, but applies the sticky so that it appears in a fixed place
     * independent of the camera.  This method overrides the ZConstraintDecorator
     * paint method in order to implement sticky objects in a special way to
     * avoid the "jitter" that results from rounding errors otherwise.
     * @param <code>renderContext</code> The render context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();

        if (child != null) {
	    AffineTransform saveTransform = g2.getTransform();      // Store the original transform

				// The ZConstraintDecorator paint method concatenates the computed
				// transform to the current transform - but that results in jitter
				// due to rounding error.  Instead, we just set the transform
				// to what it should be without the camera at all - resulting
				// in bypassing the camera's transform instead of applying it
				// and then it's inverse.
	    g2.setTransform(renderContext.getRenderingTransform());
	    g2.transform(findNode().computeGlobalCoordinateFrame());
	    child.paint(renderContext);

	    g2.setTransform(saveTransform);       // Restore the transform state
	}
    }

    /**
     * Computes the constraint that defines the child to not move
     * even as the camera view changes.
     * @return the affine transform the defines the constraint.
     */
    protected AffineTransform computeTransform() {
	AffineTransform at;
	ZNode node = findNode();

	if ((camera != null) && (node != null)) {
	    try {
		AffineTransform globalCoordFrame = node.computeGlobalCoordinateFrame();
		at = globalCoordFrame.createInverse();
		at.concatenate(camera.getInverseViewTransform().getAffineTransform());
		at.concatenate(globalCoordFrame);
	    } catch (NoninvertibleTransformException e) {
				// Couldn't invert transform - not much we can do here.
		System.out.println("ZStickyDecorator.computeTransform: Can't compute transform inverse");
		at = new AffineTransform();
	    }
	} else {
	    at = new AffineTransform();
	}

	return at;
    }
}
