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
 * <b>ZPolygon</b> is a visual component for displaying a polygonal
 * shape. It has both a pen color (used for the outline) and a fill
 * color (used to fill the shape).
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
    public ZPolygon(double x, double y) {
	super(x, y);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon with a two points
     * @param <code>x,y</code> First point
     * @param <code>x,y</code> Second point
     */
    public ZPolygon(double x1, double y1, double x2, double y2) {
	super(x1, y1, x2, y2);
	setClosed(true);
    }

    /**
     * Constructs a new ZPolygon from an array of points.
     * The xp, yp parameters are stored within this polygon, so the caller
     * must not modify them after passing them in.
     * @param <code>xp</code> Array of X points
     * @param <code>yp</code> Array of Y points
     */
    public ZPolygon(double[] xp, double[] yp) {
	super(xp, yp);
	setClosed(true);
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
	    if (absPenWidth) {
		double pw = penWidth / renderContext.getCompositeMagnification();
		stroke = new BasicStroke((float)pw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	    }
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(path);
	}
    }

    /**
     * Returns true if the specified rectangle is on the polygon.
     * @param rect Pick rectangle of object coordinates.
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return True if rectangle overlaps object.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	boolean picked = false;

	if (pickBounds(rect)) {
	    if (fillColor != null) {
		picked = ZUtil.intersectsPolygon(rect, xp, yp);
	    }
	    if (!picked && (penColor != null)) {
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
	}

	return picked;
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
	}
    }
}
