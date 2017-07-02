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
 * <b>ZPolyline</b> is a visual component that represents a line
 * with one or more segments.
 *
 * @author  Benjamin B. Bederson
 */
public class ZPolyline extends ZVisualComponent {
				// BBB: The arrowheads are not really finished.  Really, the point of the
				// arrowhead under the arrow should be moved to where the arrow starts.
				// Now, the arrowhead and line overlap which results in a bit of ugliness.
				// To do this properly, the original points need to be stored so that
				// when the arrowheads are removed, the original points can be restored...

				// Constants used in set/getArrowHead to define if polyline has arrowheads
    static public final int   ARROW_NONE = 0;               
    static public final int   ARROW_FIRST = 1;
    static public final int   ARROW_LAST = 2;
    static public final int   ARROW_BOTH = 3;

    static public final Color  DEFAULT_PEN_COLOR = Color.black;
    static public final float  DEFAULT_PEN_WIDTH = 1.0f;

    static public final int    ARRAY_INC = 10;

    protected Color       penColor  = DEFAULT_PEN_COLOR;
    protected float       penWidth  = DEFAULT_PEN_WIDTH;
    
    protected boolean     empty = true;
    protected GeneralPath path = null;
    protected float[]     xp = null;
    protected float[]     yp = null;
    protected int         np = 0;   // Current number of points in array
    protected int         cap  = BasicStroke.CAP_BUTT;
    protected int         join = BasicStroke.JOIN_BEVEL;
    protected int         arrowHead = ARROW_NONE;
    protected GeneralPath firstArrowHead = null;
    protected GeneralPath lastArrowHead = null;
    
    /**
     * Constructs a new Polyline with no points.
     */
    public ZPolyline() {
	this((Point2D)null);
    }

    /**
     * Constructs a new Polyline with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZPolyline(Point2D pt) {
	path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;
	if (pt != null) {
	    add(pt);
	}
    }

    /**
     * Constructs a new Polyline with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZPolyline(Point2D pt1, Point2D pt2) {
	path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
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
     * Constructs a new Polyline with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZPolyline(float x, float y) {
	path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;

	add(x, y);
    }

    /**
     * Constructs a new Polyline with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZPolyline(float x1, float y1, float x2, float y2) {
	path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	xp = new float[ARRAY_INC];
	yp = new float[ARRAY_INC];
	np = 0;

	add(x1, y1);
	add(x2, y2);
    }

    /**
     * Constructs a new Polyline.
     * The xp, yp parameters are stored within this polyline, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZPolyline(float[] xp, float[] yp) {
	this.xp = xp;
	this.yp = yp;
	this.np = xp.length;
	setCoords(xp, yp);
    }

    /**
     * Constructs a new ZPolyline that is a duplicate of the reference polyline, i.e., a "copy constructor"
     * @param <code>poly</code> Reference polyline
     */
    public ZPolyline(ZPolyline poly) {
	super(poly);

	localBounds = (ZBounds)poly.localBounds.clone();
	penColor = poly.penColor;
	penWidth = poly.penWidth;
	empty = poly.empty;
	path = (GeneralPath)poly.path.clone();
	xp = (float[])poly.xp.clone();
	yp = (float[])poly.yp.clone();
	np = poly.np;
	cap = poly.cap;
	join = poly.join;
	arrowHead = poly.arrowHead;
	if (poly.firstArrowHead != null) {
	    firstArrowHead = (GeneralPath)poly.firstArrowHead.clone();
	}
	if (poly.lastArrowHead != null) {
	    lastArrowHead = (GeneralPath)poly.lastArrowHead.clone();
	}
    }

    /**
     * Duplicates the current ZPolyline by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZPolyline(ZPolyline)
     */
    public Object clone() {
	return new ZPolyline(this);
    }
    
    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************
    
    public float getPenWidth() {return penWidth;}
    public void setPenWidth(float width) {
	penWidth = width;
	damage(true);
    }

    
    public Color getPenColor() {return penColor;}
    public void setPenColor(Color color) {
	boolean boundsChanged = false;

				// If turned pen color on or off, then need to recompute bounds
	if (((penColor == null) && (color != null)) ||
	    ((penColor != null) && (color == null))) {
	    boundsChanged = true;
	}

	penColor = color;
	damage(boundsChanged);
    }   
    
    public int getArrowHead() {return arrowHead;}
    public void setArrowHead(int ah) {
	arrowHead = ah;
	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }
    
    //****************************************************************************
    //
    //			
    //
    //***************************************************************************

    /**
     * Add a point to the end of this polyline.
     * @param pt The new point
     */    
    public void add(Point2D pt) {
	add((float)pt.getX(), (float)pt.getY());
    }

    /**
     * Add a point to the end of this polyline.
     * @param x,y The new point
     */    
    public void add(float x, float y) {
	if (empty) {
	    empty = false;
	    path.moveTo(x, y);
	} else {
	    path.lineTo(x, y);
	}
				// Make sure there is enough room in the point array
	if (np >= xp.length-1) {
	    int i;
	    float nxp[] = new float[xp.length + ARRAY_INC];
	    float nyp[] = new float[xp.length + ARRAY_INC];
	    for (i=0; i<np; i++) {
		nxp[i] = xp[i];
		nyp[i] = yp[i];
	    }
	    xp = nxp;
	    yp = nyp;
	}
	xp[np] = x;
	yp[np] = y;
	np++;

	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }

    /**
     * Set the coordinates of this polyline.  THe specified points completely replace
     * the previous points in this polyline.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     */
    public void setCoords(float[] xp, float[] yp) {
        int i;
	int np = xp.length;

        path = new GeneralPath(GeneralPath.WIND_NON_ZERO, np);

	if (np > 0) {
	    path.moveTo(xp[0], yp[0]);
	    for (i=1; i<np; i++) {
		path.lineTo(xp[i], yp[i]);
	    }
	    empty = false;
	} else {
	    empty = true;
	}
	this.xp = xp;
	this.yp = yp;
	this.np = xp.length;
	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }

    /**
     * Get an array of the X coordinates of the points in this polyline.
     * Note that the array may be larger than the actual number of points
     * in this polyline.  See {@link #getNumberPoints} to get the actual
     * number of points in the polyline.
     * @return Array of X coordinates of points.
     */
    public float[] getXCoords() {
	return xp;
    }

    /**
     * Get an array of the Y coordinates of the points in this polyline.
     * Note that the array may be larger than the actual number of points
     * in this polyline.  See {@link #getNumberPoints} to get the actual
     * number of points in the polyline.
     * @return Array of Y coordinates of points.
     */
    public float[] getYCoords() {
	return yp;
    }

    /**
     * Get the number of points in this polyline.
     * @return the number of points in this polyline
     */
    public int getNumberPoints() {
	return np;
    }

    /**
     * Trims the capacity of the arrays that store the polyline points to
     * the actual number of points.  Normally, the polyline arrays can be
     * slightly larger than the number of points in the polyline.
     * An application can use this operation to minimize the storage of a
     * polyline.
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
	yp = newXP;
    }

    /**
     * Paints this object.
     * @param <code>g2</code> The graphics context to paint into.  */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	
        g2.setStroke(new BasicStroke(penWidth, cap, join));
        g2.setColor(penColor);
        g2.draw(path);

	if (arrowHead != ARROW_NONE) {
	    if (firstArrowHead != null) {
		g2.fill(firstArrowHead);
	    } else if (lastArrowHead != null) {
		g2.fill(lastArrowHead);
	    }
	}
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
	if (firstArrowHead != null) {
	    PathIterator iterator = firstArrowHead.getPathIterator(null);
	    for (; !iterator.isDone(); iterator.next()) {
		iterator.currentSegment(coords);
		localBounds.add(coords[0], coords[1]);
	    }
	}
	if (lastArrowHead != null) {
	    PathIterator iterator = lastArrowHead.getPathIterator(null);
	    for (; !iterator.isDone(); iterator.next()) {
		iterator.currentSegment(coords);
		localBounds.add(coords[0], coords[1]);
	    }
	}

				// Expand the bounds to accomodate the pen width
	if (penColor != null) {
	    float p, p2;
	    p = penWidth;
	    p2 = 0.5f * p;
	    localBounds.setRect(localBounds.x - p2, localBounds.y - p2, localBounds.width + p, localBounds.height + p);
	}
    }

    /**
     * Returns true if the specified rectangle is on the polyline.
     * @param <code>rect</code> Pick rectangle of object coordinates.
     * @return True if rectangle overlaps object.
     */
    public boolean pick(Rectangle2D rect) {
	boolean picked = false;

	if (pickBounds(rect)) {
	    float width = (float)((0.5f * penWidth) + (0.5f * ((0.5f * rect.getWidth()) + (0.5f * rect.getHeight()))));
	    float px = (float)(rect.getX() + (0.5f * rect.getWidth()));
	    float py = (float)(rect.getY() + (0.5f * rect.getHeight()));
	    float squareDist;
	    float minSquareDist;
	    int i;
	    int np = xp.length;

	    if (np > 0) {
		if (np == 1) {
		    minSquareDist = (float)Line2D.ptSegDistSq(xp[0], yp[0], xp[0], yp[0], px, py);
		} else {
		    minSquareDist = (float)Line2D.ptSegDistSq(xp[0], yp[0], xp[1], yp[1], px, py);
		}
		for (i=1; i<np-1; i++) {
		    squareDist = (float)Line2D.ptSegDistSq(xp[i], yp[i], xp[i+1], yp[i+1], px, py);
		    if (squareDist < minSquareDist) {
			minSquareDist = squareDist;
		    }
		}
		if (minSquareDist <= (width*width)) {
		    picked = true;
		}
	    }
	}

	return picked;
    }

    /**
     * Updates the internal representation of the arrowheads to reflect the
     * current state of the polyline.  This should be called whenever the
     * polyline has changed.
     */
    protected void updateArrowHeads() {
	GeneralPath head = null;
	PathIterator i = path.getPathIterator(null);
	float[] coords = new float[6];
	Point2D p1 = new Point2D.Float();
	Point2D p2 = new Point2D.Float();

				// Arrowheads are represented by keeping a shape for each active arrow head.
	if (arrowHead == ARROW_NONE) {
				// If there are no arrowheads, then remove any current ones
	    firstArrowHead = null;
	    lastArrowHead = null;
	} else if ((arrowHead == ARROW_FIRST) || (arrowHead == ARROW_BOTH)) {
				// If a arrowhead at the front, find the first two points 
	    int cnt = 0;
	    for (; !i.isDone(); i.next()) {
		i.currentSegment(coords);
		p2.setLocation(p1);
		p1.setLocation(coords[0], coords[1]);
		cnt++;
		if (cnt == 2) {
		    break;
		}
	    }
	    firstArrowHead = computeArrowHead(p1, p2);
	} else if ((arrowHead == ARROW_LAST) || (arrowHead == ARROW_BOTH)) {
				// If a arrowhead at the end, find the last two points 
	    for (; !i.isDone(); i.next()) {
		i.currentSegment(coords);
		p1.setLocation(p2);
		p2.setLocation(coords[0], coords[1]);
	    }
	    lastArrowHead = computeArrowHead(p1, p2);
	}
    }

    /**
     * Calculate the points used to represent the arrowhead.  We use a simple algorithm
     * that just starts at p2, backs up to p1 a bit (as represented with p3),
     * and goes to either side by rotating (p3-p2) +/- 90 degrees.
     *
     * BBB 12/98: There is a bug with Sun's JDK1.2 where if we create a Shape
     * of zero area (due to the two points being equal, and then add
     * them to another shape (as we do when we compute the local bounds),
     * then the JDK will hang at run time.  And so, we check to make sure
     * the points aren't equal.
     */
    protected GeneralPath computeArrowHead(Point2D p1, Point2D p2) {
	GeneralPath head;
	Point2D p3 = new Point2D.Float();
	Point2D q1 = new Point2D.Float();
	float arrowWidth = penWidth * 2.0f;

	if (p1.equals(p2)) {
	    head = null;
	} else {
	    head = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	
	    head.moveTo((float)p2.getX(), (float)p2.getY());
	    p3.setLocation(p2.getX() + arrowWidth * (p1.getX() - p2.getX()) / p1.distance(p2),
			   p2.getY() + arrowWidth * (p1.getY() - p2.getY()) / p1.distance(p2));
	    q1.setLocation(p3.getX() - (p2.getY() - p3.getY()),
			   p3.getY() + (p2.getX() - p3.getX()));
	    head.lineTo((float)q1.getX(), (float)q1.getY());
	    q1.setLocation(p3.getX() + (p2.getY() - p3.getY()),
			   p3.getY() - (p2.getX() - p3.getX()));
	    head.lineTo((float)q1.getX(), (float)q1.getY());
	}

	return head;
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

	if ((penColor != null) && (penColor != DEFAULT_PEN_COLOR)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}
	if (getPenWidth() != DEFAULT_PEN_WIDTH) {
	    out.writeState("float", "penWidth", getPenWidth());
	}

	PathIterator i = path.getPathIterator(null);
	float[] pathcoords = new float[6];
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

	if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
	} else if (fieldName.compareTo("penWidth") == 0) {
	    setPenWidth(((Float)fieldValue).floatValue());
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
