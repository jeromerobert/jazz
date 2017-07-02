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
 * <b>ZPolyline</b> is a visual component that represents a line
 * with one or more segments.
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
public class ZPolyline extends ZCoordList implements Serializable {
				// Constants used in set/getArrowHead to define if polyline has arrowheads
    static public final int   ARROW_NONE = 0;
    static public final int   ARROW_FIRST = 1;
    static public final int   ARROW_LAST = 2;
    static public final int   ARROW_BOTH = 3;

    static public final int   ARROW_CLOSED = 0;
    static public final int   ARROW_OPEN = 1;

    private int                   arrowHead = ARROW_NONE;
    private transient GeneralPath firstArrowHead = null;
    private transient GeneralPath lastArrowHead = null;
    private transient Point2D     firstArrowHeadPoint = null;
    private transient Point2D     lastArrowHeadPoint = null;
    private int                   arrowHeadType = ARROW_OPEN;

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
    public ZPolyline(double x, double y) {
	super(x, y);
    }

    /**
     * Constructs a new ZPolyline with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZPolyline(double x1, double y1, double x2, double y2) {
	super(x1, y1, x2, y2);
    }

    /**
     * Constructs a new ZPolyline from an array of points.
     * The xp, yp parameters are stored within this polyline, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZPolyline(double[] xp, double[] yp) {
	super(xp, yp);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZPolyline newPoly = (ZPolyline)super.duplicateObject();

	if (firstArrowHead != null) {
	    newPoly.firstArrowHead = (GeneralPath)firstArrowHead.clone();
	}
	if (lastArrowHead != null) {
	    newPoly.lastArrowHead = (GeneralPath)lastArrowHead.clone();
	}
	return newPoly;
    }


    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
       Remove one or both arrowheads from the polyline.
       @param <code>ah</code> ARROW_FIRST, ARROW_LAST or ARROW_BOTH
     */
    protected void removeArrowHead(int ah) {
	if ((ah == ARROW_FIRST) || (ah == ARROW_BOTH)) {
	    if (firstArrowHeadPoint != null) {
		xp[0] = firstArrowHeadPoint.getX();
		yp[0] = firstArrowHeadPoint.getY();
		firstArrowHeadPoint = null;
		firstArrowHead = null;
		setCoords(false, xp, yp);
	    }
	}
	if ((ah == ARROW_LAST)  || (ah == ARROW_BOTH)) {
	    if (lastArrowHeadPoint != null) {
		int last = np - 1;
		xp[last] = lastArrowHeadPoint.getX();
		yp[last] = lastArrowHeadPoint.getY();
		lastArrowHeadPoint = null;
		lastArrowHead = null;
		setCoords(false, xp, yp);
	    }
	}
    }

    /**
     * Get the arrowhead type used by this ZPolyline. ArrowHead is one of ARROW_NONE, ARROW_FIRST, ARROW_LAST, ARROW_BOTH.
     */
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
	reshape();
    }

    /**
       Set arrowHeads for this polyline to a certain style: open, closed, etc
     * @param <code>ah</code> ArrowHead type, such as ARROW_OPEN OR ARROW_CLOSED.
     */
    public void setArrowHeadType(int aht) {
	arrowHeadType = aht;
	updateArrowHeads();	// Update arrowheads to reflect new polyline state
	reshape();
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
    public void add(double x, double y) {
	super.add(x, y);

	if (updateArrowHeads()) {	// Update arrowheads to reflect new polyline state
	    reshape();
	}
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
	super.add(x, y, index);

	if (updateArrowHeads()) {	// Update arrowheads to reflect new polyline state
	    reshape();
	}
    }

    /**
     * Set the coordinates of this polyline.  The specified points completely replace
     * the previous points in this polyline.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     */
    public void setCoords(double[] xp, double[] yp) {
	setCoords(true, xp, yp);
    }

    /**
     * Set the coordinates of this polyline to two points.
     * @param pt1 The first point of the polyline
     * @param pt2 The second first point of the polyline
     */
    public void setCoords(Point2D pt1, Point2D pt2) {
	double[] xp = new double[2];
	double[] yp = new double[2];
	xp[0] = pt1.getX();
	yp[0] = pt1.getY();
	xp[1] = pt2.getX();
	yp[1] = pt2.getY();
	setCoords(true, xp, yp);
    }

    /**
     * Set the coordinates of this polyline.  The specified points completely replace
     * the previous points in this polyline.
     * @param xp An array of the X coordinates of the new points.
     * @param yp An array of the Y coordinates of the new points.
     * @param updateArrowHeads Updates the internal representation of the arrowheads.
     */
    protected void setCoords(boolean updateArrowHeads, double[] xp, double[] yp) {
	super.setCoords(xp, yp);

	if (updateArrowHeads) {
	    if (updateArrowHeads()) {	// Update arrowheads to reflect new polyline state
		reshape();
	    }
	}
    }

    //****************************************************************************
    //
    // Visual component related methods
    //
    //***************************************************************************

    /**
     * Renders this object.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param <code>renderContext</code> The graphics context to paint into.
     */


    public void render(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();

	if (penColor != null) {
	    if (absPenWidth) {
		double pw = penWidth / renderContext.getCompositeMagnification();
		stroke = new BasicStroke((float)pw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	    }
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(path);

	    if (arrowHead != ARROW_NONE) {
		if (firstArrowHead != null) {
		    if (arrowHeadType == ARROW_CLOSED) {
			g2.fill(firstArrowHead);
		    } else {
			g2.draw(firstArrowHead);
		    }
		}
		if (lastArrowHead != null) {
		    if (arrowHeadType == ARROW_CLOSED) {
			g2.fill(lastArrowHead);
		    } else {
			g2.draw(lastArrowHead);
		    }
		}
	    }
	}
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

	if (firstArrowHead != null) {
	    PathIterator iterator = firstArrowHead.getPathIterator(null);
	    for (; !iterator.isDone(); iterator.next()) {
		iterator.currentSegment(coords);
		if (coords[0] < xmin) xmin = coords[0];
		if (coords[1] < ymin) ymin = coords[1];
		if (coords[0] > xmax) xmax = coords[0];
		if (coords[1] > ymax) ymax = coords[1];
	    }
	}
	if (lastArrowHead != null) {
	    PathIterator iterator = lastArrowHead.getPathIterator(null);
	    for (; !iterator.isDone(); iterator.next()) {
		iterator.currentSegment(coords);
		if (coords[0] < xmin) xmin = coords[0];
		if (coords[1] < ymin) ymin = coords[1];
		if (coords[0] > xmax) xmax = coords[0];
		if (coords[1] > ymax) ymax = coords[1];
	    }
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

    /**
     * Returns true if the specified rectangle is on the polyline.
     * @param rect Pick rectangle of object coordinates.
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return True if rectangle overlaps object.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	boolean picked = false;

	if (pickBounds(rect)) {
	    double p;
	    if (absPenWidth) {
		ZRenderContext rc = getRoot().getCurrentRenderContext();
		double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
		p = penWidth / mag;
	    } else {
		p = penWidth;
	    }
	    picked = ZUtil.rectIntersectsPolyline(rect, xp, yp, p);
	}

	return picked;
    }

    /**
     * Updates the internal representation of the arrowheads to reflect the
     * current state of the polyline.  This should be called whenever the
     * polyline has changed.
     * @return True if arrowheads were actually computed resulting in possible bounds changes
     */
    protected boolean updateArrowHeads() {
	GeneralPath head = null;
	PathIterator i = path.getPathIterator(null);
	double[] coords = new double[6];
	Point2D p1 = new Point2D.Double();
	Point2D p2 = new Point2D.Double();
	boolean updated = false;

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
		updated = true;
	    }

	    if ((arrowHead == ARROW_LAST) || (arrowHead == ARROW_BOTH)) {
				// If a arrowhead at the end, find the last two points
		for (; !i.isDone(); i.next()) {
		    i.currentSegment(coords);
		    p1.setLocation(p2);
		    p2.setLocation(coords[0], coords[1]);
		}
		lastArrowHead = computeArrowHead(ARROW_LAST, p1, p2);
		updated = true;
	    }
	}

	return updated;
    }

    /**
     * Calculate the points used to represent the arrowhead.  We use a simple algorithm
     * that just starts at p2, backs up to p1 a bit (as represented with p3),
     * and goes to either side by rotating (p3-p2) +/- 90 degrees.
     */
    protected GeneralPath computeArrowHead(int ah, Point2D p1, Point2D p2) {
	GeneralPath head;
	Point2D p3 = new Point2D.Double();
	Point2D q1 = new Point2D.Double();
	Point2D q2 = new Point2D.Double();
	double arrowWidth = penWidth * 2.0;

				// BBB 12/98: There is a bug with Sun's JDK1.2 where if we create a Shape
				// of zero area (due to the two points being equal, and then add
				// them to another shape (as we do when we compute the local bounds),
				// then the JDK will hang at run time.  And so, we check to make sure
				// the points aren't equal.
	if (p1.equals(p2)) {
	    head = null;
	} else {
	    head = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	    p3.setLocation(p2.getX() + arrowWidth * (p1.getX() - p2.getX()) / p1.distance(p2),
			   p2.getY() + arrowWidth * (p1.getY() - p2.getY()) / p1.distance(p2));
	    q1.setLocation(p3.getX() - (p2.getY() - p3.getY()),
			   p3.getY() + (p2.getX() - p3.getX()));
	    q2.setLocation(p3.getX() + (p2.getY() - p3.getY()),
			   p3.getY() - (p2.getX() - p3.getX()));

	    if (arrowHeadType == ARROW_CLOSED) {
		head.moveTo((float)p2.getX(), (float)p2.getY());
		head.lineTo((float)q1.getX(), (float)q1.getY());
		head.lineTo((float)q2.getX(), (float)q2.getY());
	    } else {   // ARROW_OPEN
		head.moveTo((float)p3.getX(), (float)p3.getY());
		head.lineTo((float)p2.getX(), (float)p2.getY());
		head.moveTo((float)q1.getX(), (float)q1.getY());
		head.lineTo((float)p2.getX(), (float)p2.getY());
		head.lineTo((float)q2.getX(), (float)q2.getY());
	    }

	    // Save line endpoint, shorten line to beginning of arrowhead.
	    // Restore line length when arrowhead is removed.
	    if (ah == ARROW_FIRST) {
		firstArrowHeadPoint = new Point2D.Double(xp[0], yp[0]);
		xp[0] = p3.getX();
		yp[0] = p3.getY();
		setCoords(false, xp, yp);
	    } else if (ah == ARROW_LAST) {
		int last = np - 1;
		lastArrowHeadPoint = new Point2D.Double(xp[last], yp[last]);
		xp[last] = p3.getX();
		yp[last] = p3.getY();
		setCoords(false, xp, yp);
	    }
	}

	return head;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

				// write Point2D firstArrowHeadPoint
	if (firstArrowHeadPoint == null) {
	    out.writeBoolean(false);
	} else {
	    out.writeBoolean(true);
	    out.writeDouble(firstArrowHeadPoint.getX());
	    out.writeDouble(firstArrowHeadPoint.getY());
	}
				// write Point2D lastArrowHeadPoint
	if (lastArrowHeadPoint == null) {
	    out.writeBoolean(false);
	} else {
	    out.writeBoolean(true);
	    out.writeDouble(lastArrowHeadPoint.getX());
	    out.writeDouble(lastArrowHeadPoint.getY());
	}
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	double x, y;

				// read Point2D firstArrowHeadPoint
	if (in.readBoolean()) {
	    x = in.readDouble();
	    y = in.readDouble();
	    firstArrowHeadPoint = new Point2D.Double(x, y);
	}

				// read Point2D lastArrowHeadPoint
	if (in.readBoolean()) {
	    x = in.readDouble();
	    y = in.readDouble();
	    lastArrowHeadPoint = new Point2D.Double(x, y);
	}


	if ((firstArrowHeadPoint != null) || (lastArrowHeadPoint != null)) {
	    updateArrowHeads();
	}
    }
}

