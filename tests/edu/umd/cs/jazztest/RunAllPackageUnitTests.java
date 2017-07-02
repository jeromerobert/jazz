/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import junit.ui.*;
import junit.framework.*;

public class RunAllPackageUnitTests {

    public static Test suite ( ) {
        TestSuite suite= new TestSuite();
        suite.addTest(new TestSuite(ZVisualGroupTest.class));
        suite.addTest(new TestSuite(ZVisualLeafTest.class));
        suite.addTest(new TestSuite(ZGroupTest.class));
        suite.addTest(new TestSuite(ZNodeTest.class));
        suite.addTest(new TestSuite(ZCameraTest.class));
        suite.addTest(new TestSuite(ZTransformGroupTest.class));
        suite.addTest(new TestSuite(ZLayerGroupTest.class));
        suite.addTest(new TestSuite(ZClipGroupTest.class));
        suite.addTest(new TestSuite(ZSelectionGroupTest.class));
        return suite;
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}