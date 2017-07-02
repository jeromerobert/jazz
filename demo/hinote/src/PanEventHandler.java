/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>PanEventHandler</b> is a simple event handler for panning and following hyperlinks
 *
 * @author  Benjamin B. Bederson
 */
public class PanEventHandler extends ZPanEventHandler {
    ZNode currentNode = null;

    /**
     * Constructs a new PanEventHandler.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The surface that is panned
     */
    public PanEventHandler(Component c, ZSurface v) {
	super(c, v);
    }

    protected void showLink(ZNode node) {
	ZLinkDecorator link = ZLinkEventHandler.getLink(node);
	if (link != null) {
	    link.show(getSurface().getCamera());
	}
    }

    protected void hideLink(ZNode node) {
	ZLinkDecorator link = ZLinkEventHandler.getLink(node);
	if (link != null) {
	    link.hide();
	}
    }

    protected void followLink(ZNode node) {
	ZSurface surface = getSurface();
	ZLinkDecorator link = ZLinkEventHandler.getLink(node);
	if (link != null) {
	    link.hide();
	    link.follow(surface);
	}
    }

    protected void updateCurrentNode(MouseEvent e) {
	ZNode node = getSurface().pick(e.getX(), e.getY());

	if (node != currentNode) {
	    if (currentNode != null) {
		hideLink(currentNode);
	    }
	    if (node != null) {
		showLink(node);
	    }
	    currentNode = node;
	}
    }

    public void mouseMoved(MouseEvent e) {
	updateCurrentNode(e);
	
	getSurface().restore();
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(MouseEvent e) {
	ZCamera camera = getCamera();

	super.mouseReleased(e);
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    if (!moved) {
		if (currentNode != null) {
		    followLink(currentNode);
		}
		
		updateCurrentNode(e);
	    }
	}
    }
}
