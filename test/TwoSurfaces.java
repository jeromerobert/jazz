/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * Basic test of two surfaces each attached to a JPanel.
 * @author Maria Jump
 * @author Benjamin B. Bederson
 */
public class TwoSurfaces extends JFrame {

    protected JPanel panel1, panel2;
    protected ZRootNode root;
    protected ZNode layer, selectionLayer;
    protected ZBasicComponent component1, component2;

    public TwoSurfaces() {
	super("Two Surfaces Test");
        setSize(800, 400);	// Give this window some dimensions
	getContentPane().setLayout(null);

				// Setup first internal frame
	panel1 = new JPanel();
	getContentPane().add(panel1);
	panel1.setLocation(new Point(10, 10));
	panel1.setSize(370, 350);

				// Setup second internal frame
	panel2 = new JPanel();
	getContentPane().add(panel2);
	panel2.setLocation(new Point(410, 10));
	panel2.setSize(370, 350);
	
        setVisible(true);	// Make this window visible

				// Make the basic scenegraph
	root = new ZRootNode();
	layer = new ZNode();
	root.addChild(layer);
				// Create the first surface
	component1 = new ZBasicComponent(root, layer);
	component1.setSize(panel1.getSize());
	panel1.add(component1);

				// Create the second surface
	component2 = new ZBasicComponent(root, layer);
	component2.setSize(panel2.getSize());
	panel2.add(component2);
                               // Watch for the user closing the window so we can exit gracefully
        addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                System.exit(0);
            }
        });
        
                                // Add a little content to the scene
        ZText text1 = new ZText("Left-button in the left window to select");
        ZNode node = new ZNode(text1);
        node.getTransform().translate(100, 100);
        layer.addChild(node);

        ZText text2 = new ZText("Left-button in the right window to pan");
        ZNode node1 = new ZNode(text2);
        node1.getTransform().translate(100, 115);
        layer.addChild(node1);

        ZText text3 = new ZText("Right-button (and drag left or right) in either window to zoom");
        ZNode node2 = new ZNode(text3);
        node2.getTransform().translate(100, 200);
        layer.addChild(node2);

        ZRectangle rect = new ZRectangle(50, 50, 50, 100);
	rect.setFillColor(Color.blue);
	rect.setPenColor(Color.black);
        ZNode node3 = new ZNode(rect);
        layer.addChild(node3);

	float[] xp = new float[2];
	float[] yp = new float[2];
	xp[0] = 0;   yp[0] = 0;
	xp[1] = 50;  yp[1] = 50;
        ZPolyline poly = new ZPolyline(xp, yp);
	poly.setPenColor(Color.red);
        ZNode node4 = new ZNode(poly);
        layer.addChild(node4);

        component1.getSurface().restore();      // Force modifications to all be updated
	component2.getSurface().restore();

                                // Add selection event handler to left window
	selectionLayer = new ZNode();
	root.addChild(selectionLayer);
	component1.getCamera().addPaintStartPoint(selectionLayer);
	component2.getCamera().addPaintStartPoint(selectionLayer);
	ZEventHandler selectionEventHandler = 
	    new ZSelectionEventHandler(component1, component1.getSurface(), selectionLayer) {
		public void mouseReleased(MouseEvent e) {
		    super.mouseReleased(e);
		    component1.getSurface().restore();
		    component2.getSurface().restore();
		}
	    };
	selectionEventHandler.activate();

                                // Add panning event handler to left window
        ZEventHandler panHandler2 = new ZPanEventHandler(component2, component2.getSurface());
        panHandler2.activate();

                                // Add zooming event handler to both windows
        ZEventHandler zoomHandler1 = new ZoomEventHandlerRightButton(component1, component1.getSurface());
        ZEventHandler zoomHandler2 = new ZoomEventHandlerRightButton(component2, component2.getSurface());
        zoomHandler2.activate();
        zoomHandler1.activate();
    }
        
    public static void main(String args[]) {
	TwoSurfaces app = new TwoSurfaces();
    }
}





