/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import org.w3c.dom.*;

import java.util.Vector;
import java.net.URL;
import java.util.StringTokenizer;
import java.net.MalformedURLException;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.*;
import java.awt.BasicStroke;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
  * GNode is the base class of all G.. classes.
  * This class contains all information about translation, style, font and color
  */
public class GNode {

    String nodeName;
    String id = null;
    org.w3c.dom.Node node;

    GNode gparent = null;


    Vector path = new Vector();
    Vector children = new Vector();
    Vector attributes = new Vector();

    ZGroup group = null;
    //
    // Transformation
    //
    // Transformation with respect to its parent...
    ZTransformGroup transform = new ZTransformGroup();

    // Contains visual compenents... (filled in sub-classes)
    public Vector visualComps = new Vector();
    public ZVisualLeaf visualLeaf = null;

    // Check if this node has been transformed w.r.t its parent
    // this is neccessary because of speed-up..
    // if there is no transformation.... add its visual componets to its parent..
    boolean transformed = false;

    //
    // Style variable
    // (need default value)
    //

    Color currentColor = null;
    Color fillColor = null;

    // Stroke color
    Color stroke = null;
    float strokeWidth = 1;

    // Deafault CAP_SQUARE, JOIN_MITER, a miter limit of 10.0.
    int strokeLineCap = BasicStroke.CAP_SQUARE;
    int strokeLineJoin = BasicStroke.JOIN_MITER;
    float strokeMiterLimit = 10.0f;
    float strokeDashArray[] = null;

    //
    // Fill rule
    //  nonzero, evenodd,
    //
    static final int NONZERO = 0;
    static final int EVENODD = 1;
    int fillRule = NONZERO;

    float opacity = -1;

    //
    // Font information
    //
    float fontSize = -1;
    String fontName = "Helvetica";
    int fontStyle = Font.PLAIN;

    // sURL has url of attribute e.g. href= "..."
    String sUrl = "";

    // contextURL has of current SVG file (it is used to identify relative locations
    URL contextURL = null;
    //
    // End Style
    //


    public GNode(Node _node, GNode _gparent) {
        node = _node;
        gparent = _gparent;
        nodeName = node.getNodeName();

        transform.setHasOneChild(true);

        if(gparent != null) {
            gparent.children.addElement(this);
        }

        Vector reversePath = new Vector();

        Node parent = node;
        while(parent!= null && parent.getNodeType() != SVG.DOCUMENT_TYPE) {
            reversePath.addElement(parent.getNodeName().trim());
            parent = parent.getParentNode();
        }

        for(int i=reversePath.size()-1;i>=0;i--) {
            path.addElement(reversePath.elementAt(i));
        }

        // Before reading my style read parent's style
        copyStyleFromParent();

        NamedNodeMap attrs = node.getAttributes();
        if(attrs == null) return;

        for(int i=0; i<attrs.getLength(); i++) {
            Node child = attrs.item(i);
            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("style")) {
                    readStyle(attr.getValue().trim());
                } else if(attr.getName().equalsIgnoreCase("id")) {
                    id = attr.getValue().trim();
                } else if(attr.getName().equalsIgnoreCase("transform")) {
                    readTransform(attr.getValue().trim());
                // } else if(attr.getName().equalsIgnoreCase("angle")) {
                    //processed at child node, ellipse
                    //readTransform("rotate("+attr.getValue().trim()+")");
                } else if(attr.getName().startsWith("xlink")) {
                    readXLink(attr.getName(), attr.getValue());
                } else if(attr.getName().startsWith("class")) {
                    System.out.println("Attribute class is not supported in this version");
                } else {
                    // attributes: invalid or for sub-classes
                    attributes.addElement(attr);
                }
            }
        }

    }


    public void setLocation(String url) {
        try {
            contextURL = new URL(url);
        } catch (Exception ex) {
            contextURL = null;
        }
    }

    protected void readXLink(String arg, String value) {
        StringTokenizer st = new StringTokenizer(arg, " :", false);

        if(st.countTokens() == 1) {
            System.out.println("Malformed xlink :" + arg + " " + value);
            return;
        }

        String  xLink = st.nextToken();
        String linkType = st.nextToken();
        if(linkType.equalsIgnoreCase("href")) {

            /*
            //URL current = this.getClass().getClassLoader().getResource(".");
            try {
                //url = new URL(current, value);
                url = new URL(value);
            } catch (MalformedURLException e) {
                url = null;
            }

            */

            // resolved in sub-classes
            sUrl = value;
        } else if(linkType.equalsIgnoreCase("type")) {
        } else if(linkType.equalsIgnoreCase("actuate")) {
        } else if(linkType.equalsIgnoreCase("show")) {
        } else {
            System.out.println("Unresolved linktype ("
                + linkType + ",  " + value + ") in "+ node.getNodeName());
        }

    }



    protected void readStyle(String s) {
        StringTokenizer st = new StringTokenizer(s, ";", false);
        while (st.hasMoreTokens()) {
            String arg = "";
            String value = "";
            StringTokenizer param = new StringTokenizer(st.nextToken(), ":");
            if(param.countTokens() == 1) {
                arg = param.nextToken().trim();
                addStyle(arg, "");
            } else if(param.countTokens() == 2) {
                arg = param.nextToken().trim();
                value = param.nextToken().trim();
                addStyle(arg, value);
            } else {
                System.out.println("Invalid style " + s);
            }
        }
    }

    protected Color readColor(String val) {

        if (val == null)
            return Color.gray;

        val.trim();

        if(val.equalsIgnoreCase("none")) {
            return null;
        }

        if(val.equalsIgnoreCase("currentColor")) {
            if(currentColor == null) {
                return Color.gray;
            } else {
                return currentColor;
            }
        }

        for(int i=0; i<namedColors.length;i++) {
            if(val.equalsIgnoreCase(namedColors[i])) {
                return new Color(colors[i]);
            }
        }

        if(val.startsWith("#")) {
            if(val.length() == 4) {     // #f0f
                int r, g, b;
                r = 16*interpreteColor(val.substring(1, 2));
                g = 16*interpreteColor(val.substring(2, 3));
                b = 16*interpreteColor(val.substring(3, 4));

                return new Color(r, g, b);
            } else if(val.length() == 7) {  // #ff00ff
                int r, g, b;
                r = interpreteColor(val.substring(1, 3));
                g = interpreteColor(val.substring(3, 5));
                b = interpreteColor(val.substring(5, 7));

                return new Color(r, g, b);
            }
        } else if(val.toLowerCase().startsWith("rgb")) {
            int start, end;
            start = val.indexOf('(');
            end = val.indexOf(')');
            if(start == -1 || end == -1) {
                System.out.println("Unresolved color " + val);
                return Color.gray;
            }
            String s = val.substring(start+1, end);

            StringTokenizer st = new StringTokenizer(s, ",");
            if(st.countTokens() != 3) {
                System.out.println("Unresolved color " + val);
                return Color.gray;
            }
            int r, g, b;
            r = interpreteColor(st.nextToken().trim());
            g = interpreteColor(st.nextToken().trim());
            b = interpreteColor(st.nextToken().trim());
            return new Color(r, g, b);
        }
        System.out.println("Unresolved color " + val);
        return Color.gray;
    }

    /**
     *  Return int value from string
     *  e.g. ff --> 255
     *       a --> 10
     *       50% --> 255 * 50/100
    */
    protected int interpreteColor(String s) {
        int val;

        if(s == null)
            return 0;

        s.trim();

        try {
            if(s.endsWith("%")) {
                String percent = s.substring(0, s.length()-1);
                Integer i = new Integer(percent);
                val = (int)(i.intValue()*255/100);
            } else {
                Integer i = new Integer(0);
                val = i.parseInt(s, 16);
            }
        } catch (NumberFormatException e) {
            val = 0;
        }

        if(val < 0) val = 0;
        if(val > 255) val = 255;

        return val;
    }

    protected void addStyle(String attr, String val) {

        //
        //
        // Color and Stroke Properties
        //
        //
        if(attr.equalsIgnoreCase("fill")) {
            if(val.equalsIgnoreCase("none")) {
                fillColor = null;
            } else {
                fillColor = readColor(val);
            }
        } else if(attr.equalsIgnoreCase("stroke")) {
            if(val.equalsIgnoreCase("none")) {
                stroke = null;
            } else {
                stroke = readColor(val);
            }
        } else if(attr.equalsIgnoreCase("stroke-width")) {
            // should accept cm, mm, etc not only number
            try {
                strokeWidth = Float.parseFloat(val);
            } catch (Exception ex) {
                strokeWidth = 1;        // If error, set it as default
            }

        } else if(attr.equalsIgnoreCase("stroke-linecap")) {
            // butt, round, square, inherit
            if(val.equalsIgnoreCase("butt")) {
                strokeLineCap = BasicStroke.CAP_BUTT;
            } else if(val.equalsIgnoreCase("round")) {
                strokeLineCap = BasicStroke.CAP_ROUND;
            } else if(val.equalsIgnoreCase("square")) {
                strokeLineCap = BasicStroke.CAP_SQUARE;
            } else if(val.equalsIgnoreCase("inherit")) {
                // Inherit
            } else {
                System.out.println("Unresolved Stroke linecap " + val);
            }
        } else if(attr.equalsIgnoreCase("stroke-linejoin")) {
            //<path style="stroke-linejoin:bevel"> values: miter, round, bevel, inherit
            if(val.equalsIgnoreCase("miter")) {
                strokeLineJoin = BasicStroke.JOIN_MITER;
            } else if(val.equalsIgnoreCase("round")) {
                strokeLineJoin = BasicStroke.JOIN_ROUND;
            } else if(val.equalsIgnoreCase("bevel")) {
                strokeLineJoin = BasicStroke.JOIN_BEVEL;
            } else if(val.equalsIgnoreCase("inherit")) {
                // Inherit
            } else {
                System.out.println("Unresolved Stroke linejoin " + val);
            }
        } else if(attr.equalsIgnoreCase("stroke-miterlimit")) {
            //<path style="stroke-miterlimit:4"> values: <miterlimit>, inherit
            Integer i = new Integer(val);
            strokeMiterLimit = i.intValue();
        } else if(attr.equalsIgnoreCase("stroke-dasharray")) {
            //<path style="stroke-dasharray:12 12"> values: <dasharray>, inherit
            if(val.equalsIgnoreCase("none")) {
                strokeDashArray = null;
            } else {
                StringTokenizer st = new StringTokenizer(val, " ,");
                strokeDashArray = new float[st.countTokens()];
                for(int i=0;st.countTokens()>0;i++) {
                    String token = "";
                    try {
                        token = st.nextToken();
                        strokeDashArray[i] = Float.parseFloat(token);
                    } catch (Exception ex) {
                    System.out.println("Cannot change into float "
                        + token + " in " + val);
                    }
                }
            }
        } else if(attr.equalsIgnoreCase("fill-rule")) {
            if(val.equalsIgnoreCase("nonzero")) {
                fillRule = NONZERO;
            } else if(val.equalsIgnoreCase("evenodd")) {
                fillRule = EVENODD;
            }
        }
        //
        //
        // Font Properties
        //
        //
        else if(attr.equalsIgnoreCase("font-size")) {
/*
'font-size'
Value:   <absolute-size> | <relative-size> | <length> | <percentage> | inherit
Initial:   medium
Applies to:   all elements
Inherited:   yes, the computed value is inherited
Percentages:  refer to parent element's font size
Media:   visual
Animatable:   yes
*/
            try {
                fontSize = Float.parseFloat(val);
            } catch (Exception ex) {
                System.out.println("Wrong formatted font size: "+val);
            }
        } else if(attr.equalsIgnoreCase("font-style")) {
            if(val.equalsIgnoreCase("normal")) {
            } else if(val.equalsIgnoreCase("italic")) {
            } else if(val.equalsIgnoreCase("oblique")) {
            } else {
            }
        } else if(attr.equalsIgnoreCase("font-variant")) {
            if(val.equalsIgnoreCase("normal")) {
            } else if(val.equalsIgnoreCase("small-caps")) {
            }

        } else if(attr.equalsIgnoreCase("font-weight")) {
            if(val.equalsIgnoreCase("normal")) {
            } else if(val.equalsIgnoreCase("bold")) {
            } else if(val.equalsIgnoreCase("bolder")) {
            } else if(val.equalsIgnoreCase("lighter")) {
            } else if(val.equalsIgnoreCase("100")) {
            } else if(val.equalsIgnoreCase("200")) {
            } else if(val.equalsIgnoreCase("300")) {
            } else if(val.equalsIgnoreCase("400")) {
            } else if(val.equalsIgnoreCase("500")) {
            } else if(val.equalsIgnoreCase("600")) {
            } else if(val.equalsIgnoreCase("700")) {
            } else if(val.equalsIgnoreCase("800")) {
            } else if(val.equalsIgnoreCase("900")) {
            } else if(val.equalsIgnoreCase("inherit")) {
            } else {
            }
        } else if(attr.equalsIgnoreCase("font-family")) {
        } else if(attr.equalsIgnoreCase("font-stretch")) {
        } else if(attr.equalsIgnoreCase("font-size-adjust")) {
        } else if(attr.equalsIgnoreCase("font")) {
        }
        //
        //
        // Text Properties
        //
        //
        else if(attr.equalsIgnoreCase("text-decoration")) {
        } else if(attr.equalsIgnoreCase("letter-spacing")) {
        } else if(attr.equalsIgnoreCase("word-spacing")) {
        } else if(attr.equalsIgnoreCase("direction")) {
        } else if(attr.equalsIgnoreCase("unicode-bidi")) {
        //
        //
        // Visual Properties
        //
        //
        } else if(attr.equalsIgnoreCase("visibility")) {
        } else if(attr.equalsIgnoreCase("display")) {
        } else if(attr.equalsIgnoreCase("overflow")) {
        } else if(attr.equalsIgnoreCase("clip")) {
        } else if(attr.equalsIgnoreCase("cursor")) {
        //
        //
        // Misc Properties
        //
        //
        } else if(attr.equalsIgnoreCase("opacity")) {

        } else if(attr.equalsIgnoreCase("color")) {
            currentColor = readColor(val);
        } else if(attr.equalsIgnoreCase("shape-rendering")) {
                // auto
        } else if(attr.equalsIgnoreCase("text-rendering")) {
                // optimizeLegibility
        } else if(attr.equalsIgnoreCase("clip-path")) {
            System.out.println("Style clip-path is not supported in this version");
        } else {
            System.out.println("Unresolved style (" + attr + ",  "+val+")");
        }
    }


    /** translate following properties into a transform matrix
     * translate(xx)
     * translate(xx, xx)
     * scale(xx)
     * rotate(xx)
     *   skew-x(xx)
     *   skew-y(xx)
     *   matrix(x x x x x x)
     * Please be noticed that there is a sequence which should be kept during drawing
     */
    protected void readTransform(String s) {
        int start, end = -1;

        if(s == null) return;

        transformed = true;

        s.toLowerCase().trim();
        s = s.replace(',', ' ');        // get rid of ,

        while(true) {
            // advance to the next token. If end, break
            if(end+1 == s.length())
                break;
            s = s.substring(end+1).trim();
            if(s.startsWith("matrix")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub;
                if(end == -1) {
                    sub = s.substring(start+1);
                System.out.println("With no end ("+sub+")");
                } else {
                    sub = s.substring(start+1, end);
                }
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 6) {
                    Double d00 = new Double(st.nextToken());
                    Double d10 = new Double(st.nextToken());
                    Double d01 = new Double(st.nextToken());
                    Double d11 = new Double(st.nextToken());
                    Double d20 = new Double(st.nextToken());
                    Double d21 = new Double(st.nextToken());
                    AffineTransform at = new AffineTransform(
                        d00.doubleValue(), d10.doubleValue(),
                        d01.doubleValue(), d11.doubleValue(),
                        d20.doubleValue(), d21.doubleValue());
                    transform.concatenate(at);
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                if(start != -1 && end == -1) {
                    // This combination is of the last transformation
                    break;
                }
            } else if(s.startsWith("translate")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1 || end == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub = s.substring(start+1, end);
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 1) {
                    Double d = new Double(st.nextToken());
                    double dx =  d.doubleValue();
                    transform.translate(dx, 0d);
                } else if(st.countTokens() == 2) {
                    Double dx = new Double(st.nextToken());
                    Double dy = new Double(st.nextToken());
                    transform.translate(dx.doubleValue(), dy.doubleValue());
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }

            } else if(s.startsWith("scale")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1 || end == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub = s.substring(start+1, end);
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 1) {
                    Double d = new Double(st.nextToken());
                    transform.scale(d.doubleValue());
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
            } else if(s.startsWith("rotate")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1 || end == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub = s.substring(start+1, end);
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 1) {
                    Double d = new Double(st.nextToken());
                    transform.rotate(Math.toRadians(d.doubleValue()));
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
            } else if(s.startsWith("skew-x")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1 || end == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub = s.substring(start+1, end);
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 1) {
                    Double d = new Double(st.nextToken());
                    double rad = Math.toRadians(d.doubleValue());
                    AffineTransform at = new AffineTransform(1d, 0d, Math.tan(rad), 1d, 0d, 0d);
                    transform.concatenate(at);
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
            } else if(s.startsWith("skew-y")) {
                start = s.indexOf('(');
                end = s.indexOf(')');
                if(start == -1 || end == -1) {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
                String sub = s.substring(start+1, end);
                StringTokenizer st = new StringTokenizer(sub, " ,", false);
                if(st.countTokens() == 1) {
                    Double d = new Double(st.nextToken());
                    double rad = Math.toRadians(d.doubleValue());
                    AffineTransform at = new AffineTransform(1d, Math.tan(rad), 0d, 1d, 0d, 0d);
                    transform.concatenate(at);
                } else {
                    System.out.println("Unresolved transform " + s);
                    break;
                }
            } else {
                System.out.println("Unresolved transform");
                break;
            }
        }
    }


    // To be overrided by subclasses
    public void draw(GNode parent, Vector GNodes) {
    }

    public void addVisualComponentIntoTransform() {
        if(visualLeaf == null) {
            visualLeaf = new ZVisualLeaf();
        }
        for(int i=0;i<visualComps.size();i++) {
            ZVisualComponent vis =
                (ZVisualComponent)visualComps.elementAt(i);
            visualLeaf.addVisualComponent(vis);
        }
        this.transform.addChild(visualLeaf);
    }

    public void addVisualComponent(ZVisualLeaf visualLeaf) {
        if(visualLeaf == null)
            return;

        for(int i=0;i<visualComps.size();i++) {
            ZVisualComponent vis =
                (ZVisualComponent)visualComps.elementAt(i);
            visualLeaf.addVisualComponent(vis);
        }
    }

    public void printName() {
        System.out.println("Node : " + node.getNodeName());
    }

    public void printPath() {
        String s = "";
        System.out.print("Node : " + node.getNodeName()+"  ");
        for(int i=0;i<path.size();i++) {
            s = s + "." + (String)path.elementAt(i);
        }
        System.out.println("Path : " + s);
    }

    public String getNodeName() {
        return node.getNodeName().trim();
    }

    protected void copyStyleFromParent() {
        if(gparent == null) return;

        contextURL = gparent.contextURL;

        fontSize = gparent.fontSize;

        // Stroke attributes
        stroke = gparent.stroke;
        strokeWidth = gparent.strokeWidth;
        strokeLineCap = gparent.strokeLineCap;
        strokeLineJoin = gparent.strokeLineJoin;
        strokeMiterLimit = gparent.strokeMiterLimit;
        strokeDashArray = gparent.strokeDashArray;


        fillColor = gparent.fillColor;
        currentColor = gparent.currentColor;
    }

    //
    // Commonly used colors
    //
    static final String[] namedColors = {
        "black", "silver", "gray", "white", "maroon",
        "red",   "purple", "fuchsia", "green", "lime",
        "olive", "yellow", "navy", "blue", "teal",
        "aqua",  "gold",  "seagreen", "peru", "orange",
        "chocolate", "crimson", "linen", "yellowgreen", "lemonchiffon",
        "burlywood", "forestgreen", "violet", "sienna", "sandybrown",
        "turquoise", "rosybrown", "cyan",
    };
    //
    // Code for those commonly used colors
    //
    static final int[] colors = {
        0x000000, 0xC0C0C0, 0x808080, 0xFFFFFF, 0x800000,
        0xff0000, 0x800080, 0xFF00FF, 0x008000, 0x00FF00,
        0x808000, 0xFFFF00, 0x000080, 0x0000FF, 0x008080,
        0x00FFFF, 0xFFD700, 0x2E8B57, 0xCD853F, 0xFFA500,
        0xD2691E, 0xDC143C, 0xFAF0E6, 0x9ACD32, 0xFFFACD,
        0xDEB887, 0x228B22, 0xEE82EE, 0xA0522D, 0xF4A460,
        0x40E0D0, 0xBC8F8F, 0x00FFFF
    };
}