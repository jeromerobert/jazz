/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazztest.iotest.*;
import edu.umd.cs.jazz.io.*;
import java.awt.geom.*;

/**
 * Unit test for ZSwing.
 * @author: Jesse Grosjean
 */
public class ZSwingTest extends TestCase {

    protected ZSwing swing;

    public ZSwingTest(String name) {
        super(name);
    }

    public void setUp() {
        swing = new ZSwing(new edu.umd.cs.jazz.util.ZCanvas(), new javax.swing.JTextArea());
    }

    public void testDuplicate() {
        ZSwing s = (ZSwing) swing.clone();
        doCompare(s, swing);
    }

    public void testSerialize() {
        try {
            ZSwing result = (ZSwing) FileSavingSimulator.doSerialize(swing);
            doCompare(result, swing);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    protected void doCompare(ZSwing a, ZSwing b) {
        assert(a != null);
        assert(b != null);
        assert(a != b);
    }
}