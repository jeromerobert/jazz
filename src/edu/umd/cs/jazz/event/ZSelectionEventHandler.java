/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
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
public class ZSelectionEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener, KeyListener {
    private boolean     active = false;      // True when event handlers are attached to a node
    private ZNode       node = null;         // The node the event handlers are attached to
    private ZCanvas     canvas = null;       // The canvas this event handler is associated with

    private ArrayList   prevMotionSelection; // Reusable list for implementing marquee selection
    private ArrayList   itemsToRemove;       // Reusable list for implementing marquee selection
    private ZNode       selNode;             // Selected object
    private transient Point2D     pt1, pt2;            // Utiltity points used temporarily within event handlers
    private transient Point2D     pressPt;             // Event coords of mouse press event (in window coordinates)
    private transient Point2D     dragPt;              // Event coords of mouse drag event (in window coordinates)
    private transient Point2D     prevPt;              // Event coords of previous mouse event (in window coordinates)
    private ZVisualLeaf marquee;             // Rectangle node representing marquee selection
    private ZGroup      marqueeLayer;        // Node that selection marquee should be put under

    private int        scaleUpKey        = KeyEvent.VK_PAGE_UP;   // Key that scales selected objects up a bit
    private int        scaleDownKey      = KeyEvent.VK_PAGE_DOWN; // Key that scales selected objects down a bit
    private int        translateLeftKey  = KeyEvent.VK_LEFT;      // Key that translates selected objects left a bit
    private int        translateRightKey = KeyEvent.VK_RIGHT;     // Key that translates selected objects right a bit
    private int        translateUpKey    = KeyEvent.VK_UP;        // Key that translates selected objects up a bit
    private int        translateDownKey  = KeyEvent.VK_DOWN;      // Key that translates selected objects down a bit
    private int        deleteKey         = KeyEvent.VK_DELETE;    // Key that deletes selected objects

ArrayList invisibleNodes = new ArrayList();

    /**
     * Constructs a new ZSelectionEventHandler.
     * @param <code>node</code> The node this event handler attaches to.
     * @param <code>canvas</code> The canvas this event handler attaches to
     * @param <code>marqueeLayer</code> The layer to draw the marquee on
     */
    public ZSelectionEventHandler(ZNode node, ZCanvas canvas, ZGroup marqueeLayer) {
	this.node = node;
	this.canvas = canvas;
	pt1 = new Point2D.Float();
	pt2 = new Point2D.Float();
	pressPt = new Point2D.Float();
	dragPt = new Point2D.Float();
	prevPt = new Point2D.Float();
	prevMotionSelection = new ArrayList();
	itemsToRemove = new ArrayList();
	this.marqueeLayer = marqueeLayer;
	marquee = null;
    }

    /**
     * Specifies whether this event handler is active or not.
     * @param active True to make this event handler active
     */
    public void setActive(boolean active) {
	if (this.active && !active) {
				// Turn off event handlers
	    this.active = false;
	    node.removeMouseListener(this);
	    node.removeMouseMotionListener(this);
	    canvas.removeKeyListener(this);

				// Unselect all objects when deactivated
	    ZSelectionGroup.unselectAll(canvas.getCamera());
	} else if (!this.active && active) {
				// Turn on event handlers
	    this.active = true;
	    node.addMouseListener(this);
	    node.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.requestFocus();
	}
    }

    /**
     * Specify the node that the selection "marquee" should be put on.
     * The marquee is the rectangle that the user drags around to select things within.
     * @param layer The node that the marquee should be put under
     */
    public void setMarqueeLayer(ZGroup layer) {
	marqueeLayer = layer;
    }

    /**
     * Key press event handler
     * @param <code>e</code> The event.
     */
    public void keyPressed(KeyEvent e) {
	ZCamera camera = canvas.getCamera();
	int     keyCode = e.getKeyCode();
	float   scaleDelta = 1.1f;    // Magnification factor by for incremental scales
	float   panDelta = 1.0f;      // Translation amount for incremental scales
	float   scaleZ = 1.0f;
	float   panX = 0.0f;
	float   panY = 0.0f;
	boolean scale = false;
	boolean pan = false;
	boolean delete = false;

				// First decide what operation to do
	if (keyCode == scaleUpKey) {
	    scale = true;
	    scaleZ = scaleDelta;
	} else if (keyCode == scaleDownKey) {
	    scale = true;
	    scaleZ = 1.0f / scaleDelta;
	} else if (keyCode == translateLeftKey) {
	    pan = true;
	    panX = -1.0f * panDelta;
	    panY = 0.0f;
	} else if (keyCode == translateRightKey) {
	    pan = true;
	    panX = 1.0f * panDelta;
	    panY = 0.0f;
	} else if (keyCode == translateUpKey) {
	    pan = true;
	    panX = 0.0f;
	    panY = -1.0f * panDelta;
	} else if (keyCode == translateDownKey) {
	    pan = true;
	    panX = 0.0f;
	    panY = 1.0f * panDelta;
	} else if (keyCode == deleteKey) {
	    delete = true;
	}

				// Then, iterate over the selected nodes, and do the appropriate operation
	ArrayList selection = ZSelectionGroup.getSelectedNodes(camera);

	if (pan) {
	    float dx, dy;
	    ZNode node;
	    ZTransformGroup transform;
				// Pan each selected node
	    for (Iterator i=selection.iterator(); i.hasNext();) {
		node = (ZNode)i.next();
		transform = node.editor().getTransformGroup();
				// First convert vector to node coordinate system
		pt1.setLocation(0.0f, 0.0f);
		camera.cameraToLocal(pt1, transform);
		pt2.setLocation(panX, panY);
		camera.cameraToLocal(pt2, transform);
		dx = (float)(pt2.getX() - pt1.getX());
		dy = (float)(pt2.getY() - pt1.getY());
				// Then, translate in the node's local coordinate system
		transform.translate(dx, dy);
	    }
	} else if (scale) {
	    ZTransformGroup transform = null;
	    ZBounds bounds = new ZBounds();
				// First, get bounds of all nodes
	    for (Iterator i=selection.iterator(); i.hasNext();) {
		ZNode node = (ZNode)i.next();
		bounds.add(node.getGlobalBounds());
	    }
				// Then, scale them around a common point
	    ZNode node;
	    Point2D center = bounds.getCenter2D();
	    for (Iterator i=selection.iterator(); i.hasNext();) {
		node = (ZNode)i.next();
				// Convert point and scale to node's coordinate system
		pt1.setLocation(center);
		node.globalToLocal(pt1);
				// Then, scale in node's local coordinate system
		transform = node.editor().getTransformGroup();
		transform.scale(scaleZ, (float)pt1.getX(), (float)pt1.getY());
	    }
	} else if (delete) {
	    ZNode node;
	    ZNode handle;
	    for (Iterator i=selection.iterator(); i.hasNext();) {
		node = (ZNode)i.next();
		handle = node.editor().getTop();
		handle.getParent().removeChild(handle);
	    }
	}
    }

    /**
     * Key release event handler
     * @param <code>e</code> The event.
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Key typed event handler
     * @param <code>e</code> The event.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();
	    ZCamera camera = path.getCamera();
	    boolean marqueeSelect = false;

	    path.getTopCamera().getDrawingSurface().setInteracting(true);

	    selNode = path.getNode();

	    pressPt.setLocation(e.getX(), e.getY());
	    prevPt.setLocation(pressPt);

	    if (selNode == null) {
				// If object is not pressed on
		if (!e.isShiftDown()) {
		    ZSelectionGroup.unselectAll(camera);
		}
		marqueeSelect = true;
	    } else {
				// Else, object pressed on
		if (e.isShiftDown()) {
				// Shift key down
		    if (ZSelectionGroup.isSelected(selNode)) {
			ZSelectionGroup.unselect(selNode);
		    } else {
			if (selNode.isFindable()) {
			    ZSelectionGroup.select(selNode);
			}
		    }
		} else {
				// Shift key not down
		    if (!ZSelectionGroup.isSelected(selNode)) {
			ZSelectionGroup.unselectAll(camera);
			if (selNode.isFindable()) {
			    ZSelectionGroup.select(selNode);
			}
		    }
		}
	    }

	    if (marqueeSelect) {
		float dz;
		ZRectangle rect;
		pt1.setLocation(pressPt);
		dz = camera.cameraToLocal(pt1, marqueeLayer);
		rect = new ZRectangle((float)pt1.getX(), (float)pt1.getY());
		rect.setPenWidth(1.0f * dz);
		rect.setPenColor(Color.black);
		rect.setFillColor(null);
		marquee = new ZVisualLeaf(rect);
		marquee.setPickable(false);
		marquee.setFindable(false);
		marqueeLayer.addChild(marquee);
	    }
	}
    }

    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();
	    ZCamera camera = path.getCamera();

	    dragPt.setLocation(e.getX(), e.getY());
	    if (marquee == null) {
				// Drag selected objects
		if (selNode == null) {
		    return;
		}

		float dx, dy;
		ZNode node;
		ZTransformGroup transform;
		ArrayList selection = ZSelectionGroup.getSelectedNodes(camera);
		for (Iterator i=selection.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    transform = node.editor().getTransformGroup();
				// First convert current and previous point to node coordinate systesm
		    pt1.setLocation(prevPt);
		    camera.cameraToLocal(pt1, transform);
		    pt2.setLocation(dragPt);
		    camera.cameraToLocal(pt2, transform);
		    dx = (float)(pt2.getX() - pt1.getX());
		    dy = (float)(pt2.getY() - pt1.getY());
				// Then, translate node
		    transform.translate(dx, dy);
		}
		prevPt.setLocation(dragPt);
	    } else {
				// Select by marquee:
				// First, modify marquee
		float x, y, width, height;

		pt1.setLocation(dragPt);
		camera.cameraToLocal(pt1, selNode);
		pt2.setLocation(pressPt);
		camera.cameraToLocal(pt2, selNode);

		x = (float)Math.min(pt2.getX(), pt1.getX());
		y = (float)Math.min(pt2.getY(), pt1.getY());
		width = (float)Math.abs(pt2.getX() - pt1.getX());
		height = (float)Math.abs(pt2.getY() - pt1.getY());
		ZRectangle rect = (ZRectangle)marquee.getVisualComponent();
		rect.setRect(x, y, width, height);

				// Then, update the selected items overlapping the rectangle
				// 1) Select newly overlapped ones, and
				// 2) Unselect newly un-overlapped ones

				// 1) select each item that overlaps region, but not previously selected
		ZFindFilter filter = new ZMagBoundsFindFilter(marquee.getGlobalBounds(), camera.getMagnification());
		ZNode node;
		ArrayList nodes = camera.findNodes(filter);
		for (Iterator i=nodes.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();

		    if (!prevMotionSelection.contains(node)) {
			ZSelectionGroup.select(node);
			prevMotionSelection.add(node);
		    }
		}
				// 2) unselect items that don't overlap region, but previously did
				// Make a list of these items to safely remove from list afterwards
		itemsToRemove.clear();
		for (Iterator i=prevMotionSelection.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    if (!nodes.contains(node)) {
			ZSelectionGroup.unselect(node);
			itemsToRemove.add(node);
		    }
		}

		for (Iterator i=itemsToRemove.iterator(); i.hasNext();) {
		    node = (ZNode)i.next();
		    prevMotionSelection.remove(node);
		}
 	    }
	}
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();

	    selNode = null;
	    prevMotionSelection.clear();

	    if (marquee != null) {
		marqueeLayer.removeChild(marquee);
		marquee = null;
	    }
	    path.getTopCamera().getDrawingSurface().setInteracting(false);
	}
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been moved on a node
     * (with no buttons no down).
     */
    public void mouseMoved(ZMouseEvent e) {
    }
}
