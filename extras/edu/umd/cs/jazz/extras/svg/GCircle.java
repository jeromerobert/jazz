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
 *   Support <circle> tag in SVG
 */
public class GCircle extends GNode {
    int cx = 0;
    int cy = 0;
    int r = 0;

    public GCircle(Node _node, GNode parent) {
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
                } else if(attr.getName().equalsIgnoreCase("r")) {
                    Integer rval = new Integer(attr.getValue());
                    r = rval.intValue();
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }

        int x, y, width, height;
        x = cx - r;
        y = cy - r;
        width = height = 2*r;

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

        visualComps.addElement(obj);
    }
}