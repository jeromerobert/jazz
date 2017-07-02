/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import edu.umd.cs.jazz.util.*;

public class ZComponentFactory {

    public static Random random = new Random(System.currentTimeMillis());
    public static ZCanvas theCanvas;
    public static ZVisualLeaf theLeaf;

    public static ZVisualComponent buildRandomComponent() {
        switch (random.nextInt(8)) {
            case 0: {
                return buildRectangle();
            }
            case 1: {
                return buildRoundedRectangle();
            }
            case 2: {
                return buildEllipse();
            }
            case 3: {
                return buildLabel();
            }
            case 4: {
                return buildText();
            }
            case 5: {
                return buildLine();
            }
            case 6: {
                return buildPolygon();
            }
            case 7: {
                return buildPolyline();
            }
        }
        return null;
    }

    public static ZRectangle buildRectangle() {
        return new ZRectangle(0, 0, 100, 100);
    }

    public static ZRoundedRectangle buildRoundedRectangle() {
        return new ZRoundedRectangle(0, 0, 100, 100, 5, 5);
    }

    public static ZEllipse buildEllipse() {
        return new ZEllipse(0, 0, 100, 100);
    }

    public static ZText buildText() {
        return new ZText("This is a sample text area\n And this is some more\ntext in the text area.");
    }

    public static ZLabel buildLabel() {
        return new ZLabel("Sample Label");
    }

    public static ZPolyline buildPolyline() {
        ZPolyline result = new ZPolyline(0, 0, 100, 100);
        result.lineTo(50, 50);
        result.lineTo(23, 65);
        result.lineTo(15, 70);
        result.lineTo(1, 89);
        result.lineTo(96, 50);
        result.lineTo(18, 12);
        result.lineTo(32, 67);
        result.lineTo(3, 1);
        return result;
    }

    public static ZPolygon buildPolygon() {
        ZPolygon result = new ZPolygon(0, 0, 100, 100);
        result.lineTo(50, 50);
        result.lineTo(23, 65);
        result.lineTo(15, 70);
        result.lineTo(1, 89);
        result.lineTo(96, 50);
        result.lineTo(18, 12);
        result.lineTo(32, 67);
        result.lineTo(3, 1);
        return result;
    }

    public static ZLine buildLine() {
        return new ZLine(0, 0, 100, 100);
    }

    public static ZGroup buildScenegraph() {
        ZGroup result = new ZGroup();
        ZGroup lastGroup = result;
        ZVisualComponent component = ZComponentFactory.buildNullVisualComponent();

        for (int j = 0; j < 10; j++) {
            ZGroup group = new ZGroup();

            for (int i = 0; i < 100; i++) {
                ZVisualLeaf leaf = new ZVisualLeaf(component);
                group.addChild(leaf);
                leaf.editor().getTransformGroup().rotate(30);
            }

            lastGroup.addChild(group.editor().getTop());
            group.editor().getTop().lower();
            lastGroup = group;
        }
        return result;
    }

    public static ZTimingLeaf leafInstance() {
        if (theLeaf == null) {
            theLeaf = new ZTimingLeaf();
            canvasInstance().getLayer().addChild(theLeaf);
        }
        return (ZTimingLeaf) theLeaf;
    }

    public static ZCanvas canvasInstance() {
        if (theCanvas == null) {
            theCanvas = new ZTimingCanvas();
            JFrame frame = new JFrame();

            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            frame.setBounds(0, 0, 400, 400);
            frame.setResizable(false);
            frame.setBackground(null);
            frame.setVisible(true);

            frame.getContentPane().add(theCanvas);
            frame.validate();
        }
        return theCanvas;
    }

    public static ZNullVisualComponent buildNullVisualComponent() {
        return new ZNullVisualComponent();
    }
}