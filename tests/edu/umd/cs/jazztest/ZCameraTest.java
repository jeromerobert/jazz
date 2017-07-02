/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import java.util.*;
import java.awt.geom.*;

import junit.framework.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.event.*;

public class ZCameraTest extends TestCase {
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

        assertTrue(!contains(layer1, c.getLayersReference()));
        c.addLayer(layer1);
        assertTrue(contains(layer1, c.getLayersReference()));
        c.addLayer(layer2);
        c.addLayer(layer3);
        assertTrue(contains(layer2, c.getLayersReference()));
        assertTrue(contains(layer3, c.getLayers()));
        c.removeLayer(layer2);
        assertTrue(!contains(layer2, c.getLayersReference()));
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

        assertTrue(cameraEvent);
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

        assertTrue(c.getLayersReference().length == 3);
    }

    public void testDuplicate() {
        doCompare((ZCamera)camera.clone(), camera);
    }

    public void testSerialize() {
        try {
            ZCamera result = (ZCamera) FileSavingSimulator.doSerialize(camera);
            doCompare(result, camera);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZCamera result = (ZCamera) FileSavingSimulator.doZSerialize(camera);
            doCompare(result, camera);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    protected void doCompare(ZCamera a, ZCamera b) {
        assertEquals(a.getBounds(), b.getBounds());
        assertEquals(a.getFillColor(), b.getFillColor());
        assertEquals(a.getViewTransform(), b.getViewTransform());
        assertTrue(a.getMagnification() == b.getMagnification());
    }

    public void setUp() {
        camera = new ZCamera();
        camera.scale(20);
    }
}