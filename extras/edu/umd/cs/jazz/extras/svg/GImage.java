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
import java.net.URL;
import java.net.MalformedURLException;


import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 *   Support <image> tag in SVG
 *   current version handle only extenally refenced jpeg files
 */
public class GImage extends GNode {
    int x, y;
    int width = -1;
    int height = -1;
    URL url = null;

    public GImage(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("x")) {
                    Integer xval = new Integer(attr.getValue());
                    x = xval.intValue();
                } else if(attr.getName().equalsIgnoreCase("y")) {
                    Integer yval = new Integer(attr.getValue());
                    y = yval.intValue();
                }  else if(attr.getName().equalsIgnoreCase("width")) {
                    Integer wval = new Integer(attr.getValue());
                    width = wval.intValue();
                }  else if(attr.getName().equalsIgnoreCase("height")) {
                    Integer hval = new Integer(attr.getValue());
                    height = hval.intValue();
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }

        try {
            url = new URL(contextURL, sUrl);
        } catch (MalformedURLException e) {
            url = null;
        }

        if(url == null) {
            System.out.println("Cannot load image " + sUrl);
            return;
        }

        ZImage obj = new ZImage(url);

        if(width <= 0 || height <= 0) {
            transformed = true;
            transform.translate(x, y);
        } else {
            double scaleX = (double)width/obj.getWidth();
            double scaleY = (double)height/obj.getHeight();
            transform.setTransform(scaleX, 0d, 0d, scaleY, (double)x, (double)y);
        }
        visualComps.addElement(obj);
    }
}