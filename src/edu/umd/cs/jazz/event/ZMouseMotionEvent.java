/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZMouseMotionEvent</b> is an event which indicates that a mouse motion action occurred in a node.
 * <P>
 * This low-level event is generated by a node object for:
 * <ul>
 * <li> Mouse Motion Events
 *     <ul>
 *     <li>the mouse is moved
 *     <li>the mouse is dragged
 *     </ul>
 * </ul>
 * <P>
 * A ZMouseEvent object is passed to every <code>ZMouseMotionListener</code>
 * or <code>ZMouseMotionAdapter</code> object which registered to receive
 * mouse motion events using the component's <code>addMouseMotionListener</code>
 * method. (<code>ZMouseMotionAdapter</code> objects implement the
 * <code>ZMouseMotionListener</code> interface.) Each such listener object
 * gets a <code>ZMouseEvent</code> containing the mouse motion event.
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @see ZMouseMotionAdapter
 * @see ZMouseMotionListener
 * @author: Jesse Grosjean
 */
public class ZMouseMotionEvent extends ZMouseEvent {

    /**
     * Constructs a new ZMouse event from a Java MouseEvent.
     *
     * @param id The event type (MOUSE_MOVED, MOUSE_DRAGGED)
     * @param e The original Java mouse event
     * @param path The path to use for getNode() and getPath()
     * @param mouseOverPath The path to the current node under the mouse, this may be differnt then the normal path
     * when in MOUSE_DRAGGED events.
     */
    protected ZMouseMotionEvent(int id, MouseEvent e, ZSceneGraphPath path, ZSceneGraphPath mouseOverPath) {
        super(id, e, path, mouseOverPath);
    }

    /**
     * Calls appropriate method on the listener based on this events ID.
     */
    public void dispatchTo(Object listener) {
        ZMouseMotionListener mouseMotionListener = (ZMouseMotionListener) listener;
        switch (getID()) {
            case ZMouseEvent.MOUSE_DRAGGED:
                mouseMotionListener.mouseDragged(this);
                break;
            case ZMouseEvent.MOUSE_MOVED:
                mouseMotionListener.mouseMoved(this);
                break;
            default:
                throw new RuntimeException("ZMouseMotionEvent with bad ID");
        }
    }

    /**
     * Returns the ZMouseMotionLister class.
     */
    public Class getListenerType() {
        return ZMouseMotionListener.class;
    }
}