/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>SquiggleEventHandler</b> is a simple event handler for interactively drawing a polyline.
 *
 * @author  Benjamin B. Bederson
 */
public class SquiggleEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private boolean active = false;        // True when event handlers are attached to a node
    private ZNode   node = null;           // The node the event handlers are attached to

    private HiNoteCore hinote;
    private ZPolyline polyline;
    private ZVisualLeaf leaf;
    private Point2D pt;
                                                    // Mask out mouse and mouse/key chords
    private int            all_button_mask   = (MouseEvent.BUTTON1_MASK |
                                                MouseEvent.BUTTON2_MASK |
                                                MouseEvent.BUTTON3_MASK |
                                                MouseEvent.ALT_GRAPH_MASK |
                                                MouseEvent.CTRL_MASK |
                                                MouseEvent.META_MASK |
                                                MouseEvent.SHIFT_MASK |
                                                MouseEvent.ALT_MASK);

    public SquiggleEventHandler(HiNoteCore hinote, ZNode node) {
        this.hinote = hinote;
        this.node = node;
        pt = new Point2D.Double();
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
        } else if (!this.active && active) {
                                // Turn on event handlers
            this.active = true;
            node.addMouseListener(this);
            node.addMouseMotionListener(this);
        }
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }

    public void mousePressed(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            ZCamera camera = path.getTopCamera();
            ZGroup layer = hinote.getDrawingLayer();

            camera.getDrawingSurface().setInteracting(true);

            pt.setLocation(e.getX(), e.getY());
            path.screenToGlobal(pt);

            polyline = new ZPolyline(pt);
            leaf = new ZVisualLeaf(polyline);

            //polyline.setPenWidth(hinote.penWidth / camera.getMagnification());
            polyline.setPenWidth(hinote.penWidth);
            polyline.setPenPaint(hinote.penColor);
            layer.addChild(leaf);
            leaf.editor().getTransformGroup().scale(1/camera.getMagnification(), pt.getX(), pt.getY());
        }
    }

    public void mouseDragged(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            pt.setLocation(e.getX(), e.getY());
            path.screenToGlobal(pt);
            leaf.globalToLocal(pt);
            polyline.add(pt);
        }
    }

    public void mouseReleased(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            path.getTopCamera().getDrawingSurface().setInteracting(false);
            polyline = null;
            leaf = null;
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