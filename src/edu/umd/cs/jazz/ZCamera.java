/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.event.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ZCamera</b> represents a viewport onto a list of nodes.
 * A camera can look at any number of ZLayerGroups, and can specify
 * where in space it looks as specified by
 * an arbitrary affine transformation.
 * <p>
 * When a ZCanvas is created, it automatically creates a top-level
 * camera attached to that canvas that is the same size as the canvas.
 * Thus, the whole scenegraph is rendered within that canvas.
 * <p>
 * It is also possible to create an "internal camera" that acts as
 * a portal, or internal window.  That is, it is an object within
 * the scene that looks onto the scene.  To do this, create a scenegraph
 * where the top-level camera sees both the regular layer, and a new layer
 * that contains the internal camera.  Then, make the new internal camera
 * look at the regular layer.  The following code demonstrates this:
 *
 * <pre>
 *	ZCamera portal = new ZCamera();
 *	portal.setBounds(100, 100, 200, 200);
 *	portal.setFillColor(Color.red);
 *	ZVisualLeaf leaf = new ZVisualLeaf(portal);
 *	leaf.setFindable(false);
 *	ZVisualGroup border = new ZVisualGroup(leaf);
 *	ZRectangle rect = new ZRectangle(100, 100, 200, 200);
 *	rect.setPenColor(Color.blue);
 *	rect.setFillColor(null);
 *	rect.setPenWidth(5.0f);
 *	border.setFrontVisualComponent(rect);
 *	ZLayerGroup layer = new ZLayerGroup(border);
 *	canvas.getRoot().addChild(layer);
 *	canvas.getCamera().addLayer(layer);
 *	portal.addLayer(canvas.getLayer());
 * </pre>
 *
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 * @author  Maria E. Jump
 */

public class ZCamera extends ZVisualComponent implements ZFillColor, Serializable {
				// Default values
    static public final Color  fillColor_DEFAULT = Color.white;

				// The transform that specifies where in space this camera looks.
    private AffineTransform viewTransform;

				// The inverse of the view transform.
    private AffineTransform inverseViewTransform = null;

				// A dirty bit specifying if the inverse view transform is up to date.
    private transient boolean inverseViewTransformDirty = true;

				// The list of ZLayerGroups that this camera looks onto.
    private ZLayerGroup[]    layers = null;

				// The actual number of layers.
    private int    numLayers = 0;

				// The fill color used to paint this camera's background.
    private Color            fillColor = fillColor_DEFAULT;

				// The optional surface this camera is associated with.  If this surface
				// is non-null, then the camera will render directly onto this surface.
				// Else, the camera will only be visible within other cameras that looks
				// at it.
    private transient ZDrawingSurface surface = null;

				// Debugging variable used for debugging region management
    private transient int debugRenderCount = 0;

    /**
     * List of event listeners for camera events.
     */
    protected transient EventListenerList listenerList = null;



				// Some things that gets reused within render()
				// Define here for efficiency
    private  ZBounds  paintBounds;
    private transient ZBounds  tmpBounds = new ZBounds();

    /**
     * Internal class to support animation of camera views.
     */
    class CameraTransformable implements ZTransformable {

        public void getMatrix(double[] matrix) {
	    ZCamera.this.getViewTransform().getMatrix(matrix);
        }

        public void setTransform(double m00, double m10,
			     double m01, double m11,
			     double m02, double m12) {
	    ZCamera.this.setViewTransform(m00, m10, m01, m11, m02, m12);
        }
    }


    //****************************************************************************
    //
    //                Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new ZCamera.  Cameras are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     */
    public ZCamera() {
	this((ZLayerGroup)null, (ZDrawingSurface)null);
    }

    /**
     * Constructs a new ZCamera.  Cameras are always associated with a scenegraph,
     * but are not attached to any output device (such as a window or a portal) to start.
     * @param node The part of the scenegraph this camera sees.
     * @param surf The surface this top-level camera projects onto
     */
    public ZCamera(ZLayerGroup layer, ZDrawingSurface aSurface) {
	surface = aSurface;
        viewTransform = new AffineTransform();
	numLayers = 0;
	layers = new ZLayerGroup[1];
	paintBounds = new ZBounds();
	if (layer != null) {
	    addLayer(layer);
	}
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refCamera The reference camera to copy
     */
    public void duplicateObject(ZCamera refCamera) {
	super.duplicateObject(refCamera);

	viewTransform = refCamera.getViewTransform();
	fillColor = refCamera.getFillColor();

	ZLayerGroup[] refLayers = refCamera.getLayers();
	numLayers = refCamera.getNumLayers();
	for (int i=0; i<numLayers; i++) {
	    addLayer(refLayers[i]);
	}

	if (refCamera.listenerList != null) {
	    Object[] listeners = refCamera.listenerList.getListenerList();
	    for (int i=0; i<listeners.length/2; i+=2) {
		if (listenerList == null) {
		    listenerList = new EventListenerList();
		}
		listenerList.add((Class)listeners[i], (EventListener)listeners[i+1]);
	    }
	}

	paintBounds = new ZBounds();
    }

    /**
     * Duplicates the current object by using the copy constructor.
     * The portion of the reference object that is duplicated is that necessary to reuse the object
     * in a new place within the scenegraph, but the new object is not inserted into any scenegraph.
     * The object must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     * <P>
     * In particular, the camera's layers are duplicated so that a copied
     * camera will continue to see the same part of the scenegraph.
     * However, if the reference camera was attached to a surface, the copied camera
     * will not be attached to that surface, and must be made visible by attaching to a new surface (or by
     * being visible through another camera.)
     * <P>
     * However, the event listeners for the duplicated camera are reused references
     * to the original camera's event listeners.
     *
     * @return A copy of this camera.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZCamera copy;

	objRefTable.reset();
	copy = new ZCamera();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    /**
     * Trims the capacity of the array that stores the layers list points to
     * the actual number of points.  Normally, the layers list arrays can be
     * slightly larger than the number of points in the layers list.
     * An application can use this operation to minimize the storage of a
     * layers list.
     */
    public void trimToSize() {
	ZLayerGroup[] newLayers = new ZLayerGroup[numLayers];
	for (int i=0; i<numLayers; i++) {
	    newLayers[i] = layers[i];
	}
	layers = newLayers;
    }

    /**
     * Add a portion of the scenegraph that what this camera sees.
     * If the layer is already visible by this camera, then nothing happens.
     * @param layer The part of the scenegraph added to what this camera sees.
     */
    public void addLayer(ZLayerGroup layer) {
				// Check if layer already visible by camera
	for (int i=0; i<numLayers; i++) {
	    if (layers[i] == layer) {
		return;
	    }
	}

				// If not, add it - growing array if necessary
	try {
	    layers[numLayers] = layer;
	} catch (ArrayIndexOutOfBoundsException e) {
	    ZLayerGroup[] newLayers = new ZLayerGroup[(numLayers == 0) ? 1 : (2 * numLayers)];
	    System.arraycopy(layers, 0, newLayers, 0, numLayers);
	    layers = newLayers;
	    layers[numLayers] = layer;
	}
	numLayers++;

	layer.addCamera(this);
    }

    /**
     * Removes a portion of the scenegrpah from what this camera sees
     * @param layer The part of the scenegraph removed from what this camera sees.
     */
    public void removeLayer(ZLayerGroup layer) {
	for (int i=0; i<numLayers; i++) {
				// Find layer within layers list
	    if (layer == layers[i]) {
				// Then, slide down other layers, effectively removing this one
		for (int j=i; j < (numLayers - 1); j++) {
		    layers[j] = layers[j+1];
		}
		layers[numLayers - 1] = null;
		numLayers--;
		layer.removeCamera(this);
		break;
	    }
	}
    }

    /**
     * Replaces the specified node out of the list of layers of this
     * camera, and replaces it with the specified node.
     * The replacement node will be added to layer list in the same
     * position as the original was.
     *
     * @param original is the old node that is being swapped out as a layer
     * @param replacement is the new node that is being swapped in as a layer
     */
    public void replaceLayer(ZLayerGroup original, ZLayerGroup replacement) {
	for (int i=0; i<numLayers; i++) {
	    if (original == layers[i]) {
		original.removeCamera(this);
		layers[i] = replacement;
		replacement.addCamera(this);
		break;
	    }
	}
    }

    /**
     * Returns a copy of the list of layers that this camera looks onto.
     * @return Portion of scenegraph that is visible from this camera.
     */
    public ZLayerGroup[] getLayers() {
	ZLayerGroup[] copyLayers = new ZLayerGroup[numLayers];
	System.arraycopy(layers, 0, copyLayers, 0, numLayers);
        return copyLayers;
    }

    /**
     * Internal method to return a reference to the actual layers of this camera.
     * It should not be modified by the caller.  Note that the actual number
     * of layers could be less than the size of the array.  Determine
     * the actual number of layers with @link{#getNumLayers}.
     * @return the children of this node.
     */
    ZLayerGroup[] getLayersReference() {
	return layers;
    }

    /**
     * Returns the number of layers of this camera.
     * @return the number of layers.
     */
    public int getNumLayers() {
	return numLayers;
    }

    /**
       * Get the value of surface.
       * @return Value of surface.
       */
    public ZDrawingSurface getDrawingSurface() {
	return surface;
    }

    /**
       * Set the value of surface.
       * @param v  Value to assign to surface.
       */
    public void setDrawingSurface(ZDrawingSurface aSurface) {
	surface = aSurface;
	repaint();
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
	repaint();
    }

    /**
     * Returns the bounds that this Camera sees in global scene coordinates.
     * @return The bounds.
     */
    public ZBounds getViewBounds() {
	ZBounds viewBounds = getBounds();
	cameraToLocal(viewBounds, null);
	return viewBounds;
    }

    /**
     * Sets the bounds of this camera.
     * @param x,y,w,h The new bounds of this camera
     */
    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(new ZBounds(x, y, w, h));
	reshape();
    }

    /**
     * Sets the bounds of this camera.
     * @param newBounds The new bounds of this camera
     */
    public void setBounds(Rectangle2D newBounds) {
	super.setBounds(new ZBounds(newBounds));
	reshape();
    }

    /**
     * Returns the current magnification of this camera.
     * @return The magnification factor.
     */
    public float getMagnification() {
	return (float)(Math.max(viewTransform.getScaleX(), viewTransform.getScaleY()));
    }

    /*
     * Repaint causes the portions of the surfaces that this object
     * appears in to be marked as needing painting, and queues events to cause
     * those bounds to be painted. The painting does not actually
     * occur until those events are handled.
     * If this object is visible in multiple places because more than one
     * camera can see this object, then all of those places are marked as needing
     * painting.
     * <p>
     * Scenegraph objects should call repaint when their internal
     * state has changed and they need to be redrawn on the screen.
     * <p>
     * Important note : Scenegraph objects should call reshape() instead
     * of repaint() if the internal state change effects the bounds of the
     * shape in any way (e.g. changing penwidth, selection, transform, adding
     * points to a line, etc.)
     *
     * @see #reshape()
     */
    public void repaint() {
	super.repaint();	// First pass repaint request up the tree

				// And then pass it on to the surface looking at this if there is one.
	if (surface != null) {
	    surface.repaint(getBoundsReference());
	}
    }

    /**
     * This is an internal form of repaint that is only intended to be
     * used by calls from within Jazz.  It passes repaint requests
     * up through the camera to other interested camera, and the surface (if there is one).
     * Note that the input parameter may be modified as a result of this call.
     *
     * @param repaintBounds The bounds that need to be repainted (in global coordinates)
     */
    public void repaint(ZBounds repaintBounds) {
	if (ZDebug.debug && ZDebug.debugRepaint) {
	    System.out.println("ZCamera.repaint(bounds): this = " + this);
	}

	if (repaintBounds.isEmpty()) {
	    return;
	}

				// First, map global coords backwards through the camera's view
	tmpBounds.setRect(repaintBounds);
	ZTransformGroup.transform(tmpBounds, viewTransform);

				// Now, intersect those bounds with the camera's bounds
				// Note that Rectangle2D.intersect doesn't handle empty intersections properly
	ZBounds bounds = getBoundsReference();
	float x1 = Math.max((float)tmpBounds.getMinX(), (float)bounds.getMinX());
	float y1 = Math.max((float)tmpBounds.getMinY(), (float)bounds.getMinY());
	float x2 = Math.min((float)tmpBounds.getMaxX(), (float)bounds.getMaxX());
	float y2 = Math.min((float)tmpBounds.getMaxY(), (float)bounds.getMaxY());
	if ((x1 >= x2) || (y1 >= y2)) {
            return;
	}
	tmpBounds.setRect(x1, y1, x2 - x1, y2 - y1);

				// Then, pass repaint up the tree
	for (int i=0; i<numParents; i++) {
	    parents[i].repaint(tmpBounds);
	}

				// Finally, if this camera is attached to a surface, repaint it.
	if (surface != null) {
	    surface.repaint(tmpBounds);
	}
    }

    /**
     * Renders the view this camera sees.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param renderContext The graphics context to use for rendering.
     */
    public void render(ZRenderContext renderContext) {
	Graphics2D      g2 = renderContext.getGraphics2D();
	ZBounds         visibleBounds = renderContext.getVisibleBounds();
	ZBounds         cameraBounds = getBoundsReference();
	boolean         paintingWholeCamera = false;
	AffineTransform saveTransform = g2.getTransform();
	Shape           saveClip = g2.getClip();

				// Determine if we are painting the entire camera, or a sub-region
	if (visibleBounds.contains(cameraBounds)) {
	    paintingWholeCamera = true;
	}

	renderContext.pushCamera(this);

				// Want to avoid clipping if possible since it slows things down
				// So, don't clip to camera if this is a top-level camera,
	if (renderContext.getDrawingSurface() != surface) {
	    if (ZDebug.debug && ZDebug.debugRender) {
		System.out.println("ZCamera.render: clipping to camera bounds = " + cameraBounds);
	    }
	    g2.clip(cameraBounds);
	}

				// Compute the visible bounds as transformed by the camera's view transform
	paintBounds.reset();
	paintBounds.add(visibleBounds);
	paintBounds.transform(getInverseViewTransform());
	renderContext.pushVisibleBounds(paintBounds);

				// Apply the camera view transform
	g2.transform(viewTransform);

				// Clip to the transformed visible bounds
				// (unless painting the whole camera in which case we don't need to
				// since we've already clipped to the camera's bounds)
	if (!paintingWholeCamera) {
	    if (ZDebug.debug && ZDebug.debugRender) {
		System.out.println("ZCamera.render: clipping to paint bounds = " + paintBounds);
	    }
	    g2.clip(paintBounds);
	}

	                        // Draw fill (background) color if specified
 	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(paintBounds);
	}

	if (ZDebug.debug && ZDebug.debugRender) {
	    System.out.println("ZCamera.render");
	    System.out.println("ZCamera.render: xformed visible bounds = " + paintBounds);
	    System.out.println("ZCamera.render: camera bounds = " + cameraBounds);
	    System.out.println("ZCamera.render: transform = " + g2.getTransform());
	    System.out.println("ZCamera.render: clip = " + g2.getClip().getBounds2D());
	}

				// Finally, paint all the scenegraph objects this camera looks onto
	AffineTransform origTransform = g2.getTransform();
	ZLayerGroup layer;
	for (int i=0; i<numLayers; i++) {
	    layer = layers[i];
	    g2.transform(layer.getParent().getLocalToGlobalTransform());
	    layer.render(renderContext);
	    g2.setTransform(origTransform);
	}
				// Restore render context
	renderContext.popVisibleBounds();
	renderContext.popCamera();
	g2.setTransform(saveTransform);
	g2.setClip(saveClip);

				// For debugging, render a gray transparent bounds over the actual portion of
				// the screen that was rendered.  Cycle through several different gray colors
				// so one render can be distinguished from another.
	if (ZDebug.debug && ZDebug.debugRegionMgmt) {
	    if (!paintingWholeCamera) {
		paintBounds.reset();
		paintBounds.add(visibleBounds);
		int color = 100 + (debugRenderCount % 10) * 10;
		g2.setColor(new Color(color, color, color, 150));
		g2.fill(paintBounds);
		debugRenderCount++;
	    }
	}
    }

    /**
     * Returns the root of the scene graph that this camera is looking at
     */
    public ZRoot getRoot() {
    	// Actually returns the root of the first parent
        return (numParents > 0) ? parents[0].getRoot() : null;

    }

    /**
     * Picks the first object under the
     * specified rectangle (if there is one) as searched in reverse
     * (front-to-back) order. The picked object is returned via the
     * ZSceneGraphPath. Only nodes with "pickable" visual components
     * are returned.
     * @param rect Coordinates of pick rectangle in camera coordinates.
     * @return true if pick succeeds.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	Rectangle2D viewRect = (Rectangle2D)(rect.clone());
	Rectangle2D transformedRect = new Rectangle2D.Float();
	ZLayerGroup layer;
	AffineTransform localToGlobal, globalToLocal;

				// First check if pick rectangle intersects this camera's bounds
	if (!rect.intersects(getBounds())) {
	    return false;
	}

	//
	// Setup the path
	//
	path.push(this); // add this object to the path

				// Concatenate the camera's transform with the one stored in the path
	AffineTransform origTm = path.getTransform();
	AffineTransform tm = new AffineTransform(origTm);
	tm.concatenate(viewTransform);
	path.setTransform(tm);

				// Convert the rect from parent's coordinate system to local coordinates
        AffineTransform inverse = getInverseViewTransform();
	ZTransformGroup.transform(viewRect, inverse);

				// Search nodes in reverse (front-to-back) order.
	for (int i=(numLayers - 1); i>=0; i--) {
	    layer = layers[i];
	    localToGlobal = layer.getLocalToGlobalTransform();

	    try {
		globalToLocal = localToGlobal.createInverse();
				// Convert view rectangle to layer's coordinate system
		transformedRect.setRect(viewRect);
		ZTransformGroup.transform(transformedRect, globalToLocal);
		if (layer.pick(transformedRect, path)) {
		    return true;
		}
	    } catch (NoninvertibleTransformException e) {
		System.out.println(e);
	    }
	}

	path.setTransform(origTm);
        path.pop(this);
	return false;
    }

    /**
     * Return the list of nodes that are accepted by the specified filter in the
     * portion of the scenegraph visible through this camera.
     * If a node is not "findable", then neither
     * that node, nor any of its descendants will be included.
     * The filter specifies whether or not a given node should be accepted by this
     * search, and whether the children of a node should be searched.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @return The nodes found
     * @see ZNode#isFindable()
     * @see ZFindFilter
     */
    public ArrayList findNodes(ZFindFilter filter) {
	int nodesSearched;
	ArrayList nodes = new ArrayList();

	nodesSearched = findNodes(filter, nodes);

	return nodes;
    }

    /**
     * Internal method to assist findNodes.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @param nodes the accumulation list (results will be place here).
     * @return the number of nodes searched
     */
    int findNodes(ZFindFilter filter, ArrayList nodes) {
	int nodesSearched = 0;

				// Search scenegraph nodes
	for (int i=0; i<numLayers; i++) {
	    nodesSearched += layers[i].findNodes(filter, nodes);
	}

	return nodesSearched;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = super.dump();

	str += "\n View Bounds:   " + getViewBounds()
	    + "\n View Transform: " + getViewTransform();
	for (int i=0; i<numLayers; i++) {
	    str += "\n Layer: " + layers[i];
	}

	return str;
    }

    //****************************************************************************
    //
    // Event methods
    //
    //***************************************************************************

    /**
     * Adds the specified camera listener to receive camera events from this camera
     *
     * @param l the camera listener
     */
    public void addCameraListener(ZCameraListener l) {
	if (listenerList == null) {
	    listenerList = new EventListenerList();
	}
        listenerList.add(ZCameraListener.class, l);
    }

    /**
     * Removes the specified camera listener so that it no longer
     * receives camera events from this camera.
     *
     * @param l the camera listener
     */
    public void removeCameraListener(ZCameraListener l) {
        listenerList.remove(ZCameraListener.class, l);
	if (listenerList.getListenerCount() == 0) {
	    listenerList = null;
	}
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.  The listener list is processed in last to
     * first order.
     * @param id The event id (CAMERA_VIEW_CHANGED)
     * @param origViewTransform The original view transform (for view change events)
     * @see EventListenerList
     */
    protected void fireCameraEvent(int id, AffineTransform origViewTransform) {
	if (listenerList == null) {
	    return;
	}

				// Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ZCameraEvent e = new ZCameraEvent(this, id, origViewTransform);

				// Process the listeners last to first, notifying
				// those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ZCameraListener.class) {
		switch (id) {
		case ZCameraEvent.CAMERA_VIEW_CHANGED:
		    ((ZCameraListener)listeners[i+1]).viewChanged(e);
		    break;
		}
            }
        }
    }

    //****************************************************************************
    //
    // View Transform methods
    //
    //***************************************************************************

    /**
     * Returns a copy of the view transform that specifes where in
     * space this camera looks.
     * @return The current camera view transform.
     */
    public AffineTransform getViewTransform() {
	return (AffineTransform)(viewTransform.clone());
    }

    /**
     * Internal method to compute the inverse camera transform based on the camera transform.
     * This gets called from within ZCamera
     * whenever the inverse camera transform cache has been invalidated,
     * and it is needed.
     */
    protected void computeInverseViewTransform() {
	try {
	    inverseViewTransform = viewTransform.createInverse();
	    inverseViewTransformDirty = false;
	} catch (NoninvertibleTransformException e) {
	    System.out.println(e);
	}
    }

    /**
     * Returns a copy of the inverse view transform associated with this camera.
     * @return The current inverse camera transform.
     */
    public AffineTransform getInverseViewTransform() {
	if (inverseViewTransformDirty) {
	    computeInverseViewTransform();
	}
	return (AffineTransform)(inverseViewTransform.clone());
    }

    /**
     * Sets the view transform associated with this camera.
     * This controls where in space this camera looks.
     * @param transform
     */
    public void setViewTransform(AffineTransform transform) {
	AffineTransform origViewTransform = viewTransform;

	viewTransform = transform;
	inverseViewTransformDirty = true;

	fireCameraEvent(ZCameraEvent.CAMERA_VIEW_CHANGED, origViewTransform);
	repaint();
    }

    /**
     * Sets the view transform associated with this camera.
     * This controls where in space this camera looks.
     * @param m00,&nbsp;m01,&nbsp;m02,&nbsp;m10,&nbsp;m11,&nbsp;m12 the
     * 6 floating point values that compose the 3x3 transformation matrix
     */
    public void setViewTransform(double m00, double m10,
				 double m01, double m11,
				 double m02, double m12) {
	AffineTransform origViewTransform = viewTransform;

	viewTransform.setTransform(m00, m10, m01, m11, m02, m12);
	inverseViewTransformDirty = true;

	fireCameraEvent(ZCameraEvent.CAMERA_VIEW_CHANGED, origViewTransform);
	repaint();
    }

    /**
     * Transform a point in the camera's coordinate system through the camera down the tree
     * to the specified node's local coordinate system.
     * In the typical case where this is a top-level camera, and the point
     * is in screen coordinates, this will transform the
     * point to the local coordinate system of the specified node. The input point is modified
     * by this method.  It also returns the change in scale from the camera coordinate system
     * to the node coordinate system.
     * <P>
     * If the node is specified as null, then the point is transformed through the
     * camera, but no further - thus transforming the point from window to global coordinates.
     * <P>
     * If the specified node is not on the portion of the scenegraph that is visible
     * through the camera, then a ZNodeNotFoundException is thrown.
     * @param pt The point to be transformed
     * @param node The node to transform to
     * @return dz The change in scale from the camera coordinate system to the node coordinate system.
     * @exception ZNodeNotFoundException if the node is not in the subtree of the scenegraph
     *            under one of the camera's layers.
     * @see #localToCamera(Point2D, ZNode)
     */
    public float cameraToLocal(Point2D pt, ZNode node) {
				// First transform point through camera's view
	float dz;
	AffineTransform inverse;
        inverse = getInverseViewTransform();
	inverse.transform(pt, pt);
	dz = (float)Math.max(inverse.getScaleX(), inverse.getScaleY());
	if (node != null) {
				// Then, find the layer that is the ancestor of the specified node
	    ZLayerGroup layer = null;
	    for (int i=0; i<numLayers; i++) {
		if ((node == layers[i]) || (node.isDescendentOf(layers[i]))) {
		    layer = layers[i];
		    break;
		}
	    }
	    if (layer == null) {
				// Oops - this node can't be seen through the camera
		throw new ZNodeNotFoundException("Node " + node + " is not accessible from camera " + this);
	    } else {
		dz *= node.globalToLocal(pt);
	    }
	}

	return dz;
    }

    /**
     * Transform a rectangle in the camera's coordinate system through the camera down the tree
     * to the specified node's local coordinate system.
     * In the typical case where this is a top-level camera, and the rectangle
     * is in screen coordinates, this will transform the
     * rectangle to the local coordinate system of the specified node. The input rectangle is modified
     * by this method.  It also returns the change in scale from the camera coordinate system
     * to the node coordinate system.
     * <P>
     * If the node is specified as null, then the rectangle is transformed through the
     * camera, but no further - thus transforming the rectangle from window to global coordinates.
     * <P>
     * If the specified node is not on the portion of the scenegraph that is visible
     * through the camera, then a ZNodeNotFoundException is thrown.
     * @param rect The rectangle to be transformed
     * @param node The node to transform to
     * @return dz The change in scale from the camera coordinate system to the node coordinate system.
     * @exception ZNodeNotFoundException if the node is not in the subtree of the scenegraph
     *            under one of the camera's layers.
     * @see #localToCamera(Rectangle2D, ZNode)
     */
    public float cameraToLocal(Rectangle2D rect, ZNode node) {
				// First transform rectangle through camera's view
	float dz;
	AffineTransform inverse;
        inverse = getInverseViewTransform();
	ZTransformGroup.transform(rect, inverse);
	dz = (float)Math.max(inverse.getScaleX(), inverse.getScaleY());
	if (node != null) {
				// Then, find the layer that is the ancestor of the specified node
	    ZLayerGroup layer = null;
	    for (int i=0; i<numLayers; i++) {
		if (node.isDescendentOf(layers[i])) {
		    layer = layers[i];
		    break;
		}
	    }
	    if (layer == null) {
				// Oops - this node can't be seen through the camera
		throw new ZNodeNotFoundException("Node " + node + " is not accessible from camera " + this);
	    } else {
		dz *= node.globalToLocal(rect);
	    }
	}

	return dz;
    }

    /**
     * Transform a point in a node's local coordinate system up the scenegraph backwards through the camera
     * to the camera's coordinate system.
     * In the typical case where this is a top-level camera,
     * and the point represents a coordinate in the local coordinate system
     * of a node, this will transform the point to screen coordinates.
     * The input point is modified by this method.
     * It also returns the change in scale from the node coordinate system
     * to the camera coordinate system.
     * <P>
     * If the node is specified as null, then the point is transformed from global
     * coordinates through the camera, thus transforming the point from global to window coordinates.
     * <P>
     * If the specified node is not on the portion of the scenegraph that is visible
     * through the camera, then a ZNodeNotFoundException is thrown.
     * @param pt The point to be transformed
     * @param node The node that represents the local coordinates to transform from
     * @return dz The change in scale from the node coordinate system to the camera coordinate system.
     * @exception ZNodeNotFoundException if the node is not in the subtree of the scenegraph
     *            under one of the camera's layers.
     * @see #cameraToLocal(Point2D, ZNode)
     */
    public float localToCamera(Point2D pt, ZNode node) {
	float dz = 1.0f;
	if (node != null) {
				// First, find the layer that is the ancestor of the specified node
	    ZLayerGroup layer = null;
	    for (int i=0; i<numLayers; i++) {
		if (node.isDescendentOf(layers[i])) {
		    layer = layers[i];
		    break;
		}
	    }
	    if (layer == null) {
				// Oops - this node can't be seen through the camera
		throw new ZNodeNotFoundException("Node " + node + " is not accessible from camera " + this);
	    } else {
		dz *= node.localToGlobal(pt);
	    }
	}

	viewTransform.transform(pt, pt);
	dz *= (float)Math.max(viewTransform.getScaleX(), viewTransform.getScaleY());

	return dz;
    }

    /**
     * Transform a rectangle in a node's local coordinate system up the scenegraph backwards through the camera
     * to the camera's coordinate system.
     * In the typical case where this is a top-level camera,
     * and the rectangle is in the local coordinate system
     * of a node, this will transform the rectangle to screen coordinates.
     * The input rectangle is modified by this method.
     * It also returns the change in scale from the node coordinate system
     * to the camera coordinate system.
     * <P>
     * If the node is specified as null, then the rectangle is transformed from global
     * coordinates through the camera, thus transforming the rectangle from global to window coordinates.
     * <P>
     * If the specified node is not on the portion of the scenegraph that is visible
     * through the camera, then a ZNodeNotFoundException is thrown.
     * @param rect The rectangle to be transformed
     * @param node The node that represents the local coordinates to transform from
     * @return dz The change in scale from the node coordinate system to the camera coordinate system.
     * @exception ZNodeNotFoundException if the node is not in the subtree of the scenegraph
     *            under one of the camera's layers.
     * @see #cameraToLocal(Rectangle2D, ZNode)
     */
    public float localToCamera(Rectangle2D rect, ZNode node) {
	float dz = 1.0f;
	if (node != null) {
				// First, find the layer that is the ancestor of the specified node
	    ZLayerGroup layer = null;
	    for (int i=0; i<numLayers; i++) {
		if (node.isDescendentOf(layers[i])) {
		    layer = layers[i];
		    break;
		}
	    }
	    if (layer == null) {
				// Oops - this node can't be seen through the camera
		throw new ZNodeNotFoundException("Node " + node + " is not accessible from camera " + this);
	    } else {
		dz *= node.localToGlobal(rect);
	    }
	}

	ZTransformGroup.transform(rect, viewTransform);
	dz *= (float)Math.max(viewTransform.getScaleX(), viewTransform.getScaleY());

	return dz;
    }

    /**
     * Animates the camera view so that the specified bounds (in global coordinates)
     * is centered within the view of the camera.
     * @param <code>refBounds</code> The bounds (in global coordinates) to be centered.
     * @param <code>millis</code> The time in milliseconds to perform the animation
     * @param <code>surface</code> The surface to be updated during the animation
     */
    public void center(Rectangle2D refBounds, int millis, ZDrawingSurface aSurface) {
	AffineTransform at = new AffineTransform();
	ZBounds bounds = getBoundsReference();
				// First compute transform that will result in bounds being centered
	float dx = (float)(bounds.getWidth() / refBounds.getWidth());
	float dy = (float)(bounds.getHeight() / refBounds.getHeight());
	float scale = (dx < dy) ? dx : dy;
	float ctrX = (float)(0.5f * bounds.getWidth());
	float ctrY = (float)(0.5f * bounds.getHeight());
	float refBoundsX = (float)(refBounds.getX() + (0.5f * refBounds.getWidth()));
	float refBoundsY = (float)(refBounds.getY() + (0.5f * refBounds.getHeight()));

	at.translate(ctrX + (- refBoundsX * scale), ctrY + (- refBoundsY * scale));
	at.scale(scale, scale);

				// Then, change camera to new transform
	animate(at, millis, aSurface);
    }

    /**
     * Returns the current translation of this object
     * @return the translation
     */
    public Point2D getTranslation() {
	Point2D pt = new Point2D.Float((float)viewTransform.getTranslateX(), (float)viewTransform.getTranslateY());
	return pt;
    }

    /**
     * Returns the current X translation of this object
     * @return the translation
     */
    public float getTranslateX() {
        return (float)viewTransform.getTranslateX();
    }
    /**
     * Sets the current X translation of this object
     */
    public void setTranslateX(float x) {
        setTranslate(x, getTranslateY());
    }
    /**
     * Returns the current Y translation of this object
     * @return the translation
     */
    public float getTranslateY() {
        return (float)viewTransform.getTranslateY();
    }
    /**
     * Sets the current Y translation of this object
     */
    public void setTranslateY(float y) {
        setTranslate(getTranslateX(), y);
    }

    /**
     * Translate the object by the specified deltaX and deltaY
     * @param dx X-coord of translation
     * @param dy Y-coord of translation
     */
    public void translate(float dx, float dy) {
	AffineTransform newTransform = new AffineTransform(viewTransform);
	newTransform.translate(dx, dy);
	setViewTransform(newTransform);
    }

    /**
     * Animate the object from its current position by the specified deltaX and deltaY
     * @param dx X-coord of translation
     * @param dy Y-coord of translation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void translate(float dx, float dy, int millis, ZDrawingSurface surface) {
	AffineTransform at = new AffineTransform(viewTransform);
        at.translate(dx, dy);
	CameraTransformable ct = new CameraTransformable();
	ZTransformGroup.animate(ct, at, millis, surface);
    }

    /**
     * Translate the object to the specified position
     * @param x X-coord of translation
     * @param y Y-coord of translation
     */
    public void setTranslate(float x, float y) {
	double[] mat = new double[6];
	viewTransform.getMatrix(mat);
	mat[4] = x;
	mat[5] = y;
        setViewTransform(mat[0], mat[1], mat[2], mat[3], mat[4], mat[5]);
    }

    /**
     * Animate the object from its current position to the position specified
     * by x, y
     * @param x X-coord of translation
     * @param y Y-coord of translation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void setTranslate(float x, float y, int millis, ZDrawingSurface surface) {
	AffineTransform at = new AffineTransform(viewTransform);
	double[] mat = new double[6];

	at.translate(x, y);
	at.getMatrix(mat);
	mat[4] = x;
	mat[5] = y;
	at.setTransform(mat[0], mat[1], mat[2], mat[3], mat[4], mat[5]);
	CameraTransformable ct = new CameraTransformable();
	ZTransformGroup.animate(ct, at, millis, surface);
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
	return getMagnification();
    }

    /**
     * Scale the object from its current scale to the scale specified
     * by muliplying the current scale and dz.
     * @param dz scale factor
     */
    public void scale(float dz) {
	AffineTransform newTransform = new AffineTransform(viewTransform);
	newTransform.scale(dz, dz);
	setViewTransform(newTransform);
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
	AffineTransform newTransform = new AffineTransform(viewTransform);
	newTransform.translate(x, y);
	newTransform.scale(dz, dz);
	newTransform.translate(-x, -y);
	setViewTransform(newTransform);
    }

    /**
     * Animate the object from its current scale to the scale specified
     * by muliplying the current scale and deltaZ
     * @param dz scale factor
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void scale(float dz, int millis, ZDrawingSurface surface) {
	AffineTransform at = new AffineTransform(viewTransform);
	at.scale(dz, dz);
	CameraTransformable ct = new CameraTransformable();
	ZTransformGroup.animate(ct, at, millis, surface);
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
    public void scale(float dz, float x, float y, int millis, ZDrawingSurface surface) {
	AffineTransform at = new AffineTransform(viewTransform);
	at.translate(x, y);
	at.scale(dz, dz);
	at.translate(-x, -y);
	CameraTransformable ct = new CameraTransformable();
	ZTransformGroup.animate(ct, at, millis, surface);
    }

    /**
     * Sets the scale of the view transform
     * @param the new scale
     */
    public void setScale(float finalz) {
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
    public void setScale(float finalz, float x, float y) {
	float dz = finalz / getScale();
	scale(dz, x, y);
    }

    /**
     * Animate the object from its current scale to the specified target scale.
     * @param finalz scale factor
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void setScale(float finalz, int millis, ZDrawingSurface surface) {
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
    public void setScale(float finalz, float x, float y, int millis, ZDrawingSurface surface) {
	float dz = finalz / getScale();
	scale(dz, x, y, millis, surface);
    }

    /**
     * Set the transform of this camera to the specified transform,
     * and animate the change from its current transformation over the specified
     * number of milliseconds using a slow-in slow-out animation.
     * The surface specifies which surface should be updated during the animation.
     * <p>
     * If millis is 0, then the transform is updated once, and the scene
     * is not repainted immediately, but rather a repaint request is queued,
     * and will be processed by an event handler.
     * <p>
     * @param at Final transformation
     * @param millis Number of milliseconds over which to perform the animation
     * @param surface The surface to updated during animation.
     */
    public void animate(AffineTransform at, int millis, ZDrawingSurface surface) {
	CameraTransformable ct = new CameraTransformable();
	ZTransformGroup.animate(ct, at, millis, surface);
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

	if (!viewTransform.isIdentity()) {
	    out.writeState("java.awt.geom.AffineTransform", "viewTransform", viewTransform);
	}
	if (fillColor != fillColor_DEFAULT) {
	    out.writeState("java.awt.Color", "fillColor", fillColor);
	}
	if (numLayers > 0) {
	    ZNode[] copyLayers = getLayers();  // Array can have some empty slots
	    out.writeState("List", "layers", Arrays.asList(copyLayers));
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add layers
	for (int i=0; i<numLayers; i++) {
	    out.addObject(layers[i]);
	}
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

	if (fieldName.compareTo("layers") == 0) {
	    ZLayerGroup layer;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		layer = (ZLayerGroup)i.next();
		addLayer(layer);
	    }
	} else if (fieldName.compareTo("viewTransform") == 0) {
	    setViewTransform((AffineTransform)fieldValue);
	} else if (fieldName.compareTo("fillColor") == 0) {
	    setFillColor((Color)fieldValue);
	}
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	tmpBounds = new ZBounds();
	inverseViewTransformDirty = true;
	debugRenderCount = 0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	trimToSize();   // Remove extra unused array elements
	out.defaultWriteObject();
    }
}
