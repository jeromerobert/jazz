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
 * Unit test for  ZPolyline.
 * @author: Jesse Grosjean
 */
public class ZPolylineTest extends TestCase {

    protected ZPolyline polyline;

    public ZPolylineTest(String name) {
        super(name);
    }

    public void setUp() {
        polyline = new ZPolyline(0, -1, 20, 50);
        polyline.add(15, 12);
    }

    public void testDuplicate() {
        ZPolyline l = (ZPolyline) polyline.clone();
        doCompare(l, polyline);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(polyline);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZPolyline savedShape = (ZPolyline) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, polyline);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZPolyline result = (ZPolyline) FileSavingSimulator.doZSerialize(polyline);
            doCompare(result, polyline);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    protected void doCompare(ZPolyline a, ZPolyline b) {
        assertTrue(a.getNumberPoints() == b.getNumberPoints());

        for (int i = 0; i < a.getNumberPoints(); i++) {
            assertTrue(a.getX(i) == b.getX(i));
            assertTrue(a.getY(i) == b.getY(i));
        }

        assertEquals(a.getBounds(), b.getBounds());
    }

    public void testInsertPoint() {
        ZPolygon p = new ZPolygon(0, 0, 100, 100);
        p.add(50, 50, 1);

        assertTrue(p.getX(0) == 0);
        assertTrue(p.getY(0) == 0);
        assertTrue(p.getX(1) == 50);
        assertTrue(p.getY(1) == 50);
        assertTrue(p.getX(2) == 100);
        assertTrue(p.getY(2) == 100);
        assertTrue(p.getNumberPoints() == 3);
    }

    public void testSetCoords() {
        ZPolyline aPolyline = new ZPolyline();
        aPolyline.setArrowHead(aPolyline.ARROW_BOTH);

        double[] xc = new double[2];
        double[] yc = new double[2];


        xc[0] = 0;
        yc[0] = 0;

        xc[1] = 100;
        yc[1] = 0;

        aPolyline.setCoords(xc, yc);

        assertTrue(aPolyline.getX(0) == 2);
        assertTrue(aPolyline.getX(1) == 98);

        aPolyline.setCoords(new Point2D.Double(xc[0], yc[0]), new Point2D.Double(xc[1], yc[1]));
        assertTrue(aPolyline.getX(0) == 2);
        assertTrue(aPolyline.getX(1) == 98);
    }
}