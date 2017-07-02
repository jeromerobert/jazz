/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import edu.umd.cs.jazz.util.*;

/**
 * <b>ZPathLayoutManager</b> positions a set of nodes along a path.
 *
 * @author  Lance Good
 * @author  Benjamin B. Bederson
 * @see     ZNode
 * @see     edu.umd.cs.jazz.util.ZLayout
 */

public class ZLayoutManagerPath implements ZLayoutManager {
				// Still need to add support for: 
				//    specified spacing, 
				//    specified tolerance
				//    scale down at each level
    // The shape of the path
    private Shape shape = null;

    // The layout path
    private Vector path = null;

    // Says whether path is closed
    private boolean closed = false;

    // The flatness of the FlattingPathIterator
    private double flatness = 1.0;

    // Should the distribute function iterate or use the specified space
    private boolean exact = true;

    // The specified spacing
    private float space = -1.0f;

    // The tolerance allowed if exact is specified
    private float tolerance = -1.0f;
    
    /**
     * Default Constructor - uses default values unless specifically set
     */
    public ZLayoutManagerPath() {
	path = new Vector();
	// The default shape is a line
	setShape(new Line2D.Float(0.0f,0.0f,1.0f,1.0f));
    }

    /**
     * Apply this manager's layout algorithm to the specified node's children.
     * @param node The node to apply this layout algorithm to.
     */
    public void doLayout(ZNode node) {
	Vector children = node.getChildren();
	
	if (exact) {
	    if (space >= 0) {
		ZLayout.distribute(children,path,space,tolerance,true,closed);
	    }
	    else {
		ZLayout.distribute(children,path,tolerance,closed);
	    }
	}
	else {
	    if (space >= 0) {
		ZLayout.distribute(children,path,space,tolerance,false,closed);
	    }
	    else {
		ZLayout.distribute(children,path,false,closed);
	    }
	}
    }

    /**
     * @return Is the path closed?
     */
    public boolean getClosed() {
	return this.closed;
    }

    /**
     * Sets whether the path is closed to <code>closed</code>
     * @param closed Is the path closed?
     */
    public void setClosed(boolean closed) {
	this.closed = closed;
    }

    /**
     * @return The current flatness of the FlatteningPathIterator used to convert the Shape to points
     */
    public float getFlatness() {
	return (float)flatness;
    }

    /**
     * @param flatness Sets the flatness of the FlatteninPathIterator used to convert the Shape to points
     */
    public void setFlatness(float flatness) {
	this.flatness = (double)flatness;
	setShape(shape);
    }

    /**
     * @return Is exact spacing specified
     */
    public boolean getExact() {
	return exact;
    }

    /**
     * Sets whether the algorithm should iterate to get exact spacing
     * or should run once
     * @param exact Whether exact spacing is specified.
     */
    public void setExact(boolean exact) {
	this.exact = exact;
    }

    /**
     * @return The tolerance allowed if exact spacing is specified.
     */
    public float getTolerance() {
	return tolerance;
    }

    /**
     * @param tolerance The tolerance allowed if exact spacing is specified.
     */
    public void setTolerance(float tolerance) {
	this.tolerance = tolerance;
    }

    /**
     * @return The shape currently in use by this object.
     */
    public Shape getShape() {
	return shape;
    }
    
    /**
     * Sets the shape that this layout manager will use.  Gets the points
     * from this shape using a FlatteningPathIterator with flatness constant
     * of 1.0 by default.
     * @param s The desired layout shape
     */      
    public void setShape(Shape shape) {
	this.shape = shape;

	PathIterator p = shape.getPathIterator(new AffineTransform());
		
	FlatteningPathIterator fp = new FlatteningPathIterator(p, flatness);
		
	path.clear();
	while(!fp.isDone()) {
	    float[] farr = new float[6];
	    int type = fp.currentSegment(farr);
		    
	    if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
		path.addElement(new Point2D.Float(farr[0],farr[1]));
	    }
	    if (type == PathIterator.SEG_QUADTO) {
		for(int i=0; i<2; i++) {
		    path.addElement(new Point2D.Float(farr[0],farr[1]));
		}
	    }
	    if (type == PathIterator.SEG_CUBICTO) {
		for(int i=0; i<3; i++) {
		    path.addElement(new Point2D.Float(farr[2*i],farr[2*i+1]));
		}
	    }
	    fp.next();
	}
    }

    /**
     * @return The current spacing used by the layout algorithm during its first iteration.
     */
    public float getSpace() {
	return space;
    }
    
    /**
     * @param space The spacing used by the layout algorithm during its first
     *              iteration.
     */
    public void setSpacing(float space) {
	this.space = space;
    }

}







