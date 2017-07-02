/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
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
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;

public class ZRenderingPerformance extends TestCase {
    private double tempTime;

    public ZRenderingPerformance(String name) {
        super(name);
    }

    public void setUp() {
        System.gc();
    }

    public void testRenderScenegraph() {
        ZNode aNode = ZComponentFactory.buildScenegraph();
        ZComponentFactory.canvasInstance().getLayer().addChild(aNode);

        ZPerformanceLog.instance().logTest("Rendering SceneGraph", renderSequence());

        ZComponentFactory.canvasInstance().getLayer().removeChild(aNode);
    }

    public void testRenderRectangle() {
        ZRectangle rect = ZComponentFactory.buildRectangle();

        ZComponentFactory.leafInstance().setVisualComponent(rect);
        ZPerformanceLog.instance().logTest("Render Rectangle", renderSequence());

        rect.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Render Rectangle Stroke", renderSequence());

        rect.setFillPaint(java.awt.Color.black);
        rect.setPenPaint(null);
        ZPerformanceLog.instance().logTest("Render Rectangle Fill", renderSequence());
    }

    public double renderSequence() {
        ZTimingCanvas.timeForLastPaint = 0;
        //  we know that the leaf is located at 0, 0, and is size 100, 100.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ZDrawingSurface surface = ZComponentFactory.canvasInstance().getDrawingSurface();
                    ZCamera camera = surface.getCamera();

                    double totalTime = 0;

                    for (double i = 1; i < 100; i++) {
                        camera.setScale(i / 50.0);
                        surface.paintImmediately();
                        totalTime += ZTimingCanvas.timeForLastPaint;
                    }

                    camera.setViewTransform(new java.awt.geom.AffineTransform());
                    tempTime = totalTime / 100;
                }
            });
        } catch (Exception e) {
        }
        return tempTime;
    }

    public void testRenderEllipse() {
        ZEllipse ellipse = ZComponentFactory.buildEllipse();

        ZComponentFactory.leafInstance().setVisualComponent(ellipse);
        ZPerformanceLog.instance().logTest("Render Ellipse", renderSequence());

        ellipse.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Render Ellipse Stroke", renderSequence());

        ellipse.setFillPaint(java.awt.Color.black);
        ellipse.setPenPaint(null);
        ZPerformanceLog.instance().logTest("Render Ellipse Fill", renderSequence());
    }

    public void testRenderImage() {
        ZImage image = new ZImage();

        BufferedImage bufferedImage = null;

        ZComponentFactory.leafInstance().setVisualComponent(image);

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_3BYTE_BGR);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_3BYTE_BGR", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_4BYTE_ABGR);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_4BYTE_ABGR", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_4BYTE_ABGR_PRE", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_BYTE_BINARY);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_BYTE_BINARY", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_BYTE_GRAY);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_BYTE_GRAY", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_BYTE_INDEXED);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_BYTE_INDEXED", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_INT_ARGB", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB_PRE);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_INT_ARGB_PRE", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_BGR);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_INT_BGR", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_INT_RGB", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_USHORT_555_RGB);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_USHORT_555_RGB", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_USHORT_565_RGB);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_USHORT_565_RGB", renderSequence());

        System.gc();
        bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_USHORT_GRAY);
        colorImage(bufferedImage);
        image.setImage(bufferedImage);
        ZPerformanceLog.instance().logTest("Render Image TYPE_USHORT_GRAY", renderSequence());
    }

    private void colorImage(BufferedImage image) {
        for (int i = 2; i < 147; i +=3) {
            for (int j = 2; j < 147; j +=3) {
                image.setRGB(i, j, java.awt.Color.red.getRGB());
                image.setRGB(i-1, j, java.awt.Color.blue.getRGB());
                image.setRGB(i+1, j, java.awt.Color.green.getRGB());
                image.setRGB(i, j-1, java.awt.Color.yellow.getRGB());
                image.setRGB(i, j+1, java.awt.Color.black.getRGB());
                image.setRGB(i-1, j+1, java.awt.Color.white.getRGB());
                image.setRGB(i+1, j+1, java.awt.Color.white.getRGB());
                image.setRGB(i-1, j-1, java.awt.Color.white.getRGB());
                image.setRGB(i+1, j-1, java.awt.Color.white.getRGB());
            }
        }
    }

    public void testRenderLabel() {
        ZLabel label = ZComponentFactory.buildLabel();

        ZComponentFactory.leafInstance().setVisualComponent(label);
        ZPerformanceLog.instance().logTest("Render Label", renderSequence());
    }

    public void testRenderLine() {
        ZLine line = ZComponentFactory.buildLine();

        ZComponentFactory.leafInstance().setVisualComponent(line);
        ZPerformanceLog.instance().logTest("Render Line", renderSequence());
    }

    public void testRenderNullVisualComponent() {
        ZNullVisualComponent component = ZComponentFactory.buildNullVisualComponent();
        ZComponentFactory.leafInstance().setVisualComponent(component);
        ZPerformanceLog.instance().logTest("Render null visual component", renderSequence());
    }

    public void testRenderPolygon() {
        ZPolygon polygon = ZComponentFactory.buildPolygon();

        ZComponentFactory.leafInstance().setVisualComponent(polygon);
        ZPerformanceLog.instance().logTest("Render Polygon", renderSequence());

        polygon.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Render Polygon Stroke", renderSequence());

        polygon.setFillPaint(java.awt.Color.black);
        polygon.setPenPaint(null);
        ZPerformanceLog.instance().logTest("Render Polygon Fill", renderSequence());
    }

    public void testRenderPolyline() {
        ZPolyline polyline = ZComponentFactory.buildPolyline();

        ZComponentFactory.leafInstance().setVisualComponent(polyline);
        ZPerformanceLog.instance().logTest("Render Polyline", renderSequence());
    }

    public void testRenderRoundedRectangle() {
        ZRoundedRectangle rect = ZComponentFactory.buildRoundedRectangle();

        ZComponentFactory.leafInstance().setVisualComponent(rect);
        ZPerformanceLog.instance().logTest("Render RoundedRectangle", renderSequence());

        rect.setFillPaint(null);
        ZPerformanceLog.instance().logTest("Render RoundedRectangle Stroke", renderSequence());

        rect.setFillPaint(java.awt.Color.black);
        rect.setPenPaint(null);
        ZPerformanceLog.instance().logTest("Render RoundedRectangle Fill", renderSequence());
    }

    public void testRenderText() {
        ZText text = ZComponentFactory.buildText();

        ZComponentFactory.leafInstance().setVisualComponent(text);
        ZPerformanceLog.instance().logTest("Render text", renderSequence());
    }
}