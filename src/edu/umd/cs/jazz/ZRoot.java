/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz;

import java.awt.geom.AffineTransform;
import java.io.*;
import edu.umd.cs.jazz.util.*;

/** 
 * <b>ZRoot</b> exteneds ZNode overiding several methods of ZNode to ensure that ZRoot is
 * always in the root position of a Scenegraph.
 * 
 * @author Ben Bederson
 * @author Britt McAlister
 */
public class ZRoot extends ZGroup implements Serializable {
    /**
     * Overrides ZNode.setParent() to throw an exception if an
     * attempt to set the parent of a ZRoot is made.
     * @param parent parameter is not used.
     */
    protected void setParent(ZNode parent) throws RuntimeException {
	throw new RuntimeException("Can't set parent of ZRoot");
    }
}
