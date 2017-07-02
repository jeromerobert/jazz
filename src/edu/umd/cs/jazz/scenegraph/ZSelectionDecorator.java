/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZSelectionDecorator</b> is a ZVisualComponent decorator that indicates that this
 * component chain is selected.  It shows it is selected by surrounding its children with a
 * 1 pixel wide line.  Note: ZSelectionDecorator should be top in the Visual Component chain OR any VC's above it
 * should override the default select method in ZVisualComponent.
 *
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 * @see     ZVisualComponentDecorator
 */
public class ZSelectionDecorator extends ZVisualComponentDecorator {
    /**
     * Pen color of the selection rectangle
     */
    protected Color   penColor  = Color.magenta;
    /**
     * The camera the component is selected within
     */
    protected ZCamera camera = null;
    
    /**
     * Constructs a new ZSelectionDecorator.
     * This constructor does not specify a camera to be used in calculating
     * the thickness of the selection visual.  Set the camera separately
     * to make the selection visual have a constant thickness independent
     * of the camera magnification.
     * @see #setCamera
     */
    public ZSelectionDecorator() {
    }

    /**
     * Constructs a new ZSelectionDecorator.
     * @param camera The primary camera the object is being selected within.  Used to compute selection rectangle thickness
     */
    public ZSelectionDecorator(ZCamera camera) {
	this.camera = camera;
    }

    /**
     * Select the specified child visual component by inserting this
     * as a selection decorator.
     */
    public void select(ZVisualComponent vc) {
	if (selected == false) {
	    selected = true;
	    insertAbove(vc);
	    setVolatile(true);     // Selection is volatile, so set it now, after the
				   // decorator chain is formed, so that the parents
				   // get updated properly.
	}
    }

    /**
     * Unselect the visual component under this selection decorator
     * by removing this.
     */
    public void unselect() {
	if (selected == true) {
	    selected = false;
	    ZVisualComponent c = child;
	    remove();
	    c.unselect();
	    c.updateVolatility();   // Removing selection may remove volatility of component
	}
    }
    
    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Get the pen color of the selection visual
     * @return the pen color
     */   
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Specify the pen color of the selection visual
     * @param color The new pen color
     */
    public void setPenColor(Color color) {
	penColor = color;
	damage();
    }   
        
    /**
     * Get the camera that is used to calculate the thickness of the selection visual.
     * @return the camera
     */   
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Specify the camera that is used to calculate the thickness of the selection visual.
     * The selection rectangle will have a constant thickness independent of the
     * magnification of this camera.
     * @param camera The new camera
     */
    public void setCamera(ZCamera camera) {
	this.camera = camera;
	damage();
    }   
        
    //****************************************************************************
    //
    //			
    //
    //***************************************************************************
    
    /**
     * Paints the selected object and then paints the selection indicator
     * on top of the child.
     * @param <code>g2</code> The graphics context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();

				// First, paint child
	if (child != null) {
	    child.paint(renderContext);
				// Paint a rectangle around the selected object.
				// Note that the rectangle is always 1 pixel thick, 
				// regardless of the current magnification factor.
				// However - this code only works if a particular camera is specified
				// to perform this computation with.
	    float scale = computeMag();
	    g2.setStroke(new BasicStroke(1.0f / scale));
	    g2.setColor(penColor);
	    g2.draw(child.getLocalBounds());
	}
    }

    protected void computeLocalBounds() {
	localBounds.reset();
	if (child != null) {
				// Bounds needs to be one pixel bigger than child bounds - but that is one pixel
				// bigger for all magnifications of the object as well as the camera.  In addition,
				// if the selection is rendered with anti-aliasing, it can actually take up two
				// pixels, so we allocate space for two pixels modified by the magnification.
	    Rectangle2D childBounds = child.getLocalBounds();
	    float scale = computeMag();
  	    float horSpace = 2 / scale;
  	    float vertSpace = 2 / scale;
				  
				// The bounds of this selection object need to be bigger than the highlight rect
				// to accomodate the line
	    localBounds.setRect((float)(childBounds.getX() - horSpace), (float)(childBounds.getY() - vertSpace),
				(float)(childBounds.getWidth() + 2*horSpace), (float)(childBounds.getHeight() + 2*vertSpace));
	}
    }

    /**
     * Internal method to compute magnification of selected component.
     * Magnification includes global transform of node plus magnification
     * of camera that selection is associated with.
     */
    protected float computeMag() {
	AffineTransform tx = findNode().computeGlobalCoordinateFrame();
	if (camera != null) {
	    tx.concatenate(camera.getViewTransform().getAffineTransform());
	}

	return (float)tx.getScaleX();
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Selection should not be saved - thus if this decorator is saved,
     * it just skips over itself, and saves the child instead.
     */
    public ZSerializable writeReplace() {
	return getChild();
    }
}
