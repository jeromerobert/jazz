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
public class ZPolyline extends ZCoordList {
				// Constants used in set/getArrowHead to define if polyline has arrowheads
    static public final int   ARROW_NONE = 0;               
    static public final int   ARROW_FIRST = 1;
    static public final int   ARROW_LAST = 2;
    static public final int   ARROW_BOTH = 3;

    static public final int   ARROW_CLOSED = 0;
    static public final int   ARROW_OPEN = 1;
    
    static public final Color  DEFAULT_PEN_COLOR = Color.black;
    static public final float  DEFAULT_PEN_WIDTH = 1.0f;

    protected Color       penColor  = DEFAULT_PEN_COLOR;
    protected float       penWidth  = DEFAULT_PEN_WIDTH;

    protected int         cap  = BasicStroke.CAP_BUTT;
    protected int         join = BasicStroke.JOIN_BEVEL;
    protected BasicStroke stroke = new BasicStroke(DEFAULT_PEN_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    protected int         arrowHead = ARROW_NONE;
    protected GeneralPath firstArrowHead = null;
    protected GeneralPath lastArrowHead = null;
    protected Point2D     firstArrowHeadPoint = null;
    protected Point2D     lastArrowHeadPoint = null;
    protected int         arrowHeadType = ARROW_OPEN;
    
    /**
     * Constructs a new ZPolyline with no points.
     */
    public ZPolyline() {
	super();
    }

    /**
     * Constructs a new ZPolyline with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZPolyline(Point2D pt) {
	super(pt);
    }

    /**
     * Constructs a new ZPolyline with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZPolyline(Point2D pt1, Point2D pt2) {
	super(pt1, pt2);
    }

    /**
     * Constructs a new ZPolyline with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZPolyline(float x, float y) {
	super(x, y);
    }

    /**
     * Constructs a new ZPolyline with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZPolyline(float x1, float y1, float x2, float y2) {
	super(x1, y1, x2, y2);
    }

    /**
     * Constructs a new ZPolyline.
     * The xp, yp parameters are stored within this polyline, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZPolyline(float[] xp, float[] yp) {
	super(xp, yp);
    }

    /**
     * Constructs a new ZPolyline that is a duplicate of the reference polyline, i.e., a "copy constructor"
     * @param <code>poly</code> Reference polyline
     */
    public ZPolyline(ZPolyline poly) {
	super(poly);

	penColor = poly.penColor;
	setPenWidth(poly.penWidth);
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
	damage(true);
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
	damage(boundsChanged);
    }   

    /**
       Remove one or both arrowheads from the polyline.
       @param <code>ah</code> ARROW_FIRST, ARROW_LAST or ARROW_BOTH
     */
    protected void removeArrowHead(int ah) {
	if ((ah == ARROW_FIRST) || (ah == ARROW_BOTH)) {
	    if (firstArrowHeadPoint != null) {
		xp[0] = (float)firstArrowHeadPoint.getX();
		yp[0] = (float)firstArrowHeadPoint.getY();
		firstArrowHeadPoint = null;
		firstArrowHead = null;
		setCoords(false, xp, yp);
	    }
	}
	if ((ah == ARROW_LAST)  || (ah == ARROW_BOTH)) {
	    if (lastArrowHeadPoint != null) {
		int last = xp.length-1;
		xp[last] = (float)lastArrowHeadPoint.getX();
		yp[last] = (float)lastArrowHeadPoint.getY();
		lastArrowHeadPoint = null;
		lastArrowHead = null;
		setCoords(false, xp, yp);
	    }
	}
    }
		
    public int getArrowHead() {return arrowHead;}

    /**
       Set arrowheads for this polyline.
       * @param <code>ah</code> ArrowHead Specification, such as ARROW_FIRST, ARROW_LAST, ARROW_BOTH, ARROW_NONE.
     */
    public void setArrowHead(int ah) {
	// arrowHead is what current arrowHeads are, ah is what is desired.
	if ((arrowHead == ARROW_BOTH) && (ah == ARROW_FIRST))
	    removeArrowHead(ARROW_LAST);
	else if ((arrowHead == ARROW_BOTH) && (ah == ARROW_LAST))
	    removeArrowHead(ARROW_FIRST);
	else if ((arrowHead == ARROW_FIRST) && (ah == ARROW_LAST))
	    removeArrowHead(ARROW_FIRST);
	else if ((arrowHead == ARROW_LAST) && (ah == ARROW_FIRST))
	    removeArrowHead(ARROW_LAST);
	else if ((arrowHead != ARROW_NONE) && (ah == ARROW_NONE))
	    removeArrowHead(arrowHead);
	arrowHead = ah;
	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }

    /**
       Set arrowHeads for this polyline to a certain style: open, closed, etc
     * @param <code>ah</code> ArrowHead type, such as ARROW_OPEN OR ARROW_CLOSED.
     */
    public void setArrowHeadType(int aht) {
	arrowHeadType = aht;
	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }

    //****************************************************************************
    //
    // Modify and retrieve the coordinates
    //
    //***************************************************************************

    /**
     * Add a point to the end of this polyline.
     * @param x,y The new point
     */    
    public void add(float x, float y) {
	super.add(x, y);

	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
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
	super.add(x, y, index);

	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	damage(true);
    }

    /**
     * Set the coordinates of this polyline.  The specified points completely replace
     * the previous points in this polyline.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     */
    public void setCoords(float[] xp, float[] yp) {
	setCoords(true, xp, yp);
    }

    /**
     * Set the coordinates of this polyline.  The specified points completely replace
     * the previous points in this polyline.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     * @param updateArrowHeads Updates the internal representation of the arrowheads.
     */
    protected void setCoords(boolean updateArrowHeads, float[] xp, float[] yp) {
	super.setCoords(xp, yp);

	if (updateArrowHeads) {
	    updateArrowHeads();	// Update arrowheads to reflect new polyline state
	}
	damage(true);
    }

    //****************************************************************************
    //
    // Visual component related methods
    //
    //***************************************************************************

    /**
     * Paints this object.
     * @param <code>g2</code> The graphics context to paint into.  
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	
        g2.setStroke(stroke);
	if (penColor != null) {
	    g2.setColor(penColor);
	    g2.draw(path);
	}

	if (arrowHead != ARROW_NONE) {
	    if (firstArrowHead != null) {
		if (arrowHeadType == ARROW_CLOSED)
		    g2.fill(firstArrowHead);
		else
		    g2.draw(firstArrowHead);
	    }
	    if (lastArrowHead != null) {
		if (arrowHeadType == ARROW_CLOSED)
		    g2.fill(lastArrowHead);
		else
		    g2.draw(lastArrowHead);
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
	    picked = ZUtil.rectIntersectsPolyline(rect, xp, yp, penWidth);
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
	} else {
	    if ((arrowHead == ARROW_FIRST) || (arrowHead == ARROW_BOTH)) {
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
		firstArrowHead = computeArrowHead(ARROW_FIRST, p1, p2);
	    }
	    
	    if ((arrowHead == ARROW_LAST) || (arrowHead == ARROW_BOTH)) {
				// If a arrowhead at the end, find the last two points 
		for (; !i.isDone(); i.next()) {
		    i.currentSegment(coords);
		    p1.setLocation(p2);
		    p2.setLocation(coords[0], coords[1]);
		}
		lastArrowHead = computeArrowHead(ARROW_LAST, p1, p2);
	    }
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
    protected GeneralPath computeArrowHead(int ah, Point2D p1, Point2D p2) {
	GeneralPath head;
	Point2D p3 = new Point2D.Float();
	Point2D q1 = new Point2D.Float();
	Point2D q2 = new Point2D.Float();
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
	    q2.setLocation(p3.getX() + (p2.getY() - p3.getY()),
			   p3.getY() - (p2.getX() - p3.getX()));

	    if (arrowHeadType == ARROW_CLOSED) {
		head.lineTo((float)q1.getX(), (float)q1.getY());
		head.lineTo((float)q2.getX(), (float)q2.getY());
	    } else {   // ARROW_OPEN
		head.lineTo((float)q1.getX(), (float)q1.getY());
		head.moveTo((float)p2.getX(), (float)p2.getY());
		head.lineTo((float)q2.getX(), (float)q2.getY());
	    }

	    // Save line endpoint, shorten line to beginning of arrowhead.
	    // Restore line length when arrowhead is removed.
	    if (ah == ARROW_FIRST) {
		firstArrowHeadPoint = new Point2D.Float(xp[0], yp[0]);
		xp[0] = (float)p3.getX();
		yp[0] = (float)p3.getY();
		setCoords(false, xp, yp);
	    } else if (ah == ARROW_LAST) {
		int last = xp.length-1;
		lastArrowHeadPoint = new Point2D.Float(xp[last], yp[last]);
		xp[last] = (float)p3.getX();
		yp[last] = (float)p3.getY();
		setCoords(false, xp, yp);
	    }
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
	    setCoords(true, xp, yp);
	}
    }
}
