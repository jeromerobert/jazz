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
 * ZAncestorListener
 *
 * Interface to support notification when changes occur to a ZNode or one
 * of its ancestors.  Based on Swing's AncestorListener.
 *
 * @author Ben Bederson
 */
public interface ZAncestorListener extends EventListener {
    /**
     * Called when the node or one of its ancestors is made visible
     * either by setVisible(true) being called or by its being
     * added to the node hierarchy.  The method is only called
     * if the node has actually become visible.  For this to be true
     * all its parents must be visible, and it is in a rooted hierarchy.
     */
    public void ancestorAdded(ZAncestorEvent event);

    /**
     * Called when the node or one of its ancestors is made invisible
     * either by setVisible(false) being called or by its being
     * remove from the node hierarchy.  The method is only called
     * if the node has actually become invisible.  For this to be true
     * at least one of its parents must by invisible or it is not in
     * a rooted hierarchy.
     */
    public void ancestorRemoved(ZAncestorEvent event);

    /**
     * Called when either the node or one of its ancestors is transformed
     */
    public void ancestorTransformed(ZAncestorEvent event);
}
