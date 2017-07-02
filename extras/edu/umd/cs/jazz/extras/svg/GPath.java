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
import java.awt.geom.*;
import java.awt.geom.Point2D.Float;
import java.awt.BasicStroke;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;


/**
 *   Support <path> tag in SVG
 *   This class is most widely used when handling adobe illustrator files
 *   angle is not supported now. It is drawn as a line.
 *
 *   Sample path data

  <g style="stroke:blue ; stroke-width:5 ; fill:none" >
    <path d="M 100,100" >
      <data d="L 100,200" />
      <data d="L 200,200" />
      <data d="L 200,100z" />
    </path>

    <path style="fill:red ; stroke:blue" d="M250,100" >
      <data d="v 100 h 100 v-100z" />
    </path>

    <path style="fill:yellow ; stroke:blue" d="M 400,100 v 100 h 100 v-100" >
      <data d="z" />
    </path>
  </g>

  <g style="fill:none; stroke:blue">
    <path d="M 50,50  h 50 v 50 h -50 z"/>
    <path d="M 110,50 h 50 v 50 h -50 v -50"/>
    <path d="m 50,110 h 50 v 50 h -50 z" />
  </g>

  <g style="fill:none; stroke:green">
    <path d="M 50,50  L 100,50 L 100,100 L 50,100 z "/>
    <path d="M 50,110 l 50,0 l 0,50 l -50,0 z "/>
    <path d="m 110,50,50,0,0,50,-50,0 z "/>
    <path d="M 110,110,160,110,160,160,110,160 z" />

  </g>
 */
public class GPath extends GNode {
    int x, y;
    int width, height;
    URL url = null;

    Point2D.Float current = new Point2D.Float(0,0);
    Point2D.Float lastMoveTo = new Point2D.Float(0,0);
    //GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
    GeneralPath path = new GeneralPath();

    public GPath(Node _node, GNode parent) {
        super(_node, parent);

        for(int i=0; i<attributes.size(); i++) {
            Node child = (Node)attributes.elementAt(i);

            if(child.getNodeType() == SVG.ATTR_TYPE) {
                Attr attr = (Attr)child;
                if(attr.getName().equalsIgnoreCase("d")) {
                    readPath(attr.getValue());
                } else {
                    System.out.println("Unresolved attribute (" + attr.getName()
                        + ",  "+attr.getValue()+")");
                }
            }
        }

        NodeList nodeList = node.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            org.w3c.dom.Node child = nodeList.item(i);
            int type =  child.getNodeType();
            if(type == SVG.ELEMENT_TYPE && child.getNodeName().equalsIgnoreCase("data")) {
                Element e = (Element)child;
                NamedNodeMap attrs = ((Element)child).getAttributes();
                if(attrs == null)
                    continue;
                for(int j=0; j<attrs.getLength(); j++) {
                    Node attr = attrs.item(j);
                    if(attr.getNodeType() == SVG.ATTR_TYPE
                        && ((Attr)attr).getName().equalsIgnoreCase("d")) {
                        readPath(((Attr)attr).getValue());
                    } else {
                        // attribute other than d
                        System.out.println("Unresolved attribute (" + attr
                                + ",  "+((Attr)attr).getValue()+")");
                    }
                }
            } else {
                // child other than data element
                // Ignore !!
            }
        }

        if(fillRule == GNode.NONZERO) {
            path.setWindingRule(GeneralPath.WIND_NON_ZERO);
        } else if(fillRule == GNode.EVENODD) {
            path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
        }

        ZPath obj = new ZPath(path);

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

        if(fillColor != null) {
            obj.setFillPaint(fillColor);
        }

        visualComps.addElement(obj);


    }

    protected boolean isClosed(Vector points) {
        if(points.size() <= 2)
            return false;

        Point2D.Float first = (Point2D.Float)points.elementAt(0);
        Point2D.Float last = (Point2D.Float)points.elementAt(points.size()-1);
        if(first.equals(last))
            return true;
        return false;
    }

    protected String fixString(String s) {
        // process ugly format  0.10-0.50 --> 0.10 -0.50

        char[] old = s.toCharArray();
        char[] result = new char[old.length*2];
        int count = 0;
        for(int i=0;i<old.length;i++) {
            if(old[i] == '-') {
                // insert space
                result[count++] = ' ';
            }
            result[count++] = old[i];
        }
        return new String(result, 0, count);
    }

    protected Vector readPoints(String s, char pathType) {

        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();
        boolean firstIteration = true;
        if((st.countTokens() % 2) != 0) {
            System.out.println("Wrong coordinate in " + s);
            return vp;
        }

        while(st.hasMoreTokens()) {
            String token1 = st.nextToken().trim();
            String token2 = st.nextToken().trim();

            float xval = java.lang.Float.parseFloat(token1);
            float yval = java.lang.Float.parseFloat(token2);

            Point2D.Float p;
            if(Character.isUpperCase(pathType)) {
                p = new Point2D.Float(xval, yval);
            } else {    // relative
                p = new Point2D.Float(xval+current.x, yval+current.y);
            }
            vp.addElement(p);
            current = p;

        }
        return vp;
    }

    protected Vector readCubicPoints(String s, char pathType) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();
        if((st.countTokens() % 6) != 0) {
            return vp;
        }

        while(st.hasMoreTokens()) {
            String token1 = st.nextToken().trim();
            String token2 = st.nextToken().trim();
            String token3 = st.nextToken().trim();
            String token4 = st.nextToken().trim();
            String token5 = st.nextToken().trim();
            String token6 = st.nextToken().trim();


            float x1 = java.lang.Float.parseFloat(token1);
            float y1 = java.lang.Float.parseFloat(token2);
            float x2 = java.lang.Float.parseFloat(token3);
            float y2 = java.lang.Float.parseFloat(token4);
            float x = java.lang.Float.parseFloat(token5);
            float y = java.lang.Float.parseFloat(token6);

            Point2D.Float p1, p2, p;
            if(Character.isUpperCase(pathType)) {
                p1 = new Point2D.Float(x1, y1);
                p2 = new Point2D.Float(x2, y2);
                p = new Point2D.Float(x, y);
            } else {    // relative
                p1 = new Point2D.Float(x1+current.x, y1+current.y);
                p2 = new Point2D.Float(x2+current.x, y2+current.y);
                p = new Point2D.Float(x+current.x, y+current.y);
            }

            vp.addElement(p1);
            vp.addElement(p2);
            vp.addElement(p);
            current = p;
        }
        return vp;
    }

    protected Vector readDualPoints(String s, char pathType) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();
        if((st.countTokens() % 4) != 0) {
            return vp;
        }

        while(st.hasMoreTokens()) {
            String token1 = st.nextToken().trim();
            String token2 = st.nextToken().trim();
            String token3 = st.nextToken().trim();
            String token4 = st.nextToken().trim();

            float x1 = java.lang.Float.parseFloat(token1);
            float y1 = java.lang.Float.parseFloat(token2);
            float x = java.lang.Float.parseFloat(token3);
            float y = java.lang.Float.parseFloat(token4);

            Point2D.Float p1, p;
            if(Character.isUpperCase(pathType)) {
                p1 = new Point2D.Float(x1, y1);
                p = new Point2D.Float(x, y);
            } else {    // relative
                p1 = new Point2D.Float(x1+current.x, y1+current.y);
                p = new Point2D.Float(x+current.x, y+current.y);
            }

            vp.addElement(p1);
            vp.addElement(p);
            current = p;
        }
        return vp;
    }

    protected Vector readHorizontalPoints(String s, char pathType) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();

        while(st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            float xval = java.lang.Float.parseFloat(token);
            float yval = current.y;

            Point2D.Float p;
            if(Character.isUpperCase(pathType)) {
                p = new Point2D.Float(xval, yval);
            } else {    // relative
                p = new Point2D.Float(xval+current.x, yval);
            }
            vp.addElement(p);
            current = p;
        }
        return vp;
    }

    protected Vector readVerticalPoints(String s, char pathType) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();

        while(st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            float xval = current.x;
            float yval = java.lang.Float.parseFloat(token);

            Point2D.Float p;
            if(Character.isUpperCase(pathType)) {
                p = new Point2D.Float(xval, yval);
            } else {    // relative
                p = new Point2D.Float(xval, yval+current.y);
            }
            vp.addElement(p);
            current = p;
        }
        return vp;
    }

    protected void addLinkToPath(GeneralPath path, Vector points) {
        for(int i=0;i<points.size();i++) {
            Point2D.Float p = (Point2D.Float)points.elementAt(i);
            path.lineTo(p.x, p.y);
        }
    }

    protected void processArc(String s, GeneralPath path, char pathType) {
        StringTokenizer st = new StringTokenizer(s, " ,");
        Vector vp = new Vector();
        boolean largeArc = false;
        boolean sweep = false;

        //(rx ry x-axis-rotation large-arc-flag sweep-flag x y)+
        if((st.countTokens() % 7) != 0) {
            return;
        }
        while(st.hasMoreTokens()) {
            float rx = java.lang.Float.parseFloat(st.nextToken());
            float ry = java.lang.Float.parseFloat(st.nextToken());
            float rot = java.lang.Float.parseFloat(st.nextToken());
            int arcFlag = Integer.parseInt(st.nextToken());
            int sweepFlag = Integer.parseInt(st.nextToken());
            if(arcFlag == 1) {
                largeArc = true;
            }
            if(sweepFlag == 1) {
                sweep = true;
            }
            float x = java.lang.Float.parseFloat(st.nextToken());
            float y = java.lang.Float.parseFloat(st.nextToken());

            Point2D.Float p;
            if(Character.isUpperCase(pathType)) {
                p = new Point2D.Float(x, y);
            } else {    // relative
                p = new Point2D.Float(x+current.x, y+current.y);
            }


            Point2D upLeft = path.getCurrentPoint();
            Point2D upRight = new Point2D.Double(current.getX()+rx, current.getY());
            Point2D downLeft = new Point2D.Double(current.getX(), current.getY()+ry);
            Point2D downRight = new Point2D.Double(current.getX()+rx, current.getY()+ry);

            AffineTransform.getRotateInstance(rot).transform(upRight, upRight);
            AffineTransform.getRotateInstance(rot).transform(downLeft, downLeft);
            AffineTransform.getRotateInstance(rot).transform(downRight, downRight);

            path.lineTo((float)upRight.getX(), (float)upRight.getY());
            path.lineTo((float)downRight.getX(), (float)downRight.getY());
            path.lineTo((float)downLeft.getX(), (float)downLeft.getY());
            path.lineTo((float)upLeft.getX(), (float)upLeft.getY());

            path.lineTo((float)p.getX(), (float)p.getY());

            current = p;
        }
    }


    protected void readPath(String s) {
        if(s == null) return;

        StringTokenizer st = new StringTokenizer(fixString(s), "MmHhVvLlAaCcSsQqTtZz", true);
        Vector points = null;

        final int NONE = 0;
        final int CUBIC_BAZIER_CURVE = 1;
        final int QUADRATIC_BAZIER_CURVE = 2;
        int curve = NONE;
        Point2D.Float controlPoint = null;

        while(st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if(token.length() == 0)
                continue;
            char c = token.charAt(0);   // Mode such as M l q etc

            switch(c) {
                case 'M' :      // Move
                case 'm' :
                    points = readPoints(st.nextToken().trim(), c);
                    if(points.size() == 0) {
                        // Something must be wrong with this data
                        // So ignore it..
                        System.out.println("Wrong Pair " + c);
                        break;
                    }
                    Point2D.Float move = (Point2D.Float)points.elementAt(0);
                    path.moveTo(move.x, move.y);
                    lastMoveTo = move;
                    points.remove(0);
                    addLinkToPath(path, points);
                    curve = NONE;
                    break;
                case 'H' :      // Horizontal ...
                case 'h' :      // (x)+
                    points = readHorizontalPoints(st.nextToken().trim(), c);
                    addLinkToPath(path, points);
                    curve = NONE;
                    break;
                case 'V' :      // Vertical (y)+
                case 'v' :
                    points = readVerticalPoints(st.nextToken().trim(), c);
                    addLinkToPath(path, points);
                    curve = NONE;
                    break;
                case 'L' :      // Line to
                case 'l' :
                    points = readPoints(st.nextToken().trim(), c);
                    addLinkToPath(path, points);
                    curve = NONE;
                    break;
                case 'A' :      // Arc (ellipse)
                                //(rx ry x-axis-rotation large-arc-flag sweep-flag x y)+
                case 'a' :
                    processArc(st.nextToken().trim(), path, c);
                    curve = NONE;
                    break;

                case 'Z' :      // Close Path : handled in readPoints, processXXX
                case 'z' :
                    path.lineTo(lastMoveTo.x, lastMoveTo.y);
                    current = lastMoveTo;
                    curve = NONE;
                    break;

                case 'C' :      // Cubic bezier
                case 'c' :      // (x1 y1 x2 y2 x y)+
                    points = readCubicPoints(st.nextToken().trim(), c);
                    if((points.size() % 3) != 0 || points.size() == 0) {
                        // Wrong pair of values
                        System.out.println("Wrong Pair " + c);
                        break;
                    }
                    int iteration = (int)(points.size()/3);
                    Point2D.Float p1, p2, p;
                    float controlX=0f, controlY=0f;
                    for(int i=0;i<iteration;i++) {
                        p1 = (Point2D.Float)points.elementAt(i*3);
                        p2 = (Point2D.Float)points.elementAt(i*3+1);
                        p = (Point2D.Float)points.elementAt(i*3+2);
                        path.curveTo(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
                        current = p;
                        controlX = 2*p.x-p2.x;
                        controlY = 2*p.y-p2.y;
                    }
                    if(iteration > 0) {
                        controlPoint = new Point2D.Float(controlX, controlY);
                        curve = CUBIC_BAZIER_CURVE;
                    } else {
                        curve = NONE;
                    }
                    break;
                case 'S' :      // Smooth curve to (shorthand bezier)
                case 's' :      // (x2 y2 x y)+
                    points = readDualPoints(st.nextToken().trim(), c);
                    if((points.size() % 2) != 0) {
                        // Wrong data, ignore!
                        System.out.println("Wrong Pair " + c);
                        break;
                    }
                    iteration = (int)(points.size()/2);
                    controlX = controlY = 0f;   // stupid
                    for(int i=0;i<iteration;i++) {
                        if(i == 0) {
                            if(curve != CUBIC_BAZIER_CURVE) {
                                p1 = current;
                            } else {
                                p1 = controlPoint;
                            }
                        } else {
                            p1 = new Point2D.Float(controlX, controlY);
                        }
                        p2 = (Point2D.Float)points.elementAt(i*2);
                        p = (Point2D.Float)points.elementAt(i*2+1);
                        path.curveTo(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
                        current = p;
                        controlX = 2*p.x-p2.x;
                        controlY = 2*p.y-p2.y;
                    }
                    if(iteration > 0) {
                        curve = CUBIC_BAZIER_CURVE;
                        controlPoint = new Point2D.Float(controlX, controlY);
                    } else {
                        curve = NONE;
                    }
                    break;
                case 'Q' :      // quadratic bezier curveto
                case 'q' :      // (x1 y1 x y)+
                    points = readDualPoints(st.nextToken().trim(), c);
                    if((points.size() % 2) != 0) {
                        // Wrong data, ignore!
                        System.out.println("Wrong Pair " + c);
                        break;
                    }
                    iteration = (int)(points.size()/2);
                    controlX = controlY = 0f;   // stupid
                    for(int i=0;i<iteration;i++) {
                        p1 = (Point2D.Float)points.elementAt(i*2);
                        p = (Point2D.Float)points.elementAt(i*2+1);
                        path.quadTo(p1.x, p1.y, p.x, p.y);
                        current = p;
                        controlX = 2*p.x-p1.x;
                        controlY = 2*p.y-p1.y;
                    }
                    if(iteration > 0) {
                        curve = QUADRATIC_BAZIER_CURVE;
                        controlPoint = new Point2D.Float(controlX, controlY); //reflection...
                    } else {
                        curve = NONE;
                    }
                    break;
                case 'T' :      // Shorthand/smooth quadratic bezier curveto
                case 't' :      // (x y)+
                    points = readPoints(st.nextToken().trim(), c);
                    if(points.size() == 0) {
                        System.out.println("Wrong Pair " + c);
                        break;
                    }
                    controlX = controlY = 0f;   // stupid
                    for(int i=0;i<points.size();i++) {
                        if(i == 0) {
                            if(curve != QUADRATIC_BAZIER_CURVE) {
                                p1 = current;   // p1 = p if not following...
                            } else {
                                p1 = controlPoint;
                            }
                        } else {
                            p1 = new Point2D.Float(controlX, controlY);
                        }
                        p = (Point2D.Float)points.elementAt(i);
                        path.quadTo(p1.x, p1.y, p.x, p.y);
                        //path.lineTo(p.x, p.y);
                        current = p;
                        controlX = 2*p.x-p1.x;
                        controlY = 2*p.y-p1.y;
                    }
                    if(points.size() > 0) {
                        curve = QUADRATIC_BAZIER_CURVE;
                        controlPoint = new Point2D.Float(controlX, controlY);
                    } else {
                        curve = NONE;
                    }
                    break;
                default:        // Unrecognized symbol
                    System.out.println("Unrecognized Path symbol in " + token);
                    break;
            }
        }
        //printPath(path);
    }

    private void printPoints(Vector points) {
        for(int i=0;i<points.size();i++) {
            System.out.println("P["+i+"] " + points.elementAt(i));
        }
    }
    private void printPath(GeneralPath path) {
        AffineTransform at = new AffineTransform();
        PathIterator i = path.getPathIterator(at);
        while(!i.isDone()) {
            float[] data = new float[6];
            int current = i.currentSegment(data);
            switch(current) {
                case PathIterator.SEG_MOVETO :
                    System.out.println("MoveTo ("+data[0]+","+data[1]+")");
                    break;
                case PathIterator.SEG_LINETO :
                    System.out.println("LineTo ("+data[0]+","+data[1]+")");
                    break;
                case PathIterator.SEG_QUADTO :
                    System.out.println("QuadTo ("+data[0]+","+data[1]+") ("
                        +data[2]+","+data[3]+") ("+data[4]+","+data[5]+")");
                    break;
                case PathIterator.SEG_CUBICTO :
                    System.out.println("CubicTo ("+data[0]+","+data[1]+") ("
                        +data[2]+","+data[3]+") ("+data[4]+","+data[5]+")");
                    break;
                case PathIterator.SEG_CLOSE :
                    System.out.println("Close");
                    break;
                default :
                    break;
            }

            i.next();
        }
    }

}