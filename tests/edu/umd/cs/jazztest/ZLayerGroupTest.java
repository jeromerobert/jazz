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
 * Unit test for ZLayerGroup.
 * @author: Jesse Grosjean
 */
public class ZLayerGroupTest extends TestCase {
    protected ZLayerGroup layer = null;
    protected ZCamera camera = null;

    public ZLayerGroupTest(String name) {
        super(name);
    }

    public void setUp() {
        camera = new ZCamera();
        layer = new ZLayerGroup();
        camera.addLayer(layer);
    }

    public void testSerialize() {
        try {
            ZLayerGroup result = (ZLayerGroup) FileSavingSimulator.doSerialize(layer);
            doCompare(result, layer);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZLayerGroup result = (ZLayerGroup) FileSavingSimulator.doZSerialize(layer);
            assertTrue(result.getNumCameras() == 0); // since camera did not get saved.
            ZCamera c = (ZCamera) FileSavingSimulator.doZSerialize(camera);
            doCompare(c.getLayersReference()[0], layer);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    protected void doCompare(ZLayerGroup a, ZLayerGroup b) {
        assertEquals(a.getNumCameras(), b.getNumCameras());
    }

    public void testDuplicate() {
        ZLayerGroup l = (ZLayerGroup) layer.clone();
        assertTrue(l.getNumCameras() == 0); // since camera did not get cloned this should not have any.

        ZCamera c = (ZCamera) camera.clone();
        doCompare(camera.getLayersReference()[0], layer);
    }

    public void testCreation() {
        ZLayerGroup g = new ZLayerGroup(new ZNode());
    }
}