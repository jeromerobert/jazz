/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * Create a button that pops up a menu for selecting pen width.
 *
 * @author  James J. Mokwa
 */

public class PenSelector extends JButton implements Serializable {

    protected JPopupMenu popupMenu;
    protected double penWidth = -1;
    protected ToolTipManager toolTipManager;

    public PenSelector(JPopupMenu aPopupMenu, ImageIcon icon) {
	super(icon);
	popupMenu = aPopupMenu;
	toolTipManager = ToolTipManager.sharedInstance();
    }

    public double getPenWidth() {
	return penWidth;
    }

    /**
     * When button is pressed, pop up a pen selection menu; when button
     * released, set penWidth to menu selection that was armed. If "More
     * Pen Widths" was selected, pop up a dialog, user can type in pt size.
     */
    public void processMouseEvent(MouseEvent e) {

				// Overroad mouse events, must do toolTips manually.
	if (e.getID() == e.MOUSE_ENTERED) {
	    toolTipManager.mouseEntered(e);
	}

	else if (e.getID() == e.MOUSE_EXITED) {
	    toolTipManager.mouseExited(e);
	}

	else if (e.getID() == e.MOUSE_PRESSED) {
	    popupMenu.show(e.getComponent(), 0, getHeight());
	}

				// A menu could not simply be added to the toolbar
				// to give this behavior. Items would have to have
				// been selected with a mouse click, or a JMenubar
				// used, which doesn't take an icon
	else if (e.getID() == e.MOUSE_RELEASED) {
	    MenuElement[] menuElement = popupMenu.getSubElements();
	    for (int k = 0; k < menuElement.length; k++) {
		JMenuItem menuItem = (JMenuItem)menuElement[k];
		if (menuItem.isArmed()) {
		    String label = menuItem.getText();
		    popupMenu.setVisible(false);
		    String spt = null;
		    if (label.equals("More Pen Widths")) {
			spt = JOptionPane.showInputDialog("Enter Pen Size");
		    } else {
			spt = label.substring(0, label.length()-2);
		    }
				// Convert label from string "#pt" to int #
		    if (spt != null) {
			try {
			    double pt = Integer.parseInt(spt);
			    if (pt > 0) {
				penWidth = pt;
				firePropertyChange("penComponentSelection", -1, penWidth);
			    }
			} catch (NumberFormatException ex) {
			    System.out.println("invalid PenWidth: " + spt);
			}
		    }
		} 
	    }
	}
    }
}


