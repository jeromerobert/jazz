/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazzperformancetests;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import java.awt.geom.*;

public class ZNullVisualComponent extends ZVisualComponent {

    public ZNullVisualComponent() {
        super();
        setBounds(new ZBounds(0, 0, 100, 100));
    }

    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
        return false;
    }

    public void render(ZRenderContext renderContext) {
    }
}