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

import java.awt.Font;
import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import java.util.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

import java.awt.geom.*;
import java.text.*;
import java.awt.font.TextAttribute;
import java.awt.font.TransformAttribute;

/**
 *   Support <Text> <TSpan> <TRef> tags in SVG
 */
public class GText extends GNode {
    String x = "";      // These valuse can be a list
    String y = "";
    String dx = "";
    String dy = "";

    float baseX = 0;
    float baseY = 0;

    float lastX = 0;
    float lastY = 0;

    float rotate = 0.0f;
    // Default Text Attribute
    String text = "";

    // flag for attributes has been applied
    boolean locationApplied = false;


    static protected final FontRenderContext frc
                = new FontRenderContext(null, true, true);

    public GText(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);
            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("x")) {
                    x = attr.getValue();
                    /*
                    try {
                        x = Float.parseFloat(attr.getValue());
                    } catch (Exception ex) {
                        // resolve x in cm, in form
                    }
                    */
                } else if(attr.getName().equalsIgnoreCase("y")) {
                    y = attr.getValue();
                } else if(attr.getName().equalsIgnoreCase("dx")) {
                    dx = attr.getValue();
                } else if(attr.getName().equalsIgnoreCase("dy")) {
                    dy = attr.getValue();
                } else if(attr.getName().equalsIgnoreCase("rotate")) {
                    try {
                        rotate = Float.parseFloat(attr.getValue());
                    } catch (Exception ex) {
                        // resolve y in cm, in, ... form
                    }
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }
    }

    // Called from GG or GUse or GSVG
    public void draw(GNode parent, Vector GNodes) {
        //this.visualLeaf = new ZVisualLeaf();

        if(gparent != null && gparent instanceof GText) {
            GText gtext = (GText)gparent;
            baseX = gtext.lastX;
            baseY = gtext.lastY;
        }
        readText(visualComps, GNodes);

    }

    // Called from parent gtext node when I'm a GText node referenced by TRef or Tspan
    public void draw(Vector visualComps, Vector GNodes) {
        //this.visualLeaf = new ZVisualLeaf();
        if(gparent != null && gparent instanceof GText) {
            GText gtext = (GText)gparent;
            baseX = gtext.lastX;
            baseY = gtext.lastY;
        }

        readText(visualComps, GNodes);
    }

    public void readText(Vector visualComps, Vector GNodes) {
        NodeList nodeList = this.node.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            org.w3c.dom.Node child = nodeList.item(i);
            int type =  child.getNodeType();

            // Plain text
            if(child.getNodeName().equalsIgnoreCase("#text")) {
                String s = child.getNodeValue();
                // add text at the current position
                buildString(visualComps, s);
                baseX = lastX;
                baseY = lastY;
                text += s;
            }
            // tspan child
            else if(type == SVG.ELEMENT_TYPE && child.getNodeName().equalsIgnoreCase("tspan")) {
                // extract text data
                GText tspan = null;
                for(int j=0;j<children.size();j++) {
                    GNode gchild = (GNode)children.elementAt(j);
                    if(child == gchild.node  && gchild instanceof GText) {
                        tspan = (GText)gchild;
                        break;
                    }
                }
                if(tspan == null) {
                    System.out.println("Invalid child for text node");
                    continue;
                }
                tspan.draw(visualComps, GNodes);
                baseX = tspan.lastX;
                baseY = tspan.lastY;
                text += tspan.text;
            }
            // TRef child
            else if(type == SVG.ELEMENT_TYPE && child.getNodeName().equalsIgnoreCase("tref")) {
                // tref
                GNode tref = null;
                for(int j=0;j<children.size();j++) {
                    GNode gchild = (GNode)children.elementAt(j);
                    if(child == gchild.node) {
                        tref = (GNode)gchild;
                        break;
                    }
                }
                if(tref == null) {
                    System.out.println("Invalid ref in tref");
                }
                Node refNode = null;
                if(tref.sUrl == null || !tref.sUrl.startsWith("#")) {
                    // Object at other file
                    refNode = null;
                    continue;
                }
                for(int j=0;j<GNodes.size();j++) {
                    GNode node = (GNode)GNodes.elementAt(j);
                    if(node.id != null && node.id.equals(tref.sUrl.substring(1))) {
                        refNode = node.node;
                        break;
                    }
                }

                GText textNode = new GText(refNode, this);

                textNode.draw(visualComps, GNodes);
                baseX = textNode.lastX;
                baseY = textNode.lastY;
                text += textNode.text;
                // add text from textNode
            }
        }
    }


    // Need to gather all translation routine in one static class
    private float[] parseNumber(String s) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        int tokenNum = st.countTokens();
        if(tokenNum == 0)
            return null;

        float result[] = new float[tokenNum];

        for(int i=0;i<tokenNum;i++) {
            try {
                result[i] = Float.parseFloat(st.nextToken());
            } catch(Exception ex) {
                result[i] = 0.0f;
            }
        }
        return result;
    }

    private void buildString(Vector visualComps, String s) {
        // every style is in me now....
        // font styles
        //dx, dy, x, y, rotate

        float[] xs = parseNumber(x);
        float[] ys = parseNumber(y);

        float[] dxs = parseNumber(dx);
        float[] dys = parseNumber(dy);

        float startX = 0f, startY = 0f;

        // dx, dy, x, y can be list of values
        // Change dx, dy into absolute coordinate
/*
        if(dxs != null) {
            dxs[0] += baseX;
            for(int i=1;i<dxs.length;i++)
                dxs[i] += dxs[i-1];
            if(xs != null) {
                System.out.println("Incompatible coordinate. Ignore dx: "+dx);
            } else {
                xs = dxs;
            }
        }

        if(dys != null) {
            dys[0] += baseY;
            for(int i=1;i<dys.length;i++)
                dys[i] += dys[i-1];
            if(ys != null) {
                System.out.println("Incompatible coordinate. Ignore dy: "+dy);
            } else {
                ys = dys;
            }
        }
*/
        if(!locationApplied) {
            if(xs != null) {
                startX = baseX + xs[0];
            } else if(dxs != null) {
                startX = baseX + dxs[0];
            } else {
                startX = baseX;
            }

            if(ys != null) {
                startY = baseY + ys[0];
            } else if(dys != null) {
                startY = baseY + dys[0];
            } else {
                startY = baseY;
            }
            locationApplied = true;
        } else {
            startX = baseX;
            startY = baseY;
        }

        if(fontSize < 0f)
            fontSize = 14f;     // Default

        Font font = new Font(fontName, fontStyle, (int)fontSize);

        ZLabel obj = new ZLabel(s, font);
        LineMetrics lm = font.getLineMetrics(s, frc);
        Rectangle2D bounds = font.getStringBounds(s, frc);
        obj.setTranslation(startX, startY-lm.getAscent());

        // fillColor
        if(fillColor != null) {
            obj.setPenColor(fillColor);
        }
        // stroke
        // not implemented

        lastX = startX+ (float)bounds.getWidth();
        lastY = startY;

        visualComps.addElement(obj);
    }

}