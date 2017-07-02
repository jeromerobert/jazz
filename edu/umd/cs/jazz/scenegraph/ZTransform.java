/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.util.*;
import java.awt.geom.*;
import java.io.IOException;

import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * ZTransform provides the capabilities of the AffineTransform plus some extra manipulators
 * on the transform.  ZTransform is also aware of the fact that it is a composite part of
 * a scenegraph object and notifies the associated object via a damage call whenever the 
 * transform changes state.  A ZTRansform is not obligated to have a parent object.
 * <p>
 * Where Operation is one of (translate, scale, or rotate) ZTransform provides the following matrix
 * of manipulators (the methods with mils as an arg will animate to the final state):
 * <pre>
 * operation(args)		operationTo(args)
 * operation(args, mils)        operationTo(args, mils)
 * </pre>
 * ZTransform also provides a getOperation for each Operation that returns the relevant portion of
 * of the transform.
 * <p>
 * In order to use ZTransform effectively, it is important to have a solid understanding
 * of affine transforms, matrix multiplication, and the standard use of matrices for
 * graphics coordinate systems.  The best place to start is by reading the documentation
 * on {@link java.awt.geom.AffineTransform}.  After that, a good next bit is to read
 * a standard 3D graphics text such as "Interactive Computer Graphics: a Top-Down Approach
 * with OpenGL" 2nd Edition by Angel (Addison-Wesley), or "Computer Graphics: Principles
 * and Practice, Second Edition in C" by Foley, van Dam, Feiner, Hughes, and Phillips
 * (Addison-Wesley).
 * <p>
 * Here is a very short lesson on matrices and graphics.  An 2D affine transform is typically
 * represented by a 3x3 matrix which we typically call M, and sometimes denote [M].  An
 * affine transform can represent any 2D translation, scale, rotation, shear or any combination
 * of these 4 operators.  Matrices which are pure translations, scales, and rotations are
 * typically denoted T, S, or R ([T], [S], or [R]), respectively.  The identity matrix
 * is typically denoted I or [I].
 * <p>
 * The reason these transforms are so powerful is because they can be combined in a
 * semantically straightforward way by simply concatenating the matrices.  Creating
 * a ZTransform creates I.  Calling one of the transformation methods on a transform is exactly equivalent to
 * concatenating the transform with a new transform that specifies the transformation.
 * Thus, transform.translate(dx, dy) is equivalent to [M][T] where M represents the original
 * transform, and T represents the translation matrix of dx, dy.  Similarly, transform.scale(ds)
 * is equivalent to [M][S].  And, these build up, so
 * <pre>
 *    ZTransform t = new ZTransform();
 *    t.scale(ds);
 *    t.translate(dx, dy);
 *    t.rotate(Math.PI * 0.5f);
 * </pre>
 * is equivalent to these four matrices concatenated together with standard
 * matrix multiplication [I][S][T][R]
 * <p>
 * However, a crucial place for confusion with these transforms is that the transforms
 * get applied in the <em>reverse</em> order of how you placed the calls to the transforms
 * in your code.  To understand this, you must realize that the object paint methods
 * get called <em>after</em> the transformations are applied.  Your paint methods specify
 * geometry (such as points) which get transformed by the current transformation before
 * being painted.  For a simple example, think of a point P.  Well, the point 
 * post-multiplies the current transformation.  In the example above, that works out
 * to a new point P' being computed as P' = [S][T][R]P.  You can think about this
 * as the original point P first getting multiplied (on the left) by R, then the result
 * gets multipled (on the left) by T, and that gets multiplied (on the left) by S.
 * <p>
 * Let's go through a simple example with actual numbers.  Suppose you want to take a
 * rectangle at (0, 0) with width 50 and height 50, and first translate it 50 units to the right, 
 * and then scale the whole thing by 2 about the origin.  
 * The result should be that the rectangle actually gets rendered at (100, 0) with dimensions
 * of (100x100).  The following code in Jazz implements this example.
 * <pre>
 *    ZRectangle rect = new ZRectangle(0, 0, 50, 50);
 *    ZNode node = new ZNode(rect);
 *    drawingLayer.addChild(node);
 *
 *				// Note how we call scale first even though
 *				// the translation will actually be applied before the scale.
 *    node.getTransform().scale(2);
 *    node.getTransform().translate(50, 0);
 * </pre>
 * <p>
 * Sometimes it is useful to transform an object in global coordinates - even though
 * that object has a transform of its own.  For instance, suppose you are implementing an
 * event handler for selection, and want to move an object so that it follows the pointer.
 * In order to do this, you need to translate the object in <em>global</em> coordinates.
 * Since the node you want to move may have a transform already (for instance, it may be scaled), 
 * if you simply translate the object, that transform will be applied <em>after</em> the
 * scale, and thus the translation will be modified by the scale.  That is, the object
 * may have the matrix [M].  Calling translate will generate [M][T].  If M represents a scale of 2,
 * then the translation will actually translate twice as much as you intended.  
 * <p>
 * The solution
 * is to <em>pre-concatenate</em> the translation so you end up with [T][M].  With ZTransform,
 * you can preconcatenate an affine transform onto the current transform.  So, the
 * code for this translation problem would like:
 * <pre>
 *    AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);
 *    node.getTransform().preConcatenate(tx);
 * </pre>
 *
 * @see java.awt.geom.AffineTransform
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 */
public class ZTransform implements ZSerializable, Cloneable {
    protected AffineTransform transform;
    protected ZScenegraphObject parent;
    
    /**
     * Create an ZTransform based on an identity AffineTransform
     */
    public ZTransform() {
	transform = new AffineTransform();
    }

    /**
     * Create an ZTransform based on AffineTransform parameter
     * @param <code>at</code>
     */
    public ZTransform(AffineTransform at) {
	transform = at;
    }

    /** 
     * Constructs a new ZTransform that is a copy of the specified ZTransform (i.e., a "copy constructor").
     * The portion of the reference ZTransform that is duplicated is that necessary to reuse the ZTransform
     * elsewhere, and so the new transform does not have a parent, 
     */
    public ZTransform(ZTransform xf) {
	transform = (AffineTransform)xf.transform.clone();
	parent = null;
    }

    /**
     * Duplicates the current ZTransform by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     *
     * @see #ZTransform(ZTransform)
     */
    public Object clone() {
	return new ZTransform(this);
    }

    /**
     * Get copy of affine transform stored in the ZTransform.
     */
    public AffineTransform getAffineTransform() {
	return new AffineTransform(transform);
    }

    /**
     * Return the type of transform as specified by AffineTransform.
     * @see AffineTransform#getType()
     */
    public int getType() {
	return transform.getType();
    }
    
    /**
     * Create inverse ZTransform.
     */
    public ZTransform createInverse() {
	ZTransform inverse = null;
	try {
	    inverse = new ZTransform(transform.createInverse());
	} catch (NoninvertibleTransformException e) {
	    System.out.println("ZTransform.createInverse: Error creating transform inverse");
	}
	return inverse;
    }
    
    /**
     * Create inverse affine transform stored in the ZTransform.
     */
    public AffineTransform createInverseAffineTransform() {
	AffineTransform inverse = null;
	try {
	    inverse = transform.createInverse();
	} catch (NoninvertibleTransformException e) {
	    System.out.println("ZTransform.createInverse: Error creating transform inverse");
	}
	return inverse;
    }

    /**
     * Set the underlying affine transform that this ZTransform wraps.
     * This call results in the scenegraph object associated with this transform
     * to be damaged, and it and its children's bounds to be updated.
     * The transform passed in is copied, and thus can be used by the caller
     * afterwards without affecting this ZTransform.
     * @param tx The new affine transform
     */
    public void setAffineTransform(AffineTransform tx) {
	if (parent != null) {
	    parent.damage();
	}
	transform.setTransform(tx);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }
    
    /**
     * Set the underlying affine transform that this ZTransform wraps.
     * This call results in the scenegraph object associated with this transform
     * to be damaged, and it and its children's bounds to be updated.
     * The transform passed in is copied, and thus can be used by the caller
     * afterwards without affecting this ZTransform.
     * @param m00,&nbsp;m01,&nbsp;m02,&nbsp;m10,&nbsp;m11,&nbsp;m12 the
     * 6 floating point values that compose the 3x3 transformation matrix
     */
    public void setAffineTransform(double m00, double m10, 
				   double m01, double m11,
				   double m02, double m12) {
	if (parent != null) {
	    parent.damage();
	}
	transform.setTransform(m00, m10, m01, m11, m02, m12);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }
    
    /**
     * Concatenates an <code>AffineTransform</code> <code>tx</code> to
     * this <code>AffineTransform</code> Cx in the most commonly useful
     * way to provide a new user space
     * that is mapped to the former user space by <code>Tx</code>.
     * Cx is updated to perform the combined transformation.
     * Transforming a point p by the updated transform Cx' is
     * equivalent to first transforming p by <code>Tx</code> and then
     * transforming the result by the original transform Cx like this:
     * Cx'(p) = Cx(Tx(p))  
     * In matrix notation, if this transform Cx is
     * represented by the matrix [this] and <code>Tx</code> is represented
     * by the matrix [Tx] then this method does the following:
     * <pre>
     *		[this] = [this] x [Tx]
     * </pre>
     * @param Tx the <code>AffineTransform</code> object to be
     * concatenated with this <code>AffineTransform</code> object.
     * @see #preConcatenate
     */
    public void concatenate(AffineTransform tx) {
	if (parent != null) {
	    parent.damage();
	}
	transform.concatenate(tx);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }

    /**
     * Concatenates an <code>AffineTransform</code> <code>Tx</code> to
     * this <code>AffineTransform</code> Cx
     * in a less commonly used way such that <code>Tx</code> modifies the
     * coordinate transformation relative to the absolute pixel
     * space rather than relative to the existing user space.
     * Cx is updated to perform the combined transformation.
     * Transforming a point p by the updated transform Cx' is
     * equivalent to first transforming p by the original transform
     * Cx and then transforming the result by 
     * <code>Tx</code> like this: 
     * Cx'(p) = Tx(Cx(p))  
     * In matrix notation, if this transform Cx
     * is represented by the matrix [this] and <code>Tx</code> is
     * represented by the matrix [Tx] then this method does the
     * following:
     * <pre>
     *		[this] = [Tx] x [this]
     * </pre>
     * @param Tx the <code>AffineTransform</code> object to be
     * concatenated with this <code>AffineTransform</code> object.
     * @see #concatenate
     */
    public void preConcatenate(AffineTransform tx) {
	if (parent != null) {
	    parent.damage(true);
	}
	transform.preConcatenate(tx);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }

    /**
     * Retrieves the 6 specifiable values in the 3x3 affine transformation
     * matrix that this ZTransform wraps,
     * and places them into an array of double precisions values.
     * The values are stored in the array as 
     * {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;m02&nbsp;m12&nbsp;}.
     * An array of 4 doubles can also be specified, in which case only the
     * first four elements representing the non-transform
     * parts of the array are retrieved and the values are stored into 
     * the array as {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;}
     * @param flatmatrix the double array used to store the returned
     * values.
     */
    public void getMatrix(double[] flatmatrix) {
	transform.getMatrix(flatmatrix);
    }

    public ZScenegraphObject getParent() {
	return parent;
    }

    public void setParent(ZScenegraphObject aParent) {
	parent = aParent;
    }

    /**
     * Transforms the specified <code>ptSrc</code> and stores the result
     * in <code>ptDst</code>.
     * If <code>ptDst</code> is <code>null</code>, a new {@link Point2D}
     * object is allocated and then the result of the transformation is
     * stored in this object.
     * In either case, <code>ptDst</code>, which contains the
     * transformed point, is returned for convenience.
     * If <code>ptSrc</code> and <code>ptDst</code> are the same
     * object, the input point is correctly overwritten with
     * the transformed point.
     * @param ptSrc the specified <code>Point2D</code> to be transformed
     * @param ptDst the specified <code>Point2D</code> that stores the
     * result of transforming <code>ptSrc</code>
     * @return the <code>ptDst</code> after transforming
     * <code>ptSrc</code> and storing the result in <code>ptDst</code>.
     */
    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
	return transform.transform(ptSrc, ptDst);
    }

    /**
     * Transforms an array of point objects by this transform.
     * If any element of the <code>ptDst</code> array is
     * <code>null</code>, a new <code>Point2D</code> object is allocated
     * and stored into that element before storing the results of the
     * transformation.
     * <p>
     * Note that this method does not take any precautions to
     * avoid problems caused by storing results into <code>Point2D</code>
     * objects that will be used as the source for calculations
     * further down the source array.
     * This method does guarantee that if a specified <code>Point2D</code> 
     * object is both the source and destination for the same single point
     * transform operation then the results will not be stored until
     * the calculations are complete to avoid storing the results on
     * top of the operands.
     * If, however, the destination <code>Point2D</code> object for one
     * operation is the same object as the source <code>Point2D</code> 
     * object for another operation further down the source array then
     * the original coordinates in that point are overwritten before
     * they can be converted.
     * @param ptSrc the array containing the source point objects
     * @param ptDst the array into which the transform point objects are
     * returned
     * @param srcOff the offset to the first point object to be
     * transformed in the source array
     * @param dstOff the offset to the location of the first
     * transformed point object that is stored in the destination array
     * @param numPts the number of point objects to be transformed
     */
    public void transform(Point2D[] ptSrc, int srcOff,
			  Point2D[] ptDst, int dstOff,
			  int numPts) {
	transform.transform(ptSrc, srcOff, ptDst, dstOff, numPts);
    }

    /**
     * Transforms an array of floating point coordinates by this transform.
     * The two coordinate array sections can be exactly the same or
     * can be overlapping sections of the same array without affecting the
     * validity of the results.
     * This method ensures that no source coordinates are overwritten by a
     * previous operation before they can be transformed.
     * The coordinates are stored in the arrays starting at the specified
     * offset in the order <code>[x0, y0, x1, y1, ..., xn, yn]</code>.
     * @param ptSrc the array containing the source point coordinates.
     * Each point is stored as a pair of x,&nbsp;y coordinates.
     * @param ptDst the array into which the transformed point coordinates
     * are returned.  Each point is stored as a pair of x,&nbsp;y
     * coordinates.
     * @param srcOff the offset to the first point to be transformed
     * in the source array
     * @param dstOff the offset to the location of the first
     * transformed point that is stored in the destination array
     * @param numPts the number of points to be transformed
     */
    public void transform(float[] srcPts, int srcOff,
			  float[] dstPts, int dstOff,
			  int numPts) {
	transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms an array of double precision coordinates by this transform.
     * The two coordinate array sections can be exactly the same or
     * can be overlapping sections of the same array without affecting the
     * validity of the results.
     * This method ensures that no source coordinates are
     * overwritten by a previous operation before they can be transformed.
     * The coordinates are stored in the arrays starting at the indicated
     * offset in the order <code>[x0, y0, x1, y1, ..., xn, yn]</code>.
     * @param srcPts the array containing the source point coordinates.
     * Each point is stored as a pair of x,&nbsp;y coordinates.
     * @param dstPts the array into which the transformed point
     * coordinates are returned.  Each point is stored as a pair of
     * x,&nbsp;y coordinates.
     * @param srcOff the offset to the first point to be transformed
     * in the source array
     * @param dstOff the offset to the location of the first
     * transformed point that is stored in the destination array
     * @param numPts the number of point objects to be transformed
     */
    public void transform(double[] srcPts, int srcOff,
			  double[] dstPts, int dstOff,
			  int numPts) {
	transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms an array of floating point coordinates by this transform
     * and stores the results into an array of doubles.
     * The coordinates are stored in the arrays starting at the specified
     * offset in the order <code>[x0, y0, x1, y1, ..., xn, yn]</code>.
     * @param ptSrc the array containing the source point coordinates.
     * Each point is stored as a pair of x,&nbsp;y coordinates.
     * @param ptDst the array into which the transformed point coordinates
     * are returned.  Each point is stored as a pair of x,&nbsp;y
     * coordinates.
     * @param srcOff the offset to the first point to be transformed
     * in the source array
     * @param dstOff the offset to the location of the first
     * transformed point that is stored in the destination array
     * @param numPts the number of points to be transformed
     */
    public void transform(float[] srcPts, int srcOff,
			  double[] dstPts, int dstOff,
			  int numPts) {
	transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms an array of double precision coordinates by this transform
     * and stores the results into an array of floats.
     * The coordinates are stored in the arrays starting at the specified
     * offset in the order <code>[x0, y0, x1, y1, ..., xn, yn]</code>.
     * @param srcPts the array containing the source point coordinates.
     * Each point is stored as a pair of x,&nbsp;y coordinates.
     * @param dstPts the array into which the transformed point
     * coordinates are returned.  Each point is stored as a pair of 
     * x,&nbsp;y coordinates.
     * @param srcOff the offset to the first point to be transformed
     * in the source array
     * @param dstOff the offset to the location of the first
     * transformed point that is stored in the destination array
     * @param numPts the number of point objects to be transformed
     */
    public void transform(double[] srcPts, int srcOff,
			  float[] dstPts, int dstOff,
			  int numPts) {
	transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Transforms the specified <code>rectSrc</code> and stores the result
     * in <code>rectDst</code>.
     * If <code>rectDst</code> is <code>null</code>, a new {@link Rectangle2D}
     * object is allocated and then the result of the transformation is
     * stored in this object.
     * In either case, <code>rectDst</code>, which contains the
     * transformed point, is returned for convenience.
     * If <code>rectSrc</code> and <code>rectDst</code> are the same
     * object, the input rectangle is correctly overwritten with
     * the transformed rectangle.
     * @param rectSrc the specified <code>Rectangle2D</code> to be transformed
     * @param rectDst the specified <code>Rectangle2D</code> that stores the
     * result of transforming <code>rectSrc</code>
     * @return the <code>rectDst</code> after transforming
     * <code>rectSrc</code> and storing the result in <code>recttDst</code>.
     */
    public Rectangle2D transform(Rectangle2D rectSrc, Rectangle2D rectDst) {
	Rectangle2D dest;

				// First, transform all 4 corners of the rectangle
	float[] pts = new float[8];
	pts[0] = (float)rectSrc.getX();          // top left corner
	pts[1] = (float)rectSrc.getY();
	pts[2] = (float)(rectSrc.getX() + rectSrc.getWidth());  // top right corner
	pts[3] = (float)rectSrc.getY();
	pts[4] = (float)(rectSrc.getX() + rectSrc.getWidth());  // bottom right corner
	pts[5] = (float)(rectSrc.getY() + rectSrc.getHeight());
	pts[6] = (float)rectSrc.getX();          // bottom left corner
	pts[7] = (float)(rectSrc.getY() + rectSrc.getHeight());
	transform.transform(pts, 0, pts, 0, 4);

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

	rectDst.setRect(minX, minY, maxX - minX, maxY - minY);

	return rectDst;
    }

    public void inverseTransform(Point2D srcPt, Point2D destPt) {
 	try {
 	    transform.inverseTransform(srcPt, destPt);
 	}
 	catch (NoninvertibleTransformException e) {
 	    System.out.println("Warning: jazz.scenegraph.ZTransform.inverseTransform: Error inverting point");
 	}
    }

    public void inverseTransform(Rectangle2D srcRect, Rectangle2D destRect) {
 	try {
	    Point2D p1 = new Point2D.Float((float)srcRect.getX(), (float)srcRect.getY());
	    Point2D p2 = new Point2D.Float((float)(srcRect.getX() + srcRect.getWidth()), 
					   (float)(srcRect.getY() + srcRect.getHeight()));
	    Point2D p3 = new Point2D.Float();
	    Point2D p4 = new Point2D.Float();
 	    transform.inverseTransform(p1, p3);
 	    transform.inverseTransform(p2, p4);
	    destRect.setRect(p3.getX(), p3.getY(), p4.getX() - p3.getX(), p4.getY() - p3.getY());
 	}
 	catch (NoninvertibleTransformException e) {
 	    System.out.println("Warning: jazz.scenegraph.ZTransform.inverseTransform: Error inverting rectangle");
 	}
    }

    public void inverseTransform(Area area) {
 	try {
 	    area.transform(transform.createInverse());
 	}
 	catch (NoninvertibleTransformException e) {
 	    System.out.println("Warning: jazz.scenegraph.ZTransform.inverseTransform: Error inverting point");
 	}
    }

    /**
     * Returns the current translation of this object
     * @return the translation
     */
    public Point2D getTranslation() {
	Point2D pt = new Point2D.Float((float)transform.getTranslateX(), (float)transform.getTranslateY());
	return pt;
    }
    
    /**
     * Translate the object by the specified deltaX and deltaY
     * @param dx X-coord of translation
     * @param dy Y-coord of translation
     */
    public void translate(float dx, float dy) {
	if (parent != null) {
	    parent.damage();
	}
        transform.translate(dx, dy);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }   

    /**
     * Animate the object from its current position by the specified deltaX and deltaY
     * @param dx X-coord of translation
     * @param dy Y-coord of translation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void translate(float dx, float dy, int millis, ZSurface surface) {
	AffineTransform tx = new AffineTransform(transform);
        tx.translate(dx, dy);
	animate(tx, millis, surface);
    }
    
    /**
     * Translate the object to the specified position
     * @param x X-coord of translation
     * @param y Y-coord of translation
     */
    public void translateTo(float x, float y) {
        double[] mat = new double[6];
	transform.getMatrix(mat);
	mat[4] = x;
	mat[5] = y;

	if (parent != null) {
	    parent.damage();
	}
        transform.setTransform(mat[0], mat[1], mat[2], mat[3], mat[4], mat[5]);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }

    /**
     * Animate the object from its current position to the positino specified
     * by x, y
     * @param x X-coord of translation
     * @param y Y-coord of translation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void translateTo(float x, float y, int millis, ZSurface surface) {
	AffineTransform tx = new AffineTransform(transform);
	double[] mat = new double[6];

	tx.translate(x, y);
	tx.getMatrix(mat);
	mat[4] = x;
	mat[5] = y;
	tx.setTransform(mat[0], mat[1], mat[2], mat[3], mat[4], mat[5]);
	animate(tx, millis, surface);
    }
    
    
    /**
     * Returns the current scale of this transform.
     * Note that this is implemented by applying the transform to a diagonal
     * line and returning the length of the resulting line.  If the transform
     * is sheared, or has a non-uniform scaling in X and Y, the results of
     * this method will be ill-defined.
     * @return the scale
     */
    public float getScale() {
	Point2D p1 = new Point2D.Float(0.0f, 0.0f);
	Point2D p2 = new Point2D.Float(1.0f, 1.0f);
	float origDist = (float)p1.distance(p2);
	transform.transform(p1, p1);
	transform.transform(p2, p2);
	float finalDist = (float)p1.distance(p2);

	return finalDist / origDist;
    }
    
    /**
     * Scale the object from its current scale to the scale specified
     * by muliplying the current scale and dz.
     * @param dz scale factor
     */
    public void scale(float dz) {
	if (parent != null) {
	    parent.damage();
	}
	transform.scale(dz, dz);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }
    
    /**
     * Scale the object around the specified point (x, y) 
     * from its current scale to the scale specified
     * by muliplying the current scale and dz.
     * @param dz scale factor
     * @param x X coordinate of the point to scale around
     * @param y Y coordinate of the point to scale around
     */
    public void scale(float dz, float x, float y) {
	if (parent != null) {
	    parent.damage();
	}
	transform.translate(x, y);
	transform.scale(dz, dz);
	transform.translate(-x, -y);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }
    
    /**
     * Animate the object from its current scale to the scale specified
     * by muliplying the current scale and deltaZ
     * @param dz scale factor
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void scale(float dz, int millis, ZSurface surface) {
	AffineTransform tx = new AffineTransform(transform);
	tx.scale(dz, dz);
	animate(tx, millis, surface);
    }    
    
    /**
     * Animate the object around the specified point (x, y)
     * from its current scale to the scale specified
     * by muliplying the current scale and dz
     * @param dz scale factor
     * @param x X coordinate of the point to scale around
     * @param y Y coordinate of the point to scale around
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void scale(float dz, float x, float y, int millis, ZSurface surface) {
	AffineTransform tx = new AffineTransform(transform);
	tx.translate(x, y);
	tx.scale(dz, dz);
	tx.translate(-x, -y);
	animate(tx, millis, surface);
    }

    /**
     * Set the scale of the object to the specified target scale.
     * @param finalz final scale factor
     */
    public void scaleTo(float finalz) {
	float dz = finalz / getScale();
	scale(dz);
    }
    
    /**
     * Set the scale of the object to the specified target scale,
     * scaling the object around the specified point (x, y).
     * @param finalz scale factor
     * @param x X coordinate of the point to scale around
     * @param y Y coordinate of the point to scale around
     */
    public void scaleTo(float finalz, float x, float y) {
	float dz = finalz / getScale();
	scale(dz, x, y);
    }
    
    /**
     * Animate the object from its current scale to the specified target scale.
     * @param finalz scale factor
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void scaleTo(float finalz, int millis, ZSurface surface) {
	float dz = finalz / getScale();
	scale(dz, millis, surface);
    }    
    
    /**
     * Animate the object around the specified point (x, y)
     * to the specified target scale.
     * @param finalz scale factor
     * @param x X coordinate of the point to scale around
     * @param y Y coordinate of the point to scale around
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void scaleTo(float finalz, float x, float y, int millis, ZSurface surface) {
	float dz = finalz / getScale();
	scale(dz, x, y, millis, surface);
    }

    /**
     * Returns the current rotation (Z-axis) of this object
     * Not currently immplemented BDMREV
     * @return the scale
     */
    public float getRotation() {
	return 0;
    }
    
    /**
     * Rotate the object by the specified amount
     * @param theta angle to rotate (in radians)
     */
    public void rotate(float theta) {
	rotate(theta, 0.0f, 0.0f);
    }
    
    /**
     * Rotate the object by the specified amount around the specified anchor point
     * @param theta angle to rotate (in radians)
     * @param xctr X-coord of anchor point
     * @param yctr Y-coord of anchor point
     */
    public void rotate(float theta, float xctr, float yctr) {
	if (parent != null) {
	    parent.damage();
	}
	transform.rotate(theta, xctr, yctr);
	if (parent != null) {
	    parent.updateChildBounds();
	    parent.damage();
	}
    }
    
    /**
     * Rotate the object, via animation, theta radians
     * @param theta angle to rotate (in radians)
     * @param millis Time to animate scale in milliseconds
     * @param surface The surface to updated during animation.
     */
    public void rotate(float theta, int millis, ZSurface surface) {
	rotate(theta, 0.0f, 0.0f, millis, surface);
    }
    
   /**
     * Rotate the object, via animation, theta radians about the specified anchor point
     * @param theta angle to rotate (in radians)
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
     public void rotate(float theta, float xctr, float yctr, int millis, ZSurface surface) {
	AffineTransform tx = new AffineTransform(transform);
	tx.rotate(theta, xctr, yctr);
	animate(tx, millis, surface);
    }

    /**
     * This will calculate the necessary transform in order to make this
     * transform's corresponding node appear at a particular position relative to the
     * specified node.  The source point specifies a point in the
     * unit square (0, 0) - (1, 1) that represents an anchor point on the
     * corresponding node to this transform.  The destination point specifies
     * an anchor point on the reference node.  The position method then
     * computes the transform that results in transforming this node so that
     * the source anchor point coincides with the reference anchor
     * point. This can be useful for layout algorithms as it is
     * straightforward to position one object relative to another.  
     * <p>
     * For example, If you have two nodes, A and B, and you call
     * <PRE>
     * Point2D srcPt = new Point2D.Float(1.0f, 0.0f);
     * Point2D destPt = new Point2D.Float(0.0f, 0.0f);
     * A.position(srcPt, destPt, B, 750);
     * </PRE>
     * The result is that A will move so that its upper-right corner is at
     * the same place as the upper-left corner of B, and the transition will
     * be smoothly animated over a period of 750 milliseconds.
     * @param srcPt The anchor point on this transform's node (normalized to a unit square)
     * @param destPt The anchor point on destination bounds (normalized to a unit square)
     * @param destBounds The bounds used to calculate this transform's node
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */

    public void position(Point2D srcPt, Point2D destPt, ZNode refNode, int millis, ZSurface surface) {
	position(srcPt, destPt, refNode.getCompBounds(), millis, surface);
    }

    /**
     * This will calculate the necessary transform in order to make this
     * transform's corresponding node appear at a particular position relative to the
     * specified bounding box.  The source point specifies a point in the
     * unit square (0, 0) - (1, 1) that represents an anchor point on the
     * corresponding node to this transform.  The destination point specifies
     * an anchor point on the reference node.  The position method then
     * computes the transform that results in transforming this node so that
     * the source anchor point coincides with the reference anchor
     * point. This can be useful for layout algorithms as it is
     * straightforward to position one object relative to another.  
     * <p>
     * For example, If you have two nodes, A and B, and you call
     * <PRE>
     * Point2D srcPt = new Point2D.Float(1.0f, 0.0f);
     * Point2D destPt = new Point2D.Float(0.0f, 0.0f);
     * A.position(srcPt, destPt, B.getGlobalBounds(), 750);
     * </PRE>
     * The result is that A will move so that its upper-right corner is at
     * the same place as the upper-left corner of B, and the transition will
     * be smoothly animated over a period of 750 milliseconds.
     * @param srcPt The anchor point on this transform's node (normalized to a unit square)
     * @param destPt The anchor point on destination bounds (normalized to a unit square)
     * @param destBounds The bounds used to calculate this transform's node
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void position(Point2D srcPt, Point2D destPt, Rectangle2D destBounds, int millis, ZSurface surface) {
	float srcx, srcy;
	float destx, desty;
	float dx, dy;
	AffineTransform tx;
	
	if (parent != null) {
	    ZNode node = parent.findNode();
	    Rectangle2D srcBounds = node.getCompBounds();
	    srcx = (float)ZUtil.lerp(srcPt.getX(), srcBounds.getX(), srcBounds.getX() + srcBounds.getWidth());
	    srcy = (float)ZUtil.lerp(srcPt.getY(), srcBounds.getY(), srcBounds.getY() + srcBounds.getHeight());
	    destx = (float)ZUtil.lerp(destPt.getX(), destBounds.getX(), destBounds.getX() + destBounds.getWidth());
	    desty = (float)ZUtil.lerp(destPt.getY(), destBounds.getY(), destBounds.getY() + destBounds.getHeight());
	    dx = destx - srcx;
	    dy = desty - srcy;
	    tx = new AffineTransform(transform);
	    tx.translate(dx, dy);
	    
	    animate(tx, millis, surface);
	}
    }
    
    /**
     * Set the transform of this object to the specified transform, 
     * and animate the change from its current transformation over the specified
     * number of milliseconds using a slow-in slow-out animation.
     * The surface specifies which surface should be updated during the animation.  
     * <p>
     * If millis is 0, then the transform is updated once, and the scene
     * is damaged, but not restored - and thus there are no visible changes
     * on any surface.  The caller must call surface.restore().
     * In this case, the surface is not used, and can be specified as null.
     * <p>
     * If this transform's parent is null, then there is nothing to animate,
     * and so the transform will just be set directly to the specified transform
     * without any animation.
     * @param tx Final transformation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void animate(AffineTransform tx, int millis, ZSurface surface) {
	double[] srcTx = new double[6];
	double[] currTx = new double[6];
	double[] destTx = new double[6];

	if (parent != null) {
	    ZNode node = parent.findNode();
	    
				// Extract initial transforms
	    transform.getMatrix(srcTx);
	    tx.getMatrix(destTx);
	    
	    if (millis > 0) {
		float lerp = millis / (1000.0f * 30.0f);   // Estimate first transition at 30 frames per second
		float straightLerp;
		float sisoLerp;
		long startTime;
		long elapsedTime;
		startTime = new Date().getTime();
		
				// Loop until animation time has completed
		do {
				// Computer new transform representing new step of animation
		    currTx[0] = ZUtil.lerp(lerp, srcTx[0], destTx[0]);
		    currTx[1] = ZUtil.lerp(lerp, srcTx[1], destTx[1]);
		    currTx[2] = ZUtil.lerp(lerp, srcTx[2], destTx[2]);
		    currTx[3] = ZUtil.lerp(lerp, srcTx[3], destTx[3]);
		    currTx[4] = ZUtil.lerp(lerp, srcTx[4], destTx[4]);
		    currTx[5] = ZUtil.lerp(lerp, srcTx[5], destTx[5]);
		    
				// Modify transform to reflect this step
		    setAffineTransform(currTx[0], currTx[1], currTx[2], currTx[3], currTx[4], currTx[5]);
		    
				// Force immediate render
		    surface.restore(true);
		    
				// Calculate total elapsed time
		    elapsedTime = new Date().getTime() - startTime;
		    
				// Calculate next step
		    straightLerp = (float)elapsedTime / millis;
		    sisoLerp = ZUtil.sisoLerp(straightLerp);
		    lerp = (sisoLerp > lerp) ? sisoLerp : lerp;  // Don't allow animation to move backwards
		} while (elapsedTime < millis);
	    }
	    
				// When finished animating, put object at exact final point in case of rounding error
	    node.damage();
	    transform.setTransform(destTx[0], destTx[1], destTx[2], destTx[3], destTx[4], destTx[5]);
	    node.updateChildBounds();
	    node.damage();
	    
				// Only restore if non-zero animation time specified
	    if (millis > 0) {
		surface.restore(true);
	    } else {
		surface.restore(false);
	    }
	} else {
	    transform.setTransform(tx);
	}
    }

    /**
     * Set the transforms of the specified array of nodes to the specified
     * array of transforms,
     * and animate the change over the specified
     * number of milliseconds using a slow-in slow-out animation.
     * The surface specifies which surface should be updated during the animation.
     * <p>
     * If the size of the nodes and txs arrays are not equal, then only those
     * nodes for which transforms are specified will be animated.  That is,
     * the smaller of the two array sizes will be used.
     * <p>
     * If millis is 0, then the transforms are updated once, and the scene
     * is damaged, but not restored - and thus there are no visible changes
     * on any surface.  The caller must call surface.restore().
     * In this case, the surface is not used, and can be specified as null.
     * <p>
     * The following code fragment demonstrates the use of this animate
     * method.  It creates three rectangles, and animates two of them
     * simultaneously.
     * <pre>
     *     ZRectangle rect1, rect2, rect3;
     *     ZNode node1, node2, node3;
     *
     *     rect1 = new ZRectangle(0, 0, 50, 50);
     *     rect1.setFillColor(Color.red);
     *     node1 = new ZNode(rect1);
     *     drawingLayer.addChild(node1);
     *
     *     rect2 = new ZRectangle(25, 25, 50, 50);
     *     rect2.setFillColor(Color.blue);
     *     node2 = new ZNode(rect2);
     *     drawingLayer.addChild(node2);
     *
     *     rect3 = new ZRectangle(100, 100, 50, 50);
     *     rect3.setFillColor(Color.orange);
     *     node3 = new ZNode(rect3);
     *     drawingLayer.addChild(node3);
     *
     *     ZNode[] nodes = new ZNode[2];
     *     nodes[0] = node1;
     *     nodes[1] = node2;
     *     AffineTransform[] txs = new AffineTransform[2];
     *     txs[0] = new AffineTransform();
     *     txs[0].scale(2.0f, 2.0f);
     *     txs[1] = new AffineTransform();
     *     txs[1].translate(100.0f, 25.0f);
     *     txs[1].scale(0.5f, 0.5f);
     *
     *     ZTransform.animate(nodes, txs, 1000, surface);
     * </pre>
     *
     * @param nodes The array of nodes to be animated
     * @param txs The array of final transformations of the nodes
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    static public void animate(ZNode[] nodes, AffineTransform[] txs, int millis, ZSurface surface) {
	int i;
	int len = Math.min(nodes.length, txs.length);
	double[][] srcTx = new double[len][6];
	double[][] currTx = new double[len][6];
	double[][] destTx = new double[len][6];
	ZNode node;

				// Extract initial transforms
	for (i=0; i<len; i++) {
	    nodes[i].getTransform().getMatrix(srcTx[i]);
	    txs[i].getMatrix(destTx[i]);
	}
	
	if (millis > 0) {
	    float lerp = millis / (1000.0f * 30.0f);   // Estimate first transition at 30 frames per second
	    float straightLerp;
	    float sisoLerp;
	    long startTime;
	    long elapsedTime;
	    startTime = new Date().getTime();

				// Loop until animation time has completed
	    do {
		for (i=0; i<len; i++) {
				// Computer new transform representing new step of animation
		    currTx[i][0] = ZUtil.lerp(lerp, srcTx[i][0], destTx[i][0]);
		    currTx[i][1] = ZUtil.lerp(lerp, srcTx[i][1], destTx[i][1]);
		    currTx[i][2] = ZUtil.lerp(lerp, srcTx[i][2], destTx[i][2]);
		    currTx[i][3] = ZUtil.lerp(lerp, srcTx[i][3], destTx[i][3]);
		    currTx[i][4] = ZUtil.lerp(lerp, srcTx[i][4], destTx[i][4]);
		    currTx[i][5] = ZUtil.lerp(lerp, srcTx[i][5], destTx[i][5]);
		    
				// Modify transform to reflect this step
		    nodes[i].getTransform().setAffineTransform(currTx[i][0], currTx[i][1], currTx[i][2], 
							       currTx[i][3], currTx[i][4], currTx[i][5]);
		}

				// Force immediate render
		surface.restore(true);
		
				// Calculate total elapsed time
		elapsedTime = new Date().getTime() - startTime;
		
				// Calculate next step
		straightLerp = (float)elapsedTime / millis;
		sisoLerp = ZUtil.sisoLerp(straightLerp);
		lerp = (sisoLerp > lerp) ? sisoLerp : lerp;  // Don't allow animation to move backwards
	    } while (elapsedTime < millis);
	}

				// When finished animating, put objects at exact final point in case of rounding error
	for (i=0; i<len; i++) {
	    nodes[i].getTransform().setAffineTransform(destTx[i][0], destTx[i][1], destTx[i][2], 
						       destTx[i][3], destTx[i][4], destTx[i][5]);
	}
				// Only restore immediately if non-zero animation time specified
	if (millis > 0) {
	    surface.restore(true);
	} else {
	    surface.restore(false);
	}
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String s = transform.toString();
	return super.toString() + s.substring(s.indexOf("["));
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
	double[] matrix = new double[6];
	transform.getMatrix(matrix);
	Vector v = new Vector();
	for (int i=0; i<6; i++) {
	    v.add(new Double(matrix[i]));
	}
	out.writeState("java.awt.geom.AffineTransform", "transform", v);
	
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
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
	if (fieldName.compareTo("transform") == 0) {
	    Vector v = (Vector) fieldValue;
	    Object[] tmp = v.toArray();
	    
	    if (parent != null) {
		parent.damage();
	    }
	    transform.setTransform(((Double)tmp[0]).doubleValue(), ((Double)tmp[1]).doubleValue(), ((Double)tmp[2]).doubleValue(),
				   ((Double)tmp[3]).doubleValue(), ((Double)tmp[4]).doubleValue(), ((Double)tmp[5]).doubleValue());
	    if (parent != null) {
		parent.updateChildBounds();
		parent.damage();
	    }
	}
    }
}
