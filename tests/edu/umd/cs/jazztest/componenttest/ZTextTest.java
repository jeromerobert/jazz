/**
 * Copyright 2000-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.ZText;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZText.
 * @author: Jesse Grosjean
 */
public class ZTextTest extends TestCase {
    protected ZText text;

    public ZTextTest(String name) {
        super(name);
    }

    public void setUp() {
        text = new ZText("Hello World");
    }

    public void testTranslate() {
        assertTrue(text.getTranslateX() == 0);
        assertTrue(text.getTranslateY() == 0);

        text.setTranslateX(1);
        assertTrue(text.getTranslateX() == 1);
        assertTrue(text.getTranslateY() == 0);

        text.setTranslateY(1);
        assertTrue(text.getTranslateX() == 1);
        assertTrue(text.getTranslateY() == 1);

        text.setTranslation(new java.awt.geom.Point2D.Double(2, 2));
        assertTrue(text.getTranslateX() == 2);
        assertTrue(text.getTranslateY() == 2);
    }

    public void testDuplicate() {
        doCompare((ZText)text.clone(), text);
    }

    public void testSerialize() {
        try {
            ZText result = (ZText) FileSavingSimulator.doSerialize(text);
            doCompare(result, text);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZText result = (ZText) FileSavingSimulator.doZSerialize(text);
            doCompare(result, text);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    protected void doCompare(ZText a, ZText b) {
        assertEquals(a.getBackgroundColor(), b.getBackgroundColor());
        assertEquals(a.getText(), b.getText());
        assertTrue(a.getEditable() == b.getEditable());
    }
}