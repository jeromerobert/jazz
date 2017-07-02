/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.*;
import java.io.*;

/**
 * <b>ZMagBoundsFindFilter</b> is a filter that accepts "terminal" nodes that overlap
 * the specified bounds in global coordinates, but only if the object is within
 * its visible magnification range.  
 * Terminal nodes are leaf nodes and group
 * nodes that do not "childrenFindable".
 * 
 * @author Ben Bederson
 */
public class ZMagBoundsFindFilter extends ZBoundsFindFilter implements Serializable {
    ZBounds bounds = null;
    float mag = 1.0f;

    /**
     * Create a new magnification bounds filter.  This filter accepts "terminal" nodes whose
     * bounds intersect the specified bounds, but only if the object
     * is within its visible magnification range.
     * Terminal nodes are leaf nodes and group
     * nodes that do not "childrenFindable".
     * @param bounds The bounds in global coordinates to search within.
     * @param mag The magnification to use for filtering
     */
    public ZMagBoundsFindFilter(ZBounds bounds, float mag) {
	super(bounds);

	this.bounds = bounds;
	this.mag = mag;
    }

    /**
     * Determine if the specified node is accepted by this filter.
     * @param node The node that is to be examined by this filter
     * @return True if the node is accepted by the filter
     */
    public boolean accept(ZNode node) {
	boolean visible = true;

	if (node instanceof ZFadeGroup) {
	    visible = ((ZFadeGroup)node).isVisible(mag);
	}
	if (visible) {
	    return super.accept(node);
	} else {
	    return false;
	}
    }

    /**
     * This method determines if the children of the specified node should be
     * searched.
     * @param node The node that is to be examined by this filter
     * @return True if this node's children should be searched, or false otherwise.
     */
    public boolean childrenFindable(ZNode node) {
	boolean visible = true;

	if (node instanceof ZFadeGroup) {
	    visible = ((ZFadeGroup)node).isVisible(mag);
	}
	if (visible) {
	    return super.childrenFindable(node);
	} else {
	    return false;
	}
    }
}
