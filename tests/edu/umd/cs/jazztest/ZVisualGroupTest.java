/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.*;
import junit.framework.*;

/**
 * Unit test for ZVisualGroup.
 * @author: Jesse Grosjean
 */
public class ZVisualGroupTest extends TestCase {
    public ZVisualGroupTest(String name) {
        super(name);
    }
    public void testClone() {
        ZRectangle rect1 = new ZRectangle(0,0,10,10);
        ZRectangle rect2 = new ZRectangle(0,0,10,10);

        ZVisualGroup g = new ZVisualGroup(rect1, rect2);
        ZVisualGroup gclone = (ZVisualGroup) g.clone();

        assert(g.getFrontVisualComponent() == rect1);
        assert(g.getBackVisualComponent() == rect2);

        // Make sure that when an ZVisualGroup gets cloned its visual components are also cloned.
        assert(gclone.getFrontVisualComponent() != rect1);
        assert(gclone.getBackVisualComponent() != rect2);

    }
}