/**
 * Copyright 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cs.jazz.animation.*;
import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.util.*;

/**
 * Example shows how to a make an object draw itself differently at different camera magnifications.
 * @author: Jesse Grosjean
 */
class SemanticZoomingExample extends AbstractExample {
    protected ZCanvas canvas;

    public static class SemanticGrid extends ZRectangle {
	static Line2D gridLine = new Line2D.Double();
	static Stroke gridStroke = new BasicStroke(0);

	public SemanticGrid(double x, double y, double width, double height) {
	    super(x, y, width, height);
	}

	public void render(ZRenderContext rc) {
	    super.render(rc);

	    Graphics2D g2 = rc.getGraphics2D();
	    double gridSpacing = 0;
	    double compositeMag = rc.getCompositeMagnification();    		

	    if (compositeMag < 1.5) {
		gridSpacing = 20;
	    } else {
		gridSpacing = 10;
	    }

	    g2.setStroke(gridStroke);

	    // draw vertical grid.
	    for (double x = getX(); x < getX() + getWidth(); x+=gridSpacing) {
		gridLine.setLine(x, getY(), x, getY() + getHeight());

		if (x%20 == 0) {
		   g2.setColor(Color.black);
		} else {
		   g2.setColor(Color.blue);
		}

		g2.draw(gridLine);		
            }

	    // draw horizontal grid.
	    for (double y = getY(); y < getY() + getHeight(); y+=gridSpacing) {
		gridLine.setLine(getX(), y, getX() + getWidth(), y);

		if (y%20 == 0) {
		   g2.setColor(Color.black);
		} else {
		   g2.setColor(Color.blue);
		}

		g2.draw(gridLine);		
            }
	}
    }
    
    public SemanticZoomingExample() {
        super("Semantic Zooming Example");
    }

    public String getExampleDescription() {
        return "This shows how to create a simple grid the shows more detail as you zoom into it. Once you zoom to a" +
	       " magnification of 1.5 or greater it will draw a more detailed set of blue grid lines.";
    }

    public void initializeExample() {
        super.initializeExample();

        // Set up basic frame
        setBounds(100, 100, 400, 400);
        setResizable(true);
        setBackground(null);
        setVisible(true);
        canvas = new ZCanvas();
        getContentPane().add(canvas);
        validate();

        ZVisualLeaf aLeaf = new ZVisualLeaf(new SemanticGrid(0, 0, 300, 300));
        canvas.getLayer().addChild(aLeaf);
    }
}