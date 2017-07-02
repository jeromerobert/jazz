/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.awt.image.renderable.RenderContext;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * A ZBasicComponent is a simple Swing component that can be used to render
 * onto for Jazz.  It extends JComponent, and overrides the appropriate
 * methods so that whenever Java requests that this widget gets redrawn,
 * the requests are forwarded on to Jazz to render appropriately.  It also
 * defines a very simple Jazz scenegraph consisting of a root, a camera,
 * and one node.  Finally, it supports capturing the current camera view
 * onto an Image (i.e., a screengrab).
 *
 * @author Benjamin B. Bederson
 */
public class ZBasicComponent extends JComponent {
    /**
     * The root of the scenegraph
     * @serial
     */
    protected ZRootNode		     root;

    /**
     * The camera in the scenegraph
     * @serial
     */
    protected ZCamera                camera;

    /**
     * The surface associated with the component
     * @serial
     */
    protected ZSurface               surface;

    /**
     * The single node that camera looks onto.  It is considered to
     * be the "layer" because many applications will put content
     * under this node which can then be hidden or revealed like a layer.
     * @serial
     */
    protected ZNode                  layer;

    /**
     * The default constructor for a ZBasicComponent.  This creates a simple
     * scenegraph with a root, camera, surface, and layer.  These 4 scenegraph
     * elements are accessible to the application through get methods.
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicComponent() {
	root = new ZRootNode();
	camera = new ZCamera();
	surface = new ZSurface(camera, this);
	layer = new ZNode();
	root.addChild(layer);
	root.addChild(camera);
	camera.addPaintStartPoint(layer);
    }

    /**
     * A constructor for a ZBasicComponent that uses an existing scenegraph.
     * This creates a new camera and surface.  The camera is inserted into
     * the scenegraph under the root, and the specified layer is added to
     * the camera's paint start point list.  The scenegraph
     * elements are accessible to the application through get methods.
     * @param aRoot The existing root of the scenegraph this component is attached to
     * @param layer The existing layer node of the scenegraph that this component's camera looks onto
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicComponent(ZRootNode aRoot, ZNode layer) {
	root = aRoot;
	camera = new ZCamera();
	surface = new ZSurface(camera, this);
	root.addChild(camera);
	camera.addPaintStartPoint(layer);
    }

    /**
     * This renders the Jazz scene attached to this component by passing on the Swing paint request 
     * to the underlying Jazz surface.
     * @param g The graphics to be painted onto
     */
    public void paintComponent(Graphics g) {
	surface.paint(g);
    }

    /** 
     * This captures changes in the component's bounds so the underlying Jazz camera can
     * be updated to mirror bounds change.
     * @param x The X-coord of the top-left corner of the component
     * @param y The Y-coord of the top-left corner of the component
     * @param width The width of the component
     * @param height The Height of the component
     */
    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(x, y, w, h);
	camera.setSize(w, h);
    }

    /**
     * Sets the background color of this component.
     * Actually - this is implemented by changing the fill color of the
     * camera associated with this component since the camera controls
     * the rendering onto this component.
     * @param background The new color to use for this component's background
     */
    public void setBackground(Color background) {
	super.setBackground(background);
	surface.getCamera().setFillColor(background);
    }

    /**
     * Sets the surface.
     * @param surface the surface
     */    
    public void setSurface(ZSurface aSurface) {
	surface = aSurface;
    }
    
    /**
     * Return the surface.
     * @return the surface
     */    
    public ZSurface getSurface() {
	return surface;
    }
    
    /**
     * Sets the camera.
     * @param camera the camera
     */    
    public void setCamera(ZCamera aCamera) {
	camera = aCamera;
	camera.setSize(getSize());
    }
    
    /**
     * Return the camera associated with the primary surface.
     * @return the camera
     */
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Sets the root.
     * @param root the root
     */    
    public void setRoot(ZRootNode aRoot) {
	root = aRoot;
    }
    
    /**
     * Return the root of the scenegraph.
     * @return the root
     */
    public ZRootNode getRoot() {
	return root;
    }
    
    /**
     * Sets the layer.
     * @param layer the layer
     */    
    public void setLayer(ZNode aLayer) {
	layer = aLayer;
    }
    
    /**
     * Return the "layer".  That is, the single node that
     * the camera looks onto to start.
     * @return the node
     */
    public ZNode getLayer() {
	return layer;
    }
    
    public boolean isFocusTraversable() {
	return true;
    }

    /**
     * Generate a copy of the view in the current camera scaled so that the aspect ratio
     * of the screen is maintained, and the larger dimension is scaled to
     * match the specified parameter.
     * @return An image of the camera
     */
    public Image getScreenImage(int maxDim) {
	int w, h;

	if (getSize().getWidth() > getSize().getHeight()) {
	    w = maxDim;
	    h = (int)(maxDim * getSize().getHeight() / getSize().getWidth());
	} else {
	    h = maxDim;
	    w = (int)(maxDim * getSize().getWidth() / getSize().getHeight());
	}
	return getScreenImage(w, h);
    }

    /**
     * Generate a copy of the current camera scaled to the specified dimensions.
     * @param w  Width of the image
     * @param h  Height of the image
     * @return An image of the camera
     */
    public Image getScreenImage(int w, int h) {
				// We create an image of the right size and get its graphics
	Image screenImage = createImage(w, h);
	Graphics2D g2 = (Graphics2D)screenImage.getGraphics();
				// Then, we compute the transform that will map the component into the image
	float dsx = (float)(w / getSize().getWidth());
	float dsy = (float)(h / getSize().getHeight());
	AffineTransform at = AffineTransform.getScaleInstance(dsx, dsy);
	g2.setTransform(at);
				// Finally, we paint onto the image
	surface.paint(g2);
				// And we're done
	return screenImage;
    }
}
