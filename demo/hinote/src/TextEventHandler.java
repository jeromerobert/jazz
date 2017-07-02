/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>TextEventHandler</b> is a simple event handler for interactively typing text.
 *
 * @author  Benjamin B. Bederson
 */
public class TextEventHandler extends ZEventHandler {
    static protected Font defaultFont = null;
    protected HiNoteCore hinote;
    protected ZText text;

    public TextEventHandler(HiNoteCore s, Component c, ZSurface v) {
	super(c, v);
	hinote = s;
    }

    public void deactivate() {
	super.deactivate();

	stopEditingText();
    }

    public void stopEditingText() {
	if (text != null) {
	    text.unselect();
	    text.setEditable(false);
	    if (text.getText().length() == 0) {
		hinote.getDrawingLayer().removeChild(text.findNode());
	    }
	    text = null;
	}
	hinote.getSurface().restore();
    }
    
    // translate the clicked points to coordinates
    // relative to this text node
    protected Point2D getLocalPoint(Point2D pt, ZNode node) {
	AffineTransform at = node.computeGlobalCoordinateFrame();
	Point2D localPt = new Point2D.Float();
	try {
	    at.inverseTransform(pt, localPt);
	} catch(java.awt.geom.NoninvertibleTransformException ex) {
	    System.out.println("NoninvertibleTransformException");
	}
	return localPt;
    }
    
    public void mousePressed(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getComponent().requestFocus();
	    
	    ZNode node;
	    ZSurface surface = hinote.getSurface();
	    ZCamera camera = hinote.getCamera();
	    ZNode drawingLayer = hinote.getDrawingLayer();
	    Point2D pt = new Point2D.Float();
	    Point2D localPt = null;
	    
	    node = surface.pick(e.getX(), e.getY());
	    pt.setLocation(e.getX(), e.getY());
	    camera.cameraToScene(pt);
	    if (node != null) {
		localPt = getLocalPoint(pt, node);                // Convert global to object coords
	    }
	    
				// If clicked on current node, set cursor position and continue
	    if ((text != null) &&
		(node == text.findNode())) {

		text.setCaretPos(localPt);
		surface.restore();
		return;
	    }
	    
				// Unselect previously active text, and delete it if empty
	    if (text != null) {
		stopEditingText();
	    }
	    
				// If clicked on some text, make that active
	    if (node != null) {
		text = (ZText)node.findVisualComponent(ZText.class);
		if (text != null) {
		    text.setCaretPos(localPt);
		}
	    }
				// Else, create a new text object
	    if (text == null) {
				// Set default font to Arial if it exists
		if (defaultFont == null) {
		    defaultFont = new Font("Arial", Font.PLAIN, 40);
		}
		
		float mag = camera.getMagnification();
		float fontHeight = 40.0f / mag;
		text = new ZText("");
		// text.setFont(defaultFont.deriveFont(fontHeight));
		text.setFont(defaultFont);
		text.setGreekThreshold(15);
		node = new ZNode(text);
		node.setMaxMag(HiNoteCore.MAX_ITEM_MAG * mag);
		node.getTransform().scale(1.0f / mag);
		node.getTransform().translateTo((float)pt.getX(), (float)(pt.getY() - 0.5*fontHeight));
		drawingLayer.addChild(node);
	    }
	    text.select(getCamera());	// Select the newly active text
	    text.setEditable(true);
	    
	    surface.restore();
	}
    }

    public void mouseDragged(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	ZSurface surface = hinote.getSurface();

	if (text != null) {
	    text.keyPressed(e);
	    surface.restore();
	}
    }
}
