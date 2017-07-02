/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Vector;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZRectangle</b> is a graphic object that represents a hard-cornered
 * or rounded rectangle.
 *
 * @author  Benjamin B. Bederson
 */
public class ZRectangle extends ZVisualComponent implements Cloneable {
    static public final Color  penColor_DEFAULT = Color.black;
    static public final Color  fillColor_DEFAULT = Color.white;
    static public final float  penWidth_DEFAULT = 1.0f;

    protected Color     penColor  = penColor_DEFAULT;
    protected float     penWidth  = penWidth_DEFAULT;
    protected Color     fillColor = fillColor_DEFAULT;
    
    protected Rectangle2D rect;
    /**
     * Constructs a new Rectangle.
     */
    public ZRectangle() {
        rect = new Rectangle2D.Float();
	updateBounds();
    }

    /**
     * Constructs a new Rectangle.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     */
    public ZRectangle(float x, float y) {
        rect = new Rectangle2D.Float(x, y, 0.0f, 0.0f);
	updateBounds();
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
	updateBounds();
    }

    /**
     * Constructs a new Rectangle based on the geometry of the one passed in.
     * @param <code>r</code> A rectangle to get the geometry from
     */
    public ZRectangle(Rectangle2D r) {
        rect = (Rectangle2D)r.clone();
	updateBounds();
    }

    /**
     * Constructs a new ZRectangle based on the one passed in (i.e., a "copy constructor").
     * @param <code>r</code> A rectangle to duplicate
     */
    public ZRectangle(ZRectangle r) {
        rect = (Rectangle2D)r.rect.clone();
	localBounds = (ZBounds)r.localBounds.clone();
	penColor = r.penColor;
	penWidth = r.penWidth;
	fillColor = r.fillColor;
    }

    /**
     * Duplicates the current ZRectangle by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     *
     * @see #ZRectangle(ZRectangle)
     */
    public Object clone() {
	return new ZRectangle(this);
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

    
    public Color getFillColor() {return fillColor;}
    public void setFillColor(Color color) {
	fillColor = color;

	damage();
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
     */
    public boolean pick(Rectangle2D pickRect) {
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
     * @param <code>g2</code> The graphics context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	
        g2.setStroke(new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(rect);
	}
	if (penColor != null) {
	    g2.setColor(penColor);
	    g2.draw(rect);
	}
    }

    protected void computeLocalBounds() {
				// Expand the bounds to accomodate the pen width
	float p, p2;

	if (penColor == null) {
	    p = 0.0f;
	} else {
	    p = penWidth;
	}
	p2 = 0.5f * p;
	localBounds.setRect((float)(rect.getX() - p2), (float)(rect.getY() - p2), 
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
	damage(true);
    }

    /**
     * Sets coords of rectangle
     * @param <code>r</code> The new rectangle coordinates
     */
    public void setRect(Rectangle2D r) {
	rect = r;
	damage(true);
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
	    float xpos   = ((Float)dim.elementAt(0)).floatValue();
	    float ypos   = ((Float)dim.elementAt(1)).floatValue();
	    float width  = ((Float)dim.elementAt(2)).floatValue();
	    float height = ((Float)dim.elementAt(3)).floatValue();
	    setRect(xpos, ypos, width, height);
	}
    }
}
