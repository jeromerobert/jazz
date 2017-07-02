/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.eventtest;

import junit.framework.*;
import junit.swingui.*;

public class RunAllPackageUnitTestsWithGUI {
    public static void main (String[] args) {
        if (args.length == 0) {
            args = new String[1];
            args[0] = "edu.umd.cs.jazztest.eventtest.RunAllPackageUnitTests";
        }
        new TestRunner().start(args);
    }
}