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
 *   Support <polygon> tag in SVG
 */
public class GPolygon extends GNode {
    Vector points = new Vector();

    public GPolygon(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("points")) {
                    readPoints(attr.getValue().trim());
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }


        ZPolygon obj = new ZPolygon();

        for(int i=0;i<points.size();i++) {
            Point2D.Float p = (Point2D.Float)points.elementAt(i);
            obj.add(p.getX(), p.getY());
        }

        obj.setFillPaint(fillColor);

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

    protected void readPoints(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        int iteration = (int)(st.countTokens()/2);

        for(int i=0;i<iteration;i++) {
            float xval = java.lang.Float.parseFloat(st.nextToken());
            float yval = java.lang.Float.parseFloat(st.nextToken());

            Point2D.Float p = new Point2D.Float(xval, yval);
            points.addElement(p);
        }
    }
}