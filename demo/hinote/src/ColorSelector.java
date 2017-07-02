/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

/**
 * New JComponent ColorSelector, which allows user to bring up
 * color chooser dialogs to set pen and fill colors.
 */
public class ColorSelector extends JComponent implements MouseListener, Serializable {
    public int PENCOLORSELECTED = 0;
    public int FILLCOLORSELECTED = 1;
    public int FLIPSELECTED = 2;

    protected int selectedIndex = -1;

    protected Color fillColor;
    protected Color penColor;

    protected int boxSize = 20;
    protected int penBoxX = 2;
    protected int penBoxY = 2;
    protected int fillBoxX = penBoxX + (boxSize / 2);
    protected int fillBoxY = penBoxY + (boxSize / 2);
    protected int arcX = penBoxX + boxSize -2;
    protected int arcY = penBoxY +1;
    protected int arcSize = boxSize / 2;
    protected int boundingBoxSize = boxSize + (boxSize / 2) + 2;


    public ColorSelector() {
	setPenColor(Color.black);
	setFillColor(Color.white);
	addMouseListener(this);
    }

    public ColorSelector(Color penColor, Color fillColor) {
	setPenColor(penColor);
	setFillColor(fillColor);
	addMouseListener(this);
    }

    public void setFillColor(Color color) {
	fillColor = color;
	repaint();
    }

    public Color getFillColor() {
	return fillColor;
    }

    public void setPenColor(Color color) {
	penColor = color;
	repaint();
    }

    public Color getPenColor() {
	return penColor;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        firePropertyChange("colorComponentSelection", -1, index);
    }

    public void paintComponent(Graphics g) {

				// draw border
	g.setColor(Color.black);
	g.drawRect(0, 0, boundingBoxSize, boundingBoxSize);
				// draw background (Fill Color) box
	g.setColor(fillColor);
	g.fillRect(fillBoxX, fillBoxY, boxSize, boxSize);

				// draw foreground (Pen Color) box
	g.setColor(penColor);
	g.fillRect(penBoxX, penBoxY, boxSize, boxSize);

				// draw arc (Swap Pen and Fill Colors)
	g.setColor(Color.black);
	g.drawArc(arcX, arcY, arcSize, arcSize, 90, -90);
    }

    /**
     * Determine which part of the component was clicked in: "Pen Color", "Fill Color",
     * or "Swap Colors". Set those values for this component, then fire a property
     * change event, notifying hinote to update display.
     */
    public void mouseClicked(MouseEvent e) {
	int x = e.getX();
	int y = e.getY();
	int property = -1;
	if (((x >= penBoxX) && (x <= penBoxX + boxSize)) && 
	    ((y >=  penBoxY) && (y <= penBoxY + boxSize))) {
	    property = PENCOLORSELECTED;
	} else {
	    if (((x >= fillBoxX) && (x <= fillBoxX + boxSize)) && 
		((y >=  fillBoxY) && (y <= fillBoxY + boxSize))) {
		property = FILLCOLORSELECTED;
	    } else {
		if (((x >= arcX) && (x <= arcX + arcSize)) && 
		    ((y >=  arcY) && (y <= arcY + arcSize))) {
		    property = FLIPSELECTED;
		    Color tmp = fillColor;
		    fillColor = penColor;
		    penColor = tmp;
		    setPenColor(penColor);
		    setFillColor(fillColor);
		}
	    }		
	}
	if (property != -1) {
	    setSelectedIndex(property);
	    fillColor = getFillColor();
	    penColor = getPenColor();
	    repaint();
	}
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}


