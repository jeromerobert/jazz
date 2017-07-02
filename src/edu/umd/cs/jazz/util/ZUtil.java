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

    /**
     * Determine if the specified rectangle intersects the specified polyline.
     * @param rect The rectangle that is being tested for intersection
     * @param xp The array of X-coordinates that determines the polyline
     * @param yp The array of Y-coordinates that determines the polyline
     * @param penWidth The width of the polyline
     * @return true if the rectangle intersects the polyline.
     */
    static public boolean rectIntersectsPolyline(Rectangle2D rect, float[] xp, float[] yp, float penWidth) {
	boolean picked = false;
	float width = (float)((0.5f * penWidth) + (0.5f * ((0.5f * rect.getWidth()) + (0.5f * rect.getHeight()))));
	float px = (float)(rect.getX() + (0.5f * rect.getWidth()));
	float py = (float)(rect.getY() + (0.5f * rect.getHeight()));
	float squareDist;
	float minSquareDist;
	int i;
	int np = xp.length;

	if (np > 0) {
	    if (np == 1) {
		minSquareDist = (float)Line2D.ptSegDistSq(xp[0], yp[0], xp[0], yp[0], px, py);
	    } else {
		minSquareDist = (float)Line2D.ptSegDistSq(xp[0], yp[0], xp[1], yp[1], px, py);
	    }
	    for (i=1; i<np-1; i++) {
		squareDist = (float)Line2D.ptSegDistSq(xp[i], yp[i], xp[i+1], yp[i+1], px, py);
		if (squareDist < minSquareDist) {
		    minSquareDist = squareDist;
		}
	    }
	    if (minSquareDist <= (width*width)) {
		picked = true;
	    }
	}

	return picked;
    }

    /**
     * Returns the angle in radians between point a and point b from point pt,
     * that is the angle between a-pt-b.
     * @param pt,a,b The points that specify the angle
     * @return the angle
     */
    static public float angleBetweenPoints(Point2D pt, Point2D a, Point2D b) {
	Point2D v1, v2;
	float z, s;
	float theta;
	float t1, t2;
	
	// vector from pt to a
	v1 = new Point2D.Float((float)(a.getX() - pt.getX()), (float)(a.getY() - pt.getY()));

	// vector from pt to b
	v2 = new Point2D.Float((float)(b.getX() - pt.getX()), (float)(b.getY() - pt.getY()));
	
	z = (float)((v1.getX() * v2.getY()) - (v1.getY() * v2.getX()));

	// s is the sign of z
	if (z >= 0) {
	    s = 1;
	}
	else {
	    s = -1;
	}

	// so we can calculate the angle from the cosine through the dot product
	t1 = (float)(pt.distance(a) * pt.distance(b));

	if (t1 == 0.0) {
	    theta = 0.0f;
	} else {
	    // Calculate angle between vectors by doing dot product and dividing by 
	    // the product of the length of the vectors.
	    t2 = (float)((v1.getX() * v2.getX() + v1.getY() * v2.getY()) / t1);
	    if ((t2 < -1) || (t2 > 1)) {
		theta = 0.0f;
	    } 
	    else {
		theta = (float)(s * java.lang.Math.acos(t2));
	    }
	}
	
	return theta;
    }
}
