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
import java.util.StringTokenizer;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 *   Support <line> tag in SVG
 */
public class GLine extends GNode {
    float x1, x2, y1, y2;

    public GLine(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("x1")) {
                    x1 = java.lang.Float.parseFloat(attr.getValue());
                } else if(attr.getName().equalsIgnoreCase("y1")) {
                    y1 = java.lang.Float.parseFloat(attr.getValue());
                } else if(attr.getName().equalsIgnoreCase("x2")) {
                    x2 = java.lang.Float.parseFloat(attr.getValue());
                } else if(attr.getName().equalsIgnoreCase("y2")) {
                    y2 = java.lang.Float.parseFloat(attr.getValue());
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }
        Point2D.Float p1 = new Point2D.Float(x1, y1);
        Point2D.Float p2 = new Point2D.Float(x2, y2);

        ZPolyline obj = new ZPolyline();

        obj.add(p1.getX(), p1.getY());
        obj.add(p2.getX(), p2.getY());

        float strokePenWidth;
        if(stroke != null) {
            obj.setPenPaint(stroke);
            strokePenWidth = (float)strokeWidth;
        } else {
            strokePenWidth = 0.0f;
        }

        if(strokeDashArray == null) {
            obj.setStroke(new BasicStroke(strokePenWidth, strokeLineCap, strokeLineJoin,
                strokeMiterLimit));
        } else {
            obj.setStroke(new BasicStroke(strokePenWidth, strokeLineCap, strokeLineJoin,
                strokeMiterLimit, strokeDashArray, 0));
        }

        visualComps.addElement(obj);
    }
}