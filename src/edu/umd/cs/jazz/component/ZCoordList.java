/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.io.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZCoordList</b> is an abstract visual component that stores a sequence
 * of coordinates, and the corresponding general path.  This is intended to
 * be sub-classed for specific objects that use coordinate lists.
 *
 * @author  Benjamin B. Bederson
 */
public class ZCoordList extends ZVisualComponent implements ZStroke, ZPenColor, Serializable {
    static public final boolean     DEFAULT_CLOSED = false;
    static public final float       DEFAULT_PEN_WIDTH = 1.0f;
    static public final Color       DEFAULT_PEN_COLOR = Color.black;

    protected Color                 penColor  = DEFAULT_PEN_COLOR;
    protected transient Stroke      stroke = new BasicStroke(DEFAULT_PEN_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    protected float                 penWidth  = DEFAULT_PEN_WIDTH;
    protected boolean               closed = DEFAULT_CLOSED;
    protected boolean               empty = true;
    protected transient GeneralPath path = null;
    protected float[]               xp = null;
    protected float[]               yp = null;
    protected int                   np = 0;   // Current number of points in array
    protected ZBounds               tmpBounds = new ZBounds();
    
    /**
     * Constructs a new ZCoordList with no points.
     */
    public ZCoordList() {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[2];
	yp = new float[2];
	np = 0;
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZCoordList(Point2D pt) {
	this((float)pt.getX(), (float)pt.getY());
    }

    /**
     * Constructs a new ZCoordList with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZCoordList(Point2D pt1, Point2D pt2) {
	this((float)pt1.getX(), (float)pt1.getY(), (float)pt2.getX(), (float)pt2.getY());
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZCoordList(float x, float y) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new float[2];
	yp = new float[2];
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
	xp = new float[2];
	yp = new float[2];
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
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xp.length);
	setCoords(xp, yp);
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refCoordList The reference visual component to copy
     */
    public void duplicateObject(ZCoordList refCoordList) {
	super.duplicateObject(refCoordList);

	empty = refCoordList.empty;
	path = (GeneralPath)refCoordList.path.clone();
	xp = (float[])refCoordList.xp.clone();
	yp = (float[])refCoordList.yp.clone();
	np = refCoordList.np;
	closed = refCoordList.closed;
	penColor = refCoordList.penColor;
	setPenWidth(refCoordList.penWidth);
    }

    /**
     * Duplicates the current object by using the copy constructor.
     * The portion of the reference object that is duplicated is that necessary to reuse the object
     * in a new place within the scenegraph, but the new object is not inserted into any scenegraph.
     * The object must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     *
     * @return A copy of this visual component.
     * @see #updateObjectReferences 
     */
    public Object clone() {
	ZCoordList copy;

	objRefTable.reset();
	copy = new ZCoordList();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
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
	repaint();
    }

    /**
     * Get the width of the pen used to draw the line around the edge of this polyline.
     * The pen is drawn centered around the polyline vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.
     * @return the pen width.
     */
    public float getPenWidth() {
	return penWidth;
    }

    /**
     * Set the width of the pen used to draw the line around the edge of this polyline.
     * The pen is drawn centered around the polyline vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.
     * @param width the pen width.
     */
    public void setPenWidth(float width) {
	penWidth = width;
	stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	reshape();
    }

    /**
     * Get the stroke used to draw the visual component.
     * @return the stroke.
     */
    public Stroke getStroke() {
	return stroke;
    }

    /**
     * Set the stroke used to draw the visual component.
     * @param stroke the stroke.
     */
    public void setStroke(Stroke stroke) {
	this.stroke = stroke;
    }

    /**
     * Get the pen color of this polyline.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color of this polyline.
     * @param color the pen color, or null if none.
     */
    public void setPenColor(Color color) {
	boolean boundsChanged = false;

				// If turned pen color on or off, then need to recompute bounds
	if (((penColor == null) && (color != null)) ||
	    ((penColor != null) && (color == null))) {
	    boundsChanged = true;
	}
	penColor = color;

	if (boundsChanged) {
	    reshape();
	} else {
	    repaint();
	}
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

	float p2 = 0.5f * penWidth;
	if (!bounds.contains(x, y)) {
				// Need to expand bounds to accomodate point.
				// Do it incrementally for efficiency instead of calling reshape()
	    bounds.add(x - p2, y - p2);
	    bounds.add(x + p2, y + p2);

	    boundsUpdated();
	}
				// Only refresh the portion of the shape that has changed - which is just
				// the area between the current and previous point
	if (np >= 2) {
	    tmpBounds.setRect(Math.min(xp[np-2], xp[np-1]) - p2, Math.min(yp[np-2], yp[np-1]) - p2, 
			      Math.abs(xp[np-1] - xp[np-2]) + penWidth, Math.abs(yp[np-1] - yp[np-2]) + penWidth);
	    repaint(tmpBounds);
	}
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

	float p2 = 0.5f * penWidth;
	if (!bounds.contains(x, y)) {
				// Need to expand bounds to accomodate point.
				// Do it incrementally for efficiency instead of calling reshape()
	    bounds.add(x - p2, y - p2);
	    bounds.add(x + p2, y + p2);

	    boundsUpdated();
	}
				// Only refresh the portion of the shape that has changed - which is just
				// the area between the current and previous point
	if (np >= 2) {
	    tmpBounds.setRect(Math.min(xp[np-2], xp[np-1]) - p2, Math.min(yp[np-2], yp[np-1]) - p2, 
			      Math.abs(xp[np-1] - xp[np-2]) + penWidth, Math.abs(yp[np-1] - yp[np-2]) + penWidth);
	    repaint(tmpBounds);
	}
    }

    /**
     * Ensure that there is space for at least n points in the data structures
     * that hold the list of points for this coordinate list.
     * @param n The number of points that this coordinate list should be able to hold.
     */
    protected void ensureSpace(int n) {
	if (n > xp.length) {
	    int i;
	    int newLen = Math.max(n, (xp.length == 0) ? 1 : (2 * xp.length));
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

        path.reset();
	if (np > 0) {
	    updatePath();
	    empty = false;
	} else {
	    empty = true;
	}

	reshape();
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
     */
    protected void computeBounds() {
	int i;
	float[] coords = new float[6];
	float xmin, ymin, xmax, ymax;

	bounds.reset();
	if (np == 0) {
	    return;
	}

	xmin = xp[0];
	ymin = yp[0];
	xmax = xp[0];
	ymax = yp[0];
	for (i=1; i<np; i++) {
	    if (xp[i] < xmin) xmin = xp[i];
	    if (yp[i] < ymin) ymin = yp[i];
	    if (xp[i] > xmax) xmax = xp[i];
	    if (yp[i] > ymax) ymax = yp[i];
	}

				// Expand the bounds to accomodate the pen width
	if (penColor != null) {
	    float p2;
	    p2 = 0.5f * penWidth;
	    xmin -= p2;
	    ymin -= p2;
	    xmax += p2;
	    ymax += p2;
	}

	bounds.setRect(xmin, ymin, xmax - xmin, ymax - ymin);
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
	if (getPenWidth() != DEFAULT_PEN_WIDTH) {
	    out.writeState("float", "penWidth", getPenWidth());
	}
	if ((penColor != null) && (penColor != DEFAULT_PEN_COLOR)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
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
	} else if (fieldName.compareTo("penWidth") == 0) {
	    setPenWidth(((Float)fieldValue).floatValue());
	} else if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
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

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();

				// write stroke
	out.writeInt(((BasicStroke)stroke).getEndCap());
	out.writeInt(((BasicStroke)stroke).getLineJoin());
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

				// read stroke
	int cap, join;
	cap = in.readInt();
	join = in.readInt();
	stroke = new BasicStroke(penWidth, cap, join);
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xp.length);
	updatePath();
    }
}
