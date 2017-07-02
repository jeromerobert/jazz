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
 * Unit test for ZNullList
 * @author: Jesse Grosjean
 */
public class ZNullListTest extends TestCase {

    public ZNullListTest(String name) {
        super(name);
    }
    public void testSerialize() {
        ZNullList list = new ZNullList();

        File f = new File("temp");
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(list);
            out.close();

            in = new ObjectInputStream(new FileInputStream(f));
            ZNullList listIn = (ZNullList) in.readObject();
            in.close();

            listIn.getElementData();
            assert(listIn instanceof ZNullList);

        } catch (IOException e) {
            e.printStackTrace();
            assert(false);
        } catch (ClassNotFoundException e) {
            assert(false);
        }

        f.delete();
    }
}