/**
 * The jazz.event package defines event handlers to work in coordination
 * with jazz.scenegraph.
 *
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * <b>ZEventHandler</b> is an abstract class for defining behaviors in Jazz.
 * It is really just a utility class to make it easier to group sets of
 * event handlers that work together to define a behavior.  This defines
 * event listeners for mouse and keyboard events so that a sub-class can over-ride the
 * ones it wants to define.  Then, the entire set of handles can be temporarily
 * turned on or off with calls to <code>activate</code> or <code>deactivate</code>.
 * This functionality is specifically designed for mode-driven applications so that
 * one mode might draw while another follows hyperlinks.  One event handler can
 * be defined for each mode, and then they just need to be activated and deactivated
 * as needed.
 *
 * @author  Benjamin B. Bederson
 * @see     edu.umd.cs.jazz.scenegraph.ZSurface
 */
public abstract class ZEventHandler {

    protected boolean activated = false;
    protected Component component;
    protected ZSurface surface;
    protected KeyAdapter keyAdapter;
    protected MouseAdapter mouseAdapter;
    protected MouseMotionAdapter mouseMotionAdapter;

    /**
     * Constructs a new ZEventHandler.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The surface that is panned
     */
    public ZEventHandler(Component c, ZSurface v) {
	component = c;
	surface = v;

        keyAdapter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                ZEventHandler.this.keyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
                ZEventHandler.this.keyReleased(e);
            }

            public void keyTyped(KeyEvent e) {
                ZEventHandler.this.keyTyped(e);
            }
        };

        mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                ZEventHandler.this.mousePressed(e);
            }

            public void mouseReleased(MouseEvent e) {
                ZEventHandler.this.mouseReleased(e);
            }
        };

        mouseMotionAdapter = new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                ZEventHandler.this.mouseMoved(e);
            }
            public void mouseDragged(MouseEvent e) {
                ZEventHandler.this.mouseDragged(e);
            }
        };
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Activates this event handler.
     * @see #deactivate()
     */
    public void activate() {
	if (!activated) {
	    component.addKeyListener(keyAdapter);
	    component.addMouseListener(mouseAdapter);
	    component.addMouseMotionListener(mouseMotionAdapter);
	    activated = true;
	}
    }

    /**
     * Deactivates this event handler.
     * @see #activate()
     */
    public void deactivate() {
	if (activated) {
	    component.removeKeyListener(keyAdapter);
	    component.removeMouseListener(mouseAdapter);
	    component.removeMouseMotionListener(mouseMotionAdapter);
	    activated = false;
	}
    }

    /**
     * Returns the <code>camera</code> that this event handler is associated with.
     * @return The camera.
     */
    public ZCamera getCamera() {
        return surface.getCamera();
    }

    /**
     * Returns the <code>surface</code> that this event handler is associated with.
     * @return The surface.
     */
    public ZSurface getSurface() {
        return surface;
    }

    /**
     * Returns the <code>component</code> that this event handler is associated with.
     * @return The component.
     */
    public Component getComponent() {
        return component;
    }
}

