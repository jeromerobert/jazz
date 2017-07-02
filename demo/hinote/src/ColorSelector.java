/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;

/**
 * New JComponent ColorSelector, which allows user to bring up
 * color chooser dialogs to set pen and fill colors.
 */
public class ColorSelector extends JComponent implements MouseListener, Serializable {
    public int PENCOLORSELECTED = 0;
    public int FILLCOLORSELECTED = 1;
    public int PENCOLORCHANGE = 2;
    public int FILLCOLORCHANGE = 3;
    public int FLIPSELECTED = 4;

    protected int selectedIndex = -1;

    protected Color fillColor;
    protected Color penColor;

    protected Rectangle penBoxRect = new Rectangle(2, 2, 18, 18);
    protected Rectangle fillBoxRect = new Rectangle(11, 11, 18, 18);
    protected Rectangle penNullBoxRect = new Rectangle(34, 6, 10, 10);
    protected Rectangle fillNullBoxRect = new Rectangle(34, 18, 10, 10);

    protected int boxSize = 20;
    protected int penBoxX = 2;
    protected int penBoxY = 2;
    protected int fillBoxX = penBoxX + (boxSize / 2);
    protected int fillBoxY = penBoxY + (boxSize / 2);
    protected int arcX = penBoxX + boxSize -2;
    protected int arcY = penBoxY +1;
    protected int arcSize = boxSize / 2;

    public ColorSelector() {
        this(Color.black, Color.white);
    }

    public ColorSelector(Color penColor, Color fillColor) {
        setPenColor(penColor);
        setFillColor(fillColor);
        addMouseListener(this);
        setBorder(new EtchedBorder());
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

    public void paintColorSquare(Graphics g, Color c, Rectangle r) {
        int x = (int) r.getX();
        int y = (int) r.getY();
        int width = (int) r.getWidth();
        int height = (int) r.getHeight();

        if (c == null) {
            g.setColor(Color.white);
            g.fillRect(x, y, width, height);

            g.setColor(Color.red);
            g.drawLine(x, y, x+width, y+height);
        } else {
            g.setColor(c);
            g.fillRect(x, y, width, height);
        }

        g.setColor(Color.black);
        g.drawRect(x, y, width, height);
    }

    public void paintComponent(Graphics g) {
                // draw background (Fill Color) box
        paintColorSquare(g, fillColor, fillBoxRect);

                // draw foreground (Pen Color) box
        paintColorSquare(g, penColor, penBoxRect);
        paintColorSquare(g, null, fillNullBoxRect);
        paintColorSquare(g, null, penNullBoxRect);

                // draw arc (Swap Pen and Fill Colors)
        g.setColor(Color.black);
        g.drawArc(arcX, arcY, arcSize, arcSize, 90, -90);

        g.drawLine(arcX + 5, arcY, arcX + 5, arcY + 2);
        g.drawLine(arcX + 5, arcY, arcX + 7, arcY - 2);

        g.drawLine(arcX + 10, arcY + 5, arcX + 8, arcY + 5);
        g.drawLine(arcX + 10, arcY + 5, arcX + 11, arcY + 3);
    }

    /**
     * Determine which part of the component was clicked in: "Pen Color", "Fill Color",
     * or "Swap Colors". Set those values for this component, then fire a property
     * change event, notifying hinote to update display.
     */
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int property = -1;
            boolean doubleClick = e.getClickCount() > 1;

            if (penBoxRect.contains(x, y)) {
                if (doubleClick) {
                    property = PENCOLORCHANGE;
                } else {
                    property = PENCOLORSELECTED;
                }
            } else if (fillBoxRect.contains(x, y)) {
                if (doubleClick) {
                    property = FILLCOLORCHANGE;
                } else {
                    property = FILLCOLORSELECTED;
                }
            } else if (penNullBoxRect.contains(x, y)) {
                property = PENCOLORSELECTED;
                setPenColor(null);
            } else if (fillNullBoxRect.contains(x, y)) {
                property = FILLCOLORSELECTED;
                setFillColor(null);
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
        if (property != -1) {
            setSelectedIndex(property);
            fillColor = getFillColor();
            penColor = getPenColor();
            repaint();
        }
    }
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}