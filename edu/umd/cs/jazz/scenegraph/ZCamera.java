/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.ImageObserver;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZCamera</b> represents a viewport onto a Scenegraph Node.
 * A camera can look anywhere onto a scenegraph scape as specified by
 * an arbitrary affine transformation.
 * A ZNode can have any number of cameras onto it.
 * Each camera can be associated with a top-level window,
 * a portal, or may not be mapped to any visible device at all.
 *
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 * @author  Maria E. Jump
 * @see     ZNode
 */

public class ZCamera extends ZNode {
    static public final Color  fillColor_DEFAULT = Color.white;

    protected ZTransform viewTransform;
    protected ZTransform inverseViewTransform = null;
    protected boolean    inverseViewTransformDirty = true;
    protected Vector     paintStartPoints;                 // Nodes that this camera looks at
    protected HashSet    dirty;
    protected Color      fillColor = fillColor_DEFAULT;

    protected ZArea      paintArea;                        // Some areas that gets reused within paint().  
    protected ZArea      paintVisibleArea;                 // Define here for efficiency

    protected Dimension2DFloat size;

    /**
     * The optional surface this camera is associated with.  If this surface 
     * is non-null, then the camera will render directly onto this surface.
     * Else, the camera will only be visible within other cameras that looks
     * at it.
     */
    protected ZSurface   surface;
    
    /**
     * Constructs a new ZCamera.  Camera's are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     */
    public ZCamera() {
	this(null, null);
    }

    /**
     * Constructs a new ZCamera.  Camera's are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * @param node The part of the scenegraph this camera sees.
     * @param surf The surface this top-level camera projects onto
     */
    public ZCamera(ZNode node, ZSurface aSurface) {
	surface = aSurface;
	size = new Dimension2DFloat();
        viewTransform = new ZTransform();
        viewTransform.setParent(this);
	paintStartPoints = new Vector();
	dirty  = new HashSet();
	paintArea = new ZArea();
	paintVisibleArea = new ZArea();
	if (node != null) {
	    paintStartPoints.add(node);
	    node.addCamera(this);
	}
    }

    /**
     * Add a portion of the scenegraph that what this camera sees
     * @param node The part of the scenegraph added to what this camera sees.
     */
    public void addPaintStartPoint(ZNode node) {
	paintStartPoints.add(node);
	node.addCamera(this);
	damage(node.getGlobalBounds());
    }

    /**
     * Removes a portion of the scenegrpah from what this camera sees
     * @param node The part of the scenegraph removed from what this camera sees.
     */
    public void removePaintStartPoint(ZNode node) {
	paintStartPoints.remove(node);
	node.removeCamera(this);
	damage(node.getGlobalBounds());
    }

    /**
     * Swaps the specified node out of the list of paint start points of this
     * camera, and replaces it with the specified node.
     * The replacement node will be added to paint start point list in the same
     * position as the original was.
     *
     * @param original is the old node that is being swapped out as a paint start point
     * @param replacement is the new node that is being swapped in as a paint start point
     */
    public void swapPaintStartPoint(ZNode original, ZNode replacement) {
	for (int i=0; i<paintStartPoints.size(); i++) {
	    if (paintStartPoints.get(i) == original) {
		damage(original.getGlobalBounds());
		paintStartPoints.setElementAt(replacement, i);
		original.removeCamera(this);
		replacement.addCamera(this);
		damage(replacement.getGlobalBounds());
		break;
	    }
	}
    }

    /**
     * Returns the <code>Scenegraph</code> that this camera looks onto.
     * @return Portion of scenegraph that is visible from this camera.
     */
    public Vector getPaintStartPoints() {
        return paintStartPoints;
    }

    /**
     * Returns the <code>Scenegraph</code> that is selected in this camera's view
     * @return Portion of scenegraph visible to this camera that is selected.
     */    
    public Vector getSelectedNodes() {
	Vector result = new Vector();
	
	result.addAll(this.getSelectedChildren());

	for (Iterator i=getPaintStartPoints().iterator(); i.hasNext();) {
	    ZNode node = (ZNode)i.next();
	    result.addAll(node.getSelectedChildren());
	}
	return result;
    }
    
    /**
       * Get the value of surface.
       * @return Value of surface.
       */
    public ZSurface getSurface() {
	return surface;
    }
    
    /**
       * Set the value of surface.
       * @param v  Value to assign to surface.
       */
    public void setSurface(ZSurface aSurface) {
	surface = aSurface;
    }

    /**
     * Returns the <code>ZTransform</code> associated with this camera.
     * @return The current camera transform.
     */
    public ZTransform getViewTransform() {
	return viewTransform;
    }

    /**
     * Compute the inverse camera transform based on the camera transform.
     * This is a protected method and gets called from within ZCamera
     * whenever the inverse camera transform cache has been invalidated,
     * and it is needed.
     */
    protected void computeInverseViewTransform() {
	inverseViewTransform = viewTransform.createInverse();
	inverseViewTransform.setParent(this);
	inverseViewTransformDirty = false;
    }
    
    /**
     * Returns the inverse <code>ZTransform</code> associated with this camera.
     * @return The current inverse camera transform.
     */
    public ZTransform getInverseViewTransform() {
	if (inverseViewTransformDirty) {
	    computeInverseViewTransform();
	}
	return inverseViewTransform;
    }
    
    /**
     * Sets the <code>ZTransform</code> associated with this camera.
     * @param transform
     */
    public void setViewTransform(ZTransform transform) {
	viewTransform = transform;
	viewTransform.setParent(this);
	inverseViewTransformDirty = true;
	damage();
    }

    /**
       * Get the value of fillColor.
       * @return Value of fillColor.
       */
    public Color getFillColor() {
	return fillColor;
    }
    
    /**
       * Set the value of fillColor.
       * @param v  Value to assign to fillColor.
       */
    public void setFillColor(Color aColor) {
	fillColor = aColor;
	damage();
    }

    /**
     * Animates the camera view so that the specified bounds is centered with the view of the camera.
     * @param <code>bounds</code> The bounds to be centered.
     * @param <code>millis</code> The time in milliseconds to perform the animation
     * @param <code>surface</code> The surface to be updated during the animation
     */
    public void center(Rectangle2D bounds, int millis, ZSurface aSurface) {
	AffineTransform tx = new AffineTransform();
				// First compute transform that will result in bounds being centered
	float dx = (float)(size.width / bounds.getWidth());
	float dy = (float)(size.height / bounds.getHeight());
	float scale = (dx < dy) ? dx : dy;
	float ctrX = (float)(0.5f * size.width);
	float ctrY = (float)(0.5f * size.height);
	float boundsX = (float)(bounds.getX() + (0.5f * bounds.getWidth()));
	float boundsY = (float)(bounds.getY() + (0.5f * bounds.getHeight()));
	
	tx.translate(ctrX + (- boundsX * scale), ctrY + (- boundsY * scale));
	tx.scale(scale, scale);

				// Then, change camera to new transform
	viewTransform.animate(tx, millis, aSurface);
    }
    
    /**
     * Computes the bounds of the camera as an object.
     * These bounds consist of its size and its visual component.
     * The bounds are stored in <code>globalBounds</code>
     */
    protected void computeGlobalBounds() {
	globalBoundsDirty = false;
	
	globalBounds.reset();

	globalBounds.add(new Rectangle2D.Float(0, 0, (float)size.getWidth(), (float)size.getHeight()));	
	globalBounds.add(getGlobalCompBounds());
	
	globalBounds.transform(computeGlobalCoordinateFrame());
    }
    
    /**
     * Returns the bounds that this Camera sees in global scene coordinates.
     * @return The bounds.
     */
    public ZBounds getViewBounds() {
	ZBounds viewBounds = (ZBounds)getGlobalBounds().clone();
	getInverseViewTransform().transform(viewBounds, viewBounds);
	return viewBounds;
    }

    /**
     * Sets the size of this camera.
     * @param size The new dimension of this camera
     */
    public void setSize(Dimension2D size) {
	setSize((float)size.getWidth(), (float)size.getHeight());
    }

    /**
     * Sets the size of this camera.
     * @param width The new width of this camera.
     * @param height The new height of this camera.
     */
    public void setSize(float width, float height) {
	size.setSize(width, height);
	updateGlobalBounds();
	damage();
    }

    /**
     * Returns the size of this camera.
     * @return The size.
     */
    public Dimension2D getSize() {
	return size;
    }

    /**
     * Returns the current magnification of this camera.
     * @return The magnification factor.
     */
    public float getMagnification() {
	return (float)viewTransform.getAffineTransform().getScaleX();
    }

    /**
     * This implements damage in just the same way as is described by ZNode.damage
     *
     * @see ZNode#damage()
     */
    public void damage() {
	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZCamera.damage: this = " + this);
	}

				// We cache the inverse camera transform, and so must invalidate
				// it whenever the camera transform changes.  Since it can be
				// changed from outside this class, we catch changes here since
				// all changes result in the camera being damaged.
	inverseViewTransformDirty = true;

				// When damaging this camera directly, we must keep track of
				// which cameras and surfaces are looking directly at this so
				// we can make sure we paint the full camera view bounds when
				// painting from those cameras and surfaces.
	for (Iterator i=findCameras().iterator(); i.hasNext();) {
	    ZCamera camera = (ZCamera)i.next();
	    dirty.add(camera);
	}
	if (surface != null) {
	    dirty.add(surface);
	}

	super.damage();
				// Send on damage request to surface as well
	if (surface != null) {
	    surface.damage(getViewBounds());
	}
    }

    /**
     * This implements damage in just the same way as is described by ZNode.damage
     *
     * @see ZNode#damage(boolean)
     */
    public void damage(boolean boundsChanged) {
	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZCamera.damage: this = " + this);
	}

				// We cache the inverse camera transform, and so must invalidate
				// it whenever the camera transform changes.  Since it can be
				// changed from outside this class, we catch changes here since
				// all changes result in the camera being damaged.
	inverseViewTransformDirty = true;

				// When damaging this camera directly, we must keep track of
				// which cameras and surfaces are looking directly at this so
				// we can make sure we paint the full camera view bounds when
				// painting from those cameras and surfaces.
	for (Iterator i=findCameras().iterator(); i.hasNext();) {
	    ZCamera camera = (ZCamera)i.next();
	    dirty.add(camera);
	}
	if (surface != null) {
	    dirty.add(surface);
	}

				// Send on damage request to surface as well before bounds recomputation
	if (surface != null) {
	    surface.damage(globalBounds);
	}
	super.damage(boundsChanged);
				// And send to surface with bounds recomputed as well
	if (surface != null) {
	    surface.damage(globalBounds);
	}
    }

    /**
     * This is an internal form of damage that is only intended to be
     * used by calls from within ZNode.
     *
     * @param bounds The bounds that need to be damaged
     */
    public void damage(ZBounds bounds) {
	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZCamera.damage(bounds): this = " + this);
	}

	for (Iterator i=findCameras().iterator(); i.hasNext();) {
	    ZCamera camera = (ZCamera)i.next();
	    camera.damage(bounds);
	}
	if (surface != null) {
	    surface.damage(bounds);
	}
    }
    
    /**
     * Paints the view this camera sees.
     * @param renderContext The graphics context to use for rendering.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D      g2 = renderContext.getGraphics2D();
	ZArea           saveVisibleArea = renderContext.getVisibleArea();
	Shape		saveClip = g2.getClip();
	AffineTransform saveTransform = g2.getTransform(); 
	AffineTransform cameraAT = viewTransform.getAffineTransform();
	Rectangle2D     cameraArea = new Rectangle2D.Float(0, 0,(float)size.getWidth(), (float)size.getHeight());
	ZArea           visibleArea = saveVisibleArea;
	boolean         paintingWholeCamera = false;

	if (ZDebug.getDebug() == ZDebug.DEBUG_PAINT) {
	    System.out.println("ZCamera.paint");
	    //System.out.println("ZCamera.paint: graphics = " + g2);
	    System.out.println("ZCamera.paint: vis bounds = " + saveVisibleArea);
	    System.out.println("ZCamera.paint: transform = " + saveTransform);
	    System.out.println("ZCamera.paint: clip = " + saveClip);
	}

				// This is a bit tricky.
				// When painting a camera, we have to know if we are painting the camera
				// as a result of damaging this *camera*, or for some other reason.  If
				// we are painting as a result of damaging this camera than we must paint
				// everything that the camera can see.  Else, we just paint the normal
				// area that was damaged.  For this reason, we maintain a special set
				// of dirty bits that lets us know when we are in this situation.
	ZCamera renderingCamera = renderContext.getRenderingCamera();
	if (renderingCamera == null) {
	    ZSurface renderingSurface = renderContext.getSurface();
	    if (dirty.contains(renderingSurface)) {
		visibleArea = paintVisibleArea;
		visibleArea.reset();
		visibleArea.add(getViewBounds());
		renderContext.setVisibleArea(visibleArea);
		dirty.remove(renderingSurface);
		paintingWholeCamera = true;
	    }
	} else {
	    if (dirty.contains(renderingCamera)) {
		visibleArea = paintVisibleArea;
		visibleArea.reset();
		visibleArea.add(getViewBounds());
		renderContext.setVisibleArea(visibleArea);
		dirty.remove(renderingCamera);
		paintingWholeCamera = true;
	    }
	}

				// Even though we assume that each object has computed its bounds accurately,
				// and thus the visible area is accurate - we can still end up rendering one pixel
				// outside of the visible area because of 1) rounding, and 2) anti-aliasing.
				// So, at paint time, we add 1 screen pixel to the visible area we actually paint.
	if (!paintingWholeCamera) {
	    Rectangle2D rect = visibleArea.getBounds2D();
	    float d = 2 / viewTransform.getScale();
	    float d2 = d * 0.5f;
	    rect.setRect(rect.getX() - d2, rect.getY() - d2, rect.getWidth() + d, rect.getHeight() + d);
	    visibleArea.add(rect);
	}

	renderContext.pushCamera(this);

				// Apply the camera node transform (if it isn't identity)
	if (getTransform().getType() != AffineTransform.TYPE_IDENTITY) {
	    g2.transform(getTransform().getAffineTransform());
	}

				// Want to avoid clipping if possible since it slows things down
				// So, don't clip to camera if this is a top-level camera,
	if (renderingCamera != null) {
	    g2.clip(cameraArea);
	}

	if (!paintingWholeCamera) {
	    paintArea.reset();
	    paintArea.add(visibleArea);
	    paintArea.transform(cameraAT);
	    g2.clip(paintArea);
	}

	                        // Draw fill (background) color if specified
 	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(cameraArea);
	}
				// Apply the camera view transform
	g2.transform(cameraAT);

				// First, paint all the scenegraph objects this camera looks onto
	AffineTransform origTransform = g2.getTransform();
	for (Iterator i=getPaintStartPoints().iterator(); i.hasNext();) {
            ZNode node = (ZNode)i.next();
	    
	    g2.transform(node.getParent().computeGlobalCoordinateFrame());
	    node.paint(renderContext);
	    g2.setTransform(origTransform);
	}
				// Render a gray transparent area over the actual portion of
				// the screen updated if region management debugging is on.
	if (ZDebug.getDebugRegionMgmt()) {
	    if (!paintingWholeCamera) {
		paintArea.reset();
		paintArea.add(visibleArea);
		g2.setColor(new Color(150, 150, 150, 150));
		g2.fill(paintArea);
	    }
	}

				// Restore state to how it was at the beginning of paint
	g2.setTransform(saveTransform);
	g2.setClip(saveClip);
	renderContext.popCamera();
	renderContext.setVisibleArea(saveVisibleArea);

				// Call ZNode paint method to paint any children
	super.paint(renderContext);
    }

    /**
     * Returns the first object under the specified rectangle (if there is one)
     * as searched in reverse (front-to-back) order. Only returns nodes
     * with "pickable" visual components.
     * @param rect Coordinates of pick rectangle in camera coordinates.
     * @return The picked object.
     */
    public ZNode pick(ZBounds rect) {
	ZNode pickedNode = null;
	int numNodes = getPaintStartPoints().size();
	float mag = getMagnification();
	
	Rectangle2D transformedRect = new Rectangle2D.Float();

				// Then search nodes in reverse (front-to-back) order
				// Start by transforming point into camera's view coordinates
	getInverseViewTransform().transform(rect, rect);
	for (int i=numNodes; i>0; i--) {
	    ZNode node = (ZNode)getPaintStartPoints().elementAt(i - 1);
	    ZTransform globalFrame = new ZTransform(node.computeGlobalCoordinateFrame());
	    globalFrame.inverseTransform(rect, transformedRect);
	    pickedNode = node.pick(transformedRect, mag);

	    if (pickedNode != null) {
		return pickedNode;
	    }
	}
	return null;
    }
    
    /**
     * Return the list of nodes that are accepted by the specified filter in the
     * subtree rooted with this.  If this node is hidden or it is not "findable", then neither
     * this node, nor any of its descendants will be included.
     * The filter specifies whether or not a given node should be accepted by this
     * search, and whether the children of a node should be searched.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @return The nodes found
     * @see ZVisualComponent#isFindable()
     * @see ZFindFilter
     */
    public Vector findNodes(ZFindFilter filter) {
	int nodesSearched = 0;
	Vector nodes = new Vector();
				// Search scenegraph nodes
	for (Iterator i=getPaintStartPoints().iterator(); i.hasNext();) {
	    ZNode node = (ZNode)i.next();
	    nodesSearched += node.findNodes(filter, nodes);
	}

				// Finally, check the cameras children last
	nodesSearched += findNodes(filter, nodes);

	return nodes;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String str;
	
	str = "View Bounds:   " + getViewBounds() + "\n"
	    + "View Transform: " + getViewTransform();

	return super.toString() + "\n" + str;
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

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

	if (fieldName.compareTo("paintStartPoints") == 0) {
	    ZNode node;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		node = (ZNode)i.next();
		addPaintStartPoint(node);
	    }
	} else if (fieldName.compareTo("viewTransform") == 0) {
	    setViewTransform((ZTransform)fieldValue);
	}
    }

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out); 

	if (!viewTransform.transform.isIdentity()) {
	    out.writeState("ZTransform", "viewTransform", viewTransform);
	}
	if (!paintStartPoints.isEmpty()) {
	    out.writeState("Vector", "paintStartPoints", paintStartPoints);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add view transform
	if (viewTransform != null) {
	    out.addObject(viewTransform);
	}
				// Add paint start points
	ZNode node;
	for (Iterator i=paintStartPoints.iterator(); i.hasNext();) {
	    node = (ZNode)i.next();
	    out.addObject(node);
	}
    }
}
