/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.io.*;
import java.awt.AWTEvent;
import java.awt.geom.AffineTransform;

import edu.umd.cs.jazz.*;

/**
 * <b>ZCameraEvent</b> is an event which indicates that a camera has changed.
 * <P>
 * Camera events are provided for notification purposes ONLY;
 * Jazz will automatically handle changes to the camera
 * contents internally so that the program works properly regardless of
 * whether the program is receiving these events or not.
 * <P>
 * This event is generated by a ZCamera visual component
 * when a camera's view is changed.
 * The event is passed to every <code>ZCameraListener</code>
 * or <code>ZCameraAdapter</code> object which registered to receive such
 * events using the camera's <code>addCameraListener</code> method.
 * (<code>ZCameraAdapter</code> objects implement the
 * <code>ZCameraListener</code> interface.) Each such listener object
 * gets this <code>ZCameraEvent</code> when the event occurs.
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @see ZCameraAdapter
 * @see ZCameraListener
 * @author Ben Bederson
 */
public class ZCameraEvent extends AWTEvent implements ZEvent, Serializable {

    /**
     * The first number in the range of ids used for camera events.
     */
    public static final int CAMERA_FIRST       = 400;

    /**
     * The last number in the range of ids used for camera events.
     */
    public static final int CAMERA_LAST        = 400;

    /**
     * This event indicates that the camera's view transform changed.
     */
    public static final int CAMERA_VIEW_CHANGED = CAMERA_FIRST;

    /**
     * The value of the view transform before the view transform was changed
     * for view transform events (or null for other event types).
     */
    public AffineTransform viewTransform = null;

    /**
     * Constructs a ZCameraEvent object.
     *
     * @param source    the ZCamera object that originated the event
     * @param id        an integer indicating the type of event
     * @param viewTransform The original transform of the camera (for transform events)
     * @deprecated as of Jazz 1.1, use createViewChangedEvent() instead.
     */
    public ZCameraEvent(ZCamera source, int id, AffineTransform viewTransform) {
        super(source, id);
        this.viewTransform = viewTransform;
    }

    protected ZCameraEvent(ZCamera source, int id, AffineTransform viewTransform, Object dummy) {
        super(source, id);
        this.viewTransform = viewTransform;
    }

    /**
     * Factory method to create a ZCameraEvent with a CAMERA_VIEW_CHANGED ID.
     *
     * @param source    the ZCamera object that originated the event
     * @param viewTransform The original transform of the camera (for transform events)
     */
    public static ZCameraEvent createViewChangedEvent(ZCamera source, AffineTransform viewTransform) {
        return new ZCameraEvent(source, ZCameraEvent.CAMERA_VIEW_CHANGED, viewTransform, null);
    }

    /**
     * Returns the originator of the event.
     *
     * @return the ZCamera object that originated the event
     */
    public ZCamera getCamera() {
        return (ZCamera)source;   // Cast is ok, checked in constructor
    }

    /**
     * For view change events, this returns the value of the view transform
     * before the view transform was changed.
     *
     * @return a clone of the original view transform value.
     */
    public AffineTransform getOrigViewTransform() {
        return (AffineTransform) viewTransform.clone();
    }

    /**
     * Calls appropriate method on the listener based on this events ID.
     */
    public void dispatchTo(Object listener) {
        ZCameraListener cameraListener = (ZCameraListener) listener;
        switch (getID()) {
            case ZCameraEvent.CAMERA_VIEW_CHANGED:
                cameraListener.viewChanged(this);
                break;
            default:
                throw new RuntimeException("ZCameraEvent with bad ID");
        }
    }

    /**
     * Returns the ZCameraLister class.
     */
    public Class getListenerType() {
        return ZCameraListener.class;
    }

    /**
     * Returns true if this event has previously been consumed.
     */
    public boolean isConsumed() {
        return super.isConsumed();
    }

    /**
     * Set the souce of this event. As the event is fired up the tree the source of the
     * event will keep changing to reflect the scenegraph object that is firing the event.
     */
    public void setSource(Object aSource) {
        source = aSource;
    }
}