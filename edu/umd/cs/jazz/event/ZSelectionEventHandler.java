/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.ZRectangle;

/**
 * <b>ZSelectionEventHandler</b> provides event handlers for basic 
 * selection interaction.  
 * Click to select/unselect an item.
 * Shift-click to extend the selection.
 * Click-and-drag on the background to marquee select.
 * Drag a selected item to move all of the selected items.
 *
 * @author  Benjamin B. Bederson
 */
public class ZSelectionEventHandler extends ZEventHandler {
    protected Point2D pt;
    protected Vector  prevMotionSelection;
    protected ZNode   selNode;        // Selected object
    protected Point2D prevPt;         // Event coords of previous mouse event (in global coordinates)
    protected Point2D pressPt;        // Event coords of mouse press event (in global coordinates)
    protected ZNode   marquee;        // Rectangle representing marquee selection
    protected ZNode   selectionLayer; // Node that selection marquee should be put under
    protected int     scaleUpKey   = KeyEvent.VK_PAGE_UP;    // Key that scales selected objects up a bit
    protected int     scaleDownKey  = KeyEvent.VK_PAGE_DOWN; // Key that scales selected objects down a bit
    protected int     translateLeftKey  = KeyEvent.VK_LEFT;  // Key that translates selected objects left a bit
    protected int     translateRightKey = KeyEvent.VK_RIGHT; // Key that translates selected objects right a bit
    protected int     translateUpKey    = KeyEvent.VK_UP;    // Key that translates selected objects up a bit
    protected int     translateDownKey  = KeyEvent.VK_DOWN;  // Key that translates selected objects down a bit
    protected int     deleteKey  = KeyEvent.VK_DELETE;  // Key that deletes selected objects
    
    /**
     * Constructs a new ZSelectionEventHandler.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The camera that is selected within
     */
    public ZSelectionEventHandler(Component c, ZSurface v, ZNode selectionLayer) {
	super(c, v);
	prevPt = new Point2D.Float();
	pressPt = new Point2D.Float();
	pt = new Point2D.Float();
	prevMotionSelection = new Vector();
	this.selectionLayer = selectionLayer;
	marquee = null;
    }

    /**
     * Deactivates this event handler. 
     * This results in all selected objects becoming unselected
     */    
    public void deactivate() {
	super.deactivate();

	ZNode node;
	Vector selectedNodes = getCamera().getSelectedNodes();

	for (Iterator i=selectedNodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.getVisualComponent().unselect();
	}
	getSurface().restore();
    }

    /**
     * Specify the node that the selection "marquee" should be put on.
     * The marquee is the rectangle that the user drags around to select things within.
     */    
    public void setSelectionLayer(ZNode node) {
	selectionLayer = node;
    }

    /**
     * Key press event handler
     * @param <code>e</code> The event.
     */
    public void keyPressed(KeyEvent e) {
	ZCamera camera = getCamera();
	int keyCode = e.getKeyCode();
	float   scaleDelta = 1.1f;    // Magnification factor by for incremental scales
	float   panDelta = 1.0f;      // Translation amount for incremental scales
	float scaleZ = 1.0f;
	float panX = 0.0f;
	float panY = 0.0f;
	boolean scale = false;
	boolean pan = false;
	boolean delete = false;

	if (keyCode == scaleUpKey) {
	    scale = true;
	    scaleZ = scaleDelta;
	} else if (keyCode == scaleDownKey) {
	    scale = true;
	    scaleZ = 1.0f / scaleDelta;
	} else if (keyCode == translateLeftKey) {
	    pan = true;
	    panX = (-1.0f * panDelta) / camera.getMagnification();
	    panY = 0.0f;
	} else if (keyCode == translateRightKey) {
	    pan = true;
	    panX = (1.0f * panDelta) / camera.getMagnification();
	    panY = 0.0f;
	} else if (keyCode == translateUpKey) {
	    pan = true;
	    panX = 0.0f;
	    panY = (-1.0f * panDelta) / camera.getMagnification();
	} else if (keyCode == translateDownKey) {
	    pan = true;
	    panX = 0.0f;
	    panY = (1.0f * panDelta) / camera.getMagnification();
	} else if (keyCode == deleteKey) {
	    delete = true;
	}

	if (pan) {
	    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
		ZNode node = (ZNode)i.next();
		preTranslate(node, panX, panY);
	    }
	} else if (scale) {
	    ZTransform transform = null;
	    ZBounds bounds = new ZBounds();
				// First, get bounds of all nodes
	    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
		ZNode node = (ZNode)i.next();
		transform = node.getTransform();
		bounds.add(node.getGlobalBounds());
	    }
				// Then, scale them around a common point
	    Point2D pt = bounds.getCenter2D();
	    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
		ZNode node = (ZNode)i.next();
				// Need to preconcatentate transform with this operation to
				// apply scale in node's local coordinates
		preScale(node, scaleZ, (float)pt.getX(), (float)pt.getY());
	    }
	} else if (delete) {
	    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
		ZNode node = (ZNode)i.next();
		node.getParent().removeChild(node);
	    }
	}

	getSurface().restore();
    }

    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(MouseEvent e) {
	ZCamera camera = getCamera();
	boolean marqueeSelect = false;

	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getComponent().requestFocus();
	    getSurface().startInteraction();
	    
	    pressPt.setLocation(e.getX(), e.getY());
	    camera.getInverseViewTransform().transform(pressPt, pressPt);
	    prevPt.setLocation(pressPt);
	    
	    selNode = getSurface().pick(e.getX(), e.getY());
	    if (selNode == null) {
				// If object is not pressed on
		if (!e.isShiftDown()) {
		    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
			ZNode tmpNode = (ZNode)i.next();
			tmpNode.getVisualComponent().unselect();
		    }
		}
		marqueeSelect = true;
	    } else {
		
				// Else, object pressed on
		if (e.isShiftDown()) {
				// Shift key down
		    if (selNode.getVisualComponent().isSelected()) {
			selNode.getVisualComponent().unselect();
		    } else {
			selNode.getVisualComponent().select(camera);
		    }
		} else {
				// Shift key not down
		    if (!selNode.getVisualComponent().isSelected()) {
			for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
			    ZNode tmpNode = (ZNode)i.next();
			    tmpNode.getVisualComponent().unselect();
			}
			selNode.getVisualComponent().select(camera);
		    }
		}
	    }
	    
	    if (marqueeSelect) {
		ZRectangle rect;
		Point2D rectPt = new Point2D.Float((float)prevPt.getX(), (float)prevPt.getY());
		(new ZTransform(selectionLayer.computeGlobalCoordinateFrame())).inverseTransform(rectPt, rectPt);
		rect = new ZRectangle((float)rectPt.getX(), (float)rectPt.getY());
		rect.setPenWidth(1.0f / camera.getMagnification());
		rect.setPenColor(Color.black);
		rect.setFillColor(null);
		rect.setPickable(false);
		rect.setFindable(false);
		marquee = new ZNode(rect);
		selectionLayer.addChild(marquee);
	    }
	}

	getSurface().restore();
    }
    
    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(MouseEvent e) {
	ZCamera camera = getCamera();
	
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    pt.setLocation(e.getX(), e.getY());
	    camera.getInverseViewTransform().transform(pt, pt);
	    
	    if (marquee == null) {
				// Drag selected objects
		if (selNode == null) {
		    return;
		}
		
		float dx, dy;
		dx = (float)(pt.getX() - prevPt.getX());
		dy = (float)(pt.getY() - prevPt.getY());
		
		for (Iterator i=getCamera().getSelectedNodes().iterator(); i.hasNext();) {
		    selNode = (ZNode)i.next();
		    preTranslate(selNode, dx, dy);
		}
		getSurface().restore();
		prevPt.setLocation(pt);
	    } else {
				// Select by marquee:
				// First, modify marquee
		float x, y, width, height;

		Point2D p1 = new Point2D.Float((float)pt.getX(), (float)pt.getY());
		(new ZTransform(selectionLayer.computeGlobalCoordinateFrame())).inverseTransform(p1, p1);
		Point2D p2 = new Point2D.Float((float)pressPt.getX(), (float)pressPt.getY());
		(new ZTransform(selectionLayer.computeGlobalCoordinateFrame())).inverseTransform(p2, p2);
		x = (float)Math.min(p2.getX(), p1.getX());
		y = (float)Math.min(p2.getY(), p1.getY());
		width = (float)Math.abs(p2.getX() - p1.getX());
		height = (float)Math.abs(p2.getY() - p1.getY());
		ZRectangle rect = (ZRectangle)marquee.getVisualComponent();
		rect.setRect(x, y, width, height);

				// Then, update the selected items overlapping the rectangle
				// Select newly overlapped ones, and
				// unselect newly un-overlapped ones
				
				// First, select each item that overlaps region, but not previously selected
		ZFindFilter filter = new ZFindFilterMagBounds(marquee.getGlobalBounds(), camera.getMagnification());
		Vector nodes = camera.findNodes(filter);
		ZNode node;
		for (Iterator i=nodes.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    if (!prevMotionSelection.contains(node)) {
			node.getVisualComponent().select(camera);
			prevMotionSelection.addElement(node);
		    }
		}
				// Then, unselect items that don't overlap region, but previously did
				// Make a list of these items to safely remove from list afterwards
		Vector itemsToRemove = new Vector();
		for (Iterator i=prevMotionSelection.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    if (!nodes.contains(node)) {
			node.getVisualComponent().unselect();
			itemsToRemove.addElement(node);
		    }
		}
		
		for (Iterator i=itemsToRemove.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    prevMotionSelection.removeElement(node);
		}
 	    }
	}
	getSurface().restore();
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(MouseEvent e) {
	ZCamera camera = getCamera();
	Vector cameraList = new Vector();
	
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    selNode = null;
	    prevMotionSelection.clear();

	    if (marquee != null) {
		selectionLayer.removeChild(marquee);
		marquee = null;
	    }
	    getSurface().endInteraction();
	}
    } 

    /**
     * Scale the node using preConcatenation which will result in the
     * scale happening in global coordinates.
     */
    protected void preScale(ZNode node, float dz, float x, float y) {
	AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
	tx.scale(dz, dz);
	tx.translate(-x, -y);
	node.getTransform().preConcatenate(tx);
    }

    /**
     * Translate the node using preConcatenation which will result in the
     * translate happening in global coordinates.
     */
    protected void preTranslate(ZNode node, float dx, float dy) {
	AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
	node.getTransform().preConcatenate(at);
    }
}
