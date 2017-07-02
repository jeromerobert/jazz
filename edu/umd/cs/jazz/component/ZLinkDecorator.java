/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.scenegraph.*;

/**
 * <b>ZLinkDecorator</b> is a ZVisualComponent decorator that creates a spatial hyperlink from this
 * visual component chain to somewhere else.  
 * It can link to a ZNode, or to a specific bounds on the scenegraph.  It has
 * methods to visually show that there is a link (typically used by an application when the user
 * mouses over the visual component), and to follow a link.
 * <p>
 * ZLinkDecorator indicates the link by simply drawing an arrowed line from the source
 * to the destination of the link.  Applications should extend this class and replace the
 * show() method to make a different visual indication of the link.
 *
 * @author  Benjamin B. Bederson
 * @see     ZVisualComponent
 */
public class ZLinkDecorator extends ZVisualComponentDecorator implements ZSerializable {
    static protected final int LINK_ANIMATION_TIME = 750;
    static protected final int DEFAULT_CONNECTOR_WIDTH = 5;
    static protected Color     nodeColor = new Color(150, 150, 0);
    static protected Color     boundsColor = new Color(150, 150, 150);

    protected ZNode            destNode = null;
    protected Rectangle2D      destBounds = null;
    protected ZPolyline        connector = null;
    protected ZNode            connectorNode = null;
    protected float            xp[] = new float[2];
    protected float            yp[] = new float[2];
    
    /**
     * Constructs a new ZLinkDecorator
     */
    public ZLinkDecorator() {
    }

    public ZLinkDecorator(ZNode src) {
	setSrc(src);
    }

    /**
     * Constructs a new ZLinkDecorator that is a duplicate of the reference link, i.e., a "copy constructor"
     * @param <code>link</code> Reference link
     */
    public ZLinkDecorator(ZLinkDecorator link) {
	super(link);

	destNode = link.destNode;
	if (link.destBounds != null) {
	    destBounds = (Rectangle2D)link.destBounds.clone();
	}
    }

    /**
     * Duplicates the current ZLinkDecorator by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZLinkDecorator(ZLinkDecorator)
     */
    public Object clone() {
	return new ZLinkDecorator(this);
    }

    /**
     * Remove this decorator from the visual component chain.
     */
    public void remove() {
	hide();
	super.remove();
    }

    public void setSrc(ZNode src) {
	if (parent != null) {
	    remove();
	}
	insertAbove(src.getVisualComponent());
	updateBounds();
    }

    public void setDest(ZNode dest) {
	destNode = dest;
	destBounds = null;
	updateConnectorPts();
	if (connector != null) {
	    connector.setPenColor(nodeColor);
	}
    }

    public void setDest(Rectangle2D bounds) {
	destBounds = bounds;
	destNode = null;
	updateConnectorPts();
	if (connector != null) {
	    connector.setPenColor(boundsColor);
	}
    }

    public void updateConnectorPts() {
	ZNode src = findNode();
	Rectangle2D bounds = null;

	if (src != null) {
	    bounds = src.getGlobalBounds();
	    setSrcPt((float)(bounds.getX() + 0.5*bounds.getWidth()), (float)(bounds.getY() + 0.5*bounds.getHeight()));
	}

	if (destNode != null) {
				// Set destination point to center of destination object
	    bounds = destNode.getGlobalBounds();
	} else if (destBounds != null) {
				// Set destination point to center of destination bounds
	    bounds = destBounds;
	}
	if (bounds != null) {
	    setDestPt((float)(bounds.getX() + 0.5*bounds.getWidth()), (float)(bounds.getY() + 0.5*bounds.getHeight()));
	}
    }

    protected void setSrcPt(float x, float y) {
	xp[0] = x;
	yp[0] = y;
	if (connector != null) {
	    connector.setCoords(xp, yp);
	}
    }

    public void setDestPt(float x, float y) {
	xp[1] = x;
	yp[1] = y;
	if (connector != null) {
	    connector.setCoords(xp, yp);
	}
    }

    public void updateConnectorWidth(ZCamera camera) {
	if (connector != null) {
	    connector.setPenWidth(DEFAULT_CONNECTOR_WIDTH / camera.getMagnification());
	}
    }

    public void show(ZCamera camera) {
	if (connector == null) {
	    connector = createConnector();
	    connectorNode = new ZNode(connector);
	    connectorNode.setSave(false);
	    updateConnectorPts();
	    updateConnectorWidth(camera);
	    connector.setArrowHead(ZPolyline.ARROW_LAST);
	    connector.setPickable(false);
	    if (destBounds != null) {
		connector.setPenColor(boundsColor);
	    } else {
		connector.setPenColor(nodeColor);
	    }
	    findNode().getParent().addChild(connectorNode);
	} else {
	    updateConnectorPts();
	}
    }

    public ZPolyline createConnector() {
	return new ZPolyline();
    }

    public void hide() {
	if (connectorNode != null) {
	    connectorNode.getParent().removeChild(connectorNode);
	    connector = null;
	    connectorNode = null;
	}
    }

    public void follow(ZSurface surface) {
	Rectangle2D bounds = null;
	if (destNode != null) {
	    bounds = destNode.getGlobalBounds();
	} else if (destBounds != null) {
	    bounds = destBounds;
	}
	if (bounds != null) {
	    surface.startInteraction();
	    surface.getCamera().center(bounds, LINK_ANIMATION_TIME, surface);
	    surface.endInteraction();
	}
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

	if (fieldName.compareTo("destNode") == 0) {
	    setDest((ZNode)fieldValue);
	} else if (fieldName.compareTo("destBounds") == 0) {
	    setDest((Rectangle2D)fieldValue);
	}
    }

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out);

	if (destNode != null) {
	    out.writeState("ZNode", "destNode", destNode);
	}
	if (destBounds != null) {
	    out.writeState("java.awt.geom.Rectangle2D", "destBounds", destBounds);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

	if (destNode != null) {
	    out.addObject(destNode);
	}
    }
}
