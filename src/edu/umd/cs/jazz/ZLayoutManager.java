/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import edu.umd.cs.jazz.io.*;

/**
 * <b>ZLayoutManager</b> represents an object that can layout the
 * children of a node.
 *
 * @author  Benjamin B. Bederson
 * @see     ZNode
 */
public interface ZLayoutManager extends ZSerializable {
    /**
     * Apply this manager's layout algorithm to the specified node's children.
     * @param The node to apply this layout algorithm to.
     */
    public void doLayout(ZGroup node);

    /**
     * Notify the layout manager that a potentially recursive layout is starting.
     * This is called before any children are layed out.
     * @param The node to apply this layout algorithm to.
     */
    public void preLayout(ZGroup node);

    /**
     * Notify the layout manager that the layout for this node has finished
     * This is called after all children and the node itself are layed out.
     * @param The node to apply this layout algorithm to.
     */
    public void postLayout(ZGroup node);
}
