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
 * Unit test for ZTransformGroup.
 * @author: Jesse Grosjean
 */
public class ZTransformGroupTest extends TestCase {

    boolean globalChangeEvent = false;
    boolean localChangeEvent = false;
    boolean nodeEventFired = false;
    boolean transformEvent = false;
    protected ZTransformGroup transformGroup = null;

    public ZTransformGroupTest(String name) {
        super(name);
    }

    public void testFireNodeEvent() {
        final ZNode aNode = new ZNode();
        ZGroup aGroup = new ZGroup();
        aGroup.addChild(aNode);

        ZTransformGroup t = aGroup.editor().getTransformGroup();

        aNode.addNodeListener(new ZNodeAdapter() {
            public void globalBoundsChanged(ZNodeEvent e) {
                globalChangeEvent = true;
                assertEquals(aNode, e.getNode());
            }
            public void boundsChanged(ZNodeEvent e) {
                localChangeEvent = true;
                assertEquals(aNode, e.getNode());
            }
        });

        // this should start the recursive call down the tree. and aNode
        // should here about the global change, but not the local.
        t.translate(0, 1);

        assert(!localChangeEvent);
        assert(globalChangeEvent);
    }

    public void testFireTransformEvent() {
        ZNode aNode = new ZNode();
        final ZTransformGroup t = aNode.editor().getTransformGroup();
        t.addTransformListener(new ZTransformAdapter() {
            public void transformChanged(ZTransformEvent e) {
                assertEquals(e.getTransform(), t);
                transformEvent = true;
            }
        });

        t.translate(1, 1);

        assert(transformEvent);
    }

    protected void doCompare(ZTransformGroup a, ZTransformGroup b) {
        assertEquals(a.getTransform(), b.getTransform());
    }

    public void testDuplicate() {
        ZTransformGroup g = (ZTransformGroup) transformGroup.clone();
        doCompare(g, transformGroup);
    }

    public void setUp() {
        transformGroup = new ZTransformGroup();
        transformGroup.setScale(10);
        transformGroup.setRotation(1.5);
    }

    public void testSerialize() {
        try {
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doSerialize(transformGroup);
            doCompare(result, transformGroup);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZTransformGroup result = (ZTransformGroup) FileSavingSimulator.doZSerialize(transformGroup);
            doCompare(result, transformGroup);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }
}