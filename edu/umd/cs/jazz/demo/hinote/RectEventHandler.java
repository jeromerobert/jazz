/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.demo.hinote;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;

/**
 * <b>RectEventHandler</b> is a simple event handler for interactively drawing a rectangle.
 *
 * @author  Benjamin B. Bederson
 */
public class RectEventHandler extends ZEventHandler {
    protected HiNoteCore hinote;
    protected ZRectangle rect;
    protected Point2D pt;
    protected Point2D pressObjPt;	// Event coords of mouse press (in object space)

    public RectEventHandler(HiNoteCore s, Component c, ZSurface v) {
	super(c, v);
	hinote = s;
	pt = new Point2D.Float();
    }
    
    public void mousePressed(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getComponent().requestFocus();
	    getSurface().startInteraction();
	    
	    pt.setLocation(e.getX(), e.getY());
	    getCamera().getInverseViewTransform().transform(pt, pt);
	    pressObjPt = (Point2D)pt.clone();
	    
	    rect = new ZRectangle((float)pt.getX(), (float)pt.getY(), 0.0f, 0.0f);
	    ZNode node = new ZNode(rect);
	    rect.setPenWidth(5.0f / getCamera().getMagnification());
	    rect.setPenColor(Color.blue);
	    hinote.getDrawingLayer().addChild(node);
	}
    }
    
    public void mouseDragged(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    pt.setLocation(e.getX(), e.getY());
	    getCamera().getInverseViewTransform().transform(pt, pt);
	    
	    float x, y, width, height;
	    x = (float)Math.min(pressObjPt.getX(), pt.getX());
	    y = (float)Math.min(pressObjPt.getY(), pt.getY());
	    width = (float)Math.abs(pressObjPt.getX() - pt.getX());
	    height = (float)Math.abs(pressObjPt.getY() - pt.getY());
	    
	    rect.setRect(x, y, width, height);

	    getSurface().restore();
	    
	}
    }
    
    public void mouseReleased(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getSurface().endInteraction();
	    rect = null;
	}
    }
}
