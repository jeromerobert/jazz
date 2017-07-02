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
 * Unit test for  ZPolygon.
 * @author: Jesse Grosjean
 */
public class ZPolygonTest extends TestCase {

    protected ZPolygon polygon;

    public ZPolygonTest(String name) {
        super(name);
    }

    public void setUp() {
        polygon = new ZPolygon(0, -1, 20, 50);
        polygon.add(15, 12);
    }

    public void testDuplicate() {
        ZPolygon l = (ZPolygon) polygon.clone();
        doCompare(l, polygon);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(polygon);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZPolygon savedShape = (ZPolygon) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, polygon);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testSetCoords() {
        polygon.setCoords(new Point2D.Double(10, 10), new Point2D.Double(100, 100));
        assertTrue(polygon.getX(0) == 10);
        assertTrue(polygon.getY(0) == 10);
        assertTrue(polygon.getX(1) == 100);
        assertTrue(polygon.getY(1) == 100);

        double[] xps = new double[3];
        double[] yps = new double[3];

        xps[0] = 4;
        xps[1] = 1;
        xps[2] = 2;

        yps[0] = 6;
        yps[1] = 1;
        yps[2] = 2;

        polygon.setCoords(xps, yps);

        assertTrue(polygon.getX(0) == 4);
        assertTrue(polygon.getY(0) == 6);

        assertTrue(polygon.getX(1) == 1);
        assertTrue(polygon.getY(1) == 1);

        assertTrue(polygon.getX(2) == 2);
        assertTrue(polygon.getY(2) == 2);
    }

    public void testZSerialize() {
        try {
            ZPolygon result = (ZPolygon) FileSavingSimulator.doZSerialize(polygon);
            doCompare(result, polygon);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    protected void doCompare(ZPolygon a, ZPolygon b) {
        assertTrue(a.getNumberPoints() == b.getNumberPoints());

        for (int i = 0; i < a.getNumberPoints(); i++) {
            assertTrue(a.getX(i) == b.getX(i));
            assertTrue(a.getY(i) == b.getY(i));
        }

        assertEquals(a.getBounds(), b.getBounds());
    }

    public void testInsertPoint() {
        ZPolygon p = new ZPolygon(0, 0, 100, 100);
        p.add(200, 200);

        p.add(50, 50, 1);

        assertTrue(p.getX(0) == 0);
        assertTrue(p.getY(0) == 0);
        assertTrue(p.getX(1) == 50);
        assertTrue(p.getY(1) == 50);
        assertTrue(p.getX(2) == 100);
        assertTrue(p.getY(2) == 100);
        assertTrue(p.getX(3) == 200);
        assertTrue(p.getY(3) == 200);
        assertTrue(p.getNumberPoints() == 4);
    }

    public void testAddPoint() {
        ZPolygon p = new ZPolygon();
        p.add(10, 10);
        assertTrue(p.getXCoords()[0] == 10);
        assertTrue(p.getYCoords()[0] == 10);
        p.add(20, 20);
        assertTrue(p.getXCoords()[1] == 20);
        assertTrue(p.getYCoords()[1] == 20);
        assertTrue(p.getX(1) == 20);
        assertTrue(p.getY(1) == 20);

        p.add(15, 15, 1);
        assertTrue(p.getXCoords()[0] == 10);
        assertTrue(p.getYCoords()[0] == 10);
        assertTrue(p.getXCoords()[1] == 15);
        assertTrue(p.getYCoords()[1] == 15);
        assertTrue(p.getXCoords()[2] == 20);
        assertTrue(p.getYCoords()[2] == 20);

        assertTrue(p.getNumberPoints() == 3);
    }

    public void testGetCoords() {
        ZPolygon p = new ZPolygon();
        p.add(1, 1);
        p.add(2, 2);
        p.add(3, 3);

        p.setCoords(p.getXCoords(), p.getYCoords());

        assertTrue(p.getX(0) == 1);
        assertTrue(p.getX(1) == 2);
        assertTrue(p.getX(2) == 3);

        assertTrue(p.getY(0) == 1);
        assertTrue(p.getY(1) == 2);
        assertTrue(p.getY(2) == 3);

        p = new ZPolygon(p.getXCoords(), p.getYCoords());

        assertTrue(p.getX(0) == 1);
        assertTrue(p.getX(1) == 2);
        assertTrue(p.getX(2) == 3);

        assertTrue(p.getY(0) == 1);
        assertTrue(p.getY(1) == 2);
        assertTrue(p.getY(2) == 3);

        assertTrue(p.getNumberPoints() == 3);
    }

    public void testInsertPointAtListFront() {
        ZPolygon p = new ZPolygon(0, 0, 100, 100);
        p.setPenWidth(0);
        p.add(200, 200);

        p.add(300, 300, 0);

        assertTrue(p.getX(0) == 300);
        assertTrue(p.getY(0) == 300);
        assertTrue(p.getX(1) == 0);
        assertTrue(p.getY(1) == 0);
        assertTrue(p.getX(2) == 100);
        assertTrue(p.getY(2) == 100);
        assertTrue(p.getX(3) == 200);
        assertTrue(p.getY(3) == 200);
        assertTrue(p.getBoundsReference().getCenterX() == 150);
        assertTrue(p.getNumberPoints() == 4);
    }
}