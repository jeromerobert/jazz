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
 * Unit test for ZQuadCurve.
 * @author: Jesse Grosjean
 */
public class ZQuadCurveTest extends TestCase {

    protected ZQuadCurve curve;

    public ZQuadCurveTest(String name) {
        super(name);
    }

    public void setUp() {
        curve = new ZQuadCurve(0, 0, 50, 0, 100, 100);
    }

    public void testDuplicate() {
        ZQuadCurve c = (ZQuadCurve) curve.clone();
        doCompare(c, curve);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(curve);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZQuadCurve savedShape = (ZQuadCurve) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, curve);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZQuadCurve result = (ZQuadCurve) FileSavingSimulator.doZSerialize(curve);
            doCompare(result, curve);
        } catch (Exception e) {
            assert(e.toString(), false);
        }
    }

    protected void doCompare(ZArc a, ZArc b) {
        Arc2D arcA = a.getArc();
        Arc2D arcB = a.getArc();

        assertEquals(arcA.getStartPoint(), arcB.getStartPoint());
        assertEquals(arcA.getEndPoint(), arcB.getEndPoint());
        assert(arcA.getArcType() == arcB.getArcType());
        assert(arcA.getAngleExtent() == arcB.getAngleExtent());
        assert(arcA.getAngleStart() == arcB.getAngleStart());

        assertEquals(a.getBounds(), b.getBounds());
    }

    protected void doCompare(ZQuadCurve a, ZQuadCurve b) {
        QuadCurve2D curveA = a.getQuadCurve();
        QuadCurve2D curveB = a.getQuadCurve();

        assert(curveA.getX1() == curveB.getX1());
        assert(curveA.getX2() == curveB.getX2());
        assert(curveA.getY1() == curveB.getY1());
        assert(curveA.getY2() == curveB.getY2());
        assertEquals(curveA.getCtrlPt(), curveB.getCtrlPt());
    }
}