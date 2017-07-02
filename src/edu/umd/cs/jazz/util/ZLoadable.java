/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import javax.swing.*;
import edu.umd.cs.jazz.scenegraph.*;

/**
 * A ZLoadable object is one that can be dynamically loaded into Jazz, and be
 * made aware of the basic scenegraph structure so that it can add itself
 * to the application and do something useful.  When a ZLoadable object
 * is loaded into Jazz, all these methods are guaranteed to be called which
 * specify the primary menubar of the application, and the basic elements
 * of the scenegraph (camera, surface, and top node).
 * <P>
 * The following code is a sample stand-alone code segment that implements 
 * ZLoadable and can be imported directly into the demo HiNote program.
 * <PRE>
 *   import java.awt.geom.*;
 *   import java.awt.event.*;
 *   import javax.swing.*;
 *   
 *   import edu.umd.cs.jazz.scenegraph.*;
 *   import edu.umd.cs.jazz.util.*;
 *   
 *   public class PathTest implements Runnable, ZLoadable {
 *       JMenuBar menubar = null;
 *       ZCamera camera = null;
 *       ZSurface surface = null;
 *       ZNode layer = null;
 *   
 *       public PathTest() {
 *       }
 *   
 *       public void run() {
 *           ZPathLayoutManager layout = new ZPathLayoutManager();
 *           layout.setShape(new Ellipse2D.Float(0, 0, 200, 200));
 *           layer.setLayoutManager(layout);
 *   
 *           JMenu layoutMenu = new JMenu("Layout");
 *           JMenuItem menuItem = new JMenuItem("doLayout");
 *           menuItem.addActionListener(new ActionListener() {
 *               public void actionPerformed(ActionEvent e) {
 *                   layer.doLayout();
 *                   surface.restore();
 *               }
 *           });
 *           layoutMenu.add(menuItem);
 *   
 *           menubar.add(layoutMenu);
 *           menubar.revalidate();
 *       }
 *   
 *       public void setMenubar(JMenuBar aMenubar) {
 *           menubar = aMenubar;
 *       }
 *   
 *       public void setCamera(ZCamera aCamera) {
 *           camera = aCamera;
 *       }
 *   
 *       public void setSurface(ZSurface aSurface) {
 *           surface = aSurface;
 *       }
 *   
 *       public void setLayer(ZNode aLayer) {
 *           layer = aLayer;
 *       }
 *   }
 * </PRE>
 * @author Ben Bederson
 */
public interface ZLoadable {
    public void setMenubar(JMenuBar menubar);
    public void setCamera(ZCamera camera);
    public void setSurface(ZSurface surface);
    public void setLayer(ZNode layer);
}
