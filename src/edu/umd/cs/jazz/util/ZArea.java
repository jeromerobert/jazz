/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.geom.*;

/**
 * ZArea provides a very simple implementation of Area that only supports
 * rectangle geometry.  The goal is here to do implement what jazz needs
 * in a faster way than Area can provide.  We don't need the generality
 * of Area, and can do the subset that we require faster.
 * 
 * @author Ben Bederson
 */
public class ZArea implements Shape, Cloneable {

    protected ZBounds bounds;

    /**
     * Constructs a new ZArea.  
     */
    public ZArea() {
	bounds = new ZBounds();
    }
    
    /**
     * Constructs a new ZArea with a starting content of the passed in bounds.
     * @param rect The rectangle to initialize the ZArea with.
     */
    public ZArea(Rectangle rect) {
	bounds = new ZBounds(rect);
    }

    /**
     * Constructs a new ZArea with a starting content of the passed in bounds.
     * @param rect The rectangle to initialize the ZArea with.
     */
    public ZArea(Rectangle2D rect) {
	bounds = new ZBounds(rect);
    }

    /**
     * Constructs a new ZArea with a starting content of the passed in bounds.
     * @param rect The rectangle to initialize the ZArea with.
     */
    public ZArea(ZBounds rhs) {
	bounds = rhs;
    }

    /**
     * Constructs a new ZArea with a starting content of the passed in ZArea.
     * @param rect The rectangle to initialize the ZArea with.
     */
    public ZArea(ZArea rhs) {
	bounds = new ZBounds(rhs.getBounds2D());
    }

    /**
     * Duplicates the current ZArea by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZArea(ZArea)
     */
    public Object clone() {
	return new ZArea(this);
    }

    /**
     * Determines if the area is empty.
     * @return true if the area is empty, or false otherwise.
     */
    public boolean isEmpty() {
	return bounds.isEmpty();
    }

    /**
     * Set the ZArea to be the union of passed in rectangle and the current contents
     * of the ZArea.
     * @param rhs The rectangle to add to the ZArea.
     */
    public void add(Rectangle2D rhs) {
	if (rhs.isEmpty()) {
	    return;
	}
	bounds.add(rhs);
    }

    /**
     * Set the ZArea to be the union of passed in bounds and the current contents
     * of the ZArea.
     * @param rhs The bounds to add to the ZArea.
     */
    public void add(ZBounds rhs) {
	if (rhs.isEmpty()) {
	    return;
	}
	bounds.add(rhs);
    }

    /**
     * Set the ZArea to be the union of the passed in area and the current contents
     * of the ZArea.
     * @param rhs The ZArea to add to the ZArea.
     */
    public void add(ZArea rhs) {
	if (rhs.isEmpty()) {
	    return;
	}
	bounds.add(rhs.getBounds2D());
    }

    /**
     * Return the current bounds of this ZArea
     */
    public Rectangle getBounds() {
	return new Rectangle((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
    }

    /**
     * Return the current bounds2D of this ZArea
     */
    public Rectangle2D getBounds2D() {
	return bounds.getBounds2D();
    }

    /**
     * Removes all content from this ZArea and restores it to an empty area.
     */
    public void reset() {
	bounds.reset();
    }
    
    /**
     * Transforms the geometry of this ZArea using the specified AffineTransform.
     * @param t The AffineTransform to apply to the ZAreas current contents
     */
    public void transform(AffineTransform t) {
	bounds.transform(t);
    }

    /**
     * Creates a copy of this area, and transforms its geometry using the specified AffineTransform.
     * @param t The AffineTransform to apply to the ZAreas current contents
     */
    public ZArea createTransformedArea(AffineTransform t) {
	ZArea area = new ZArea(this);
	area.transform(t);
	return area;
    }

    /**
     * Tests if the specified coordinates are inside the boundary of the 
     * <code>Shape</code>.
     * @param x,&nbsp;y the specified coordinates
     * @return <code>true</code> if the specified coordinates are inside 
     *         the <code>Shape</code> boundary; <code>false</code>
     *         otherwise.
     */
    public boolean contains(double x, double y) {
	double x0 = bounds.getX();
	double y0 = bounds.getY();
	return (x >= x0 &&
		y >= y0 &&
		x < (x0 + bounds.getWidth()) &&
		y < (y0 + bounds.getHeight()));
    }

    /**
     * Tests if a specified {@link Point2D} is inside the boundary
     * of the <code>Shape</code>.
     * @param p a specified <code>Point2D</code>
     * @return <code>true</code> if the specified <code>Point2D</code> is 
     *          inside the boundary of the <code>Shape</code>;
     *		<code>false</code> otherwise.
     */
    public boolean contains(Point2D p) {
	return contains(p.getX(), p.getY());
    }

    /**
     * Tests if the interior of the <code>Shape</code> entirely contains 
     * the specified rectangular area.  All coordinates that lie inside
     * the rectangular area must lie within the <code>Shape</code> for the
     * entire rectanglar area to be considered contained within the 
     * <code>Shape</code>.
     * <p>
     * This method might conservatively return <code>false</code> when:
     * <ul>
     * <li>
     * the <code>intersect</code> method returns <code>true</code> and
     * <li>
     * the calculations to determine whether or not the
     * <code>Shape</code> entirely contains the rectangular area are
     * prohibitively expensive.
     * </ul>
     * This means that this method might return <code>false</code> even
     * though the <code>Shape</code> contains the rectangular area.
     * The <code>Area</code> class can be used to perform more accurate 
     * computations of geometric intersection for any <code>Shape</code>
     * object if a more precise answer is required.
     * @param x,&nbsp;y the coordinates of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Shape</code>
     * 		entirely contains the specified rectangular area;
     * 		<code>false</code> otherwise or, if the <code>Shape</code>    
     *		contains the rectangular area and the   
     *		<code>intersects</code> method returns <code>true</code> 
     * 		and the containment calculations would be too expensive to
     * 		perform.
     * @see java.awt.geom.Area
     * @see #intersects
     */
    public boolean contains(double x, double y, double w, double h) {
	if (isEmpty() || w <= 0 || h <= 0) {
	    return false;
	}
	double x0 = bounds.getX();
	double y0 = bounds.getY();
	return (x >= x0 &&
		y >= y0 &&
		(x + w) <= (x0 + bounds.getWidth()) &&
		(y + h) <= (y0 + bounds.getHeight()));
    }

    /**
     * Tests if the interior of the <code>Shape</code> entirely contains the 
     * specified <code>Rectangle2D</code>.
     * This method might conservatively return <code>false</code> when:
     * <ul>
     * <li>
     * the <code>intersect</code> method returns <code>true</code> and
     * <li>
     * the calculations to determine whether or not the
     * <code>Shape</code> entirely contains the <code>Rectangle2D</code>
     * are prohibitively expensive.
     * </ul>
     * This means that this method might return <code>false</code> even   
     * though the <code>Shape</code> contains the
     * <code>Rectangle2D</code>.
     * The <code>Area</code> class can be used to perform more accurate 
     * computations of geometric intersection for any <code>Shape</code>  
     * object if a more precise answer is required.
     * @param r The specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>Shape</code>
     *          entirely contains the <code>Rectangle2D</code>;
     *          <code>false</code> otherwise or, if the <code>Shape</code>
     *          contains the <code>Rectangle2D</code> and the
     *          <code>intersects</code> method returns <code>true</code>
     *          and the containment calculations would be too expensive to
     *          perform. 
     * @see #contains(double, double, double, double)
     */
    public boolean contains(Rectangle2D r) {
	return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the interior of the <code>ZArea</code> intersects the 
     * interior of a specified rectangular area.
     * The rectangular area is considered to intersect the <code>ZArea</code> 
     * if any point is contained in both the interior of the 
     * <code>ZArea</code> and the specified rectangular area.
     * @param x,&nbsp;y the coordinates of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Shape</code> and
     * 		the interior of the rectangular area intersect, or are
     * 		both highly likely to intersect and intersection calculations 
     * 		would be too expensive to perform; <code>false</code> otherwise.
     */
    public boolean intersects(float x, float y, float w, float h) {
	if (isEmpty() || w <= 0 || h <= 0) {
	    return false;
	}
	float x0 = (float)bounds.getX();
	float y0 = (float)bounds.getY();
	return (((x + w) > x0) &&
		((y + h) > y0) &&
		x < (x0 + (float)bounds.getWidth()) &&
		y < (y0 + (float)bounds.getHeight()));
    }

    /**
     * Tests if the interior of the <code>ZArea</code> intersects the 
     * interior of a specified rectangular area.
     * The rectangular area is considered to intersect the <code>ZArea</code> 
     * if any point is contained in both the interior of the 
     * <code>ZArea</code> and the specified rectangular area.
     * @param x,&nbsp;y the coordinates of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Shape</code> and
     * 		the interior of the rectangular area intersect, or are
     * 		both highly likely to intersect and intersection calculations 
     * 		would be too expensive to perform; <code>false</code> otherwise.
     */
    public boolean intersects(double x, double y, double w, double h) {
	if (isEmpty() || w <= 0 || h <= 0) {
	    return false;
	}
	double x0 = bounds.getX();
	double y0 = bounds.getY();
	return ((x + w) > x0 &&
		(y + h) > y0 &&
		x < (x0 + bounds.getWidth()) &&
		y < (y0 + bounds.getHeight()));
    }

    /**
     * Tests if the interior of the <code>Shape</code> intersects the 
     * interior of a specified <code>Rectangle2D</code>.
     * This method might conservatively return <code>true</code> when:
     * <ul>
     * <li>
     * there is a high probability that the <code>Rectangle2D</code> and the
     * <code>Shape</code> intersect, but
     * <li>
     * the calculations to accurately determine this intersection
     * are prohibitively expensive.
     * </ul>
     * This means that this method might return <code>true</code> even
     * though the <code>Rectangle2D</code> does not intersect the
     * <code>Shape</code>. 
     * @param r the specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>Shape</code> and  
     * 		the interior of the specified <code>Rectangle2D</code>
     *		intersect, or are both highly likely to intersect and intersection
     *		calculations would be too expensive to perform; <code>false</code>
     * 		otherwise.
     * @see #intersects(double, double, double, double)
     */
    public boolean intersects(Rectangle2D r) {
	return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns an iterator object that iterates along the 
     * <code>Shape</code> boundary and provides access to the geometry of the 
     * <code>Shape</code> outline.  If an optional {@link AffineTransform}
     * is specified, the coordinates returned in the iteration are
     * transformed accordingly.
     * <p>
     * Each call to this method returns a fresh <code>PathIterator</code>
     * object that traverses the geometry of the <code>Shape</code> object
     * independently from any other <code>PathIterator</code> objects in use
     * at the same time.
     * <p>
     * It is recommended, but not guaranteed, that objects 
     * implementing the <code>Shape</code> interface isolate iterations
     * that are in process from any changes that might occur to the original
     * object's geometry during such iterations.
     * <p>
     * Before using a particular implementation of the <code>Shape</code> 
     * interface in more than one thread simultaneously, refer to its 
     * documentation to verify that it guarantees that iterations are isolated 
     * from modifications.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or 
     *		<code>null</code> if untransformed coordinates are desired
     * @return a new <code>PathIterator</code> object, which independently    
     *		traverses the geometry of the <code>Shape</code>.
     */
    public PathIterator getPathIterator(AffineTransform at) {
	return new ZAreaPathIterator(this, at);
    }

    /**
     * Returns an iterator object that iterates along the <code>Shape</code>
     * boundary and provides access to a flattened view of the
     * <code>Shape</code> outline geometry.
     * <p>
     * Only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point types are
     * returned by the iterator.
     * <p>
     * If an optional <code>AffineTransform</code> is specified,
     * the coordinates returned in the iteration are transformed
     * accordingly.
     * <p>
     * The amount of subdivision of the curved segments is controlled
     * by the <code>flatness</code> parameter, which specifies the
     * maximum distance that any point on the unflattened transformed
     * curve can deviate from the returned flattened path segments.
     * Note that a limit on the accuracy of the flattened path might be
     * silently imposed, causing very small flattening parameters to be
     * treated as larger values.  This limit, if there is one, is
     * defined by the particular implementation that is used.
     * <p>
     * Each call to this method returns a fresh <code>PathIterator</code>
     * object that traverses the <code>Shape</code> object geometry 
     * independently from any other <code>PathIterator</code> objects in use at
     * the same time.
     * <p>
     * It is recommended, but not guaranteed, that objects 
     * implementing the <code>Shape</code> interface isolate iterations
     * that are in process from any changes that might occur to the original
     * object's geometry during such iterations.
     * <p>
     * Before using a particular implementation of this interface in more
     * than one thread simultaneously, refer to its documentation to
     * verify that it guarantees that iterations are isolated from
     * modifications.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or 
     *		<code>null</code> if untransformed coordinates are desired
     * @param flatness the maximum distance that the line segments used to
     *          approximate the curved segments are allowed to deviate
     *          from any point on the original curve
     * @return a new <code>PathIterator</code> that independently traverses 
     * 		the <code>Shape</code> geometry.
    */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
	return getPathIterator(at);
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	return bounds.toString();
    }
}

class ZAreaPathIterator implements PathIterator {
    int index = 0;      // Index to element of area we are iterating over
    ZArea area;         // Area this is an iterator over
    AffineTransform at; // Affine transform to modify path by (or null if none)

    /**
     * Constructor for new ZAreaPathIterator.
     */
    public ZAreaPathIterator(ZArea area, AffineTransform at) {
	this.area = area;
	this.at = at;
    }

    /**
     * Returns the winding rule for determining the interior of the
     * path.
     * @return the winding rule.
     * @see PathIterator#WIND_EVEN_ODD
     * @see PathIterator#WIND_NON_ZERO
     */
    public int getWindingRule() {
	return PathIterator.WIND_EVEN_ODD;
    }

    /**
     * Tests if the iteration is complete.
     * @return <code>true</code> if all the segments have 
     * been read; <code>false</code> otherwise.
     */
    public boolean isDone() {
	if (index == 5) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    public void next() {
	if (!isDone()) {
	    index++;
	}
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
     * A float array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path segment type of the current path segment.
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
	boolean requestTransform = false;
	int type = SEG_MOVETO;
	Rectangle2D bounds = area.getBounds2D();

	switch (index) {
	case 0:
	    coords[0] = (float)bounds.getX();
	    coords[1] = (float)bounds.getY();
	    requestTransform = true;
	    type = SEG_MOVETO;
	    break;
	case 1:
	    coords[0] = (float)(bounds.getX() + bounds.getWidth());
	    coords[1] = (float)bounds.getY();
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 2:
	    coords[0] = (float)(bounds.getX() + bounds.getWidth());
	    coords[1] = (float)(bounds.getY() + bounds.getHeight());
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 3:
	    coords[0] = (float)bounds.getX();
	    coords[1] = (float)(bounds.getY() + bounds.getHeight());
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 4:
	    type = SEG_CLOSE;
	    break;
	}

				// Transform points if requested
	if (requestTransform && (at != null)) {
	    at.transform(coords, 0, coords, 0, 1);
	}

	return type;
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A double array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * SEG_QUADTO returns two points,
     * SEG_CUBICTO returns 3 points
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
	boolean requestTransform = false;
	int type = SEG_MOVETO;
	Rectangle2D bounds = area.getBounds2D();

	switch (index) {
	case 0:
	    coords[0] = bounds.getX();
	    coords[1] = bounds.getY();
	    requestTransform = true;
	    type = SEG_MOVETO;
	    break;
	case 1:
	    coords[0] = bounds.getX() + bounds.getWidth();
	    coords[1] = bounds.getY();
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 2:
	    coords[0] = bounds.getX() + bounds.getWidth();
	    coords[1] = bounds.getY() + bounds.getHeight();
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 3:
	    coords[0] = bounds.getX();
	    coords[1] = bounds.getY() + bounds.getHeight();
	    requestTransform = true;
	    type = SEG_LINETO;
	    break;
	case 4:
	    type = SEG_CLOSE;
	    break;
	}

				// Transform points if requested
	if (requestTransform && (at != null)) {
	    at.transform(coords, 0, coords, 0, 1);
	}

	return type;
    }
}
