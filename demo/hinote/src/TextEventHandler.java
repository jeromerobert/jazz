/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>TextEventHandler</b> is a simple event handler for interactively typing text.
 *
 * @author  Benjamin B. Bederson
 */
public class TextEventHandler implements ZEventHandler, ZMouseListener, KeyListener {
    private boolean active = false;       // True when event handlers are attached to a node
    private ZNode   node = null;          // The node the event handlers are attached to
    private ZCanvas canvas = null;        // The canvas this event handler is associated with

    private HiNoteCore hinote;
    private ZVisualLeaf textNode;
                                                    // Mask out mouse and mouse/key chords
    private int            all_button_mask   = (MouseEvent.BUTTON1_MASK |
                                                MouseEvent.BUTTON2_MASK |
                                                MouseEvent.BUTTON3_MASK |
                                                MouseEvent.ALT_GRAPH_MASK |
                                                MouseEvent.CTRL_MASK |
                                                MouseEvent.META_MASK |
                                                MouseEvent.SHIFT_MASK |
                                                MouseEvent.ALT_MASK);

    public TextEventHandler(HiNoteCore hinote, ZNode node, ZCanvas canvas) {
        this.hinote = hinote;
        this.node = node;
        this.canvas = canvas;
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
            canvas.removeKeyListener(this);

            stopEditingText();
        } else if (!this.active && active) {
                                // Turn on event handlers
            this.active = true;
            node.addMouseListener(this);
            canvas.addKeyListener(this);
            canvas.requestFocus();
        }
    }

    /**
     * Determines if this event handler is active.
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }

    public void stopEditingText() {
        if (textNode != null) {
            ZGroup layer = hinote.getDrawingLayer();
            ZText textComp = (ZText)textNode.getFirstVisualComponent();
            hinote.unselect(textNode);
            textComp.setEditable(false);
            if (textComp.getText().length() == 0) {
                ZNode handle = textNode.editor().getTop();
                handle.getParent().removeChild(handle);
            }
            textNode = null;
        }
    }

    public void mousePressed(ZMouseEvent e) {
        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            ZSceneGraphPath path = e.getPath();
            canvas.requestFocus();

            ZNode node;
            ZDrawingSurface surface = canvas.getDrawingSurface();
            ZCamera camera = hinote.getCamera();
            ZGroup layer = hinote.getDrawingLayer();
            Point2D ptGlobal = new Point2D.Double();
            ZText textComp = null;

                                // Get current text component if one
            if (textNode != null) {
                textComp = (ZText)textNode.getFirstVisualComponent();
            }

                                // Pick object under cursor, and convert cursor point to local coords
            node = surface.pick(e.getX(), e.getY()).getNode();
            ptGlobal.setLocation(e.getX(), e.getY());
            path.screenToGlobal(ptGlobal);

                                // If clicked on current text, set cursor position and return
            if ((textNode != null) &&
                (node == textNode)) {
                textNode.globalToLocal(ptGlobal);
                textComp.setCaretPos(ptGlobal);
                textNode.localToGlobal(ptGlobal);
                return;
            }

                                // Unselect previously active text, and delete it if empty
            if (textNode != null) {
                stopEditingText();
            }

                                // If clicked on some other text, make that active
            if ((node != null) && (node instanceof ZVisualLeaf)) {
                ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
                if (vc instanceof ZText) {
                    textNode = (ZVisualLeaf)node;
                    textComp = (ZText)vc;
                    textNode.globalToLocal(ptGlobal);
                    textComp.setCaretPos(ptGlobal);
                    textNode.localToGlobal(ptGlobal);
                }
            }
                                // Else, create a new text object
            if (textNode == null) {
                                // Set default font to Arial if it exists
                double mag = camera.getMagnification();
                double fontHeight = 40.0 / mag;
                textComp = new ZText("");
                textComp.setFont(hinote.font);
                textComp.setPenColor(hinote.penColor);
                textComp.setGreekThreshold(15);
                textNode = new ZVisualLeaf(textComp);
                ZTransformGroup transform = new ZTransformGroup();
                transform.setHasOneChild(true);
                transform.addChild(textNode);
                transform.scale(1.0 / mag);
                transform.setTranslation(ptGlobal.getX(), (ptGlobal.getY() - 0.5*fontHeight));
                layer.addChild(transform);
            }
            hinote.select(textNode);    // Select the newly active text
            textComp.setEditable(true);
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

    public void keyPressed(KeyEvent e) {
        ZDrawingSurface surface = hinote.getDrawingSurface();

        if (textNode != null) {
            ZText textComp = (ZText)textNode.getFirstVisualComponent();
            textComp.keyPressed(e);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}