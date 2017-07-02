/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * ZFindFilterMagBounds determines if the specified node's visual component falls within
 * the specified bounds in global coordinates, but only if the object is within
 * its visible magnification range.
 * 
 * @author Ben Bederson
 */
public class ZFindFilterMagBounds extends ZFindFilterBounds {
    ZBounds bounds = null;
    float mag = 1.0f;

    /**
     * Create a new magnification bounds filter.  This filter accepts nodes whose
     * visual component's bounds intersect the specified bounds, but only if the object
     * is within its visible magnification range.
     * @param bounds The bounds in global coordinates to search within.
     */
    public ZFindFilterMagBounds(ZBounds bounds, float mag) {
	super(bounds);

	this.bounds = bounds;
	this.mag = mag;
    }

    /**
     * The specified node is accepted by this filter if its visual component's bounds
     * intersect this filter's bounds.
     * @param node The node that is to be examined by this filter
     * @return True if the node is accepted by the filter
     */
    public boolean accept(ZNode node) {
	if ((mag < node.getMinMag()) || ((node.getMaxMag() > 0) && (mag > node.getMaxMag()))) {
	    return false;
	} else {
	    return super.accept(node);
	}
    }

    /**
     * This method determines if the children of the specified node should be
     * searched.
     * @param node The node that is to be examined by this filter
     * @return True if this node's children should be searched, or false otherwise.
     */
    public boolean findChildren(ZNode node) {
	if ((mag < node.getMinMag()) || ((node.getMaxMag() > 0) && (mag > node.getMaxMag()))) {
	    return false;
	} else {
	    return super.findChildren(node);
	}
    }
}
