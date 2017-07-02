/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * Basic test of per-node event handlers
 * @author Benjamin B. Bederson
 */
public class EventTest extends JFrame {
    protected ZCanvas             canvas; 

    public EventTest() {
				// Support exiting application
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

				// Set up basic frame
	setBounds(100, 100, 400, 400);
	setResizable(true);
	setBackground(null);
	setVisible(true);
	canvas = new ZCanvas();
	getContentPane().add(canvas);
	validate();

				// Add some background rectangles
	canvas.getLayer().addChild(new ZVisualLeaf(new ZRectangle(50, 50, 60, 70)));
	canvas.getLayer().addChild(new ZVisualLeaf(new ZRectangle(120, 130, 40, 25)));
				// Then, add the interactive rectangle
	ZRectangle rect = new ZRectangle(100, 100, 50, 50);
	rect.setPenColor(Color.red);
	rect.setFillColor(Color.red);
	ZVisualLeaf leaf = new ZVisualLeaf(rect);
	canvas.getLayer().addChild(leaf);
	leaf.addMouseListener(new ZMouseAdapter() {
	    public void mouseEntered(ZMouseEvent e) {
		ZNode node1 = e.getNode();
		ZVisualLeaf leaf1 = (ZVisualLeaf)node1;
		ZRectangle rect1 = (ZRectangle)leaf1.getFirstVisualComponent();
		rect1.setPenColor(Color.blue);
	    }
	    public void mouseExited(ZMouseEvent e) {
		ZNode node1 = e.getNode();
		ZVisualLeaf leaf1 = (ZVisualLeaf)node1;
		ZRectangle rect1 = (ZRectangle)leaf1.getFirstVisualComponent();
		rect1.setPenColor(Color.red);
	    }
	    public void mousePressed(ZMouseEvent e) {
		ZNode node = e.getNode();
		node.editor().getFadeGroup().setAlpha(0.5);
	    }
	    public void mouseReleased(ZMouseEvent e) {
		ZNode node = e.getNode();
		node.editor().getFadeGroup().setAlpha(1.0);
	    }
	});
	leaf.addMouseMotionListener(new ZMouseMotionAdapter() {
	    public void mouseMoved(ZMouseEvent e) {
		ZNode node = e.getNode();
		node.editor().getTransformGroup().rotate(0.01f, 125, 125);
	    }
	    public void mouseDragged(ZMouseEvent e) {
		ZNode node = e.getNode();
		node.editor().getTransformGroup().rotate(0.01f, 125, 125);
	    }
	});
    }

    public static void main(String[] argv) {
	new EventTest();
    }
}
