/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import junit.ui.*;
import junit.framework.*;

public class RunAllUnitTests {
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

    public static Test suite ( ) {
        TestSuite suite= new TestSuite();
        suite.addTest(edu.umd.cs.jazztest.RunAllPackageUnitTests.suite());
        suite.addTest(edu.umd.cs.jazztest.componenttest.RunAllPackageUnitTests.suite());
        suite.addTest(edu.umd.cs.jazztest.animationtest.RunAllPackageUnitTests.suite());
        suite.addTest(edu.umd.cs.jazztest.eventtest.RunAllPackageUnitTests.suite());
        suite.addTest(edu.umd.cs.jazztest.iotest.RunAllPackageUnitTests.suite());
        suite.addTest(edu.umd.cs.jazztest.utiltest.RunAllPackageUnitTests.suite());
        return suite;
    }
}