/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * <b>ZBasicApplet</b> is a basic applet that creates a simple extendable
 * applet with a single window for Jazz, and basic pan/zoom event handlers.
 *
 * @author  Benjamin B. Bederson
 */
public class ZBasicApplet extends JApplet {
    static protected String windowsClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    static protected String metalClassName   = "javax.swing.plaf.metal.MetalLookAndFeel";
    static protected String motifClassName   =  "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

				// Look & Feel types
    final static public int WINDOWS_LAF = 1;
    final static public int METAL_LAF   = 2;
    final static public int MOTIF_LAF   = 3;

    /**
     * The component in the frame that Jazz renders onto.
     * @serial
     */
    protected ZBasicComponent        component; 

    /**
     * The root of the scenegraph
     * @serial
     */
    protected ZRootNode		     root;

    /**
     * The camera in the scenegraph
     * @serial
     */
    protected ZCamera                camera;

    /**
     * The surface associated with the component
     * @serial
     */
    protected ZSurface               surface;

    /**
     * The single node that camera looks onto.  It is considered to
     * be the "layer" because many applications will put content
     * under this node which can then be hidden or revealed like a layer.
     * @serial
     */
    protected ZNode                  layer;

    /**
     * The event handler that supports panning
     * @serial
     */
    protected ZEventHandler          panEventHandler;

    /**
     * The event handler that supports zooming
     * @serial
     */
    protected ZEventHandler          zoomEventHandler;

    /**
     * The currently active event handler
     * @serial
     */
    protected ZEventHandler          activeEventHandler=null;

    /**
     * Creates a new basic Jazz applet.
     * It consists of a simple Jazz scenegraph mapped to a simple component in an applet.
     * The scenegraph consists of a root, a node, and a camera that looks onto that node, plus
     * a surface that is mapped to the window.
     * Each of the scenegraph elements can be used via the accessor functions.
     * @see #getRoot()
     * @see #getSurface()
     * @see #getCamera()
     * @see #getLayer()
     */
    public ZBasicApplet() {
    }

    public void init() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	catch (Exception exc) {
	    System.err.println("Error loading L&F: " + exc);
	}

	setBackground(null);
	this.setVisible(true);
	Container container = getContentPane();

				// Create a basic Jazz scene, and attach it to this window
	component = new ZBasicComponent();
	container.add(component);

	surface = component.getSurface();
	camera = component.getCamera();
	root = component.getRoot();
	layer = component.getLayer();

	activateEventHandlers();    // Create some basic event handlers

	component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	component.requestFocus();   // We want to be able to get key events, so request the focus

	/*
	  From JApplet docs:
	  Both Netscape Communicator and Internet Explorer 4.0 unconditionally 
	  print an error message to the Java console when an applet attempts to
	  access the AWT system event queue. Swing applets do this once, to check
	  if access is permitted. To prevent the warning message in a production
	  applet one can set a client property called "defeatSystemEventQueueCheck" 
	  on the JApplets RootPane to any non null value.
	*/
	
	JRootPane rp = getRootPane();
	rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
    }

    /**
     * Return the camera associated with the primary surface.
     * @return the camera
     */
    public ZCamera getCamera() {
	return camera;
    }

    /**
     * Return the surface.
     * @return the surface
     */    
    public ZSurface getSurface() {
	return surface;
    }
    
    /**
     * Return the root of the scenegraph.
     * @return the root
     */
    public ZRootNode getRoot() {
	return root;
    }
    
    /**
     * Return the "layer".  That is, the single node that
     * the camera looks onto to start.
     * @return the node
     */
    public ZNode getLayer() {
	return layer;
    }
    
    /**
     * Return the component that the surface is attached to.
     * @return the component
     */
    public ZBasicComponent getComponent() {
	return component;
    }

    /**
     * Return the pan event handler.
     * @return the pan event handler.
     */
    public ZEventHandler getPanEventHandler() {
	return panEventHandler;
    }

    /**
     * Return the zoom event handler.
     * *@eturn the zoom event handler.
     */
    public ZEventHandler getZoomEventHandler() {
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
}
