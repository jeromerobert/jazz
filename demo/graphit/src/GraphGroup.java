/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * A simple group that uses semantic zooming to render itself as
 * a large circle, based on the bounds of its children, above the render
 * cutoff and render its children below the render cutoff.
 *
 * @author Lance Good
 */
public class GraphGroup extends ZGroup {

	// The pen color for the group ellipse
	static final Color   PEN_COLOR = GraphItCore.NODE_PEN_COLOR;    

	// The fill color for the group ellipse
	static final Color   FILL_COLOR = GraphItCore.NODE_FILL_COLOR;

	// The fade range default
	static final double   fadeRange_DEFAULT = 0.2f;

	// The number of pre-allocated alpha levels
	static final int     NUM_ALPHA_LEVELS = 16;    

	// The pre-allocated alpha levels
	static Composite alphas[] = null;             

	// The percentage of magnification change over which an object is faded
	double fadeRange = fadeRange_DEFAULT;

	// The ellipse representing this group
	ZEllipse ellipse;
	
	// The cutoff magnification at which the group is rendered 
	double groupRenderCutoff;
		
	static {
	// Allocate the pre-computed alpha composites the first time a fade group is used.
	double value;
	alphas = new Composite[NUM_ALPHA_LEVELS];

	for (int i=0; i<NUM_ALPHA_LEVELS; i++) {
	    value = (double)i / (NUM_ALPHA_LEVELS - 1);
	    alphas[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)value);
	}
	}    
	
	/**
	 * The default constructor
	 */
	public GraphGroup() {	
	ellipse = new ZEllipse();
	ellipse.setPenPaint(PEN_COLOR);
	ellipse.setFillPaint(FILL_COLOR);
	}
	/**
	 * First, the children's bounds are computed then the circle enscribing
	 * those bounds.  Finally, the bounds enscribing the circle are computed.
	 */
	public void computeBounds() {
	super.computeBounds();

	double diameter = Math.sqrt(bounds.getWidth()*bounds.getWidth()+
					  bounds.getHeight()*bounds.getHeight());
	double topX = bounds.getX()+(bounds.getWidth()/2.0)-diameter/2.0;
	double topY = bounds.getY()+(bounds.getHeight()/2.0)-diameter/2.0;

	bounds.setRect(topX,topY,diameter,diameter);
	ellipse.setFrame(topX,topY,diameter,diameter);
	}
	/**
	 * Internal method to compute and access an alpha Composite given the current rendering
	 * composite, and the current magnification.  It determines the alpha based
	 * on this node's alpha value, minimum magnification, and maximum magnification.
	 * @param currentComposite The composite in the current render context
	 * @param currentMag The magnification of the current rendering camera
	 * @return Composite The Composite to use to render this node
	 */
	protected Composite getGroupComposite(Composite currentComposite, double currentMag) {
	double newAlpha = 1.0;
	double groupFadeStart;
	double groupFadeEnd;

	groupFadeStart = groupRenderCutoff * (1.0 - fadeRange);
	groupFadeEnd = groupRenderCutoff * (1.0 + fadeRange);

	if (currentMag > groupFadeStart && currentMag < groupFadeEnd) {
	    newAlpha = (groupFadeEnd - currentMag) / (groupFadeEnd - groupFadeStart);
	}
	else if (currentMag > groupFadeEnd) {
	    newAlpha = 0.0;
	}

	if ((currentComposite != null) &&
	    (currentComposite instanceof AlphaComposite)) {

	    newAlpha *= ((AlphaComposite)currentComposite).getAlpha();
	}

	if (newAlpha == 1.0) {
	    return currentComposite;
	} else {
	    return alphas[(int)(newAlpha * NUM_ALPHA_LEVELS)];
	}
	}
	/**
	 * A utility method to determine if the group is completely opaque
	 * @param currentMag The current magnification
	 */
	public boolean isGroupFullyVisible(double currentMag) {
	if (currentMag < groupRenderCutoff * (1.0 - fadeRange)) {
	    return true;
	}
	else {
	    return false;
	}
	}
	/**
	 * A utility method to determine is the group is completely transparent
	 * @param currentMag The current magnification
	 */
	public boolean isGroupInvisible(double currentMag) {
	if (currentMag > groupRenderCutoff * (1.0 + fadeRange)) {
	    return true;
	}
	else {
	    return false;
	}
	}
	/**
	 * Determines if the group is being rendered as a circle or
	 * if it is rendering its children.
	 */
	public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	if (isGroupInvisible(path.getCamera().getMagnification())) {
	    return super.pick(rect,path);
	}
	else {
	    if (rect.intersects(bounds)) {
		path.setObject(this);
		return true;
	    }	    

	    return false;
	}
	}
	/**
	 * This method avoids setting the composite as often as possible since
	 * this is expensive.  For example, the children could be faded exactly
	 * opposite of the group but this turns out to be pretty slow.
	 */
	public void render(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	Composite saveComposite = null;
	double currentMag = renderContext.getCameraMagnification();
	ZNode[] children = getChildren();
	
	saveComposite = g2.getComposite();
	for(int i=0; i<children.length; i++) {
	    children[i].render(renderContext);
	}	

	if (!isGroupInvisible(currentMag)) {
	    if (isGroupFullyVisible(currentMag)) {
		ellipse.render(renderContext);
	    }
	    else {
		g2.setComposite(getGroupComposite(saveComposite, currentMag));
		ellipse.render(renderContext);
	    }
	}

	g2.setComposite(saveComposite);	
	}
	/**
	 * Sets the level at which the group renders itself as a circle vs.
	 * rendering its children.
	 * @param level The render cutoff level
	 */
	public void setGroupRenderCutoff(double level) {
	groupRenderCutoff = level / (1.0 - fadeRange);
	}
}
