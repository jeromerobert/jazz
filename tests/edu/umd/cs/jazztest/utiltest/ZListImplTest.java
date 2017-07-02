/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.utiltest;

import java.util.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.*;
import java.io.*;
import junit.framework.*;
import edu.umd.cs.jazz.component.ZText;

/**
 * Unit test for ZListImpl
 * @author: Jesse Grosjean
 */
public class ZListImplTest extends TestCase {
    int numElements = 0;
    String[] rawArray = null;

    public ZListImplTest(String name) {
        super(name);
    }

    public void setUp() {
    }

    private void addRaw(String s) {
        try {
            rawArray[numElements] = s;
        } catch (ArrayIndexOutOfBoundsException e) {
            String[] newArray = new String[(numElements == 0) ? 1 : (2 * numElements)];
            System.arraycopy(rawArray, 0, newArray, 0, numElements);
            rawArray = newArray;
            rawArray[numElements] = s;
        }
        numElements++;
    }

    private void removeRaw(String s) {
        for (int i = 0; i < numElements; i++) {
            if (rawArray[i] == s) {
                for (int j=i; j < (numElements); j++) {
                    rawArray[j] = rawArray[j+1];
                }
                rawArray[numElements] = null;
                numElements--;
                return;
            }
        }
    }

    public void testAddObject() {
        ZList a = new ZListImpl.ZObjectListImpl(5);
        String h = "Hello";
        a.add(h);
        assert(a.contains(h));
        assert(a.size() == 1);
    }

    public void testPop() {
        ZList a = new ZListImpl.ZObjectListImpl(1);
        String h = "Hello";
        a.add(h);
        assert(a.contains(h));
        a.pop();
    }

    public void testMoveElementToIndex() {
        ZList list = new ZListImpl.ZObjectListImpl(0);

        String a1 = "a1";
        String a2 = "a2";
        String a3 = "a3";
        String a4 = "a4";

        list.add(a1);
        list.add(a2);
        list.add(a3);
        list.add(a4);

        assertEquals(list.get(0), a1);
        assertEquals(list.get(3), a4);

        list.moveElementToIndex(a2, 2);

        assertEquals(list.get(0), a1);
        assertEquals(list.get(1), a3);
        assertEquals(list.get(2), a2);
        assertEquals(list.get(3), a4);
    }

    public void testObjectCreation() {
        ZList a = new ZListImpl.ZNodeListImpl(5);

        // size initialized?
        assert(a.getElementData().length == 5);
        assert(a.size() == 0);

        // Add with no exceptions?
        a.add(new ZNode());

        boolean exception = false;
        try {
            // this should throw an exception.
            a.add(new Integer(4));
        } catch (Exception e) {
            exception = true;
        }
        assert(exception);
    }

    public void testRemoveObject() {
        ZList a = new ZListImpl.ZObjectListImpl(5);

        String h = "Hello";
        a.add(h);

        a.remove(h);
        assert(!a.contains(h));
        assert(a.size() == 0);
    }

    public void testReplaceWith() {
        ZList list = new ZListImpl.ZObjectListImpl(5);

        Object a1 = new Object();
        Object a2 = new Object();
        Object a3 = new Object();
        Object a4 = new Object();

        list.add(a1);
        list.add(a2);
        list.add(a3);

        assert(list.replaceWith(a2, a4));
        assertEquals(list.get(1), a4);
    }

    public void testTimes() {
    /*  ZArray objectArray = new ZArray(String.class);
        String[] raw = new String[0];
        ZArrayList list = new ZArrayList(String.class);
        rawArray = new String[0];
        int num = 0;

        String[] strings = new String[100000];
        for (int i = 0; i < 100000; i++) {
            strings[i] = ""+i;
        }
        long time = 0;


        // Test add.

        time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            list.add(strings[i]);
        }
        System.out.println("add ArrayList way:" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            addRaw(strings[i]);
        }
        System.out.println("add raw way: " + (System.currentTimeMillis() - time));


        // Test remove.

        time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            list.remove(strings[i]);
        }
        System.out.println("remove ArrayList way: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            removeRaw(strings[i]);
        }
        System.out.println("remove Old way: " + (System.currentTimeMillis() - time));

        // Test iterate.

        time = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            String s = (String) itr.next();
            s = null;
        }
        }
        System.out.println("Iterate with ArrayList.iterator: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            s = null;
        }
        }
        System.out.println("Iterate with ArrayList.get: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        String[] data = (String[]) list.getArrayReference();
        for (int j = 0; j < 10; j++) {
        for (int i = 0; i < list.size(); i++) {
            String s = data[i];
            s = null;
        }
        }
        System.out.println("Iterate over ArrayList internal way: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
        for (int i = 0; i < rawArray.length; i++) {
            String s = rawArray[i];
            s = null;
        }
        }
        System.out.println("Iterate raw way: " + (System.currentTimeMillis() - time));
    */
    }

    public void testTrimToSize() {
        ZList a = new ZListImpl.ZObjectListImpl(5);

        for (int i = 0; i < 5; i++) {
            a.add(new Integer(i));
        }
        assert(a.size() == 5);
        a.trimToSize();
        assert(a.size() == a.getElementData().length);
    }
}