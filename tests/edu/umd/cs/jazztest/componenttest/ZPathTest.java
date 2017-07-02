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
 * Unit test for  ZPath.
 * @author: Jesse Grosjean
 */
public class ZPathTest extends TestCase {

    protected ZPath path;

    public ZPathTest(String name) {
        super(name);
    }

    public void setUp() {
        GeneralPath p = new GeneralPath();
        p.append(new Ellipse2D.Double(0, 0, 10, 10), true);
        p.append(new Rectangle2D.Double(10, 4, 2, 2), true);
        path = new ZPath(p);
    }

    public void testDuplicate() {
        ZPath p = (ZPath) path.clone();
        doCompare(p, path);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(path);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZPath savedShape = (ZPath) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, path);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZPath result = (ZPath) FileSavingSimulator.doZSerialize(path);
            doCompare(result, path);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    protected void doCompare(ZPath a, ZPath b) {
        GeneralPath pa = a.getPath();
        GeneralPath pb = b.getPath();

        assertEquals(a.getBounds(), b.getBounds());

        PathIterator ia = pa.getPathIterator(null);
        PathIterator ib = pb.getPathIterator(null);

        double[] aPoints = new double[6];
        double[] bPoints = new double[6];
        while (!ia.isDone()) {

            assertTrue(ia.currentSegment(aPoints) == ib.currentSegment(bPoints));

            assertTrue(aPoints[0] == bPoints[0]);
            assertTrue(aPoints[1] == bPoints[1]);
            assertTrue(aPoints[2] == bPoints[2]);
            assertTrue(aPoints[3] == bPoints[3]);
            assertTrue(aPoints[4] == bPoints[4]);
            assertTrue(aPoints[5] == bPoints[5]);

            ia.next();
            ib.next();
        }
    }
}