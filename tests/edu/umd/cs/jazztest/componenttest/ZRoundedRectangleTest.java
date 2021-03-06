/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.io.*;
import java.awt.geom.*;
import edu.umd.cs.jazz.*;

/**
 * Unit test for ZRoundedRectangle.
 * @author: Jesse Grosjean
 */
public class ZRoundedRectangleTest extends TestCase {

    protected ZRoundedRectangle rect;

    public ZRoundedRectangleTest(String name) {
        super(name);
    }

    public void setUp() {
        rect = new ZRoundedRectangle(10, 11, 20, 21, 5, 6);
    }

    public void testDuplicate() {
        ZRoundedRectangle r = (ZRoundedRectangle) rect.clone();
        doCompare(r, rect);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(rect);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZRoundedRectangle savedShape = (ZRoundedRectangle) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, rect);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZRoundedRectangle result = (ZRoundedRectangle) FileSavingSimulator.doZSerialize(rect);
            doCompare(result, rect);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    protected void doCompare(ZRoundedRectangle a, ZRoundedRectangle b) {
        RoundRectangle2D rectA = a.getRounedRect();
        RoundRectangle2D rectB = a.getRounedRect();

        assertTrue(rectA.getX() == rectB.getX());
        assertTrue(rectA.getY() == rectB.getY());
        assertTrue(rectA.getWidth() == rectB.getWidth());
        assertTrue(rectA.getHeight() == rectB.getHeight());
        assertTrue(rectA.getArcHeight() == rectB.getArcHeight());
        assertTrue(rectA.getArcWidth() == rectB.getArcWidth());

        assertEquals(a.getBounds(), b.getBounds());
    }
}