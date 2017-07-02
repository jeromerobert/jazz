/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.util.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * ZLayout is a utility class that provides general-purpose layout mechanisms
 * to position nodes.
 * 
 * @author Lance Good
 * @author Ben Bederson
 */
public class ZLayout {

    /**
     * Distributes a set of <code>objects</codes> (those being ZNodes)
     * along the path specified by <code>coordinates</code>.  Assumes
     * exact spacing on a non-closed path with default tolerance.
     * @param objects The objects to be distributed.
     * @param coordinates The coordinates of the path.
     * @see #distribute(Vector, Vector, float, boolean)
     */
    static public void distribute(Vector objects, Vector coordinates) {
	distribute(objects,coordinates,-1.0f,false);
    }

    /**
     * Distributes a set of <code>objects</code> (those being ZNodes) along
     * the (optionally closed) path specified by <code>coordinates</code>.
     * <code>exact</code> specifies, if false, that the algorithm should
     * run once using a first guess at spacing or, if true, that the
     * algorithm should attempt to evenly space the objects using the entire
     * path.
     * @param objects The objects to be distributed.
     * @param coordinates The coordinates of the path.
     * @param exact Should the algorithm run once and stop or iterate to 
     *              get exact spacing.
     * @param closedPath Does the path represent a closed path?
     * @see #distribute(Vector, Vector, float, float, boolean, boolean)
     */
    static public void distribute(Vector objects, Vector coordinates, boolean exact, boolean closedPath) {
	// The number of objects
	int numObjs;

	// The amount of space to start the algorithm
	float space;

	// The length of the path 
	float pathLength;

	// The total dimension of all the objects
	float totalDim;

	// The current ZNode
	ZNode obj;

	// The bounds for the current ZNode
	Rectangle2D bounds;
	
	if (objects.isEmpty() || coordinates.isEmpty()) {
	    return;
	}

	pathLength = pathLength(coordinates);
	totalDim = 0.0f;
	numObjs = 0;
	for(int i=0; i<objects.size(); i++) {
	    obj = (ZNode)objects.elementAt(i);
	    bounds = obj.getGlobalBounds();
	    totalDim = totalDim + ((float)(bounds.getWidth()+bounds.getHeight())/(float)2.0);
	    numObjs++;
	}

	if (pathLength == 0.0f) {
	    distribute(objects,coordinates,0.0f,-1.0f,false,closedPath);
	}
	else {
	    if (closedPath) {
		space = (pathLength - totalDim) / (numObjs);
	    }
	    else {
		space = (pathLength - totalDim) / (numObjs-1);
	    }
	    distribute(objects,coordinates,space,-1.0f,exact,closedPath);
	}
    }

    
    /**
     * Distributes a set of <code>objects</code> (those being ZNodes) along
     * the (optionally closed) path specified by <code>coordinates</code>.
     * The algorithm will iterate until the objects are placed along the
     * path within <code>tolerance</code> of the given path.
     * @param objects The objects to be distributed.
     * @param coordinates The coordinates of the path.
     * @param tolerance The error allowed in placing the objects
     * @param closedPath Does the path represent a closed path?
     * @see #distribute(Vector, Vector, float, float, boolean, boolean)
     */
    static public void distribute(Vector objects, Vector coordinates, float tolerance, boolean closedPath) {
	// The number of objects
	int numObjs;

	// The amount of space to start the algorithm
	float space;

	// The length of the path 
	float pathLength;

	// The total dimension of all the objects
	float totalDim;

	// The current ZNode
	ZNode obj;

	// The bounds for the current ZNode
	Rectangle2D bounds;
	
	if (objects.isEmpty() || coordinates.isEmpty()) {
	    return;
	}

	pathLength = pathLength(coordinates);
	totalDim = 0.0f;
	numObjs = 0;
	for(int i=0; i<objects.size(); i++) {
	    obj = (ZNode)objects.elementAt(i);
	    bounds = obj.getGlobalBounds();
	    totalDim = totalDim + ((float)(bounds.getWidth()+bounds.getHeight())/(float)2.0);
	    numObjs++;
	}

	if (pathLength == 0.0f) {
	    distribute(objects,coordinates,0.0f,-1.0f,false,closedPath);
	}
	else {
	    if (closedPath) {
		space = (pathLength - totalDim) / (numObjs);
	    }
	    else {
		space = (pathLength - totalDim) / (numObjs-1);
	    }
	    distribute(objects,coordinates,space,tolerance,true,closedPath);
	}
    }

    /**
     * Distributes the given <code>objects</code> (those being ZNodes)
     * along the (optionally closed) path specified by
     * <code>coordinates</code>.  The algorithm initially distributes
     * the objects with the specified <code>space</code> along the path.
     * If <code>exact</code> spacing is not requested, the algorithm
     * does not iterate.  Hence, the objects will be spaced with the
     * intial value of <code>space</code> regardless of the objects' positions.
     * If <code>exact</code> spacing is requested, the algorithm
     * iterates until the objects and spacing are within
     * <code>tolerance</code> of the given path, where <code>tolerance</code>
     * is specified as a percentage of the total path length.  The algorithm
     * will terminate with suboptimal results if the path is too short for
     * the objects or if an optimal result cannot be computed.
     * @param objects The objects to be distributed.
     * @param coordinates The coordinates of the path.
     * @param space The initial amount of spacing to leave between objects.
     * @param tolerance The percent of the total path length to which
     *                   the algorithm should compute, a value from 0.0-100.0.
     * @param exact Should the algorithm iterate or make a single pass?
     * @param closedPath Does the path represent a closed path?
     */
    static public void distribute(Vector objects, Vector coordinates,
				  float space, float tolerance,
				  boolean exact, boolean closedPath) {
	
	// The default tolerance value
	final float DEFAULT_TOLERANCE = 0.1f;

	// The number of points on the path
	int numCoords;

	// The number of objects 
	int numObjs;

	// The length of the path given by coordinates
	float pathLength;

	// The amount of path passing through the first object - for closed paths
	float closingLength;
	
	// The current copy of objects
	Vector objs;
	
	// The current copy of coordinates
	Vector coords;
	
	// Are we finished?	 
	boolean done = false;
	
	// represents the current error in distributing along the path
	float currentError = -1.0f;
	
	// Storage for a frequently used variable
	Point2D halfDim = new Point2D.Float();

	if ((objects.isEmpty() || (coordinates.isEmpty()))) {
	    return;
	}

	if (!(tolerance > 0.0f && tolerance <= 100.0f)) {
	    tolerance = DEFAULT_TOLERANCE;
	}
	tolerance = tolerance/100.0f;
	
	pathLength = pathLength(coordinates);
	numCoords = coordinates.size();
	numObjs = objects.size();
	closingLength = 0.0f;
	
	
	/* This code handles the special case when the path is closed.  Since
	   the center of the first object will be placed at the first
	   coordinate, we must calculate how much of the path, on closing
	   will intersect the object in order to add this to our
	   pathLengthInObjs */	   
	if (closedPath) {
	    Point2D dir = new Point2D.Float((float)1.0,(float)1.0);
	    Point2D entranceP = (Point2D)((Point2D)coordinates.elementAt(coordinates.size()-1)).clone();
	    ZNode firstObj = (ZNode)objects.elementAt(0);
	    Rectangle2D bounds = firstObj.getGlobalBounds();
	    float halfWidth = ((float)bounds.getWidth())/(2.0f);
	    float halfHeight = ((float)bounds.getHeight())/(2.0f);
	    
	    float curX = (float)entranceP.getX();
	    float curY = (float)entranceP.getY();
	    normalizeVector(dir);

	    Point2D curP = new Point2D.Float(curX, curY);
	    halfDim.setLocation(halfWidth, halfHeight);

	    closingLength = computeDimensionTranslation(halfDim,curP,new Point2D.Float(0.0f,0.0f),new Point2D.Float(0.0f,0.0f),dir,coordinates,false);   
	}


	/* ************** The meat of the algorithm *****************

	   If exact spacing is not specified, this loop is executed once.
	   Otherwise, we loop until we are within "tolerance" of the given
	   path.  The algorithm also exits this loop if it detects an	   
	   infinite loop or the path is too small for the objects
	 */
	while (!done) {
	    float pathLengthInObjs = 0.0f;
	    // Start off with the direction that doesn't favor either dimension
	    Point2D dir = new Point2D.Float((float)1.0,(float)1.0);

	    objs = (Vector)objects.clone();
	    coords = (Vector)coordinates.clone();

	    Point2D entranceP = (Point2D)((Point2D)coords.elementAt(0)).clone();
	    float remainderX = 0.0f;
	    float remainderY = 0.0f;
	    float curX = 0.0f;
	    float curY = 0.0f;	    

	    boolean determined = false;
	    float centerX = 0.0f;
	    float centerY = 0.0f;

	    coords.removeElementAt(0);
	    normalizeVector(dir);	    	    
	    
	    if (closedPath) {
		determined = true;
		centerX = (float)entranceP.getX();
		centerY = (float)entranceP.getY();
	    }

	    // Perform this loop once for each of the objects
	    while (!objs.isEmpty()) {		
		ZNode obj = (ZNode)objs.elementAt(0);
		Rectangle2D bounds = obj.getGlobalBounds();
		Point2D curP;
		Point2D remainderP;
		
		float halfWidth = (float)bounds.getWidth()/(float)2.0;
		float halfHeight = (float)bounds.getHeight()/(float)2.0;
		float entranceX = (float)entranceP.getX();
		float entranceY = (float)entranceP.getY();

		objs.removeElementAt(0);
		curX = entranceX;
		curY = entranceY;


		/******************************************/
		/* Part 1 - find the new center of the    */
		/*   object                               */
		/******************************************/

		if (determined == false) {

		    /* halfDim is a Point2D instead of a Dimension2D because
		       the Java interface doesn't define any Dimension2D
		       classes with floats */
		    Point2D centerP = new Point2D.Float(centerX, centerY);

		    halfDim.setLocation(halfWidth, halfHeight);
		    curP = new Point2D.Float(curX, curY);	    
		    remainderP = new Point2D.Float(remainderX, remainderY);
		    
		    pathLengthInObjs += computeDimensionTranslation(halfDim,curP,remainderP,centerP,dir,coords,true);

		    curX = (float)curP.getX();
		    curY = (float)curP.getY();

		    remainderX = (float)remainderP.getX();
		    remainderY = (float)remainderP.getY();

		    centerX = (float)centerP.getX();
		    centerY = (float)centerP.getY();

		}
		
		determined = false;

		/******************************************/		
		/* End Part 1                             */
		/******************************************/		

		curX = centerX;
		curY = centerY;

		/* Actually Move the object to this center point */

		obj.getTransform().translate((centerX-(float)bounds.getCenterX())/obj.getTransform().getScale(),(centerY-(float)bounds.getCenterY())/obj.getTransform().getScale());

		/******************************************/
		/* Part 2 - Now that we have the center - */
		/*  find out how much more of the path is */
		/*   in the object's bounding box         */
		/* Plus we will find the point where the  */
		/*   path leaves the object's bounding box*/
		/******************************************/		

		float boundaryX = 0.0f;
		float boundaryY = 0.0f;
		
		/* This is a Point2D instead of a Dimension because
		   the very consistent Java interface doesn't define
		   any Dimension classes with floats */
		Point2D boundaryP = new Point2D.Float(boundaryX, boundaryY);

		halfDim.setLocation(halfWidth, halfHeight);
		curP = new Point2D.Float(curX, curY);	    
		remainderP = new Point2D.Float(remainderX, remainderY);

		pathLengthInObjs += computeDimensionTranslation(halfDim,curP,remainderP,boundaryP,dir,coords,true);

		curX = (float)curP.getX();
		curY = (float)curP.getY();
		
		remainderX = (float)remainderP.getX();
		remainderY = (float)remainderP.getY();
		
		boundaryX = (float)boundaryP.getX();
		boundaryY = (float)boundaryP.getY();

		/******************************************/
		/* End Part 2                             */
		/******************************************/

		curX = boundaryX;
		curY = boundaryY;
		
		/******************************************/
		/* Part 3 - Now we add the appropriate    */
		/* amount of space based on our latest    */
		/* calculations for the amount of path    */
		/* in objects the idea here is to         */
		/* eventually get all the objects on the  */
		/* path with equal spacing between them   */
		/******************************************/

		boolean spaced = false;
		float spaceSoFar = 0.0f;
		curP = new Point2D.Float(curX,curY);
		remainderP = new Point2D.Float(remainderX+curX,remainderY+curY);
		
		if ((remainderX != 0.0f) || (remainderY != 0.0f)) {

		    if (remainderP.distance(curP) > space) {
			entranceP.setLocation(curX + space*(float)dir.getX(),curY + space*(float)dir.getY());
			
			spaced = true;
			remainderX = remainderX - ((float)entranceP.getX() - curX);
			remainderY = remainderY - ((float)entranceP.getY() - curY);
		    }
		    else {
			spaceSoFar = (float)remainderP.distance(curP);
			curP.setLocation(remainderP);
			remainderX = 0.0f;
			remainderY = 0.0f;
		    }
		    
		}

		while (!spaced) {

		    if (!coords.isEmpty()) {
			Point2D newP = (Point2D)((Point2D)coords.elementAt(0)).clone();
			float newX = (float)newP.getX();
			float newY = (float)newP.getY();

			coords.removeElementAt(0);			    
			dir.setLocation(newX-(float)curP.getX(),newY-(float)curP.getY());
			normalizeVector(dir);

			if (newP.distance(curP)+spaceSoFar > space) {
			    entranceP.setLocation((float)curP.getX() + (space-spaceSoFar)*(float)dir.getX(),(float)curP.getY() + (space-spaceSoFar)*(float)dir.getY());
			    
			    spaced = true;
			    remainderX = (float)newP.getX() - (float)entranceP.getX();
			    remainderY = (float)newP.getY() - (float)entranceP.getY();
			}
			else {
			    spaceSoFar = (float)newP.distance(curP) + spaceSoFar;
			    curP.setLocation(newX, newY);			    
			}
			
		    }
		    else {
			entranceP.setLocation((float)curP.getX() + (space-spaceSoFar)*(float)dir.getX(),(float)curP.getY() + (space-spaceSoFar)*(float)dir.getY());			
			spaced = true;						
		    }

		}

		/******************************************/
		/* End Part 3                             */
		/******************************************/
		
	    }

	    /******************************************/
	    /* Part 4 - Check to see if we're done,   */
	    /*   then update spacing                  */
	    /******************************************/

	    if (exact) {

		float length;

		/* Add in the closing path length for the case of a
		   closed path (it's 0 anyway if path isn't closed) */
		pathLengthInObjs = pathLengthInObjs + closingLength;
		
		if (closedPath) {
		    length = pathLengthInObjs + (numObjs)*space;
		}
		else {
		    if (numObjs != 1) {
			length = pathLengthInObjs + (numObjs-1)*space;
		    }
		    else {
			length = pathLength;
		    }
		    
		}

		if ((float)Math.abs((length-pathLength)/(pathLength)) < tolerance) {
		    done = true;
		}
		else {
		    
		    if (closedPath) {
			space = (pathLength - pathLengthInObjs)/(numObjs);
		    }
		    else {
			if (numObjs != 1) {
			    space = (pathLength - pathLengthInObjs)/(numObjs-1);
			}
			else {
			    space = pathLength - pathLengthInObjs;
			}
		    }
		    
		    if (space < 0) {
			done = true;
		    }

		    if (pathLength == 0) {
			done = true;
		    }

		}
		
		/* This case was added in the event that the error is not
		   decreasing - signaling a likely infinite loop */
		if (currentError != -1.0f) {
		    if (currentError <= (float)Math.abs(length-pathLength)) {
			done = true;
		    }
		}
		currentError = (float)Math.abs(length-pathLength);

	    }
	    else {
		done = true;
	    }
	    
	    /******************************************/
	    /* End Part 4                             */
	    /******************************************/
	    
	}	
    }
    

    /**
     * A helper function that computes the length of path in the given
     * dimension. Computes the destination location for the given
     * dimension based on the current point, current remainder, and
     * current direction.  The caller can specify whether the
     * coordinates should be viewed in ascending or descending
     * order.  If descending order is used, the coordinates are not
     * removed from the vector.
     * @param dim The dimension of the bounds in which to perfrom the computations
     * @param currentP The current point on the path
     * @param remainderP The remainder of path from previous point
     * @param finishedP The storage location for the resultant destination point
     * @param dir The current direction of the path
     * @param coordinates The coordinates of the path
     * @param ascending Look at the points of the path in ascending or descending order?
     * @return A float equal to the length of the path in dim
     */
    static protected float computeDimensionTranslation(Point2D dim, Point2D currentP,
						       Point2D remainderP, Point2D finishedP,
						       Point2D dir, Vector coordinates,
						       boolean ascending) {		
	boolean finished = false;

	float width = (float)dim.getX();
	float height = (float)dim.getY();	
	
	float currentX = (float)currentP.getX();
	float currentY = (float)currentP.getY();

	float remainderX = (float)remainderP.getX();
	float remainderY = (float)remainderP.getY();
	
	float finishedX = (float)finishedP.getX();
	float finishedY = (float)finishedP.getY();	
	
	float pathSoFarX = 0.0f;
	float pathSoFarY = 0.0f;

	float pathLengthInObjs = 0.0f;
	
	int vectorPosition;

	if (ascending) {
	    vectorPosition = coordinates.size();
	}
	else {
	    vectorPosition = coordinates.size()-2;
	}

	if (remainderX != 0.0f || remainderY != 0.0f) {
	    
	    if (((float)Math.abs((double)remainderX) > width) || ((float)Math.abs((double)remainderY) > height)) {
		/* This case indicates that the remainder extends past
		   the dimensions in one direction */

		/* Here we add NaN checks because 0.0f/0.0f returns NaN
		   instead of infinity.  Thus, if not checked, we could
		   get incorrect results - don't know if 0 will be passed
		   as a dimension or not - better safe */
		if (Float.isNaN(width/(float)Math.abs((double)dir.getX())) || Float.isNaN(height/(float)Math.abs((double)dir.getY()))) {
		    if (Float.isNaN(width/(float)Math.abs((double)dir.getX()))) {
			/* This case means the length of the path
			   along the current direction is
			   smaller in the y direction
			*/
			
			finishedX = currentX + (height)*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			finishedY = currentY + (height)*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			
		    }
		    else {
			/* This case means the length of the path
			   along the current direction is smaller in the
			   x direction
			*/
			
			finishedX = currentX + (width)*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			finishedY = currentY + (width)*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
		    }
		}
		else {
		    
		    if ((width/(float)Math.abs((double)dir.getX())) < (height/(float)Math.abs((double)dir.getY()))) {
			/* This case means the length of the path
			   along the current direction is smaller in the
			   x direction
			*/
			
			finishedX = currentX + (width)*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			finishedY = currentY + (width)*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
		    }
		    else {
			/* This case means the length of the path
			   along the current direction is
			   smaller in the y direction
			*/
			
			finishedX = currentX + (height)*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			finishedY = currentY + (height)*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
		    }
		}		    
		finished = true;
		remainderX = remainderX - (finishedX - currentX);
		remainderY = remainderY - (finishedY - currentY);
		pathLengthInObjs = pathLengthInObjs + (float)Point2D.distance(0.0f,0.0f,finishedX-currentX,finishedY-currentY);
	    }
	    else {
		/* In this case, the segment isn't long enough
		   to extend past the dimensions in either direction
		*/
		pathSoFarX = remainderX;
		pathSoFarY = remainderY;
		currentX = currentX + remainderX;
		currentY = currentY + remainderY;
		pathLengthInObjs = pathLengthInObjs + (float)Point2D.distance(0.0f,0.0f,remainderX,remainderY);
		remainderX = 0.0f;
		remainderY = 0.0f;
	    }
	    
	}
	
	while(!finished) {
	    
	    if (!coordinates.isEmpty() && vectorPosition >=0) {
		Point2D newP = null;

		if (ascending) {
		    newP = (Point2D)((Point2D)coordinates.elementAt(0)).clone();
		    coordinates.removeElementAt(0);			    
		}
		else {
		    newP = (Point2D)((Point2D)coordinates.elementAt(vectorPosition)).clone();
		    vectorPosition--;
		}
		
		float newX = (float)newP.getX();
		float newY = (float)newP.getY();
		dir.setLocation(newX-currentX,newY-currentY);
		normalizeVector(dir);
		
		float curDistX = newX - currentX;
		float curDistY = newY - currentY;
		if (((float)Math.abs(curDistX+pathSoFarX) > width) || ((float)Math.abs(curDistY+pathSoFarY) > height)) {
		    /* This case means that the
		       current segment at least
		       extends past the dimensions
		       in one direction
		    */

		    
		    /* Generally floating point numbers in java are
		       fairly cooperative in divide by zero
		       instances, yielding "Float.POSITIVE_INFINITY"
		       or "Float.NEGATIVE_INFINITY". These
		       "INFINITY" values even work for comparisons.

		       However, if you perform operations on
		       "INFINITY" you get "Float.NaN".  This value does
		       not cooperate with comparison statements so we
		       have to check beforehand to see if
		       we got any NaN.  We shouldn't get division by
		       both components of "dir" yielding NaN
		       because the vector is normalized

		       This applies to all the remaining operations as
		       well.
		    */

		    
		    if (Float.isNaN(width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX()) || Float.isNaN(height/(float)Math.abs((double)dir.getY()) - pathSoFarY/(float)dir.getY())) {
			if (Float.isNaN(width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX())) {
			    if ((double)Math.abs((double)pathSoFarY/(double)dir.getY()) == (double)pathSoFarY/(double)dir.getY()) {
				finishedX = currentX + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
				finishedY = currentY + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    }
			    else {
				finishedX = currentX + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
				finishedY = currentY + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    }			    
			}
			else {
			    if ((double)Math.abs((double)pathSoFarX/(double)dir.getX()) == (double)pathSoFarX/(double)dir.getX()) {
				finishedX = currentX + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
				finishedY = currentY + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    }
			    else {
				finishedX = currentX + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
				finishedY = currentY + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    }
			}
		    }
		    else {

			if ((width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX()) < (height/(float)Math.abs((double)dir.getY()) - pathSoFarY/(float)dir.getY())) {
			    
				/* Now we check whether the current
				   direction is the same as the
				   direction of the previous movements
				*/
			    if ((double)Math.abs((double)pathSoFarX/(double)dir.getX()) == (double)pathSoFarX/(double)dir.getX()) {
				finishedX = currentX + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
				finishedY = currentY + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    }
			    else {
				finishedX = currentX + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
				finishedY = currentY + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    }
			}
			else {
				
				/* Now we check whether the
				   current direction is the same
				   as the direction of the previous
				   movements
				*/	    
			    if ((double)Math.abs((double)pathSoFarY/(double)dir.getY()) == (double)pathSoFarY/(double)dir.getY()) {
				finishedX = currentX + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
				finishedY = currentY + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    }
			    else {
				finishedX = currentX + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
				finishedY = currentY + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    }
			}
						
		    }
		    
		    
		    finished = true;
		    remainderX = newX - finishedX;
		    remainderY = newY - finishedY;
		    pathLengthInObjs = pathLengthInObjs + (float)Point2D.distance(0.0f,0.0f,finishedX-currentX,finishedY-currentY);
		}		
		else {
		    /* This case means that the
		       current segment doesn't
		       extends past the dimensions
		       in either direction
		    */

		    pathSoFarX = pathSoFarX + curDistX;
		    pathSoFarY = pathSoFarY + curDistY;
		    currentX = currentX + curDistX;
		    currentY = currentY + curDistY;
		    pathLengthInObjs = pathLengthInObjs + (float)Point2D.distance(0.0f,0.0f,curDistX,curDistY);				
		}
	    }
	    else {
		if (Float.isNaN(width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX()) || Float.isNaN(height/(float)Math.abs((double)dir.getY()) - pathSoFarY/(float)dir.getY())) {
		    if (Float.isNaN(width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX())) {
			if ((double)Math.abs((double)pathSoFarY/(double)dir.getY()) == (double)pathSoFarY/(double)dir.getY()) {
			    finishedX = currentX + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    finishedY = currentY + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			}
			else {
			    finishedX = currentX + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    finishedY = currentY + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			}			
		    }
		    else {
			if ((double)Math.abs((double)pathSoFarX/(double)dir.getX()) == (double)pathSoFarX/(double)dir.getX()) {
			    finishedX = currentX + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    finishedY = currentY + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			}
			else {
			    finishedX = currentX + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    finishedY = currentY + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			}
		    }
		}
		else {
		    if ((width/(float)Math.abs((double)dir.getX()) - pathSoFarX/(float)dir.getX()) < (height/(float)Math.abs((double)dir.getY()) - pathSoFarY/(float)dir.getY())) {
			/* Now we check whether the current direction
			   is the same as the direction of the
			   previous movements
			*/
			if ((double)Math.abs((double)pathSoFarX/(double)dir.getX()) == (double)pathSoFarX/(double)dir.getX()) {
			    finishedX = currentX + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    finishedY = currentY + (width - (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			}
			else {
			    finishedX = currentX + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			    finishedY = currentY + (width + (float)Math.abs((double)pathSoFarX))*((float)dir.getY()/(float)dir.getX())*((float)dir.getX()/(float)Math.abs((double)dir.getX()));
			}
		    }
		    else {
			
			/* Now we check whether the current direction
			   is the same as the direction of the previous
			   movements
			*/
			
			if ((double)Math.abs((double)pathSoFarY/(double)dir.getY()) == (double)pathSoFarY/(double)dir.getY()) {
			    finishedX = currentX + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    finishedY = currentY + (height - (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			}
			else {
			    finishedX = currentX + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getX()/(float)dir.getY())*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			    finishedY = currentY + (height + (float)Math.abs((double)pathSoFarY))*((float)dir.getY()/(float)Math.abs((double)dir.getY()));
			}
		    }
		}
		finished = true;
		pathLengthInObjs = pathLengthInObjs + (float)Point2D.distance(0.0f,0.0f,finishedX-currentX,finishedY-currentY);			
	    }
	    
	}

	currentP.setLocation(currentX, currentY);
	remainderP.setLocation(remainderX, remainderY);
	finishedP.setLocation(finishedX, finishedY);
	
	return pathLengthInObjs;
    }


    /**
     * Computes the length of the path for a given vector of coordinates
     * @param coords The path for which to compute the length
     * @return The length of the given path 
     */
    static protected float pathLength (Vector coords) {
	float len = 0.0f;
	if (coords.size() > 1) {
	    for(int i=0; i<(coords.size()-1); i++) {
		len = len + (float)((Point2D)coords.elementAt(i)).distance((Point2D)coords.elementAt(i+1));
	    }
	}
	return len;
    }

    /**
     * Normalizes the given vector ie. maintains the vectors direction
     * but gives it unit length
     * @param p Vector (vector in the physics sense, actually a point) to be normalized
     */
    static protected void normalizeVector(Point2D p) {
	float len = (float)p.distance(0.0f, 0.0f);
	if (len != 0.0f) {
	    p.setLocation((float)p.getX()/len,(float)p.getY()/len);
	}
	else {
	    p.setLocation(1.0f/(float)Point2D.distance(0.0f,0.0f,1.0f,1.0f),1.0f/(float)Point2D.distance(0.0f,0.0f,1.0f,1.0f));
	}
    }
}

