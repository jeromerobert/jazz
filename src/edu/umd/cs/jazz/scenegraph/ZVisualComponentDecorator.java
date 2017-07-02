/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZVisualComponentDecorator</b> is the base class used to create a visual component decorator.
 * Normally, a node has a single visual component.  However, with the use of this class,
 * it is possible to form a linear chain of visual components.  Typically the last element
 * of the chain represents the primary visual component that gets drawn, and all the other
 * elements of the chain are decorators that somehow modify the primary visual component.
 * Two standard decorators that are part of Jazz are ZSelectionDecorator and ZLinkDecorator.
 *
 * This class defines the minimum components of a visual component decorator, and will
 * typically be extended by an application for any specific decorator.  This class defines a
 * child pointer to reference the next element of the chain, implements child manipulation
 * methods, and wraps the paint, computeLocalBounds, pick and io methods.
 *
 * @author  Benjamin B. Bederson
 */
public class ZVisualComponentDecorator extends ZVisualComponent implements ZHasChild, ZSerializable {
    protected ZVisualComponent child = null;

    /**
     * Constructs a new ZVisualComponentDecorator
     */
    public ZVisualComponentDecorator() {
    }

    /**
     * Constructs a new ZVisualComponentDecorator that decorates the specified child.
     * @param child The child that should go directly below this decorator.
     */
    public ZVisualComponentDecorator(ZVisualComponent c) {
	insertAbove(c);
    }

    /**
     * Constructs a new ZVisualComponentDecorator that is a copy of the specified decorator
     *  component (i.e., a "copy constructor").
     * The portion of the reference decorator that is duplicated is that necessary to reuse the component
     * within a new node, but the new decorator is not inserted into a node.
     */
    public ZVisualComponentDecorator(ZVisualComponentDecorator dec) {
	super(dec);
				// Do a deep copy of child
	child = (ZVisualComponent)dec.getChild().clone();
	child.setParent(this);
    }

    /**
     * Duplicates the current visual component decorator by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZVisualComponentDecorator(ZVisualComponentDecorator)
     */
    public Object clone() {
	return new ZVisualComponentDecorator(this);
    }
    
    /**
     * Insert this decorator in the chain above the specified child visual component.
     * If the child visual component already had a parent, then this decorator will be
     * properly inserted inbetween the original parent and the child.
     * If this decorator was already part of a chain, then it will be removed
     * before being inserted.
     * @param c the child visual component that this decorator should go above.
     */
    public void insertAbove(ZVisualComponent c) {
				// If this decorator is already in a chain somewhere,
				// then first remove it
	if ((parent != null) || (child != null)) {
	    remove();
	}

				// Now, go ahead and insert it above the specified component
	c.damage();
	ZScenegraphObject p = c.getParent();
	if (p != null) {
	    p.setVisualComponent(this);
	}
	this.setVisualComponent(c);
	updateBounds();
	damage(); 
    }

    /**
     * Remove this decorator from the visual component chain.
     * If this decorator had a parent, then after this decorator is removed,
     * the child will be properly inserted underneath this decorator's original parent.
     */
    public void remove() {
	ZScenegraphObject p = parent;
	ZVisualComponent c = child;
	p.damage();
	this.setVisualComponent(null);
	p.setVisualComponent(c);       // Note that this call sets the child's parent pointer to p
	p.updateBounds();
	p.damage();
    }

    public ZVisualComponent getChild() { return child;}
    public void setChild(ZVisualComponent  v) {this.child = v;}
    
    public void setVisualComponent(ZVisualComponent c) {
				// if this.child exists, clear child
	if (child != null) {
	    child.setParent(null);
	}
				// set parameter
	child = c;
	if (c != null) {
	    c.setParent(this);
	}
    }
    
    /**
     * Calls the child's paint method
     * @param <code>g2</code> The graphics context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	if (child != null) {
	    child.paint(renderContext);
	}
    }

    /**
     * Calls the child's computeLocalBounds method.
     */
    protected void computeLocalBounds() {
	localBounds.reset();
	if (child != null) {
	    localBounds.add(child.getLocalBounds());
	}
    }

    /**
     * Returns true if the specified rectangle is on the child.
     * @param <code>rect</code> Pick rectangle in object coordinates.
     * @return True if point is on object.
     */
    public boolean pick(Rectangle2D rect) {
	if (child != null) {
	    return child.pick(rect);
	} else {
	    return false;
	}
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String childStr = "";
	if (child != null) {
	    childStr = child.toString();
	}
	return super.toString() + " wraps \n                      " + childStr;
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

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
	super.setState(fieldType, fieldName, fieldValue);

	if (fieldName.compareTo("child") == 0) {
	    setVisualComponent((ZVisualComponent)fieldValue);
	}
    }

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out); 

	if (child != null) {
	    out.writeState("ZVisualComponent", "child", child);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);

				// Add child visual component
	if (child != null) {
	    out.addObject(child);
	}
    }
}
