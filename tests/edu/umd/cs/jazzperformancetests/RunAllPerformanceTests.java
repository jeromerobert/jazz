/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import junit.ui.*;
import junit.framework.*;

public class RunAllPerformanceTests {

    public static void main (String[] args) {
        ZPerformanceLog.initLog();
        junit.textui.TestRunner.run(suite());
        ZPerformanceLog.writeLog();
        System.exit(0);
    }

    public static Test suite ( ) {
        TestSuite suite= new TestSuite();

        suite.addTest(new TestSuite(ZRenderingPerformance.class));
        suite.addTest(new TestSuite(ZPickingPerformance.class));
        suite.addTest(new TestSuite(ZSceneGraphPerformance.class));

        return suite;
    }
}