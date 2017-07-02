/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
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
 * <b>ZRectangle</b> is a graphic object that represents a hard-cornered
 * or rounded rectangle.
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
public class ZRectangle extends ZVisualComponent implements ZPenColor, ZFillColor, ZStroke, Serializable {
    static public final Color   penColor_DEFAULT = Color.black;
    static public final Color   fillColor_DEFAULT = Color.white;
    static public final double  penWidth_DEFAULT = 1.0;
    static public final boolean absPenWidth_DEFAULT = false;

    /**
     * Pen color for perimeter of rectangle
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
     * Fill color for interior of rectangle.
     */
    private Color     fillColor = fillColor_DEFAULT;

    /**
     * Position and Dimensions of rectangle
     */
    private transient Rectangle2D rect;

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
     * Constructs a new Rectangle.
     */
    public ZRectangle() {
        rect = new Rectangle2D.Double();
	reshape();
    }

    /**
     * Constructs a new Rectangle at the specified location, with dimensions of zero.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     */
    public ZRectangle(double x, double y) {
        rect = new Rectangle2D.Double(x, y, 0.0, 0.0);
	reshape();
    }

    /**
     * Constructs a new Rectangle at the specified location, with the given dimensions.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of rectangle
     * @param <code>height</code> Height of rectangle
     */
    public ZRectangle(double x, double y, double width, double height) {
        rect = new Rectangle2D.Double(x, y, width, height);
	reshape();
    }

    /**
     * Constructs a new Rectangle based on the geometry of the one passed in.
     * @param <code>r</code> A rectangle to get the geometry from
     */
    public ZRectangle(Rectangle2D r) {
        rect = (Rectangle2D)r.clone();
	reshape();
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZRectangle newRectangle = (ZRectangle)super.duplicateObject();

        newRectangle.rect = (Rectangle2D)rect.clone();

	return newRectangle;	
    }


    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Get the width of the pen used to draw the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.  If the pen width is absolute
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
     * Set the width of the pen used to draw the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.
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
     * Set the absolute width of the pen used to draw the line around the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.
     * @param width the pen width.
     */
    public void setAbsPenWidth(double width) {
	penWidth = width;
	absPenWidth = true;
	setVolatileBounds(true);
	reshape();
    }

    /**
     * Get the absolute width of the pen used to draw the line around the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.  If the pen width is not absolute
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
     * Get the pen color of this rectangle.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color of this rectangle.
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
     * Get the fill color of this rectangle.
     * @return the fill color.
     */
    public Color getFillColor() {
	return fillColor;
    }

    /**
     * Set the fill color of this rectangle.
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
     * Determines if the specified rectangle overlaps this rectangle.
     * @param pickRect The rectangle that is picking this rectangle
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return true if the rectangle picks this visual component
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D pickRect, ZSceneGraphPath path) {
	if (fillColor == null) {
				// If no fill color, then don't pick inside of rectangle, only edge
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
		innerBounds.setRect((rect.getX() + p2), (rect.getY() + p2),
				    (rect.getWidth() - p), (rect.getHeight() - p));

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
	    g2.fill(rect);
	}
	if (penColor != null) {
	    if (absPenWidth) {
		double pw = penWidth / renderContext.getCompositeMagnification();
		stroke = new BasicStroke((float)pw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	    }
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(rect);
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

	bounds.setRect((rect.getX() - p2), (rect.getY() - p2),
		       (rect.getWidth() + p), (rect.getHeight() + p));
    }


    /**
     * Return x-coord of rectangle.
     * @return x-coord.
     */
    public double getX() {
	return rect.getX();
    }

    /**
     * Return y-coord of rectangle.
     * @return y-coord.
     */
    public double getY() {
	return rect.getY();
    }

    /**
     * Return width of rectangle.
     * @return width.
     */
    public double getWidth() {
	return rect.getWidth();
    }

    /**
     * Return height of rectangle.
     * @return height.
     */
    public double getHeight() {
	return rect.getHeight();
    }

    /**
     * Return rectangle.
     * @return rectangle.
     */
    public Rectangle2D getRect() {
	return rect;
    }

    /**
     * Sets location and size of the rectangle.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of rectangle
     * @param <code>height</code> Height of rectangle
     */
    public void setRect(double x, double y, double width, double height) {
	rect.setRect(x, y, width, height);
	reshape();
    }

    /**
     * Sets coordinates of rectangle.
     * @param <code>r</code> The new rectangle coordinates
     */
    public void setRect(Rectangle2D r) {
	rect = r;
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
	dimensions.add(new Double(rect.getX()));
	dimensions.add(new Double(rect.getY()));
	dimensions.add(new Double(rect.getWidth()));
	dimensions.add(new Double(rect.getHeight()));
	out.writeState("rectangle", "rect", dimensions);
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
	} else if (fieldName.compareTo("rect") == 0) {
	    Vector dim = (Vector)fieldValue;
	    double xpos   = ((Double)dim.get(0)).doubleValue();
	    double ypos   = ((Double)dim.get(1)).doubleValue();
	    double width  = ((Double)dim.get(2)).doubleValue();
	    double height = ((Double)dim.get(3)).doubleValue();
	    setRect(xpos, ypos, width, height);
	}
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

				// write Rectangle2D rect
	out.writeDouble(rect.getX());
	out.writeDouble(rect.getY());
	out.writeDouble(rect.getWidth());
	out.writeDouble(rect.getHeight());

				// write Stroke stroke
	int cap = (int)((BasicStroke)stroke).getEndCap();
 	out.writeInt(cap);

	int join = (int)((BasicStroke)stroke).getLineJoin();
	out.writeInt(join);
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

				// read Rectangle2D rect
	double x, y, w, h;
	x = in.readDouble();
	y = in.readDouble();
	w = in.readDouble();
	h = in.readDouble();

				// read Stroke stroke
	int cap, join;
	cap = in.readInt();
	join = in.readInt();

	rect = new Rectangle2D.Double(x, y, w, h);
	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    }
}
