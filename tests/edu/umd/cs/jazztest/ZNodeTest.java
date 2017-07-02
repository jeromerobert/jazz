/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;
import junit.framework.*;
import edu.umd.cs.jazz.event.*;
import java.util.*;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZNode.
 * @author: Jesse Grosjean
 */
public class ZNodeTest extends TestCase {
    boolean nodeEventFired = false;
    protected ZNode node = null;

    public ZNodeTest(String name) {
        super(name);
    }

    public void testAddRemoveProperty() {

        String key1 = "key1";
        String key2 = "key2";

        ZNode node = new ZNode();

        assertNull(node.getClientProperty(key1));
        node.putClientProperty(key1, node);
        node.putClientProperty(key2, key1);
        assertEquals(node, node.getClientProperty(key1));
        assertEquals(key1, node.getClientProperty(key2));
        node.putClientProperty(key1, null);
        assertNull(node.getClientProperty(key1));
        node.putClientProperty(key2, null);
        assertNull(node.getClientProperty(key2));
    }

    public void testNodeEvent() {
        final ZVisualLeaf aNode = new ZVisualLeaf();

        aNode.addNodeListener(new ZNodeAdapter() {
            public void globalBoundsChanged(ZNodeEvent e) {
                assertEquals(e.getNode(), aNode);
                nodeEventFired = true;
            }
        });

        aNode.addVisualComponent(new ZRectangle(0, 0, 1, 1));

        assert(nodeEventFired);
    }

    public void testTrimToSize() {
        ZNode node = new ZNode();
        node.trimToSize();
        node.putClientProperty("a", "b");
        node.putClientProperty("c", "b");
        node.putClientProperty("z", "b");
        node.trimToSize();
    }

    protected void doCompare(ZNode a, ZNode b) {
        assertEquals(a.getClientProperty(a), "property");
        assertEquals(b.getClientProperty(b), "property");
    }

    public void testDuplicate() {
        ZNode copy = (ZNode) node.clone();
        doCompare(node, copy);

        node.putClientProperty(node, null);
        assert(node.getClientProperty(node) == null);
        assert(copy.getClientProperty(copy) != null);
    }

    public void setUp() {
        node = new ZNode();
        node.putClientProperty(node, "property");
    }

    public void testSerialize() {
        try {
            ZNode result = (ZNode) FileSavingSimulator.doSerialize(node);
            doCompare(result, node);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZNode result = (ZNode) FileSavingSimulator.doZSerialize(node);
            doCompare(result, node);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }
}