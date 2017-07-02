/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Vector;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZEllipse</b> is a graphic object that represents a hard-cornered
 * or rounded ellipse.
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Benjamin B. Bederson
 * @author  adapted from ZRectangle by Wayne Westerman, University of Delaware
 */
public class ZEllipse extends ZVisualComponent implements ZPenColor, ZFillColor, ZStroke, Serializable {
    static public final Color  penColor_DEFAULT = Color.black;
    static public final Color  fillColor_DEFAULT = Color.white;
    static public final double  penWidth_DEFAULT = 1.0;
    static public final boolean absPenWidth_DEFAULT = false;

    /**
     * Pen color for perimeter of ellipse
     */
    private Color     penColor  = penColor_DEFAULT;

    /**
     * Pen width of pen color.
     */
    private double     penWidth  = penWidth_DEFAULT;

    /**
     * Specifies if pen width is an absolute specification (independent of camera magnification)
     */
    private boolean    absPenWidth = absPenWidth_DEFAULT;

    /**
     * Fill color for interior of ellipse.
     */
    private Color     fillColor = fillColor_DEFAULT;

    /**
     * Position and Dimensions of ellipse
     */
    private transient Ellipse2D ellipse;

    /**
     * Stroke for rendering pen color
     */
    private transient Stroke stroke = new BasicStroke((float)penWidth_DEFAULT, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    //****************************************************************************
    //
    //                Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new Ellipse, initialized to location (0, 0) and size (0, 0).
     */
    public ZEllipse() {
        ellipse = new Ellipse2D.Double();
	reshape();
    }

    /**
     * Constructs an Ellipse2D at the specified location, initialized to size (0, 0).
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     */
    public ZEllipse(double x, double y) {
        ellipse = new Ellipse2D.Double(x, y, 0.0, 0.0);
	reshape();
    }

    /**
     * Constructs and initializes an Ellipse2D from the specified coordinates.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of ellipse
     * @param <code>height</code> Height of ellipse
     */
    public ZEllipse(double x, double y, double width, double height) {
        ellipse = new Ellipse2D.Double(x, y, width, height);
	reshape();
    }

    /**
     * Constructs a new Ellipse based on the geometry of the one passed in.
     * @param <code>r</code> A ellipse to get the geometry from
     */
    public ZEllipse(Ellipse2D r) {
        ellipse = (Ellipse2D)r.clone();
	reshape();
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Get the width of the pen used to draw the perimeter of this ellipse.
     * The pen is drawn centered around the ellipse vertices, so if the pen width
     * is thick, the bounds of the ellipse will grow. If the pen width is absolute
     * (independent of magnification), then this returns 0.
     * @return the pen width.
     */
    public double getPenWidth() {
	if (absPenWidth) {
	    return 0.0d;
	} else {
	    return penWidth;
	}
    }

    /**
     * Set the width of the pen used to draw the perimeter of this ellipse.
     * The pen is drawn centered around the ellipse vertices, so if the pen width
     * is thick, the bounds of the ellipse will grow.
     * @param width the pen width.
     */
    public void setPenWidth(double width) {
	penWidth = width;
	absPenWidth = false;
	setVolatileBounds(false);
	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	reshape();
    }

    /**
     * Set the absolute width of the pen used to draw the line around the perimeter of this ellipse.
     * The pen is drawn centered around the ellipse vertices, so if the pen width
     * is thick, the bounds of the ellipse will grow.
     * @param width the pen width.
     */
    public void setAbsPenWidth(double width) {
	penWidth = width;
	absPenWidth = true;
	setVolatileBounds(true);
	reshape();
    }

    /**
     * Get the absolute width of the pen used to draw the line around the perimeter of this ellipse.
     * The pen is drawn centered around the ellipse vertices, so if the pen width
     * is thick, the bounds of the ellipse will grow.  If the pen width is not absolute
     * (dependent on magnification), then this returns 0.
     * @return the absolute pen width.
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
     * Get the pen color of this ellipse.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color of this ellipse.
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

    /**
     * Get the fill color of this ellipse.
     * @return the fill color.
     */
    public Color getFillColor() {
	return fillColor;
    }

    /**
     * Set the fill color of this ellipse.
     * @param color the fill color, or null if none.
     */
    public void setFillColor(Color color) {
	fillColor = color;

	repaint();
    }


    //****************************************************************************
    //
    //
    //
    //***************************************************************************

    /**
     * Determines if the specified Rectangle2D overlaps this ellipse.
     * @param pickRect The rect that is picking this ellipse
     * @param path returns the scenegraph path to this ellipse.
     * @return True if pickRect overlaps this ellipse.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D pickRect, ZSceneGraphPath path) {
	if (fillColor == null) {
				// If no fill color, then don't pick inside of ellipse, only edge
	    if (pickBounds(pickRect)) {
		Rectangle2D innerBounds = new Rectangle2D.Double();
		double p, p2;

		if (penColor == null) {
		    p = 0.0;
		} else {
		    if (absPenWidth) {
			ZRenderContext rc = getRoot().getCurrentRenderContext();
			double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
			p = penWidth / mag;
		    } else {
			p = penWidth;
		    }
		}
		p2 = 0.5 * p;
		innerBounds.setRect((ellipse.getX() + p2), (ellipse.getY() + p2),
				    (ellipse.getWidth() - p), (ellipse.getHeight() - p));

		if (innerBounds.contains(pickRect)) {
		    return false;
		} else {
		    return true;
		}
	    } else {
		return false;
	    }
	} else {
	    return pickBounds(pickRect);
	}
    }

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

	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(ellipse);
	}
	if (penColor != null) {
	    if (absPenWidth) {
		double pw = penWidth / renderContext.getCompositeMagnification();
		stroke = new BasicStroke((float)pw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	    }
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(ellipse);
	}
    }

    /**
     * Notifies this object that it has changed and that it should update
     * its notion of its bounding box.  Note that this should not be called
     * directly.  Instead, it is called by <code>updateBounds</code> when needed.
     */
    protected void computeBounds() {
				// Expand the bounds to accomodate the pen width
	double p, p2;

	if (penColor == null) {
	    p = 0.0;
	} else {
	    if (absPenWidth) {
		ZRenderContext rc = getRoot().getCurrentRenderContext();
		double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
		p = penWidth / mag;
	    } else {
		p = penWidth;
	    }
	}
	p2 = 0.5 * p;

	bounds.setRect((ellipse.getX() - p2), (ellipse.getY() - p2),
		       (ellipse.getWidth() + p), (ellipse.getHeight() + p));
    }


    /**
     * Return the x coordinate of this ellipse.
     * @return the x coordinate.
     */
    public double getX() {
	return ellipse.getX();
    }

    /**
     * Return y coordinate of this ellipse.
     * @return the y coordinate.
     */
    public double getY() {
	return ellipse.getY();
    }

    /**
     * Return the width of this ellipse.
     * @return the width.
     */
    public double getWidth() {
	return ellipse.getWidth();
    }

    /**
     * Return the height of this ellipse.
     * @return the height.
     */
    public double getHeight() {
	return ellipse.getHeight();
    }

    /**
     * Return the ellipse.
     * @return the Ellipse2D.
     */
    public Ellipse2D getEllipse() {
	return ellipse;
    }

    /**
     * Sets the coordinates of this ellipse.
     * @param <code>x</code> X coordinate of top-left corner.
     * @param <code>y</code> Y coordinate of top-left corner.
     * @param <code>width</code> Width of ellipse.
     * @param <code>height</code> Height of ellipse.
     */
    public void setFrame(double x, double y, double width, double height) {
	ellipse.setFrame(x, y, width, height);
	reshape();
    }

    /**
     * Sets coords of ellipse
     * @param <code>r</code> The new ellipse coordinates
     */
    public void setFrame(Ellipse2D r) {
	ellipse = r;
	reshape();
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

	if ((penColor != null) && (penColor != penColor_DEFAULT)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}
	if ((fillColor != null) && (fillColor != fillColor_DEFAULT)) {
	    out.writeState("java.awt.Color", "fillColor", fillColor);
	}
	if (absPenWidth != absPenWidth_DEFAULT) {
	    out.writeState("boolean", "absPenWidth", absPenWidth);
	}
	if (getPenWidth() != penWidth_DEFAULT) {
	    out.writeState("double", "penWidth", getPenWidth());
	}

	Vector dimensions = new Vector();
	dimensions.add(new Double(ellipse.getX()));
	dimensions.add(new Double(ellipse.getY()));
	dimensions.add(new Double(ellipse.getWidth()));
	dimensions.add(new Double(ellipse.getHeight()));
	out.writeState("ellipse", "ellipse", dimensions);
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
	    if (absPenWidth) {
		setAbsPenWidth(((Double)fieldValue).doubleValue());
	    } else {
		setPenWidth(((Double)fieldValue).doubleValue());
	    }
	} else if (fieldName.compareTo("ellipse") == 0) {
	    Vector dim = (Vector)fieldValue;
	    double xpos   = ((Double)dim.get(0)).doubleValue();
	    double ypos   = ((Double)dim.get(1)).doubleValue();
	    double width  = ((Double)dim.get(2)).doubleValue();
	    double height = ((Double)dim.get(3)).doubleValue();
	    setFrame(xpos, ypos, width, height);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

				// write Ellipse2D ellipse
	out.writeDouble(ellipse.getX());
	out.writeDouble(ellipse.getY());
	out.writeDouble(ellipse.getWidth());
	out.writeDouble(ellipse.getHeight());

				// write Stroke stroke
	int cap = (int)((BasicStroke)stroke).getEndCap();
 	out.writeInt(cap);

	int join = (int)((BasicStroke)stroke).getLineJoin();
	out.writeInt(join);
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

				// read Ellipse2D ellipse
	double x, y, w, h;
	x = in.readDouble();
	y = in.readDouble();
	w = in.readDouble();
	h = in.readDouble();

				// read Stroke stroke
	int cap, join;
	cap = in.readInt();
	join = in.readInt();

	ellipse = new Ellipse2D.Double(x, y, w, h);
	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    }
}
