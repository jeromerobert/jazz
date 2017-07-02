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
import java.awt.BasicStroke;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 *   Support <ellipse> tag in SVG
 */
public class GEllipse extends GNode {
    int cx = 0;
    int cy = 0;
    int rx = 0;
    int ry = 0;
    String angle = null;

    public GEllipse(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("cx")) {
                    Integer xval = new Integer(attr.getValue());
                    cx = xval.intValue();
                } else if(attr.getName().equalsIgnoreCase("cy")) {
                    Integer yval = new Integer(attr.getValue());
                    cy = yval.intValue();
                } else if(attr.getName().equalsIgnoreCase("rx")) {
                    Integer rval = new Integer(attr.getValue());
                    rx = rval.intValue();
                } else if(attr.getName().equalsIgnoreCase("ry")) {
                    Integer rval = new Integer(attr.getValue());
                    ry = rval.intValue();
                } else if(attr.getName().equalsIgnoreCase("angle")) {
                    angle = attr.getValue().trim();
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }

        int x, y, width, height;

        x = cx - rx;
        y = cy - ry;
        width = 2*rx;
        height = 2*ry;

        ZEllipse obj = new ZEllipse(x, y, width, height);

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
                strokeMiterLimit, strokeDashArray, 0.0f));
        }

        //ref.) public BasicStroke(float width, int cap, int join, float miterlimit,
        //              float[] dash, float dash_phase)


/*
        // When there's an angle attribute
        // rotate angle ellipse..
        // Not-yet implemented

        if(angle != null) {
            ZTransformGroup r = new ZTransformGroup();
            r.rotate(Math.toRadians(Double.parseDouble(angle)));
            transform.translate(-rx, -ry);
            r.addChild(new ZVisualLeaf(obj));
            transform.translate(x+rx, y+ry);
            transform.addChild(r);
        } else {
            transform.translate(x, y);
            transform.addChild(new ZVisualLeaf(obj));
        }
*/
        visualComps.addElement(obj);
    }

}