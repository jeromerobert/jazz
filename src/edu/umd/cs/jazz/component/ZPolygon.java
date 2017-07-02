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
 * <b>ZPolygon</b> is a visual component that represents a line
 * with one or more segments.
 *
 * @author  Benjamin B. Bederson
 */
public class ZPolygon extends ZCoordList {
    static public final Color  DEFAULT_PEN_COLOR  = Color.black;
    static public final Color  DEFAULT_FILL_COLOR = Color.white;
    static public final float  DEFAULT_PEN_WIDTH  = 1.0f;

    protected Color       penColor   = DEFAULT_PEN_COLOR;
    protected Color       fillColor  = DEFAULT_FILL_COLOR;
    protected float       penWidth   = DEFAULT_PEN_WIDTH;
    protected int         join       = BasicStroke.JOIN_BEVEL;
    protected BasicStroke stroke = new BasicStroke(DEFAULT_PEN_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    
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
     * Constructs a new ZPolygon that is a duplicate of the reference polygon, i.e., a "copy constructor"
     * @param <code>poly</code> Reference polygon
     */
    public ZPolygon(ZPolygon poly) {
	super(poly);

	penColor = poly.penColor;
	fillColor = poly.fillColor;
	penWidth = poly.penWidth;
	join = poly.join;
	stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, join);
    }

    /**
     * Duplicates the current ZPolygon by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZPolygon(ZPolygon)
     */
    public Object clone() {
	return new ZPolygon(this);
    }
    
    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************
    
    /**
     * Get the width of the pen used to draw the line around the edge of this polygon.
     * The pen is drawn centered around the polygon vertices, so if the pen width
     * is thick, the bounds of the polygon will grow.
     * @return the pen width.
     */
    public float getPenWidth() {
	return penWidth;
    }

    /**
     * Set the width of the pen used to draw the line around the edge of this polygon.
     * The pen is drawn centered around the polygon vertices, so if the pen width
     * is thick, the bounds of the polyline will grow.
     * @param width the pen width.
     */
    public void setPenWidth(float width) {
	penWidth = width;
	stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	damage(true);
    }

    /**
     * Get the pen color of this polygon.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color of this polygon.
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
    }   

    //****************************************************************************
    //
    // Visual component related methods
    //
    //***************************************************************************

    /**
     * Paints this object.
     * @param <code>g2</code> The graphics context to paint into.  */
    public void paint(ZRenderContext renderContext) {
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

				// Expand the bounds to accomodate the pen width
	if (penColor != null) {
	    float p, p2;
	    p = penWidth;
	    p2 = 0.5f * p;
	    localBounds.setRect(localBounds.x - p2, localBounds.y - p2, localBounds.width + p, localBounds.height + p);
	}
    }

    /**
     * Returns true if the specified rectangle is on the polygon.
     * @param <code>rect</code> Pick rectangle of object coordinates.
     * @return True if rectangle overlaps object.
     */
    public boolean pick(Rectangle2D rect) {
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

	if ((penColor != null) && (penColor != DEFAULT_PEN_COLOR)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}
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

	if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
	} else if (fieldName.compareTo("fillColor") == 0) {
	    setFillColor((Color)fieldValue);
	} else if (fieldName.compareTo("penWidth") == 0) {
	    setPenWidth(((Float)fieldValue).floatValue());
	}
    }
}
