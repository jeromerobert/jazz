/**
 * Copyright 2001 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import java.io.*;
import java.util.*;

public class ZPerformanceLog {

    private static ZPerformanceLog thePerformanceLog;
    private static Vector theHeaderRow;
    private static Vector thePerformanceRow;
    private static Hashtable theTestTable;

    public ZPerformanceLog() {
        super();
    }

    public static ZPerformanceLog instance() {
        if (thePerformanceLog == null) {
            thePerformanceLog = new ZPerformanceLog();
            theTestTable = new Hashtable();
        }
        return thePerformanceLog;
    }

    public void logTest(String testName, double testDuration) {
        String columnLable = new java.util.Date().toString();
        String rowLable = testName;
        Vector columnData = (Vector) theTestTable.get(testName);

        if (columnData == null) {
            columnData = new Vector();
            theTestTable.put(testName, columnData);
        }

        columnData.add(new Double(testDuration));

        theHeaderRow.add(testName);
        thePerformanceRow.add(""+testDuration);
    }

    public static void initLog() {
        theHeaderRow = new Vector();
        thePerformanceRow = new Vector();

        theHeaderRow.add("Date");
        thePerformanceRow.add(new Date());
    }

    public static void writeLog() {

        System.out.println();
        System.out.println("Test data for input into spreadsheet:");
        System.out.println();

        for (int i = 0; i < theHeaderRow.size(); i++) {
            System.out.println(thePerformanceRow.get(i));
        }

        System.out.println();
        System.out.println("Labled test results, see above for simple column \n of times for input into spreadsheet:");
        System.out.println();

        for (int i = 0; i < theHeaderRow.size(); i++) {
            System.out.println(theHeaderRow.get(i) + ", " + thePerformanceRow.get(i));
        }

    }
}