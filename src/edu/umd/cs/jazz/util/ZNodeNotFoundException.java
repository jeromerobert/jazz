/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.io.*;

/** 
 * <b>ZNodeNotFoundException</b> is an exception that is thrown
 * to indicate that a node that was being searched
 * for in the scenegraph was not found.
 * @author Ben Bederson
 */

public class ZNodeNotFoundException extends RuntimeException implements Serializable {
    public ZNodeNotFoundException() {
    }

    public ZNodeNotFoundException(String msg) {
	super(msg);
    }
}
