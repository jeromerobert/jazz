/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.ZSceneGraphObject;
import java.io.*;

/** 
 * <b>Thrown</b> to indicate that a node that was being searched
 * for in the scenegraph was not found.
 * @author Ben Bederson
 */

public class ZDanglingReferenceException extends RuntimeException implements Serializable {
    ZSceneGraphObject origObj;

    public ZDanglingReferenceException(ZSceneGraphObject origObj) {
	this.origObj = origObj;
    }

    public ZDanglingReferenceException(ZSceneGraphObject origObj, String msg) {
	super(msg);
	this.origObj = origObj;
    }

    public ZSceneGraphObject getOriginalObject() {
	return origObj;
    }
}
