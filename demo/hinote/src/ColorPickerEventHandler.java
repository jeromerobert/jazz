/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ColorPickerEventHandler</b> is a simple event handler for interactively picking colors..
 *
 * @author  Jesse Grosjean
 */
public class ColorPickerEventHandler implements ZEventHandler, ZMouseListener, ZMouseMotionListener {
    private boolean active = false;        // True when event handlers are attached to a node
    private ZNode   node = null;           // The node the event handlers are attached to

    private HiNoteCore hinote;


                                                    // Mask out mouse and mouse/key chords
    private int            all_button_mask   = (MouseEvent.BUTTON1_MASK |
                                                MouseEvent.BUTTON2_MASK |
                                                MouseEvent.BUTTON3_MASK |
                                                MouseEvent.ALT_GRAPH_MASK |
                                                MouseEvent.CTRL_MASK |
                                                MouseEvent.META_MASK |
                                                MouseEvent.SHIFT_MASK |
                                                MouseEvent.ALT_MASK);

    public ColorPickerEventHandler(HiNoteCore hinote, ZNode node) {
        this.hinote = hinote;
        this.node = node;
    }

    /**
     * Specifies whether this event handler is active or not.
     * @param active True to make this event handler active
     */
    public void setActive(boolean active) {
        if (this.active && !active) {
                                // Turn off event handlers
            this.active = false;
            node.removeMouseListener(this);
            node.removeMouseMotionListener(this);
        } else if (!this.active && active) {
                                // Turn on event handlers
            this.active = true;
            node.addMouseListener(this);
            node.addMouseMotionListener(this);
        }
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }

    public void mousePressed(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            ZSceneGraphObject pick = path.getObject();

            Paint fillColor = hinote.colorComponent.getFillColor();
            Paint penColor = hinote.colorComponent.getPenColor();

            if (pick instanceof ZFillPaint) {
                fillColor = ((ZFillPaint)pick).getFillPaint();
            }
            if (pick instanceof ZPenPaint) {
                penColor = ((ZPenPaint)pick).getPenPaint();
            }

            hinote.updateFillColor((Color)fillColor);
            hinote.updatePenColor((Color)penColor);
        }
    }

    public void mouseDragged(ZMouseEvent e) {
    }

    public void mouseReleased(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(ZMouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been moved on a node
     * (with no buttons no down).
     */
    public void mouseMoved(ZMouseEvent e) {
    }
}