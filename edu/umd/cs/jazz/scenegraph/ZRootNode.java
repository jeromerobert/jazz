/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.geom.AffineTransform;

import edu.umd.cs.jazz.util.*;

/** 
 * ZRootNode exteneds ZNode overiding several methods of ZNode to ensure that ZRootNode is
 * always in the root position of a Scenegraph.
 * 
 * @author Ben Bederson
 * @author Britt McAlister
 */
public class ZRootNode extends ZNode {

    /**
     * Overrides ZNode.findRoot() to end the traversal. Finds the
     * scenegraph root by returning this object.
     * @return The root node of this scenegraph.
     */
    public ZRootNode findRoot() {
	return this;
    }

    /**
     * Overrides ZNode.setParent() to throw an ZOperationNotAllowedException if an
     * attempt to set the parent of a ZRootNode is made.
     * @param parent parameter is not used.
     */
    protected void setParent(ZNode parent) throws ZOperationNotAllowedException {
	throw new ZOperationNotAllowedException("Can't set parent of ZRootNode");
    }

    /**
     * Overrides ZNode.raiseInternal() to throw an ZOperationNotAllowedException if an
     * attempt to raise the ZRootNode is made.
     */
    protected void raiseInternal() throws ZOperationNotAllowedException {
	throw new ZOperationNotAllowedException("Can't raise ZRootNode");
    }

    /**
     * Overrides ZNode.raiseInternal() to throw an ZOperationNotAllowedException if an
     * attempt to raise the ZRootNode is made.
     * @param afterNode parameter is not used.
     */
    protected void raiseToInternal(ZNode afterNode) throws ZOperationNotAllowedException {
	throw new ZOperationNotAllowedException("Can't raise ZRootNode");
    }

    /**
     * Overrides ZNode.lowerInternal() to throw an ZOperationNotAllowedException if an
     * attempt to lower the ZRootNode is made.
     */
    protected void lowerInternal() throws ZOperationNotAllowedException {
	throw new ZOperationNotAllowedException("Can't lower ZRootNode");
    }

    /**
     * Overrides ZNode.raiseInternal() to throw an ZOperationNotAllowedException if an
     * attempt to lower the ZRootNode is made.
     * @param afterNode parameter is not used.
     */
    protected void lowerToInternal(ZNode afterNode) throws ZOperationNotAllowedException {
	throw new ZOperationNotAllowedException("Can't lower ZRootNode");
    }

    /**
     * Overrides ZNode.computeGlobalCoordinateFrame() to end the traversal. Builds the
     * concatenation of transforms that exist between this node and the
     * rootNode.  In the case of the rootNode, this is just the identity transform.
     * @return A new identity transform.
     */
    public AffineTransform computeGlobalCoordinateFrame() {
	return new AffineTransform();
    }
}
