/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.event.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>PanEventHandler</b> is a simple event handler for panning and following hyperlinks
 *
 * @author  Benjamin B. Bederson
 */
public class PanEventHandler extends ZPanEventHandler {
    private ZCanvas canvas = null;
    private ZNode currentNode = null;
    private ZAnchorGroup link = null;
                                                    // Mask out mouse and mouse/key chords
    private int            all_button_mask   = (MouseEvent.BUTTON1_MASK |
                                                MouseEvent.BUTTON2_MASK |
                                                MouseEvent.BUTTON3_MASK |
                                                MouseEvent.ALT_GRAPH_MASK |
                                                MouseEvent.CTRL_MASK |
                                                MouseEvent.META_MASK |
                                                MouseEvent.SHIFT_MASK |
                                                MouseEvent.ALT_MASK);

    public PanEventHandler(ZNode node, ZCanvas canvas) {
        super(node);
        this.canvas = canvas;
    }

    protected void showLink(ZNode node) {
        ZSceneGraphEditor editor = node.editor();
        if (editor.hasAnchorGroup()) {
            ZAnchorGroup link = editor.getAnchorGroup();
            if (link != null) {
                link.setVisible(true, canvas.getCamera());
                if ((link.getDestNode() != null) || (link.getDestBounds() != null)) {
                    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    canvas.addKeyListener(fFilteredEventDispatcher);
                }
            }
        }
    }

    protected void hideLink(ZNode node) {
        ZSceneGraphEditor editor = node.editor();
        if (editor.hasAnchorGroup()) {
            ZAnchorGroup link = editor.getAnchorGroup();
            if (link != null) {
                link.setVisible(false, null);
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                canvas.removeKeyListener(fFilteredEventDispatcher);
            }
        }
    }

    protected void followLink(ZNode node) {
        ZSceneGraphEditor editor = node.editor();
        if (editor.hasAnchorGroup()) {
            ZAnchorGroup link = editor.getAnchorGroup();
            link.setVisible(false, null);
            canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            link.follow(canvas.getCamera());
        }
    }

    protected void updateCurrentNode(ZMouseEvent e) {
        ZSceneGraphPath path = e.getPath();
        ZNode node = path.getNode();

        if (node != currentNode) {
            if (currentNode != null) {
                hideLink(currentNode);
            }
            if (node != null) {
                showLink(node);
            }
            currentNode = node;
        }
    }

    protected void filteredMouseMoved(ZMouseEvent e) {
        super.filteredMouseMoved(e);

        updateCurrentNode(e);
    }

    protected void filteredMousePressed(ZMouseEvent e) {
        super.filteredMousePressed(e);

        updateCurrentNode(e);
    }

    /**
     * Mouse release event handler
     * @param <code>e</code> The event.
     */
    protected void filteredMouseReleased(ZMouseEvent e) {
        super.filteredMouseReleased(e);

        if ((e.getModifiers() & all_button_mask) == MouseEvent.BUTTON1_MASK) {   // Left button only
            if (!isDragging()) {
                if (currentNode != null) {
                    followLink(currentNode);
                }
            }
        }
    }

    protected void filteredKeyPressed(KeyEvent e) {
                                // if a link is visible, and the delete key is pressed,
                                // delete the link
        if (e.getKeyChar() == KeyEvent.VK_DELETE) {
            if (currentNode != null) {
                ZSceneGraphEditor editor = currentNode.editor();
                if (editor.hasAnchorGroup()) {
                    ZAnchorGroup link = editor.getAnchorGroup();
                    if (link != null) {
                        link.setVisible(false, null);
                        link.extract();
                        currentNode = null;
                    }
                }
            }
        }
    }

    /**
     * Make sure that all links are hidden when the PanEventHandler is deactivated.
     */
    public void setActive(boolean active) {
        if (!active) {
            if (currentNode != null) {
                hideLink(currentNode);
            }
        }
        super.setActive(active);
    }
}