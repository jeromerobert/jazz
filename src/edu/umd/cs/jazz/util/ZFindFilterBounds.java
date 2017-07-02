/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * ZFindFilterBounds determines if the specified node's visual component falls within
 * the specified bounds in global coordinates.
 * 
 * @author Ben Bederson
 */
public class ZFindFilterBounds implements ZFindFilter {
    ZBounds bounds = null;

    /**
     * Create a new bounds filter.  This filter accepts nodes whose
     * visual component's bounds intersect the specified bounds.
     * @param bounds The bounds in global coordinates to search within.
     */
    public ZFindFilterBounds(ZBounds bounds) {
	this.bounds = bounds;
    }

    /**
     * The specified node is accepted by this filter if its visual component's bounds
     * intersect this filter's bounds.
     * @param node The node that is to be examined by this filter
     * @return True if the node is accepted by the filter
     */
    public boolean accept(ZNode node) {
	ZBounds nodeCompBounds = node.getGlobalCompBounds();
	if (nodeCompBounds.intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())) {
	    return true;
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
    public boolean findChildren(ZNode node) {
	ZBounds nodeBounds = node.getGlobalBounds();
	if (nodeBounds.intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())) {
	    return true;
	} else {
	    return false;
	}
    }
}
