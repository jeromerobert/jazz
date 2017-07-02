/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZCoordList</b> is an abstract visual component that stores a sequence
 * of coordinates, and the corresponding general path.  This is intended to
 * be sub-classed for specific objects that use coordinate lists.
 *
 * @author  Benjamin B. Bederson
 */
public class ZCoordList extends ZVisualComponent {
    static public final int     ARRAY_INC = 10;

    static public final boolean DEFAULT_CLOSED = false;

    protected boolean     closed = DEFAULT_CLOSED;
    protected boolean     empty = true;
    protected GeneralPath path = null;
    protected float[]     xp = null;
    protected float[]     yp = null;
    protected int         np = 0;   // Current number of points in array
    
    /**
     * Constructs a new ZCoordList with no points.
     */
    public ZCoordList() {
	this((Point2D)null);
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZCoordList(Point2D pt) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;
	if (pt != null) {
	    add(pt);
	}
    }

    /**
     * Constructs a new ZCoordList with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZCoordList(Point2D pt1, Point2D pt2) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;
	if (pt1 != null) {
	    add(pt1);
	}
	if (pt2 != null) {
	    add(pt2);
	}
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZCoordList(float x, float y) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;

	add(x, y);
    }

    /**
     * Constructs a new ZCoordList with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZCoordList(float x1, float y1, float x2, float y2) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;

	add(x1, y1);
	add(x2, y2);
    }

    /**
     * Constructs a new ZCoordList.
     * The xp, yp parameters are stored within this coordinate list, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZCoordList(float[] xp, float[] yp) {
	setCoords(xp, yp);
    }

    /**
     * Constructs a new ZCoordList that is a duplicate of the reference coordinate list, i.e., a "copy constructor"
     * @param <code>coordList</code> Reference coordinate list
     */
    public ZCoordList(ZCoordList coordList) {
	super(coordList);

	localBounds = (ZBounds)coordList.localBounds.clone();
	empty = coordList.empty;
	path = (GeneralPath)coordList.path.clone();
	xp = (float[])coordList.xp.clone();
	yp = (float[])coordList.yp.clone();
	np = coordList.np;
	closed = coordList.closed;
    }

    /**
     * Duplicates the current ZCoordList by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZCoordList(ZCoordList)
     */
    public Object clone() {
	return new ZCoordList(this);
    }
    
    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************
    
    /**
     * Determine if this coordinate list is closed.  A closed coordinate list means
     * that the last point is always the same as the first point in the path that
     * is used for painting.  The actual coordinates are not affected by this.
     * @return the closed value.
     */
    public boolean isClosed() {
	return closed;
    }

    /**
     * Specify that this coordinate list is closed.  A closed coordinate list means
     * that the last point is always the same as the first point in the path that
     * is used for painting.  The actual coordinates are not affected by this.
     * @param the closed value.
     */
    public void setClosed(boolean closed) {
	this.closed = closed;
	if (!empty) {
	    path.closePath();
	}
	damage();
    }

    //****************************************************************************
    //
    // Modify and retrieve the coordinates
    //
    //***************************************************************************

    /**
     * Add a point to the end of this coordinate list.
     * @param pt The new point
     */    
    public void add(Point2D pt) {
	add((float)pt.getX(), (float)pt.getY());
    }

    /**
     * Add a point to the end of this coordinate list.
     * @param x,y The new point
     */    
    public void add(float x, float y) {
	ensureSpace(np + 1);    // Make sure there is enough room in the point array

	xp[np] = x;
	yp[np] = y;
	np++;

				// Update the path that is used for drawing the coordinate list
	if (closed) {
	    updatePath();
	} else {
	    if (empty) {
		path.moveTo(x, y);
	    } else {
		path.lineTo(x, y);
	    }
	}
	empty = false;

	damage(true);
    }

    /**
     * Add a point to the specified part of this coordinate list.
     * Specifying an index of 0 puts the point at the beginning of the list.
     * Specifying an index greater than the number of points in the coordinate list puts the point
     * at the end of the list of points.
     * @param pt The new point
     * @param index The index of the new point.
     */    
    public void add(Point2D pt, int index) {
	add((float)pt.getX(), (float)pt.getY(), index);
    }

    /**
     * Add a point to the specified part of this coordinate list.
     * Specifying an index of 0 puts the point at the beginning of the list.
     * Specifying an index greater than the number of points in the coordinate list puts the point
     * at the end of the list of points.
     * @param x,y The new point
     * @param index The index of the new point.
     */    
    public void add(float x, float y, int index) {
	int i;

	ensureSpace(np + 1);    // Make sure there is enough room in the point array

				// If asked to put point past end, just put it at the end
	if (index > xp.length) {
	    index = xp.length;
	}

	for (i=np; i>index; i--) {
	    xp[i] = xp[i-1];
	    yp[i] = yp[i-1];
	}
	xp[index] = x;
	yp[index] = y;
	np++;

	updatePath();		// Need to regenerate path from scratch since a point
				// could have been added anywhere in the path
	empty = false;

	damage(true);
    }

    /**
     * Ensure that there is space for at least n points in the data structures
     * that hold the list of points for this coordinate list.
     * @param n The number of points that this coordinate list should be able to hold.
     */
    protected void ensureSpace(int n) {
	if (n > xp.length) {
	    int i;
	    int newLen = Math.max(n, xp.length + ARRAY_INC);
	    float nxp[] = new float[newLen];
	    float nyp[] = new float[newLen];
	    for (i=0; i<np; i++) {
		nxp[i] = xp[i];
		nyp[i] = yp[i];
	    }
	    xp = nxp;
	    yp = nyp;
	}
    }

    /**
     * Set the coordinates of this coordinate list.  The specified points completely replace
     * the previous points in this coordinate list.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     */
    public void setCoords(float[] xp, float[] yp) {
        int i;
	int np = xp.length;

	this.xp = xp;
	this.yp = yp;
	this.np = xp.length;

        path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, np);
	if (np > 0) {
	    updatePath();
	    empty = false;
	} else {
	    empty = true;
	}

	damage(true);
    }
    
    /**
     * Internal method to update the path within the coordinate list.
     */
    protected void updatePath() {
	path.reset();
	if (np > 0) {
	    path.moveTo(xp[0], yp[0]);
	    for (int i=1; i<np; i++) {
		path.lineTo(xp[i], yp[i]);
	    }
	    
	    if (closed) {
		path.closePath();
	    }
	}
    }

    /**
     * Get an array of the X coordinates of the points in this coordinate list.
     * These are the original coordinates of this list, and must not be
     * modified by the caller.
     * @return Array of X coordinates of points.
     */
    public float[] getXCoords() {
	trimToSize();
	return xp;
    }

    /**
     * Get an array of the Y coordinates of the points in this coordinate list.
     * These are the original coordinates of this list, and must not be
     * modified by the caller.
     * @return Array of Y coordinates of points.
     */
    public float[] getYCoords() {
	trimToSize();
	return yp;
    }

    /**
     * Get the number of points in this coordinate list.
     * @return the number of points in this coordinate list
     */
    public int getNumberPoints() {
	return np;
    }

    /**
     * Get the GeneralPath object used by this coordinate list.
     * These are the original coordinates of this list, and must not be
     * modified by the caller.
     * @return GeneralPath object used by this coordinate list.
     */
    public GeneralPath getPath() {
	return path;
    }
    
    /**
     * Trims the capacity of the arrays that store the coordinate list points to
     * the actual number of points.  Normally, the coordinate list arrays can be
     * slightly larger than the number of points in the coordinate list.
     * An application can use this operation to minimize the storage of a
     * coordinate list.
     * @see #getXCoords
     * @see #getYCoords
     * @see #getNumberPoints
     */
    public void trimToSize() {
	float[] newXP = new float[np];
	float[] newYP = new float[np];
	for (int i=0; i<np; i++) {
	    newXP[i] = xp[i];
	    newYP[i] = yp[i];
	}
	xp = newXP;
	yp = newYP;
    }

    /**
     * Notifies this object that it has changed and that it should update 
     * its notion of its bounding box.  Note that this should not be called
     * directly.  Instead, it is called by <code>updateBounds</code> when needed.
     *
     * @see ZNode#getGlobalBounds()
     */
    protected void computeLocalBounds() {
	int i;
	float[] coords = new float[6];

	localBounds.reset();
	for (i=0; i<np; i++) {
	    localBounds.add(xp[i], yp[i]);
	}
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

	if (isClosed() != DEFAULT_CLOSED) {
	    out.writeState("boolean", "closed", isClosed());
	}

	PathIterator i = path.getPathIterator(null);
	double[] pathcoords = new double[6];
	Vector coords = new Vector();

	for (; !i.isDone(); i.next()) {
	    i.currentSegment(pathcoords);
	    coords.add(new Float(pathcoords[0]));
	    coords.add(new Float(pathcoords[1]));
	}
	out.writeState("Vector", "coords", coords);
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

	if (fieldName.compareTo("closed") == 0) {
	    setClosed(((Boolean)fieldValue).booleanValue());
	} else if (fieldName.compareTo("coords") == 0) {
	    float element;
	    float[] xp;
	    float[] yp;
	    int np;
	    int index = 0;

	    np = ((Vector)fieldValue).size() / 2;
	    xp = new float[np];
	    yp = new float[np];
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		element = ((Float)i.next()).floatValue();
		xp[index] = element;
		element = ((Float)i.next()).floatValue();
		yp[index] = element;
		index++;
	    }
	    setCoords(xp, yp);
	}
    }
}
