/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import java.util.Vector;
import java.io.InputStream;
import java.io.File;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZSVG</b> reads SVG format file and load objects into jazz scenegraph.
 * @author  Bongwon Suh
 *
 * Usage:       ZSVG svg = new ZSVG(input);
 *              ZGroup group = svg.getGroup();
 *
 *              or
 *
 *              ZSVG svg = new ZSVG();
 *              ZGroup group = svg.read(input);
 *
 *
 *      input:  can be one of the following
 *              java.lang.String uri    URI as string e.g. http://my.site/sample.svg
 *              java.io.File file       opened file descripter e.g. new File("sample.svg")
 *              java.io.InputStream is  inputstream opened for svg data
 *
 *
 * Limitations: The current SVG reader does not support the following features
 *      Clipping
 *      Dynamic behavior
 *      Anchors
 *      Style
 *      Limited font support
 *      Gradient
 *      Ornament on shapes (e.g. dashed/rounded rectangles)
 *      Non-numeric unit (e.g. 300px, 2.5in, etc)
 *
 */
public class ZSVG {
    /**
     * SVG objects are drawn on this canvas.
     */
    protected ZGroup group = new ZGroup();

    /**
     * Default Constructor
     */
    public ZSVG() {
    }

    /**
     * Load SVG data from inputstream and add objects on the group.
     * @param <code>in</code> read SVG data from the inputstream.
     * Warning: if you use inputstream as SVG input, you might have problems with DTD location.
     * DTD Definition should be specified by URL, not by relative path from the current location
     */
    public ZSVG(InputStream in) {
        read(in);
    }

    /**
     * Load SVG data from specified URL (String form) and add objects on my group.
     * @param <code>url</code> read SVG data from the URL (String form).
     */
    public ZSVG(String url) {
        read(url);
    }

    /**
     * Load SVG data from the file and add objects on the group.
     * @param <code>file</code> read SVG data from the file.
     */
    public ZSVG(File file) {
        read(file);
    }

    /**
     * Load SVG data from the inputsream and begin to draw objects.
     * @param <code>in</code> read SVG data from the inputstream.
     */
    public ZGroup read(InputStream in) {
        SVG svg = new SVG();
        svg.loadFromStream(in);
        addObject(svg);
        return group;
    }

    /**
     * Load SVG data from the file and begin to draw objects.
     * @param <code>in</code> read SVG data from the File in.
     */
    public ZGroup read(File in) {
        SVG svg = new SVG();
        svg.loadFromFile(in);
        addObject(svg);
        return group;
    }

    /**
     * Load SVG data from the url and begin to draw objects.
     * @param <code>url</code> read SVG data from the url (Warning: String url)
     */
    public ZGroup read(String url) {
        SVG svg = new SVG();
        svg.loadFromURI(url);
        addObject(svg);
        return group;
    }

    /**
     * Read objects and begin to add them from the root of DOM tree
     */
    private void addObject(SVG svg) {
        Vector GNodes = new Vector();
        GNode root = svg.getRoot();

        if(root == null || !(root instanceof GSVG))
            return;

        GNodes.addElement(root);

        GNode node = null;
        do {
            node = svg.getNextNode(GNodes);
            if(node != null) {
                GNodes.addElement(node);
            }
        } while(node != null);

        ((GSVG)root).draw(GNodes, group);
    }

    /**
     * Return ZGroup group, which has all nodes as children
     */
    public ZGroup getGroup() {
        return group;
    }
}