/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZFadeGroup</b> is a group node that has control over transparency of itself (and sub-tree),
 * and its minimum and maximum magnification.
 *
 * @author Ben Bederson
 */
public class ZFadeGroup extends ZGroup implements ZSerializable, Serializable {
				// Default values
    static public final float   alpha_DEFAULT = 1;
    static public final float   minMag_DEFAULT = 0.0f;
    static public final float   maxMag_DEFAULT = 1000.0f;

    static private final int    NUM_ALPHA_LEVELS = 16;    // The number of pre-allocated alpha levels
    static private final float  MIN_FADE_RANGE = 1.3f;    // The range over which objects fade to minimum magnification
    static private final float  MAX_FADE_RANGE = 0.7f;    // The range over which objects fade to maximum magnification

    static private Composite alphas[] = null;             // The pre-allocated alpha levels

    /**
     * The alpha value that will be applied to the transparency (multiplicitively) of the graphics
     * context during render.
     */
    private float alpha = alpha_DEFAULT;

    /**
     * The minimum magnification that this node gets rendered at.
     */
    private float minMag = minMag_DEFAULT;

    /**
     * The maximum magnification that this node gets rendered at.
     */
    private float maxMag = maxMag_DEFAULT;

    static {
				// Allocate the pre-computed alpha composites the first time a fade group is used.
	float value;
	alphas = new Composite[NUM_ALPHA_LEVELS];

	for (int i=0; i<NUM_ALPHA_LEVELS; i++) {
	    value = (float)i / (NUM_ALPHA_LEVELS - 1);
	    alphas[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, value);
	}
    }

    //****************************************************************************
    //
    //                  Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new empty fade group node.
     */
    public ZFadeGroup() { }

    /**
     * Constructs a new fade group node with the specified node as a child of the
     * new group.
     * @param child Child of the new group node.
     */
    public ZFadeGroup(ZNode child) {
	super(child);
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
    public void duplicateObject(ZFadeGroup refNode) {
	super.duplicateObject(refNode);

	alpha = refNode.alpha;
	minMag = refNode.minMag;
	maxMag = refNode.maxMag;
    }

    /**
     * Duplicates the current node by using the copy constructor.
     * The portion of the reference node that is duplicated is that necessary to reuse the node
     * in a new place within the scenegraph, but the new node is not inserted into any scenegraph.
     * The node must be attached to a live scenegraph (a scenegraph that is currently visible)
     * or be registered with a camera directly in order for it to be visible.
     * <P>
     * In particular, the visual component associated with this group gets duplicated
     * along with the subtree.
     *
     * @return A copy of this node.
     * @see #updateObjectReferences
     */
    public Object clone() {
	ZFadeGroup copy;

	objRefTable.reset();
	copy = new ZFadeGroup();
	copy.duplicateObject(this);
	objRefTable.addObject(this, copy);
	objRefTable.updateObjectReferences();

	return copy;
    }

    //****************************************************************************
    //
    // Get/Set pairs
    //
    //***************************************************************************

    /**
     * Set the alpha value (opacity) for this node.  Alpha values are applied
     * multiplicitively with the current alpha values at render time.
     * @return The alpha value for this node.
     */
    public float getAlpha() {
	return alpha;
    }

    /**
     * Set the alpha value (opacity) for this node.  Alpha values are applied
     * multiplicitively with the current alpha values at render time.
     * Alpha valus range from 0 to 1 where 0 represents a node being completely
     * transparent, and 1 represents a node being completely opaque.  Alpha
     * is clamped to the range [0, 1].
     *
     * @param alpha The new alpha value for this node.
     */
    public void setAlpha(float alpha) {
	if (this.alpha != alpha) {
	    if (alpha < 0.0f) {
		alpha = 0.0f;
	    } else if (alpha > 1.0f) {
		alpha = 1.0f;
	    }
	    this.alpha = alpha;
	    repaint();
	}
    }

    /**
     * Get the minimumn magnification for this node.
     * @return The minimum magnification of this node
     */
    public float getMinMag() {
	return minMag;
    }

    /**
     * Set the minimumn magnification for this node.  This node will not be rendered
     * when the current rendering camera's magnification is less than then specified
     * minimum magnification for this node.  The node with smoothly fade from its
     * regular opacity to completely transparent during a small magnification range
     * just before the minimum magnification is reached.  This transparency control
     * affects the entire sub-tree rooted at this node.  The minimum magnification
     * has a minimum value of 0, and is clamped to that value.
     *
     * @param minMag The new minimumn magnification for this node.
     */
    public void setMinMag(float minMag) {
	if (minMag < 0.0f) {
	    minMag = 0.0f;
	}
	this.minMag = minMag;
	repaint();
    }

    /**
     * Get the maximum magnification for this node.
     * @return The maximum magnification of this node
     */
    public float getMaxMag() {
	return maxMag;
    }

    /**
     * Set the maximumn magnification for this node.  This node will not be rendered
     * when the current rendering camera's magnification is greater than then specified
     * maximum magnification for this node.  The node with smoothly fade from its
     * regular opacity to completely transparent during a small magnification range
     * just before the maximum magnification is reached.  This transparency control
     * affects the entire sub-tree rooted at this node.  To disable the maximum magnification
     * feature, set the value to any negative value.
     *
     * @param maxMag The new maximumn magnification for this node.
     */
    public void setMaxMag(float maxMag) {
	this.maxMag = maxMag;
	repaint();
    }


    //****************************************************************************
    //
    // Painting related methods
    //
    //***************************************************************************

    /**
     * Determines if this fade node is visible at the specified magnification.
     * @param mag The magnification to check at
     * @return True if this node is visible at the specified magnification.
     */
    public boolean isVisible(float mag) {
	boolean visible;

	if ((alpha == 0.0f) ||
	    (mag < minMag) ||
	    ((maxMag >= 0) && (mag > maxMag))) {

	    visible = false;
	} else {
	    visible = true;
	}

	return visible;
    }

    /**
     * Internal method to compute and access an alpha Composite given the current rendering
     * composite, and the current magnification.  It determines the alpha based
     * on this node's alpha value, minimum magnification, and maximum magnification.
     * @param currentComposite The composite in the current render context
     * @param currentMag The magnification of the current rendering camera
     * @return Composite The Composite to use to render this node
     */
    protected Composite getComposite(Composite currentComposite, float currentMag) {
	float newAlpha = alpha;
	float maxMagFadeStart;
	float minMagFadeStart;

				// Assume that there is NO overlap between min and max mag fading...
				// beware the assignment inside the first clause of both if conditions
	if ((maxMag >= 0) && (currentMag > (maxMagFadeStart = maxMag * MAX_FADE_RANGE))) {

	    newAlpha *= (maxMag - currentMag)  / (maxMag - maxMagFadeStart);

	} else if ((currentMag < (minMagFadeStart = minMag * MIN_FADE_RANGE))) {

	    newAlpha *= (currentMag - minMag)  / (minMagFadeStart - minMag);
	}

	if ((currentComposite != null) &&
	    (currentComposite instanceof AlphaComposite)) {

	    newAlpha *= ((AlphaComposite)currentComposite).getAlpha();
	}

	if (newAlpha == 1.0f) {
	    return currentComposite;
	} else {
	    return alphas[(int)(newAlpha * NUM_ALPHA_LEVELS)];
	}
    }

    /**
     * Renders this node which results in the node's visual component getting rendered,
     * followed by its children getting rendered.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param renderContext The graphics context to use for rendering.
     */
    public void render(ZRenderContext renderContext) {
	Graphics2D      g2 = renderContext.getGraphics2D();
	Composite       saveComposite = g2.getComposite();
	float           currentMag = renderContext.getCameraMagnification();

				// Don't paint nodes that are invisible, too big or too small
	if (!isVisible(currentMag)) {
	    return;
	}

				// Apply transparency for this node
	g2.setComposite(getComposite(saveComposite, currentMag));
				// Render node
	super.render(renderContext);
				// Restore graphics state
	g2.setComposite(saveComposite);
    }

    //****************************************************************************
    //
    //			Other Methods
    //
    //****************************************************************************

    /**
     * Returns the first object under the specified rectangle (if there is one)
     * in the subtree rooted with this as searched in reverse (front-to-back) order.
     * This performs a depth-first search, first picking children.
     * Only returns a node if this is "pickable".
     * If no nodes in the sub-tree are picked, then this node's visual
     * component is picked.
     * <P>
     * If childrenPickable is false, then this will never return a child as the picked node.
     * Instead, this node will be returned if any children are picked, or if this node's
     * visual component is picked.  Else, it will return null.
     * @param rect Coordinates of pick rectangle in local coordinates
     * @param mag The magnification of the camera being picked within.
     * @return The picked node, or null if none
     * @see ZDrawingSurface#pick(int, int);
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
				// Don't pick nodes that are invisible, too big or too small
	if (!isVisible((float)ZTransformGroup.computeScale(path.getTransform()))) {
	    return false;
	}
				// Use super's pick method
	return super.pick(rect, path);
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
	super.writeObject(out);

	if (alpha != alpha_DEFAULT) {
	    out.writeState("float", "alpha", alpha);
	}
	if (minMag != minMag_DEFAULT) {
	    out.writeState("float", "minMag", minMag);
	}
	if (maxMag != maxMag_DEFAULT) {
	    out.writeState("float", "maxMag", maxMag);
	}
    }

    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	super.writeObjectRecurse(out);
    }

    /**
     * Set some state of this object as it gets read back in.
     * After the object is created with its default no-arg constructor,
     * this method will be called on the object once for each bit of state
     * that was written out through calls to ZObjectOutputStream.writeState()
     * within the writeObject method.
     * @param fieldType The fully qualified type of the field
     * @param fieldName The name of the field
     * @param fieldValue The value of the field
     */
    public void setState(String fieldType, String fieldName, Object fieldValue) {
	super.setState(fieldType, fieldName, fieldValue);

	if (fieldName.compareTo("alpha") == 0) {
	    alpha = ((Float)fieldValue).floatValue();
	} else if (fieldName.compareTo("minMag") == 0) {
	    minMag = ((Float)fieldValue).floatValue();
	} else if (fieldName.compareTo("maxMag") == 0) {
	    maxMag = ((Float)fieldValue).floatValue();
	}
    }
}
