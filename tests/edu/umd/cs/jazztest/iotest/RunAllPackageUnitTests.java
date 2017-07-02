/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.iotest;

import junit.ui.*;
import junit.framework.*;

public class RunAllPackageUnitTests {

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite ( ) {
        TestSuite suite= new TestSuite();
        return suite;
    }
}