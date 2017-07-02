/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.*;
import javax.swing.*;
import edu.umd.cs.jazz.*;
import edu.umd.cs.jazztest.eventtest.*;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZImage.
 * @author: Jesse Grosjean
 */
public class ZImageTest extends TestCase {
    protected ZImage image;

    public ZImageTest(String name) {
        super(name);
    }
    public void setUp() {
        image = new ZImage();
    }
    public void testTranslate() {
        assertTrue(image.getTranslateX() == 0);
        assertTrue(image.getTranslateY() == 0);

        image.setTranslateX(1);
        assertTrue(image.getTranslateX() == 1);
        assertTrue(image.getTranslateY() == 0);

        image.setTranslateY(1);
        assertTrue(image.getTranslateX() == 1);
        assertTrue(image.getTranslateY() == 1);

        image.setTranslation(new java.awt.geom.Point2D.Double(2, 2));
        assertTrue(image.getTranslateX() == 2);
        assertTrue(image.getTranslateY() == 2);
    }
    protected void doCompare(ZImage a, ZImage b) {
        assertEquals(a.getBounds(), b.getBounds());
        assertEquals(a.getFileName(), b.getFileName());
        assertEquals(a.getImage(), b.getImage());
        assertEquals(a.getTranslation(), b.getTranslation());
        assertEquals(a.getBounds(), b.getBounds());

    }

    public void testDuplicate() {
        ZImage i = (ZImage) image.clone();
        doCompare(i, image);
    }

    public void testSerialize() {
        try {
            ZImage result = (ZImage) FileSavingSimulator.doSerialize(image);
            doCompare(result, image);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZImage result = (ZImage) FileSavingSimulator.doZSerialize(image);
            doCompare(result, image);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }
}