/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.eventtest;

import java.util.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.*;
import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.ZText;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Unit test for ZEventsTest
 * @author: Jesse Grosjean
 */
public class ZEventsTest extends TestCase {

    ZCanvasSimulator canvasSimulator = null;

    public ZEventsTest(String name) {
        super(name);
    }

    public void setUp() {
        canvasSimulator = new ZCanvasSimulator();
        canvasSimulator.setBounds(0, 0, 400, 400);
    }

    public void testEventConsumedStopsPropogation() {
        final ZRectangle rect = new ZRectangle(0, 0, 20 ,20);
        final ZVisualLeaf leaf = new ZVisualLeaf(rect);
        canvasSimulator.getLayer().addChild(leaf);

        rect.setPenPaint(Color.black);
        assertEquals(rect.getPenPaint(), Color.black);

        // add listener to node.
        leaf.addMouseListener(new ZMouseAdapter() {
            public void mousePressed(ZMouseEvent e) {
                rect.setPenPaint(Color.green); // this should not get called since the
                                               // visual component consumes the event.
                assertEquals(e.getNode(), leaf);
                assertEquals(e.getPath().getObject(), rect);
            }
        });

        // add listener to visual component
        rect.addMouseListener(new ZMouseAdapter() {
            public void mousePressed(ZMouseEvent e) {
                rect.setPenPaint(Color.red);
                assertEquals(e.getNode(), leaf);
                assertEquals(e.getPath().getObject(), rect);
                e.consume();
            }
        });

        // wrong kind of event.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_CLICKED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.black);

        // wrong location.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 30, 30);
        assertEquals(rect.getPenPaint(), Color.black);

        // this should activate the event handler and color should change.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.red);


    }

    public void testMousePressedOnNode() {
        final ZRectangle rect = new ZRectangle(0, 0, 20 ,20);
        final ZVisualLeaf leaf = new ZVisualLeaf(rect);
        canvasSimulator.getLayer().addChild(leaf);

        rect.setPenPaint(Color.black);
        assertEquals(rect.getPenPaint(), Color.black);

        // add listener to node.
        leaf.addMouseListener(new ZMouseAdapter() {
            public void mousePressed(ZMouseEvent e) {
                rect.setPenPaint(Color.red);
                assertEquals(e.getNode(), leaf);
                assertEquals(e.getPath().getObject(), rect);
            }
        });

        // wrong kind of event.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_CLICKED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.black);

        // wrong location.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 30, 30);
        assertEquals(rect.getPenPaint(), Color.black);

        // this should activate the event handler and color should change.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.red);


    }

    public void testMousePressedOnVisualComponent() {
        final ZRectangle rect = new ZRectangle(0, 0, 20 ,20);
        final ZVisualLeaf leaf = new ZVisualLeaf(rect);
        canvasSimulator.getLayer().addChild(leaf);

        rect.setPenPaint(Color.black);
        assertEquals(rect.getPenPaint(), Color.black);

        // add listener to visual component
        rect.addMouseListener(new ZMouseAdapter() {
            public void mousePressed(ZMouseEvent e) {
                rect.setPenPaint(Color.red);
                assertEquals(e.getNode(), leaf);
                assertEquals(e.getPath().getObject(), rect);
            }
        });

        // wrong kind of event.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_CLICKED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.black);

        // wrong location.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 30, 30);
        assertEquals(rect.getPenPaint(), Color.black);

        // this should activate the event handler and color should change.
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 10, 10);
        assertEquals(rect.getPenPaint(), Color.red);
    }
}