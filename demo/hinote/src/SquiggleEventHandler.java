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
 * <b>SquiggleEventHandler</b> is a simple event handler for interactively drawing a polyline.
 *
 * @author  Benjamin B. Bederson
 */
public class SquiggleEventHandler extends ZEventHandler {
    protected HiNoteCore hinote;
    protected ZPolyline polyline;
    protected Point2D pt;

    public SquiggleEventHandler(HiNoteCore s, Component c, ZSurface v) {
	super(c, v);
	hinote = s;
	pt = new Point2D.Float();
    }
    
    public void mousePressed(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getComponent().requestFocus();
	    getSurface().startInteraction();
	    
	    pt.setLocation(e.getX(), e.getY());
	    getCamera().cameraToScene(pt);
	    
	    polyline = new ZPolyline(pt);
	    ZNode node = new ZNode(polyline);
	    polyline.setPenWidth(5.0f / getCamera().getMagnification());
	    polyline.setPenColor(Color.red);
	    hinote.getDrawingLayer().addChild(node);

				// Test Transform event code
	    /*
	    node.addNodeListener(new ZNodeAdapter() {
		public void nodeTransformed(ZNodeEvent e) {
		    System.out.println("Node transformed: source = " + e.getNode() + ", orig transform = " + e.getOrigTransform() + ", new transform = " + e.getNode().getTransform().getAffineTransform());
		}
	    });
	    */
	    
	}
    }
    
    public void mouseDragged(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    pt.setLocation(e.getX(), e.getY());
	    getCamera().cameraToScene(pt);
	    
	    polyline.add(pt);
	    getSurface().restore();
	}
    }
    
    public void mouseReleased(MouseEvent e) {
	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    getSurface().endInteraction();
	    polyline = null;
	}
    }
}
