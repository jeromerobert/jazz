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
 * Unit test for  ZEllipse.
 * @author: Jesse Grosjean
 */
public class ZEllipseTest extends TestCase {

    protected ZEllipse ellipse;

    public ZEllipseTest(String name) {
        super(name);
    }

    public void setUp() {
        ellipse = new ZEllipse(10, -20, 30, 54);
    }

    public void testDuplicate() {
        ZEllipse e = (ZEllipse) ellipse.clone();
        doCompare(e, ellipse);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(ellipse);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZEllipse savedShape = (ZEllipse) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, ellipse);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZEllipse result = (ZEllipse) FileSavingSimulator.doZSerialize(ellipse);
            doCompare(result, ellipse);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    protected void doCompare(ZArc a, ZArc b) {
        Arc2D arcA = a.getArc();
        Arc2D arcB = a.getArc();

        assertEquals(arcA.getStartPoint(), arcB.getStartPoint());
        assertEquals(arcA.getEndPoint(), arcB.getEndPoint());
        assertTrue(arcA.getArcType() == arcB.getArcType());
        assertTrue(arcA.getAngleExtent() == arcB.getAngleExtent());
        assertTrue(arcA.getAngleStart() == arcB.getAngleStart());
        assertEquals(a.getBounds(), b.getBounds());

    }

    protected void doCompare(ZEllipse a, ZEllipse b) {
        assertEquals(a.getFrame(), b.getFrame());
    }
}