/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.io.*;

/**
 * Unit test for ZLabel.
 * @author: Jesse Grosjean
 */
public class ZLabelTest extends TestCase {
    protected ZLabel label;

    public ZLabelTest(String name) {
        super(name);
    }

    public void setUp() {
        label = new ZLabel("Hello World");
        label.getBoundsReference();
    }

    public void testDuplicate() {
        ZLabel l = (ZLabel) label.clone();
        doCompare(l, label);
    }

    public void testSerialize() {
        try {
            ZLabel result = (ZLabel) FileSavingSimulator.doSerialize(label);
            doCompare(result, label);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testTranslate() {
        assertTrue(label.getTranslateX() == 0);
        assertTrue(label.getTranslateY() == 0);

        label.setTranslateX(1);
        assertTrue(label.getTranslateX() == 1);
        assertTrue(label.getTranslateY() == 0);

        label.setTranslateY(1);
        assertTrue(label.getTranslateX() == 1);
        assertTrue(label.getTranslateY() == 1);

        label.setTranslation(new java.awt.geom.Point2D.Double(2, 2));
        assertTrue(label.getTranslateX() == 2);
        assertTrue(label.getTranslateY() == 2);
    }
    protected void doCompare(ZLabel a, ZLabel b) {
        assertEquals(a.getText(), b.getText());
        assertEquals(a.getTranslation(), b.getTranslation());
        assertEquals(a.getPenColor(), b.getPenColor());
        assertEquals(a.getBounds(), b.getBounds());
    }

    public void testZSerialize() {
        try {
            ZLabel result = (ZLabel) FileSavingSimulator.doZSerialize(label);
            doCompare(result, label);
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }
}