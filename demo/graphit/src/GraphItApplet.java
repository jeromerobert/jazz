/**
 * Copyright (C) 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>GraphIt</b> is an application built on Jazz that supports building
 * simple graphs.
 *
 * @author Antony Courtney
 * @author Lance Good
 */
public class GraphItApplet extends JApplet {
	protected int width = 500;
	protected int height = 500;	
	protected ZCanvas        canvas;	
	protected GraphItCore    graphIt;	

	/**
	 * Default Constructor
	 */
	public GraphItApplet() {
	}
	/**
	 * The real core of an applet (no pun, of course)
	 */
	public void init() {

	///////////////////////////////////////////////////////////////////
	// JAZZ Initializations
	///////////////////////////////////////////////////////////////////

	// Create a basic Jazz Scene
	canvas = new ZCanvas();
	canvas.setNavEventHandlersActive(false);
	graphIt = new GraphItCore(canvas);

	///////////////////////////////////////////////////////////////////
	// SWING Initializations
	///////////////////////////////////////////////////////////////////

	/*
	  From JApplet docs:
	  Both Netscape Communicator and Internet Explorer 4.0 unconditionally 
	  print an error message to the Java console when an applet attempts to
	  access the AWT system event queue. Swing applets do this once, to check
	  if access is permitted. To prevent the warning message in a production
	  applet one can set a client property called "defeatSystemEventQueueCheck" 
	  on the JApplets RootPane to any non null value.
	*/
	setSize(width, height);
	JRootPane rp = getRootPane();
	rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

	// Add the Jazz canvas to the window
	getContentPane().add(canvas);

	// Create and set the menu bar
	JMenuBar menuBar = graphIt.createJMenuBar();
	setJMenuBar(menuBar);

	// Create the toolbar
	JToolBar toolBar = graphIt.createJToolBar();
	getContentPane().add(toolBar,BorderLayout.NORTH);
	
	// Put the HCIL logo at the bottom of the applet
	JPanel logoPanel = graphIt.createLogoPanel();
	getContentPane().add(logoPanel, BorderLayout.SOUTH);

	// Show the applet
	setVisible(true);

	}
}
