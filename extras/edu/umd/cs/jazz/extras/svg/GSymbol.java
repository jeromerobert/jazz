/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import java.util.Vector;


/**
 *   Support <Symbol> tags in SVG
 *   Currently, do nothing
 */
public class GSymbol extends GNode {

/*
    private Vector makeNodes(Vector children, Vector GNodes) {
        for(int i=0;i<children.size();i++) {
        }
    }

    public void draw(GNode parent, Vector GNodes) {
        makeNodes(this.children);
    }
*/
    public GSymbol(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(false) {
                // No attributes for this component
                // use what is read in GNode
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }
    }

/*
    private Vector makeNodes(Vector children, Vector GNodes) {
        for(int i=0;i<children.size();i++) {
        }
    }

    public void draw(GNode parent, Vector GNodes) {
        makeNodes(this.children);
    }
*/
}