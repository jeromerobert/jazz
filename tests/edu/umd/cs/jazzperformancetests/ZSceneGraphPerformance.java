/**
 * Copyright 2001 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;
import junit.framework.*;
import edu.umd.cs.jazz.event.*;
import java.util.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.util.*;

public class ZSceneGraphPerformance extends TestCase {
    private ZCanvas fCanvas = null;

    public ZSceneGraphPerformance(String name) {
        super(name);
    }

    public void setUp() {
        fCanvas = new ZCanvas();
    }

    public void testUpdateBounds() {

        ZGroup g = new ZGroup();
        new ZGroup().addChild(g);
        ZRectangle rect = new ZRectangle(0, 0, 100, 100);
        g.addChild(new ZVisualLeaf(rect));

        System.gc();

        long startTime = System.currentTimeMillis();


        for (int i = 0; i < 5000; i++) {
            rect.setRect(0 + i, 0, 100, 100);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("Update Bounds", totalTime);

    }

    public void testAdd2000NoTransaction() {
        ZGroup g = fCanvas.getLayer();
        ArrayList l = new ArrayList(2000);
        for (int i = 0; i < 2000; i++) {
            l.add(new ZVisualLeaf(new ZRectangle(i * 5, i * 5, 100, 100)));
        }

        long startTime = System.currentTimeMillis();

        Iterator i = l.iterator();
        while (i.hasNext()) {
            g.addChild((ZNode)i.next());
        }
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("Add 2000, no transaction", totalTime);
    }

    public void testAdd2000WithTransaction() {
        ZGroup g = fCanvas.getLayer();
        ArrayList l = new ArrayList(2000);
        for (int i = 0; i < 2000; i++) {
            l.add(new ZVisualLeaf(new ZRectangle(i * 5, i * 5, 100, 100)));
        }

        long startTime = System.currentTimeMillis();
        g.startTransaction();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            g.addChild((ZNode)i.next());
        }
        g.endTransaction();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("Add 2000, with transaction", totalTime);
    }

    public void testRemove2000NoTransaction() {
        add2000Nodes();
        long startTime = System.currentTimeMillis();
        remove2000Nodes();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("remove 2000, no transaction", totalTime);
    }

    public void testRemove2000WithTransaction() {
        add2000Nodes();
        long startTime = System.currentTimeMillis();
        fCanvas.getLayer().startTransaction();
        remove2000Nodes();
        fCanvas.getLayer().endTransaction();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("remove 2000, with transaction", totalTime);
    }

    public void testTranslate2000NoTransaction() {
        add2000Nodes();
        translate2000Nodes();
        long startTime = System.currentTimeMillis();
        translate2000Nodes();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("translate 2000, no transaction", totalTime);
    }

    public void testTranslate2000WithTransaction() {
        add2000Nodes();
        translate2000Nodes();
        long startTime = System.currentTimeMillis();
        fCanvas.getLayer().startTransaction();
        translate2000Nodes();
        fCanvas.getLayer().endTransaction();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("translate 2000, with transaction", totalTime);
    }

    public void remove2000Nodes() {
        ZGroup g = fCanvas.getLayer();
        ArrayList l = new ArrayList(g.getNumChildren());
        Iterator i = g.getChildrenIterator();
        while (i.hasNext()) {
            l.add(i.next());
        }


        i = l.iterator();
        while (i.hasNext()) {
            ZNode each = (ZNode) i.next();
            g.removeChild(each);
        }

    }

    public void add2000Nodes() {
        ZGroup g = fCanvas.getLayer();
        for (int i = 0; i < 2000; i++) {
            g.addChild(new ZVisualLeaf(new ZRectangle(i * 5, i * 5, 100, 100)));
        }
    }

    public void translate2000Nodes() {
        ZGroup g = fCanvas.getLayer();

        Iterator i = fCanvas.getLayer().getChildrenIterator();
        while (i.hasNext()) {
            ZNode each = (ZNode) i.next();
            each.editor().getTransformGroup().translate(10, 10);
        }
    }

    public void testAdd10000NoTransaction() {
        ZGroup g = fCanvas.getLayer();
        ArrayList l = new ArrayList(10000);
        for (int i = 0; i < 10000; i++) {
            l.add(new ZVisualLeaf(new ZRectangle(i * 5, i * 5, 100, 100)));
        }

        long startTime = System.currentTimeMillis();

        Iterator i = l.iterator();
        while (i.hasNext()) {
            g.addChild((ZNode)i.next());
        }
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("Add 10000, no transaction", totalTime);
    }

    public void testAdd10000WithTransaction() {
        ZGroup g = fCanvas.getLayer();
        ArrayList l = new ArrayList(10000);
        for (int i = 0; i < 10000; i++) {
            l.add(new ZVisualLeaf(new ZRectangle(i * 5, i * 5, 100, 100)));
        }

        long startTime = System.currentTimeMillis();
        g.startTransaction();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            g.addChild((ZNode)i.next());
        }
        g.endTransaction();
        long totalTime = System.currentTimeMillis() - startTime;
        ZPerformanceLog.instance().logTest("Add 10000, with transaction", totalTime);
    }
}