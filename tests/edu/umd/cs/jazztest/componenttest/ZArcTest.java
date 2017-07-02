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
 * Unit test for  ZArc.
 * @author: Jesse Grosjean
 */
public class ZArcTest extends TestCase {

    protected ZArc arc;

    public ZArcTest(String name) {
        super(name);
    }

    public void setUp() {
        arc = new ZArc(new Arc2D.Double(1, 2, 3, 4, 5, 5, Arc2D.OPEN));
    }

    public void testDuplicate() {
        ZArc a = (ZArc) arc.clone();
        doCompare(a, arc);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(arc);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZArc savedShape = (ZArc) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedShape, arc);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZArc result = (ZArc) FileSavingSimulator.doZSerialize(arc);
            doCompare(result, arc);
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

    protected void doCompare(ZRoundedRectangle a, ZRoundedRectangle b) {
        RoundRectangle2D rectA = a.getRounedRect();
        RoundRectangle2D rectB = a.getRounedRect();

        assert(rectA.getX() == rectB.getX());
        assert(rectA.getY() == rectB.getY());
        assert(rectA.getWidth() == rectB.getWidth());
        assert(rectA.getHeight() == rectB.getHeight());
        assert(rectA.getArcHeight() == rectB.getArcHeight());
        assert(rectA.getArcWidth() == rectB.getArcWidth());
    }
}