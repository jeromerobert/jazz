/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.utiltest;

import junit.ui.*;
import junit.framework.*;

public class RunAllPackageUnitTests {

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite ( ) {
        TestSuite suite= new TestSuite();
        suite.addTest(new TestSuite(ZSceneGraphEditorTest.class));
        suite.addTest(new TestSuite(ZListImplTest.class));
        suite.addTest(new TestSuite(ZNullListTest.class));
        suite.addTest(new TestSuite(ZCanvasTest.class));
        suite.addTest(new TestSuite(ZPriorityQueueTest.class));
        return suite;
    }
}