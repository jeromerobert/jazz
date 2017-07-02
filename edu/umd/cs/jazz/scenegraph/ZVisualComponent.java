/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * @author Ben Bederson
 * @author Britt McAlister
 * @see    ZVisualComponentDecorator
 */
public class ZVisualComponent implements ZScenegraphObject, ZSerializable {
				// Default values
    static public final boolean pickable_DEFAULT = true;
    static public final boolean findable_DEFAULT = true;

    /**
     * Bounds of this visual component relative to its coordinate frame.
     */
    protected ZBounds localBounds = null;

    /**
     * The parent of this visual component (or null if none).
     */
    protected ZScenegraphObject parent = null;

    // BDM convert to bits
    protected boolean selected = false;              // True if selected
    protected boolean pickable = pickable_DEFAULT;   // True if this should be able to be picked
    protected boolean findable = findable_DEFAULT;   // True if this should be able to be found
    protected boolean isVolatile = false;            // True if this component specifically set to be volatile
    protected boolean cacheVolatile = false;         // Cached volatility computation
    protected boolean localBoundsDirty = true;       // True if localBounds is out of date and needs to be recomputed

    /**
     * Default constructor for visual component.
     * <p>
     * Most visual components will want to store their
     * visual bounds, and so we allocate localBounds here.
     * However, if a particular visual component is implemented by
     * computing its bounds every time it is asked instead of allocating
     * it, then it can free up the ZBounds allocated here.
     */
    public ZVisualComponent() {
	localBounds = new ZBounds();
    }

    /**
     * Constructs a new ZVisualComponent that is a copy of the specified component (i.e., a "copy constructor").
     * The portion of the reference component that is duplicated is that necessary to reuse the component
     * within a new node, but the new visual component is not inserted into a node.
     * <p>
     * Most visual components will want to store their
     * visual bounds, and so we allocate localBounds here.
     * However, if a particular visual component is implemented by
     * computing its bounds every time it is asked instead of allocating
     * it, then it can free up the ZBounds allocated here.
     */
    public ZVisualComponent(ZVisualComponent vc) {
	if (vc.localBounds != null) {
	    localBounds = (ZBounds)vc.localBounds.clone();
	}
	isVolatile = vc.isVolatile;
	cacheVolatile = vc.cacheVolatile;
	parent = null;
	selected = vc.selected;
	pickable = vc.pickable;
	findable = vc.findable;
    }

    /**
     * Duplicates the current visual component by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZVisualComponent(ZVisualComponent)
     */
    public Object clone() {
	return new ZVisualComponent(this);
    }
    
    //****************************************************************************
    //
    //			Get/Set  pairs
    //
    //***************************************************************************

    /**
     * Determines if this visual component is volatile.
     * A visual component is considered to be volatile if it is specifically set
     * to be volatile with {@link #setVolatile}, or if any of its descendants are volatile.
     * <p>
     * Volatile objects are those objects that change regularly, such as an object
     * that is animated, or one whose rendering depends on its context.  For instance,
     * a selection marker {@link ZSelectionDecorator} is always one-pixel thick, and thus its bounds
     * depend on the current magnification.
     * @return true if this component is volatile
     * @see #setVolatile(boolean)
     */
    public boolean isVolatile() {
	return cacheVolatile;
    }

    /**
     * Specifies that this visual component is volatile.
     * Note that this component is considered to be volatile if any of its
     * descendants are volatile, even if this component's volatility is set to false.
     * This implies that all parents of this visual component are also volatile
     * when this is volatile.
     * <p>
     * Volatile objects are those objects that change regularly, such as an object
     * that is animated, or one whose rendering depends on its context.  For instance,
     * a selection marker {@link ZSelectionDecorator} is always one-pixel thick, and thus its bounds
     * depend on the current magnification.
     * @param v the new specification of whether this component is volatile.
     * @see #isVolatile()
     */
    public void setVolatile(boolean v) {
	isVolatile = v;
	updateVolatility();
    }
    
    /**
     * Internal method to compute and cache the volatility of a component,
     * to recursively call the parents to compute volatility.
     * A component is considered to be volatile if it is set to be volatile,
     * or if any of its descendants are volatile.
     * @see #setVolatile(boolean)
     * @see #isVolatile()
     */
    public void updateVolatility() {
	cacheVolatile = isVolatile;
	if (!cacheVolatile) {
	    if (this instanceof ZHasChild) {
		ZVisualComponent child = ((ZHasChild)this).getChild();
		if (child != null) {
		    cacheVolatile = child.isVolatile();
		}
	    }
	}
	if (parent != null) {
	    parent.updateVolatility();
	}
    }

    /**
     * Determines if this component is selected.
     * @return true if this component is selected.
     */    
    public boolean isSelected() {return selected;}

    /**
     * Specify if this component is selected.  A component is selected by adding
     * a ZSelectionDecorator that wraps this component.  The default selection
     * decorator renders a constant one-pixel thick rectangle around the component.
     * The camera parameter is necessary to specify through which camera the
     * selection rectangle is one-pixel thick within.
     * @param b true if the component should be selected, false otherwise
     * @param camera the camera that the selection is rendered constant thickness within
     * @see ZSelectionDecorator
     */
    public void setSelected(boolean b, ZCamera camera) {
	if (b) {
	    select(camera);
	}
	else {
	    unselect();
	}
    }

    /**
     * Select this visual component by inserting a ZSelectionDecorator component
     * just above this as a decorator.  A component is selected by adding
     * a ZSelectionDecorator that wraps this component.  The default selection
     * decorator renders a constant one-pixel thick rectangle around the component.
     * The camera parameter is necessary to specify through which camera the
     * selection rectangle is one-pixel thick within.
     * @param camera the camera that the selection is rendered constant thickness within
     * @see ZSelectionDecorator
     * @see #select(ZSelectionDecorator)
     */   
    public void select(ZCamera camera) {
	select(new ZSelectionDecorator(camera));
    }
    
    /**
     * Select this visual component.  This uses the selection decorator that is
     * passed in.
     * @param selectionDecorator - the decorator that should wrap this
     * @see #select(ZCamera)
     */
    public void select(ZSelectionDecorator selectionDecorator) {
	if (selected == false) {
	    selectionDecorator.select(this);
	    selected = true;
	}
    }

    /**
     * Unselect this visual component by searching its parents for a
     * ZSelectionDecorator to remove.
     * @see #select(ZCamera)
     * @see #select(ZSelectionDecorator)
     */    
    public void unselect() {
	if (selected == true) {
	    ZScenegraphObject obj = getParent();
	    while (obj != null) {
		if (obj instanceof ZSelectionDecorator) {
		    ((ZSelectionDecorator)obj).unselect();
		    break;
		}
		if (obj instanceof ZVisualComponent) {
		    obj = ((ZVisualComponent)obj).getParent();
		} else {
		    obj = null;
		}
	    }
	    selected = false;
	}
    }

    /**
     * Returns the parent of this component, or null if none.
     * @return the parent.
     */
    public ZScenegraphObject getParent() {return parent;}

    /**
     * Sets the parent of this component.  Note that generally, this method
     * should not be used by an application as the parent pointer is properly
     * set or cleared when a visual component is added removed.
     * @param parent The new parent of this visual component
     * @see ZNode#addChild
     * @see ZNode#removeChild
     */
    public void setParent(ZScenegraphObject parent) {
	this.parent = parent;
    }

    /**
     * Determines if this visual component is pickable.
     * When a visual component is not pickable, it will be ignored by
     * the Jazz pick methods that determine which object is under a point.
     * If an object is not pickable, it will not respond to input events.
     * @return True if this component is pickable
     * @see #setPickable
     * @see ZCamera#pick
     * @see ZNode#pick
     * @see #isFindable
     */
    public boolean isPickable() {
	return pickable;
    }

    /**
     * Specifies whether this visual component is pickable.
     * When a visual component is not pickable, it will be ignored by
     * the Jazz pick methods that determine which object is under a point.
     * If an object is not pickable, it will not respond to input events.
     * @see #isPickable
     * @see ZCamera#pick
     * @see ZNode#pick
     * @see #isFindable
     */
    public void setPickable(boolean pickable) {
	this.pickable = pickable;
    }

    /**
     * Determines if this visual component is findable.
     * When a visual component is not findable, it will be ignored by
     * the Jazz findNodes methods that determine which object satisfy
     * some search criteria.
     * @return True if this component is findable
     * @see #setFindable
     * @see ZCamera#findNodes
     * @see ZNode#findNodes
     * @see #isPickable
     */
    public boolean isFindable() {
	return findable;
    }

    /**
     * Specifies whether this visual component is findable.
     * When a visual component is not findable, it will be ignored by
     * the Jazz findNodes methods that determine which object satisfy
     * some search criteria.
     * @param findable True if this component should be findable
     * @see #isFindable
     * @see ZCamera#findNodes
     * @see ZNode#findNodes
     * @see #isPickable
     */
    public void setFindable(boolean findable) {
	this.findable = findable;
    }

    /**
     * This is a utility function to determine if the specified rectangle
     * intersects the bounds of this visual component.
     * @param rect the rectangle that this method tests for intersection with
     * @return true if this component's local bounds intersects the specified rectangle
     */
    public boolean pickBounds(Rectangle2D rect) {
	if ((localBounds == null) ||
	    (localBounds.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()))) {
	    return true;
	}
	return false;
    }

    /**
     * Determines whether the specified rectangle should "pick" this visual component.
     * Picking is typically used to determine if a pointer is over an object, and
     * thus pick should be implemented to retur true if the rectangle intersects the object.
     * <p>
     * The implementation of this pick method for the base visual component returns
     * true if the rectangle intersects the bounds of the component.  If a sub-class
     * wants more detailed picking, then it must extend this class.  For example,
     * a circle may only consider itself to be picked if the pointer is within
     * the circle - rather than within the rectangular bounds.
     * @param rect The rectangle that is picking this visual component
     * @return true if the rectangle picks this visual component
     */
    public boolean pick(Rectangle2D rect) {
	return pickBounds(rect);
    }

    /**
     * Traverses the parents until it finds the {@link ZNode} that references
     * this visual component (or the decorator chain that leads to this component).
     * @return this component's node
     */
    public ZNode findNode() {
	if (parent == null) {
	    return null;
	} else {
	    return parent.findNode();
	}
    }

    /**
     * Traverse the list of visual components (downwards), and return the
     * first one that is of the specified Class, or null if none.
     */
    public ZVisualComponent findVisualComponent(Class type) {
	if (type.isInstance(this)) {
	    return this;
	} else {
	    if (this instanceof ZHasChild) {
		ZVisualComponent vc = ((ZHasChild)this).getChild();
		return vc.findVisualComponent(type);
	    } else {
		return null;
	    }
	}
    }

    /**
     * This should never be called.  It is a place holder because all ZScenegraphObjects must
     * implement the setVisualComponent method.  A regular visual component
     * does not have any children, and so it does not make any sense to
     * try and add a child to it.
     */
    public void setVisualComponent(ZVisualComponent child)  {
	System.out.println("ZVisualComponent.setVisualComponent: Error: Can't add child to visual component: child = " + 
			   child);
    }
     
     /**
     * Paints this visual component.  The rendercontext contains various
     * aspects of the rendering state including the Graphics2D and the
     * camera being rendered within.  
     * <p>
     * It is guaranteed that
     * the transform, clip, and composite of the Graphics2D will be set properly
     * for this component.
     * However, the color, font, and stroke are unset, and the visual component
     * must set those things as needed.  The visual component is
     * not obligated to restore any aspect of the Graphics2D state.
     * @param renderContext The graphics context to use for rendering.
     * @see ZVisualComponent#paint(ZRenderContext)
     */
    public void paint(ZRenderContext renderContext) {
    }

    /* 
     * Damage causes each camera that can see this component to be notified
     * so they can update their representation of the portion of the screen that
     * needs to be updated.
     * <p>
     * Important note : There are two proper uses of damage.
     * <ol>
     * <li>When the bounds of the object do not change, you can simply call this damage method
     * <li>When making a change to an object that affects it's bounds
     * in any way (change of penWidth, selection, transform, etc.) you must call
     * {@link #damage(boolean)}.
     * </ol>
     */
    public void damage() {
	if (parent != null) {
	    parent.damage();
	}
    }

    /* 
     * Damage causes each camera that can see this component to be notified
     * so they can update their representation of the portion of the screen that
     * needs to be updated.
     * <p>
     * Important note : There are two proper uses of damage.
     * <ol>
     * <li>When the bounds of the object do not change, you should call 
     * {@link #damage}.
     * <li>When making a change to an object that affects it's bounds
     * in any way (change of penWidth, selection, transform, etc.) you must call
     * this damage method.
     * </ol>
     */
    public void damage(boolean boundsChanged) {
	if (parent != null) {
	    parent.damage(false);
	    updateBounds();
	    parent.damage();
	}
    }

    /**
     * Notifies this component that it must recompute its bounds.
     * It actually just sets a bit saying that the bounds are out of date,
     * and the next time the bounds are requested, they will be recomputed
     * by calling {@link #computeLocalBounds}
     */
    public void updateBounds() {
	updateLocalBounds();
	if (parent != null) {
	    parent.updateBounds();
	}
    }

    /**
     * Since a visual component does not have children, this method does
     * nothing, but it may do something for other types of scenegraph objects.
     * @see ZNode#updateChildBounds
     */
    public void updateChildBounds() {
    }

    /**
     * Specifies that this component's cached local bounds are out of date,
     * and should be recomputed before being accessed.
     */
    public void updateLocalBounds() {
	localBoundsDirty = true;
    }

    /**
     * All sub-classes need to define this method to compute their local bounds.
     * The current bounds should be stored in the 'localBounds' variable. which
     * will be accessed by getLocalBounds().
     * <p>
     * Note that this method is not typically called directly by application writers,
     * but if it is called, then the caller <b>must</b> clear the dirty bit
     * which records the fact that the stored bounds is up to date.  Do this with:
     * <tt>localBoundsDirty = false</tt>
     *
     * @see #getLocalBounds()
     */    
    protected void computeLocalBounds() {
    }

    /**
     * Returns the local bounds of this component.
     * If the bounds are out date, or the component is volatile,
     * then the bounds will be recomputed with {@link #computeLocalBounds} first.
     * @return the bounds
     */    
    public ZBounds getLocalBounds() {
	if (localBoundsDirty || isVolatile()) {
	    localBoundsDirty = false;
	    computeLocalBounds(); 
	}
	return localBounds;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String bounds;
	
	if (localBounds != null) {
	    bounds = localBounds.toString();
	}
	else {
	    bounds = "[0, 0, -1, -1]";
	}

	return super.toString() + "\nBounds: " + bounds;
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Write out all of this object's state.
     * Also note that visual components
     * are never written out selected.  So, even if it is selected, the
     * selected bit is not written out.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
				// Note that we don't have to write out anything here.
				//   - localBounds gets recalculated when any state is set
				//   - parent gets set when node is inserted into tree
	if (pickable != pickable_DEFAULT) {
	    out.writeState("boolean", "pickable", pickable);
	}
	if (findable != findable_DEFAULT) {
	    out.writeState("boolean", "findable", findable);
	}
	if (isVolatile()) {
	    out.writeState("boolean", "isvolatile", true);
	}
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
	if (fieldName.compareTo("isvolatile") == 0) {
	    setVolatile(((Boolean)fieldValue).booleanValue());
	}
	if (fieldName.compareTo("pickable") == 0) {
	    setPickable(((Boolean)fieldValue).booleanValue());
	}
	if (fieldName.compareTo("findable") == 0) {
	    setFindable(((Boolean)fieldValue).booleanValue());
	}
    }
}
