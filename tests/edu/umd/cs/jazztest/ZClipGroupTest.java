/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;
import junit.framework.*;
import edu.umd.cs.jazz.event.*;
import java.util.*;
import edu.umd.cs.jazztest.iotest.*;

/**
 * Unit test for ZClipGroup.
 * @author: Jesse Grosjean
 */
public class ZClipGroupTest extends TestCase {
    protected ZClipGroup clip = null;

    public ZClipGroupTest(String name) {
        super(name);
    }



    public void testDuplicate() {
        ZClipGroup copy = (ZClipGroup) clip.clone();
        doCompare(clip, copy);
    }

    public void setUp() {
        clip = new ZClipGroup();
        clip.setClip(new ZRectangle(0, 0, 100, 100));
    }

    public void testSerialize() {
        try {
            ZClipGroup result = (ZClipGroup) FileSavingSimulator.doSerialize(clip);
            doCompare(result, clip);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }

    public void testZSerialize() {
        try {
            ZClipGroup result = (ZClipGroup) FileSavingSimulator.doZSerialize(clip);
            doCompare(result, clip);
        } catch (Exception e) {
            assert(e.getMessage(), false);
        }
    }
    protected void doCompare(ZClipGroup a, ZClipGroup b) {
        ZRectangle arect = (ZRectangle) a.getClip();
        ZRectangle brect = (ZRectangle) b.getClip();

        assert(arect != brect);
        assert(arect.getShape().equals(brect.getShape()));
    }}