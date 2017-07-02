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
 * ZNodeListener
 * <P>
 * Interface to support notification when changes occur to a ZNode.
 * Based on Swing's ComponentListener.
 * <P>
 * The listener interface for receiving ZNode events.
 * The class that is interested in processing a ZNode event
 * either implements this interface (and all the methods it
 * contains) or extends the abstract <code>ZNodeAdapter</code> class
 * (overriding only the methods of interest).
 * The listener object created from that class is then registered with a
 * ZNode using the ZNode's <code>addNodeListener</code> 
 * method. When the ZNode's size, location, or visibility
 * changes, the relevant method in the listener object is invoked,
 * and the <code>ZNodeEvent</code> is passed to it.
 * <P>
 * ZNode events are provided for notification purposes ONLY;
 * The AWT will automatically handle ZNode transforms
 * internally so that GUI layout works properly regardless of
 * whether a program registers a <code>ZNodeListener</code> or not.
 *
 * @see ZNodeAdapter
 * @see ZNodeEvent
 * @author Ben Bederson
 */
public interface ZNodeListener extends EventListener {
    /**
     * Invoked when the node's transform changes.
     */
    public void nodeTransformed(ZNodeEvent e);

    /**
     * Invoked when the node has been made visible.
     */
    public void nodeShown(ZNodeEvent e);

    /**
     * Invoked when the node has been made invisible.
     */
    public void nodeHidden(ZNodeEvent e);
}
