/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * An event reported to a child node that originated from an
 * ancestor in the node hierarchy.  Based on Swing's AncestorEvent.
 *
 * @author Ben Bederson
 */
public class ZAncestorEvent extends AWTEvent {
    /**
     * An ancestor-node was added to the hierarchy of
     * visible objects (made visible), and is currently being displayed.
     */
    public static final int ANCESTOR_ADDED = 1;

    /**
     * An ancestor-node was removed from the hierarchy
     * of visible objects (hidden) and is no longer being displayed.
     */
    public static final int ANCESTOR_REMOVED = 2;

    /** 
     * An ancestor-node changed its position on the screen. 
     */
    public static final int ANCESTOR_MOVED = 3;

    ZNode ancestor;
    ZNode ancestorParent;

    /**
     * Constructs an ZAncestorEvent object to identify a change
     * in an ancestor-component's display-status.
     *
     * @param source          the ZNode that originated the event
     *                        (typically <code>this</code>)
     * @param id              an int specifying {@link ANCESTOR_ADDED}, 
     *                        {@link ANCESTOR_REMOVED} or {@link ANCESTOR_MOVED}
     * @param ancestor        a ZNode object specifying the ancestor-node
     *                        whose display-status changed
     * @param ancestorParent  a ZNode object specifying the ancestor's parent
     */
    public ZAncestorEvent(ZNode source, int id, ZNode ancestor, ZNode ancestorParent) {
        super(source, id);
        this.ancestor = ancestor;
        this.ancestorParent = ancestorParent;
    }

    /**
     * Returns the ancestor that the event actually occured on.
     */
    public ZNode getAncestor() {
        return ancestor;
    }

    /**
     * Returns the parent of the ancestor the event actually occured on.
     * This is most interesting in an ANCESTOR_REMOVED event, as
     * the ancestor may no longer be in the component hierarchy.
     */
    public ZNode getAncestorParent() {
        return ancestorParent;
    }

    /**
     * Returns the ZNode that the listener was added to.
     */
    public ZNode getComponent() {
        return (ZNode)getSource();
    }
}
