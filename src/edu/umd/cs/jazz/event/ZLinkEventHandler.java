/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZLinkEventHandler</b> is a simple event handler for interactively creating hyperlinks
 *
 * @author  Benjamin B. Bederson
 */
public class ZLinkEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener, KeyListener {
    private boolean        active = false;          // True when event handlers are attached to a node
    private ZNode          node = null;             // The node the event handlers are attached to
    private ZCanvas        canvas = null;           // The canvas this event handler is associated with

    private ZNode          currentNode = null;      // The node the pointer is over
    private ZAnchorGroup   currentLink = null;      // The link currently being defined
    private ZAnchorGroup   hiliteLink = null;       // The link currently being hilited because of a mouse-over
    private Vector         links = null;            // The list of currently visible links

    /**
     * Create a new link event handler.
     * It handles the interaction to interactively create a hyperlink.
     * @param <code>node</code> The node this event handler attaches to.
     * @param <code>canvas</code> The canvas this event handler attaches to
     */
    public ZLinkEventHandler(ZNode node, ZCanvas canvas) {
	this.node = node;
	this.canvas = canvas;
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
	    
				// Hide any visible links
	    ZAnchorGroup link;
	    for (Iterator i=links.iterator(); i.hasNext();) {
		link = (ZAnchorGroup)i.next();
		link.setVisible(false, null);
	    }
	    links = null;
	} else if (!this.active && active) {
				// Turn on event handlers
	    this.active = true;
	    node.addMouseListener(this);
	    node.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.requestFocus();

				// Initialize links
	    links = new Vector();
	}
    }

    /**
     * Internal method to update the hilite on the object the mouse moves over.
     * It draws a box around the object, and also shows the link if there is one.
     * @param e The mouse event that generated the hilite request
     */
    protected void updateHilite(ZMouseEvent e) {
	ZSceneGraphPath path = e.getPath();
	ZNode node = path.getNode();

				// If defining a link, then stop managing a special hilited link
	if (currentLink != null) {
	    hiliteLink = null;
	}
	if (node != currentNode) {
				// If there was a hilited link, unhilite it
	    if (hiliteLink != null) {
		hiliteLink.setVisible(false, null);
	    }
	    if (currentNode != null) {
		ZSelectionGroup.unselect(currentNode);
	    }
	    if (node != null) {
		ZSelectionGroup.select(node);
				// If not defining a link, and we are over a node with an invisible link, then hilite it
		ZAnchorGroup link = node.editor().getAnchorGroup();
		if ((currentLink == null) && (link != null) && !link.isVisible()) {
		    link.setVisible(true, path.getCamera());
		    hiliteLink = link;
		}
	    }
	    currentNode = node;
	}
    }

    /**
     * Key press event handler
     * @param <code>e</code> The event.
     */
    public void keyPressed(KeyEvent e) {
	ZCamera camera = canvas.getCamera();
	ZAnchorGroup link;
				// Press on 'space' to define a link to the current camera
	if (e.getKeyChar() == ' ') {
	    if (currentLink != null) {
		if (currentNode != null) {
		    ZSelectionGroup.unselect(currentNode);
		}
		currentLink.setDestBounds(camera.getViewBounds(), camera);
		currentNode = null;
		currentLink = null;
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

    public void mouseMoved(ZMouseEvent e) {
	updateHilite(e);
	if (currentLink != null) {
				// Currently defining a link, so update it
	    ZSceneGraphPath path = e.getPath();
	    Point2D pt = new Point2D.Float(e.getX(), e.getY());
	    path.screenToGlobal(pt);
	    currentLink.setDestPt(pt);
	    currentLink.updateLinkComponent(path.getCamera());
	}
    }

    public void mousePressed(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSceneGraphPath path = e.getPath();
	    ZCamera camera = path.getCamera();

	    if (currentLink == null) {
				// If not currently defining link, then try to start a new one
		if (currentNode != null) {
		    ZSelectionGroup.unselect(currentNode);
		    currentLink = currentNode.editor().getAnchorGroup();

		    Point2D pt = new Point2D.Float(e.getX(), e.getY());
		    path.screenToGlobal(pt);
		    currentLink.setSrcPt(pt);
		    currentLink.setDestPt(pt);
		    currentLink.setDestNode(null, camera);
		    currentLink.setDestBounds(null, camera);
		    currentLink.setVisible(true, camera);
		    links.addElement(currentLink);
		}
	    } else {
				// Currently defining a link, so conclude this one's definition
		if (currentNode == null) {
		    currentLink.setVisible(false, null);
		    currentLink.remove();
		} else {
		    ZSelectionGroup.unselect(currentNode);
		    currentLink.setDestNode(currentNode, camera);
		}
		currentNode = null;
		currentLink = null;
	    }
	}
    }

    public void mouseDragged(ZMouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (currentLink != null) {
		updateHilite(e);

		ZSceneGraphPath path = e.getPath();
		Point2D pt = new Point2D.Float(e.getX(), e.getY());
		path.screenToGlobal(pt);
		currentLink.setDestPt(pt);
	    }
	}
    }

    public void mouseReleased(ZMouseEvent e) {
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
}
