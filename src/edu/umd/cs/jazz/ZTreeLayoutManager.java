package edu.umd.cs.jazz;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.component.ZStroke;

/**
 * <b>ZTreeLayoutManager</b> implements a generic tree layout manager
 * that can layout hierarchical Jazz objects, ie. a scenegraph.  This
 * layout does not give each child subtree in the scenegraph enough room for
 * the bounding box of the entire subtree, rather it tries to minimize the
 * total space used while maintaining no overlap among child subtrees.
 *
 * @author  Jin Tong
 * @author  Ben Bederson
 * @author  Lance Good 
 */
public class ZTreeLayoutManager implements ZLayoutManager, ZSerializable {

    /**
     * Vertical Tree Layout - a top to bottom layout
     */
    public static final int ORIENT_VERTICAL   = 0;

    /**
     * Horizontal Tree Layout - a left to right layout
     */
    public static final int ORIENT_HORIZONTAL = 1;

    /**
     * Heading style that puts current node at the middle of its
     * immediate children
     */
    public static final int HEAD_IN   = 0;

    /**
     * Heading style that puts current node at the middle of all
     * its children
     */
    public static final int HEAD_OUT  = 1;

    /**
     * Heading style that puts current node at one side of all its children
     * (ie. left-most or top-most)
     */
    public static final int HEAD_SIDE = 2;
    
    /**
     * This option connects parent nodes to child nodes with a straight line
     */
    public static final int LINK_STRAIGHTLINE = 0;

    /**
     * This option connects parent nodes to child nodes with vertical and
     * horizontal lines only
     */
    public static final int LINK_ANGLEDLINE   = 1;

    
    //
    // Internal Variables
    //

    // The Default Spacing
    protected static float DEFAULT_SPACING = 20.0f;

    // A Permanent holder for an Origin point
    protected static final Point2D ORIGIN = new Point2D.Float(0.0f, 0.0f);
    
    // Current Heading Style
    protected int currentHeadStyle = HEAD_IN;

    // Current Orientation
    protected static int currentOrientation = ORIENT_VERTICAL;

    // Current X Spacing
    protected float currentXSpacing = DEFAULT_SPACING;

    // Current Y Spacing
    protected float currentYSpacing = DEFAULT_SPACING;

    // Current Link Style
    protected int currentLinkStyle = LINK_STRAIGHTLINE;

    // Are links visible?
    protected boolean linkVisible = true;

    // A hashtable to store the areas for nodes using this manager
    protected Hashtable areaManager = new Hashtable();

    // A hashtable to store the transforms for nodes in the current
    // set of recursive calls to doLayout
    protected Hashtable transformTable = new Hashtable();

    // A hashtable to store nodes that need transforming in the current
    // set of recursive calls to doLayout
    protected ArrayList transformNodes = new ArrayList();

    // The current level of recursion in this layout manager
    protected int recurseLevel = 0;    

    
    /**
     * The default constructor - uses all default values
     */
    public ZTreeLayoutManager() {
    }

    /**
     * Fully qualified constructor
     * @param orientation The desired tree layout orientation - ORIENT_VERTICAL or ORIENT_HORIZONTAL
     * @param headingStyle The desired head style - HEAD_IN, HEAD_OUT, or HEAD_SIDE
     * @param showLink Should links be displayed
     * @param linkStyle If links are displayed, the style of links - LINK_STRAIGHTLINE or LINK_ANGLEDLINE
     */
    public ZTreeLayoutManager(int orientation, int headingStyle, boolean showLink, int linkStyle) {
	currentOrientation = orientation;
	currentHeadStyle = headingStyle;
	if (showLink) {
	    linkVisible = true;
	    setLinkStyle(null, linkStyle);
	}
	else {
	    linkVisible = false;
	}
    }
    
    /**
     * Set the current orientation. If the orientation
     * is not supported, nothing will happen. 
     * @param orientation   the desired orientation - ORIENT_VERTICAL or ORIENT_HORIZONTAL
     * @return              <code>true</code> if orientation
     *                      set, <code>false</code> if orientation
     *                      not supported.
     */
    public boolean setCurrentOrientation(ZLayoutGroup layout, int orientation) {
	boolean rc;

	if ((orientation == ORIENT_VERTICAL) ||
	    (orientation == ORIENT_HORIZONTAL)) {
	    currentOrientation = orientation;
	    rc = true;
	}
	else {
	    rc = false;
	}

	if (layout != null && rc == true) {
	    ZLayoutGroup.invalidateChildren(layout);
      	    layout.invalidate();
	}

	return rc;
    }   


    /**
     * Get the current orientation.
     *
     * @return   returns the current orientation for this tree
     */
    public int getCurrentOrientation() {
	return currentOrientation;
    }

    
    /**
     * @param h    the desired heading style - HEAD_IN, HEAD_OUT, or HEAD_SIDE
     * @return     <code>true</code> if set successfuly, <code>false</code>
     *             otherwise
     */
    public boolean setCurrentHeadingStyle(ZLayoutGroup layout, int h) {
	boolean rc;

	if (((h == HEAD_IN) ||
	     (h == HEAD_OUT) ||
	     (h == HEAD_SIDE)) &&
	     currentHeadStyle != h) {
	    currentHeadStyle = h;
	    rc = true;
	}
	else {
	    rc = false;
	}

	if (layout != null && rc == true) {
	    ZLayoutGroup.invalidateChildren(layout);
	    layout.invalidate();
	}

	return rc;
    }


    /**
     * Get the current heading style
     * @return the current heading style
     */
    public int getCurrentHeadStyle() {
	return currentHeadStyle;
    }


    /**
     * @param x  the value for x spacing -- horizontal spacing
     * @return     <code>true</code> if set successfuly, <code>false</code>
     *             otherwise
     */
    public boolean setCurrentXSpacing(float x) {
	if ((x > 0.0f) && (x != currentXSpacing)) {
	    currentXSpacing = x;
	    return true;
	}
	else {
	    return false;
	}
    }


    /**
     * @param y  the value for y spacing -- horizontal spacing
     * @return     <code>true</code> if set successfuly, <code>false</code>
     *             otherwise
     */
    public boolean setCurrentYSpacing(float y) {
	if ((y > 0.0f) && (y != currentYSpacing)) {
	    currentYSpacing = y;
	    return true;
	}
	else {
	    return false;
	}
    }


    /**
     * Get the current x spacing
     * @return   current x spacing
     */
    public float getCurrentXSpacing() {
	return currentXSpacing;
    }


    /**
     * Get the current y spacing
     * @return   current y spacing
     */
    public float getCurrentYSpacing() {
	return currentYSpacing;
    }


    /**
     * Get the value of linkStyle.
     * @return Value of linkStyle.
     */
    public int getLinkStyle() {
	return currentLinkStyle;
    }


    /**
     * Set the value of linkStyle.
     * @param v  Value to assign to linkStyle - LINK_STRAIGHTLINE or LINK_ANGLEDLINE
     * @return <code>true</code> if set successfuly
     */
    public boolean setLinkStyle(ZLayoutGroup layout, int v) {
	boolean rc;
	
	if (((v == ZTreeLayoutManager.LINK_STRAIGHTLINE) ||
	     (v == ZTreeLayoutManager.LINK_ANGLEDLINE)) &&
	    (v != currentLinkStyle)) {
	    currentLinkStyle = v;
	    rc = true;
	}
	else {
	    rc = false;
	}
	
	if (layout != null) {
	    ZLayoutGroup.invalidateChildren(layout);
	    layout.invalidate();
	}
	
	return rc;
    }


    /**
     * Method from the ZLayoutManager interface
     * Called before doLayout
     * @param aLayoutGroup The layout group currently under consideration
     */
    public void preLayout(ZGroup aLayoutGroup) {
	recurseLevel++;
    }


    /**
     * Method from the ZLayoutManager interface
     * Called to layout the layout group
     * @param aLayoutGroup The layout group currently under consideration
     */
    public void doLayout(ZGroup aLayoutGroup) {

	ZNode primary = aLayoutGroup.editor().getNode();
	
	if (primary instanceof ZGroup) {
	    calculateChildrenLayout((ZGroup)primary);
	}
	
	computeNodeArea(primary);
    }


    /**
     * Method from the ZLayoutManager interface
     * Called after doLayout
     * @param aLayoutGroup The layout group currently under consideration
     */
    public void postLayout(ZGroup aLayoutGroup) {
	recurseLevel--;
	if (recurseLevel == 0) {
	    updateTree();
	    resetTransformVariables();
	    updateInvalidLinks(aLayoutGroup.editor().getLayoutGroup());
	}
    }


    /**
     * Appropriately Lays out the children of the provided node 
     * @param aPrimaryGroup The primary group for which the children should be laid out
     */
    protected void calculateChildrenLayout(ZGroup aPrimaryGroup) {
	float lastX = 0.0f;
	float lastY = 0.0f;
	Area currentUsage = new Area();
	Area childUsage = null;
	ZBounds immediateChildrenBounds = new ZBounds();

	ZNode[] children = aPrimaryGroup.getChildren();
	ZSceneGraphEditor childEditor;
	ZNode childPrimary;
	double[] matrix = new double[6];

	for(int i=0; i<children.length; i++) {
	    childEditor = children[i].editor();
	    childPrimary = childEditor.getNode();
	    ZBounds bounds = null;

	    if (i == 0) {

		Point2D trans = setDestinationPoint(childPrimary, ORIGIN);
		updateChildArea(childPrimary, trans);		

		
		bounds = getFrontVisualComponentBounds(childPrimary);
		if (bounds == null) {
		    bounds = new ZBounds();
		}
		bounds.transform(childEditor.getTransformGroup().getTransform());
		padBounds(bounds);
		bounds.setRect(0.0f,0.0f,(float)bounds.getWidth(),(float)bounds.getHeight());

		currentUsage.add(getNodeArea(childPrimary));
	
		lastX = (float)bounds.getWidth();
		lastY = (float)bounds.getHeight();
	    }
	    else {
		Point2D dest;
		if (currentOrientation == ORIENT_VERTICAL) {
		    dest = new Point2D.Float(lastX,0.0f);
		}
		else {
		    dest = new Point2D.Float(0.0f,lastY);
		}

		Point2D trans = setDestinationPoint(childPrimary,dest);

		childUsage = updateChildArea(childPrimary,trans);

		do {

		    trans = computeOverlap(currentUsage, childUsage);

		    dest.setLocation((float)dest.getX()+(float)trans.getX(),
				     (float)dest.getY()+(float)trans.getY());


		    translateDestinationPoint(childPrimary,trans);
		    childUsage = updateChildArea(childPrimary,trans);

		} while (trans.getX() > 0.0 || trans.getY() > 0.0);


		bounds = getFrontVisualComponentBounds(childPrimary);
		if (bounds == null) {
		    bounds = new ZBounds();
		}
		bounds.transform(childEditor.getTransformGroup().getTransform());		
		padBounds(bounds);

		bounds.setRect((float)dest.getX(),(float)dest.getY(),
			       (float)bounds.getWidth(),(float)bounds.getHeight());

		currentUsage.add(childUsage);

		lastX = (float)bounds.getX()+(float)bounds.getWidth();
		lastY = (float)bounds.getY()+(float)bounds.getHeight();
	    }

	    immediateChildrenBounds.add(bounds);
	}

	if (children.length > 0) {
	    ZBounds bounds = getFrontVisualComponentBounds(aPrimaryGroup);

	    if (bounds != null) {

		float transX = 0.0f;
		float transY = 0.0f;

		Rectangle2D allChildrenBounds = currentUsage.getBounds();

		if (currentHeadStyle == HEAD_IN) {
		    if (currentOrientation == ORIENT_VERTICAL) {
			transX = 0.5f*((float)bounds.getWidth()-(float)immediateChildrenBounds.getWidth()) +
			    (float)bounds.getX()-(float)immediateChildrenBounds.getX();
			transY = (float)bounds.getHeight() + 0.5f*currentYSpacing;

		    }
		    else {
			transX = (float)bounds.getWidth() + 0.5f*currentXSpacing;

			transY = 0.5f*((float)bounds.getHeight()-(float)immediateChildrenBounds.getHeight()) +
			    (float)bounds.getY()-(float)immediateChildrenBounds.getY();
			
		    }
		}
		else if (currentHeadStyle == HEAD_OUT) {
		    if (currentOrientation == ORIENT_VERTICAL) {
			transX = 0.5f*((float)bounds.getWidth()-(float)allChildrenBounds.getWidth()) +
			    (float)bounds.getX()-(float)allChildrenBounds.getX();
			transY = (float)bounds.getHeight() + 0.5f*currentYSpacing;
		    }
		    else {
			transX = (float)bounds.getWidth() + 0.5f*currentXSpacing;
			transY = 0.5f*((float)bounds.getHeight()-(float)allChildrenBounds.getHeight()) +
			    (float)bounds.getY()-(float)allChildrenBounds.getY();
		    }
		}
		else {
		    if (currentOrientation == ORIENT_VERTICAL) {
			transX = (float)bounds.getX()-(float)immediateChildrenBounds.getX()-0.5f*currentXSpacing;
			transY = (float)bounds.getHeight() + 0.5f*currentYSpacing;
		    }
		    else {
			transX = (float)bounds.getWidth() + 0.5f*currentXSpacing;
			transY = (float)bounds.getY()-(float)immediateChildrenBounds.getY()-0.5f*currentYSpacing;			
		    }
		}

		Point2D trans = new Point2D.Float(transX,transY);

		// Translate each child the appropriate amount
		for(int i=0; i<children.length; i++) {
		    childPrimary = children[i].editor().getNode();
		    translateDestinationPoint(childPrimary, trans);
		    updateChildArea(childPrimary,trans);
		}

	    }	    
	}

					// Update the links if available
	ZLayoutGroup layoutGroup = aPrimaryGroup.editor().getLayoutGroup();
	if ((layoutGroup != null) && (linkVisible)) {
	    ZVisualComponent vc = layoutGroup.getFrontVisualComponent();
	    ZTreeLayoutManagerLink link = null;
	    if ((vc == null) || !(vc instanceof ZTreeLayoutManagerLink)) {
		link = new ZTreeLayoutManagerLink();
		layoutGroup.setFrontVisualComponent(link);
	    } else {
		link = (ZTreeLayoutManagerLink)vc;
		link.setLinkDirty(true);
	    }
	}
	// Need to set immediate children links dirty in case they aren't
	// explicity laid out themselves
	if ((linkVisible)) {
	    for (int i=0; i<children.length; i++) {
		childEditor = children[i].editor();
		if (childEditor.hasLayoutGroup()) {
		    ZVisualComponent vc = childEditor.getLayoutGroup().getFrontVisualComponent();
		    if (vc instanceof ZTreeLayoutManagerLink) {
			((ZTreeLayoutManagerLink)vc).setLinkDirty(true);
		    }
		}
	    }
	}	
	
    }


    /**
     * Actually transforms all nodes that have a stored transform in the
     * transformTable
     */
    protected void updateTree() {
	
	Object[] groups = transformNodes.toArray();
	
	AffineTransform[] transforms = new AffineTransform[groups.length];
	
	for(int i=0; i<groups.length; i++) {
	    transforms[i] = ((ZTransformGroup)groups[i]).getTransform();
	    transforms[i].preConcatenate((AffineTransform)transformTable.get(groups[i]));
	    ((ZTransformGroup)groups[i]).setTransform(transforms[i]);
	}
	    
    }

    
    /**
     * Translates the transform for the transform corresponding to the given
     * node in the transformTable
     * This is meant to be a pure translation that will not depend on any
     * scaling in this node's transform - ie. this resulting transform
     * will be preConcatenated with the node's current transform
     * @param aPrimaryNode the primary node to transform
     * @param trans the amount to translate
     */
    protected void translateDestinationPoint(ZNode aPrimaryNode, final Point2D trans) {
	AffineTransform at = new AffineTransform();
	at.translate((float)trans.getX(),(float)trans.getY());

	ZTransformGroup transGroup = aPrimaryNode.editor().getTransformGroup();
	
	AffineTransform oldAt = (AffineTransform)transformTable.get(transGroup);

	if (oldAt == null) {
	    transformNodes.add(transGroup);
	    transformTable.put(transGroup, at);
	}
	else {
	    // Since the transform can only be translations we don't
	    // have to worry about preconcatenate or concatenate
	    // We also know that the node has been added to the list
	    oldAt.concatenate(at);
	    transformTable.put(transGroup, oldAt);
	}
    }


    /**
     * Sets the bounds location for the given node to the specified point
     * The resulting translation is meant to be a pure translation that will
     * not depend on any scaling in this node's transform - ie. this resulting
     * will be preConcatenated with the node's current transform
     * @param aPrimaryNode the primary node to transform
     * @param trans the amount to translate
     * @return The translation needed to set the destination point to
     *         <code>dest</code>
     */
    protected Point2D setDestinationPoint(ZNode aPrimaryNode, final Point2D dest) {
	ZTransformGroup transGroup = aPrimaryNode.editor().getTransformGroup();
	AffineTransform at = transGroup.getTransform();		
	ZBounds localBounds = getFrontVisualComponentBounds(aPrimaryNode);
	
	if (localBounds == null) {
	    localBounds = new ZBounds();	    
	}
	localBounds.transform(at);
	padBounds(localBounds);

	// Make the new transform be such that if it is preconcatenated with
	// the old one - the primaryNode is at dest
	Point2D trans = new Point2D.Float((float)(dest.getX()-localBounds.getX()),(float)(dest.getY()-localBounds.getY()));

	at.setToTranslation((float)trans.getX(),(float)trans.getY());  

	// This is really slick - put on a hashtable returns the last object
	// stored with this key or null if none
	if (transformTable.put(transGroup,at) == null) {
	    transformNodes.add(transGroup);
	};

	return trans;
    }


    /**
     * Translates the child nodes area, potentially stored in the area manager,
     * by the specified translation
     * @param aPrimaryNode the primary child node whose area is to be updated
     * @param trans the translation by which the area is to be updated
     * @return The currently stored area for the given node
     */
    protected Area updateChildArea(ZNode aPrimaryNode, Point2D trans) {

	AffineTransform at = new AffineTransform();
	at.translate((float)trans.getX(),(float)trans.getY());

	Area area = (Area)areaManager.get(aPrimaryNode);

	if (area == null) {
	    ZBounds bounds = aPrimaryNode.getBounds();
	    bounds.transform(aPrimaryNode.editor().getTransformGroup().getTransform());
	    padBounds(bounds);
	    area = new Area(bounds);
	}
	    
	area.transform(at);
       
	areaManager.put(aPrimaryNode,area);
	return area;
    }


    /**
     * Computes the given nodes area - this includes the bounds of its visual
     * component and the stored areas for its immediate children
     * @param aPrimaryNode the node for which the area is computed
     */
    protected void computeNodeArea(ZNode aPrimaryNode) {

	Area area;
	AffineTransform at = aPrimaryNode.editor().getTransformGroup().getTransform();
				// Now add node's own local bounds
	ZBounds bounds = getFrontVisualComponentBounds(aPrimaryNode);

	if (bounds != null) {
	    bounds.transform(at);
	    padBounds(bounds);
	    area = new Area(bounds);
	}
	else {
	    area = new Area();
	}
    
			        // Walk through all children to add their area/bounds
	if (aPrimaryNode instanceof ZGroup) {
	    ZNode[] children = ((ZGroup)aPrimaryNode).getChildren();
	    ZSceneGraphEditor editor;
	    for (int i=0; i<children.length; i++) {
		editor = children[i].editor();
		Area childArea;

		try {		    
		    childArea = getNodeArea(editor.getNode());
		}
		catch (Exception e) {
		    childArea = new Area();
		}
		
		childArea.transform(at);
		area.add(childArea);
	    }
	}
	
				// Put it in the table.
	areaManager.put(aPrimaryNode,area);
    }


    /**
     * Gets the stored area for the given node
     * @param aPrimaryNode The node for which the area is desired
     */
    protected Area getNodeArea(ZNode aPrimaryNode) throws ConcurrentModificationException {
				// If node has area info in areaManager,
	                        // return a copy
				// Otherwise, return the bounds.
	Area area = (Area)(((Area)areaManager.get(aPrimaryNode)).clone());

	// updateChildrenArea was already called, this shouldn't happen
	if (area == null) {
	    throw new ConcurrentModificationException("Jazz Scenegraph Modified Outside The Swing Event Thread");
	}

	return area;
    }


    /**
     * Convenience method to get the bounds of the front visual component of
     * the given node
     * @param aVisualNode The node for which the visual component bounds are desired
     */
    protected ZBounds getFrontVisualComponentBounds(ZNode aVisualNode) {
	ZBounds bounds = null;
	if (aVisualNode instanceof ZVisualLeaf) {
	    bounds = ((ZVisualLeaf)aVisualNode).getVisualComponent().getBounds();
	} else if (aVisualNode instanceof ZVisualGroup) {
	    bounds = ((ZVisualGroup)aVisualNode).getFrontVisualComponentBounds();
	}

	return bounds;
    }


    /**
     * Convenience method to pad the given bounds with the current spacing
     * @param bounds the bounds to be padded
     * @return Convenience return of the padded bounds
     */
    protected ZBounds padBounds(ZBounds bounds) {
	float spaceX = getCurrentXSpacing();
	float spaceY = getCurrentYSpacing();
	bounds.setRect(((float)bounds.getX()) - 0.5f*spaceX,
		       ((float)bounds.getY()) - 0.5f*spaceY,
		       ((float)bounds.getWidth()) + spaceX,
		       ((float)bounds.getHeight())+ spaceY);
	return bounds;
    }


    /**
     * Returns the overlap of the two given areas
     * @param a one area for which overlap is computed
     * @param b one area for which overlap is computed
     * @return The current amount to translate to reduce overlap
     */
    protected Point2D computeOverlap(Area a, Area b) {

	Point2D.Float retVal = new Point2D.Float();
	float x = 0f;
	float y = 0f;
	Area tmp = (Area)a.clone();
	tmp.intersect(b);
	if (!tmp.isEmpty()) {

	    Rectangle bound = tmp.getBounds();
	    if (currentOrientation == ORIENT_VERTICAL) {
		// Warning: the clash area bound's width may not
		//  always be the right offset to translate. This is
		//  just a safe (yet potentially slow) solution: to move
		//  the spacing when the width is too big


	        x = ((float)bound.getWidth() > currentXSpacing) ?
		    currentXSpacing:
		(float)bound.getWidth();

	    }
	    else { // left right layout
		
		y = ((float)bound.getHeight() > currentYSpacing) ?
		    currentYSpacing:
		(float)bound.getHeight();

	    }
	}
	retVal.setLocation(x,y);	

	// Now we try to eliminate rounding error and any unnecessary translation
	Rectangle2D bounds = tmp.getBounds2D();
	Rectangle2D boundsRound = new Rectangle2D.Float((float)(int)(bounds.getX()+1.0),
							(float)(int)(bounds.getY()+1.0),
							(float)(int)(bounds.getWidth()),
							(float)(int)(bounds.getHeight()));
	Area round = new Area(boundsRound);
	tmp.intersect(round);
	if (tmp.isEmpty()) {
	    retVal.setLocation(0.0f, 0.0f);
	}
	
	return retVal;
    }


    /**
     * Resets the transformTable and the transformNodes
     */
    protected void resetTransformVariables() {
	transformTable.clear();
	transformNodes.clear();
    }

    
    /**
     * Updates all links, in depth-first order, below the supplied ZNode
     * @param top The node below which all links should be updated
     */
    protected static void updateInvalidLinks(ZNode top) {
	if (top instanceof ZGroup) {
	    
	    ZNode[] children = ((ZGroup)top).getChildren();
	    for(int i=0; i<children.length; i++) {
		updateInvalidLinks(children[i]);
	    }
	    
	    if (top instanceof ZLayoutGroup) {
		ZVisualComponent vis = ((ZLayoutGroup)top).getFrontVisualComponent();
		if (vis instanceof ZTreeLayoutManagerLink) {
		    ((ZTreeLayoutManagerLink)vis).updateLink();
		}		    
	    }
	}
    }
	

    

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////


    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
    }


    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
    }


    /**
     * Set some state of this object as it gets read back in.
     * After the object is created with its default no-arg constructor,
     * this method will be called on the object once for each bit of state
     * that was written out through calls to ZObjectOutputStream.writeState()
     * within the writeObject method.
     * @param fieldType The fully qualified type of the field
     * @param fieldName The name of the field
     * @param fieldValue The value of the field
     */
    public void setState(String fieldType, String fieldName, Object fieldValue) {
    }

    
    /**
     * This Visual Component renders the links between a ZVisualGroup parent
     * node and its visual children for the ZTreeLayoutManager
     *
     * The scenegraph structure should look something like:
     *
     *    ...
     *    ZLayoutNode      
     *      => ZTreeLayoutManagerLink [Visual link between PARENT and CHILD(ren)]
     *      ...
     *      ZTransformNode
     *         ...
     *         ZVisualGroup [**This is the PARENT]
     *            ...
     *            ZTransformNode
     *               ...
     *               ZVisual(Group or Leaf) [**This is a CHILD]
     *
     *
     * NOTE:  This visual component cannot be reused 
     *        (ie. have more than one parent per instance)
     */
    class ZTreeLayoutManagerLink extends ZVisualComponent implements ZStroke {
	
	//  Pen color of the link     
	protected Color penColor  = Color.black;
	
	// The Basic Stroke
	protected BasicStroke stroke = new BasicStroke(2);
	
	// The pen width
	protected float penWidth = 1;
	
	// The visual link
	Shape visLink = null;
	
	// Control for link dirty, this one is local
	protected  boolean linkDirty = true;
	
	
	public ZTreeLayoutManagerLink() {
	    setLinkDirty(true);
	}
	
	/**
	 * Correctly computes the bounds for this link
	 */
	protected void computeBounds() {
	    if (visLink != null) {
		bounds = new ZBounds(visLink.getBounds());
	    }
	    else {
		bounds.setRect(0.0f,0.0f,0.0f,0.0f);
	    }
	}
	
	/**
	 * Get the value of linkDirty.
	 * @return Value of linkDirty.
	 */
	public boolean getLinkDirty() {return linkDirty;}
	
	/**
	 * Set the value of linkDirty.
	 * @param v  Value to assign to linkDirty.
	 */
	public void setLinkDirty(boolean  v) {linkDirty = v;}
	
	
	/**
	 * Get the pen color of the selection visual
	 * @return the pen color
	 */   
	public Color getPenColor() {
	    return penColor;
	}
	
	/**
	 * Specify the pen color of the selection visual
	 * @param color The new pen color
	 */
	public void setPenColor(Color color) {
	    penColor = color;
	    setLinkDirty(true);
	    repaint();
	}   
	
	/**
	 * Get the current stroke
	 * @return The current stroke.
	 */
	public Stroke getStroke() {
	    return stroke;
	}

	/**
	 * Sets the stroke
	 * @param s the new stroke
	 */
	public void setStroke(Stroke s) {
	    if (s instanceof BasicStroke) {
		stroke = (BasicStroke)s;
		repaint();
	    }
	}

	/**
	 * Get the current pen width
	 * @return The current pen width
	 */
	public float getPenWidth() {
	    return penWidth;
	}

	/**
	 * Sets the pen width
	 * @param w The new pen width
	 */
	public void setPenWidth(float w) {
	    penWidth = w;
	    repaint();
	}

	/**
	 * Renders this link
	 * @param renderContext The current rendering context
	 */
	public void render(ZRenderContext renderContext) {
	    Graphics2D g2 = renderContext.getGraphics2D();
	    
	    float mag = renderContext.getCompositeMagnification();
	    
	    if (mag*stroke.getLineWidth() != penWidth) {
		stroke = new BasicStroke(penWidth/mag);
	    }
	    
	    g2.setStroke(stroke);
	    
	    paint(g2);
	}
	
	/**
	 * Paints this link
	 * @param g2 The graphics to paint into.
	 */
	public void paint(Graphics2D g2) {
	    if (visLink != null) {
		g2.setColor(penColor);
		g2.draw(visLink);
	    }
	}
	
	/**
	 * Update/Create the visLink if needed.
	 */
	protected void updateLink() {
	    if (!linkDirty) {
		return;
	    } else {
		linkDirty = false;
		
		ZNode parent = parents[0];
		ZNode parentPrimary = null;
		ZSceneGraphEditor editor;
		ZTransformGroup parentTransform = null;
		ZNode[] children = null;	    
		
		// The parent's editor
		editor = parent.editor();
		// The parent's primary node
		parentPrimary = editor.getNode();
		// The parent primary's transform
		parentTransform = editor.getTransformGroup();
		
		// Make sure we actually got a Group with Children
		if (parentPrimary instanceof ZGroup) {
		    children = ((ZGroup)parentPrimary).getChildren();
		    
		    if (children.length == 0) {
			visLink = null;
			reshape();
			return;
		    }
		} else {
		    visLink = null;
		    reshape();
		    return;
		}
		
		// Get ParentPrimary's local bounds
		ZBounds parentBounds = ZTreeLayoutManager.this.getFrontVisualComponentBounds(parentPrimary);
		if (parentBounds == null) {
		    parentBounds = new ZBounds();
		}
		
		// Transform the parentPrimary bounds so that they will be
		// appropriate for parent
	    
		// Transform the bounds by the ParentPrimary's transform to put
		// them in the correct coord system for parent (since it is
		// actually above ParentPrimary's transform node
		parentBounds.transform(parentTransform.getTransform());
		
		ZNode child = null;
		ZNode childPrimary = null;
		ZTransformGroup childTransform = null;
		ZBounds childBounds = null;
		
		if (children.length > 0) {
		    visLink = new GeneralPath();
		}
		else {
		    return;
		}
		
		switch (ZTreeLayoutManager.this.currentLinkStyle) {
		case ZTreeLayoutManager.LINK_STRAIGHTLINE:

		    for (int i=0; i<children.length; i++) {
			// Get all the child nodes - top, primary, transform
			child = children[i];
			childPrimary = child.editor().getNode();
			childTransform = childPrimary.editor().getTransformGroup();
			
			// Get the local bounds for childPrimary's vis component
			childBounds = ZTreeLayoutManager.this.getFrontVisualComponentBounds(childPrimary);
			
			// Transform the bounds for parent - since parent is
			// above the transform node for ParentPrimary
			childBounds.transform(childTransform.getTransform());
			childBounds.transform(parentTransform.getTransform());
			
			if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {		    
			    ((GeneralPath) visLink).moveTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							   (float)parentBounds.getY() + (float)parentBounds.getHeight());
			    ((GeneralPath) visLink).lineTo((float)childBounds.getX() + (float)childBounds.getWidth() / 2.0f,
							   (float)childBounds.getY());
			}
			else {
			    ((GeneralPath) visLink).moveTo((float)parentBounds.getX() + (float)parentBounds.getWidth(),
							   (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);
			    ((GeneralPath) visLink).lineTo((float)childBounds.getX(),
							   (float)childBounds.getY() + (float)childBounds.getHeight() / 2.0f);
			}
		    }
		    
		    break;
		case ZTreeLayoutManager.LINK_ANGLEDLINE:
		    
		    ZBounds firstBounds = null;
		    ZBounds lastBounds = null;
		    boolean start = true;
		    ZNode first = null;
		    ZNode last = null;
		    
		    for (int i=0; i<children.length; i++) {
			
			// Get all the child nodes - top, primary, transform
			child = children[i];
			childPrimary = child.editor().getNode();
			childTransform = childPrimary.editor().getTransformGroup();
			
			if (start) {
			    start = false;
			    first = child;
			}
			
			// Get the local bounds
			childBounds = ZTreeLayoutManager.this.getFrontVisualComponentBounds(childPrimary);
			
			// Transform the childPrimary bounds so that they will
			// be appropriate for parent
			
			// Put them in the coord system of the parent - since
			// parent is above ParentPrimary's transform node
			childBounds.transform(childTransform.getTransform());
			childBounds.transform(parentTransform.getTransform());
			

			float yMiddle = (float)parentBounds.getY()+(float)parentBounds.getHeight() + ((float)childBounds.getY() - ((float)parentBounds.getY()+(float)parentBounds.getHeight())) / 2.0f;
			float xMiddle = (float)parentBounds.getX()+(float)parentBounds.getWidth() + ((float)childBounds.getX() - ((float)parentBounds.getX()+(float)parentBounds.getWidth())) / 2.0f;
			
			
			if (first == child) {
			    firstBounds = childBounds;
			}
			
			if ((first == child) && (ZTreeLayoutManager.this.getCurrentHeadStyle() == ZTreeLayoutManager.HEAD_SIDE)) {
			    // Draw the line up from the child
			    if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {


				((GeneralPath) visLink).moveTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							       yMiddle);
				((GeneralPath) visLink).lineTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							       (float)parentBounds.getY() + (float)parentBounds.getHeight());

				((GeneralPath) visLink).moveTo((float)childBounds.getX() + (float)childBounds.getWidth() /2.0f,
							       yMiddle);

				((GeneralPath) visLink).lineTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							       yMiddle);

				((GeneralPath) visLink).moveTo((float)childBounds.getX() + (float)childBounds.getWidth() / 2.0f,
							       (float)childBounds.getY());

				((GeneralPath) visLink).lineTo((float)childBounds.getX() + (float)childBounds.getWidth() /2.0f,
							       yMiddle);
				
			    }
			    else { //if (ZTreeLayoutManager.getCurrentOrientation() == ZTreeLayoutManager.Orientation_HORIZONTAL)

				((GeneralPath) visLink).moveTo(xMiddle,
							       (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);
				((GeneralPath) visLink).lineTo((float)parentBounds.getX() + (float)parentBounds.getWidth(),
							       (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);

				((GeneralPath) visLink).moveTo(xMiddle,
							       (float)childBounds.getY() + (float)childBounds.getHeight() / 2.0f);

				((GeneralPath) visLink).lineTo(xMiddle,
							       (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);

				((GeneralPath) visLink).moveTo((float)childBounds.getX(),
							       (float)childBounds.getY() + (float)childBounds.getHeight() / 2.0f);

				((GeneralPath) visLink).lineTo(xMiddle,
							       (float)childBounds.getY() + (float) childBounds.getHeight() / 2.0f);			

			    }
			}
			else {
			    // Draw the line up from the child
			    if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {
				
				((GeneralPath) visLink).moveTo((float)childBounds.getX() + (float)childBounds.getWidth() / 2.0f,
							       (float)childBounds.getY());

				((GeneralPath) visLink).lineTo((float)childBounds.getX() + (float)childBounds.getWidth() / 2.0f,
							       yMiddle);
				
			    }
			    else { //if (ZTreeLayoutManager.getCurrentOrientation() == ZTreeLayoutManager.Orientation_HORIZONTAL)
				// Half up link
				((GeneralPath) visLink).moveTo((float)childBounds.getX(),
							       (float)childBounds.getY() + (float)childBounds.getHeight() / 2.0f);

				((GeneralPath) visLink).lineTo(xMiddle,
							       (float)childBounds.getY() + (float)childBounds.getHeight() / 2.0f);
				
			    }
			}
			last = child;
			lastBounds = childBounds;
		    } // end for

		    float yMiddle = (float)parentBounds.getY()+(float)parentBounds.getHeight() + ((float)firstBounds.getY() - ((float)parentBounds.getY()+(float)parentBounds.getHeight())) / 2.0f;
		    float xMiddle = (float)parentBounds.getX()+(float)parentBounds.getWidth() + ((float)firstBounds.getX() - ((float)parentBounds.getX()+(float)parentBounds.getWidth())) / 2.0f;
		    
		    // Draw down link if not Heading_SIDE -- Heading_SIDE's down link has already benn drawn
		    if (ZTreeLayoutManager.this.getCurrentHeadStyle() != ZTreeLayoutManager.HEAD_SIDE) {
			if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {
			    
			    ((GeneralPath) visLink).moveTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							   (float)parentBounds.getY() + (float)parentBounds.getHeight());

			    ((GeneralPath) visLink).lineTo((float)parentBounds.getX() + (float)parentBounds.getWidth() / 2.0f,
							   yMiddle);
			    
			}
			else { //if (ZTreeLayoutManager.getCurrentOrientation() == ZTreeLayoutManager.Orientation_HORIZONTAL)
			    ((GeneralPath) visLink).moveTo((float)parentBounds.getX() + (float)parentBounds.getWidth(),
							   (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);


			    ((GeneralPath) visLink).lineTo(xMiddle,
			    			   (float)parentBounds.getY() + (float)parentBounds.getHeight() / 2.0f);
			
			}
		    }
		    
		    // Now draw the straight line
		    if (first != last) {
			if  (ZTreeLayoutManager.this.getCurrentHeadStyle() == ZTreeLayoutManager.HEAD_SIDE) {
			    if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {

				
				((GeneralPath) visLink).moveTo((float)lastBounds.getX() + (float)lastBounds.getWidth() / 2.0f,
							       yMiddle);

				((GeneralPath) visLink).lineTo((float)firstBounds.getX() + (float)(firstBounds.getWidth()) / 2.0f,
							       yMiddle);

				
			    }
			    else { //if (ZTreeLayoutManager.getCurrentOrientation() == ZTreeLayoutManager.Orientation_HORIZONTAL)

				((GeneralPath) visLink).moveTo(xMiddle,
							       (float)lastBounds.getY() + (float)lastBounds.getHeight() / 2.0f);
				((GeneralPath) visLink).lineTo(xMiddle,
							       (float)firstBounds.getY() + (float)firstBounds.getHeight() / 2.0f);
				
			    }
			}
			else {
			    if (ZTreeLayoutManager.this.getCurrentOrientation() == ZTreeLayoutManager.ORIENT_VERTICAL) {

				((GeneralPath) visLink).moveTo((float)lastBounds.getX() + (float)lastBounds.getWidth() / 2.0f,
							       yMiddle);
				
				((GeneralPath) visLink).lineTo((float)firstBounds.getX() + (float)firstBounds.getWidth() / 2.0f,
							       yMiddle);
							       
			    }
			    else { //if (ZTreeLayoutManager.getCurrentOrientation() == ZTreeLayoutManager.Orientation_HORIZONTAL)

				((GeneralPath) visLink).moveTo(xMiddle,
							       (float)lastBounds.getY() + (float)lastBounds.getHeight() / 2.0f);
				((GeneralPath) visLink).lineTo(xMiddle,
							       (float)firstBounds.getY() + (float)firstBounds.getHeight() /2.0f);
				
			    }
			}
		    }
		    
		    break;
		}
		
	    }
	    
	    reshape();	    
	}
    } // ZTreeLayoutManagerLink
    
}