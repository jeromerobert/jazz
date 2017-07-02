import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;


public class Tree extends ZBasicFrame {
    ZNode startNode    = null;
    ZLayoutManagerTree manager = new ZLayoutManagerTree();

    public Tree () {

	// Add menu bar
	JMenuBar menubar = buildMenu();
	setJMenuBar(menubar);
	
	JToolBar toolBar = buildToolBar();

	JPanel contentPane = new JPanel();
	contentPane.setLayout(new BorderLayout());
	contentPane.add(toolBar, BorderLayout.NORTH);

	setContentPane(contentPane);
		
	component = new ZBasicComponent();
	contentPane.add(component, BorderLayout.CENTER);

	// Create some basic event handlers
	panEventHandler = new ZPanEventHandler(component, component.getSurface());
	zoomEventHandler = new ZoomEventHandlerRightButton(component, component.getSurface());
	panEventHandler.activate();
	zoomEventHandler.activate();
	component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	
	getPanEventHandler().activate();
	getZoomEventHandler().activate();
	ZSelectionEventHandler selectionHandler =
	    new ZTSelectionEventHandler(component, component.getSurface(),
				       component.getLayer());
	selectionHandler.activate();
	

	// Initialize and draw the first ellipse
	initScreen();
    }

    public JToolBar buildToolBar() {
	JToolBar toolBar = new JToolBar();
	JButton button;
	
	button = new JButton("Add");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Add a node");
		addSomeChildren();
	    }
	});
	
	button.setToolTipText("Add a node");
	toolBar.add(button);
	
	button = new JButton("Remove");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Remove a node");
		removeSomeChildren();
	    }
	});
	button.setToolTipText("Remove a node");
	toolBar.add(button);
	toolBar.addSeparator();
	
	// Link style choices
	JToggleButton angleLinkButton = new JToggleButton("Angled");
	angleLinkButton.setToolTipText("Angled Link Style");
	angleLinkButton.addActionListener(new ActionListener () {
	    public void actionPerformed(ActionEvent e) {
		toggleLinkStyle(ZLayoutManagerTree.link_ANGLEDLINE);
	    }
	});
	
	JToggleButton straighLinkButton = new JToggleButton("Straight");
	straighLinkButton.setSelected(true);
	straighLinkButton.setToolTipText("Straight Line Link Style");
	straighLinkButton.addActionListener(new ActionListener () {
	    public void actionPerformed(ActionEvent e) {
		toggleLinkStyle(ZLayoutManagerTree.link_STRAIGHTLINE);
	    }
	});
	    
	ButtonGroup linkGroup = new ButtonGroup();
	linkGroup.add(straighLinkButton);
	linkGroup.add(angleLinkButton);

	toolBar.add(angleLinkButton);
	toolBar.add(straighLinkButton);

	toolBar.addSeparator();

	// Head alignment choices
	JToggleButton alignmentButton = new JToggleButton("Head In");
	alignmentButton.setSelected(true);
	alignmentButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		toggleParentAlign(ZLayoutManagerTree.Heading_IN);
	    }
	});
	alignmentButton.setToolTipText("Center node by its immediate children");

	ButtonGroup alignGroup = new ButtonGroup();
	alignGroup.add(alignmentButton);
	toolBar.add(alignmentButton);

	alignmentButton = new JToggleButton("Head Out");
	alignmentButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		toggleParentAlign(ZLayoutManagerTree.Heading_OUT);
	    }
	});
	alignmentButton.setToolTipText("Center node by all of its children");
	alignGroup.add(alignmentButton);
	toolBar.add(alignmentButton);


	alignmentButton = new JToggleButton("Head Side");
	alignmentButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		toggleParentAlign(ZLayoutManagerTree.Heading_SIDE);
	    }
	});
	alignmentButton.setToolTipText("Put node at side");
	alignGroup.add(alignmentButton);
	toolBar.add(alignmentButton);

	toolBar.addSeparator();
	
	// Head orientation choices
	JToggleButton orientationButton = new JToggleButton("Horizontal");
		orientationButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		toggleOrientation(ZLayoutManagerTree.Orientation_LEFTRIGHT);
	    }
	});
	orientationButton.setToolTipText("Tree from Left to Right");
	
	ButtonGroup orientGroup = new ButtonGroup();
	orientGroup.add(orientationButton);
	toolBar.add(orientationButton);
	
	orientationButton = new JToggleButton("Vertical");
	orientationButton.setSelected(true);
	orientationButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		toggleOrientation(ZLayoutManagerTree.Orientation_TOPDOWN);
	    }
	});
	orientationButton.setToolTipText("Tree from Top to Down");
	orientGroup.add(orientationButton);
	toolBar.add(orientationButton);

	return toolBar;

    }


    
    public JMenuBar buildMenu() {
	JMenuBar retVal = new JMenuBar();

	// File menu
	JMenu file = new JMenu("File");
	file.setMnemonic('F');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Exit");
	menuItem.setMnemonic('X');
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	});
	file.add(menuItem);

	retVal.add(file);

	// Action menu
	JMenu action = new JMenu("Action");
	file.setMnemonic('A');

	menuItem = new JMenuItem("Add Node");
	menuItem.setMnemonic('C');
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		addSomeChildren();
	    }
	});
	action.add(menuItem);


	menuItem = new JMenuItem("Remove Node");
	menuItem.setMnemonic('R');
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		removeSomeChildren();
	    }
	});
	action.add(menuItem);

	action.addSeparator();
	
	menuItem = new JMenuItem("Refresh");
	menuItem.setMnemonic('f');
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		refresh();
	    }
	});
	action.add(menuItem);

	retVal.add(action);
	
	return retVal;
    }

    
    public void addSomeChildren() {
	if (startNode == null) {
	    return;
	}
	
	ZVisualComponent vis = null;
	
	ZNode aChild = null;
	ZNode parent = null;

	Vector nodes = component.getLayer().getSelectedChildren();

	// only for debugging
	if (nodes.size() != 1) {
	    System.out.println("No node is selected.");
	    return;
	}

	for (Iterator i = nodes.iterator() ; i.hasNext() ;) {
	    parent = (ZNode) i.next();
	    vis  = new ZRectangle(0.0f, 0.0f, 40.0f, 40.0f);
	    aChild = new ZNode(vis);
	    parent.addChild(aChild);
	}
	    
	component.getLayer().unselectAll();
	startNode.doLayout();
	component.getSurface().restore();
    }

    public void removeSomeChildren() {
	Vector nodes = component.getLayer().getSelectedChildren();
	ZNode node  = null;

	for (Iterator i = nodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    
	    if (node != startNode) {
		// Always keep start node
		ZNode parent = (ZNode) node.getParent();
		parent.removeChild(node);
	    }
	    else {
		System.out.println("Don't remove the root here.");
		return;
	    }
	}

	if (nodes.size() >0) {
	    // Did removal, so do relayout, etc.
	    component.getLayer().unselectAll();
	    startNode.doLayout();
	    component.getSurface().restore();
	}
	else {
	    System.out.println("No node is selected");
	}
    }

    void refresh() {
	if (startNode != null) {
	    component.getLayer().removeChild(startNode);
	}
	initScreen();
    }
    
    void toggleLinkStyle(int wanted) {
	if (wanted != ZLayoutManagerTree.getZLinkStyle()) {
	    if ((ZLayoutManagerTree.setZLinkStyle(wanted)) &&
		(startNode != null)) {
		startNode.doLayout();
		component.getSurface().restore();
	    }
	}
    }


    void toggleParentAlign(int wanted) {
	if (wanted != ZLayoutManagerTree.getCurrentHeadStyle()) {
	    if ((ZLayoutManagerTree.setCurrentHeadingStyle(wanted)) &&
		(startNode != null)) {
		startNode.doLayout();
		component.getSurface().restore();
	    }
	}
    }

    public void toggleOrientation(int wanted) {
	if (wanted != ZLayoutManagerTree.getCurrentOrientation()) {
	    if ((ZLayoutManagerTree.setCurrentOrientation(wanted)) &&
		(startNode != null)) {
		startNode.doLayout();
		component.getSurface().restore();
	    }
	}
    }

    public void initScreen() {
	ZNode eleNode = new ZNode(new ZRectangle(0f, 0f, 40f, 40f));
	component.getLayer().addChild(eleNode);
	eleNode.setLayoutManager(manager);
	startNode = eleNode;
	startNode.doLayout();
	component.getSurface().restore();
    }
    
    public static void main(String[] argv) {
	Tree f = new Tree();
	f.setSize(800, 400);
	f.setVisible(true);
    }
    
} // FreshFrame

/**
 * <b>ZSelectionEventHandler</b> provides event handlers for basic 
 * selection interaction.  
 * Click to select/unselect an item.
 * Shift-click to extend the selection.
 * Click-and-drag on the background to marquee select.
 * Drag a selected item to move all of the selected items.
 *
 * @author  Benjamin B. Bederson
 */
class ZTSelectionEventHandler extends ZSelectionEventHandler {
    protected Point2D pt;
    protected Vector  prevMotionSelection;
    protected ZNode   selNode;        // Selected object
    protected Point2D prevPt;         // Event coords of previous mouse event (in global coordinates)
    protected Point2D pressPt;        // Event coords of mouse press event (in global coordinates)
    protected ZNode   selectionLayer; // Node that selection marquee should be put under
    protected int     scaleUpKey   = KeyEvent.VK_PAGE_UP;    // Key that scales selected objects up a bit
    protected int     scaleDownKey  = KeyEvent.VK_PAGE_DOWN; // Key that scales selected objects down a bit
    protected int     translateLeftKey  = KeyEvent.VK_LEFT;  // Key that translates selected objects left a bit
    protected int     translateRightKey = KeyEvent.VK_RIGHT; // Key that translates selected objects right a bit
    protected int     translateUpKey    = KeyEvent.VK_UP;    // Key that translates selected objects up a bit
    protected int     translateDownKey  = KeyEvent.VK_DOWN;  // Key that translates selected objects down a bit
    protected int     deleteKey  = KeyEvent.VK_DELETE;  // Key that deletes selected objects

    /**
     * Constructs a new ZSelectionEventHandler.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The camera that is selected within
     */
    public ZTSelectionEventHandler(Component c, ZSurface v, ZNode selectionLayer) {
	super(c, v, selectionLayer);
	prevPt = new Point2D.Float();
	pressPt = new Point2D.Float();
	pt = new Point2D.Float();
	prevMotionSelection = new Vector();
	this.selectionLayer = selectionLayer;

    }

    /**
     * Deactivates this event handler. 
     * This results in all selected objects becoming unselected
     */    
    public void deactivate() {
	super.deactivate();

	ZNode node;
	Vector selectedNodes = getCamera().getSelectedNodes();

	for (Iterator i=selectedNodes.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
	    node.getVisualComponent().unselect();
	}
	getSurface().restore();
    }

    /**
     * Specify the node that the selection "marquee" should be put on.
     * The marquee is the rectangle that the user drags around to select things within.
     */    
    public void setSelectionLayer(ZNode node) {
	selectionLayer = node;
    }

    /**
     * Key press event handler
     * @param <code>e</code> The event.
     */
    public void keyPressed(KeyEvent e) {

	//Disable keyevents
	return;
    }

    /**
     * Mouse press event handler
     * @param <code>e</code> The event.
     */
    public void mousePressed(MouseEvent e) {
	ZCamera camera = getCamera();

	if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {   // Left button only
	    pressPt.setLocation(e.getX(), e.getY());
	    camera.getInverseViewTransform().transform(pressPt, pressPt);
	    prevPt.setLocation(pressPt);
	    
	    selNode = getSurface().pick(e.getX(), e.getY());
	    if (selNode == null) {
		for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
		    ZNode tmpNode = (ZNode)i.next();
		    tmpNode.getVisualComponent().unselect();
		}
	    } else {
		if (!selNode.getVisualComponent().isSelected()) {
		    for (Iterator i=camera.getSelectedNodes().iterator(); i.hasNext();) {
			ZNode tmpNode = (ZNode)i.next();
			tmpNode.getVisualComponent().unselect();
		    }
		    selNode.getVisualComponent().select(camera);
		}
	    }
	}

	getSurface().restore();
    }
    
    /**
     * Mouse drag event handler
     * @param <code>e</code> The event.
     */
    public void mouseDragged(MouseEvent e) {
    }
    
    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    public void mouseReleased(MouseEvent e) {
    }


    /**
     * Scale the node using preConcatenation which will result in the
     * scale happening in global coordinates.
     */
    protected void preScale(ZNode node, float dz, float x, float y) {
	AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
	tx.scale(dz, dz);
	tx.translate(-x, -y);
	node.getTransform().preConcatenate(tx);
    }

    /**
     * Translate the node using preConcatenation which will result in the
     * translate happening in global coordinates.
     */
    protected void preTranslate(ZNode node, float dx, float dy) {
	AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
	node.getTransform().preConcatenate(at);
    }
}
