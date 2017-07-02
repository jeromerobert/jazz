/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
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
 * Unit test for ZGroup.
 * @author: Jesse Grosjean
 */
public class ZGroupTest extends TestCase {
    boolean groupEventFired = false;
    boolean parentGroupEventFired = false;
    protected ZGroup group = null;

    public ZGroupTest(String name) {
        super(name);
    }

    private boolean contains(ZNode node, Iterator i) {
        while (i.hasNext()) {
            ZNode each = (ZNode) i.next();
            if (each == node) return true;
        }
        return false;
    }

    public void testCreation() {
        new ZGroup(new ZNode());
    }

    public void testAddRemoveChild() {

        ZNode child1 = new ZNode();
        ZNode child2 = new ZNode();
        ZNode child3 = new ZNode();

        ZGroup g = new ZGroup();

        assert(!contains(child1, g.getChildrenIterator()));
        g.addChild(child1);
        assert(contains(child1, g.getChildrenIterator()));

        g.addChild(child2);
        g.addChild(child3);

        assert(contains(child3, g.getChildrenIterator()));

        g.removeChild(child2);

        assert(!contains(child2, g.getChildrenIterator()));

        assert(contains(child1, g.getChildrenIterator()));
        assert(contains(child3, g.getChildrenIterator()));

    }

    public void testFireGroupEvent() {
        ZGroup g1 = new ZGroup();
        ZGroup g2 = new ZGroup();
        final ZNode n = new ZNode();
        boolean eventFired = false;

        g2.addChild(g1);

        g1.addGroupListener(new ZGroupAdapter() {
            public void nodeAdded(ZGroupEvent e) {
                assertEquals(e.getChild(), n);
                groupEventFired = true;
            }
        });

        g2.addGroupListener(new ZGroupAdapter() {
            public void nodeAdded(ZGroupEvent e) {
                assertEquals(e.getChild(), n);
                parentGroupEventFired = true;
            }
        });

        g1.addChild(n);

        assert(groupEventFired);
        assert(parentGroupEventFired);
    }

    public void testTrimToSize() {
        ZNode child1 = new ZNode();
        ZNode child2 = new ZNode();
        ZNode child3 = new ZNode();

        ZGroup g = new ZGroup();

        g.trimToSize();

        g.addChild(child1);
        g.addChild(child2);
        g.addChild(child3);

        g.trimToSize();

        assert(g.getChildrenReference().length == 3);
    }
    public void testDuplicateObject() {
        ZGroup group = new ZGroup();
        ZVisualLeaf a = new ZVisualLeaf(new ZRectangle());
        ZNode b = new ZNode();

        group.addChild(a);
        group.addChild(b);

        ZGroup group2 = (ZGroup) group.clone();

        assert(group.getChildrenReference() != group2.getChildrenReference());
        assert(group.getChildrenReference()[0] != group2.getChildrenReference()[0]);
        assert(group.getChildrenReference()[1] != group2.getChildrenReference()[1]);

    }
    public void testExtract() {
        ZGroup g1 = new ZGroup();
        ZGroup g2 = new ZGroup();
        ZGroup g3 = new ZGroup();

        g1.addChild(g2);
        g2.addChild(g3);

        g2.extract();

        assert(g2.getParent() == null);
        assert(g2.getNumChildren() == 0);

        assert(g1.getChildrenReference()[0] == g3);
        assert(g3.getParent() == g1);
    }

    public void testInsertAbove() {
        ZGroup g1 = new ZGroup();
        ZGroup g2 = new ZGroup();
        ZGroup g3 = new ZGroup();

        g1.addChild(g2);


        g3.insertAbove(g2);

        assert(g3.getParent() == g1);
        assert(g1.getChildrenReference()[0] == g3);
        assert(g3.getChildrenReference()[0] == g2);
        assert(g2.getParent() == g3);
    }

    public void testRaiseToLowerTo() {
        ZGroup g1 = new ZGroup();
        ZNode a = new ZNode();
        ZNode b = new ZNode();
        ZNode c = new ZNode();

        g1.addChild(a);
        g1.addChild(b);
        g1.addChild(c);

        assert(g1.getChildrenReference()[0] == a);
        assert(g1.getChildrenReference()[1] == b);
        assert(g1.getChildrenReference()[2] == c);

        g1.raise(a);

        assert(g1.getChildrenReference()[2] == a);

        g1.raiseTo(b, a);

        assert(g1.getChildrenReference()[2] == b);

        c.raise();

        assert(g1.getChildrenReference()[2] == c);

        g1.lowerTo(c, a);

        assert(g1.getChildrenReference()[0] == c);
        assert(g1.getChildrenReference()[1] == a);
        assert(g1.getChildrenReference()[2] == b);
    }

    protected void doCompare(ZGroup a, ZGroup b) {
        assertEquals(a.getNumChildren(), b.getNumChildren());
    }

    public void setUp() {
        group = new ZGroup();
        group.addChild(new ZNode());
    }

    public void testSerialize() {
        try {
            ZGroup result = (ZGroup) FileSavingSimulator.doSerialize(group);
            doCompare(result, group);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZGroup result = (ZGroup) FileSavingSimulator.doZSerialize(group);
            doCompare(result, group);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testBounds() {
        ZGroup g1 = new ZGroup();
        ZGroup g2 = new ZGroup();
        ZVisualLeaf leaf = new ZVisualLeaf(new ZRectangle(0, 0, 50, 50));
        g1.addChild(leaf);
        g1.addChild(g2);

        assertEquals(g1.getBoundsReference(), leaf.getBoundsReference());

        g1.removeChild(leaf);

        g2.editor().getTransformGroup().scale(0.5);
        g2.addChild(leaf);

        assert(!g1.getBoundsReference().equals(leaf.getBoundsReference()));
    }

    public void testUpdateBounds() {
        ZGroup g1 = new ZGroup();
        ZGroup g2 = new ZGroup();
        ZVisualLeaf leaf = new ZVisualLeaf(new ZRectangle(0, 0, 50, 50));

        g1.addChild(g2);
        g2.addChild(leaf);

        assertEquals(g2.getBoundsReference(), g1.getBoundsReference());

        g2.removeChild(leaf);

        assertEquals(g2.getBoundsReference(), g1.getBoundsReference());
    }
}