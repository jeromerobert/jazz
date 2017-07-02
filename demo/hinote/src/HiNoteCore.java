/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import java.net.URLConnection;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * <b>HiNoteCore</b> implements the core of HiNote that
 * is shared by the application and the applet.
 *
 * @author  Benjamin B. Bederson
 */
public class HiNoteCore {
    static final protected float MAX_ITEM_MAG = 10;
    static final protected int   ANIMATION_TIME = 1000;

				// Event modes
    static final public int PAN_MODE       = 1;
    static final public int LINK_MODE      = 2;
    static final public int POLYLINE_MODE  = 3;
    static final public int RECTANGLE_MODE = 4;
    static final public int TEXT_MODE      = 5;
    static final public int SELECTION_MODE = 6;

    protected ZBasicComponent        component; 
    protected ZRootNode		     root;
    protected ZCamera                camera;
    protected ZSurface               surface;
    protected ZNode                  layer;

    protected CmdTable               cmdTable;
    protected JToolBar               toolBar;
    protected ZPanEventHandler       panEventHandler;
    protected ZoomEventHandlerRightButton zoomEventHandler;
    protected ZLinkEventHandler      linkEventHandler;
    protected ZEventHandler          keyboardNavEventHandler;
    protected ZEventHandler          squiggleEventHandler;
    protected TextEventHandler	     textEventHandler;
    protected ZEventHandler          rectEventHandler;
    protected ZSelectionEventHandler selectionEventHandler;
    protected ZEventHandler          activeEventHandler=null;
    protected int		     currentEventHandlerMode = PAN_MODE;
    protected Cursor		     crosshairCursor = null;
    protected String                 currentFileName = null;
    protected ZParser                parser = null;
    protected Vector                 copyBuffer = null;     // A Vector of nodes
    protected File                   prevFile = new File(".");

    public HiNoteCore(Container container, ZBasicComponent component) {
	cmdTable = new CmdTable(this);
	copyBuffer = new Vector();

				// Create the tool palette
	toolBar = createToolBar();
	container.add(toolBar, BorderLayout.NORTH);

				// Extract the basic elements of the scenegraph
	this.component = component;
	surface = component.getSurface();
	camera = surface.getCamera();
	root = camera.findRoot();
	layer = (ZNode)camera.getPaintStartPoints().firstElement();

				// Add a selection layer
        ZNode selectionLayer = new ZNode();
	getRoot().addChild(selectionLayer);
	getCamera().addPaintStartPoint(selectionLayer);

				// Create some basic event handlers
	textEventHandler =        new TextEventHandler(this, component, surface);
	squiggleEventHandler =    new SquiggleEventHandler(this, component, surface);
	rectEventHandler =        new RectEventHandler(this, component, surface);
	linkEventHandler =        new ZLinkEventHandler(component, surface);
	selectionEventHandler =   new ZSelectionEventHandler(component, surface, selectionLayer);
	panEventHandler =         new PanEventHandler(component, surface);
	zoomEventHandler =        new ZoomEventHandlerRightButton(component, surface);
	keyboardNavEventHandler = new ZNavEventHandlerKeyBoard(component, surface);
	panEventHandler.activate();
	zoomEventHandler.activate();
	activeEventHandler = panEventHandler;
	keyboardNavEventHandler.activate();
	component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    ///////////////////////////////////////////////////////////////    
    //
    // Some utility methods
    //
    ///////////////////////////////////////////////////////////////    

    JToolBar createToolBar() {
	JToolBar toolBar = new JToolBar();
	JButton button;

	button = toolBar.add(cmdTable.lookupAction("pan"));
	button.setText(null);
	button.setToolTipText("Pan and follow links");

	button = toolBar.add(cmdTable.lookupAction("select"));
	button.setText(null);
	button.setToolTipText("Select");

	button = toolBar.add(cmdTable.lookupAction("link"));
	button.setText(null);
	button.setToolTipText("Create hyper links");

	button = toolBar.add(cmdTable.lookupAction("polyline"));
	button.setText(null);
	button.setToolTipText("Draw polylines");
	
	button = toolBar.add(cmdTable.lookupAction("rectangle"));
	button.setText(null);
	button.setToolTipText("Draw rectangles");

	button = toolBar.add(cmdTable.lookupAction("text"));
	button.setText(null);
	button.setToolTipText("Type text");

	return toolBar;
    }

    /**
     * Load the images to be used by the toolbar
     * Images found via the java resources method
     */
    public void loadToolbarImages() {
	Action action;
	URL resource;
	action = getCmdTable().lookupAction("pan");
	resource = this.getClass().getClassLoader().getResource("resources/hand.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));
	action = getCmdTable().lookupAction("link");
	resource = this.getClass().getClassLoader().getResource("resources/link.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));
	action = getCmdTable().lookupAction("select");
	resource = this.getClass().getClassLoader().getResource("resources/select.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));
	action = getCmdTable().lookupAction("polyline");
	resource = this.getClass().getClassLoader().getResource("resources/drawing.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));
	action = getCmdTable().lookupAction("rectangle");
	resource = this.getClass().getClassLoader().getResource("resources/rect.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));
	action = getCmdTable().lookupAction("text");
	resource = this.getClass().getClassLoader().getResource("resources/letter.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(resource));

				// Create some custom cursors
	resource = this.getClass().getClassLoader().getResource("resources/crosshair.gif");
	Image crosshairImage = component.getToolkit().getImage(resource);
	setCrossHairCursorImage(crosshairImage);
    }

    public void helpUsing() {
	String resourceName = "resources/using.jazz";
	InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
	if (inStream == null) {
	    System.out.println("resource: "+resourceName+" not found");
	}
	URL resourceURL = this.getClass().getClassLoader().getResource(resourceName);
	URLConnection urlCon = null;
	int contentLength = -1;
	try {
	    urlCon = resourceURL.openConnection();
	    contentLength = urlCon.getContentLength() + 10;
	} catch (IOException e) {
	    System.out.println("Error opening URL connection to \""+resourceName+"\": "+e);
	}
	if (contentLength == -1) {
	    contentLength = 1000000;
	}
				// InputStreamBuffer size (contentLength) must be larger than resouce file
				// size, because a net stream can't back up.
	HiNoteBufferedInputStream bufferedInputStream = new HiNoteBufferedInputStream(inStream, contentLength);

	openStream(getComponent(), bufferedInputStream);
	getComponent().requestFocus();
    }

    public void helpAbout() {
	ZBasicFrame frame = new ZBasicFrame();
	frame.setLocation(200, 100);

				// Use our own pan event handler so we can follow hyperlinks
	frame.getPanEventHandler().deactivate();
	new PanEventHandler(frame.getComponent(), frame.getSurface()).activate();
	new ZNavEventHandlerKeyBoard(frame.getComponent(), frame.getSurface()).activate();

				// Don't want application to exit when help window is closed
	frame.removeWindowListener(frame.getWindowListener());

	String resourceName = "resources/about.jazz";
	InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
	if (inStream == null) {
	    System.out.println("resource \""+resourceName+"\" not found");
	}

	URL resourceURL = this.getClass().getClassLoader().getResource(resourceName);
	URLConnection urlCon = null;
	int contentLength = -1;
	try {
	    urlCon = resourceURL.openConnection();
	    contentLength = urlCon.getContentLength() +10;
	} catch (IOException e) {
	    System.out.println("Error opening URL connection to \""+resourceName+"\": "+e);
	}
	if (contentLength == -1) {
	    contentLength = 1000000;
	}
	// InputStreamBuffer size (contentLength) must be larger than resouce file
	// size, because an net stream can't back up.
	HiNoteBufferedInputStream bufferedInputStream = new HiNoteBufferedInputStream(inStream, contentLength);
	openStream(frame.getComponent(), bufferedInputStream);
    }

    /**
     * Set the image to be used by the crosshair cursor.
     */
    public void setCrossHairCursorImage(Image image) {
	crosshairCursor = component.getToolkit().createCustomCursor(image, new Point(8, 8), "Crosshair Cursor");
    }

    public void setToolBar(boolean show) {
	toolBar.setVisible(show);
	toolBar.getParent().validate();
    }

    /**
     * Set the rendering quality.
     * Values are ZRenderContext.RENDER_QUALITY_LOW, ZRenderContext.RENDER_QUALITY_MEDIUM and ZRenderContext.RENDER_QUALITY_HIGH
     */
    public void setRenderQuality(int qualityRequested) {
	surface.setRenderQuality(qualityRequested);
	surface.repaint();
    }

    /**
     * Determine the current render quality of this app.
     */
    public int getRenderQuality() {
	return surface.getRenderQuality();
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
    public ZPanEventHandler getPanEventHandler() {
	return panEventHandler;
    }

    /**
     * Return the zoom event handler.
     * *@eturn the zoom event handler.
     */
    public ZoomEventHandlerRightButton getZoomEventHandler() {
	return zoomEventHandler;
    }

    public CmdTable getCmdTable() {
	return cmdTable;
    }
    
    public ZNode getDrawingLayer() {
	return getLayer();
    }

    ///////////////////////////////////////////////////////////////    
    //
    // Basic functionality methods that are priniciply called
    // from the menubar.
    //
    ///////////////////////////////////////////////////////////////    

    public void newView() {
	ZBasicFrame frame = new ZBasicFrame(false, getRoot(), getLayer());
	frame.setLocation(200, 100);

				// Make camera in new window look at same place as current window
	frame.getCamera().center(getCamera().getViewBounds(), 0, frame.getSurface());

				// Use same render quality as current window
	frame.getSurface().setRenderQuality(getRenderQuality());

				// Use our own pan event handler so we can follow hyperlinks
	frame.getPanEventHandler().deactivate();
	new PanEventHandler(frame.getComponent(), frame.getSurface()).activate();
	new ZNavEventHandlerKeyBoard(frame.getComponent(), frame.getSurface()).activate();

				// Don't want application to exit when secondary windows are closed
	frame.removeWindowListener(frame.getWindowListener());
    }

    /**
     * Create a fullscreen view of the scene.
     * Press the 'Esc' key to close it.
     */
    public void fullScreen() {
	ZBasicWindow window = new ZBasicWindow(getRoot(), getLayer());

				// Make camera in new window look at same place as current window
	window.getCamera().center(getCamera().getViewBounds(), 0, window.getSurface());

				// Use same render quality as current window
	window.getSurface().setRenderQuality(getRenderQuality());

				// Use our own pan event handler so we can follow hyperlinks
	window.getPanEventHandler().deactivate();
	new PanEventHandler(window.getComponent(), window.getSurface()).activate();
    }

    /**
     * Change the camera to the identity view - i.e., home.
     */
    public void goHome() {
	getCamera().getViewTransform().animate(new AffineTransform(), ANIMATION_TIME, getSurface());
    }

    public void open() {
	ExtensionFileFilter filter = new ExtensionFileFilter();
	filter.addExtension("jazz");
	filter.setDescription("Jazz files");
	
	File file = QueryUserForFile(filter, "Open");
	if (file != null) {
	    currentFileName = file.getAbsolutePath();

	    // Pathnames are to be stored in jazz files relative to
	    // current jazz save directory. Do a temporary 'set cwd'
	    // so the readObject methods can change filenames to
	    // be relative to this new cwd
	    String cwd = System.getProperty("user.dir");
	    System.setProperty("user.dir",file.getParent());

	    openFile(getComponent(), currentFileName);

	    // restore the cwd
	    System.setProperty("user.dir",cwd);

	    root = getComponent().getRoot();
	    layer = (ZNode)root.getChildren().elementAt(0);
	    camera = (ZCamera)root.getChildren().elementAt(1);
	    ZNode selectionLayer = (ZNode)root.getChildren().elementAt(2);
	    selectionEventHandler.setSelectionLayer(selectionLayer);
	}
    }

    public void openFile(ZBasicComponent component, String fileName) {
	FileInputStream inStream = null;
	
	try {
	    inStream = new FileInputStream(fileName);
	    openStream(component, inStream);
	}
	catch (java.io.FileNotFoundException e) {
	    System.out.println("File " + fileName + " not found.");
	}
    }

    public void openStream(ZBasicComponent component, InputStream inStream) {
	if (inStream != null) {
	    if (parser == null) {
		parser = new ZParser();
	    } 
	    try {
		component.setRoot((ZRootNode)parser.parse(inStream));
		component.setLayer((ZNode)component.getRoot().getChildren().elementAt(0));
		component.setCamera((ZCamera)component.getRoot().getChildren().elementAt(1));
		component.getSurface().setCamera(component.getCamera());
		component.getCamera().setSurface(component.getSurface());

		root = component.getRoot();
		layer = (ZNode)root.getChildren().elementAt(0);
		camera = (ZCamera)root.getChildren().elementAt(1);
		
		ZNode selectionLayer = (ZNode)root.getChildren().elementAt(2);
		selectionEventHandler.setSelectionLayer(selectionLayer);

		component.getSurface().repaint();
	    } catch (ParseException e) {
		System.out.println(e.getMessage());
		System.out.println("Invalid file format");
	    }
	}
    }

    public void save() {
	if (currentFileName == null) {
	    ExtensionFileFilter filter = new ExtensionFileFilter();
	    filter.addExtension("jazz");
	    filter.setDescription("Jazz files");

	    File file = QueryUserForFile(filter, "Save");
	    if (file != null) {
		currentFileName = file.getAbsolutePath();
	    }
	}
	if (currentFileName != null) {
	    try {
		FileOutputStream fos =  new FileOutputStream(currentFileName);
		ZObjectOutputStream out = new ZObjectOutputStream(fos);

		// Pathnames are to be stored in jazz files relative to
		// current jazz save directory. Do a temporary 'set cwd'
		// so the writeObject methods can change filenames to
		// be relative to this new cwd
		String cwd = System.getProperty("user.dir");
		File cfn = new File(currentFileName);
		System.setProperty("user.dir",cfn.getParent());

		out.writeObject(getRoot());

		// restore the cwd
		System.setProperty("user.dir",cwd);

		out.close();
		fos.close();
	    } catch (Exception exception) {
		System.out.println(exception);
	    }
	}
    }

    public void saveAs() {
	currentFileName = null;
	save();
    }

    public void printScreen() {
	surface.printSurface();
    }

    public File QueryUserForFile(javax.swing.filechooser.FileFilter filter, String approveText) {
	File file = null;
	JFileChooser fileChooser = new JFileChooser(prevFile);

	if (filter != null) {
	    fileChooser.addChoosableFileFilter(filter);
	    fileChooser.setFileFilter(filter);
	}
	int retval = fileChooser.showDialog(getComponent(), approveText);
	if (retval == JFileChooser.APPROVE_OPTION) {
	    file = fileChooser.getSelectedFile();
	}

	prevFile = file;

	return file;
    }

    public void insertImage() {
	File file = QueryUserForFile(null, "Open");
	if (file != null) {
	    java.awt.Image ji = getComponent().getToolkit().getImage(file.getAbsolutePath());
	    MediaTracker tracker = new MediaTracker(getComponent());
	    tracker.addImage(ji, 0);
	    try {
		tracker.waitForID(0);
	    }
	    catch (InterruptedException exception) {
		System.out.println("Couldn't load image: " + file);
	    }
	    ZImage zi = new ZImage(ji);
	    zi.setFileName(file.getAbsolutePath());
	    zi.setWriteEmbeddedImage(false);
	    ZNode node = new ZNode(zi);
	    node.setTransform((ZTransform)getCamera().getInverseViewTransform().clone());
	    getDrawingLayer().addChild(node);
	    
	    getSurface().restore();
	}
    }

    public void cut() {
	ZNode node;

	copy();

	Vector nodes = getDrawingLayer().getSelectedChildren();
	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    getDrawingLayer().removeChild(node);
	}
	surface.restore();
    }
	
    public void copy() {
	ZNode node, copy;
	Vector nodes = getDrawingLayer().getSelectedChildren();
	float delta = 10.0f / getCamera().getMagnification();

	copyBuffer.clear();
	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    copy = (ZNode)node.clone();
	    preTranslate(copy, delta, delta);
	    copyBuffer.add(copy);
	}
    }
	
    public void paste() {
	ZNode node, copy;
	Vector newCopyBuffer = new Vector();
	float delta = 10.0f / getCamera().getMagnification();

	getDrawingLayer().unselectAll();
	for (Iterator i=copyBuffer.iterator(); i.hasNext();) {
            node = (ZNode)i.next();

	    copy = (ZNode)node.clone();
	    preTranslate(copy, delta, delta);
	    newCopyBuffer.add(copy);

	    getDrawingLayer().addChild(node);
	    node.getVisualComponent().select(getCamera());
	}

	copyBuffer = newCopyBuffer;
	surface.restore();
    }
	
    /**
     * Translate the node using preConcatenation which will result in the
     * translate happening in global coordinates.
     */
    protected void preTranslate(ZNode node, float dx, float dy) {
	AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
	node.getTransform().preConcatenate(at);
    }

    public void raise() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.raise();
	}
	surface.restore();
    }
	
    public void lower() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.lower();
	}
	surface.restore();
    }
	
    public void setMinMag() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.setMinMag(getCamera().getMagnification());
	}
	surface.restore();
    }
	
    public void setMaxMag() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.setMaxMag(getCamera().getMagnification());
	}
	surface.restore();
    }
	
    public void clearMinMaxMag() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.setMinMag(0);
	    node.setMaxMag(-1);
	}
	surface.restore();
    }
	
    public void makeSticky() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    Class c = ZConstraintDecorator.class;
	    ZConstraintDecorator constraint = (ZConstraintDecorator)node.findVisualComponent(c);
				// If already has a constraint, then remove it
	    if (constraint != null) {
		constraint.applyTransform();
		constraint.remove();
	    }
				// Then, apply the new constraint
	    node.getVisualComponent().unselect();
	    constraint = new ZStickyDecorator(getCamera());
	    constraint.insertAbove(node.getVisualComponent());
	    constraint.applyInverseTransform();
	    node.getVisualComponent().select(getCamera());
	}
	surface.restore();
    }
	
    public void makeStickyZ() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    Class c = ZConstraintDecorator.class;
	    ZConstraintDecorator constraint = (ZConstraintDecorator)node.findVisualComponent(c);
				// If already has a constraint, then remove it
	    if (constraint != null) {
		constraint.applyTransform();
		constraint.remove();
	    }
				// Then, apply the new constraint
	    node.getVisualComponent().unselect();
	    constraint = new ZStickyZDecorator(getCamera());
	    constraint.insertAbove(node.getVisualComponent());
	    constraint.applyInverseTransform();
	    node.getVisualComponent().select(getCamera());
	}
	surface.restore();
    }
	
    public void makeUnSticky() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    Class c = ZConstraintDecorator.class;
	    ZConstraintDecorator sticky = (ZConstraintDecorator)node.findVisualComponent(c);
	    if (sticky != null) {
		sticky.applyTransform();
		sticky.remove();
	    }
	}
	surface.restore();
    }
	
    public void deleteSelected() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.getParent().removeChild(node);
	}
	surface.restore();
    }

    /* Group all selected nodes. If other nodes are part of other groups,
     * the structure is preserved.
     */
    public void groupSelected() {
	ZNode node;
	Vector nodes = getDrawingLayer().getSelectedChildren();

	// There are no nodes to group
	if (nodes.isEmpty()) return;

	// can't group a single node
	if (nodes.size() == 1) return;

	// create a new groupNode, link it to the layer node
        ZNode groupNode = new ZNode();

	layer.addChild(groupNode);

	// remove nodes to be grouped from the layer, add them
	// to the new groupNode
	ZNode topNode;
	Vector topNodes = new Vector();
	for (Iterator i=nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();

	    // climb up tree to layer node (handles groups of groups)
	    topNode = node;
	    while (!topNode.getParent().equals(layer)) {
		topNode = topNode.getParent();
	    }

	    if (!topNodes.contains(topNode)) {
		topNodes.addElement(topNode);
	    }
	}
	for (Iterator v=topNodes.iterator(); v.hasNext();) {
	    topNode = (ZNode)v.next();
	    groupNode.addChild(topNode);
	}
	    
	getDrawingLayer().unselectAll();

	// add an invisible box to this groupNode so it can be selected
	ZRectangle rect = new ZRectangle(groupNode.getGlobalBounds());
	rect.setPenColor(null);
	rect.setPenWidth(0.0f);
	rect.setFillColor(null);
	groupNode.setVisualComponent(rect);
	rect.select(getCamera());

	surface.restore();
    }

    /**
     * Ungroup any selected groups.
     */
    public void ungroupSelected() {
	Vector nodes = getDrawingLayer().getSelectedChildren();

	if (nodes.isEmpty()) return;

	ZNode groupNode;
	for (Iterator g=nodes.iterator(); g.hasNext();) {
	    groupNode = (ZNode)g.next();

	    // A "group" has a colection of children
	    if (groupNode.getChildren().isEmpty()) {
		continue;
	    }

	    // Should not happen: an individual member of a group has been selected
	    if (!groupNode.getParent().equals(layer)) {
		continue;
	    }

	    layer.removeChild(groupNode);

	    // Collect children of former group node
	    Vector childrenV = new Vector();
	    for (Iterator i=groupNode.getChildren().iterator(); i.hasNext();) {
		ZNode childNode = (ZNode)i.next();
		childrenV.addElement(childNode);
	    }

	    // Add those children to layer
	    for (Iterator i=childrenV.iterator(); i.hasNext();) {
		ZNode childNode = (ZNode)i.next();
		childNode.reparent(layer);
		childNode.getVisualComponent().select(getCamera());
	    }
	}
	
	surface.restore();
    }

    public void setEventHandler(int newEventHandlerMode) {
				// First, exit old event handler mode
	switch (currentEventHandlerMode) {
	case TEXT_MODE:
	    keyboardNavEventHandler.activate();
	    break;
	case SELECTION_MODE:
	    keyboardNavEventHandler.activate();
	    break;
	default:
	}

				// Then, deactivate old event handler
	if (activeEventHandler != null) {
	    activeEventHandler.deactivate();
	}

				// Set up new event handler mode
	switch (newEventHandlerMode) {
	case PAN_MODE:
	    activeEventHandler = panEventHandler;
	    getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    break;
	case LINK_MODE:
	    activeEventHandler = linkEventHandler;
	    getComponent().setCursor(crosshairCursor);
	    break;
	case POLYLINE_MODE:
	    activeEventHandler = squiggleEventHandler;
	    getComponent().setCursor(crosshairCursor);
	    break;
	case RECTANGLE_MODE:
	    activeEventHandler = rectEventHandler;
	    getComponent().setCursor(crosshairCursor);
	    break;
	case TEXT_MODE:
	    keyboardNavEventHandler.deactivate();
	    activeEventHandler = textEventHandler;
	    getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	    break;
	case SELECTION_MODE:
	    keyboardNavEventHandler.deactivate();
	    activeEventHandler = selectionEventHandler;
	    getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    break;
	default:
	    activeEventHandler = null;
	    currentEventHandlerMode = 0;
	}
				// Finally, activate new event handler
	if (activeEventHandler != null) {
	    activeEventHandler.activate();
	    currentEventHandlerMode = newEventHandlerMode;
	}
    }

    public int getCurrentHandlerMode() {
	return currentEventHandlerMode;
    }
}
