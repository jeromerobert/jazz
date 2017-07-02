/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import java.awt.*;

public class ZTimingCanvas extends ZCanvas {

    public static double timeForLastPaint;

    public ZTimingCanvas() {
        super();
    }

    public void paintComponent(Graphics g) {
        long startTime = System.currentTimeMillis();
        super.paintComponent(g);
        ZTimingCanvas.timeForLastPaint = System.currentTimeMillis() - startTime;
    }
}