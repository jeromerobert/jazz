/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.jar.Attributes;
import javax.swing.*;

import edu.umd.cs.jazz.*;
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
public class HiNoteApplet extends JApplet implements Serializable {
    static final public String packageFileName = "edu" + File.separatorChar + "umd" + File.separatorChar + "cs" +
	File.separatorChar + "jazz" + File.separatorChar + "demo" + File.separatorChar + "hinote" + File.separatorChar;

    protected AppletMenuBar  menubar;
    protected AppletCmdTable cmdTable;
    protected HiNoteCore     hinote;
    protected ZCanvas        canvas;

    public HiNoteApplet() {
    }

    public void init() {
	setBackground(null);
	setVisible(true);

				// Create a basic Jazz scene, and attach it to this window
	canvas = new ZCanvas();
	getContentPane().add(canvas);

	/*
	  From JApplet docs:
	  Both Netscape Communicator and Internet Explorer 4.0 unconditionally 
	  print an error message to the Java console when an applet attempts to
	  access the AWT system event queue. Swing applets do this once, to check
	  if access is permitted. To prevent the warning message in a production
	  applet one can set a client property called "defeatSystemEventQueueCheck" 
	  on the JApplets RootPane to any non null value.
	*/
	
	JRootPane rp = getRootPane();
	rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);


	cmdTable = new AppletCmdTable(this);
	hinote = new HiNoteCore(getContentPane(), canvas);
	HiNoteCore.setLookAndFeel(HiNoteCore.METAL_LAF, this);

				// Create menu bar
	menubar = new AppletMenuBar(hinote.getCmdTable(), cmdTable);
	setJMenuBar(menubar);

	JPanel logoPanel = createLogoPanel();
	getContentPane().add(logoPanel, BorderLayout.SOUTH);

				// Deactivate ZCanvas event handlers since HiNote makes its own
	canvas.setNavEventHandlersActive(false);

                                // Load toolbar images and cursors
	hinote.loadToolbarImageCursors();

				// Load initial jazz file
	String startupJazzFile = getParameter("startupfile");
	if (startupJazzFile != null) {
	    String startupJarFile = getParameter("startupjarfile");
	    if (startupJarFile != null) {
		hinote.loadJazzFile(startupJazzFile, startupJarFile);
	    } else {
		hinote.loadJazzFile(startupJazzFile, "help.jar");
	    }
	}
    }

    public Map getHelpMap() {
	return hinote.getHelpMap();
    }

    /**
     * Create a JPanel with the HCIL logo 
     */
    protected JPanel createLogoPanel() {
	URL logoURL = this.getClass().getClassLoader().getResource("resources/HCIL-logo.gif");

	ImageIcon logoImage = new ImageIcon(logoURL);
	JLabel logoLabel = new JLabel(logoImage);

	JPanel logoPanel = new JPanel();
	logoPanel.setLayout(new BorderLayout());
	logoPanel.add(logoLabel, BorderLayout.EAST);
	logoPanel.setBackground(hinote.fillColor);
	
	return logoPanel;
    }
}

/**
 * <b>AppletMenuBar</b> builds the menubar for the HiNote applet.
 * @author  Benjamin B. Bederson
 * @author  Britt McAlister
 */
class AppletMenuBar extends JMenuBar {
    protected HiNoteApplet hinote;

    public AppletMenuBar(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
        JMenu file = createFileMenu(cmdTable, appletCmdTable);
        JMenu edit = createEditMenu(cmdTable, appletCmdTable);
        JMenu view = createViewMenu(cmdTable, appletCmdTable);
        JMenu help = createHelpMenu(cmdTable, appletCmdTable);

        this.add(file);
        this.add(edit);
        this.add(view);
        this.add(help);
    }

    protected JMenu createFileMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
	JMenu file = new JMenu("File");
	file.setMnemonic('F');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Print...");
	menuItem.setMnemonic('P');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("print"));
	file.add(menuItem);
	
	file.addSeparator();

	JMenu laf =  createLAFMenu(cmdTable, appletCmdTable);
	file.add(laf);

	return file;
    }

    protected JMenu createEditMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
	JMenu edit = new JMenu("Edit");
	edit.setMnemonic('E');
	JMenuItem menuItem;
	
	menuItem = new JMenuItem("Font Chooser");
	menuItem.addActionListener(cmdTable.lookupActionListener("font"));
	edit.add(menuItem);

	edit.addSeparator();

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

	menuItem = new JMenuItem("Group");
	menuItem.setMnemonic('G');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("group"));
	edit.add(menuItem);

	menuItem = new JMenuItem("Ungroup");
	menuItem.setMnemonic('U');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("ungroup"));
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
	menuItem.setMnemonic('Y');
	menuItem.addActionListener(cmdTable.lookupActionListener("unsticky"));
	edit.add(menuItem);

	edit.addSeparator();
        
	menuItem = new JMenuItem("Select All");
	menuItem.setMnemonic('A');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(cmdTable.lookupActionListener("select all"));
	edit.add(menuItem);
	
	edit.addSeparator();

	JRadioButtonMenuItem radioButton;
        ButtonGroup qualityGroup = new ButtonGroup();
	radioButton = new JRadioButtonMenuItem("Low Quality");
	qualityGroup.add(radioButton);
	radioButton.setMnemonic('L');
	radioButton.addItemListener(cmdTable.lookupItemListener("low-quality"));
	edit.add(radioButton);
	
	radioButton = new JRadioButtonMenuItem("Medium Quality");
	qualityGroup.add(radioButton);
	radioButton.setMnemonic('M');
	radioButton.addItemListener(cmdTable.lookupItemListener("med-quality"));
	radioButton.setSelected(true);
	edit.add(radioButton);

	radioButton = new JRadioButtonMenuItem("High Quality");
	qualityGroup.add(radioButton);
	radioButton.setMnemonic('Q');
	radioButton.addItemListener(cmdTable.lookupItemListener("high-quality"));
	edit.add(radioButton);
	return edit;
    }
    
    protected JMenu createViewMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
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

    protected JMenu createLAFMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
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
	radioButton.addActionListener(appletCmdTable.lookupActionListener("cross platform"));
	laf.add(radioButton);

	radioButton = new JRadioButtonMenuItem("Motif");
	radioButton.setMnemonic('M');
	if (UIManager.getLookAndFeel().getID() == "Motif") {
	    radioButton.setSelected(true);
	} else {
	    radioButton.setSelected(false);
	}
	group.add(radioButton);
	radioButton.addActionListener(appletCmdTable.lookupActionListener("motif"));
	laf.add(radioButton);

	radioButton = new JRadioButtonMenuItem("MS Windows");
	radioButton.setMnemonic('W');
	if (UIManager.getLookAndFeel().getID() == "Windows") {
	    radioButton.setSelected(true);
	} else {
	    radioButton.setSelected(false);
	}
	group.add(radioButton);
	radioButton.addActionListener(appletCmdTable.lookupActionListener("ms windows"));
	laf.add(radioButton);

	return laf;
    }

    protected JMenu createDebugMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
	JMenu debug = new JMenu("Debug");
	debug.setMnemonic('D');
	JMenuItem menuItem;
	JCheckBoxMenuItem checkBox;
	
	menuItem = new JMenuItem("Dump Scenegraph");
	menuItem.setMnemonic('S');
	menuItem.addActionListener(cmdTable.lookupActionListener("dump scenegraph"));
	debug.add(menuItem);

	debug.addSeparator();

        checkBox = new JCheckBoxMenuItem("Debug Rendering");
	checkBox.setMnemonic('P');
        checkBox.addItemListener(cmdTable.lookupItemListener("debug render"));
	checkBox.setSelected(false);
        debug.add(checkBox);

        checkBox = new JCheckBoxMenuItem("Debug Repainting");
	checkBox.setMnemonic('M');
        checkBox.addItemListener(cmdTable.lookupItemListener("debug repaint"));
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

    /**
     * Help menu files are loaded from help.jar.
     */
    protected JMenu createHelpMenu(CmdTable cmdTable, AppletCmdTable appletCmdTable) {
	JMenu help = new JMenu("Help");
	help.setMnemonic('H');
	JMenuItem menuItem;

	Map helpMap = appletCmdTable.getHelpMap();
	if (helpMap != null) {
	    Set set = helpMap.entrySet();
	    Iterator i = set.iterator();
	    while (i.hasNext()) {
		Map.Entry me = (Map.Entry)i.next();
		Attributes attr = (Attributes)me.getValue();
		
		String aMenuItem = attr.getValue("MenuItem");
		menuItem = new JMenuItem(aMenuItem);
		
		String mnemonic = attr.getValue("Mnemonic");
		menuItem.setMnemonic(mnemonic.charAt(0));
		
		String actionListener = attr.getValue("ActionListener");
		menuItem.addActionListener(cmdTable.lookupActionListener(actionListener));
		
		help.add(menuItem);
	    }
	}

	if (ZDebug.debug) {
	    help.addSeparator();
	    JMenu debug = createDebugMenu(cmdTable, appletCmdTable);
	    help.add(debug);
	}

	return help;
    }
}

class AppletCmdTable  {
    protected Hashtable    actionMap;
    protected HiNoteApplet hinote;
    
    public AppletCmdTable(HiNoteApplet hn) {
	hinote = hn;
	actionMap = new Hashtable();

	actionMap.put("cross platform", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		HiNoteCore.setLookAndFeel(HiNoteCore.METAL_LAF, hinote);
	    }
	});

	actionMap.put("motif", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		HiNoteCore.setLookAndFeel(HiNoteCore.MOTIF_LAF, hinote);
	    }
	});

	actionMap.put("ms windows", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		HiNoteCore.setLookAndFeel(HiNoteCore.WINDOWS_LAF, hinote);
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

    public Map getHelpMap() {
	return hinote.getHelpMap();
    }
}
