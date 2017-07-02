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
import java.net.URL;
import java.util.jar.Attributes;
import javax.swing.*;

import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

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
public class HiNote extends JFrame implements Serializable {
    protected int                 width = 500;
    protected int                 height = 500;
    protected ApplicationMenuBar  menubar;
    protected ApplicationCmdTable cmdTable;
    protected HiNoteCore          hinote;
    protected String              title = "HiNote";
    protected ZCanvas             canvas; 

    public HiNote() {
				// Support exiting application
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

				// Set up basic frame
	setBounds(100, 100, width, height);
	setResizable(true);
	setBackground(null);
	canvas = new ZCanvas();
	getContentPane().add(canvas);

				// Set up core of Hinote
	cmdTable = new ApplicationCmdTable(this);
	hinote = new HiNoteCore(getContentPane(), canvas);
	hinote.setLookAndFeel(HiNoteCore.METAL_LAF, this);
	setTitle(title);
	menubar = new ApplicationMenuBar(hinote.getCmdTable(), cmdTable);
	setJMenuBar(menubar);
				// Deactivate ZCanvas event handlers since HiNote makes its own
	canvas.setNavEventHandlersActive(false);

                                // Load toolbar images and cursors
	hinote.loadToolbarImageCursors();

	setVisible(true);
    }

    public void loadModule() {
	ExtensionFileFilter filter = new ExtensionFileFilter();
	filter.addExtension("class");
	filter.setDescription("Class files");
	
	File file = hinote.QueryUserForFile(filter, "Load Module");
	if (file != null) {
	    String fileName = file.getAbsolutePath();
	    String parent = file.getParent();
	    FileClassLoader classLoader = new FileClassLoader(parent);
	    try {
		Class cl = classLoader.loadClass(file.getName(), true);
		Object instance = cl.newInstance();
		if (instance instanceof ZLoadable) {
		    ZLoadable loadable = (ZLoadable)instance;
		    loadable.setMenubar(menubar);
		    loadable.setCamera(canvas.getCamera());
		    loadable.setDrawingSurface(canvas.getDrawingSurface());
		    loadable.setLayer(canvas.getLayer());
		}
		if (instance instanceof Runnable) {
		    Runnable runnable = (Runnable)instance;
		    runnable.run();
		}
	    } catch (InstantiationException e) {
		System.out.println("Instantiation exception: " + e);
	    } catch (IllegalAccessException e) {
		System.out.println("Illegal access exception: " + e);
	    } catch (ClassNotFoundException e) {
		System.out.println("Can't find class: " + fileName);
		System.out.println(e);
	    } catch (SecurityException e) {
		System.out.println("Security exception while loading class: " + e);
	    }
	}
    }

    public Map getHelpMap() {
	return hinote.getHelpMap();
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
	JMenu insert = createInsertMenu(cmdTable, applicationCmdTable);
	JMenu view = createViewMenu(cmdTable, applicationCmdTable);
	JMenu help = createHelpMenu(cmdTable, applicationCmdTable);

	this.add(file);
	this.add(edit);
	this.add(view);
	this.add(insert);
	this.add(help); 
    }
    
    /**
     * Create a JLabel with the HCIL logo 
     */
    protected JLabel createLogoLabel() {
	URL logoURL = this.getClass().getClassLoader().getResource("resources/HCIL-logo.gif");
	ImageIcon logo = new ImageIcon(logoURL);
	JLabel label = new JLabel(logo);
	return label;
    }

    protected JMenu createFileMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu file = new JMenu("File");
	file.setMnemonic('F');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Open...");
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

	menuItem = new JMenuItem("Load Module...");
	menuItem.setMnemonic('M');
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
	menuItem.addActionListener(applicationCmdTable.lookupActionListener("loadmodule"));
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
	
	menuItem = new JMenuItem("Font Chooser");
	menuItem.addActionListener(cmdTable.lookupActionListener("font"));
	menuItem.addPropertyChangeListener(cmdTable.lookupPropertyListener("fontComponent"));
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
	edit.add(radioButton);

	radioButton = new JRadioButtonMenuItem("High Quality");
	qualityGroup.add(radioButton);
	radioButton.setMnemonic('Q');
	radioButton.addItemListener(cmdTable.lookupItemListener("high-quality"));
	radioButton.setSelected(true);
	edit.add(radioButton);
	
	return edit;
    }
    
    protected JMenu createInsertMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu insert = new JMenu("Insert");
	insert.setMnemonic('I');
	JMenuItem menuItem;

	menuItem = new JMenuItem("Image ...");
	menuItem.setMnemonic('m');
	menuItem.addActionListener(cmdTable.lookupActionListener("insert image"));
	insert.add(menuItem);

	menuItem = new JMenuItem("File ...");
	menuItem.setMnemonic('f');
	menuItem.addActionListener(cmdTable.lookupActionListener("insert file"));
	insert.add(menuItem);

	return insert;
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
	debug.add(menuItem);

	menuItem = new JMenuItem("Browse Scene graph");
	menuItem.setMnemonic('O');
	menuItem.addActionListener(cmdTable.lookupActionListener("showTreeView"));
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

	debug.addSeparator();

	menuItem = new JMenuItem("Index a Group");
	menuItem.setMnemonic('I');
	menuItem.addActionListener(cmdTable.lookupActionListener("rtree index"));
	debug.add(menuItem);

	menuItem = new JMenuItem("UnIndex a Group");
	menuItem.setMnemonic('J');
	menuItem.addActionListener(cmdTable.lookupActionListener("rtree unindex"));
	debug.add(menuItem);

	menuItem = new JMenuItem("Dump RTree");
	menuItem.addActionListener(cmdTable.lookupActionListener("dump rtree"));
	debug.add(menuItem);

	return debug;
    }

    protected JMenu createHelpMenu(CmdTable cmdTable, ApplicationCmdTable applicationCmdTable) {
	JMenu help = new JMenu("Help");
	help.setMnemonic('H');
	JMenuItem menuItem;
	
	Map helpMap = applicationCmdTable.getHelpMap();
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
	    JMenu debug = createDebugMenu(cmdTable, applicationCmdTable);
	    help.add(debug);
	}

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

	actionMap.put("loadmodule", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.loadModule();
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
