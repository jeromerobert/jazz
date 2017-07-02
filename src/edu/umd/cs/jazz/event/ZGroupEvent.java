/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.AWTEvent;
import java.awt.geom.AffineTransform;
import java.io.*;

import edu.umd.cs.jazz.*;

/**
 * <b>ZGroupEvent</b> is an event which indicates that a group node has changed.
 * <P>
 * Group events are provided for notification purposes ONLY;
 * Jazz will automatically handle changes to the group
 * contents internally so that the program works properly regardless of
 * whether the program is receiving these events or not.
 * <P>
 * This event is generated by a ZGroup
 * when a node is added or removed from it.
 * The event is passed to every <code>ZGroupListener</code>
 * or <code>ZGroupAdapter</code> object which registered to receive such
 * events using the group's <code>addGroupListener</code> method.
 * (<code>ZGroupAdapter</code> objects implement the 
 * <code>ZGroupListener</code> interface.) Each such listener object 
 * gets this <code>ZGroupEvent</code> when the event occurs.
 *
 * @see ZGroupAdapter
 * @see ZGroupListener
 *
 * @author Ben Bederson
 */
public class ZGroupEvent extends AWTEvent {

    /**
     * The first number in the range of ids used for group events.
     */
    public static final int GROUP_FIRST       = 100;

    /**
     * The last number in the range of ids used for group events.
     */
    public static final int GROUP_LAST        = 101;

   /**
     * This event indicates that a node was added to the group.
     */
    public static final int NODE_ADDED	= GROUP_FIRST;

    /**
     * This event indicates that a node was removed from the group.
     */
    public static final int NODE_REMOVED = 1 + GROUP_FIRST;

    /**
     * The non-null node that is being added or
     * removed from the group.
     *
     * @see #getChild
     */
    ZNode child;

    /**
     * Constructs a ZGroupEvent object.
     * 
     * @param source    the ZGroup object that originated the event
     * @param id        an integer indicating the type of event
     * @param child     the node that was added or removed
     */
    public ZGroupEvent(ZGroup source, int id, ZNode child) {
        super(source, id);
	this.child = child;
    }

    /**
     * Returns the originator of the event.
     *
     * @return the ZGroup object that originated the event
     */
    public ZGroup getGroup() {
        return (ZGroup)source;   // Cast is ok, checked in constructor
    }

    /**
     * Returns the ZNode that was affected by the event.
     *
     * @return the ZNode object that was added or removed
     */
    public ZNode getChild() {
        return child;
    }
}
