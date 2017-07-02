/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>ZLinkEventHandler</b> is a simple event handler for interactively creating hyperlinks
 *
 * @author  Benjamin B. Bederson
 */
public class ZLinkEventHandler extends ZEventHandler {
    protected ZNode          src = null;
    protected ZNode          currentNode = null;
    protected ZLinkDecorator currentLink = null;
    protected Vector         links = null;
    protected boolean        definingLink = false;   // True while currently defining a link

    public ZLinkEventHandler(Component c, ZSurface v) {
	super(c, v);
    }

    /**
     * Return an instance of a Class that corresponds to the specified class name.
     * This is juat a wrapper around Class.forName, but catches the exception that
     * gets thrown if the name is invalid, and prints an error.  This way, applications
     * that are quite sure there will not be an error don't have to catch the exception
     * themselves.
     */    
    static public Class findClass(String arg) {
	Class c = null;
	try {
	    c = Class.forName(arg);
	} catch (ClassNotFoundException ex) {
	    System.out.println("Internal Error: Util.findClass: " + arg + " " + ex);
	}
	return c;
    }

    static public ZLinkDecorator getLink(ZNode node) {
	return (ZLinkDecorator)node.findVisualComponent(findClass("edu.umd.cs.jazz.component.ZLinkDecorator"));
    }

    public void activate() {
	super.activate();

	links = new Vector();
    }

    public void deactivate() {
	super.deactivate();

	ZLinkDecorator link;
	for (Iterator i=links.iterator(); i.hasNext();) {
            link = (ZLinkDecorator)i.next();
	    link.hide();
        }
	links = null;
	getSurface().restore();
    }
    
    protected void updateHilite(MouseEvent e) {
	ZNode node = getSurface().pick(e.getX(), e.getY());
	
	if (node != currentNode) {
	    if (currentNode != null) {
		currentNode.getVisualComponent().unselect();
	    }
	    if (node != null) {
		node.getVisualComponent().select(getSurface().getCamera());
	    }
	    currentNode = node;
	}
    }

    public ZLinkDecorator createLinkDecorator(ZNode src) {
	return new ZLinkDecorator(src);
    }

    public void keyTyped(KeyEvent e) {
	ZSurface surface = getSurface();
	ZCamera camera = getSurface().getCamera();
	ZLinkDecorator link;
				// Press on 'space' to define a link to the current camera
	if (e.getKeyChar() == ' ') {
	    if (definingLink) {
		if ((currentNode != null) && (src != null)) {
		    ZVisualComponent vc = currentNode.getVisualComponent();
		    if (vc != null) {
			vc.unselect();
		    }
		}
		currentLink.setDest(camera.getViewBounds());
		src = null;
		currentNode = null;
		definingLink = false;
	    }
	    surface.restore();
	}
    }
    
    public void mouseMoved(MouseEvent e) {
	ZSurface surface = getSurface();
	ZCamera camera = getSurface().getCamera();
	
	updateHilite(e);
	if (definingLink) {
	    Point2D pt = new Point2D.Float(e.getX(), e.getY());
	    camera.cameraToScene(pt);
	    currentLink.setDestPt((float)pt.getX(), (float)pt.getY());
	    currentLink.updateConnectorWidth(camera);
	}
	
	surface.restore();
    }
    
    public void mousePressed(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSurface surface = getSurface();
	    ZCamera camera = getSurface().getCamera();
	    getComponent().requestFocus();

	    if (!definingLink) {
				// If not currently defining link, then try to start a new one
		ZNode node = currentNode;
		if (node != null) {
		    Point2D pt = new Point2D.Float();
		    
		    src = node;
		    
		    pt.setLocation(e.getX(), e.getY());
		    camera.cameraToScene(pt);
		    src.getVisualComponent().unselect();
		    currentLink = getLink(src);
		    if (currentLink == null) {
			currentLink = createLinkDecorator(src);
		    }
		    currentLink.setDestPt((float)pt.getX(), (float)pt.getY());
		    currentLink.show(camera);
		    currentLink.updateConnectorWidth(camera);
		    links.addElement(currentLink);
		    definingLink = true;
		}
	    } else {
				// Currently defining a link, so conclude this one's definition
		if (currentNode == null) {
		    ZLinkDecorator link = getLink(src);
		    link.remove();
		} else {
		    ZVisualComponent vc = currentNode.getVisualComponent();
		    vc.unselect();
		    ZNode dest = currentNode;
		    currentLink.setDest(dest);
		}
		src = null;
		currentNode = null;
		currentLink = null;
		definingLink = false;
	    }
	    
	    surface.restore();
	}
    }
    
    public void mouseDragged(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSurface surface = getSurface();
	    ZCamera camera = getSurface().getCamera();
	    
	    if (definingLink) {
		updateHilite(e);
		
		Point2D pt = new Point2D.Float(e.getX(), e.getY());
		camera.cameraToScene(pt);
		currentLink.setDestPt((float)pt.getX(), (float)pt.getY());
	    }
	    
	    surface.restore();
	}
    }

    public void mouseReleased(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    ZSurface surface = getSurface();

	    surface.restore();
	}
    }
}
