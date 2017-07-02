/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.scenegraph.ZNode;

/** 
 * A general filter interface that is used to determine if a specified
 * node should be accepted.
 * 
 * @author Ben Bederson
 * @see ZNode#findNodes
 */
public interface ZFindFilter {
    /**
     * This method determines if the specified node should be accepted by
     * the filter.  Users of this filter determine the semantics of how
     * the filter is applied, but generally if a node is not accepted, then
     * its children are still examined.
     * @param node The node that is to be examined by this filter
     * @return True if the node is accepted by the filter
     * @see ZNode#findNodes
     */
    public boolean accept(ZNode node);

    /**
     * This method determines if the children of the specified node should be
     * searched.
     * @param node The node that is to be examined by this filter
     * @return True if this node's children should be searched, or false otherwise.
     */
    public boolean findChildren(ZNode node);
}
