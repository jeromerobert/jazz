import java.awt.*;
import java.awt.geom.*;   
import java.awt.event.*;
import java.util.*;
import javax.swing.*;   
import javax.swing.border.*;
import edu.umd.cs.jazz.scenegraph.*;   
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.util.*;   

/**   
 * This is a ZLoadable module that must be loaded into an application like
 * HiNote to run.
 *
 * @author Lance Good
 */
public class LayoutTest implements Runnable, ZLoadable, ActionListener {

    // Constants to denote the type of layout
    final int LINE = 0;
    final int ELLIPSE = 1;
    final int RECT = 2;
    final int QUAD = 3;
    final int CUBIC = 4;
    final int ARC = 5;
    final int POLY = 6;
    final int TREE = 7;

    // Items from the ZLoadable interface
    JMenuBar menubar = null;       
    ZCamera camera = null;
    ZSurface surface = null;       
    ZNode layer = null;   

    // The menu bar items
    JMenuItem setLayoutOptions = null;
    JMenuItem doLayout = null;

    // The main window
    JDialog optionsDialog;

    // The two main options for layout
    JRadioButton treeOption;
    JRadioButton pathOption;

    // The options within Path layout
    JRadioButton lineOption;
    JRadioButton ellipseOption;
    JRadioButton rectOption;
    JRadioButton quadOption;
    JRadioButton cubicOption;
    JRadioButton arcOption;
    JRadioButton polyOption;

    // Button used to dismiss the dialog
    JButton done;

    // The current layout manager
    ZLayoutManager layoutManager = new ZLayoutManagerPath();

    // The current layout option within Path layout
    int currentLayout = LINE;

    // The general path used by the polyOption
    GeneralPath polyPath = null;

    // The button pressed to specify the polyPath
    JButton polyButton;

    // An ActionEvent pointer to reuse
    ActionEvent action;
    
    /**
     * Constructor to build GUI
     */
    public LayoutTest() {       
	optionsDialog = new JDialog();
	optionsDialog.setTitle("Layout Options");
	optionsDialog.setSize(300,300);
	
	// The top level - tree or path layout
	pathOption = new JRadioButton("Path Layout");
	treeOption = new JRadioButton("Tree Layout");
	
	// Tree Option will be disabled until it works with DummyVisualComponents
	treeOption.setEnabled(false);
	
	pathOption.addActionListener(this);
	treeOption.addActionListener(this);
	
	ButtonGroup layoutOption = new ButtonGroup();
	layoutOption.add(pathOption);    
	layoutOption.add(treeOption);
	pathOption.setSelected(true);

	// Within the path layout - line, ellipse, rect, quad, cubic, arc, & manual
	lineOption = new JRadioButton("Line");
	ellipseOption = new JRadioButton("Ellipse");
	rectOption = new JRadioButton("Rectangle");
	quadOption = new JRadioButton("Quadratic");
	cubicOption = new JRadioButton("Cubic");
	arcOption = new JRadioButton("Arc");
	polyOption = new JRadioButton("Selected Polyline");
	
	lineOption.addActionListener(this);
	ellipseOption.addActionListener(this);
	rectOption.addActionListener(this);
	quadOption.addActionListener(this);
	cubicOption.addActionListener(this);
	arcOption.addActionListener(this);
	polyOption.addActionListener(this);
	
	ButtonGroup layoutsGroup = new ButtonGroup();
	layoutsGroup.add(lineOption);
	layoutsGroup.add(ellipseOption);
	layoutsGroup.add(rectOption);
	layoutsGroup.add(quadOption);
	layoutsGroup.add(cubicOption);
	layoutsGroup.add(arcOption);
	layoutsGroup.add(polyOption);
	lineOption.setSelected(true);

	polyButton = new JButton("Set Polyline");
	polyButton.addActionListener(this);
	polyButton.setEnabled(false);
	
	JPanel linePanel = new JPanel();
	JPanel ellipsePanel = new JPanel();
	JPanel rectPanel = new JPanel();
	JPanel quadPanel = new JPanel();
	JPanel cubicPanel = new JPanel();
	JPanel arcPanel = new JPanel();
	JPanel polyPanel = new JPanel();
	
	linePanel.setLayout(new BorderLayout());
	ellipsePanel.setLayout(new BorderLayout());
	rectPanel.setLayout(new BorderLayout());
	quadPanel.setLayout(new BorderLayout());
	cubicPanel.setLayout(new BorderLayout());
	arcPanel.setLayout(new BorderLayout());
	polyPanel.setLayout(new GridLayout(2,1));

	linePanel.add(lineOption,BorderLayout.CENTER);
	ellipsePanel.add(ellipseOption,BorderLayout.CENTER);
	rectPanel.add(rectOption,BorderLayout.CENTER);    
	quadPanel.add(quadOption,BorderLayout.CENTER);
	cubicPanel.add(cubicOption,BorderLayout.CENTER);
	arcPanel.add(arcOption,BorderLayout.CENTER);    
	polyPanel.add(polyOption);
	polyPanel.add(polyButton);
	
	lineOption.setHorizontalAlignment(SwingConstants.LEFT);
	ellipseOption.setHorizontalAlignment(SwingConstants.LEFT);
	rectOption.setHorizontalAlignment(SwingConstants.LEFT);
	quadOption.setHorizontalAlignment(SwingConstants.LEFT);
	cubicOption.setHorizontalAlignment(SwingConstants.LEFT);
	arcOption.setHorizontalAlignment(SwingConstants.LEFT);
	polyOption.setHorizontalAlignment(SwingConstants.LEFT);
	
	JPanel optionsPanel = new JPanel();
	optionsPanel.setBorder(new EmptyBorder(0,20,0,0));
	optionsPanel.setLayout(new GridBagLayout());

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1;
	gbc.weighty = 0;
	
	optionsPanel.add(linePanel,gbc);

	gbc.gridy = GridBagConstraints.RELATIVE;
	
	optionsPanel.add(ellipsePanel,gbc);
	optionsPanel.add(rectPanel,gbc);
	optionsPanel.add(quadPanel,gbc);
	optionsPanel.add(cubicPanel,gbc);
	optionsPanel.add(arcPanel,gbc);

	gbc.weighty = 1;
	
	optionsPanel.add(polyPanel,gbc);
	
	
	// Add everything to the two main panels
	JPanel pathOptionPanel = new JPanel();
	JPanel treeOptionPanel = new JPanel();
	pathOptionPanel.setLayout(new BorderLayout());
	treeOptionPanel.setLayout(new BorderLayout());
	pathOptionPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	treeOptionPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	pathOptionPanel.add(pathOption,BorderLayout.NORTH);
	pathOptionPanel.add(optionsPanel,BorderLayout.CENTER);
	treeOptionPanel.add(treeOption,BorderLayout.NORTH);
	
	// Now create the main window
	JPanel holder = new JPanel();
	holder.setLayout(new GridLayout(1,2));
	holder.add(pathOptionPanel);
	holder.add(treeOptionPanel);
	
	done = new JButton("Done");
	done.addActionListener(this);
	
	JPanel buttons = new JPanel();
	buttons.setBorder(new EmptyBorder(5,50,5,50));
	buttons.add(done);
	
	optionsDialog.getContentPane().setLayout(new BorderLayout());
	optionsDialog.getContentPane().add(holder,BorderLayout.CENTER);
	optionsDialog.getContentPane().add(buttons,BorderLayout.SOUTH);
	
    }

    /**
     * Method that ZLoadable calls to finish initialization after
     * constructor and ZLoadable components set
     */
    public void run() {
	
	JMenu layoutMenu = new JMenu("Layout");
	setLayoutOptions = new JMenuItem("Set Layout Options");
	doLayout = new JMenuItem("Do Layout");
	
	setLayoutOptions.addActionListener(this);
	doLayout.addActionListener(this);
	
	layoutMenu.add(setLayoutOptions);   
	layoutMenu.add(doLayout);
	menubar.add(layoutMenu);           
	menubar.revalidate();       
	
	optionsDialog.setLocationRelativeTo(menubar);

    }   

    /**
     * Method to set the Menubar from the appropriate window.
     * Part of the ZLoadable interface.
     * @param The menubar that this component should use.
     */
    public void setMenubar(JMenuBar aMenubar) {           
	menubar = aMenubar;
    }

    /**
     * Method to set the ZCamera
     * Part of the ZLoadable interface.
     * @param The camera that this component should use.
     */
    public void setCamera(ZCamera aCamera) {
	camera = aCamera;       
    }

    /**
     * Method to set the ZSurface
     * Part of the ZLoadable interface.
     * @param The surface that this component should use.
     */
    public void setSurface(ZSurface aSurface) {           
	surface = aSurface;
    }

    /**
     * Method to set the drawing layer
     * Part of the ZLoadable interface.
     * @param The drawing layer that this component should use
     */
    public void setLayer(ZNode aLayer) {           
	layer = aLayer;
    }   

    /**
     * Action Listener interface for all the buttons on this components
     * dialog.
     * @param The ActionEvent from the appropriate button.
     */
    public void actionPerformed(ActionEvent ae) {
	if (ae.getSource() == setLayoutOptions) {
	    // Show the dialog
	    optionsDialog.setVisible(true);	    
	}
	else if (ae.getSource() == doLayout) {
	    // First set the layout manager
	    Rectangle2D bounds = camera.getViewBounds();	    	    
	    switch (currentLayout) {
	    case LINE:
		((ZLayoutManagerPath)layoutManager).setShape(new Line2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(float)bounds.getHeight()/10.0f+(float)bounds.getY(),(float)bounds.getX()+(float)bounds.getWidth()-(float)bounds.getWidth()/10.0f,(float)bounds.getY()+(float)bounds.getHeight()-(float)bounds.getHeight()/10.0f));
		((ZLayoutManagerPath)layoutManager).setClosed(false);
		break;
	    case ELLIPSE:
		((ZLayoutManagerPath)layoutManager).setShape(new Ellipse2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(float)bounds.getHeight()/10.0f+(float)bounds.getY(),(8.0f/10.0f)*(float)bounds.getWidth(),(8.0f/10.0f)*(float)bounds.getHeight()));
		((ZLayoutManagerPath)layoutManager).setClosed(true);
		break;
	    case RECT:
		((ZLayoutManagerPath)layoutManager).setShape(new Rectangle2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(float)bounds.getHeight()/10.0f+(float)bounds.getY(),(8.0f/10.0f)*(float)bounds.getWidth(),(8.0f/10.0f)*(float)bounds.getHeight()));
		((ZLayoutManagerPath)layoutManager).setClosed(true);
		break;
	    case QUAD:
		((ZLayoutManagerPath)layoutManager).setShape(new QuadCurve2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(9.0f/10.0f)*(float)bounds.getHeight()+(float)bounds.getY(),(float)bounds.getWidth()/2.0f+(float)bounds.getX(),(float)bounds.getY()-(float)bounds.getHeight()/1.5f,(9.0f/10.0f)*(float)bounds.getWidth()+(float)bounds.getX(),(9.0f/10.0f)*(float)bounds.getHeight()+(float)bounds.getY()));
		((ZLayoutManagerPath)layoutManager).setClosed(false);	
		break;
	    case CUBIC:
		((ZLayoutManagerPath)layoutManager).setShape(new CubicCurve2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(1.0f/2.0f)*(float)bounds.getHeight()+(float)bounds.getY(),(float)bounds.getWidth()/3.0f+(float)bounds.getX(),(float)bounds.getY()-(float)bounds.getHeight()/1.5f,(2.0f/3.0f)*(float)bounds.getWidth()+(float)bounds.getX(),(2.5f/1.5f)*(float)bounds.getHeight()+(float)bounds.getY(),(9.0f/10.0f)*(float)bounds.getWidth()+(float)bounds.getX(),(1.0f/2.0f)*(float)bounds.getHeight()+(float)bounds.getY()));
		((ZLayoutManagerPath)layoutManager).setClosed(false);	
		break;
	    case ARC:
		((ZLayoutManagerPath)layoutManager).setShape(new Arc2D.Float((float)bounds.getWidth()/10.0f+(float)bounds.getX(),(1.0f/4.0f)*(float)bounds.getHeight()+(float)bounds.getY(),(8.0f/10.0f)*(float)bounds.getWidth(),(8.0f/10.0f)*(float)bounds.getHeight(),0.0f,180.0f,Arc2D.OPEN));
		((ZLayoutManagerPath)layoutManager).setClosed(false);	
		break;
	    case POLY:
		if (polyPath != null) {
		    ((ZLayoutManagerPath)layoutManager).setShape(polyPath);
		    ((ZLayoutManagerPath)layoutManager).setClosed(false);
		}
		break;
	    case TREE:
		// Here for later Tree Options
		break;
	    }
	    
	    // Perform the layout on the currently selected nodes
	    // First group them - then layout - then ungroup
	    Vector nodes = layer.getSelectedChildren();
	    
	    // There are no nodes to group
	    if (nodes.isEmpty()) return;
	    
	    // can't group a single node - so just layout
	    if (nodes.size() == 1) {
		((ZNode)nodes.elementAt(0)).setLayoutManager(layoutManager);
		((ZNode)nodes.elementAt(0)).doLayout();
	    }
	    
	    // create a new groupNode, link it to the layer node
	    ZNode groupNode = new ZNode();
	    
	    layer.addChild(groupNode);
	    
	    // remove nodes to be grouped from the layer, add them
	    // to the new groupNode
	    ZNode topNode;
	    Vector topNodes = new Vector();
	    for (Iterator i=nodes.iterator(); i.hasNext();) {
		// climb up tree to layer node (handles groups of groups)
		topNode = (ZNode)i.next();
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

	    // Now we layout
	    if ((currentLayout != POLY) || (polyPath != null)) {
		groupNode.setLayoutManager(layoutManager);
		groupNode.doLayout();
	    }

	    // Now we remove the group
	    for (Iterator v=topNodes.iterator(); v.hasNext();) {
		topNode = (ZNode)v.next();
		layer.addChild(topNode);
	    }
	    
	    layer.removeChild(groupNode);
	    
	    surface.restore();
	}
	else if (ae.getSource() == done) {
	    // Dismiss the dialog
	    optionsDialog.setVisible(false);
	}
	else if (ae.getSource() == treeOption) {
	    // Tree Layout was selected - disable path options
	    lineOption.setEnabled(false);
	    ellipseOption.setEnabled(false);
	    rectOption.setEnabled(false);
	    quadOption.setEnabled(false);
	    cubicOption.setEnabled(false);
	    arcOption.setEnabled(false);
	    polyOption.setEnabled(false);
	    polyButton.setEnabled(false);	    
	    
	    currentLayout = TREE;
	    layoutManager = new ZLayoutManagerTree();

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);
	}
	else if (ae.getSource() == pathOption) {
	    // Path Layout was selected - disable tree options
	    lineOption.setEnabled(true);
	    ellipseOption.setEnabled(true);
	    rectOption.setEnabled(true);
	    quadOption.setEnabled(true);
	    cubicOption.setEnabled(true);
	    arcOption.setEnabled(true);
	    polyOption.setEnabled(true);
	    polyButton.setEnabled(false);	    
	    
	    lineOption.setSelected(true);
	    
	    currentLayout = LINE;
	    layoutManager = new ZLayoutManagerPath();

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == lineOption) {

	    currentLayout = LINE;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);
	}
	else if (ae.getSource() == ellipseOption) {
	    
	    currentLayout = ELLIPSE;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == rectOption) {

	    currentLayout = RECT;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == quadOption) {

	    currentLayout = QUAD;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == cubicOption) {

	    currentLayout = CUBIC;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == arcOption) {

	    currentLayout = ARC;
	    polyButton.setEnabled(false);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == polyOption) {

	    currentLayout = POLY;
	    polyButton.setEnabled(true);

	    action = new ActionEvent(doLayout,0,"Fake Event");
	    actionPerformed(action);	    
	}
	else if (ae.getSource() == polyButton) {
	    
	    // We need to see if a single ZPolyline is selected
	    // - if so - set that as the path - otherwise
	    // don't let this option be selected
	    Vector nodes = layer.getSelectedChildren();
	    boolean reset = false;
	    
	    if (nodes.size() == 1) {

		ZNode node = (ZNode)nodes.elementAt(0);
		ZSelectionDecorator decorator = (ZSelectionDecorator)node.getVisualComponent();
		ZVisualComponent vc = decorator.getChild();
		if (vc instanceof ZPolyline) {
		    polyPath = (GeneralPath)((ZPolyline)vc).getPath().clone();
		    polyPath.transform(node.getTransform().getAffineTransform());
		}

	    }

	}
    }
    
    
}






