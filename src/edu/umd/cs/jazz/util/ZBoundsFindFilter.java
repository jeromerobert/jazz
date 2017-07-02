/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.*;
import java.io.*;

/**
 * <b>ZBoundsFindFilter</b> is a filter that accepts visual and terminal 
 * nodes that overlap the specified bounds in global coordinates.
 * Visual nodes are those node types that have visual components, and
 * are not editors of another node (i.e., do not have 'hasOneChild' set).
 * Terminal nodes are leaf nodes and group
 * nodes that do not "childrenFindable".
 * 
 * @author Ben Bederson
 */
public class ZBoundsFindFilter implements ZFindFilter, Serializable {
    ZBounds bounds = null;

    /**
     * Create a new bounds filter.
     * @param bounds The bounds in global coordinates to search within.
     */
    public ZBoundsFindFilter(ZBounds bounds) {
	this.bounds = bounds;
    }

    /**
     * Determine if the specified node is accepted by this filter.
     * @param node The node that is to be examined by this filter
     * @return True if the node is accepted by the filter
     */
    public boolean accept(ZNode node) {
				// If node is "visual" or "terminal"
	if (((node instanceof ZVisualGroup) && !((ZGroup)node).hasOneChild()) || 
	    (node instanceof ZLeaf) || 
	    ((node instanceof ZGroup) && !((ZGroup)node).getChildrenFindable())) {
				// If node intersects bounds
	    ZBounds nodeBounds = node.getGlobalBounds();
	    if (nodeBounds.intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())) {
		return true;
	    } else {
		return false;
	    }
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
	ZBounds nodeBounds = node.getGlobalBounds();
	if (nodeBounds.intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())) {
	    return true;
	} else {
	    return false;
	}
    }
}
