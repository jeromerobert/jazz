/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * <b>ZSelectionGroup</b> is a group node that represents the selection of
 * a node.
 * It shows it is selected by surrounding its children with a
 * 1 pixel wide line.
 * It provides utility methods of selecting, unselected nodes, and
 * for determining the selected nodes in a sub-tree.
 *
 * @author Ben Bederson
 */
public class ZSelectionGroup extends ZVisualGroup implements ZSerializable, Serializable {
				// Default values
    static public final Color penColor_DEFAULT = Color.magenta;

    /**
     * Pen color for rendering of selection
     */
    private Color     penColor  = penColor_DEFAULT;

    //****************************************************************************
    //
    //                 Inner Classes
    //
    //***************************************************************************


    //
    // Internal class used to render selection as a rectangle.
    //
    static class SelectionRect extends ZVisualComponent {

    	public SelectionRect() { }

	public Object clone() {
	    SelectionRect copy = new SelectionRect();

	    objRefTable.reset();
	    copy.duplicateObject(this);
	    objRefTable.addObject(this, copy);
	    objRefTable.updateObjectReferences();

	    return copy;
	}

	public boolean pick(Rectangle2D pickRect, ZSceneGraphPath path) {
	    return false; // pick handled by SelectionGroup
	}

	public void render(ZRenderContext ctx) {
	    Graphics2D g2 = ctx.getGraphics2D();
	    double mag = ctx.getCompositeMagnification();
	    double sz = 1.0 / mag;
	    ZNode p = parents[0];
	    if (p instanceof ZSelectionGroup) {
		ZSelectionGroup g = (ZSelectionGroup)p;
		Rectangle2D r = g.getBounds();
		double x = r.getX();
		double y = r.getY();
		double w = r.getWidth();
		double h = r.getHeight();

                // don't draw very small selection objects
	        if (w * mag < 2 || h * mag < 2) return;

		// shrink bounds by 1 pixel to ensure I am
		// inside them
                r.setRect(x + sz, y + sz, w - sz*2, h - sz*2);

	        g2.setStroke(new BasicStroke((float)sz));
		g2.setColor(g.getPenColor());
		g2.draw(r);
	    }
        }

	// SelectionRect's have no logical bounds.
	protected void computeBounds() {
	}
    }

    //
    // Internal class used to find the children of selection nodes.
    //
    private static class SelectionFilter implements ZFindFilter {
        public boolean accept(ZNode node) {
	    if ((node instanceof ZGroup) && (((ZGroup)node).hasOneChild())) {
	        return false;
	    } else {
	        return ZSelectionGroup.isSelected(node);
	    }
        }

        public boolean childrenFindable(ZNode node) {
	    return true;
        }
    }


    //****************************************************************************
    //
    //                 Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new ZSelectionGroup.  The node must be attached to a live scenegraph (a scenegraph that is
     * currently visible) order for it to be visible.
     */
    public ZSelectionGroup () {
	setFrontVisualComponentPickable(false);
    	setFrontVisualComponent(createSelectComponent());
    }

    /**
     * Constructs a new select group node with the specified node as a child of the
     * new group.
     * @param child Child of the new group node.
     */
    public ZSelectionGroup(ZNode child) {
	this();
	addChild(child);
    }

    /**
     * Copies all object information from the reference object into the current
     * object. This method is called from the clone method.
     * All ZSceneGraphObjects objects contained by the object being duplicated
     * are duplicated, except parents which are set to null.  This results
     * in the sub-tree rooted at this object being duplicated.
     *
     * @param refNode The reference node to copy
     */
    public void duplicateObject(ZSelectionGroup refNode) {
	super.duplicateObject(refNode);

	penColor = refNode.penColor;
    }


    /**
     * Duplicates the current node by using the copy constructor.
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     *
     * @return A copy of this node.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZSelectionGroup copy;

	objRefTable.reset();
	copy = new ZSelectionGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }


    //****************************************************************************
    //
    // Static convenience methods to manage selection
    //
    //***************************************************************************

    /**
     * Return a list of the selected nodes in the subtree rooted
     * at the specified node (including the root if it is selected).
     * Note that the nodes returned are the nodes at the bottom of
     * the "decorator chain".  See @link{ZSceneGraphEditor} for
     * more information.
     * @param node The subtree to check for selection
     * @return The list of selected nodes.
     */
    static public ArrayList getSelectedNodes(ZNode node) {
	ArrayList selection = new ArrayList();
	node.findNodes(new SelectionFilter(), selection);

	return selection;
    }

    /**
     * Return a list of the selected nodes in the portion of the
     * scenegraph visible from the specified camera.
     * Note that the nodes returned are the nodes at the bottom of
     * the "decorator chain".  See @link{ZSceneGraphEditor} for
     * more information.
     * @param camera The camera to look through for selected nodes.
     * @return The list of selected nodes.
     */
    static public ArrayList getSelectedNodes(ZCamera camera) {
	ArrayList selection = new ArrayList();
	ZLayerGroup[] layers = camera.getLayers();
	for (int i=0; i<layers.length; i++) {
	    layers[i].findNodes(new SelectionFilter(), selection);
	}

	return selection;
    }

    /**
     * Select the specified node.
     * If the node is already selected, then do nothing.
     * This manages the selection as a decorator as described in @link{ZSceneGraphEditor}.
     * @param node the node to select
     * @return the ZSelectionGroup that represents the selection
     */
    static public ZSelectionGroup select(ZNode node) {
	return node.editor().getSelectionGroup();
    }

    /**
     * Unselect the specified node.
     * If the node is not already selected, then do nothing.
     * This manages the selection as a decorator as described in @link{ZNode}.
     * @param node the node to unselect
     */
    static public void unselect(ZNode node) {
	node.editor().removeSelectionGroup();
    }

    /**
     * Unselect all currently selected nodes in the subtree rooted
     * at the specified node (including the root if it is selected).
     * This manages the selection as a decorator as described in @link{ZNode}.
     * @param node The subtree to check for selection
     */
    static public void unselectAll(ZNode node) {
	ArrayList selection = getSelectedNodes(node);
	for (Iterator i=selection.iterator(); i.hasNext();) {
	    node = (ZNode)i.next();
	    unselect(node);
	}
    }

    /**
     * Unselect all currently selected nodes in the portion of the
     * scenegraph visible from the specified camera.
     * This manages the selection as a decorator as described in @link{ZNode}.
     * @param camera The camera to look through for selected nodes.
     */
    static public void unselectAll(ZCamera camera) {
	ZNode node;
	ArrayList selection = getSelectedNodes(camera);
	for (Iterator i=selection.iterator(); i.hasNext();) {
	    node = (ZNode)i.next();
	    unselect(node);
	}
    }

    /**
     * Determine if the specified node is selected.
     * @return true if the node is selected.
     */
    static public boolean isSelected(ZNode node) {
	if (node.editor().hasSelectionGroup()) {
	    return true;
	} else {
	    return false;
	}
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Get the pen color that is used to render the selection.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color that is used to render the selection.
     * @param color the pen color, or null if none.
     */
    public void setPenColor(Color color) {
	penColor = color;
	repaint();
    }


    //****************************************************************************
    //
    // Painting related methods
    //
    //***************************************************************************


    //****************************************************************************
    //
    //                 Other Methods
    //
    //****************************************************************************

    /**
     * Internal method to create the visual component
     * that represents the selection.  Applications can
     * change visual representation of a selected object
     * by extending this class, and overriding this method.
     * @return the visual component that represents the selection.
     */
    protected ZVisualComponent createSelectComponent() {
	return new SelectionRect();
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
				// Selection is tricky to write out because, it defines
				// a custom visual component which is a private class - 
				// and private classes are not ZSerializable.  Because
				// the class is referenced from this class's superclass
				// (ZVisualComponent), we can not just make the reference
				// transient so it doesn't get written out.  Instead,
				// we do this trick here of setting that object reference
				// to null before we write out this selection, and then
				// restore it when we are finished.
	ZVisualComponent vc = getFrontVisualComponent();
	setFrontVisualComponent(null);
	super.writeObject(out);
	setFrontVisualComponent(vc);
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	ZVisualComponent vc = getFrontVisualComponent();
	setFrontVisualComponent(null);
	super.writeObjectRecurse(out);
	setFrontVisualComponent(vc);
    }
}

