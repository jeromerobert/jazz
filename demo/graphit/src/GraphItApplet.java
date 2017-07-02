/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
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
 * The Main GraphIt Applet Class - handles interface issues such as
 * the toolbar and the menubar.
 *
 * @author Lance Good
 */
public class GraphItApplet extends JApplet {

    // Constants to specify Application Colors
    static final Color NODE_HIGHLIGHT_COLOR = Color.red;
    static final Color NODE_FILL_COLOR = Color.green;
    static final Color NODE_PEN_COLOR = Color.blue;
    
    // Constants to specify the current application mode
    static final int PAN_MODE = 1;
    static final int SELECT_MODE = 2;
    static final int DRAW_MODE = 3;
    static final int LINK_MODE = 4;
    
    // This applets Jazz Canvas
    ZCanvas canvas = null;

    // The new view frame
    JFrame newViewFrame;

    // The current event handler mode
    int currentMode = DRAW_MODE;

    // The current event handler
    ZEventHandler currentEventHandler = null;

    // The Pan event handler
    ZPanEventHandler panEventHandler = null;

    // The Zoom event handler (always active!)
    ZoomEventHandler zoomEventHandler = null;

    // The Selection event handler
    ZSelectionEventHandler selectionEventHandler = null;    

    // The Node creating event handler
    NodeDropper nodeDropperEventHandler = null;

    // The Link creating event handler
    LinkConnector linkEventHandler = null;

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

	ZLayerGroup linkLayer = new ZLayerGroup();
	canvas.getRoot().addChild(linkLayer);
	canvas.getCamera().replaceLayer(canvas.getLayer(),linkLayer);
	canvas.getCamera().addLayer(canvas.getLayer());

	// Create a selection layer for drawing the marquee
	ZLayerGroup selectionLayer = new ZLayerGroup();	
	canvas.getRoot().addChild(selectionLayer);
	canvas.getCamera().addLayer(selectionLayer);

	// Initialize the event handlers
	initEventHandlers(canvas,selectionLayer,linkLayer);

	setMode(DRAW_MODE);

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
	JRootPane rp = getRootPane();
	rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

	// Add the Jazz canvas to the window
	getContentPane().add(canvas);

	// Create and set the menu bar
	JMenuBar menuBar = createJMenuBar();
	setJMenuBar(menuBar);

	// Create the toolbar
	JToolBar toolBar = createJToolBar();
	getContentPane().add(toolBar,BorderLayout.NORTH);

	// Put the HCIL logo at the bottom of the applet
	JPanel logoPanel = createLogoPanel();
	getContentPane().add(logoPanel, BorderLayout.SOUTH);

	// Show the applet
	setVisible(true);
    }

    /**
     * Initialize the event handlers
     */
    public void initEventHandlers(ZCanvas canvas, ZLayerGroup selectionLayer, ZLayerGroup linkLayer) {
	panEventHandler = new ZPanEventHandler(canvas.getCameraNode());
	zoomEventHandler = new ZoomEventHandler(canvas.getCameraNode());
	selectionEventHandler = new ZSelectionEventHandler(canvas.getCameraNode(),canvas,selectionLayer);
	nodeDropperEventHandler = new NodeDropper(canvas);
	linkEventHandler = new LinkConnector(canvas,linkLayer);

	zoomEventHandler.setActive(true);
    }

    /**
     * Create the MenuBar
     */
    public JMenuBar createJMenuBar() {
	JMenuBar menuBar = new JMenuBar();

	// The 'Edit' Menu
	JMenu edit = new JMenu("Edit");
	edit.setMnemonic('E');
	menuBar.add(edit);

	// The 'Group' Menu Item
	JMenuItem group = new JMenuItem("Group");
	group.setMnemonic('G');
	group.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    group();
		}
	    });
	edit.add(group);

	// The 'UnGroup' Menu Item
	JMenuItem ungroup = new JMenuItem("UnGroup");
	ungroup.setMnemonic('U');
	ungroup.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    ungroup();
		}
	    });
	edit.add(ungroup);

	// The 'View' Menu
	JMenu view = new JMenu("View");
	edit.setMnemonic('V');
	menuBar.add(view);

	// The 'New View' Menu Item
	JMenuItem newView = new JMenuItem("New View");
	newView.setMnemonic('N');
	newView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    newView();
		}
	    });
	view.add(newView);

	return menuBar;
    }

    /**
     * Create the Toolbar
     */
    public JToolBar createJToolBar() {
	JToolBar toolBar = new JToolBar();
	JToggleButton button;
	URL resource;
	ButtonGroup group = new ButtonGroup();


				// Node dropper button
	resource = this.getClass().getClassLoader().getResource("resources/ellipse.gif");
	JToggleButton ellipse = new JToggleButton(new ImageIcon(resource), false);
	ellipse.setToolTipText("Node Dropper");
	ellipse.setText(null);
	ellipse.setSelected(true);	
	ellipse.setPreferredSize(new Dimension(34, 30));	
	ellipse.setMaximumSize(new Dimension(34, 30));
	ellipse.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    setMode(DRAW_MODE);
		}
	    });
	group.add(ellipse);
	toolBar.add(ellipse);


	
				// Select button
	resource = this.getClass().getClassLoader().getResource("resources/select.gif");
	JToggleButton select = new JToggleButton(new ImageIcon(resource), false);
	select.setToolTipText("Select");
	select.setText(null);
	select.setPreferredSize(new Dimension(34, 30));
	select.setMaximumSize(new Dimension(34, 30));
	select.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    setMode(SELECT_MODE);
		}
	    });
	group.add(select);
	toolBar.add(select);	

	
				// Link button
	resource = this.getClass().getClassLoader().getResource("resources/link.gif");
	JToggleButton link = new JToggleButton(new ImageIcon(resource), false);
	link.setToolTipText("Link");
	link.setText(null);
	link.setPreferredSize(new Dimension(34, 30));
	link.setMaximumSize(new Dimension(34, 30));
	link.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    setMode(LINK_MODE);
		}
	    });
	group.add(link);
	toolBar.add(link);

				// Pan Button
	resource = this.getClass().getClassLoader().getResource("resources/hand.gif");
	JToggleButton pan = new JToggleButton(new ImageIcon(resource), false);
	pan.setToolTipText("Pan and Zoom");
	pan.setText(null);
	pan.setPreferredSize(new Dimension(34, 30));
	pan.setMaximumSize(new Dimension(34, 30));
	pan.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    setMode(PAN_MODE);
		}
	    });
	group.add(pan);
	toolBar.add(pan);

	
	return toolBar;
    }

    /**
     * Create a JPanel with the HCIL logo 
     */
    public JPanel createLogoPanel() {
	URL logoURL = this.getClass().getClassLoader().getResource("resources/HCIL-logo.gif");

	ImageIcon logoImage = new ImageIcon(logoURL);
	JLabel logoLabel = new JLabel(logoImage);

	JPanel logoPanel = new JPanel();
	logoPanel.setLayout(new BorderLayout());
	logoPanel.add(logoLabel, BorderLayout.EAST);
	logoPanel.setBackground(Color.white);
	
	return logoPanel;
    }

    /**
     * Sets the current mode of the application
     * @param mode The new mode
     */
    public void setMode(int mode) {
	if (currentEventHandler != null) {
	    currentEventHandler.setActive(false);
	}

	switch(mode) {
	case PAN_MODE:
	    currentEventHandler = panEventHandler;
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    break;
	case SELECT_MODE:
	    currentEventHandler = selectionEventHandler;
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    break;
	case DRAW_MODE:
	    currentEventHandler = nodeDropperEventHandler;
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    break;
	case LINK_MODE:
	    currentEventHandler = linkEventHandler;
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	    break;
	}

	if (currentEventHandler != null) {
	    currentEventHandler.setActive(true);
	}
    }

    /**
     * Creates a new view of the scene
     */
    public void newView() {
	if (newViewFrame == null) {

	    newViewFrame = new JFrame();
	    ZCanvas newCanvas = new ZCanvas(canvas.getRoot(),canvas.getLayer());
	    newViewFrame.setBounds(200, 100, 400, 400);
	    newViewFrame.setResizable(true);
	    newViewFrame.setBackground(null);
	    newViewFrame.getContentPane().add(newCanvas);
	    newViewFrame.show();
	    final ZCamera camera = newCanvas.getCamera();
				// Copy visible layers from primary camera to new camera
	    ZLayerGroup[] layers = canvas.getCamera().getLayers();
	    for (int i=0; i<layers.length; i++) {	      
		if (i==0) {
		    camera.replaceLayer(newCanvas.getLayer(),layers[i]);
		}
		else {
		    camera.addLayer(layers[i]);
		}
	    }

				// Make camera in new window look at same place as current window
	    camera.center(canvas.getCamera().getViewBounds(), 0, canvas.getDrawingSurface());
	}
	else {
	    newViewFrame.show();
	}
    }

    /**
     * Groups the currently selected nodes - has to be a little bit smart
     * using ZGroup.insertAbove rather than ZGroup.addChild and
     * ZGroup.removeChild so events get fired at the right times.
     */
    public void group() {
	ZNode node;
	ZNode handle;
	ArrayList selection = ZSelectionGroup.getSelectedNodes(canvas.getCamera());

				// Not enough nodes to group
	if (selection.size() <= 1) {
	    return;
	}


				// Create a new group node, and put it under the layer node
        GraphGroup group = new GraphGroup();
	group.setGroupRenderCutoff(canvas.getCamera().getMagnification());
	group.putClientProperty("group", group);
	canvas.getLayer().addChild(group);
	group.setChildrenPickable(false);
	group.setChildrenFindable(false);

				// Move nodes to be grouped to the new group
	for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    ZSelectionGroup.unselect(node);
	    handle = node.editor().getTop();
	    group.insertAbove(handle);
	}
	ZSelectionGroup.select(group);
    }

    /**
     * Ungroups the currently selected nodes - has to be a little bit smart
     * by doing the children reparenting before removing the group from
     * the parent so that events get fired at the right time.
     */
    public void ungroup() {
	ZNode node;
	ArrayList selection = ZSelectionGroup.getSelectedNodes(canvas.getCamera());

	if (selection.isEmpty()) {
	    return;
	}

	GraphGroup group;
	ZGroup handle;
	ZGroup newParent;
	ZNode[] children;

	for (Iterator i=selection.iterator(); i.hasNext();) {
	    node = (ZNode)i.next();

	    if (node.getClientProperty("group") != null) {
		group = (GraphGroup)node;
		ZSelectionGroup.unselect(group);
		children = group.getChildren();

		handle = (ZGroup)group.editor().getTop();
		newParent = handle.getParent();
		
		for (int j=0; j<children.length; j++) {		    	    
		    children[j].reparent(newParent);
		    ZSelectionGroup.select(children[j].editor().getNode());
		}

		newParent.removeChild(handle);
	    }
	}
    }
}



