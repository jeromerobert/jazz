/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import java.util.Vector;
import java.awt.BasicStroke;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;


/**
 *   Support <rect> tag in SVG
 *   Rounded rectangle and dashed rectangle is not suported
 */
public class GRect extends GNode {
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;


/*  Attributes defined in super class GNode
    Color stroke = null;
    int strokeWidth = 1;
    // Deafault CAP_SQUARE, JOIN_MITER, a miter limit of 10.0.
    int strokeLineCap = BasicStroke.CAP_SQUARE;
    int strokeLineJoin = BasicStroke.JOIN_MITER;
    float strokeMiterLimit = 10.0f;
    float strokeDasharray[] = null;
*/

    public GRect(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                try {
                if(attr.getName().equalsIgnoreCase("x")) {
                    Integer xval = new Integer(attr.getValue());
                    x = xval.intValue();
                } else if(attr.getName().equalsIgnoreCase("y")) {
                    Integer yval = new Integer(attr.getValue());
                    y = yval.intValue();
                } else if(attr.getName().equalsIgnoreCase("width")) {
                    Integer wval = new Integer(attr.getValue());
                    width = wval.intValue();
                } else if(attr.getName().equalsIgnoreCase("height")) {
                    Integer hval = new Integer(attr.getValue());
                    height = hval.intValue();
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
                } catch (Exception ex) {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }

            }
        }

        ZRectangle obj = new ZRectangle(x, y, width, height);

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

        //ref.) public BasicStroke(float width, int cap, int join, float miterlimit,
        //              float[] dash, float dash_phase)

        visualComps.addElement(obj);
    }

}