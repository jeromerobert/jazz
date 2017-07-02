/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

import javax.swing.*;

/**
 * An abstract adapter class for receiving ZNodeContainer events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Extend this class to create a <code>ZNodeContainerEvent</code> listener 
 * and override the methods for the events of interest. (If you implement the 
 * <code>ZNodeContainerListener</code> interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with 
 * a node using the node's <code>addNodeContainerListener</code> 
 * method. When the container's contents change because a node has
 * been added or removed, the relevant method in the listener object is invoked,
 * and the <code>ZNodeContainerEvent</code> is passed to it.
 * <P>
 * Based on Swing's ContainerAdapter class.
 *
 * @see ZNodeContainerAdapter
 * @see ZNodeContainerEvent
 * @author Ben Bederson
 */
public class ZNodeContainerAdapter implements ZNodeContainerListener {
    /**
     * Invoked when a node has been added to this node.
     */
    public void nodeAdded(ZNodeContainerEvent e) {}

    /**
     * Invoked when a node has been removed from this node.
     */    
    public void nodeRemoved(ZNodeContainerEvent e) {}
}
