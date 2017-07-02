/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

/** A general exception used to signal various types of illegal operations
 * 
 * @author Ben Bederson
 * @author Britt McAlister
 */
public class ZOperationNotAllowedException extends Exception {
    public ZOperationNotAllowedException() {
    }

    public ZOperationNotAllowedException(String msg) {
	super(msg);
    }
}
