/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
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
class GraphIt extends JFrame {
	protected int width = 500;
	protected int height = 500;
	protected GraphItCore graphIt;
	protected ZCanvas canvas; 	
	public GraphIt() {
		
	// Support exiting application
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
			System.exit(0);
	    }
	});

				// Set up basic frame
	setBounds(100, 100, width, height);
	setResizable(true);
	setBackground(null);
	setIconImage(loadFrameIcon());
	canvas = new ZCanvas();
	graphIt = new GraphItCore(canvas);
	getContentPane().add(canvas);

				// Set up core of Hinote
	setTitle("GraphIt");
	setJMenuBar(graphIt.createJMenuBar());

	// Create the toolbar
	JToolBar toolBar = graphIt.createJToolBar();
	getContentPane().add(toolBar,BorderLayout.NORTH);
	
	// Put the HCIL logo at the bottom of the applet
	JPanel logoPanel = graphIt.createLogoPanel();
	getContentPane().add(logoPanel, BorderLayout.SOUTH);

		
				// Deactivate ZCanvas event handlers since HiNote makes its own
	canvas.setNavEventHandlersActive(false);


	setVisible(true);
	}
	private Image loadFrameIcon() {
		Toolkit toolkit= Toolkit.getDefaultToolkit();
		try {
			java.net.URL url= getClass().getResource("resources/jazzlogo.gif");
			return toolkit.createImage((ImageProducer) url.getContent());
		} catch (Exception ex) {
		}
		return null;
	}
	static public void main(String s[]) {
	new GraphIt();
	}
}
