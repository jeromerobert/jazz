/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import javax.swing.event.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;

/** 
 * ZNode is the basic hierarchial structure.  It has
 * zero or more child nodes.  The ZNode may also exhibit a visual appearance if it contains
 * a ZVisualComponent in it's visualComponent slot.  In discussion/comments, node is used
 * interchangeably with ZNode.
 * 
 * @author Ben Bederson
 * @see ZVisualComponent
 */
public class ZNode implements ZScenegraphObject, ZSerializable {
				// Default values
    static public final float   alpha_DEFAULT = 1;
    static public final boolean compBoundsDirty_DEFAULT = true;
    static public final boolean globalBoundsDirty_DEFAULT = true;
    static public final boolean visible_DEFAULT = true;
    static public final float   minMag_DEFAULT = 0.0f;
    static public final float   maxMag_DEFAULT = 1000.0f;
    static public final boolean save_DEFAULT = true;

    /**
     * This node's parent
     */
    protected ZNode parent;
    
    /**
     * This node's children
     */
    protected Vector children;

    /**
     * The visual content of this node.  Can never be null (should be the dummy component rather
     * than null).
    */
    protected ZVisualComponent visualComponent;
    
    /**
     * All the cameras explicitly looking onto the scene graph rooted in this node.  Other cameras
     * may actually see this node *indirectly* (some ancestor may have a camera in ITS vector).
     * However, this Vector contains only those cameras that have called addRenderingStartPoint
     * with this node as a param 
     */
    protected transient Vector cameras;
    
    /**
     * The transform applied to this node and by inheritance all of it's children as well.  It
     * modifies the nodes globalBounds and globalCompBounds in the global space
     */
    protected ZTransform transform;	
    
    /**
     * The region (currently a rectangle) in the global space occupied by the intersection of
     * this nodes VC and the global bounds of this nodes children
     */
    protected transient ZBounds globalBounds;
    
    /**
        The region (currently a rectangle) in the global space occupied by the of this nodes VC
    */
    protected transient ZBounds globalCompBounds;
    
    /**
     * The alpha value that will be applied to the composite field (multiplicitivly) of the graphics2D
     * during paint.  All children of this node inherit this nodes alpha
     */
    protected float alpha = alpha_DEFAULT;
    
    /**
     * This field is checked at render time.  If the zoom at the time of render is less than the
     * value of this field, neither this node nor any of its children will be rendered
     */
    protected float minMag = minMag_DEFAULT;
    
    /**
     * This field is checked at render time.  If the zoom at the time of render is greater than
     * the value of this field, neither this node nor any of its children will be rendered
     */
    protected float maxMag = maxMag_DEFAULT;
    
    
    // BDM may want to convert the following booleans to a bit field
    
    /**
     *  True if globalCompBounds is out of date and needs to be recomputed
     */
    protected boolean compBoundsDirty = compBoundsDirty_DEFAULT;
    
    /**
     *  True if globalBounds is out of date and needs to be recomputed
     */
    protected boolean globalBoundsDirty = globalBoundsDirty_DEFAULT;
    
    /**
     *  True if visible (when not visible, nodes are not rendered or picked)
     */
    protected boolean visible = visible_DEFAULT;

    /**
     *  True if this node is specifically set to be volatile
     */
    protected boolean isVolatile = false;

    /**
     *  True if this node should be saved
     */
    protected boolean save = save_DEFAULT;

    /**
     * The layout manager that lays out the children of this node,
     * or null if none.
     */
    protected ZLayoutManager layoutManager = null;

    /**
     * Specifies if this node should be recursively layed out using
     * its parent layout manager if it doesn' have one.
     */
    protected boolean recursiveLayout = true;

    /**
     *  Cached volatility computation
     */
    protected boolean cacheVolatile = false;

    protected Vector clientProperties = null;

    protected ZAncestorNotifier ancestorNotifier = null;

    protected EventListenerList listenerList = null;

    /** 
     * Constructs a new ZNode.  ZNodes start out empty with no children, no VC and no cameras.  A node has
     * no visual content by itself.  Either add a VC to this node or add a child with a non-dummy VC for this
     * node to have visual content.  Also, The node must be attached to a live scenegraph (a scenegraph that is
     * currently visible) or be registered with a camera directly in order for it to be visible.
     */
    public ZNode () {
	this((ZVisualComponent)null);	// don't be fooled by the null, it gets replaced by the dummy VC
					// in the constructor that takes the VC.
    }

    /** 
     * Constructs a new Znode with the specified visual component.  ZNodes instantiated with this constructor
     * start out empty with no children,  and no cameras. The node must be attached to a live scenegraph
     * (a scenegraph that is currently visible) or be registered with a camera directly in order for
     * it to be visible.
     */
    public ZNode (ZVisualComponent vc) {
        children = new Vector();
        cameras = new Vector(); 
	setTransform(new ZTransform());
	globalBounds = new ZBounds();	
	globalCompBounds = new ZBounds();
	if (vc == null) {
	    visualComponent = ZDummyVisualComponent.getInstance();	    
	} else {
	    visualComponent = vc;
	    vc.setParent(this);
	    updateGlobalCompBounds();
	    updateGlobalBounds();
	}
    }

    /**
     * Constructs a new ZNode that is a copy of the specified node (i.e., a "copy constructor").
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any  scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     * The information copied includes the node's visual component, and all children.
     * However, If any cameras were looking at the reference node, they will not look at the new node,
     * and the new node does not have a parent.  In addition, if the associated visual component
     * is selected, then the duplicated visual component will not be selected.
     */
    public ZNode(ZNode node) {
	parent = null;
				// Do a deep copy of children
	children = new Vector();
	ZNode child, childCopy;
	for (Iterator i = node.children.iterator() ; i.hasNext() ; ) {
	    child = (ZNode)i.next();
	    childCopy = (ZNode)child.clone();
	    childCopy.parent = this;
	    children.add(childCopy);
	}
        cameras = new Vector();
	setTransform((ZTransform)node.transform.clone());
	globalBounds = (ZBounds)node.globalBounds.clone();
	globalCompBounds = (ZBounds)node.globalCompBounds.clone();
	alpha = node.alpha;
	minMag = node.minMag;
	maxMag = node.maxMag;
	compBoundsDirty = node.compBoundsDirty;
	globalBoundsDirty = node.globalBoundsDirty;
	visible = node.visible;
	isVolatile = node.isVolatile;
	cacheVolatile = node.cacheVolatile;
	if (node.visualComponent == null) {
	    visualComponent = ZDummyVisualComponent.getInstance();	    
	} else {
				// If original visual component is dummy, then reuse it.
	    if (node.visualComponent instanceof ZDummyVisualComponent) {
		visualComponent = node.visualComponent;
	    } else {
				// Clone the visual component, but if it is selected,
				// don't clone the selection.
		ZVisualComponent vc = node.visualComponent;
		if (vc instanceof ZSelectionDecorator) {
		    vc = ((ZSelectionDecorator)vc).getChild();
		}
		visualComponent = (ZVisualComponent)vc.clone();
		visualComponent.selected = false;
		visualComponent.setParent(this);
	    }
	}
    }

    /**
     * Duplicates the current node by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZNode(ZNode)
     */
    public Object clone() {
	return new ZNode(this);
    }
    

    /**
     * Registers <i>listener</i> so that it will receive ZAncestorEvents
     * when it or any of its ancestors are changed in the following ways:
     * <ul>
     * <li>A node is moved by a transform change
     * <li>A node is added/removed to/from the tree
     * <li>A node is made visible/invisible
     * <li>A node's alpha value is changed
     * <li>A node's min/max magnification is changed
     * </ul>
     * Events are also sent when the component or its ancestors are added
     * or removed from the Component hierarchy
     *
     * @see ZAncestorEvent
     */
    public void addAncestorListener(ZAncestorListener listener) {
        if (ancestorNotifier == null) {
            ancestorNotifier = new ZAncestorNotifier(this);
        }
        ancestorNotifier.addAncestorListener(listener);
    }

    /**
     * Unregisters <i>listener</i> so that it will no longer receive
     * ZAncestorEvents
     *
     * @see #addAncestorListener
     */
    public void removeAncestorListener(ZAncestorListener listener) {
        if (ancestorNotifier == null) {
            return;
        }
        ancestorNotifier.removeAncestorListener(listener);
        if (ancestorNotifier.listenerList.getListenerList().length == 0) {
            ancestorNotifier.removeAllListeners();
            ancestorNotifier = null;
        }
    }

    /**
     * Adds the specified node container listener to receive 
     * node container events from this node.
     *
     * @param l the node container listener
     */ 
    public void addNodeContainerListener(ZNodeContainerListener l) {
	if (listenerList == null) {
	    listenerList = new EventListenerList();
	}
        listenerList.add(ZNodeContainerListener.class, l);
    }

    /**
     * Removes the specified node container listener so that it no longer
     * receives node container events from this node.
     *
     * @param l the node container listener 
     */ 
    public  void removeNodeContainerListener(ZNodeContainerListener l) {
        listenerList.remove(ZNodeContainerListener.class, l);
	if (listenerList.getListenerCount() == 0) {
	    listenerList = null;
	}
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.  The listener list is processed in last to
     * first order.
     * @param id The event id (NODE_ADDED, NODE_REMOVED)
     * @param child The child being added or removed from this node
     * @see EventListenerList
     */
    protected void fireNodeContainerEvent(int id, ZNode child) {
	if (listenerList == null) {
	    return;
	}

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ZNodeContainerEvent e = new ZNodeContainerEvent(this, id, child);

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ZNodeContainerListener.class) {
		switch (id) {
		case ZNodeContainerEvent.NODE_ADDED:
		    ((ZNodeContainerListener)listeners[i+1]).nodeAdded(e);
		    break;
		case ZNodeContainerEvent.NODE_REMOVED:
		    ((ZNodeContainerListener)listeners[i+1]).nodeRemoved(e);
		    break;
		}
            }          
        }
    }

    /**
     * Adds the specified node listener to receive 
     * node events from this node.
     *
     * @param l the node listener
     */ 
    public void addNodeListener(ZNodeListener l) {
	if (listenerList == null) {
	    listenerList = new EventListenerList();
	}
        listenerList.add(ZNodeListener.class, l);
    }

    /**
     * Removes the specified node listener so that it no longer
     * receives node events from this node.
     *
     * @param l the node listener 
     */ 
    public  void removeNodeListener(ZNodeListener l) {
        listenerList.remove(ZNodeListener.class, l);
	if (listenerList.getListenerCount() == 0) {
	    listenerList = null;
	}
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.  The listener list is processed in last to
     * first order.
     * @param id The event id (NODE_TRANSFORMED, NODE_SHOWN, or NODE_HIDDEN)
     * @param origTransform The original transform (for transform events)
     * @see EventListenerList
     */
    protected void fireNodeEvent(int id, AffineTransform origTransform) {
	if (listenerList == null) {
	    return;
	}

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ZNodeEvent e = new ZNodeEvent(this, id, origTransform);

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ZNodeListener.class) {
		switch (id) {
		case ZNodeEvent.NODE_TRANSFORMED:
		    ((ZNodeListener)listeners[i+1]).nodeTransformed(e);
		    break;
		case ZNodeEvent.NODE_SHOWN:
		    ((ZNodeListener)listeners[i+1]).nodeShown(e);
		    break;
		case ZNodeEvent.NODE_HIDDEN:
		    ((ZNodeListener)listeners[i+1]).nodeHidden(e);
		    break;
		}
            }          
        }
    }

    /**
     * Specifies the layout manager for this node.
     * @param manager The new layout manager.
     */
    public void setLayoutManager(ZLayoutManager manager) {
	layoutManager = manager;
    }

    /**
     * Returns the current layout manager for this node.
     * If this node specifies that it desires recursive layout, and
     * it does not have its own layout manager, then it returns its
     * parent's layout manager.
     * @return The current layout manager.
     */
    public ZLayoutManager getLayoutManager() {
	ZLayoutManager manager = layoutManager;

	if ((layoutManager == null) && recursiveLayout && (getParent() != null)) {
	    manager = getParent().getLayoutManager();
	}

	return manager;
    }

    /**
     * Specify if this node should be laid out recursively
     * using its parent's layout manager if it doesn't have one.
     * @param recursive True to layout recursively.
     */
    public void setRecursiveLayout(boolean recursive) {
	recursiveLayout = recursive;
    }

    /**
     * Determine if this node is laid out recursively using
     * its parent's layout manager if this doesn't have one.
     * @return true if this node is laid out recursively.
     */
    public boolean getRecursiveLayout() {
	return recursiveLayout;
    }

    /** 
     * Causes the subtree rooted at this node to be laid out.
     * This works by doing a depth-first search on the tree,
     * laying out children first, and then the node (recursively).
     * If there is no layout manager for this node, then nothing happens.
     * @see #getLayoutManager
     */
    public void doLayout() {
				// First, recursively apply layout to children
	ZNode child;
	for (Iterator i=children.iterator(); i.hasNext();) {
	    child = (ZNode)i.next();
	    child.doLayout();
	}

				// Then, apply layout to self
	ZLayoutManager manager = getLayoutManager();
	if (manager != null) {
	    manager.doLayout(this);
	}
    }

    /** 
     * Adds a child to the node.  This method will automatically damage the global space.  However,
     * the application will need to call restore on any surfaces that see this node before the
     * visual content will be updated.
     * @param child is the child that gets added to this.
     * @see ZSurface#restore()
     */
    public void addChild(ZNode child) {
				// Remove child from previous parent if there was one
	if (child.getParent() != null) {
	    child.getParent().removeChild(child);
	}
	
				// Now, add it to this
	try {
	    child.setParent(this);
	} catch (Exception e) {
	    System.out.println("ZNode.addChild: " + e);
	}
	children.add(child);
	child.updateBounds();
	child.damage();

	fireNodeContainerEvent(ZNodeContainerEvent.NODE_ADDED, child);
    }

    /** 
     * Removes a child from the node.  This method will automatically damage the global space.  However,
     * the application will need to call restore on any surfaces that see this node before the
     * visual content will be updated.
     * @param child is the child that is removed.
     * @see ZSurface#restore()
     */
    public void removeChild(ZNode child) {
        ZCamera camera;

				// First, make sure area that child was gets updated
				// And if any cameras point to this child, then stop that.
	child.damage();
	while (!child.cameras.isEmpty()) {
	    camera = (ZCamera)child.cameras.firstElement();
	    camera.removePaintStartPoint(child);
	}
	
				// Finally, remove child and recompute bounds
	try {
	    child.setParent(null);
	} catch (Exception e) {
	    System.out.println(e);
	}
	children.removeElement(child);
	updateBounds();

	fireNodeContainerEvent(ZNodeContainerEvent.NODE_REMOVED, child);
    }
    
    /**
     * Return the node's children.
     */
    public Vector getChildren() {
	return children;
    }

    /**
     * Move this node in the hierarchy, and update its transform so that 
     * its global position does not change.  This node will be removed
     * from its current parent, and added as a child to the new parent.
     * Its transform will be changed so that its global hierarchical transform
     * is the same under the new parent as the under the old parent.
     *
     * @param newParent the node that this node will be put under
     */
    public void reparent(ZNode newParent) {
	AffineTransform origAT = computeGlobalCoordinateFrame();
	AffineTransform newParentAT = newParent.computeGlobalCoordinateFrame();
	AffineTransform newAT = null;
	
	try {
	    newAT = newParentAT.createInverse();
	} catch (NoninvertibleTransformException exc) {
	    System.out.println("ZNode.reparent: Can't invert transform");
	    return;
	}
	newAT.concatenate(origAT);
	
	newParent.addChild(this);
	getTransform().setAffineTransform(newAT);
    }

    /**
     * Swaps this node out of the scenegraph tree, and replaces it with the specified
     * replacement node.  This node is left dangling, and it is up to the caller to
     * manage it.  The replacement node will be added to this's parent in the same
     * position as this was.  That is, if this was the 3rd child of its parent, then
     * after calling swap(), the replacement node will also be the 3rd child of its parent.
     * If this node has no parent when swap is called, then nothing will be done at all.
     * <p>
     * If this was a paint start point for any cameras, then it will be removed from
     * those camera lists, and the replacement will be added to those cameras in the
     * same position as this node was.
     *
     * @param replacement the new node that replaces the current node in the scenegraph tree.
     */
    public void swap(ZNode replacement) {
	ZNode parent = getParent();
	ZCamera camera;

	if (parent != null) {
	    damage();		// Need to damage old bounds of node.

				// First, find any cameras that point to this, and
				// swap the camera's paint start point from this to the replacement.
	    while (!cameras.isEmpty()) {
		camera = (ZCamera)cameras.firstElement();
		camera.swapPaintStartPoint(this, replacement);
	    }
				// Then, find out the position of this node in its parent child list,
				// and swap it with its replacement
	    Vector cousins = parent.getChildren();
	    for (int i=0; i<cousins.size(); i++) {
		if (cousins.get(i) == this) {
		    try {
			setParent(null);
		    } catch (ZOperationNotAllowedException e) {
			System.out.println("ZNode.swap: Internal Error A: " + e);
		    }
		    cousins.setElementAt(replacement, i);
		    try {
			replacement.setParent(parent);
		    } catch (ZOperationNotAllowedException e) {
			System.out.println("ZNode.swap: Internal Error B: " + e);
		    }
		    replacement.updateGlobalBounds();
		    replacement.damage();
		    break;
		}
	    }
	}
    }

    /** 
     * Raises this node within the drawing order of its siblings,
     * so it gets rendered above (after) all of its siblings.
     * This is done by moving this node to the end of its parent's child list.
     */
    public void raise() {
	ZNode parent = getParent();
	Vector children = parent.getChildren();
	if (parent != null) {
	    boolean exists = children.removeElement(this);
	    if (exists) {
		children.addElement(this);
		damage();
	    }
	}
    }

    /** 
     * Raises this node within the drawing order of its siblings,
     * so it gets rendered above (after) the specified node.
     * This is done by moving this node just after the specified node in its parent's child list.
     * If the specified node is not a sibling of this, then this call does nothing.
     * <p>
     * If the specified node is null, then this node is raised to be the
     * last node rendered of its siblings (i.e., equivalent to calling {link #raise}
     * 
     * @param afterNode The node to raise this node after.
     */
     public void raiseTo(ZNode afterNode) {
	 if (afterNode == null) {
	     raise();
	 } else {
	     ZNode parent = getParent();
	     Vector children = parent.getChildren();
	     if ((parent != null) && (afterNode.getParent() == parent)) {
		 boolean exists = children.removeElement(this);
		 if (exists) {
		     int index = children.indexOf(afterNode) + 1;
		     children.insertElementAt(this, index);
		     damage();
		 }
	     }
	 }
    }

    /** 
     * Lowers this node within the drawing order of its siblings,
     * so it gets rendered below (before) all of its siblings.
     * This is done by moving this node to the beginning of its parent's child list.
     */
    public void lower() {
	ZNode parent = getParent();
	Vector children = parent.getChildren();
	if (parent != null) {
	    boolean exists = children.removeElement(this);
	    if (exists) {
		children.insertElementAt(this, 0);
		damage();
	    }
	}
    }

    /** 
     * Lowers this node within the drawing order of its siblings,
     * so it gets rendered below (before) the specified node.
     * This is done by moving this node just before the specified node in its parent's child list.
     * If the specified node is not a sibling of this, then this call does nothing.
     * <p>
     * If the specified node is null, then this node is lowered to be the
     * first node rendered of its siblings (i.e., equivalent to calling {link #lower}
     * 
     * @param beforeNode The node to lower this node before.
     */
     public void lowerTo(ZNode beforeNode) {
	 if (beforeNode == null) {
	     lower();
	 } else {
	     ZNode parent = getParent();
	     Vector children = parent.getChildren();
	     if ((parent != null) && (beforeNode.getParent() == parent)) {
		 boolean exists = children.removeElement(this);
		 if (exists) {
		     int index = children.indexOf(beforeNode);
		     children.insertElementAt(this, index);
		     damage();
		 }
	     }
	 }
    }

    /**
     * Returns this node's list of cameras.  That is, this returns the
     * list of cameras for which this node is one of those cameras
     * "paintStartPoints".  <br>
     * There are three methods for getting a list of camera's that can see
     * the portion of the scenegraph containing a node.  This method getCameras() returns
     * only those cameras that have registered the node as a paintStartPoint.  The second
     * method findCameras() contains all the cameras that getCameras() would return plus all
     * of the cameras returned by calling getCameras() on the ancestors of the node.  The
     * last method findAllCamaras, returns all the cameras found with findCameras() plus the
     * the cameras that can be found by ascending up the ancestors of all the cameras (remember
     * that cameras are also nodes)
     *
     * @return the list of cameras.
     * @see #findCameras()
     * @see #findAllCameras()
     */
    public Vector getCameras() {
	return cameras;
    }
    
    /** 
     * Returns a list of cameras that look directly at this node or any of it's parents.
     * Note that this does not check if this node is actually visible within the
     * cameras' view, just that the camera renders the portion
     * of the scenegraph that includes this node.
     *
     * @return the list of cameras.
     * @see #getCameras()
     */
    public Vector findCameras() {
	Vector result;
	
				// This is implemented carefully so that a new Vector
				// is only allocated if necessary.  If there are either
				// cameras only on this node, or only on its ancestors,
				// then we just reuse those lists.
	if (parent != null) {
	    Vector parentCameras = parent.findCameras();
	    if (parentCameras.isEmpty()) {
		result = cameras;
	    } else {
		if (cameras.isEmpty()) {
		    result = parentCameras;
		} else {
		    result = new Vector();
		    result.addAll(cameras);
		    result.addAll(parentCameras);
		}
	    }
	} else {
	    result = cameras;
	}

	return result;
    }

    /** 
     * Returns a list of all the cameras that look on to this
     * node.  This performs a recursive search on the cameras
     * so even cameras that see a camera that see a camera
     * that see this node are included.  Note that this does
     * not check if this node is actually visible within the
     * cameras' view, just that the camera renders the portion
     * of the scenegraph that includes this node.
     *
     * @return the list of cameras.
     * @see #getCameras()
     */
    public Vector findAllCameras() {
	Vector result = new Vector();
	helpFindAllCameras(result);
	return result;
    }

    protected void helpFindAllCameras(Vector result) {
	ZCamera camera;
	for (Iterator i = findCameras().iterator() ; i.hasNext() ; ) {
	    camera = (ZCamera)i.next();
	    result.add(camera);
	    camera.helpFindAllCameras(result);
	}
    }
    
    //****************************************************************************
    //
    //			Get/Set and Add/Remove pairs
    //
    //***************************************************************************

    /**
     * Determines if this node is volatile.
     * A node is considered to be volatile if it is specifically set
     * to be volatile with {@link #setVolatile}, or if its visual component or any of its descendants are volatile.
     * <p>
     * Volatile objects are those objects that change regularly, such as an object
     * that is animated, or one whose rendering depends on its context.  For instance,
     * a selection marker {@link ZSelectionDecorator} is always one-pixel thick, and thus its bounds
     * depend on the current magnification.
     * @return true if this node is volatile
     * @see #setVolatile(boolean)
     */
    public boolean isVolatile() {
	return cacheVolatile;
    }

    /**
     * Specifies that this node is volatile.
     * Note that this node is considered to be volatile if its visual component or any of its
     * descendants are volatile, even if this node's volatility is set to false.
     * This implies that all parents of this node are also volatile when this is volatile.
     * @param v the new specification of whether this node is volatile.
     * @see #isVolatile()
     */
    public void setVolatile(boolean v) {
	isVolatile = v;
	updateVolatility();
    }
    
    /**
     * Internal method to compute and cache the volatility of a node,
     * to recursively call the parents to compute volatility.
     * A node is considered to be volatile if it is set to be volatile,
     * or its visual component or any of its descendants are volatile.
     * @see #setVolatile(boolean)
     * @see #isVolatile()
     */
    public void updateVolatility() {
	ZNode child;
				// If this node set to volatile, then it is volatile
	cacheVolatile = isVolatile;
	if (!cacheVolatile) {
				// Else, if its visual component is volatile, then it is volatile
	    ZVisualComponent vc = getVisualComponent();
	    cacheVolatile = vc.isVolatile();
	    if (!cacheVolatile) {
				// Else, if any of its children are volatile, then it is volatile
		for (Iterator i=children.iterator(); i.hasNext();) {
		    child = (ZNode)i.next();
		    if (child.isVolatile()) {
			cacheVolatile = true;
			break;
		    }
		}
	    }
	}
				// Now that this is up-to-date, update parent
	if (parent != null) {
	    parent.updateVolatility();
	}
    }
    
    /**
     * Get the alpha value (transparency) for this node.  Alpha values are applied
     * multiplicitivly with the alpha values of ancestors.  That is, alpha is an
     * inherited attribute.
     * @return Value of alpha.
     */
    public float getAlpha() {return alpha;}

    /**
     * set the alpha value (transparency) for this node.  Alpha values are applied
     * multiplicitivly with the alpha values of ancestors.  That is, alpha is an
     * inherited attribute.
     */
    public void setAlpha(float alpha) {
	if (this.alpha != alpha) {
	    this.alpha = alpha;
	    damage();
	}
    }

    /**
     * Get the value of 'visible'.  Nodes with the visible flag set to false are not painted
     * or displayed.  Note that invisible nodes do not contribute to the bounds of their
     * parents.
     * Same as isVisible().
     * @return Value of hidden.
     * @see #isVisible()
     */
    public boolean getVisible() {
	return visible;
    }

    /**
     * Get the value of 'visible'.  Nodes with the visible flag set to false are not painted
     * or displayed.  Note that invisible nodes do not contribute to the bounds of their
     * parents.
     * Same as getVisible().
     * @return Value of hidden.
     * @see #getVisible()
     */
    public boolean isVisible() {
	return visible;
    }
    
    /**
     * Set the value of 'visible'.  Nodes with the visible flag set to false are not painted
     * or displayed.  Note that invisible nodes do not contribute to the bounds of their
     * parents.
     * @param v Value of visible.
     */
    public void setVisible(boolean v) {
	visible = v;
	updateBounds();
	damage();

	if (visible) {
	    fireNodeEvent(ZNodeEvent.NODE_SHOWN, null);
	} else {
	    fireNodeEvent(ZNodeEvent.NODE_HIDDEN, null);
	}
    }    
    
    /**
     * Determine if this node gets saved when written out.
     * @return true if this node gets saved.
     */
    public boolean getSave() {
	return save;
    }

    /**
     * Specify if this node should be saved.  If not, then all references to this
     * will be skipped in saved files.
     * @param s true if node should be saved
     */
    public void setSave(boolean s) {
	save = s;
    }    
    
    /** 
     * Get the nodes transform.
     */
    public ZTransform getTransform() {return transform;}
    
    /** 
     * Set the nodes transform.
     */
    public void setTransform(ZTransform v) {
	AffineTransform origTransform = null;
	if (transform != null) {
	    transform.getAffineTransform();
	}
	
	transform = v;
	transform.setParent(this);

	transformChanged(origTransform);
    }


    /**
     * Internal method used to notify a node that its transform has changed.
     * @param origTransform The value of the original transform (before it was changed)
     */
    public void transformChanged(AffineTransform origTransform) {
				// This method is used to capture when the transform
				// changes so it can fire a transform change event.
	fireNodeEvent(ZNodeEvent.NODE_TRANSFORMED, origTransform);
    }

    /** 
     * Returns the visual component.  Remember that the visual component is quite
     * likely not a single object, but a chain of objects.  Often you will need
     * to descend the chain looking for a particular type of visual component.
     * Regardless, always check the type of the visual component before performing
     * an operation on it.
     */
    public ZVisualComponent getVisualComponent() {return visualComponent;}
    
    /** 
     * Set the visual component.  Remember that the visual component is quite
     * likely not a single object, but a chain of objects.  Often you will need
     * to descend the chain looking for a particular type of visual component.
     * Regardless, always check the type of the visual component before performing
     * an operation on it.  <br>
     * Calling this method will damage the space accordingly.  Be sure to call restore()
     * on any applicable surfaces.  The visual content of the node on the surface
     * will not be updated until a call to restore() is made.
     * @param vc The new visual component
     */
    public void setVisualComponent(ZVisualComponent vc) {
	damage();
	if (vc == null) {
	    clearVisualComponent();
	    visualComponent = ZDummyVisualComponent.getInstance();	    
	} else {
	    visualComponent = vc;
	    vc.setParent(this);
	    updateGlobalCompBounds();
	    updateGlobalBounds();
	}
	damage();
    }
    
    /** 
     * Removes a child from the node.
     * Damage causes each camera to be updated so they can maintain their data structures.
     */
    protected void clearVisualComponent() {
        if (visualComponent != null ) {
	    ZVisualComponent vc = visualComponent;
	    if (visualComponent.isSelected()) {
		visualComponent.unselect();
	    }

	    for (Iterator i=findCameras().iterator(); i.hasNext();) {
		ZCamera camera = (ZCamera)i.next();
		camera.damage(this.getGlobalBounds());
	    }

	    visualComponent.setParent(null);
	    visualComponent = ZDummyVisualComponent.getInstance();;
	    
	    updateGlobalCompBounds();
	    updateGlobalBounds();

	    for (Iterator i=findCameras().iterator(); i.hasNext();) {
		ZCamera camera = (ZCamera)i.next();
		camera.damage(this.getGlobalBounds());
	    }
	}
    }

    /** 
     * Used internally to set a node's parent.
     * @param parent 
     */
    protected void setParent(ZNode parent) throws ZOperationNotAllowedException {
	this.parent = parent;
    } 

    /** 
     * Get the  node's parent.  Note that this method has no associated public
     * setParent method (parent.add(thisNode) is used instead)
     * @see #addChild(ZNode)
     */
    public ZNode getParent() {return parent;}

    /** 
     * Adds the camera to the list of cameras that this node is visible within.
     * If camera is already listed, it will not be added again.
     * 
     * @param camera The camera this node should be visible within
     */
    void addCamera(ZCamera camera) {
	if (!cameras.contains(camera)) {
	    cameras.add(camera);
	}
    }

    /** 
     * Removes camera from the list of cameras that this node is visible within.
     * 
     * @param camera The camera this node is no longer visible within
     */
    void removeCamera(ZCamera camera) {
	cameras.remove(camera);
    }
    
    /** 
     * Get the minimumn magnification for this node.  Magnification is a culling
     * mechanism.  During paint, the current magnification (from the render context)
     * is compared to the min magnification.  If the current magnification is less
     * than min magnification.  Neither this node, nor any of its children will be
     * rendered.  
     * @return The minimum magnification of this node
     */
    public float getMinMag() {
	return minMag;
    }

    /** 
     * set the minimumn magnification for this node.  Magnification is a culling
     * mechanism.  During paint, the current magnification (from the render context)
     * is compared to the min magnification.  If the current magnification is less
     * than min magnification.  Neither this node, nor any of its children will be
     * rendered.
     * 
     * @param v The new minimumn magnification for this node.
     */
    public void setMinMag(float v) {
	minMag = v;
	damage();
    }

    /** 
     * Get the maximum magnification for this node.  Magnification is a culling
     * mechanism.  During paint, the current magnification (from the render context)
     * is compared to the max magnification.  If the current magnification is greater
     * than max magnification.  Neither this node, nor any of its children will be
     * rendered.  If the maximum magnification is set to the special value of -1,
     * then this feature is disabled (i.e., it never is culled because it is too large).
     * @return The maximum magnification of this node
     */
    public float getMaxMag() {
	return maxMag;
    }

    /** 
     * Set the maximum magnification for this node.  Magnification is a culling
     * mechanism.  During paint, the current magnification (from the render context)
     * is compared to the max magnification.  If the current magnification is greater
     * than max magnification.  Neither this node, nor any of its children will be
     * rendered. If the maximum magnification is set to the special value of -1,
     * then this feature is disabled (i.e., it never is culled because it is too large).
     * 
     * @param v The new maximum magnification for this node.
     */
    public void setMaxMag(float  v) {
	maxMag = v;
	damage();
    }
   
   
    //****************************************************************************
    //
    //			Region Mgmt and displaying
    //
    //***************************************************************************

    /**
     * Return a vector containing all of the nodes in the subgraph rooted at this node
     * (including this node) that are currently selected.
     */
    public Vector getSelectedChildren() {
	ZNode child;
	Vector result = new Vector();

	if (visualComponent.isSelected()) {
	    result.add(this);
	}
	
	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();

	    result.addAll(child.getSelectedChildren());
	}
	
	return result;
    }

    protected Composite createComposite(Composite currentComposite, float currentMag) {
	float newAlpha = alpha;
	float maxMagFadeStart;
	float minMagFadeStart;
	
	// Assume that there is NO overlap between min and max mag fading...
	// beware the assignment inside the first clause of both if conditions
	if ((maxMag >= 0) && (currentMag > (maxMagFadeStart = maxMag * .7f))) {
	    
	    float fade = (maxMag - currentMag)  / (maxMag - maxMagFadeStart);
	    newAlpha *= fade;
	    
	} else if ((currentMag < (minMagFadeStart = minMag * 1.3f))) {
	    
	    float fade = (currentMag - minMag)  / (minMagFadeStart - minMag);
	    newAlpha *= fade;
	    
	}  
	
	if ((currentComposite != null) &&
	    (currentComposite instanceof AlphaComposite)) {

	    newAlpha *= ((AlphaComposite)currentComposite).getAlpha();
	}

	if (newAlpha == 1.0f) {
	    return currentComposite;
	} else {
	    return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, newAlpha);
	}
	
	
    }
    
     /**
     * Paints the associated visual component and children of this node.
     * Nothing is painted if the node is invisible or is magnified more
     * than its maximum or less than its minimum magnification.
     * Before anything is rendered, the node's transformation and transparency
     * is applied.
     * <p>
     * The node makes the guarantee that before anything is rendered,
     * the transform, clip, and composite of the Graphics2D will be set properly.
     * However, the color, font, and stroke are unset, and the visual component
     * must set those things as needed.  The visual components that are painted
     * are not obligated to restore any aspect of the Graphics2D state.
     * @param renderContext The graphics context to use for rendering.
     * @see ZVisualComponent#paint(ZRenderContext)
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D      g2 = renderContext.getGraphics2D();
	ZArea           visibleArea = renderContext.getVisibleArea();
	AffineTransform saveTransform = g2.getTransform();
	Composite       saveComposite = g2.getComposite();
	Shape           saveClip = g2.getClip();
	float           currentMag = renderContext.getCameraMagnification();

				// Don't paint nodes that are invisible, too big or too small
	if (!visible ||
	    (currentMag < minMag) ||
	    ((maxMag >= 0) && (currentMag > maxMag))) {
	    
	    return;
	}
	
	if (!(visualComponent instanceof ZDummyVisualComponent)) {
	    ZDebug.incPaintCount(g2);
	}

	g2.transform(transform.getAffineTransform());
	g2.setComposite(createComposite(saveComposite, currentMag));

				// Paint visual component
	visualComponent.paint(renderContext);
	
				// Paint children
	ZNode child;
	ZBounds childBounds;
	for (Iterator i=children.iterator(); i.hasNext();) {
	    child = (ZNode)i.next();
	    childBounds = child.getGlobalBounds();
	    if (visibleArea.intersects(childBounds)) {
		child.paint(renderContext);
	    }
	}

				// Draw component bounding box if requested for debugging
	if (ZDebug.getShowBounds()) {
	    g2.setColor(new Color(60, 60, 60));
	    g2.setStroke(new BasicStroke(1.0f / renderContext.getCompositeMagnification(), 
					 BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
	    g2.draw(getVisualComponent().getLocalBounds());
	}

				// Restore graphics state
	g2.setComposite(saveComposite);
	g2.setTransform(saveTransform);
	g2.setClip(saveClip);
    }
    
    /**
     * Returns the first object under the specified rectangle (if there is one)
     * as searched in reverse (front-to-back) order. Only returns nodes
     * with "pickable" visual components.  The magnification of the camera being
     * picked within is specified, and objects which larger than their maximum magnification,
     * or smaller than minimum magnification are skipped.
     * @param rect Coordinates of pick rectangle in camera coordinates.
     * @param mag The magnification of the camera being picked within.
     * @return The picked object, or null if none
     * @see ZVisualComponent#isPickable()
     */
    public ZNode pick(Rectangle2D rect, float mag) {
				// If node is invisible, don't pick it
	if (!visible ||
	    (mag < minMag) ||
	    ((maxMag >= 0) && (mag > maxMag))) {
	    return null;
	}

				// BBB: We really should only continue if rect is in this node's bounds,
				//      but the rect is in the node's coord frame, and we don't have the node's
				//      bounds in its local coord frame.

	ZNode pickedNode;
	Rectangle2D transformedRect = new Rectangle2D.Float();
	int numChildren = children.size();
	ZNode child;
	
	for (int i=numChildren; i>0; i--) {
	    child = (ZNode)children.elementAt(i - 1);
	    child.getTransform().inverseTransform(rect, transformedRect);
	    pickedNode = child.pick(transformedRect, mag);

	    if (pickedNode != null) {
		return pickedNode;
	    }
	}

	ZVisualComponent vc = getVisualComponent();
	if (vc.isPickable() &&
	    vc.pick(rect)) {
	    return this;
	}
	
	return null;
    }
   
    /**
     * Return the list of nodes that are accepted by the specified filter in the
     * subtree rooted with this.  If this node is invisible or it is not "findable", then neither
     * this node, nor any of its descendants will be included.
     * The filter specifies whether or not this node should be accepted by the
     * search, and whether the node's children should be searched.
     * @param filter The filter that decides whether or not to include individual nodes in the find list
     * @param nodes the accumulation vector (results will be place here).
     * @return the number of nodes searched
     * @see ZVisualComponent#isFindable()
     * @see ZFindFilter
     */
    public int findNodes(ZFindFilter filter, Vector nodes) {
	int nodesSearched = 1;
	ZNode child;

				// Go no further if this node is invisible or not findable
	if (!visible || (!getVisualComponent().isFindable())) {
	    return nodesSearched;
	}

				// Check if this node is accepted by the filter
	if (filter.accept(this)) {
	    nodes.add(this);
	}

				// Check node's children
	if (filter.findChildren(this)) {
	    for (Iterator i=children.iterator(); i.hasNext();) {
		child = (ZNode)i.next();
		nodesSearched += child.findNodes(filter, nodes);
	    }
	}

	return nodesSearched;
    }
   
    /**
     * A utility function that supports finding the node that references a particular visual component.
     * @return this node
     * @see ZVisualComponent#findNode
     */
    public ZNode findNode() {
	return this;
    }

    /**
     * Traverse the tree, find the root node, and return it.
     * @return The root node of this scenegraph
     * @see ZRootNode
     */
    public ZRootNode findRoot() {
	if (parent == null) {
	    return null;
	} else {
	    return parent.findRoot();
	}
    }

    /**
     * Traverse the list of visual components, and return the
     * first one that is of the specified Class, or null if none.
     * @param type the object class to look for.
     * @return The first visual component encountered of <i> type</i>
     */
    public ZVisualComponent findVisualComponent(Class type) {
	return getVisualComponent().findVisualComponent(type);
    }

    /** 
     * Damage causes the portions of the surfaces that this object appears in to
     * be marked as needing to be repainted.  The repainting does not actually 
     * occur until ZSurface.restore() is called.
     * <p>
     * If this object is visible in multiple places on a surface because the
     * surface has more than one camera where each camera can see this object,
     * then this call to damage causes all of those places to be marked
     * as needing to be repainted.  A single call to that surface's restore
     * method will cause all of those damaged areas to be repainted.
     * <p>
     * If this object is visible within multiple surfaces, then this call to damage
     * causes those places that this object is visible on each surface to be
     * marked as needing to be repainted.  The restore method of each surface must 
     * be called in order to repaint the damaged area associated with this object.
     * <p>
     * Important note : There are two proper uses of damage.
     * <ol>
     * <li>When the bounds of the object do not change, you can simply call this damage method
     * <li>When making a change to an object that affects it's bounds
     * in any way (change of penWidth, selection, transform, etc.) you must call
     * {@link #damage(boolean)}.
     * </ol>
     *
     * @see ZSurface#restore()
     * @see #damage(boolean)
     */
    public void damage() {
	ZBounds bounds = getGlobalBounds();

	if (ZDebug.getDebug() == ZDebug.DEBUG_DAMAGE) {
	    System.out.println("ZNode.damage: this = " + this + ", bounds = " + getGlobalBounds());
	}
				// If debugging bounds is enabled, we must increase the
				// damage area just a bit to accomodate the increased area being painted.
	if (ZDebug.getShowBounds()) {
	    float s = 1.0f;
	    if (ZDebug.getBoundsCamera() != null) {
		AffineTransform tx = ZDebug.getBoundsCamera().getViewTransform().getAffineTransform();
		s /= (float)tx.getScaleX();
	    }

	    bounds = new ZBounds(getGlobalBounds());
	    bounds.setRect(bounds.getX() - s, bounds.getY() - s, bounds.getWidth() + 2*s, bounds.getHeight() + 2*s);
	}
	    
	for (Iterator i=findCameras().iterator(); i.hasNext();) {
	    ZCamera camera = (ZCamera)i.next();
	    camera.damage(bounds);
	}
    }
    
    /** 
     * Damage causes the portions of the surfaces that this object appears in to
     * be marked as needing to be repainted.  The repainting does not actually 
     * occur until ZSurface.restore() is called.
     * <p>
     * If this object is visible in multiple places on a surface because the
     * surface has more than one camera where each camera can see this object,
     * then this call to damage causes all of those places to be marked
     * as needing to be repainted.  A single call to that surface's restore
     * method will cause all of those damaged areas to be repainted.
     * <p>
     * If this object is visible within multiple surfaces, then this call to damage
     * causes those places that this object is visible on each surface to be
     * marked as needing to be repainted.  The restore method of each surface must 
     * be called in order to repaint the damaged area associated with this object.
     * <p>
     * Important note : There are two proper uses of damage.
     * <ol>
     * <li>When the bounds of the object do not change, you should call 
     * {@link #damage}.
     * <li>When making a change to an object that affects it's bounds
     * in any way (change of penWidth, selection, transform, etc.) you must call
     * this damage method.
     * </ol>
     *
     * @see ZSurface#restore()
     * @see #damage
     */
    public void damage(boolean boundsChanged) {
				// An alternative to using this method is to do the following 4 step sequence:
				//   * node.damage()        - damage original untouched node
				//   * node...              - modify node
				//   * node.updatebounds()  - Recompute bounds to reflect on modification
				//   * node.damage()        - damage modified bounds
				// This approach is actually a bit safer, but calling this method
				// should do the same thing in just about all cases.

				// We first must damage the original cached bounds,
				// so we use the 'globalBounds' variable directly, rather
				// then calling getGlobalBounds() which could result in the
				// new bounds being computed if the object is volatile, or out of date.
	for (Iterator i=findCameras().iterator(); i.hasNext();) {
	    ZCamera camera = (ZCamera)i.next();
	    camera.damage(globalBounds);
	}

	if (boundsChanged) {
				// Then, if the bounds have been changed, we recompute the bounds,
				// and damage the area of the new bounds.
	    updateBounds();
	    damage();
	}
    }


    /**
     * Implementation of inherited interface method.  This method will propagate the request to
     * updateChildBounds to all it's children and update its own bounds as well.<br>
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     * @see ZScenegraphObject#updateChildBounds     
     */
    public void updateChildBounds() {
	ZNode child;
	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    child.updateChildBounds();
	}
	updateBounds();
    }
    
    /**
     * Request this node to recompute its bounds.  This will result in propagating the
     * request to its parent and update its own (both types) bounds as well.
     * <p>
     * This method invalidates the cached values for both the globalBounds and globalCompBounds.
     * GlobalCompBounds consists of the bounding rectangle containing this nodes visual components.
     * GlobalBounds consists of the bounding rectangle containing the globalCompBounds and
     * all of this nodes descendents globalBounds.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     * @see ZScenegraphObject#updateBounds     
     * @see ZNode#updateGlobalCompBounds     
     * @see ZNode#updateGlobalBounds     
     */
    public void updateBounds() {
	updateGlobalCompBounds();
	updateGlobalBounds();
	if (parent != null) {
	    parent.updateBounds();
	}
    }
    
    /**
     * Notifies the node that the cached bounds for its visual component are no longer valid.  Used when
     * the visual component changes in some way that changes its bounds in the space.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     */
    public void updateGlobalCompBounds() {
	compBoundsDirty = true;
	ZNode p = getParent();
	if (p != null) {
	    p.updateGlobalBounds();
	}
    }
    
   /**
     * Recomputes and caches the bounds for the nodes visual component.  Generally this method is
     * called by getGlobalCompBounds when the dirty bit is set.  Should rarely be called from
     * application code.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     */
    protected void computeGlobalCompBounds() {
	compBoundsDirty = false;
	
	ZBounds vcBounds = visualComponent.getLocalBounds();

	globalCompBounds.reset();
	globalCompBounds.add(vcBounds);
	AffineTransform globalFrame = computeGlobalCoordinateFrame();
	globalCompBounds.transform(globalFrame);
    }
    
    /**
     * Returns the bounds (in global coords) for this nodes visual component.  If a valid cached value
     * is available, this method returns it.  If a valid cache is not available (i.e. the dirty bit is
     * set) then the bounds are recomputed, cached and then returned to the caller.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     * @see ZNode#updateGlobalCompBounds     
     * @see ZNode#computeGlobalCompBounds     
     * @return The node's visual component bounds in global coords
     */
    public ZBounds getGlobalCompBounds() {
	if (compBoundsDirty || isVolatile()) {
	    computeGlobalCompBounds();
	}
	return globalCompBounds;
    }

    /**
     * Return the bounds of this node's visual component transformed
     * by this node's transform.  That is, this is neither "local", nor "global"
     * bounds, but just the result of transforming the visual component's
     * bounds by this node's transform.
     * Note that the node component bounds is not cached, and this
     * is computed every time it is requested.
     * @return The node's component bounds
     */
    public ZBounds getCompBounds() {
	ZBounds bounds = new ZBounds();
	bounds.add(visualComponent.getLocalBounds());
	bounds.transform(getTransform().getAffineTransform());
	return bounds;
    }
    
    /**
     * Notifies the node that its cached bounds are no longer valid.  Used when one of its
     * descendents or its visual component  changes in some way that changes their bounds
     * in the space.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     */
    public void updateGlobalBounds() {
	globalBoundsDirty = true;
	ZNode p = getParent();
	if (p != null) {
	    p.updateGlobalBounds();
	}
    }
    
    /**
     * Recomputes and caches the bounds for this node.  Generally this method is
     * called by getGlobalBounds when the dirty bit is set.  It should rarely be called from
     * application code.  Note that if a node is invisible, it does not contribute
     * to its parent's bounds.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     */
    protected void computeGlobalBounds() {
	ZNode child;

	globalBoundsDirty = false;
	globalBounds.reset();

	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    if (child.isVisible()) {
		globalBounds.add(child.getGlobalBounds());
	    }
	}
	globalBounds.add(getGlobalCompBounds());	
    }
    
   /**
     * Return the bounds of the union of all descendents including the node's visual
     * component.  If a valid cached value is available, this method returns it.  If a
     * valid cache is not available (i.e. the dirty bit is set) then the bounds are
     * recomputed, cached and then returned to the caller.
     * For a discussion on coordinate systems and bounds see ZScenegraphObject.
     * @see ZScenegraphObject
     * @see ZNode#updateGlobalBounds     
     * @see ZNode#computeGlobalBounds     
     * @return The bounds of the intersection of all descendents including the node's visual
     * component bounds in global coords* Note that the node component bounds is not cached, and this
     */
    public ZBounds getGlobalBounds() {
	if (globalBoundsDirty || isVolatile()) {
	    computeGlobalBounds();
	}
	return globalBounds;
    }
    
    //****************************************************************************
    //
    //			Other Methods
    //
    //***************************************************************************

    /**
     * Return the preConcatenation of transforms that exist between this node and the rootNode
     * This node's transform IS included. If you need the transform between this
     * node and the rootNode without this node, call computeGlobalCoordinateFrame
     * on this node's parent.
     * @return The concatentation of ZNode transforms of this
     * node's transform and all the anscestors of this node.
     */
    public AffineTransform computeGlobalCoordinateFrame() {
	AffineTransform result = getTransform().getAffineTransform();

	if (parent != null) {
	    result.preConcatenate(parent.computeGlobalCoordinateFrame());
	}	
	
	return result;	
    }

    /** 
     * Returns the children within the specified bounds
     * 
     * @return A Vector of ZNodes.
     */
    public Vector getObjs(Rectangle2D bounds) {
	Vector v = new Vector();
	ZNode child;

	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    if (child.getGlobalCompBounds().intersects(bounds.getX(), bounds.getY(),
						       bounds.getWidth(), bounds.getHeight())) {
		v.addElement(child);
	    }
        }

        return v;
    }

    /**
     * Method to determine if node is a descendent of queryNode.
     * @param queryNode a possible ancenstor of node
     * @return true of queryNode is an ancestor of node.
     */
    public boolean isDescendent(ZNode queryNode) {
	ZNode tmp = getParent();
	while(true) {
	    if (tmp == null) {
		return false;
	    }
	    if (tmp == queryNode) {
		return true;
	    }
	    tmp = tmp.getParent(); 
	}
	
    }

    /**
     * Method to determine if node is ancenstor of queryNode.
     * @param queryNode a possible descendent of node
     * @return true of queryNode is an descendent of node.
     */
    public boolean isAncestor(ZNode queryNode) {
	ZNode child;

	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    if (child == queryNode) {
		return true;
	    }
	    if (child.isAncestor(queryNode)) {
		return true;
	    }
	}
	return false;	
    }

    /**  
     * Select this node's visual component.  Propagate the request to all of its children
     */
    public void selectAll(ZCamera camera) {
	ZNode child;

	visualComponent.select(camera);

	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    child.selectAll(camera);
	}
    }
    
    /**  
	 Un-Select this node's visual component.  Propagate the request to all of its children
    */
    public void unselectAll() {
	ZNode child;

	visualComponent.unselect();

	for (Iterator i=children.iterator(); i.hasNext();) {
            child = (ZNode)i.next();
	    child.unselectAll();
	}
    }

    /**
     * Add a new property to this node.  The property is specified by the String key.
     * If there is already a property with the same key, it will be replaced.
     * @param key The key to the new property.
     * @param value The value of the new property.
     */
    public void addProperty(String key, Object value) {
	ZProperty prop = null;
	boolean found = false;

	if (clientProperties == null) {
	    clientProperties = new Vector();
	}

	for (Iterator i = clientProperties.iterator(); i.hasNext() ; ) {
	    prop = (ZProperty)i.next();
	    if (prop.key.equals(key)) {
		prop.set(key, value);
		found = true;
		break;
	    }
	}
	if (!found) {
	    prop = new ZProperty(key, value);
	    clientProperties.add(prop);
	}
    }

    /**
     * Add a new property to this node.  The property is specified by the String key.
     * If there is already a property with the same key, it will be replaced.
     * @param property The new property.
     */
    public void addProperty(ZProperty newProp) {
	ZProperty prop;
	boolean found = false;

	if (clientProperties == null) {
	    clientProperties = new Vector();
	}

	for (Iterator i = clientProperties.iterator(); i.hasNext() ; ) {
	    prop = (ZProperty)i.next();
	    if (prop.getKey().equals(newProp.getKey())) {
		prop.set(newProp.getKey(), newProp.getValue());
		found = true;
		break;
	    }
	}

	if (!found) {
	    clientProperties.add(newProp);
	}
    }

    /**
     * Get the property with the specified key.  If the property doesn't
     * exist, then this returns null.  Note that it is impossible to have
     * two properties with the same key.
     * @return the value of the specified property, or null if it doesn't exist
     */
    public Object getProperty(String key) {
	ZProperty prop = null;
	Iterator i = clientProperties.iterator();
	for (; i.hasNext() ; ) {
	    prop = (ZProperty)i.next();
	    if (prop.key.equals(key)) {
		return prop.value;
	    }
	}

	return null;
    }

    /**
     * Get all the properties that this node has.
     * @return the properties of this node, or null if none.
     */
    public Vector getProperties() {
	return clientProperties;
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
	if (visualComponent != ZDummyVisualComponent.getInstance()) {
	    out.writeState("ZVisualComponent", "visualComponent", visualComponent);
	}
	if (!children.isEmpty()) {
	    out.writeState("Vector", "children", children);
	}
	if (alpha != alpha_DEFAULT) {
	    out.writeState("float", "alpha", alpha);
	}
	if (visible != visible_DEFAULT) {
	    out.writeState("boolean", "visible", visible);
	}
	if (!transform.transform.isIdentity()) {
	    out.writeState("ZTransform", "transform", transform);
	}
	if (minMag != minMag_DEFAULT) {
	    out.writeState("float", "minMag", minMag);
	}
	if (maxMag != maxMag_DEFAULT) {
	    out.writeState("float", "maxMag", maxMag);
	}
	if (clientProperties != null) {
	    out.writeState("Vector", "properties", clientProperties);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	ZNode child;
				// Add visual component if it is not a dummy
	if (visualComponent != ZDummyVisualComponent.getInstance()) {
	    out.addObject(visualComponent);
	}
				// Add children
	for (Iterator i=children.iterator(); i.hasNext();) {
	    child = (ZNode)i.next();
	    out.addObject(child);
	}
				// Add non-identity transform
	if (!transform.transform.isIdentity()) {
	    out.addObject(transform);
	}
				// Add properties
	if (clientProperties != null) {
	    ZProperty prop;
	    for (Iterator i=clientProperties.iterator(); i.hasNext();) {
		prop = (ZProperty)i.next();
		out.addObject(prop);
	    }
	}
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
	if (fieldName.compareTo("visualComponent") == 0) {
	    setVisualComponent((ZVisualComponent)fieldValue);
	} else if (fieldName.compareTo("children") == 0) {
	    ZNode child;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		child = (ZNode)i.next();
		addChild(child);
	    }
	} else if (fieldName.compareTo("properties") == 0) {
	    ZProperty prop ;
	    for (Iterator i=((Vector)fieldValue).iterator(); i.hasNext();) {
		prop = (ZProperty)i.next();
		addProperty(prop);
	    }
	} else if (fieldName.compareTo("transform") == 0) {
	    setTransform((ZTransform)fieldValue);
	} else if (fieldName.compareTo("alpha") == 0) {
	    alpha = ((Float)fieldValue).floatValue();
	} else if (fieldName.compareTo("visible") == 0) {
	    visible = ((Boolean)fieldValue).booleanValue();
	} else if (fieldName.compareTo("minMag") == 0) {
	    minMag = ((Float)fieldValue).floatValue();
	} else if (fieldName.compareTo("maxMag") == 0) {
	    maxMag = ((Float)fieldValue).floatValue();
	}
    }

    /**
     * Node doesn't get written out if save property is false
     */
    public ZSerializable writeReplace() {
	if (save) {
	    return this;
	} else {
	    return null;
	}
    }
}
