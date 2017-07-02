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
import java.awt.geom.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 *   Support <g> tag in SVG
 *   <g> tag is the base grouping unit in svg
 */
public class GG extends GNode {

    public GG(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(false) {
                // No attributes for this component
                // use what is read in GNode (superclass)
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }
    }

    public void draw(GNode parent, Vector GNodes) {
        this.group = parent.group;
        for(int i=0;i< children.size();i++) {
            GNode gnode = (GNode)children.elementAt(i);
            if(gnode instanceof GG || gnode instanceof GUse || gnode instanceof GSVG) {
                if(parent.transformed || transformed) {
                    AffineTransform at = parent.transform.getTransform();
                    this.transform.preConcatenate(at);
                    this.transformed = true;
                    gnode.draw(this, GNodes);
                } else {
                    gnode.draw(parent, GNodes);
                }
            } else if(gnode instanceof GRect
                || gnode instanceof GCircle || gnode instanceof GEllipse
                || gnode instanceof GImage || gnode instanceof GPath
                || gnode instanceof GPolygon || gnode instanceof GPolyline
                || gnode instanceof GLine) {

                if(parent.transformed || transformed) {
                    AffineTransform at = parent.transform.getTransform();
                    this.transform.preConcatenate(at);
                    this.transformed = true;
                }
                if(gnode.transformed) {
                    AffineTransform at = this.transform.getTransform();
                    gnode.transform.preConcatenate(at);
                    gnode.transformed = true;
                }

                //
                // case 1       parent, group and object not transformed
                //
                if(!parent.transformed && !transformed && !gnode.transformed) {
                    // append visualcomponents to root's visualLeaf
                    if(parent.visualLeaf == null) {
                        parent.visualLeaf = new ZVisualLeaf();
                        group.addChild(parent.visualLeaf);
                    }
                    gnode.addVisualComponent(parent.visualLeaf);
                }
                //
                // case 3       children is not transformed
                //              draw them under group's visualleaf
                //
                else if(!gnode.transformed) {
                    if(this.visualLeaf == null) {
                        this.visualLeaf = new ZVisualLeaf();
                        this.transform.addChild(this.visualLeaf);
                        group.addChild(this.transform);
                    }
                    gnode.addVisualComponent(this.visualLeaf);
                }
                //
                // case 3       children are transformed
                //
                else if(gnode.transformed) {
                    gnode.addVisualComponentIntoTransform();
                    group.addChild(gnode.transform);
                }
            } else if(gnode instanceof GText) {
                    gnode.draw(this, GNodes);
                    AffineTransform at = this.transform.getTransform();
                    gnode.transform.preConcatenate(at);

                    at = parent.transform.getTransform();
                    gnode.transform.preConcatenate(at);

                    gnode.addVisualComponentIntoTransform();
                    group.addChild(gnode.transform);
            }
        }
    }
}