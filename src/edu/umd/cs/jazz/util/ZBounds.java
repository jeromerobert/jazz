/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.geom.*;

/**
 * A ZBounds is simply a Rectangle2D.Float with extra methods that more
 * properly deal with the case when the rectangle is "empty".  A ZBounds
 * has an extra bit to store emptiness.  In this state, adding new geometry
 * replaces the current geometry.  
 * <p>
 * This is intended for use by visual objects that store their dimensions, and
 * which may be empty.
 */

public class ZBounds extends Rectangle2D.Float {
    protected boolean empty = true;

    public ZBounds() {
    }

    public ZBounds(float x, float y, float w, float h) {
	super(x, y, w, h);
	empty = false;
    }

    public ZBounds(Rectangle2D rect) {
	super((float)rect.getX(), (float)rect.getY(), (float)rect.getWidth(), (float)rect.getHeight());
	empty = false;
    }

    public ZBounds(ZBounds bounds) {
	super(bounds.x, bounds.y, bounds.width, bounds.height);
	empty = false;
    }

    public void reset() {
	empty = true;
    }
    
    public void transform(AffineTransform tf) {
	if (isEmpty()) {
	    return;
	}

				// First, transform all 4 corners of the rectangle
	float[] pts = new float[8];
	pts[0] = x;          // top left corner
	pts[1] = y;
	pts[2] = x + width;  // top right corner
	pts[3] = y;
	pts[4] = x + width;  // bottom right corner
	pts[5] = y + height;
	pts[6] = x;          // bottom left corner
	pts[7] = y + height;
	tf.transform(pts, 0, pts, 0, 4);

				// Then, find the bounds of those 4 transformed points.
	float minX = pts[0];
	float minY = pts[1];
	float maxX = pts[0];
	float maxY = pts[1];
	int i;
	for (i=1; i<4; i++) {
	    if (pts[2*i] < minX) {
		minX = pts[2*i];
	    }
	    if (pts[2*i+1] < minY) {
		minY = pts[2*i+1];
	    }
	    if (pts[2*i] > maxX) {
		maxX = pts[2*i];
	    }
	    if (pts[2*i+1] > maxY) {
		maxY = pts[2*i+1];
	    }
	}
	setRect(minX, minY, maxX - minX, maxY - minY);
    }

    /** Return center point of bounds
     */
    public Point2D getCenter2D() {
	return new Point2D.Float(x + 0.5f*width, y + 0.5f*height);
    }

    public boolean isEmpty() {
	return empty;
    }
    
    public void setRect(float x, float y, float w, float h) {
	super.setRect(x, y, w, h);
	empty = false;
    }

    public void setRect(double x, double y, double w, double h) {
	super.setRect(x, y, w, h);
	empty = false;
    }

    public void setRect(Rectangle2D r) {
	super.setRect(r);
	empty = false;
    }

    public void add(double newx, double newy) {
	if (empty) {
	    setRect(newx, newy, 0, 0);
	    empty = false;
	} else {
	    super.add(newx, newy);
	}
    }

    public void add(Rectangle2D r) {
	if (empty) {
	    setRect(r);
	    empty = false;
	} else {
	    super.add(r);
	}
    }

    public void add(ZBounds r) {
	if (r.isEmpty()) {
	    return;
	}

	if (empty) {
	    setRect(r);
	    empty = false;
	} else {
	    super.add(r);
	}
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String str;
	if (isEmpty()) {
	    str = "jazz.util.ZBounds[Empty]";
	} else {
	    str = super.toString();
	}
	return str;
    }
}
