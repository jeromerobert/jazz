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
 * ZNodeContainerListener
 * <P>
 * Interface to support notification when nodes are added or removed from a ZNode.
 * Based on Swing's ContainerListener.
 * <P>
 * The listener interface for receiving ZNodeContainer events.
 * The class that is interested in processing a ZNodeContainer event
 * either implements this interface (and all the methods it
 * contains) or extends the abstract <code>ZNodeContainerAdapter</code> class
 * (overriding only the methods of interest).
 * The listener object created from that class is then registered with a
 * ZNode using the ZNode's <code>addNodeContainerListener</code> 
 * method. When a node is added or removed from a ZNode,
 * the relevant method in the listener object is invoked,
 * and the <code>ZNodeContainerEvent</code> is passed to it.
 * <P>
 * ZNodeContainer events are provided for notification purposes ONLY;
 * The AWT will automatically handle ZNode adds and removes
 * internally so that GUI layout works properly regardless of
 * whether a program registers a <code>ZNodeContainerListener</code> or not.
 *
 * @see ZNodeContainerAdapter
 * @see ZNodeContainerEvent
 * @author Ben Bederson
 */
public interface ZNodeContainerListener extends EventListener {
    /**
     * Invoked when a node has been added to this node.
     */
    public void nodeAdded(ZNodeContainerEvent e);

    /**
     * Invoked when a node has been removed from this node.
     */    
    public void nodeRemoved(ZNodeContainerEvent e);
}
