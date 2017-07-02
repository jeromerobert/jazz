/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.util.*;

/** ZDummyVisualComponent is the default Visual Component to all ZNodes.  It serves as a placeholder until a ZNode has
 * a real visual component.  Using ZDummyVisualComponent allows us to 'decouple` ZNodes and Visual Components because
 * a ZNode need not check if it contains a Visual Component before performing some operation on it
 * (i.e. pick, paint, etc).  There will always be a Visual Component (even if it's only a Dummy that does
 * nothing).
 * Also, the ZDummyVisualComponent is a singleton type object.  Do not call the constructor directly.  Use
 * the getInstance method
 * 
 * 
 * @author Ben Bederson
 * @author Britt McAlister
 */
public class ZDummyVisualComponent extends ZVisualComponent {

    /** Hang onto the one and only instance allowed to be created.
     * 
     */
    static protected ZDummyVisualComponent    instance;

    
    /** 
     * Do not call this method directly.  Use the static method getInstance
     * 
     * @exception ZOperationNotAllowedException An attempt to create more
     *      than one ZDummyVisualComponent
     * @see #getInstance()
     */
    public ZDummyVisualComponent() throws ZOperationNotAllowedException {
	if (instance != null) {
	    throw new ZOperationNotAllowedException();
	}
    }

    
    /** 
     * Returns the one and only ZDummyVisualComponent
     */
    static public ZDummyVisualComponent getInstance() {
	if (instance == null) {
	    try {
		instance = new ZDummyVisualComponent();
	    }
	    catch (ZOperationNotAllowedException e) {
	    }
	}
	return instance;
    }

    public ZNode findNode() {
	return null;
    }
    //****************************************************************************
    //
    //			Get/Set  pairs
    //
    //***************************************************************************

    /** Always returns false
     */
    public boolean isVolatile() {return false;}
    /** Always a noop
     * 
     * @param v 
     */
    public void setVolatile(boolean  v) {}

    
    /** Always returns false
     */
    public Object getAppData() {return null;}

    
    /** Always a noop
     * 
     * @param data 
     */ 
    public void setAppData(Object data) {}

    
    /** always returns 0
     */
    public float getMinMag() {return 0;}

    
    /** always a noop
     * 
     * @param minMag 
     */
    public void setMinMag(float minMag) {}
    
    
    /** always return 0
     */
    public float getMaxMag() {return 0;}

    
    /** always a noop
     * 
     * @param maxMag 
     */
    public void setMaxMag(float maxMag) {}

    
    /** always return false
     */
    public boolean isSelected() {return false;}

    
    /** always a noop
     * 
     * @param b 
     */
    public void setSelected(boolean b) {}
    

    /** always return null
     */
    public ZScenegraphObject getParent() {return null;}

    
    /** always a noop
     * 
     * @param parent 
     */
    public void setParent(ZScenegraphObject parent) {}

    
    /** always return false
     */
    public boolean isPickable() {return false;}

    
    /** always a noop
     * 
     * @param pickable 
     */
    public void setPickable(boolean pickable) {}

    
   //****************************************************************************
    //
    //			Other Methods
    //
    //***************************************************************************

    /** always a noop
     */
    public void select(ZCamera camera) {}

    
    /** always a noop
     */
    public void unselect() {}

    
    /** always a noop
     * 
     * @param g2 
     */
    public void paint(ZRenderContext renderContext) {}

    
    /** always a noop
     */
    public void damage() {}

    
    /** always a noop
     */
    public void updateBounds() {}

    
    /** always a noop
     */
    public void updateChildBounds() {}

    
    /** always a noop
     */
    public void updateLocalBounds() {}    

    
    /** always a noop
     */
    public ZBounds getLocalBounds() {return localBounds;}

    
    /** always a noop
     * 
     * @param pt 
     */
    public boolean pick(Point2D pt) {return false;}

    
    /** always a noop
     * 
     * @param child 
     */
    public void setVisualComponent(ZVisualComponent child){}
}
