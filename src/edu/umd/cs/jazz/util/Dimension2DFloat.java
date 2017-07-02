/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 * Dimension2D.Float.java
 *
 */

package edu.umd.cs.jazz.util;

import java.awt.geom.Dimension2D;

/**
 * The <code>Dimension</code> class encapsulates the width and 
 * height of a component (in float precision) in a single object. 
 * <p>
 * Normally the values of <code>width</code> 
 * and <code>height</code> are non-negative integers. 
 * The constructors that allow you to create a dimension do 
 * not prevent you from setting a negative value for these properties. 
 * If the value of <code>width</code> or <code>height</code> is 
 * negative, the behavior of some methods defined by other objects is 
 * undefined. 
 * 
 * This is modified from java.awt.Dimension
 */
public class Dimension2DFloat extends Dimension2D {
    
    /**
     * The width dimension. Negative values can be used. 
     *
     * @see #setSize(float, float)
     */
    public float width;

    /**
     * The height dimension. Negative values can be used. 
     *
     * @see #setSize(float, float)
     */
    public float height;

    /** 
     * Creates an instance of <code>Dimension2DFloat</code> with a width 
     * of zero and a height of zero. 
     */
    public Dimension2DFloat() {
	this(0, 0);
    }

    /** 
     * Creates an instance of <code>Dimension2DFloat</code> whose width  
     * and height are the same as for the specified dimension. 
     * @param    d   the specified dimension for the 
     *               <code>width</code> and 
     *               <code>height</code> values.
     */
    public Dimension2DFloat(Dimension2D d) {
	this((float)d.getWidth(), (float)d.getHeight());
    }

    /** 
     * Constructs a Dimension and initializes it to the specified width and
     * specified height.
     * @param width the specified width dimension
     * @param height the specified height dimension
     */
    public Dimension2DFloat(float width, float height) {
	this.width = width;
	this.height = height;
    }

    /**
     * Returns the width of this dimension in double precision.
     */
    public double getWidth() {
	return width;
    }

    /**
     * Returns the height of this dimension in double precision.
     */
    public double getHeight() {
	return height;
    }

    /**
     * Set the size of this Dimension object to the specified width
     * and height in double precision.
     * @param width  the new width for the Dimension object
     * @param height  the new height for the Dimension object
     */
    public void setSize(double width, double height) {
	width = (float)Math.ceil(width);
	height = (float)Math.ceil(height);
    }

    /**
     * Set the size of this <code>Dimension</code> object to the specified size.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     * @param    d  the new size for this <code>Dimension</code> object.
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    JDK1.1
     */
    public void setSize(Dimension2D d) {
	setSize(d.getWidth(), d.getHeight());
    }	

    /**
     * Set the size of this <code>Dimension</code> object 
     * to the specified width and height.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     * @param    width   the new width for this <code>Dimension</code> object.
     * @param    height  the new height for this <code>Dimension</code> object.
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    JDK1.1
     */
    public void setSize(float width, float height) {
    	this.width = width;
    	this.height = height;
    }	

    /**
     * Checks whether two dimension objects have equal values.
     */
    public boolean equals(Object obj) {
	if (obj instanceof Dimension2DFloat) {
	    Dimension2DFloat d = (Dimension2DFloat)obj;
	    return (width == d.width) && (height == d.height);
	}
	return false;
    }

    /**
     * Returns a string representation of the values of this 
     * <code>Dimension</code> object's <code>height</code> and 
     * <code>width</code> fields. This method is intended to be used only 
     * for debugging purposes, and the content and format of the returned 
     * string may vary between implementations. The returned string may be 
     * empty but may not be <code>null</code>.
     * 
     * @return  a string representation of this <code>Dimension</code> 
     *          object.
     */
    public String toString() {
	return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }
}
