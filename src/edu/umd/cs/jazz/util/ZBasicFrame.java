/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * <b>ZBasicFrame</b> defines a simple top-level Swing Frame (i.e., Window) for use by Jazz.
 * It is intended to be the basis for simple applications, and so also defines
 * basic pan/zoom event handlers.
 *
 * @author  Benjamin B. Bederson
 */
public class ZBasicFrame extends JFrame {
    static protected int WIDTH = 500;
    static protected int HEIGHT = 500;

				// Look & Feel types
    static final public int WINDOWS_LAF = 1;
    static final public int METAL_LAF   = 2;
    static final public int MOTIF_LAF   = 3;
    static protected String windowsClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    static protected String metalClassName   = "javax.swing.plaf.metal.MetalLookAndFeel";
    static protected String motifClassName   =  "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

    /**
     * The component in the frame that Jazz renders onto.
     * @serial
     */
    protected ZBasicComponent        component; 

    /**
     * The event handler that supports panning
     * @serial
     */
    protected ZPanEventHandler            panEventHandler;

    /**
     * The event handler that supports zooming
     * @serial
     */
    protected ZoomEventHandlerRightButton zoomEventHandler;

    /**
     * The window listener for the default basic frame that exits
     * the application when the main window is closed.  Remove
     * this listener to avoid having the application exit when the frame is closed.
     * @serial
     */
    protected WindowListener         windowListener = null;

    /**
     * Creates a new top-level window with a basic Jazz scenegraph in a component within the frame.
     * The scenegraph consists of a root, a node, and a camera that looks onto that node, plus
     * a surface that is mapped to the window.
     * Each of the scenegraph elements can be used via the accessor functions.
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicFrame() {
	this(false, null, null);
    }

    /**
     * Creates a new top-level window with a basic Jazz scenegraph in a component within the frame.
     * The component can either fill the frame, or it can be put within an
     * internal frame in a Swing desktop environment.
     * The scenegraph consists of a root, a node, and a camera that looks onto that node, plus
     * a surface that is mapped to the window.
     * Each of the scenegraph elements can be used via the accessor functions.
     * @param desktopRequested If true, then the Jazz window is created as an internal frame in a Swing desktop.  Else, it is in a Swing JFrame
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicFrame(boolean desktopRequested) {
	this(desktopRequested, null, null);
    }

    /**
     * Creates a new top-level window with a basic Jazz scenegraph in a component within the frame.
     * The component can either fill the frame, or it can be put within an
     * internal frame in a Swing desktop environment.
     * The scenegraph consists of a root, a node, and a camera that looks onto that node, plus
     * a surface that is mapped to the window.
     * Each of the scenegraph elements can be used via the accessor functions.
     * <p>
     * To attach a new frame to an existing scenegraph, then specify the root and layer parameters.
     * The root should be an existing scenegraph, and the layer should be an existing layer
     * that should be seen on this frame.  A new camera and surface will be created, and
     * the camera will be set to look at the layer.  If root and layer are specified as null,
     * then a new scenegraph will be created.
     * @param desktopRequested If true, then the Jazz window is created as an internal frame in a Swing desktop.  Else, it is in a Swing JFrame
     * @param root Existing root of scenegraph, or null for none
     * @param layer Existing layer of scenegraph, or null for none
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicFrame(boolean desktopRequested, ZRootNode root, ZNode layer) {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	catch (Exception exc) {
	    System.err.println("Error loading L&F: " + exc);
	}

	windowListener = new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	};
	addWindowListener(windowListener);

	setBounds(100, 100, WIDTH, HEIGHT);
	setResizable(true);
	setBackground(null);
	this.setVisible(true);
	Container container = getContentPane();

				// If request for a desktop, then create a Swing desktop
				// Else - just put the jazz window in a regular JFrame.
	if (desktopRequested) {
	    JDesktopPane desktop = new JDesktopPane();
	    getContentPane().add(desktop);
	    
	    JInternalFrame frame = new JInternalFrame();
	    frame.setBounds(50, 50, 400, 400);
	    frame.setVisible(true);
	    frame.setResizable(true);
	    container = frame.getContentPane();
	    desktop.add(frame);
	}

				// Create a basic Jazz scene, and attach it to this window
	if ((root == null) || (layer == null)) {
	    component = new ZBasicComponent();
	} else {
	    component = new ZBasicComponent(root, layer);
	}
	component.requestFocus();   // We want to be able to get key events, so request the focus

	container.add(component);
	show();

	activateEventHandlers();    // Create some basic event handlers

	component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Return the camera associated with the primary surface.
     * @return the camera
     */
    public ZCamera getCamera() {
	if (component == null) {
	    return null;
	} else {
	    return component.getCamera();
	}
    }

    /**
     * Return the surface.
     * @return the surface
     */    
    public ZSurface getSurface() {
	if (component == null) {
	    return null;
	} else {
	    return component.getSurface();
	}
    }
    
    /**
     * Return the root of the scenegraph.
     * @return the root
     */
    public ZRootNode getRoot() {
	if (component == null) {
	    return null;
	} else {
	    return component.getRoot();
	}
    }
    
    /**
     * Return the "layer".  That is, the single node that
     * the camera looks onto to start.
     * @return the node
     */
    public ZNode getLayer() {
	if (component == null) {
	    return null;
	} else {
	    return component.getLayer();
	}
    }
    
    /**
     * Return the component that the surface is attached to.
     * @return the component
     */
    public ZBasicComponent getComponent() {
	return component;
    }

    /**
     * Returns the window listener for the default basic frame that exits
     * the application when the main window is closed.  Remove
     * this listener to avoid having the application exit when the frame is closed.
     */
    public WindowListener getWindowListener() {
	return windowListener;
    }

    /**
     * Return the pan event handler.
     * @return the pan event handler.
     */
    public ZPanEventHandler getPanEventHandler() {
	return panEventHandler;
    }

    /**
     * Return the zoom event handler.
     * @return the zoom event handler.
     */
    public ZoomEventHandlerRightButton getZoomEventHandler() {
	return zoomEventHandler;
    }

    /**
     * Activate the default event handlers for ZBasicFrame.
     * This turns on basic panning and zooming event handlers for the mouse,
     * so that the left button pans, and the right button zooms.
     * If the event handlers are already activated, then this does nothing.
     */
    public void activateEventHandlers() {
	if (panEventHandler == null) {
	    panEventHandler = new ZPanEventHandler(component, component.getSurface());
	}
	if (zoomEventHandler == null) {
	    zoomEventHandler = new ZoomEventHandlerRightButton(component, component.getSurface());
	}
	panEventHandler.activate();
	zoomEventHandler.activate();
    }

    /**
     * Deactivate the default event handlers for ZBasicFrame.
     * If the event handlers are already deactivated, then this does nothing.
     */
    public void deactivateEventHandlers() {
	if (panEventHandler != null) {
	    panEventHandler.deactivate();
	}
	if (zoomEventHandler != null) {
	    zoomEventHandler.deactivate();
	}
    }

    /**
     * Set the Swing look and feel.
     * @param laf The look and feel, can be WINDOWs_LAF, METAL_LAF, or MOTIF_LAF
     */
    public void setLookAndFeel(int laf) {
	try {
	    switch (laf) {
	    case WINDOWS_LAF:
		UIManager.setLookAndFeel(windowsClassName);
		break;
	    case METAL_LAF:
		UIManager.setLookAndFeel(metalClassName);
		break;
	    case MOTIF_LAF:
		UIManager.setLookAndFeel(motifClassName);
		break;
	    default:
		UIManager.setLookAndFeel(metalClassName);
	    }
	    SwingUtilities.updateComponentTreeUI(this);
	}
	catch (Exception exc) {
	    System.err.println("Error loading L&F: " + exc);
	}
     }

    //////////////////////////////////////////////////////////////////////////
    //
    // TESTING CODE
    //
    //////////////////////////////////////////////////////////////////////////

    /**
     * Simple test that creates a basic application and puts some text in the scenegraph.
     */
    static public void main(String s[]) {
	ZBasicFrame app = new ZBasicFrame(true);
	ZText text = new ZText("Hello World!");
	ZNode node = new ZNode(text);
	node.getTransform().translate(100, 100);
	app.getLayer().addChild(node);
	app.getSurface().restore();
    }
}
