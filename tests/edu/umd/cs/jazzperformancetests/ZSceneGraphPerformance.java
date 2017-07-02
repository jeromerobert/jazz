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

    public ZSceneGraphPerformance(String name) {
        super(name);
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

    }}