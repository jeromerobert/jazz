/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import java.util.Vector;
import java.awt.geom.AffineTransform;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 *   Support <use/> tag in SVG
 */
public class GUse extends GNode {
    // Linked object
    GNode gnode = null;

    public GUse(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);
            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(false) {

                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }

        // load Linking GNode using href value (that is now stored in sUrl)

    }

    public void draw(GNode parent, Vector GNodes) {
        // url has a link
        // find the transform of referenced obj --> gnode

        if(!sUrl.startsWith("#")) {
            // Object at other file
            // does not support link referencing other file
            gnode = null;
            return;
        }

        for(int i=0;i<GNodes.size();i++) {
            GNode node = (GNode)GNodes.elementAt(i);
            if(node.id != null && node.id.equals(sUrl.substring(1))) {
                // need to copy into gnode rather than have reference
                gnode = node;
                break;
            }
        }

        if(gnode == null) {
            System.out.println("Cannot find linking object " + sUrl);
            return;
        }

// create new object
// add them under the group....

        this.group = parent.group;

    }
}