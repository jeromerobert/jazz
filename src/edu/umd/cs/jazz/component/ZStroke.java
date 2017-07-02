/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.Stroke;

/**
 * <b>ZStroke</b> represents the "stroke" attribute of a visual component.
 * Any visual components that support a stroke should implement this interface.
 * A stroke describes the rendering details of drawing a path - including
 * details such as pen width, join style, cap style, and dashes.
 *
 * @author  Benjamin B. Bederson
 */
public interface ZStroke extends ZAppearance {

    /**
     * Get the width of the pen used to draw the visual component.
     * @return the pen width.
     */
    public float getPenWidth();

    /**
     * Set the width of the pen used to draw the visual component.
     * If the pen width is set here, then the stroke is set to solid (un-dashed),
     * with a "butt" cap style, and a "bevel" join style.
     * @param width the pen width.
     */
    public void setPenWidth(float width);

    /**
     * Get the stroke used to draw the visual component.
     * @return the stroke.
     */
    public Stroke getStroke();

    /**
     * Set the stroke used to draw the visual component.
     * @param stroke the stroke.
     */
    public void setStroke(Stroke stroke);
}
