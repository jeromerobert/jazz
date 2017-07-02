package edu.umd.cs.jazz.scenegraph;

import java.util.*;
import java.awt.geom.*;
import java.awt.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZLayoutManagerTree</b> implements a generic tree layout manager
 * that can layout hierarchical Jazz objects.
 *
 * @author  Jin Tong
 */
public class ZLayoutManagerTree implements ZLayoutManager {

    /* support 2 orientations only */
    public static final int Orientation_TOPDOWN   = 0;
    public static final int Orientation_LEFTRIGHT = 1;
    
    public static final int link_STRAIGHTLINE = 0;
    public static final int link_ANGLEDLINE   = 1;
    //public static final int link_BOXLINE      = 2;

    protected static int currentOrientation       = Orientation_TOPDOWN;

    /**
     * Set the current orientation. If the orientation
     * is not supported, nothing will happen. 
     *
     * @param orientation   the orientation want to set
     * @return              <code>true</code> if orientation
     *                      set, <code>false</code> if orientation
     *                      not supported.
     */
    public static boolean setCurrentOrientation(int orientation) {
	if ((orientation == Orientation_TOPDOWN) ||
	    (orientation == Orientation_LEFTRIGHT)) {
	    currentOrientation = orientation;
	    return true;
	}
	else {
	    return false;
	}
    }   

    /**
     * Get the current orientation.
     *
     * @return   returns the current orientation for this tree
     */
    public static int getCurrentOrientation() {
	return currentOrientation;
    }

    // support 3 "heading" styles:
    //   HEADIN : put current node at the middle of its immediate children
    //   HEADOUT: put current node at the middle of all its children
    //   head side: put current node at one side of the all its children 
    //  (topmost, leftmost)  often, HEADIN looks best.
    public static final int Heading_IN   = 0;
    public static final int Heading_OUT  = 1;
    public static final int Heading_SIDE = 2;
    protected static int currentHeadStyle = Heading_IN;

    /**
     * @param h    the heading style want to set
     * @return     <code>true</code> if set successfuly, <code>false</code>
     *             otherwise
     */
    public static boolean setCurrentHeadingStyle(int h) {
	if ((h == Heading_IN) ||
	    (h == Heading_OUT) ||
	    (h == Heading_SIDE)) {
	    currentHeadStyle = h;
	    return true;
	}
	else {
	    return false;
	}
    }

    /**
     * @return the current heading style
     */
    public static int getCurrentHeadStyle() {
	return currentHeadStyle;
    }

    /* spacing defaults */
    public static final float DEFAULT_XSpacing = 20f;
    public static final float DEFAULT_YSpacing = 20f;
    protected static float currentXSpacing       = DEFAULT_XSpacing;
    protected static float currentYSpacing       = DEFAULT_YSpacing;

    /**
     * @param x  the value for x spacing -- horizontal spacing
     * @return     <code>true</code> if set successfuly, <code>false</code>
     *             otherwise
     */
    public static boolean setCurrentXSpacing(float x) {
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
    public static boolean setCurrentYSpacing(float y) {
	if ((y > 0.0f) && (y != currentYSpacing)) {
	    currentYSpacing = y;
	    return true;
	}
	else {
	    return false;
	}
    }
  
    /**
     * @return   current x spacing
     */
    public static float getCurrentXSpacing() {
	return currentXSpacing;
    }

    /**
     * @return   current y spacing
     */
    public static float getCurrentYSpacing() {
	return currentYSpacing;
    }

    /* default anchoring position -- the origin*/
    public static final float DEFAULT_LeftMostPos = 0.0f;
    public static final float DEFAULT_TopMostPos  = 0.0f;

    //  Area management information of all the nodes that use this manager 
    //  goes here in the hashtable. ZNode will be the key, value will be
    //  the Area that node controls. Make it static so all treelayout
    //  share this one.
    protected static Hashtable areaManager = new Hashtable();

    // Reset the hashtable after each round of layout
    //  Used only internally
    private static void resetAreaManager() {
	areaManager.clear();
    }

    // Control whether to show links
    protected static boolean linkVisible = true;

    /**
     * Default constructor, calls a longer version with all the default
     * settings.
     */
    public ZLayoutManagerTree() {
	this(ZLayoutManagerTree.Orientation_TOPDOWN, ZLayoutManagerTree.Heading_IN, true, ZLayoutManagerTree.link_STRAIGHTLINE);
    }

    /**
     * Constructor with all the custom options
     *
     * @param orientation  the orientation style
     * @param headingStyle the heading style
     * @param showLink     switch to control whether link is visible
     * @param linkStyle    the link style
     */
    public ZLayoutManagerTree(int orientation, int headingStyle, boolean showLink, int linkStyle) {
	setCurrentOrientation(orientation);
	setCurrentHeadingStyle(headingStyle);
	if (showLink) {
	    ZLayoutManagerTree.linkVisible = true;
	    ZLayoutManagerTreeLink.setZLinkStyle(linkStyle);
	}
	else {
	    ZLayoutManagerTree.linkVisible = false;
	}
    }
    
    /** 
     * This is a wrapper to update with all this node's chilren
     *  area.
     *
     * @param node   this is the node whose area info needs update
     * @param x      translation along x-axis
     * @param y      translation along y-axis
     */
    private static void updateChildrenArea(ZNode node, float x, float y) {
	for (Iterator i = node.getChildren().iterator(); i.hasNext() ;) {
	    ZNode child = (ZNode) i.next();
	    updateNodeArea(child, x, y);
	}
    }
    
    /**
     * Update this node's area info in area management hashtable.
     *
     * @param node   the node whose area info needs update
     * @return        the area of this node after update
     */
    private static Area updateNodeArea(ZNode node) {
	// Add up all the bounds of each node to an Area
    
	Area area;
	if (areaManager.containsKey(node)) {
	    area = (Area) areaManager.get(node);
	}
	else {
	    area = new Area();
	    // Now add node's own local bounds
	    area.add(new Area(wrapPadding(node.getGlobalCompBounds())));
	}
    
	// Walk through all children to add their area/bounds
	for (Iterator i = node.getChildren().iterator(); i.hasNext() ;) {
	    ZNode child = (ZNode) i.next();
	    if (areaManager.containsKey(child)) {
		area.add((Area) getNodeArea(child));
	    }
	    else {
		// If child's layout manager is not a treelayout, use bounds instead
		area.add(new Area(wrapPadding(child.getGlobalBounds())));
	    }
	}

	// Put it back.
	areaManager.put(node, area);
	return area;
    }
  
    /**
     * Update this node's area by transation
     *
     * @param node    the target node
     * @param transX  transation along x-axis
     * @param transY  transation along y-axis
     * @return        the area of this node after update
     */
    private static Area updateNodeArea(ZNode node, float transX,
				       float transY) {
	// only update this node's area by this af.
	// if this node has not been in manager yet, just put in its bounds
	// (bounds has been transformed alread
	Area area;
	AffineTransform af = new AffineTransform();
	af.translate(transX, transY);

	if (areaManager.containsKey(node)) {
	    area = (Area) areaManager.get(node);
	    area.transform(af);
	}
	else {
	    area = new Area(wrapPadding(node.getGlobalBounds()));
	}
	areaManager.put(node, area);
	return area;
    }


    // Yet another updateNodeArea: this one add in the "protection area"
    //   i.e. the immediateChildrenArea
    private static Area updateNodeArea(ZNode node, ZBounds protectionBox) {
	Area area;
	if (areaManager.containsKey(node)) {
	    area = (Area) areaManager.get(node);
	}
	else {
	    area = new Area();
	    // Now add node's own local bounds
	    area.add(new Area(wrapPadding(node.getGlobalCompBounds())));
	}
	
	// Walk through all children to add their area/bounds
	for (Iterator i = node.getChildren().iterator(); i.hasNext() ;) {
	    ZNode child = (ZNode) i.next();
	    if (areaManager.containsKey(child)) {
		area.add((Area) getNodeArea(child));
	    }
	    else {
		// If child's layout manager is not a treelayout, use bounds instead
		area.add(new Area(wrapPadding(child.getGlobalBounds())));
	    }
	}

	area.add(new Area(protectionBox));
	
	// Put it back.
	areaManager.put(node, area);
	return area;
    }

    
    // Convinience function getArea(ZNode node)
    private static Area getNodeArea(ZNode node) {
	// If node has area info in areaManager, return a copy
	// Otherwise, return the bounds.
	if (areaManager.containsKey(node)) {
	    return (Area) ((Area) areaManager.get(node)).clone();
	}
	else {
	    return new Area(wrapPadding(node.getGlobalBounds()));
	}
    }

    private static void removedNode(ZNode node) {
	// This should remove all its children as well
	// Or for efficiency reason: does it hurt just to remove
	//  the node and not its children from areaManagement ?
	// Just remove node for now
	if (areaManager.containsKey(node)) {
	    areaManager.remove(node);
	}
    }	

    /**
     * Get the value of ZLinkStyle.
     * @return Value of ZLinkStyle.
     */
    public static int getZLinkStyle() {
	return ZLayoutManagerTreeLink.getZLinkStyle();
    }

    /**
     * Set the value of ZLinkStyle.
     * @param v  Value to assign to ZLinkStyle.
     * @return <code>true</code> if set successfuly
     */
    public static boolean setZLinkStyle(int v) {
	return ZLayoutManagerTreeLink.setZLinkStyle(v);
    }

    /**
     * The tree layout algorithm. Layout will place this node at anchor 
     *  place first, and layout all its children alone the base line, 
     *  and translate them so that this node will sit in the middle
     *  (according to heading style.)
     *
     * @param node  the node that needs to lay out its children
     */
    public void doLayout(ZNode node) {
	ZBounds nodeCompWithPadding = wrapPadding(node.getGlobalCompBounds());
	
	float transX = DEFAULT_LeftMostPos - (float) nodeCompWithPadding.getX();
	float transY = DEFAULT_TopMostPos - (float) nodeCompWithPadding.getY();
	node.getTransform().translate(transX, transY);
	ZLayoutManagerTree.updateChildrenArea(node, transX, transY);

	Vector children = node.getChildren();
	if (children.size() == 0) {
	    // This node is a terminal, update area manager
	    ZLayoutManagerTree.updateNodeArea(node, transX, transY);
	}

	else {
	    boolean firstChild = true;
	    Area currentUsage  = new Area();
	    Area childUsage    = null;
	    float lastXPos     = DEFAULT_LeftMostPos;
	    float lastYPos     = DEFAULT_TopMostPos;
	    float currentXPos  = DEFAULT_LeftMostPos;
	    float currentYPos  = DEFAULT_TopMostPos;

	    ZBounds immediateChildrenArea = new ZBounds();

	    // Now layout the children so they won't overlap.
	    for (Iterator i = children.iterator(); i.hasNext() ;) {
		ZNode child = (ZNode) i.next();

		if (firstChild) {
		    // place first child at anchor place as well
		    ZBounds childCompWithPadding = wrapPadding(child.getGlobalCompBounds());
		    transX = DEFAULT_LeftMostPos - (float) childCompWithPadding.getX();
		    transY = DEFAULT_TopMostPos - (float) childCompWithPadding.getY();
		    child.getTransform().translate(transX, transY);
		    currentUsage.add(updateNodeArea(child, transX, transY));
		    childCompWithPadding = wrapPadding(child.getGlobalCompBounds());
		    lastXPos = DEFAULT_LeftMostPos +
			(float) childCompWithPadding.getWidth();
		    lastYPos = DEFAULT_TopMostPos +
			(float) childCompWithPadding.getHeight();
		    firstChild = false;
		}

		else {
		    // Place this one after the last one first
		    ZBounds childCompWithPadding = wrapPadding(child.getGlobalCompBounds());
		    if (ZLayoutManagerTree.currentOrientation ==
			ZLayoutManagerTree.Orientation_TOPDOWN) {
			transX = lastXPos - (float) childCompWithPadding.getX();
			transY = ZLayoutManagerTree.DEFAULT_TopMostPos - (float) childCompWithPadding.getY();
		    }
		    else { // layout left to right
			transY = lastYPos - (float) childCompWithPadding.getY();
			transX = ZLayoutManagerTree.DEFAULT_LeftMostPos - (float) childCompWithPadding.getX();
		    }

		    // Move this much first
		    child.getTransform().translate(transX, transY);
		    childUsage = ZLayoutManagerTree.updateNodeArea(child, transX, transY);

		    Point2D.Float moveStep = ZLayoutManagerTree.overlap(currentUsage, childUsage);

		    // Move until no clash
		    while ((moveStep.getX() > 0.0) || (moveStep.getY() > 0.0)) {
			child.getTransform().translate((float) moveStep.getX(), (float) moveStep.getY());
			childUsage = ZLayoutManagerTree.updateNodeArea(child, (float) moveStep.getX(),
								       (float) moveStep.getY());
			moveStep = ZLayoutManagerTree.overlap(currentUsage, childUsage);
		    }

		    currentUsage.add(childUsage);
		    childCompWithPadding = wrapPadding(child.getGlobalCompBounds());
		    lastXPos = (float) childCompWithPadding.getX() + (float) childCompWithPadding.getWidth();
		    lastYPos = (float) childCompWithPadding.getY() + (float) childCompWithPadding.getHeight();
		}

		// Keep track of the immediate children area
		immediateChildrenArea.add(child.getGlobalCompBounds());
	    } // end for

	    // Second pass, to move all children again, so node is in the middle
	    float nodeW, nodeH, nodeX, nodeY;
	    nodeW = (float) node.getGlobalCompBounds().getWidth();
	    nodeH = (float) node.getGlobalCompBounds().getHeight();
	    nodeX = (float) node.getGlobalCompBounds().getX();
	    nodeY = (float) node.getGlobalCompBounds().getY();
	    
	    if (ZLayoutManagerTree.currentHeadStyle == ZLayoutManagerTree.Heading_IN) {
		if (ZLayoutManagerTree.currentOrientation == ZLayoutManagerTree.Orientation_TOPDOWN) {
		    transX = 0.5f*(nodeW - (float) immediateChildrenArea.getWidth()) +
			nodeX - (float) immediateChildrenArea.getX();
		    transY = nodeH + ZLayoutManagerTree.currentYSpacing;
		}
		else { // left to right style
		    transX = nodeW + ZLayoutManagerTree.currentXSpacing;
		    transY = 0.5f*(nodeH - (float) immediateChildrenArea.getHeight()) +
			nodeY - (float) immediateChildrenArea.getY();
		}
	    }
	    else if (ZLayoutManagerTree.currentHeadStyle == ZLayoutManagerTree.Heading_OUT) {
		if (ZLayoutManagerTree.currentOrientation == ZLayoutManagerTree.Orientation_TOPDOWN) {
		    transX = 0.5f*(nodeW - (float) currentUsage.getBounds().getWidth()) +
			nodeX - (float) currentUsage.getBounds().getX();
		    transY = nodeH + ZLayoutManagerTree.currentYSpacing;
		}
		else { // left to right style
		    transX = nodeW + ZLayoutManagerTree.currentXSpacing;
		    transY = 0.5f*(nodeH - (float) currentUsage.getBounds().getHeight()) +
			nodeY - (float) currentUsage.getBounds().getY();
		}
	    }
	    else { //if (ZLayoutManagerTree.currentHeadStyle == ZLayoutManagerTree.Heading_SIDE)
		if (ZLayoutManagerTree.currentOrientation == ZLayoutManagerTree.Orientation_TOPDOWN) {
		    transX = nodeX - (float) immediateChildrenArea.getX();
		    transY = nodeH + ZLayoutManagerTree.currentYSpacing;
		}
		else { // left to right style
		    transX = nodeW + ZLayoutManagerTree.currentXSpacing;
		    transY = nodeY - (float) immediateChildrenArea.getY();
		}
	    }

	    for (Iterator i = children.iterator(); i.hasNext(); ) {
		ZNode child = (ZNode) i.next();
		child.getTransform().translate(transX, transY);
		ZLayoutManagerTree.updateNodeArea(child, transX, transY);
	    }

	    // Adding immediateChildrenArea as the protection area
	    immediateChildrenArea.setRect(immediateChildrenArea.getX() + transX,
					  immediateChildrenArea.getY() + transY,
					  immediateChildrenArea.getWidth(),
					  immediateChildrenArea.getHeight());
	    ZLayoutManagerTree.updateNodeArea(node, immediateChildrenArea);
	    
	}

	// Update the links if available
	ZLayoutManagerTreeLink link = (ZLayoutManagerTreeLink) node.findVisualComponent(ZLayoutManagerTreeLink.class);
	if (link != null) {
	    link.setLinkDirty(true);
	    link.updateLink();
	}
	else if (ZLayoutManagerTree.linkVisible) {
	    // Make the links
	    ZVisualComponent vis = node.getVisualComponent();
	    ZLayoutManagerTreeLink alink = new ZLayoutManagerTreeLink(vis);
	    alink.updateLink();
	}
	
	// reset the hashtable when tree layout is done.
	if ((node.getParent() == null) || (node.getParent().getLayoutManager() == null) ||
	    (! (node.getParent().getLayoutManager() instanceof ZLayoutManagerTree))) {
	    ZLayoutManagerTree.resetAreaManager();
	}
    }

    // Make the comp bound + half the spacing as the area on node has
    private static ZBounds wrapPadding(ZBounds b) {
	ZBounds bounds = new ZBounds();
	bounds.setRect(((float) b.getX()) - 0.5f*ZLayoutManagerTree.getCurrentXSpacing(),
		       ((float) b.getY()) - 0.5f*ZLayoutManagerTree.getCurrentYSpacing(),
		       ((float) b.getWidth()) + ZLayoutManagerTree.getCurrentXSpacing(),
		       ((float) b.getHeight())+ ZLayoutManagerTree.getCurrentYSpacing());
	return bounds;
    }

    // Checking the clash area. Return the coordinates that
    // holds the translation.
    private static Point2D.Float overlap(Area a, Area b) {
	Point2D.Float retVal = new Point2D.Float();
	float x = 0f;
	float y = 0f;
	Area tmp = (Area) a.clone();
	tmp.intersect(b);
	if (!tmp.isEmpty()) {

	    Rectangle bound = tmp.getBounds();
	    if (ZLayoutManagerTree.currentOrientation == ZLayoutManagerTree.Orientation_TOPDOWN) {
		// Warning: the clash area bound's width may not
		//  always be the right offset to translate. This is
		//  just a safe (yet potentially slow) solution: to move
		//  the spacing when the width is too big

		x = ((float) bound.getWidth() > ZLayoutManagerTree.currentXSpacing) ?
		    ZLayoutManagerTree.currentXSpacing:
		    (float) bound.getWidth();
	    }
	    else { // left right layout
		y = ((float) bound.getHeight() > ZLayoutManagerTree.currentYSpacing) ?
		    ZLayoutManagerTree.currentYSpacing:
		    (float) bound.getHeight();
	    }
	}
	retVal.setLocation(x, y);
	return retVal;
    }
}

class ZLayoutManagerTreeLink extends ZVisualComponentDecorator {
  /**
   * Pen color of the link
   */
  protected Color penColor  = Color.black;

  protected static int ZLinkStyle = ZLayoutManagerTree.link_STRAIGHTLINE;

  /**
   * Get the value of ZLinkStyle.
   * @return Value of ZLinkStyle.
   */
  public static int getZLinkStyle() {return ZLinkStyle;}

    
  /**
   * Set the value of ZLinkStyle.
   * @param v  Value to assign to ZLinkStyle.
   * @return <code>true</code> if set successfuly
   */
  public static boolean setZLinkStyle(int  v) {
    if (((v == ZLayoutManagerTree.link_STRAIGHTLINE) ||
	 (v == ZLayoutManagerTree.link_ANGLEDLINE))
	&&
	(v != ZLayoutManagerTreeLink.ZLinkStyle)) {
      ZLayoutManagerTreeLink.ZLinkStyle = v;
      return true;
    }
    else {
      return false;
    }
  }


  public static boolean globalLinkDirty = false;
  public static void setGlobalLinkDirty(boolean val) {
    ZLayoutManagerTreeLink.globalLinkDirty = val;
  }

  // The visual link
  Shape visLink = null;

  // Control for link dirty, this one is local
  protected  boolean linkDirty = true;
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
    
    
  public ZLayoutManagerTreeLink(ZVisualComponent vis) {
    super(vis);
    setLinkDirty(true);
  }

    
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
    damage();
  }   

  /**
   * Paints the child, and then update (if needed) the visLink
   * and paint the link.
   *
   * @param <code>g2</code> The graphics context to paint into.
   */
  public void paint(ZRenderContext renderContext) {
    Graphics2D g2 = renderContext.getGraphics2D();
	    
    // First, paint child
    if (child != null) {
      child.paint(renderContext);
		
      updateLink();
      if (visLink != null) {
	g2.setColor(penColor);
	g2.draw(visLink);
      }
    }
  }

  protected void computeLocalBounds() {
    localBounds.reset();
    if (child != null) {

      // filter out selection rect
      ZVisualComponent vis = child;
      while (vis instanceof ZVisualComponentDecorator) {
	vis = ((ZVisualComponentDecorator) vis).getChild();
	if (vis == null) {
	  return;
	}
      }
	    
      Rectangle2D childBounds = vis.getLocalBounds();
      localBounds.add(childBounds);
    }
  }

  /**
   * Update/Create the visLink if needed.
   */
  protected void updateLink() {
    if ((!linkDirty) && (!ZLayoutManagerTreeLink.globalLinkDirty)) {
      return;
    }

    else {
      linkDirty = false;
      setGlobalLinkDirty(false);
	    
      ZNode myparent = (ZNode) findNode();
      Vector allChildren = myparent.getChildren();
	    
      // If no children, no link
      if (allChildren.size() == 0) {
	visLink = null;
	return;
      }

      ZBounds myBound = (ZBounds) myparent.getGlobalCompBounds().clone();
      AffineTransform localTransform;
      try {
	localTransform= myparent.computeGlobalCoordinateFrame().createInverse();
      }
      catch (NoninvertibleTransformException e) {
	System.err.println("In ZLayoutManagerTreeLink Some Transformation problem, should really not happen.");
	localTransform = new AffineTransform();
      }
      // Transform myBound to local coordinates
      myBound.transform(localTransform);
	    		
      ZBounds childBound;
	    
      switch (ZLayoutManagerTreeLink.ZLinkStyle) {
		
      case ZLayoutManagerTree.link_STRAIGHTLINE:
	visLink = new GeneralPath();
		
	// draw links to children only
	for (Iterator i=allChildren.iterator(); i.hasNext() ;) {
	  ZNode child = (ZNode) i.next();
		    
		    
	  // Updating drawing position if moving
	  if (!child.isVisible()) {
	    break;
	  }

	  // Got child's local comp bounds, inverse transform the child's transform to this node's coordinates
	  childBound = (ZBounds) child.getGlobalCompBounds().clone();
	  childBound.transform(localTransform);
		    
	  if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_TOPDOWN) {
	    //Orientation topdown
	    ((GeneralPath) visLink).moveTo((float) myBound.getX() + (float) myBound.getWidth() / 2.0f,
					   (float) myBound.getY() + (float) myBound.getHeight());
	    ((GeneralPath) visLink).lineTo((float) childBound.getX() + (float) childBound.getWidth() / 2.0f,
					   (float) childBound.getY());
	  }
	  else { // if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_LEFTRIGHT)
	    ((GeneralPath) visLink).moveTo((float) myBound.getX() + (float) myBound.getWidth(),
					   (float) myBound.getY() + (float) myBound.getHeight() / 2.0f);
	    ((GeneralPath) visLink).lineTo((float) childBound.getX(),
					   (float) childBound.getY() + (float) childBound.getHeight() / 2.0f);
	  }
	}
	break;
		
      case ZLayoutManagerTree.link_ANGLEDLINE:
	visLink = new GeneralPath();

	ZBounds firstBound = null;
	ZBounds lastBound = null;
	boolean started = false;
	ZNode first = null;
	ZNode last = null;
		
	for (Iterator i = allChildren.iterator() ; i.hasNext();) {
	  ZNode child = (ZNode) i.next();
	  if (!child.isVisible()) {
	    break;
	  }

	  if (!started) {
	    started = true;
	    first = child;
	  }
		    
	  childBound = (ZBounds) child.getGlobalCompBounds().clone();
	  childBound.transform(localTransform);

	  if (first == child) {
	    firstBound = childBound;
	  }
		    
	  // Draw the line up from the child

	  if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_TOPDOWN) {

	    ((GeneralPath) visLink).moveTo((float) childBound.getX() + (float) childBound.getWidth() / 2.0f,
					   (float) childBound.getY());
	    ((GeneralPath) visLink).lineTo((float) childBound.getX() + (float) childBound.getWidth() / 2.0f,
					   (float) childBound.getY() - ZLayoutManagerTree.getCurrentYSpacing() / 2.0f);

	  }
	  else { //if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_LEFTRIGHT)
	    // Half up link
	    ((GeneralPath) visLink).moveTo((float) childBound.getX(),
					   (float) childBound.getY() + (float) childBound.getHeight() / 2.0f);
	    ((GeneralPath) visLink).lineTo((float) childBound.getX() - ZLayoutManagerTree.getCurrentXSpacing() / 2.0f,
					   (float) childBound.getY() + (float) childBound.getHeight() / 2.0f);
	  }

	  last = child;
	  lastBound = childBound;
	} // end for

	if (!started) {
	  visLink = null;
	  break;
	}

	// Draw down linke
	if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_TOPDOWN) {
		    
	  ((GeneralPath) visLink).moveTo((float) myBound.getX() + (float) myBound.getWidth() / 2.0f,
					 (float) myBound.getY() + (float) myBound.getHeight());
	  ((GeneralPath) visLink).lineTo((float) myBound.getX() + (float) myBound.getWidth() / 2.0f,
					 (float) myBound.getY() + (float) myBound.getHeight()
+ ZLayoutManagerTree.getCurrentYSpacing() / 2.0f);

	}
	else { //if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_LEFTRIGHT)
	  // Half up link
	  ((GeneralPath) visLink).moveTo((float) myBound.getX() + (float) myBound.getWidth(),
					 (float) myBound.getY() + (float) myBound.getHeight() / 2.0f);
	  ((GeneralPath) visLink).lineTo((float) myBound.getX() + (float) myBound.getWidth()
+ ZLayoutManagerTree.getCurrentXSpacing() / 2.0f,
(float) myBound.getY() + (float) myBound.getHeight() / 2.0f);
	}

	// Now draw the straight line
	if (first != last) {
	  if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_TOPDOWN) {
	    ((GeneralPath) visLink).moveTo((float) firstBound.getX() + (float) firstBound.getWidth() / 2.0f,
					   (float) firstBound.getY() - ZLayoutManagerTree.getCurrentYSpacing() / 2.0f);
	    ((GeneralPath) visLink).lineTo((float) lastBound.getX() + (float) lastBound.getWidth() / 2.0f,
					   (float) lastBound.getY() - ZLayoutManagerTree.getCurrentYSpacing() /2.0f);
	  }
	  else { //if (ZLayoutManagerTree.getCurrentOrientation() == ZLayoutManagerTree.Orientation_LEFTRIGHT)
	    ((GeneralPath) visLink).moveTo((float) firstBound.getX() - ZLayoutManagerTree.getCurrentXSpacing() / 2.0f,
					   (float) firstBound.getY() + (float) firstBound.getHeight() / 2.0f);
	    ((GeneralPath) visLink).lineTo((float) lastBound.getX() - ZLayoutManagerTree.getCurrentXSpacing() / 2.0f,
					   (float) lastBound.getY() + (float) lastBound.getHeight() / 2.0f);
	  }
	}
		
	break;
      default:
	break;
      }
    }
  }

  // Overwrite the select method, so it will call its child vis' select
  public void select(ZCamera cam) {
    getChild().select(cam);
    if (getChild().isSelected()) {
      selected = true;
    }
    else {
      selected = false;
    }
  }

  public void unselect() {
    child.unselect();
    if (child.isSelected()) {
      selected = true;
    }
    else {
      selected = false;
    }
  }
    
  public boolean pick(Rectangle2D rect) {
    if (child != null) {
      return child.pick(rect);
    } else {
      return false;
    }
  }
} // ZLayoutManagerTreeLink
