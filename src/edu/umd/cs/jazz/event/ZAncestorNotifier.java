/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import javax.swing.event.*;
import java.awt.event.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * Supports processing of ZAncestorEvents.
 * Based on Swing's AncestorNotifier.
 *
 * @author Ben Bederson
 */

public class ZAncestorNotifier implements ZNodeContainerListener
{
    ZNode firstInvisibleAncestor;
    ZNode root;
    public EventListenerList listenerList = new EventListenerList();

    public ZAncestorNotifier(ZNode root) {
	this.root = root;
       	addListeners(root, true);
    }

    public void addAncestorListener(ZAncestorListener l) {
	listenerList.add(ZAncestorListener.class, l);
    }

    public void removeAncestorListener(ZAncestorListener l) {
	listenerList.remove(ZAncestorListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected void fireAncestorAdded(ZNode source, int id, ZNode ancestor, ZNode ancestorParent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ZAncestorListener.class) {
		// Lazily create the event:
		ZAncestorEvent ancestorEvent = new ZAncestorEvent(source, id, ancestor, ancestorParent);
		((ZAncestorListener)listeners[i+1]).ancestorAdded(ancestorEvent);
	    }	       
	}
    }	
    
    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected void fireAncestorRemoved(ZNode source, int id, ZNode ancestor, ZNode ancestorParent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ZAncestorListener.class) {
		// Lazily create the event:
		ZAncestorEvent ancestorEvent = new ZAncestorEvent(source, id, ancestor, ancestorParent);
		((ZAncestorListener)listeners[i+1]).ancestorRemoved(ancestorEvent);
	    }	       
	}
    }	
    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected void fireAncestorTransformed(ZNode source, int id, ZNode ancestor, ZNode ancestorParent) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();

	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==ZAncestorListener.class) {
		// Lazily create the event:
		ZAncestorEvent ancestorEvent = new ZAncestorEvent(source, id, ancestor, ancestorParent);
		((ZAncestorListener)listeners[i+1]).ancestorTransformed(ancestorEvent);
	    }	       
	}
    }	

    public void removeAllListeners() {
	removeListeners(root);
    }

    void addListeners(ZNode ancestor, boolean addToFirst) {
	ZNode a;

	firstInvisibleAncestor = null;
	for (a = ancestor;
	     firstInvisibleAncestor == null;
	     a = a.getParent()) {
	    if (addToFirst || a != ancestor) {
		a.addNodeContainerListener(this);
	    }
	    if (!a.isVisible() || a.getParent() == null) {
		firstInvisibleAncestor = a;
	    }
	}
	if (firstInvisibleAncestor instanceof ZRootNode) {
	    firstInvisibleAncestor = null;
	}
    }

    void removeListeners(ZNode ancestor) {
	ZNode a;
	for (a = ancestor; a != null; a = a.getParent()) {
	    a.removeNodeContainerListener(this);
	    if (a == firstInvisibleAncestor) {
		break;
	    }
	}
    }

    public void nodeAdded(ZNodeContainerEvent e) {
	ZNode ancestor = e.getNode();

	if (ancestor == firstInvisibleAncestor) {
	    addListeners(ancestor, false);
	    if (firstInvisibleAncestor == null) {
		fireAncestorAdded(root, ZAncestorEvent.ANCESTOR_ADDED,
				  ancestor, ancestor.getParent());
	    }
	}
    }
    
    public void nodeRemoved(ZNodeContainerEvent e) {
	ZNode ancestor = e.getNode();
	boolean needsNotify = firstInvisibleAncestor == null;

	removeListeners(ancestor.getParent());
	firstInvisibleAncestor = ancestor;
	if (needsNotify) {
	    fireAncestorRemoved(root, ZAncestorEvent.ANCESTOR_REMOVED,
				ancestor, ancestor.getParent());
	}
    }
}
