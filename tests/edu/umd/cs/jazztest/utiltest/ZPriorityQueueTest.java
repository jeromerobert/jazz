/**
 * Copyright 2001-@year@ by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.utiltest;

import java.util.*;
import edu.umd.cs.jazz.util.*;
import junit.framework.*;

/**
 * Unit test for ZPriorityQueue
 * @author: Jesse Grosjean
 */
public class ZPriorityQueueTest extends TestCase {

    public ZPriorityQueueTest(String name) {
        super(name);
    }

    public void testInsert() {
        ZPriorityQueue queue = new ZPriorityQueue();

        assertTrue(queue.size() == 0);
        queue.insert(new Double(1));
        assertTrue(queue.size() == 1);
    }

    public void testFirst() {
        ZPriorityQueue queue = new ZPriorityQueue();

        queue.insert(new Double(7));
        queue.insert(new Double(34));
        queue.insert(new Double(12));
        queue.insert(new Double(4));

        assertTrue(queue.size() == 4);
        assertTrue(((Double)queue.first()).doubleValue() == 34);
        assertTrue(queue.size() == 4);
        assertTrue(((Double)queue.first()).doubleValue() == 34);
    }

    public void testExtractFirst() {
        ZPriorityQueue queue = new ZPriorityQueue();

        queue.insert(new Double(7));
        queue.insert(new Double(34));
        queue.insert(new Double(12));
        queue.insert(new Double(4));

        assertTrue(queue.size() == 4);
        assertTrue(((Double)queue.extractFirst()).doubleValue() == 34);
        assertTrue(queue.size() == 3);
        assertTrue(((Double)queue.extractFirst()).doubleValue() == 12);
        assertTrue(queue.size() == 2);
        assertTrue(((Double)queue.extractFirst()).doubleValue() == 7);
        assertTrue(queue.size() == 1);
        assertTrue(((Double)queue.extractFirst()).doubleValue() == 4);
        assertTrue(queue.size() == 0);

    }
}