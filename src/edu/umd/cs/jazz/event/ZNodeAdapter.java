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
 *
 * An abstract adapter class for receiving node events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Extend this class to create a <code>ZNodeEvent</code> listener 
 * and override the methods for the events of interest. (If you implement the 
 * <code>ZNodeListener</code> interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using your class and then register it with a
 * component using the component's <code>addNodeListener</code> 
 * method. When the component's size, location, or visibility
 * changes, the relevant method in the listener object is invoked,
 * and the <code>ZNodeEvent</code> is passed to it.
 * <P>
 * Based on Swing's ComponentAdapter class.
 *
 * @see ZNodeAdapter
 * @see ZNodeEvent
 * @author Ben Bederson
 */
public class ZNodeAdapter implements ZNodeListener {
    /**
     * Invoked when the node's transform changes.
     */
    public void nodeTransformed(ZNodeEvent e) {}

    /**
     * Invoked when the node has been made visible.
     */
    public void nodeShown(ZNodeEvent e) {}

    /**
     * Invoked when the node has been made invisible.
     */
    public void nodeHidden(ZNodeEvent e) {}
}
