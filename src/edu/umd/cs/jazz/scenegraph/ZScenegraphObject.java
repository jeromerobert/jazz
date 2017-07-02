/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.awt.geom.AffineTransform;

/**
 * <b>ZScenegraphObject</b> is the base class for all objects in the jazz scenegraph.  It
 * has no purpose other than providing a common superclass for ZNode and
 * ZVisualComponent.<br><P>
 * <B> Coordinate Systems </B><br>
 * Application developers should understand the three coordinates systems (global, local, window)
 * that exist in Jazz.  Local bounds are each objects own coordinate system.  It starts at 0,0 and
 * are un-modified by any transformation and encompasses the width and height of the object.
 * Global coorinates are an objects local coordinates modified by any applicable transforms.  There is
 * one set of global coordinates and they may also be referred to as the global space.  The last type of
 * coordinates are those that map directly to a java component.  They originate at the top-left corner and
 * increase as you progress down and to the right.  
 * <pre>
 *                       // Create a basic Jazz scene
 *       JComponent component = buildScene(container);
 *                       // Extract the basic elements of the scenegraph
 *       ZCamera camera = surface.getCamera();
 *       ZNode layer = (ZNode)camera.getPaintStartPoints().firstElement();
 *       ZRectangle rect = new ZRectangle(10, 10, 50, 50);
 *       ZNode node = new ZNode(rect);
 </pre>
 * The rectangle exists in local coordinates at 10, 10.  It's width and height are both 50.  The bounding
 * box of this object in local coordinates is 10, 10, 60, 60.  Since there are no transforms applied to
 * this object, it's global coords are the same as it's local coordinates.  And, since there is no translation
 * applied to the camera, the window coordinates are the same as the global coordinates.  The rectangle will
 * appear on the component at 10, 10 and have a width and height of 50.  <br>
 * <b> NOTE</b> : for this discussion, bounding boxes are expressed as x1, y1, x2, y2
 * <ul>
 * <li> Local coordinates bounding box  = 10, 10, 60, 60
 * <li> Global coordinates bounding box = 10, 10, 60, 60
 * <li> Window coordinates bounding box = 10, 10, 60, 60
 * </ul>
 * <P> Now, if the node's transform is modified by translation:
 * <pre>
 *       node.getTransform().translate(100, 100);</pre>
 * The rectangle's local coordiantes are unchanged.  The local coordinates are transformed by the node's
 * transform to produce the global coordinates.  Since there is still no transformation
 * applied to the camera, the window coordinates are the same as the global coordinates.
 * The rectangle will appear on the component at 110, 110 and have a width and height of 50.
 * <ul>
 * <li> Local coordinates bounding box  = 10, 10, 60, 60
 * <li> Global coordinates bounding box = 110, 110, 160, 160
 * <li> Window coordinates bounding box = 110, 110, 160, 160
 * </ul>
 *
 * <P> Now, if the node's transform is modified by scale:
 * <pre>
 *       node.getTransform().scale(2);</pre>
 * The rectangle's local coordiantes are unchanged.  The local coordinates are transformed by the node's
 * transform to produce the global coordinates (scale first, then translated).  Since there is still
 * no transformation applied to the camera, the window coordinates are the same as the global coordinates.
 * The rectangle will appear on the component at 120, 120 and have a width and height of 100.
 * <ul>
 * <li> Local coordinates bounding box  = 10, 10, 60, 60
 * <li> Global coordinates bounding box = 120, 120, 220, 220
 * <li> Window coordinates bounding box = 120, 120, 220, 220
 * </ul>
 *
 * <P> Now, if the camera is translated
 * <pre>
 *       camera.getCamaraTransform().translate(-100, -100);</pre>
 * The rectangle's local coordiantes are unchanged.  The local coordinates are transformed by the node's
 * transform to produce the global coordinates (scale first, then translated).  The window coordinates are
 * procuded by applying the cameras transform to the global coordinates.
 * The rectangle will appear on the component at 120, 120 and have a width and height of 100.
 * <ul>
 * <li> Local coordinates bounding box  = 10, 10, 60, 60
 * <li> Global coordinates bounding box = 20, 20, 120, 120
 * <li> Window coordinates bounding box = 20, 20, 120, 120
 * </ul>
 *
 * @author  Britt McAlister
 * @see     ZNode
 * @see     ZVisualComponent
 */
abstract public interface ZScenegraphObject {

    /** 
     * Damage causes the portions of the surfaces that this object appears in to
     * be marked as needing to be repainted.  The repainting does not actually 
     * occur until ZSurface.restore() is called.
     * public abstract void damage();
     * @see ZNode#damage
     */
    public void damage();
    public void damage(boolean boundsChanged);
    
    /**
     * Notifies the object that it must update it's bounds.
     */
    public void updateBounds();

    /**
     * Notifies an object that it must update it's bounds and request that any children it
     * has update their bounds as well.
     */
    public void updateChildBounds();

    /**
     * Set the visual component.
     * Calling this method will damage the space accordingly.  Be sure to call restore()
     * on any applicable surfaces.  The visual content on the surface
     * will not be updated until a call to restore() is made.
     * @param vc The new visual component
     */
    public void setVisualComponent(ZVisualComponent child);

    /**
     * Used to find the containing object in the scenegraph hierarchy.  ZNodes will just return themselves.
     * ZVisualComponents will ascend the component chain and return the parenting ZNode.
     */
    public ZNode findNode();

    /**
     * Internal method used to compute whether a node is volatile based on its children.
     */
    public void updateVolatility();

    /**
     * Internal method used to notify a node that its transform has changed
     * @param origTransform The value of the original transform (before it was changed)
     */
    public void transformChanged(AffineTransform origTransform);
}
