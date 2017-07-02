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
 * Unit test for  ZLine.
 * @author: Jesse Grosjean
 */
public class ZLineTest extends TestCase {

    protected ZLine line;

    public ZLineTest(String name) {
        super(name);
    }

    public void setUp() {
        line = new ZLine(0, -1, 20, 50);
    }

    public void testDuplicate() {
        ZLine l = (ZLine) line.clone();
        doCompare(l, line);
    }

    public void testSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(line);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(leaf.editor().getTransformGroup());
            ZLine savedLine = (ZLine) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedLine, line);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZVisualLeaf leaf = new ZVisualLeaf(line);
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doZSerialize(leaf.editor().getTransformGroup());
            ZLine savedLine = (ZLine) ((ZVisualLeaf)result.getChild(0)).getVisualComponent(0);
            doCompare(savedLine, line);
        } catch (Exception e) {
            assert(e.toString(), false);
        }
    }

    protected void doCompare(ZLine a, ZLine b) {
        Line2D lineA = a.getLine();
        Line2D lineB = a.getLine();

        assertEquals(lineA.getP1(), lineB.getP1());
        assertEquals(lineA.getP2(), lineB.getP2());
        assertEquals(a.getBounds(), b.getBounds());

    }
}