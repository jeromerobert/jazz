/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import java.util.*;

import junit.framework.*;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZSelectionGroup.
 * @author: Jesse Grosjean
 */
public class ZSelectionGroupTest extends TestCase {

    protected ZSelectionGroup selection = null;

    public ZSelectionGroupTest(String name) {
        super(name);
    }

    public void testDuplicate() {
        ZSelectionGroup copy = (ZSelectionGroup) selection.clone();
        doCompare(selection, copy);
    }

    public void setUp() {
        ZVisualLeaf leaf = new ZVisualLeaf(new ZRectangle(0, 0, 100, 100));
        selection = leaf.editor().getSelectionGroup();
    }

    public void testSerialize() {
        try {
            ZSelectionGroup result = (ZSelectionGroup) FileSavingSimulator.doSerialize(selection);
            doCompare(result, selection);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZSelectionGroup result = (ZSelectionGroup) FileSavingSimulator.doZSerialize(selection);
            doCompare(result, selection);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    protected void doCompare(ZSelectionGroup a, ZSelectionGroup b) {
        assertTrue(a.getBounds().equals(b.getBounds()));
        assertTrue(a.getPenColor().equals(b.getPenColor()));
    }
}