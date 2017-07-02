/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
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
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Benjamin B. Bederson
 */
public class ZCoordList extends ZVisualComponent implements ZStroke, ZPenColor, Serializable {
    /**
     * Default value specifying if these coordinates are closed
     * (last point is always the same as the first point in the path).
     */
    static public final boolean     DEFAULT_CLOSED = false;

    /**
     * Default pen width.
     */
    static public final double      DEFAULT_PEN_WIDTH = 1.0;

    /**
     * Default absolute pen width.
     */
    static public final boolean     DEFAULT_ABS_PEN_WIDTH = false;

    /**
     * Default pen color.
     */
    static public final Color       DEFAULT_PEN_COLOR = Color.black;

    /**
     * Current pen color.
     */
    protected Color                 penColor  = DEFAULT_PEN_COLOR;

    /**
     * Current stroke.
     */
    protected transient Stroke      stroke = new BasicStroke((float)DEFAULT_PEN_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    /**
     * Current pen width.
     */
    protected double                penWidth  = DEFAULT_PEN_WIDTH;

    /**
     * Current absolute pen width.
     */
    protected boolean               absPenWidth = DEFAULT_ABS_PEN_WIDTH;

    /**
     * True if coordinates are closed.
     */
    protected boolean               closed = DEFAULT_CLOSED;

    /**
     * True if coordinate list is empty.
     */
    protected boolean               empty = true;

    /**
     * GeneralPath created from coordinate list.
     */
    protected transient GeneralPath path = null;

    /**
     * An x point.
     */
    protected double[]              xp = null;

    /**
     * A y point.
     */
    protected double[]              yp = null;

    /**
     * Current number of points in array.
     */
    protected int                   np = 0;

    /**
     * Temporary ZBounds variable.
     */
    protected ZBounds               tmpBounds = new ZBounds();
    
    /**
     * Constructs a new ZCoordList with no points.
     */
    public ZCoordList() {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new double[2];
	yp = new double[2];
	np = 0;
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZCoordList(Point2D pt) {
	this(pt.getX(), pt.getY());
    }

    /**
     * Constructs a new ZCoordList with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZCoordList(Point2D pt1, Point2D pt2) {
	this(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
    }

    /**
     * Constructs a new ZCoordList with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZCoordList(double x, double y) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new double[2];
	yp = new double[2];
	np = 0;

	add(x, y);
    }

    /**
     * Constructs a new ZCoordList with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZCoordList(double x1, double y1, double x2, double y2) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	xp = new double[2];
	yp = new double[2];
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
    public ZCoordList(double[] xp, double[] yp) {
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xp.length);
	setCoords(xp, yp);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZCoordList newCoordList = (ZCoordList)super.duplicateObject();

	newCoordList.path = (GeneralPath)path.clone();
	newCoordList.xp = (double[])xp.clone();
	newCoordList.yp = (double[])yp.clone();

	return newCoordList;
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
     * @return true if the coodinate list is closed, false otherwise.
     */
    public boolean isClosed() {
	return closed;
    }

    /**
     * Specify that this coordinate list is closed.  A closed coordinate list means
     * that the last point is always the same as the first point in the path that
     * is used for painting.  The actual coordinates are not affected by this.
     * @param closed true if the coodinate list is closed, false otherwise.
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
     * is thick, the bounds of the polyline will grow.  If the pen width is absolute
     * (independent of magnification), then this returns 0.
     * @return the pen width.
     * @see #getAbsPenWidth
     */
    public double getPenWidth() {
	if (absPenWidth) {
	    return 0.0d;
	} else {
	    return penWidth;
	}
    }

    /**
     * Set the width of the pen used to draw the line around the edge of this polyline.
     * The pen is drawn centered around the polyline vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.
     * @param width the pen width.
     */
    public void setPenWidth(double width) {
	penWidth = width;
	absPenWidth = false;
	setVolatileBounds(false);
	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	reshape();
    }

    /**
     * Set the absolute width of the pen used to draw the line around the edge of this polyline.
     * The pen is drawn centered around the polyline vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.
     * @param width the pen width.
     */
    public void setAbsPenWidth(double width) {
	penWidth = width;
	absPenWidth = true;
	setVolatileBounds(true);
	reshape();
    }

    /**
     * Get the absolute width of the pen used to draw the line around the edge of this polyline.
     * The pen is drawn centered around the polyline vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.  If the pen width is not absolute
     * (dependent on magnification), then this returns 0.
     * @return the pen width.
     * @see #getPenWidth
     */
    public double getAbsPenWidth() {
	if (absPenWidth) {
	    return penWidth;
	} else {
	    return 0.0d;
	}
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
	reshape();
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
	add(pt.getX(), pt.getY());
    }

    /**
     * Add a point to the end of this coordinate list.
     * @param x,y The new point
     */    
    public void add(double x, double y) {
	ensureSpace(np + 1);    // Make sure there is enough room in the point array

	xp[np] = x;
	yp[np] = y;
	np++;

				// Update the path that is used for drawing the coordinate list
	if (closed) {
	    updatePath();
	} else {
	    if (empty) {
		path.moveTo((float)x, (float)y);
	    } else {
		path.lineTo((float)x, (float)y);
	    }
	}
	empty = false;

	double p2 = 0.5 * penWidth;
	if (!bounds.contains(x, y)) {
				// Need to expand bounds to accomodate point.
				// Do it incrementally for efficiency instead of calling reshape()
	    bounds.add(x - p2, y - p2);
	    bounds.add(x + p2, y + p2);

	    updateParentBounds();
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
	add(pt.getX(), pt.getY(), index);
    }

    /**
     * Add a point to the specified part of this coordinate list.
     * Specifying an index of 0 puts the point at the beginning of the list.
     * Specifying an index greater than the number of points in the coordinate list puts the point
     * at the end of the list of points.
     * @param x,y The new point
     * @param index The index of the new point.
     */    
    public void add(double x, double y, int index) {
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

	double p2 = 0.5 * penWidth;
	if (!bounds.contains(x, y)) {
				// Need to expand bounds to accomodate point.
				// Do it incrementally for efficiency instead of calling reshape()
	    bounds.add(x - p2, y - p2);
	    bounds.add(x + p2, y + p2);

	    updateParentBounds();
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
	    double nxp[] = new double[newLen];
	    double nyp[] = new double[newLen];
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
    public void setCoords(double[] xp, double[] yp) {
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
	    path.moveTo((float)xp[0], (float)yp[0]);
	    for (int i=1; i<np; i++) {
		path.lineTo((float)xp[i], (float)yp[i]);
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
    public double[] getXCoords() {
	trimToSize();
	return xp;
    }

    /**
     * Get an array of the Y coordinates of the points in this coordinate list.
     * These are the original coordinates of this list, and must not be
     * modified by the caller.
     * @return Array of Y coordinates of points.
     */
    public double[] getYCoords() {
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
	double[] newXP = new double[np];
	double[] newYP = new double[np];
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
	double[] coords = new double[6];
	double xmin, ymin, xmax, ymax;

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
	    double p2;
	    if (absPenWidth) {
		ZRenderContext rc = getRoot().getCurrentRenderContext();
		double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
		p2 = 0.5 * penWidth / mag;
	    } else {
		p2 = 0.5 * penWidth;
	    }
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
	if (absPenWidth != DEFAULT_ABS_PEN_WIDTH) {
	    out.writeState("boolean", "absPenWidth", absPenWidth);
	}
	if (getPenWidth() != DEFAULT_PEN_WIDTH) {
	    out.writeState("double", "penWidth", getPenWidth());
	}
	if ((penColor != null) && (penColor != DEFAULT_PEN_COLOR)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}

	PathIterator i = path.getPathIterator(null);
	double[] pathcoords = new double[6];
	Vector coords = new Vector();

	for (; !i.isDone(); i.next()) {
	    i.currentSegment(pathcoords);
	    coords.add(new Double(pathcoords[0]));
	    coords.add(new Double(pathcoords[1]));
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
	    if (absPenWidth) {
		setAbsPenWidth(((Double)fieldValue).doubleValue());
	    } else {
		setPenWidth(((Double)fieldValue).doubleValue());
	    }
	} else if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
	} else if (fieldName.compareTo("coords") == 0) {
	    double element;
	    double[] xp;
	    double[] yp;
	    int np;
	    int index = 0;

	    np = ((Vector)fieldValue).size() / 2;
	    xp = new double[np];
	    yp = new double[np];
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		element = ((Double)i.next()).doubleValue();
		xp[index] = element;
		element = ((Double)i.next()).doubleValue();
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
	stroke = new BasicStroke((float)penWidth, cap, join);
	path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xp.length);
	updatePath();
    }
}
