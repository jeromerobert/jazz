/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
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
 * Unit test for ZSelectionManager
 * @author: Jesse Grosjean
 */
public class ZSelectionManagerTest extends TestCase {
    ZCanvasSimulator canvasSimulator = null;

    public ZSelectionManagerTest(String name) {
        super(name);
    }
    public void setUp() {
        canvasSimulator = new ZCanvasSimulator();
        canvasSimulator.setBounds(0, 0, 400, 400);
        canvasSimulator.getPanEventHandler().setActive(false);
        canvasSimulator.getZoomEventHandler().setActive(false);
        ZLayerGroup marquee = new ZLayerGroup();
        canvasSimulator.getLayer().addChild(marquee);
        ZCompositeSelectionHandler selectionEventHandler = new ZCompositeSelectionHandler(
            canvasSimulator.getCameraNode(),
            canvasSimulator,
            marquee);
        selectionEventHandler.setActive(true);
    }

    public void testMove() {
        ZVisualLeaf leaf = new ZVisualLeaf(new ZRectangle(0, 0, 10, 10));
        canvasSimulator.getLayer().addChild(leaf);

        assertTrue(leaf.editor().getTransformGroup().getTranslateX() == 0);
        assertTrue(leaf.editor().getTransformGroup().getTranslateY() == 0);

        assertTrue(!ZSelectionManager.isSelected(leaf));
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 5, 5, MouseEvent.BUTTON1_MASK);
        assertTrue(ZSelectionManager.isSelected(leaf));

        canvasSimulator.simulateEvent(MouseEvent.MOUSE_DRAGGED, 10, 11, MouseEvent.BUTTON1_MASK);

        assertTrue(leaf.editor().getTransformGroup().getTranslateX() == 5);
        assertTrue(leaf.editor().getTransformGroup().getTranslateY() == 6);
    }

    public void testSelect() {
        ZVisualLeaf leaf = new ZVisualLeaf(new ZRectangle(0, 0, 10, 10));
        canvasSimulator.getLayer().addChild(leaf);

        assertTrue(!ZSelectionManager.isSelected(leaf));
        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 5, 5, MouseEvent.BUTTON1_MASK);
        assertTrue(ZSelectionManager.isSelected(leaf));

        canvasSimulator.simulateEvent(MouseEvent.MOUSE_PRESSED, 20, 20, MouseEvent.BUTTON1_MASK);
        assertTrue(!ZSelectionManager.isSelected(leaf));
    }
}