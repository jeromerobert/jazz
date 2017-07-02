/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
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
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZPolyline result = (ZPolyline) FileSavingSimulator.doZSerialize(polyline);
            doCompare(result, polyline);
        } catch (Exception e) {
            assert(e.toString(), false);
        }
    }

    protected void doCompare(ZPolyline a, ZPolyline b) {
        assert(a.getNumberPoints() == b.getNumberPoints());

        for (int i = 0; i < a.getNumberPoints(); i++) {
            assert(a.getX(i) == b.getX(i));
            assert(a.getY(i) == b.getY(i));
        }

        assertEquals(a.getBounds(), b.getBounds());
    }

    public void testInsertPoint() {
        ZPolygon p = new ZPolygon(0, 0, 100, 100);
        p.add(50, 50, 1);

        assert(p.getX(0) == 0);
        assert(p.getY(0) == 0);
        assert(p.getX(1) == 50);
        assert(p.getY(1) == 50);
        assert(p.getX(2) == 100);
        assert(p.getY(2) == 100);
        assert(p.getNumberPoints() == 3);
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

        assert(aPolyline.getX(0) == 2);
        assert(aPolyline.getX(1) == 98);

        aPolyline.setCoords(new Point2D.Double(xc[0], yc[0]), new Point2D.Double(xc[1], yc[1]));
        assert(aPolyline.getX(0) == 2);
        assert(aPolyline.getX(1) == 98);
    }
}