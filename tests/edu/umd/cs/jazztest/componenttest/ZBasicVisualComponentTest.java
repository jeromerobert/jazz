/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import java.awt.geom.*;

import junit.framework.*;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.io.*;


/**
 * Unit test for ZBasicVisualComponent.
 * @author: Jesse Grosjean
 */
public class ZBasicVisualComponentTest extends TestCase {

    protected ZPath path;

    public ZBasicVisualComponentTest(String name) {
        super(name);
    }

    public void setUp() {
        GeneralPath p = new GeneralPath();
        p.append(new Ellipse2D.Double(0, 0, 10, 10), true);
        p.append(new Rectangle2D.Double(10, 4, 2, 2), true);
        path = new ZPath(p);
    }

    public void testAbsPenWidthWithNoRoot() {
        ZRectangle rect = new ZRectangle();
        rect.setAbsPenWidth(3.3);
        assert(rect.getAbsPenWidth() == 3.3);
        assert(rect.getPenWidth() == 0);
    }

    public void testSaveNoFill() {
        path.setPenPaint(null);

        try {
            ZPath result = (ZPath) FileSavingSimulator.doSerialize(path);
            assert(result.getPenPaint() == null);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }

        try {
            ZPath result = (ZPath) FileSavingSimulator.doZSerialize(path);
            assert(result.getPenPaint() == null);
        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }
}