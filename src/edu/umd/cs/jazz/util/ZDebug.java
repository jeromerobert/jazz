/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.io.*;
import java.util.Iterator;
import java.util.ArrayList;

import edu.umd.cs.jazz.*;

/**
 * <b>ZDebug</b> provides
 * static methods for maintaining/setting/retrieving global debugging state.
 * It is not inteneded to be instantiated.
 * @author Ben Bederson
 * @author Britt McAlister
 * @see ZNode
 */
public class ZDebug implements Serializable {
    /**
     * Flag denoting whether debugging in general should be enabled.
     * Settig thisto false will cause all debugging code will be optimized out
     * of Jazz during compilation.
     */
    static final public boolean debug              = false;

    /**
     * Flag denoting whether the bounds of each object are being rendered
     * Don't set this directly - rather call @link{#setShowBounds}.
     */
    static public boolean showBounds              = false;

    /**
     * Camera that is associated with bounds.  It is used show that when
     * damaging nodes and displaying bounds, we know what magnification
     * we are at so we can compute appropriate thin bounds.
     */
    static private ZCamera boundsCamera           = null;

    /**
     * Flag denoting whether there is a display to help debug region management
     */
    static public boolean debugRegionMgmt         = false;

    /**
     * Flag denoting whether to print debugging info related to render operations
     */
    static public boolean debugRender             = false;

    /**
     * Flag denoting whether to print debugging info related to repaint operations
     */
    static public boolean debugRepaint            = false;

    /**
     * Flag denoting whether to print debugging info related to timing
     */
    static public boolean debugTiming             = false;

    /**
     * Flag denoting whether to print debugging info related to picking
     */
    static public boolean debugPick             = false;

    /**
     * Tally of the number of ZNodes (that contain a visual) that
     * have been painted
     */
    static private int paintCount;

    /**
     * Controls whether the bounds of each object should be drawn as a debugging aid.
     * Caller must also specify the camera that should be used in computing how to display bounds.
     * @param showBounds true to show bounds, or false to hide bounds
     * @param camera The camera the bounds should be scaled for.
     */
    static public void setShowBounds(boolean showBounds, ZCamera camera) {
	if (boundsCamera != null) {
	    boundsCamera.repaint();
	}
	ZDebug.showBounds = showBounds;
	boundsCamera = camera;
	if (camera != null) {
	    camera.repaint();
	}
    }

    /** Clears the paint count.  This method should be called at the begining of each render
     *
     * @see #incPaintCount
     * @see #getPaintCount
     */
    static public void clearPaintCount() {
	paintCount = 0;
    }


    /** Call this method whenever a node paints itself
     *
     * @see #clearPaintCount
     * @see #getPaintCount
     */
    static public void incPaintCount() {
	paintCount++;
    }


    /** Returns the number of nodes that painted themselves during the last render
     *
     * @see #clearPaintCount
     * @see #incPaintCount
     */
    static public int getPaintCount() {
	return paintCount;
    }

    /**
     * Debugging function to dump the scenegraph rooted at the specified node to stdout.
     * It uses @link{ZSceneGraphObject#dump} to display each object, and descends the hierarchy.
     * @param node The root of the subtree to display
     */
    static public void dump(ZNode node) {
	dump(node, 0);
    }

    /**
     * Internal method for dump(ZNode node).  This method handles pretty indenting
     * of each level as it recurses down the tree.
     * @param node
     * @param level
     */
    static protected void dump(ZSceneGraphObject sgo, int level) {
	int i;
	String space = "";

	for (i=0; i<level; i++) {
	    space = space.concat("    ");
	}
	dumpElement(space, "* ", sgo.dump());

				// Dump children of group nodes here so that we can indent them on display properly
	if (sgo instanceof ZGroup) {
	    ZNode[] children = ((ZGroup)sgo).getChildren();
	    if (children.length > 0) {
		System.out.println(space + "  - Children:      ");
		for (i=0; i<children.length; i++) {
		    ZNode child = children[i];
		    if (child.getParent() != sgo) {
			System.out.println();
			System.out.println("WARNING: parent pointer of " + child + " not equal to " + sgo);
			System.out.println("WARNING: instead it is set to " + child.getParent());
			System.out.println();
		    }
		    dump(child, level + 1);
		}
	    }
	}

				// Dump visual components of certain nodes here so that we can indent them on display properly
	ZVisualComponent vc = null;
	if (sgo instanceof ZVisualLeaf) {
	    vc = ((ZVisualLeaf)sgo).getVisualComponent();
	    if (vc != null) {
		System.out.println(space + "  => Visual Component:      ");
		dump(vc, level + 1);
	    }
	}
	if (sgo instanceof ZVisualGroup) {
	    vc = ((ZVisualGroup)sgo).getFrontVisualComponent();
	    if (vc != null) {
		System.out.println(space + "  => Front Visual Component:      ");
		dump(vc, level + 1);
	    }
	    vc = ((ZVisualGroup)sgo).getBackVisualComponent();
	    if (vc != null) {
		System.out.println(space + "  => Back Visual Component:      ");
		dump(vc, level + 1);
	    }
	}
    }

    /**
     * Print the element for the scenegraph dump.
     * Parse the element, and if there are any newlines, space out
     * each line with the 'space' parameter.  Also, print the
     * header for the first line, and a matching number of spaces for ensuing lines.
     */
    static protected void dumpElement(String space, String origHeader, String element) {
	boolean done = false;
	boolean newLine = true;
	String header = origHeader;
	StringReader reader = new StringReader(element);
	StreamTokenizer tokenizer = new StreamTokenizer(reader);

	tokenizer.eolIsSignificant(true);
	tokenizer.wordChars('.', '.');
	tokenizer.wordChars(',', ',');
	tokenizer.wordChars(':', ':');
	tokenizer.wordChars('=', '=');
	tokenizer.wordChars('@', '@');
	tokenizer.wordChars('(', '(');
	tokenizer.wordChars(')', ')');
	tokenizer.wordChars('[', '[');
	tokenizer.wordChars(']', ']');
	tokenizer.wordChars('\'', '\'');
	do {
	    if (newLine) {
		System.out.print(space + header);
				// Replace header with a matching number spaces
		String temp = new String();
		for (int i=0; i<origHeader.length(); i++) {
		    temp = temp.concat(" ");
		}
		temp = temp.concat("- ");
		header = temp;
		newLine = false;
	    }
	    try {
		tokenizer.nextToken();
		switch (tokenizer.ttype) {
		case StreamTokenizer.TT_WORD:
		    System.out.print(tokenizer.sval + " ");
		    break;
		case StreamTokenizer.TT_NUMBER:
		    System.out.print(tokenizer.nval + " ");
		    break;
		case StreamTokenizer.TT_EOL:
		    System.out.println();
		    newLine = true;
		    break;
		case StreamTokenizer.TT_EOF:
		    done = true;
		    break;
		}
	    }
	    catch (IOException e) {
		System.out.println();
		System.out.println("Error parsing string while dumping scenegraph: " + element);
		done = true;
	    }
	} while (!done);
	System.out.println();
    }
}
