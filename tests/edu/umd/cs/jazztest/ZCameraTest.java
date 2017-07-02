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

import java.awt.geom.*;public class ZCameraTest extends TestCase {
    protected boolean cameraEvent = false;
    protected ZCamera camera = null;

    public ZCameraTest(String name) {
        super(name);
    }

    private boolean contains(ZLayerGroup layer, ZLayerGroup[] array) {
        if (array == null) return false;

        for (int i = 0; i < array.length; i++) {
            if (array[i] == layer) return true;
        }
        return false;
    }

    public void testAddRemoveLayer() {
        ZLayerGroup layer1 = new ZLayerGroup();
        ZLayerGroup layer2 = new ZLayerGroup();
        ZLayerGroup layer3 = new ZLayerGroup();

        ZCamera c = new ZCamera();

        assert(!contains(layer1, c.getLayersReference()));
        c.addLayer(layer1);
        assert(contains(layer1, c.getLayersReference()));
        c.addLayer(layer2);
        c.addLayer(layer3);
        assert(contains(layer2, c.getLayersReference()));
        assert(contains(layer3, c.getLayers()));
        c.removeLayer(layer2);
        assert(!contains(layer2, c.getLayersReference()));
    }

    public void testCameraEvent() {
        final ZCamera camera = new ZCamera();
        camera.translate(2.23423523, 17);
        final AffineTransform originalTransform = camera.getViewTransform();

        camera.addCameraListener(new ZCameraAdapter() {
            public void viewChanged(ZCameraEvent e) {
                assertEquals(e.getCamera(), camera);
                assertEquals(e.getOrigViewTransform(), originalTransform);
                cameraEvent = true;
            }
        });

        camera.translate(1, 1);

        assert(cameraEvent);
    }

    public void testTrimToSize() {
        ZLayerGroup layer1 = new ZLayerGroup();
        ZLayerGroup layer2 = new ZLayerGroup();
        ZLayerGroup layer3 = new ZLayerGroup();

        ZCamera c = new ZCamera();
        c.trimToSize();

        c.addLayer(layer1);
        c.addLayer(layer2);
        c.addLayer(layer3);

        c.trimToSize();

        assert(c.getLayersReference().length == 3);
    }

    public void testDuplicate() {
        doCompare((ZCamera)camera.clone(), camera);
    }

    public void testSerialize() {
        try {
            ZCamera result = (ZCamera) FileSavingSimulator.doSerialize(camera);
            doCompare(result, camera);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZCamera result = (ZCamera) FileSavingSimulator.doZSerialize(camera);
            doCompare(result, camera);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    protected void doCompare(ZCamera a, ZCamera b) {
        assertEquals(a.getBounds(), b.getBounds());
        assertEquals(a.getFillColor(), b.getFillColor());
        assertEquals(a.getViewTransform(), b.getViewTransform());
        assert(a.getMagnification() == b.getMagnification());
    }

    public void setUp() {
        camera = new ZCamera();
        camera.scale(20);
    }
}