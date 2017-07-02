/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * <b>LineIcon</b> creates an icon used by the HiNite "select line width"
 * button. The icons contain a line of varying width, and are used in
 * the popup menu items of the button.
 *
 * @author  Jim Mokwa
 */
public class LineIcon implements Icon, Serializable {
    private int width = 50;
    private int height = 20;
    private int lineSize = 1;

    public LineIcon(int aLineSize) {
	lineSize = aLineSize;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
	for (int i=0; i < lineSize; i++) {
	    y = (height / 2) + i; 
	    g.drawLine(x, y, x + width - 10, y);
	}
    }

    public int getIconWidth() {
	return width;
    }

    public int getIconHeight() {
	return height;
    }
}
