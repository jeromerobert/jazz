/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;

import edu.umd.cs.jazz.scenegraph.*;

/** 
 * The ZDebug class is not inteneded to be instantiated.  Rather it provides 
 * static methods for maintaining/setting/retrieving global debugging state.
 *
 * @author Ben Bederson
 * @author Britt McAlister
 * @see ZNode
 */
public class ZDebug {
    static public final int DEBUG_NONE = 0;
    static public final int DEBUG_ALL = 1;
    static public final int DEBUG_DAMAGE = 2;
    static public final int DEBUG_PAINT = 3;
    static public final int DEBUG_TIME = 4;

    /** 
     * Flag denoting the current debug state.
     */
    static protected int debug                   = DEBUG_NONE;
    
    /** 
     * Flag denoting whether the bounds of each object are being rendered
     */
    static protected boolean showBounds              = false;
    
    /** 
     * Camera that is associated with bounds.  It is used show that when
     * damaging nodes and displaying bounds, we know what magnification
     * we are at so we can compute appropriate thin bounds.
     */
    static protected ZCamera boundsCamera            = null;
    
    /** 
     * Flag denoting whether there is a display to help debug region management
     */
    static protected boolean debugRegionMgmt         = false;
    
    /** 
     * Flag denoting whether a counter should be kept and incremented
     * each time a ZNode (that contains a visual) is painted
     */
    static protected boolean trackNodePaint          = false;
    
    /** 
     * Tally of the number of ZNodes (that contain a visual) that 
     * have been painted
     */
    static protected int paintCount;

    
    /** 
     * Controls whether debug statements should be sent to stdout for various
     * kinds of internal debugging. The legal kinds of debugging are:
     *   DEBUG_NONE    - No Debugging
     *   DEBUG_ALL     - Debug everything
     *   DEBUG_DAMAGE  - Debug damage()
     *   DEBUG_PAINT   - Debug paint()
     *   DEBUG_TIME    - Debug render timing
     * @param value 
     */
    static public void setDebug(int value) {
	debug = value;
    }

    /** 
     * Returns the current state of debugging output
     * @return the thing being debugged.  See <code>setDebug</code> for legal values
     */
    static public int getDebug() {
	return debug;
    }

    /** 
     * Controls whether the bounds of each object should be drawn as a debugging aid.
     * Caller must also specify the camera that should be used in computing how to display bounds.
     * @param showBounds true to show bounds, or false to hide bounds
     * @param camera The camera the bounds should be scaled for.
     */
    static public void setShowBounds(boolean showBounds, ZCamera camera) {
	ZDebug.showBounds = showBounds;
	boundsCamera = camera;
    }

    /** 
     * Returns the current state of showBounds
     * @return <code>true</code> if showBounds is on, or <code>false</code> otherwise
     */
    static public boolean getShowBounds() {
	return showBounds;
    }

    /** 
     * Returns the camera associated with the bounds.  It is used to compute the bounds line thickness.
     * @return The camera associated with the bounds.
     */
    static public ZCamera getBoundsCamera() {
	return boundsCamera;
    }

    /** 
     * Controls whether the region currently being painted should be indicated with
     * a shadowed area on the screen.
     * @param value 
     */
    static public void setDebugRegionMgmt(boolean value) {
	debugRegionMgmt = value;
    }

    /** 
     * Returns the current state of debugRegionMgmt
     * @return <code>true</code> if debugRegionMgmt is on, or <code>false</code> otherwise
     */
    static public boolean getDebugRegionMgmt() {
	return debugRegionMgmt;
    }

    /** 
     * Dump the subGraph passed in to stdout
     * @param subGraph 
     */
    static public void dump(ZNode subGraph) {
	dump(subGraph, 0);
    }

    /** 
     * Helper method for Dump(ZNode subGraph).  This method handles pretty indenting
     * of each level as it recurses down the tree.
     * 
     * @param level 
     * @param subGraph 
     */
    static protected void dump(ZNode subGraph, int level) {
	String space = "";

	for (int i=0; i<level; i++) {
	    space = space.concat("    ");
	}
	dumpElement(space, "* ", subGraph.toString());
	System.out.println(space + "  - Transform:     " + subGraph.getTransform().getAffineTransform());
	System.out.println(space + "  - Comp Bounds:   " + subGraph.getGlobalCompBounds());
	System.out.println(space + "  - Global Bounds: " + subGraph.getGlobalBounds());
	if (subGraph.isVolatile()) {
	    System.out.println(space + "  - Volatile");
	}
	if (!(subGraph.getVisualComponent() instanceof ZDummyVisualComponent)) {
	    dumpElement(space, "  - Visual Comp:   ", subGraph.getVisualComponent().toString());
	}

	Vector children = subGraph.getChildren();
	if (!children.isEmpty()) {
	    System.out.println(space + "  - Children:      ");
	    for (Iterator i=children.iterator(); i.hasNext();) {
		ZNode child = (ZNode)i.next();
		if (child.getParent() != subGraph) {
		    System.out.println();
		    System.out.println("WARNING: parent pointer of " + child + " not equal to " + subGraph);
		    System.out.println("WARNING: instead it is set to " + child.getParent());
		    System.out.println();
		}
		dump(child, level + 1);
	    }
	}

	Vector properties = subGraph.getProperties();
	if ((properties != null) && (!properties.isEmpty())) {
	    System.out.println(space + "  - Properties:      ");
	    for (Iterator i=properties.iterator(); i.hasNext();) {
		ZProperty prop = (ZProperty)i.next();
		dumpElement(space, "  - Property      ", prop.getKey() + ": " + prop.getValue());
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
	tokenizer.wordChars('@', '@');
	tokenizer.wordChars('(', '(');
	tokenizer.wordChars(')', ')');
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

    /** Pass true to this method to enable gathering info relating to how many nodes are
     * painted each render.
     * 
     * @param value 
     * @see #clearPaintCount(Object)
     * @see #incPaintCount(Object)
     * @see #getPaintCount(Object)
     */
    static public void setTrackNodePaint(boolean value) {
	trackNodePaint = value;
    }

    /** Clears the paint count.  This method should be called at the begining of each render
     * 
     * @param g 
     * @see #setTrackNodePaint
     * @see #incPaintCount
     * @see #getPaintCount
     */
    static public void clearPaintCount(Object g) {
	// we'll use the Object (an instance of a Graphics) later when using multiple cameras
	paintCount = 0;
    }

    
    /** Call this method whenever a node paints itself
     * 
     * @param g 
     * @see #setTrackNodePaint
     * @see #clearPaintCount
     * @see #getPaintCount
     */
    static public void incPaintCount(Object g) {
	paintCount ++;
    }

    
    /** Returns the number of nodes that painted themselves during the last render
     * 
     * @param g 
     * @see #setTrackNodePaint
     * @see #clearPaintCount
     * @see #incPaintCount
     */
    static public int getPaintCount(Object g) { 
	return paintCount;
    }
}
