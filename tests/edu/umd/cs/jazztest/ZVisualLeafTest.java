/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import junit.framework.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZVisualLeaf.
 * @author: Jesse Grosjean
 */
public class ZVisualLeafTest extends TestCase {
    protected ZVisualLeaf leaf = null;
    ZRectangle rect1 = null;
    ZRectangle rect2 = null;

    public ZVisualLeafTest(String name) {
        super(name);
    }

    public void setUp() {
        rect1 = new ZRectangle(0,0,10,10);
        rect2 = new ZRectangle(0,0,10,10);
        leaf = new ZVisualLeaf(rect1);
        leaf.addVisualComponent(rect2);
    }
    public void testClone() {

        ZVisualLeaf lclone = (ZVisualLeaf) leaf.clone();
        doCompare(leaf, lclone);

        assertTrue(leaf.getFirstVisualComponent() == rect1);
        assertTrue(leaf.getVisualComponent(1) == rect2);

        // Make sure that when an ZVisualGroup gets cloned
        // its visual components are not cloned.
        assertTrue(lclone.getFirstVisualComponent() != rect1);
        assertTrue(lclone.getVisualComponent(1) != rect2);

    }

    public void testZSerialize() {
        try {
            ZVisualLeaf result = (ZVisualLeaf) FileSavingSimulator.doZSerialize(leaf);
            doCompare(result, leaf);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testSerialize() {
        try {
            ZVisualLeaf result = (ZVisualLeaf) FileSavingSimulator.doSerialize(leaf);
            doCompare(result, leaf);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    protected void doCompare(ZVisualLeaf a, ZVisualLeaf b) {
    }
}