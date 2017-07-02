/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

/**
 * <b>ZLayoutManager</b> represents an object that can layout the
 * children of a node.
 *
 * @author  Benjamin B. Bederson
 * @see     ZNode
 */
public interface ZLayoutManager {
    /**
     * Apply this manager's layout algorithm to the specified node's children.
     * @param The node to apply this layout algorithm to.
     */
    public void doLayout(ZNode node);
}
