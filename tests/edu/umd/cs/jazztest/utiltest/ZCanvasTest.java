/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.utiltest;

import java.util.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.*;
import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.ZText;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import edu.umd.cs.jazztest.eventtest.*;

/**
 * Unit test for ZCanvas
 * @author: Jesse Grosjean
 */
public class ZCanvasTest extends TestCase {
    public ZCanvasTest(String name) {
        super(name);
    }

    public void testScrollbars() {
    /*    ZCanvasSimulator canvas = new ZCanvasSimulator();
        JScrollPane scrollPane = new JScrollPane(canvas);

        JFrame frame = new JFrame();
        frame.setBounds(100, 100, 400, 400);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);

	ZRectangle rect = new ZRectangle(0, 0, 50, 50);
	rect.setFillColor(Color.red);
        final ZVisualLeaf node = new ZVisualLeaf(rect);
        canvas.getLayer().addChild(node);

        // node inside camera view bounds.
        assert(!canvas.isShowingHorizontalScrollBar());
        assert(!canvas.isShowingVerticalScrollBar());

        // node outside left camera view bounds.
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    node.editor().getTransformGroup().translate(-10, 0);
		}
	    });
	}
	catch (Exception e) {
	    assert(false);
	}
        assert(canvas.isShowingHorizontalScrollBar());
        assert(!canvas.isShowingVerticalScrollBar());

        // node outside left and outside above camera view bounds.
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    node.editor().getTransformGroup().translate(0, -10);
		}
	    });
	}
	catch (Exception e) {
	    assert(false);
	}
        assert(canvas.isShowingHorizontalScrollBar());
        assert(canvas.isShowingVerticalScrollBar());

        // node outside above camera view bounds.
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    node.editor().getTransformGroup().translate(10, 0);
		}
	    });
	}
	catch (Exception e) {
	    assert(false);
	}
        assert(!canvas.isShowingHorizontalScrollBar());
        assert(canvas.isShowingVerticalScrollBar());

        // node inside camera view bounds.
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    node.editor().getTransformGroup().translate(0, 10);
		}
	    });
	}
	catch (Exception e) {
	    assert(false);
	}
        assert(!canvas.isShowingHorizontalScrollBar());
        assert(!canvas.isShowingVerticalScrollBar());

        frame.dispose(); */
    }
}
