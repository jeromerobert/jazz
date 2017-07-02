/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;

/**
 * <b>ZNavEventHandlerKeyBoard</b> provides event handlers for basic zooming
 * and panning of a Jazz camera with the keyboard.  Applications can define which keys
 * are used for navigation, and how much each key moves the camera.
 * <p>
 * The camera is changed a little bit with each keypress.
 * If a key is held down so it auto-repeats, that is detected, and the camera
 * will then be continuously moved in until the key is released, or another
 * key is pressed at which point it will return to the original behavior
 * of one increment per key press.
 * <p>
 * The default parameters are:
 *   PageUp zooms in
 *   PageDown zooms out
 *   Arrow keys pan
 *   Each keypress zooms in 25%, or pans 25%
 *   Each camera change is animated over 250 milliseconds
 *   The camera is zoomed around the cursor
 *
 * @author  Benjamin B. Bederson
 */
public class ZNavEventHandlerKeyBoard extends ZEventHandler {
    protected boolean autoNav = false;      // True while this autonav is on
    protected float   scaleDelta = 1.25f;   // Magnification factor by for incremental zooms
    protected float   panDelta = 25;        // Number of pixels to pan for incremental pans
    protected float   autoPanXDelta;        // Amount to pan in X by for auto-nav
    protected float   autoPanYDelta;        // Amount to pan in Y by for auto-nav
    protected float   autoZoomDelta;        // Amount to zoom by for auto-nav
    protected Point2D pointerPosition;      // Event coords of current mouse position (in screen space)
    protected int     animTime = 250;       // Time for animated zooms in milliseconds
    protected int     zoomInKey   = KeyEvent.VK_PAGE_UP;    // Key that zooms in a bit
    protected int     zoomOutKey  = KeyEvent.VK_PAGE_DOWN;  // Key that zooms out a bit
    protected int     panLeftKey  = KeyEvent.VK_RIGHT;      // Key that pans to the left
    protected int     panRightKey = KeyEvent.VK_LEFT;       // Key that pans to the right
    protected int     panUpKey    = KeyEvent.VK_DOWN;       // Key that pans to the up
    protected int     panDownKey  = KeyEvent.VK_UP;         // Key that pans to the down
    protected int     homeKey     = KeyEvent.VK_HOME;       // Key that navigates to home
    protected int     prevKeyPress = 0;     // The previous key pressed (or 0 if previous key event not a press)
    protected int     delay = 20;           // Delay (in milliseconds) between auto-nav increments
    

    /**
     * Constructs a new ZNavEventHandlerKeyBoard.
     * @param <code>c</code> The component that this event handler listens to events on
     * @param <code>v</code> The surface that is panned
     */
    public ZNavEventHandlerKeyBoard(Component c, ZSurface v) {
	super(c, v);
	c.requestFocus();
	pointerPosition = new Point2D.Float();
    }

    /**
     * Define the keys that are used to zoom.  
     * A key can be set to 0 to disable that function.
     * @param <code>inKey</code> The keycode of the key that should trigger zoom in events.
     * @param <code>outKey</code> The keycode of the key that should trigger zoom out events.
     */
    public void setZoomKeys(int inKey, int outKey) {
	zoomInKey = inKey;
	zoomOutKey = outKey;
    }

    /**
     * Define the keys that are used to pan.
     * A key can be set to 0 to disable that function.
     * @param <code>leftKey</code> The keycode of the key that should trigger pan left events.
     * @param <code>rightKey</code> The keycode of the key that should trigger pan right events.
     * @param <code>upKey</code> The keycode of the key that should trigger pan up events.
     * @param <code>downKey</code> The keycode of the key that should trigger pan down events.
     */
    public void setPanKeys(int leftKey, int rightKey, int upKey, int downKey) {
	panLeftKey = leftKey;
	panRightKey = rightKey;
	panUpKey = upKey;
	panDownKey = downKey;
    }

    /**
     * Define the key that is used to home.  
     * A key can be set to 0 to disable that function.
     * @param <code>homeKey</code> The keycode of the key that should trigger the home event.
     */
    public void setHomeKey(int homeKey) {
	this.homeKey = homeKey;
    }

    /**
     * Key press event handler
     * @param <code>e</code> The event.
     */
    public void keyPressed(KeyEvent e) {
	// System.out.println("Key press: " + e);

	float delta = 0.0f;
	float panX = 0.0f;
	float panY = 0.0f;
	boolean pan  = false;
	boolean zoom = false;
	int keyCode = e.getKeyCode();
	ZCamera camera = getCamera();

				// Detect auto key-repeat
				// If we get two of the same key presses in a row without
				// an intervening key release, then that means the keyboard
				// auto-repeat function is activated and we should queue
				// an event for continuous navigation which we will stop as soon
				// the key release arrives.
	if (keyCode == prevKeyPress) {
	    if (!isAutoNav()) {
				// Set amount to zoom for this increment
		if (keyCode == zoomInKey) {
		    autoZoomDelta = 1.0f + 0.3f * (scaleDelta - 1.0f);
		    zoom = true;
		} else if (keyCode == zoomOutKey) {
		    autoZoomDelta = 1.0f / (1.0f + 0.3f * (scaleDelta - 1.0f));
		    zoom = true;
		} else if (keyCode == panLeftKey) {
		    autoPanXDelta = (1.0f * panDelta) / camera.getMagnification();
		    autoPanYDelta = 0.0f;
		    pan = true;
		} else if (keyCode == panRightKey) {
		    autoPanXDelta = (-1.0f * panDelta) / camera.getMagnification();
		    autoPanYDelta = 0.0f;
		    pan = true;
		} else if (keyCode == panUpKey) {
		    autoPanXDelta = 0.0f;
		    autoPanYDelta = (1.0f * panDelta) / camera.getMagnification();
		    pan = true;
		} else if (keyCode == panDownKey) {
		    autoPanXDelta = 0.0f;
		    autoPanYDelta = (-1.0f * panDelta) / camera.getMagnification();
		    pan = true;
		}
		if (zoom || pan) {
		    getSurface().startInteraction();
		    startAutoNav();
		}
	    }
	} else {
				// No auto key-repeat, just zoom a bit
				// Stop auto-nav if it was going
	    if (isAutoNav()) {
		stopAutoNav();
	    }

				// Set amount to zoom for this increment
	    if (keyCode == zoomInKey) {
		delta = scaleDelta;
		zoom = true;
	    } else if (keyCode == zoomOutKey) {
		delta = 1.0f / scaleDelta;
		zoom = true;
	    } else if (keyCode == panLeftKey) {
		panX = (1.0f * panDelta) / camera.getMagnification();
		panY = 0.0f;
		pan = true;
	    } else if (keyCode == panRightKey) {
		panX = (-1.0f * panDelta) / camera.getMagnification();
		panY = 0.0f;
		pan = true;
	    } else if (keyCode == panUpKey) {
		panX = 0.0f;
		panY = (1.0f * panDelta) / camera.getMagnification();
		pan = true;
	    } else if (keyCode == panDownKey) {
		panX = 0.0f;
		panY = (-1.0f * panDelta) / camera.getMagnification();
		pan = true;
	    } else if (keyCode == homeKey) {
		AffineTransform tf = new AffineTransform();
		camera.getViewTransform().animate(tf, animTime, getSurface());
	    }

	    if (zoom || pan) {
		ZTransform tx = camera.getViewTransform();
		getSurface().startInteraction();
		if (zoom) {
		    Point2D pt = new Point2D.Double(pointerPosition.getX(), pointerPosition.getY());
		    camera.getInverseViewTransform().transform(pt, pt);
		    tx.scale(delta, (float)pt.getX(), (float)pt.getY(), animTime, getSurface());
		} else {
		    tx.translate(panX, panY, animTime, getSurface());
		}
		getSurface().endInteraction();
	    }
	}	    
	prevKeyPress = keyCode;
    }

    /**
     * Key release event handler
     * @param <code>e</code> The event.
     */
    public void keyReleased(KeyEvent e) {
	// System.out.println("Key release: " + e);

	prevKeyPress = 0;
	autoZoomDelta = 0.0f;
	autoPanXDelta = 0;
	autoPanYDelta = 0;
				// Stop auto-zoom if it was going
	if (isAutoNav()) {
	    stopAutoNav();
	    getSurface().endInteraction();
	}
    }

    /**
     * Watch mouse motion so we always know where the mouse is.
     * We can use this info to zoom around the mouse position.
     */
    public void mouseMoved(MouseEvent e) {
	pointerPosition.setLocation(e.getX(), e.getY());
    }
    public void mouseDragged(MouseEvent e) {
	pointerPosition.setLocation(e.getX(), e.getY());
    }

    /**
     * Return true if currently auto-zooming
     */
    public boolean isAutoNav() {
	return autoNav;
    }

    /**
     * Start the auto navigation
     */
    public void startAutoNav() {
	if (!autoNav) {
	    autoNav = true;
	    navOneStep();
	}
    }

    /**
     * Stops the auto navigation
     */
    public void stopAutoNav() {
	autoNav = false;
    }

    /**
     * Implements auto-navigation
     */
    public void navOneStep() {
	if (autoNav) {
	    ZCamera camera = getCamera();
	    
	    if (autoZoomDelta > 0) {
		Point2D pt = new Point2D.Float((float)pointerPosition.getX(), (float)pointerPosition.getY());
		camera.getInverseViewTransform().transform(pt, pt);
		
		camera.getViewTransform().scale(autoZoomDelta, (float)pt.getX(), (float)pt.getY());
		getSurface().restore();
	    }
	    if ((autoPanXDelta != 0) || (autoPanYDelta != 0)) {
		camera.getViewTransform().translate(autoPanXDelta, autoPanYDelta);
		getSurface().restore();
	    }

	    try {
				// The sleep here is necessary.  Otherwise, there won't be
				// time for the primary event thread to get and respond to
				// input events.
		Thread.sleep(20);

				// If the sleep was interrupted, then cancel the zooming,
				// so don't do the next zooming step
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ZNavEventHandlerKeyBoard.this.navOneStep();
		    }
		});
	    } catch (InterruptedException e) {
		autoNav = false;
	    }
	}
    }
}
