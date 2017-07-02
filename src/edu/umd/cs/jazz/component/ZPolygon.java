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
 * <b>ZPolygon</b> is a visual component that represents a line
 * with one or more segments.
 *
 * @author  Benjamin B. Bederson
 */
public class ZPolygon extends ZCoordList implements ZFillColor, Serializable {
    static public final Color  DEFAULT_FILL_COLOR = Color.white;

    protected Color       fillColor  = DEFAULT_FILL_COLOR;

    /**
     * Constructs a new ZPolygon with no points.
     */
    public ZPolygon() {
	super();
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon with a single point.
     * @param <code>pt</code> Initial point
     */
    public ZPolygon(Point2D pt) {
	super(pt);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon with two points.
     * @param <code>pt1</code> First point
     * @param <code>pt2</code> Second point
     */
    public ZPolygon(Point2D pt1, Point2D pt2) {
	super(pt1, pt2);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon with a single point.
     * @param <code>x,y</code> Initial point
     */
    public ZPolygon(float x, float y) {
	super(x, y);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZPolygon(float x1, float y1, float x2, float y2) {
	super(x1, y1, x2, y2);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon.
     * The xp, yp parameters are stored within this polygon, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZPolygon(float[] xp, float[] yp) {
	super(xp, yp);
	setClosed(true);
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refPoly The reference visual component to copy
     */
    public void duplicateObject(ZPolygon refPoly) {
	super.duplicateObject(refPoly);

	fillColor = refPoly.fillColor;
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
	ZPolygon copy;

	objRefTable.reset();
	copy = new ZPolygon();
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
     * Get the fill color of this polygon.
     * @return the fill color.
     */
    public Color getFillColor() {
	return fillColor;
    }

    /**
     * Set the fill color of this polygon.
     * @param color the fill color, or null if none.
     */
    public void setFillColor(Color color) {
	fillColor = color;

	repaint();
    }

    //****************************************************************************
    //
    // Visual component related methods
    //
    //***************************************************************************

    /**
     * Paints this object.
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

				// First fill in polygon
	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(path);
	}

				// Then, draw pen
	if (penColor != null) {
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(path);
	}
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

    /**
     * Returns true if the specified rectangle is on the polygon.
     * @param <code>rect</code> Pick rectangle of object coordinates.
     * @return True if rectangle overlaps object.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	boolean picked = false;

	if (pickBounds(rect)) {
	    if (fillColor != null) {
		picked = intersectsPolygon(rect);
	    }
	    if (!picked && (penColor != null)) {
		picked = ZUtil.rectIntersectsPolyline(rect, xp, yp, penWidth);
	    }
	}

	return picked;
    }

    /**
     * Determines if any part of the rectangle is inside this polygon.
     * @param rect The rectangle being tested for intersecting this polygon
     * @return true if the rectangle intersects the polygon
     */
    protected boolean intersectsPolygon(Rectangle2D rect) {
	boolean inside = false;

	// we check each vertix of the rectangle to see if it's inside the polygon

	// check upper left corner
	inside = isInsidePolygon(new Point2D.Double(rect.getX(), rect.getY()));
	if (!inside) {
	    // check upper right corner
	    inside = isInsidePolygon(new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY()));
	    if (!inside) {
		// check lower right corner
		inside = isInsidePolygon(new Point2D.Double(rect.getX() + rect.getWidth(),
							   rect.getY() + rect.getHeight()));
		if (!inside) {
		    // check lower left corner
		    inside = isInsidePolygon(new Point2D.Double(rect.getX(),
							       rect.getY() + rect.getHeight()));
		    if (!inside) {
			// if none of the corners are inside of the polygon
			// we check to see if any of the edges go through the rectangle
			int i = 0;
			while (!inside && (i < (np - 1))) {
			    // for each edge
			    inside = rect.intersectsLine(new Line2D.Double(xp[i], yp[i],
									  xp[i + 1], yp[i + 1]));

			    i++;
			}
			inside = rect.intersectsLine(new Line2D.Double(xp[np - 1], yp[np - 1],
								      xp[0], yp[0]));
		    }
		}
	    }
	}

	return inside;
    }

    /**
     * Determines if point is inside this polygon.
     * @param pt The point being tested for containment within polygon
     * @return true if the point is inside the polygon
     */
    protected boolean isInsidePolygon(Point2D pt) {
	int i;
	double angle = 0.0;
	boolean inside = false;
	Point2D p1 = new Point2D.Float();
	Point2D p2 = new Point2D.Float();

	for (i = 0; i < (np - 1); i++) {
	    p1.setLocation(xp[i], yp[i]);
	    p2.setLocation(xp[i + 1], yp[i + 1]);
	    angle += ZUtil.angleBetweenPoints(pt, p1, p2);
	}
	p1.setLocation(xp[np - 1], yp[np - 1]);
	p2.setLocation(xp[0], yp[0]);
	angle += ZUtil.angleBetweenPoints(pt, p1, p2);

				// Allow for a bit of rounding
				// Ideally, angle should be 2*pi.
	if (java.lang.Math.abs(angle) > 6.2) {
	    inside = true;
	} else {
	    inside = false;
	}

	return inside;
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

	if ((fillColor != null) && (fillColor != DEFAULT_FILL_COLOR)) {
	    out.writeState("java.awt.Color", "fillColor", fillColor);
	}
	if (getPenWidth() != DEFAULT_PEN_WIDTH) {
	    out.writeState("float", "penWidth", getPenWidth());
	}
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

	if (fieldName.compareTo("fillColor") == 0) {
	    setFillColor((Color)fieldValue);
	} else if (fieldName.compareTo("penWidth") == 0) {
	    setPenWidth(((Float)fieldValue).floatValue());
	}
    }
}
