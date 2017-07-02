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
 * <b>ZRectangle</b> is a graphic object that represents a hard-cornered
 * or rounded rectangle.
 *
 * @author  Benjamin B. Bederson
 */
public class ZRectangle extends ZVisualComponent implements ZPenColor, ZFillColor, ZStroke, Serializable {
    static public final Color  penColor_DEFAULT = Color.black;
    static public final Color  fillColor_DEFAULT = Color.white;
    static public final float  penWidth_DEFAULT = 1.0f;

    /**
     * Pen color for perimeter of rectangle
     */
    private Color     penColor  = penColor_DEFAULT;

    /**
     * Pen width of pen color.
     */
    private float     penWidth  = penWidth_DEFAULT;

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
    private transient Stroke stroke = new BasicStroke(penWidth_DEFAULT, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    //****************************************************************************
    //
    //                Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new Rectangle.
     */
    public ZRectangle() {
        rect = new Rectangle2D.Float();
	reshape();
    }

    /**
     * Constructs a new Rectangle.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     */
    public ZRectangle(float x, float y) {
        rect = new Rectangle2D.Float(x, y, 0.0f, 0.0f);
	reshape();
    }

    /**
     * Constructs a new Rectangle.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of rectangle
     * @param <code>height</code> Height of rectangle
     */
    public ZRectangle(float x, float y, float width, float height) {
        rect = new Rectangle2D.Float(x, y, width, height);
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
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refRect The reference visual component to copy
     */
    public void duplicateObject(ZRectangle refRect) {
	super.duplicateObject(refRect);

        rect = (Rectangle2D)refRect.rect.clone();
	penColor = refRect.penColor;
	setPenWidth(refRect.penWidth);
	fillColor = refRect.fillColor;
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
	ZRectangle copy;

	objRefTable.reset();
	copy = new ZRectangle();
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
     * Get the width of the pen used to draw the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.
     * @return the pen width.
     */
    public float getPenWidth() {
	return penWidth;
    }

    /**
     * Set the width of the pen used to draw the perimeter of this rectangle.
     * The pen is drawn centered around the rectangle vertices, so if the pen width
     * is thick, the bounds of the rectangle will grow.
     * @param width the pen width.
     */
    public void setPenWidth(float width) {
	penWidth = width;
	stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
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
     * @return true if the rectangle picks this visual component
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D pickRect, ZSceneGraphPath path) {
	if (fillColor == null) {
				// If no fill color, then don't pick inside of rectangle, only edge
	    if (pickBounds(pickRect)) {
		Rectangle2D innerBounds = new Rectangle2D.Float();
		float p, p2;

		if (penColor == null) {
		    p = 0.0f;
		} else {
		    p = penWidth;
		}
		p2 = 0.5f * p;
		innerBounds.setRect((float)(rect.getX() + p2), (float)(rect.getY() + p2),
				    (float)(rect.getWidth() - p), (float)(rect.getHeight() - p));

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

        g2.setStroke(stroke);
	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(rect);
	}
	if (penColor != null) {
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
	float p, p2;

	if (penColor == null) {
	    p = 0.0f;
	} else {
	    p = penWidth;
	}
	p2 = 0.5f * p;

	bounds.setRect((float)(rect.getX() - p2), (float)(rect.getY() - p2),
		       (float)(rect.getWidth() + p), (float)(rect.getHeight() + p));
    }


    /**
     * Return x-coord of rectangle.
     * @return x-coord.
     */
    public float getX() {
	return (float)rect.getX();
    }

    /**
     * Return y-coord of rectangle.
     * @return y-coord.
     */
    public float getY() {
	return (float)rect.getY();
    }

    /**
     * Return width of rectangle.
     * @return width.
     */
    public float getWidth() {
	return (float)rect.getWidth();
    }

    /**
     * Return height of rectangle.
     * @return height.
     */
    public float getHeight() {
	return (float)rect.getHeight();
    }

    /**
     * Return rectangle.
     * @return rectangle.
     */
    public Rectangle2D getRect() {
	return rect;
    }

    /**
     * Sets coords of rectangle
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of rectangle
     * @param <code>height</code> Height of rectangle
     */
    public void setRect(float x, float y, float width, float height) {
	rect.setRect(x, y, width, height);
	reshape();
    }

    /**
     * Sets coords of rectangle
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
	if (getPenWidth() != penWidth_DEFAULT) {
	    out.writeState("float", "penWidth", getPenWidth());
	}

	Vector dimensions = new Vector();
	dimensions.add(new Float(rect.getX()));
	dimensions.add(new Float(rect.getY()));
	dimensions.add(new Float(rect.getWidth()));
	dimensions.add(new Float(rect.getHeight()));
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
	    setPenWidth(((Float)fieldValue).floatValue());
	} else if (fieldName.compareTo("rect") == 0) {
	    Vector dim = (Vector)fieldValue;
	    float xpos   = ((Float)dim.get(0)).floatValue();
	    float ypos   = ((Float)dim.get(1)).floatValue();
	    float width  = ((Float)dim.get(2)).floatValue();
	    float height = ((Float)dim.get(3)).floatValue();
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

	rect = new Rectangle2D.Float((float)x, (float)y, (float)w, (float)h);
	stroke = new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    }
}
