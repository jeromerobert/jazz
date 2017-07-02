/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.componenttest;

import junit.ui.*;
import junit.framework.*;

public class RunAllPackageUnitTests {
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite ( ) {
        TestSuite suite= new TestSuite();
        suite.addTest(new TestSuite(ZTextTest.class));
        suite.addTest(new TestSuite(ZImageTest.class));
        suite.addTest(new TestSuite(ZLabelTest.class));
        suite.addTest(new TestSuite(ZRoundedRectangleTest.class));
        suite.addTest(new TestSuite(ZLineTest.class));
        suite.addTest(new TestSuite(ZPolygonTest.class));
        suite.addTest(new TestSuite(ZArcTest.class));
        suite.addTest(new TestSuite(ZEllipseTest.class));
        suite.addTest(new TestSuite(ZPathTest.class));
        suite.addTest(new TestSuite(ZQuadCurveTest.class));
        suite.addTest(new TestSuite(ZSwingTest.class));
        suite.addTest(new TestSuite(ZPolylineTest.class));
        suite.addTest(new TestSuite(ZBasicVisualComponentTest.class));
        return suite;
    }
}