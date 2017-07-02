/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.scenegraph.*;

/** 
 * Provide some generic, useful routines
 * 
 * @author Ben Bederson
 * @author Britt McAlister
 */

public class ZUtil {
    /** 
     * Linearly interpolates between a and b, based on t.
     * Specifically, it computes lerp(a, b, t) = a + t*(b - a).
     * This produces a result that changes from a (when t = 0) to b (when t = 1).
     * 
     * @param a from point
     * @param b to Point
     * @param t variable 'time' parameter
     */
    static public float lerp(float t, float a, float b) {
	return (a + t * (b - a));
    }

    /** 
     * Linearly interpolates between a and b, based on t.
     * Specifically, it computes lerp(a, b, t) = a + t*(b - a).
     * This produces a result that changes from a (when t = 0) to b (when t = 1).
     * 
     * @param a from point
     * @param b to Point
     * @param t variable 'time' parameter
     */
    static public double lerp(double t, double a, double b) {
	return (a + t * (b - a));
    }

    /** 
     * Map the input linear lerp (a linear interpolation from (0-1) to
     * a slow-in, slow-out lerp.
     * 
     * @param t 
     */
    static public float sisoLerp(float t) {
	float siso, t1, t2, l;
	
	t1 = t * t;
	t2 = 1 - (1 - t) * (1 - t);
	l = lerp(t, t1, t2);
	siso = lerp(l, t1, t2);
	
	return siso;
    }

    /** 
     * Map the input linear lerp (a linear interpolation from (0-1) to
     * a slow-in, slow-out lerp.
     * 
     * @param t 
     */
    static public double sisoLerp(double t) {
	double siso, t1, t2, l;
	
	t1 = t * t;
	t2 = 1 - (1 - t) * (1 - t);
	l = lerp(t, t1, t2);
	siso = lerp(l, t1, t2);
	
	return siso;
    }

    /**
     * Apply the specified transform to the specified rectangle, modifying the rect.
     */
    static public void transform(Rectangle2D rect, AffineTransform tf) {
				// First, transform all 4 corners of the rectangle
	float[] pts = new float[8];
	pts[0] = (float)rect.getX();          // top left corner
	pts[1] = (float)rect.getY();
	pts[2] = (float)rect.getX() + (float)rect.getWidth();  // top right corner
	pts[3] = (float)rect.getY();
	pts[4] = (float)rect.getX() + (float)rect.getWidth();  // bottom right corner
	pts[5] = (float)rect.getY() + (float)rect.getHeight();
	pts[6] = (float)rect.getX();          // bottom left corner
	pts[7] = (float)rect.getY() + (float)rect.getHeight();
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
	rect.setRect(minX, minY, maxX - minX, maxY - minY);
    }
}
