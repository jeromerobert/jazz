/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.util.Stack;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.*;

import edu.umd.cs.jazz.util.*;

/** 
 * ZRenderContext stores information relevant to the current render
 * as it occurs.  The ZRenderContext lives with the scenegraph because
 * this way the scenegraph can be queried about how it is currently being
 * rendered (if at all).
 * The ZRenderContext stores information about what cameras are being rendered
 * (there could be more than one if a portal is being recursively rendered).
 * 
 * @author Benjamin B. Bederson
 * @see ZCamera
 */
public class ZRenderContext extends Object {
    /** List of (recursive) cameras currently rendering the scenegraph     */
    protected Stack      cameras;

    /**
     * List of (recursive) transforms that are the transform that the
     * camera started with before it started painting.  Visual components
     * could need to know this for advanced techniques.
     */
    protected Stack      transforms;

    protected ZSurface   surface;
    protected Graphics2D g2;
    protected ZArea      visibleArea;
    protected Stack      cameraMagStack;
    protected float      cameraMagnification = 1.0f;
    
    /** 
     * Constructs a new ZRenderContext.
     */
    public ZRenderContext(Graphics2D aG2, ZArea aVisibleArea, ZSurface aSurface) {
	surface = null;
	cameras = new Stack();
	transforms = new Stack();
	cameraMagStack = new Stack();
	g2 = aG2;
	visibleArea = aVisibleArea;
	surface = aSurface;
    }

    public Graphics2D getGraphics2D() {
	return g2;
    }

    public void setVisibleArea(ZArea area) {
	visibleArea = area;
    }

    public ZArea getVisibleArea() {
	return visibleArea;
    }

    public ZSurface getSurface() {
	return surface;
    }

    /**      */
    public ZCamera getRenderingCamera() {
	if (cameras.isEmpty()) {
	    return null;
	} else {
	    return (ZCamera)cameras.peek();
	}
    }

    /**      */
    public AffineTransform getRenderingTransform() {
	if (transforms.isEmpty()) {
	    return null;
	} else {
	    return (AffineTransform)transforms.peek();
	}
    }

    /**      */
    public void pushCamera(ZCamera camera) {
	cameras.push(camera);
	transforms.push(g2.getTransform());
	cameraMagStack.push(new Float(cameraMagnification));
	
	cameraMagnification *= camera.getMagnification();
    }

    /**      */
    public void popCamera() {
	cameras.pop();
	transforms.pop();
	cameraMagnification = ((Float)cameraMagStack.pop()).floatValue();
    }

    /**
     * Returns the magnification of the current camera being rendered within.
     * If currently being rendered within nested cameras, then this returns <em>only</em>
     * the magnification of the current camera.  Note that this does <em>not</em>
     * include the transformations of the current or any other object being rendered.
     * @see #getCompositeMagnification
     */
    public float getCameraMagnification() {
	return cameraMagnification;
    }

    /**
     * Returns the total current magnification that is currently being used for rendering.
     * This includes the magnifcation of the current cameras as well as the scale
     * of the current any parent objects.
     * @see #getCameraMagnification
     */
    public float getCompositeMagnification() {
	Point2D pt1 = new Point2D.Float(0.0f, 0.0f);
        Point2D pt2 = new Point2D.Float(1.0f, 0.0f);
	AffineTransform t = g2.getTransform();
        t.transform(pt1, pt1);
        t.transform(pt2, pt2);
        float mag = (float)pt1.distance(pt2);

        return mag;   
    }
}
