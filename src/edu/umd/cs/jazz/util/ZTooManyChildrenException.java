/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import edu.umd.cs.jazz.ZGroup;
import java.io.*;

/** 
 * <b>ZTooManyChildrenException</b> is an exception that is thrown
 * to indicate that an operation was attempted that would
 * have resulted in a decorator node having more than one child.
 * @author Ben Bederson
 */

public class ZTooManyChildrenException extends RuntimeException implements Serializable {
    ZGroup decorator = null;

    public ZTooManyChildrenException(ZGroup decorator) {
	this.decorator = decorator;
    }

    public ZTooManyChildrenException(ZGroup decorator, String msg) {
	super(msg);
	this.decorator = decorator;
    }

    /**
     * Get the decorator that the operation to add more than one child to was attempted on.
     * @return the decorator.
     */
    public ZGroup getDecorator() {
	return decorator;
    }
}
