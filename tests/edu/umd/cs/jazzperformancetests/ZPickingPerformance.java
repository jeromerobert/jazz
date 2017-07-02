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

public class ZPickingPerformance extends TestCase {

    ZCanvas fCanvas;

    public ZPickingPerformance(String name) {
        super(name);
    }

    public void setUp() {
        fCanvas = new ZCanvas();
        fCanvas.getCamera().setBounds(0, 0, 400, 400);
    }

    public double pickSequence() {
        ZDrawingSurface surface = ZComponentFactory.canvasInstance().getDrawingSurface();
        ZVisualLeaf leaf = ZComponentFactory.leafInstance();
        ZCamera camera = surface.getCamera();

        System.gc();

        long startTime = System.currentTimeMillis();

        for (double i = 1; i < 1000; i++) {
            surface.pick(50, 50);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        return (double )totalTime / 1000.0;
    }

    public void testPickEllipse() {
        ZEllipse ellipse = ZComponentFactory.buildEllipse();
        ZComponentFactory.leafInstance().setVisualComponent(ellipse);
        ZPerformanceLog.instance().logTest("Pick ellipse", pickSequence());
        ellipse.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Pick ellipse stroke", pickSequence());
    }

    public void testPickLabel() {
        ZLabel component = ZComponentFactory.buildLabel();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick label", pickSequence());
    }

    public void testPickLine() {
        ZLine line = ZComponentFactory.buildLine();
        ZComponentFactory.leafInstance().setVisualComponent(line);
        ZPerformanceLog.instance().logTest("Pick Line", pickSequence());
    }

    public void testPickNullVisualComponent() {
        ZNullVisualComponent component = ZComponentFactory.buildNullVisualComponent();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick null visual component", pickSequence());
    }

    public void testPickPolygon() {
        ZPolygon component = ZComponentFactory.buildPolygon();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick polygon", pickSequence());
        component.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Pick polygon stroke", pickSequence());
    }

    public void testPickPolyline() {
        ZPolyline component = ZComponentFactory.buildPolyline();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick polyline", pickSequence());
    }

    public void testPickRectangle() {
        ZRectangle component = ZComponentFactory.buildRectangle();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick rectangle", pickSequence());
        component.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Pick rectangle stroke", pickSequence());
    }

    public void testPickRoundedRect() {
        ZRoundedRectangle component = ZComponentFactory.buildRoundedRectangle();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick roundedrectangle", pickSequence());
        component.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Pick roundedrectangle stroke", pickSequence());
    }

    public void testPickScenegraph() {
        fCanvas.getLayer().addChild(ZComponentFactory.buildScenegraph());

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            ZSceneGraphPath aPath = fCanvas.getCamera().getDrawingSurface().pick(1, 1, 5);
        }
        long totalTime = System.currentTimeMillis() - startTime;

        ZPerformanceLog.instance().logTest("SceneGraph Picking", (double) totalTime / 500.0);
    }

    public void testPickText() {
        ZText component = ZComponentFactory.buildText();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Pick text", pickSequence());
    }
}