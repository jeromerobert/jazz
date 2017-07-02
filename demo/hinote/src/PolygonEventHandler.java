/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>PolygonEventHandler</b> is a simple event handler for interactively drawing a polygon.
 *
 * @author  James J. Mokwa
 */
public class PolygonEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
                                // When user clicks, draw a line segment from that
                                // point to the mouse, wherever it moves.
                                // When the user clicks again, add that line segment
                                // to a polyline, and draw a new line segment to the mouse.
                                // When the user finishes the polygon, by clicking
                                // near the starting point, remove the polyline and use its
                                // points to create one complete polygon.
                                // If user double-clicks, just end polyline at that point,
                                // do not build a closed polygon.

    static private final int MINDISTANCE = 6; // Clicking within this distance from starting point closes the polygon

    private boolean active = false;        // True when event handlers are attached to a node
    private ZNode   node = null;           // The node the event handlers are attached to

    private HiNoteCore hinote;
    private ZGroup layer;

    private double distance;    // distance from current point to initial point.

    private ZPolyline polyline; // polyline created as user is drawing the polygon
    private ZVisualLeaf polylineLeaf;   // visual leaf of above

    private ZPolyline segment; // for efficiency: tmp line seg from last click to current mouse positon.
    private ZVisualLeaf segmentLeaf; // visual leaf of above

    private Point2D  startPoint; // initial start point of polygon. Close polygon to this point.
    private Point2D  lastClickPoint = new Point2D.Double(); // last point of polygon drawn so far
    private Point2D pt = new Point2D.Double();  // point clicked
    private double x, y;        // MouseEvent X,Y coordinates clicked on.
    private boolean drawing = false; // true if user is currently drawing a polygon.
    private boolean startNewPolygon = true; // if true, next click will start a new polygon
                                           // Mask out mouse and mouse/key chords
    private int     all_button_mask   = (MouseEvent.BUTTON1_MASK |
                                         MouseEvent.BUTTON2_MASK |
                                         MouseEvent.BUTTON3_MASK |
                                         MouseEvent.ALT_GRAPH_MASK |
                                         MouseEvent.CTRL_MASK |
                                         MouseEvent.META_MASK |
                                         MouseEvent.SHIFT_MASK |
                                         MouseEvent.ALT_MASK);

    public PolygonEventHandler(HiNoteCore hinote, ZNode node) {
        this.hinote = hinote;
        this.node = node;
        layer = hinote.getDrawingLayer();
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
            camera.getDrawingSurface().setInteracting(true);

                                // End polyline at last point if user double-clicks
            if  (e.getClickCount() > 1) {
                endPolyline(e);
                return;
            }

            x = e.getX();
            y = e.getY();

            if (startNewPolygon) { // first click of a new polygon
                startNewPolygon = false;
                drawing = true;
                startPoint = new Point2D.Double(x, y);
                path.screenToGlobal(startPoint);

                                // a polyline is drawn as the user clicks points building
                                // a polygon. When the polygon is finished, the polyline
                                // is removed and a complete polygon created.
                polyline = new ZPolyline();
                polylineLeaf = new ZVisualLeaf(polyline);
                polyline.setPenWidth(hinote.penWidth);
                polyline.setPenPaint(hinote.penColor);
                layer.addChild(polylineLeaf);
                polylineLeaf.editor().getTransformGroup().scale(1/camera.getMagnification(), pt.getX(), pt.getY());
                polylineLeaf.globalToLocal(startPoint);
                polyline.moveTo(startPoint.getX(), startPoint.getY());

                                // A line segment follows the mouse around as the
                                // user decides where the next point of the polygon goes.
                segment = new ZPolyline();
                segmentLeaf = new ZVisualLeaf(segment);
                layer.addChild(segmentLeaf);
                segment.setPenWidth(hinote.penWidth / camera.getMagnification());
                segment.setPenPaint(hinote.penColor);

            } else {            // subsequent clicks, drawing a polygon
                pt.setLocation(x, y);
                path.screenToGlobal(pt);
                polylineLeaf.globalToLocal(pt);
                                // polygon shape is completed when user
                                // clicks close to the starting point.
                if (startPoint.distance(pt) < MINDISTANCE) {
                    endPolygon(e);
                    return;
                }

                polyline.add(pt);
            }
        }
    }

    public void mouseReleased(ZMouseEvent e) {
    }

    public void endPolyline(ZMouseEvent e) {
        ZSceneGraphPath path = e.getPath();

        path.getTopCamera().getDrawingSurface().setInteracting(false);
        drawing = false;
        layer.removeChild(segmentLeaf); // remove tmp segment used while drawing
        startNewPolygon = true;
    }

    public void endPolygon(ZMouseEvent e) {
        ZSceneGraphPath path = e.getPath();
        ZCamera camera = path.getTopCamera();

        path.getTopCamera().getDrawingSurface().setInteracting(false);
        drawing = false;

        AffineTransform at = polylineLeaf.editor().getTransformGroup().getTransform();
        layer.removeChild(segmentLeaf.editor().getTop()); // remove tmp segment used while drawing
        layer.removeChild(polylineLeaf.editor().getTop()); // remove the polyline

                                // Create a new polygon from points of polyline
        ZPolygon polygon = new ZPolygon(polyline);
        ZVisualLeaf polygonLeaf = new ZVisualLeaf(polygon);
        //polygon.setPenWidth(hinote.penWidth / camera.getMagnification());
        polygon.setPenWidth(hinote.penWidth);
        polygon.setPenPaint(hinote.penColor);
        polygon.setFillPaint(hinote.fillColor);
        polylineLeaf.setVisualComponent(polygon);
        layer.addChild(polygonLeaf);
        polygonLeaf.editor().getTransformGroup().setTransform(at);
        startNewPolygon = true;
    }

    public void mouseMoved(ZMouseEvent e) {
        if (! drawing) {
            return;
        }
        ZSceneGraphPath path = e.getPath();

                                // Draw a line segment from the last point clicked
                                // (last point of polyline) to the current mouse position.
        pt.setLocation(e.getX(), e.getY());
        path.screenToGlobal(pt);
        int n = polyline.getNumberPoints()-1;
        lastClickPoint.setLocation(polyline.getX(n), polyline.getY(n));
        polylineLeaf.localToGlobal(lastClickPoint);
        segmentLeaf.globalToLocal(pt);
        segmentLeaf.globalToLocal(lastClickPoint);
        segment.setCoords(lastClickPoint, pt);
    }

    public void mouseDragged(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            pt.setLocation(e.getX(), e.getY());
            path.screenToGlobal(pt);
            polylineLeaf.globalToLocal(pt);
            polyline.add(pt);
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
}