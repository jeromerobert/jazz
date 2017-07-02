/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.demo.hinote;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;

/**
 * <b>HiNote</b> is an application built on Jazz that supports authoring 
 * of a zoomable space.
 *
 * @author  Benjamin B. Bederson
 */
public class HiNote extends ZBasicFrame {
    protected ApplicationMenuBar  menubar;
    protected ApplicationCmdTable cmdTable;
    protected HiNoteCore          hinote;
    protected String              title = "HiNote";

    public HiNote() {
	setLookAndFeel(METAL_LAF);
	cmdTable = new ApplicationCmdTable(this);
	hinote = new HiNoteCore(getContentPane(), getComponent());
	setTitle(title);

				// Create menu bar
	menubar = new ApplicationMenuBar(hinote.getCmdTable(), cmdTable);
	setJMenuBar(menubar);

				// Deactivate ZBasicFrame event handlers since HiNote makes its own
	getPanEventHandler().deactivate();
	getZoomEventHandler().deactivate();

				// Load the images to be used by the toolbar
	Action action;
	action = hinote.getCmdTable().lookupAction("pan");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/hand.gif"));
	action = hinote.getCmdTable().lookupAction("link");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/link.gif"));
	action = hinote.getCmdTable().lookupAction("select");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/select.gif"));
	action = hinote.getCmdTable().lookupAction("polyline");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/drawing.gif"));
	action = hinote.getCmdTable().lookupAction("rectangle");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/rect.gif"));
	action = hinote.getCmdTable().lookupAction("text");
	action.putValue(Action.SMALL_ICON, new ImageIcon("icons/letter.gif"));

				// Create some custom cursors
	Image crosshairImage = component.getToolkit().getImage("cursors/crosshair.gif");
	hinote.setCrossHairCursorImage(crosshairImage);
    }

    static public void main(String s[]) {
	new HiNote();
    }
}

/**
 * <b>ApplicationMenuBar</b> builds the menubar for the HiNote application.
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 */
class ApplicationMenuBar extends JMenuBar {
    public ApplicationMenuBar(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu file = createFileMenu(cmdTable, applicationCmdTable);
	JMenu edit = createEditMenu(cmdTable, applicationCmdTable);
	JMenu insert = createInsertMenu( cmdTable, applicationCmdTable);
	JMenu view = createViewMenu(cmdTable, applicationCmdTable);
	JMenu help = createHelpMenu(cmdTable, applicationCmdTable);

	this.add(file);
	this.add(edit);
	this.add(view);
	this.add(insert);
	this.add(help); 
    }
    
    protected JMenu createFileMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu file = new JMenu("File");
	file.setMnemonic('F');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Open");
	menuItem.setMnemonic('O');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("open"));
	file.add(menuItem);

	menuItem = new JMenuItem("New");
	menuItem.setMnemonic('N');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("new"));
	menuItem.setEnabled(false);
	file.add(menuItem);

	menuItem = new JMenuItem("Save");
	menuItem.setMnemonic('S');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("save"));
	file.add(menuItem);
	
	menuItem = new JMenuItem("Save As...");
	menuItem.setMnemonic('A');
	menuItem.addActionListener(cmdTable.lookupActionListener("save as"));
	file.add(menuItem);

	menuItem = new JMenuItem("Print...");
	menuItem.setMnemonic('P');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("print"));
	file.add(menuItem);
	
	file.addSeparator();

	JMenu laf =  createLAFMenu(cmdTable, applicationCmdTable);
	file.add(laf);

	file.addSeparator();

	menuItem = new JMenuItem("Exit");
	menuItem.setMnemonic('X');
	menuItem.addActionListener(cmdTable.lookupActionListener("exit"));
	file.add(menuItem);
	
	return file;
    }
    protected JMenu createEditMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu edit = new JMenu("Edit");
	edit.setMnemonic('E');
	JMenuItem menuItem;
	
	menuItem = new JMenuItem("Cut");
	menuItem.setMnemonic('T');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("cut"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Copy");
	menuItem.setMnemonic('C');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("copy"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Paste");
	menuItem.setMnemonic('P');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("paste"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Delete");
	menuItem.setMnemonic('D');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
	menuItem.addActionListener(cmdTable.lookupActionListener("delete"));
	edit.add(menuItem);

	edit.addSeparator();

	menuItem = new JMenuItem("Bring to Front");
	menuItem.setMnemonic('F');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("raise"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Send to Back");
	menuItem.setMnemonic('B');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("lower"));
	edit.add(menuItem);

	edit.addSeparator();

	menuItem = new JMenuItem("Clear Fade");
	menuItem.addActionListener(cmdTable.lookupActionListener("clearminmaxmag"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Fade on Zoom in");
	menuItem.setMnemonic('O');
	menuItem.addActionListener(cmdTable.lookupActionListener("setmaxmag"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Fade on Zoom out");
	menuItem.setMnemonic('N');
	menuItem.addActionListener(cmdTable.lookupActionListener("setminmag"));
	edit.add(menuItem);

	edit.addSeparator();

	menuItem = new JMenuItem("Sticky");
	menuItem.setMnemonic('S');
	menuItem.addActionListener(cmdTable.lookupActionListener("sticky"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Sticky Z");
	menuItem.setMnemonic('Z');
	menuItem.addActionListener(cmdTable.lookupActionListener("stickyz"));
	edit.add(menuItem);

	menuItem = new JMenuItem("UnSticky");
	menuItem.setMnemonic('U');
	menuItem.addActionListener(cmdTable.lookupActionListener("unsticky"));
	edit.add(menuItem);

	edit.addSeparator();
        
	menuItem = new JMenuItem("Select All");
	menuItem.setMnemonic('A');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("select all"));
	edit.add(menuItem);
	
	edit.addSeparator();
	JCheckBoxMenuItem checkBox;
	
        checkBox = new JCheckBoxMenuItem("Anti-Aliasing");
	checkBox.setMnemonic('L');
        checkBox.addItemListener(cmdTable.lookupItemListener("anti-aliasing"));
	checkBox.setSelected(false);
        edit.add(checkBox);
        
	return edit;
    }
    
    protected JMenu createViewMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu view = new JMenu("View");
	view.setMnemonic('V');
	JMenuItem menuItem;
	JCheckBoxMenuItem checkBox;
	
	menuItem = new JMenuItem("New View");
	menuItem.setMnemonic('N');
	menuItem.addActionListener(cmdTable.lookupActionListener("newview"));
	view.add(menuItem);

	menuItem = new JMenuItem("Full Screen");
	menuItem.setMnemonic('U');
	menuItem.addActionListener(cmdTable.lookupActionListener("fullscreen"));
	view.add(menuItem);

	view.addSeparator();

	menuItem = new JMenuItem("Go Home");
	menuItem.setMnemonic('G');
	menuItem.addActionListener(cmdTable.lookupActionListener("gohome"));
	view.add(menuItem);

	view.addSeparator();

        checkBox = new JCheckBoxMenuItem("ToolBar");
	checkBox.setMnemonic('T');
        checkBox.addItemListener(cmdTable.lookupItemListener("toolbar"));
	checkBox.setSelected(true);
        view.add(checkBox);
        
	return view;
    }

    protected JMenu createInsertMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu insert = new JMenu("Insert");
	insert.setMnemonic('I');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Image ...");
	menuItem.setMnemonic('m');
	menuItem.addActionListener(cmdTable.lookupActionListener("insert image"));
	menuItem.setEnabled(true);
	insert.add(menuItem);

	return insert;
    }

    protected JMenu createLAFMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu laf = new JMenu("Look & Feel");
	laf.setMnemonic('L');
	ButtonGroup group = new ButtonGroup();
	JRadioButtonMenuItem radioButton;
	
	radioButton = new JRadioButtonMenuItem("Cross Platform");
	radioButton.setMnemonic('C');
	if (UIManager.getLookAndFeel().getID() == "Metal") {
	    radioButton.setSelected(true);
	} else {
	    radioButton.setSelected(false);
	}
	group.add(radioButton);
	radioButton.addActionListener(applicationCmdTable.lookupActionListener("cross platform"));
	laf.add(radioButton);

	radioButton = new JRadioButtonMenuItem("Motif");
	radioButton.setMnemonic('M');
	if (UIManager.getLookAndFeel().getID() == "Motif") {
	    radioButton.setSelected(true);
	} else {
	    radioButton.setSelected(false);
	}
	group.add(radioButton);
	radioButton.addActionListener(applicationCmdTable.lookupActionListener("motif"));
	laf.add(radioButton);

	radioButton = new JRadioButtonMenuItem("MS Windows");
	radioButton.setMnemonic('W');
	if (UIManager.getLookAndFeel().getID() == "Windows") {
	    radioButton.setSelected(true);
	} else {
	    radioButton.setSelected(false);
	}
	group.add(radioButton);
	radioButton.addActionListener(applicationCmdTable.lookupActionListener("ms windows"));
	laf.add(radioButton);

	return laf;
    }

    protected JMenu createDebugMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu debug = new JMenu("Debug");
	debug.setMnemonic('D');
	JMenuItem menuItem;
	JCheckBoxMenuItem checkBox;
	
	menuItem = new JMenuItem("Dump Scenegraph");
	menuItem.setMnemonic('S');
	menuItem.addActionListener(cmdTable.lookupActionListener("dump scenegraph"));
	menuItem.setEnabled(true);
	debug.add(menuItem);

	debug.addSeparator();

        checkBox = new JCheckBoxMenuItem("Debug Painting");
	checkBox.setMnemonic('P');
        checkBox.addItemListener(cmdTable.lookupItemListener("debug paint"));
	checkBox.setSelected(false);
        debug.add(checkBox);

        checkBox = new JCheckBoxMenuItem("Debug Damage");
	checkBox.setMnemonic('M');
        checkBox.addItemListener(cmdTable.lookupItemListener("debug damage"));
	checkBox.setSelected(false);
        debug.add(checkBox);

        checkBox = new JCheckBoxMenuItem("Debug Timing");
	checkBox.setMnemonic('T');
        checkBox.addItemListener(cmdTable.lookupItemListener("debug time"));
	checkBox.setSelected(false);
        debug.add(checkBox);

	debug.addSeparator();

        checkBox = new JCheckBoxMenuItem("Show Bounds");
	checkBox.setMnemonic('B');
        checkBox.addItemListener(cmdTable.lookupItemListener("show bounds"));
	checkBox.setSelected(false);
        debug.add(checkBox);

        checkBox = new JCheckBoxMenuItem("Show Region Mgmt");
	checkBox.setMnemonic('R');
        checkBox.addItemListener(cmdTable.lookupItemListener("show rgn mgmt"));
	checkBox.setSelected(false);
        debug.add(checkBox);

	debug.addSeparator();

        checkBox = new JCheckBoxMenuItem("Double Buffer");
	checkBox.setMnemonic('D');
        checkBox.addItemListener(cmdTable.lookupItemListener("double buffer"));
	checkBox.setSelected(true);
        debug.add(checkBox);

	return debug;
    }

    protected JMenu createHelpMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu help = new JMenu("Help");
	help.setMnemonic('H');
	JMenuItem menuItem;
	
	menuItem = new JMenuItem("Using HiNote");
	menuItem.setMnemonic('U');
	menuItem.addActionListener(cmdTable.lookupActionListener("using hinote"));
	help.add(menuItem);

	menuItem = new JMenuItem("About Jazz");
	menuItem.setMnemonic('A');
	menuItem.addActionListener(cmdTable.lookupActionListener("about jazz"));
	help.add(menuItem);

	help.addSeparator();

	JMenu debug = createDebugMenu(cmdTable, applicationCmdTable);
	help.add(debug);

	return help;
    }
}

class ApplicationCmdTable  {
    protected Hashtable actionMap;
    protected HiNote hinote;
    
    public ApplicationCmdTable(HiNote hn) {
	hinote = hn;
	actionMap = new Hashtable();

	actionMap.put("cross platform", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.setLookAndFeel(ZBasicFrame.METAL_LAF);
	    }
	});

	actionMap.put("motif", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.setLookAndFeel(ZBasicFrame.MOTIF_LAF);
	    }
	});

	actionMap.put("ms windows", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.setLookAndFeel(ZBasicFrame.WINDOWS_LAF);
	    }
	});
    }


    public Action lookupAction(String key) {
	return (Action)actionMap.get(key);
    }

    public ActionListener lookupActionListener(String key) {
	return (ActionListener)actionMap.get(key);
    }

    public ItemListener lookupItemListener(String key) {
	return (ItemListener)actionMap.get(key);
    }
}
