/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.eventtest;

import java.awt.event.*;
import edu.umd.cs.jazz.util.*;

/**
 * This class is to help you write Event tests. It provides methods to programaticly
 * simulate events on a ZCanvas. See DragTest for example.
 *
 * @author: Jesse
 */
public class ZCanvasSimulator extends ZCanvas {
    public ZCanvasSimulator() {
        super();
    }
    protected void simulateEvent(int id, int x, int y) {
        simulateEvent(id, System.currentTimeMillis(), 0, x, y, 0, false);
    }
    protected void simulateEvent(int id, int x, int y, int modifiers) {
        simulateEvent(id, System.currentTimeMillis(), modifiers, x, y, 0, false);
    }
    protected void simulateEvent(int id, long when, int modifiers, int x, int y, int clickCount, boolean popuptrigger) {
        processEvent(new MouseEvent(this, id, when, modifiers, x, y, clickCount, popuptrigger));
    }
}
