/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

/**
 * <b>ZHasChild</b> is a simple interface that ZVisualComponent decorators
 * must implement in order to represent the fact that they store a pointer
 * to a child visual component.  This interface is necessary because it
 * is used by ZVisualComponent.findVisualComponent() to traverse the list
 * of visual components to find one of a specific type. 
 *
 * @author  Benjamin B. Bederson
 * @see     ZVisualComponent
 */
public interface ZHasChild {
    public ZVisualComponent getChild();
}
