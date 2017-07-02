/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.util.jar.Attributes;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.component.*;

public class CmdTable implements Serializable  {
    protected Hashtable actionMap;
    protected HiNoteCore hinote;
    protected String entryName;

    public CmdTable(HiNoteCore hn) {
	hinote = hn;
	actionMap = new Hashtable();

				// add action listeners from help.jar manifest entries
	Map helpMap = hn.getHelpMap();
	if (helpMap != null) {
	    Set set = helpMap.entrySet();
	    Iterator i = set.iterator();
	    while (i.hasNext()) {
		Map.Entry me = (Map.Entry)i.next();
		entryName = me.getKey().toString();
		Attributes attr = (Attributes)me.getValue();
		String actionListener = attr.getValue("ActionListener");

		actionMap.put(actionListener, new ActionListener() {
		    final public String entry = entryName;
		    public void actionPerformed(ActionEvent e) {
			hinote.loadJazzFile(entry, "help.jar");
		    }
		});
	    }
	}

	actionMap.put("open", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.open();
	    }
	});

	actionMap.put("new", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    }
	});

	actionMap.put("newview", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.newView();
	    }
	});

	actionMap.put("fullscreen", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.fullScreen();
	    }
	});

	actionMap.put("gohome", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.goHome();
	    }
	});

	actionMap.put("save", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.save();
	    }
	});

	actionMap.put("save as", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.saveAs();
	    }
	});

	actionMap.put("print", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.printScreen();
	    }
	});

	actionMap.put("exit", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	});

	actionMap.put("insert image", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.insertImage();
	    }
	});

	actionMap.put("pan", new AbstractAction("Pan") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.PAN_MODE);
	    }
	});

	actionMap.put("link", new AbstractAction("Link") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.LINK_MODE);
	    }
	});

	actionMap.put("select", new AbstractAction("Select") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.SELECTION_MODE);
	    }
	});

	actionMap.put("polyline", new AbstractAction("Polyline") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.POLYLINE_MODE);
	    }
	});

	actionMap.put("polygon", new AbstractAction("Polygon") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.POLYGON_MODE);
	    }
	});

	actionMap.put("rectangle", new AbstractAction("Rectangle") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.RECTANGLE_MODE);
	    }
	});

	actionMap.put("text", new AbstractAction("Text") {
	    public void actionPerformed(ActionEvent e) {
		hinote.setEventHandler(HiNoteCore.TEXT_MODE);
	    }
	});

	actionMap.put("font", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.fontChooser();
	    }
	});

	actionMap.put("cut", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.cut();
	    }
	});

	actionMap.put("copy", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.copy();
	    }
	});

	actionMap.put("paste", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.paste();
	    }
	});

	actionMap.put("raise", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.raise();
	    }
	});

	actionMap.put("lower", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.lower();
	    }
	});

	actionMap.put("setminmag", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.setMinMag();
	    }
	});

	actionMap.put("setmaxmag", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.setMaxMag();
	    }
	});

	actionMap.put("clearminmaxmag", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.clearMinMaxMag();
	    }
	});

	actionMap.put("sticky", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.makeSticky(ZStickyGroup.STICKY);
	    }
	});

	actionMap.put("stickyz", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.makeSticky(ZStickyGroup.STICKYZ);
	    }
	});

	actionMap.put("unsticky", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.makeUnSticky();
	    }
	});

	actionMap.put("delete", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.delete();
	    }
	});

	actionMap.put("group", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.group();
	    }
	});

	actionMap.put("ungroup", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.ungroup();
	    }
	});

	actionMap.put("select all", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.selectAll();
	    }
	});

	actionMap.put("toolbar", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		boolean show = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
		hinote.setToolBar(show);
	    }
	});

	actionMap.put("low-quality", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		hinote.setRenderQuality(ZDrawingSurface.RENDER_QUALITY_LOW);
	    }
	});

	actionMap.put("med-quality", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		hinote.setRenderQuality(ZDrawingSurface.RENDER_QUALITY_MEDIUM);
	    }
	});

	actionMap.put("high-quality", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		hinote.setRenderQuality(ZDrawingSurface.RENDER_QUALITY_HIGH);
	    }
	});

	actionMap.put("dump scenegraph", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("----------------------------------------------------"); 
		ZDebug.dump(hinote.getRoot());
		System.out.println("----------------------------------------------------");
	    }
	});

	actionMap.put("debug render", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.debugRender = true;
		} else {
		    ZDebug.debugRender = false;
		}
	    }
	});

	actionMap.put("debug repaint", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.debugRepaint = true;
		} else {
		    ZDebug.debugRepaint = false;
		}
	    }
	});

	actionMap.put("debug time", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.debugTiming = true;
		} else {
		    ZDebug.debugTiming = false;
		}
	    }
	});

	actionMap.put("show bounds", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.setShowBounds(true, hinote.getCamera());
		}
		else {
		    ZDebug.setShowBounds(false, null);
		}
	    }
	});

	actionMap.put("show rgn mgmt", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.debugRegionMgmt = true;
		} else {
		    ZDebug.debugRegionMgmt = false;
		}
	    }
	});

	actionMap.put("double buffer", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		ZCanvas component = hinote.getCanvas();
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    RepaintManager.currentManager(component).setDoubleBufferingEnabled(true);
		} else {
		    RepaintManager.currentManager(component).setDoubleBufferingEnabled(false);
		}
	    }
	});

	actionMap.put("fontComponent", new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName() == "fontComponentSelection") {
		    hinote.chooseFonts();
		}
	    }
	});

	actionMap.put("colorComponent", new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName() == "colorComponentSelection") {
		    hinote.chooseColors();
		}
	    }
	});

	actionMap.put("penComponent", new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName() == "penComponentSelection") {
		    hinote.updatePenWidth();
		}
	    }
	});

	actionMap.put("penColorChange", new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		hinote.updatePenColor();
	    }
	});
	    
	actionMap.put("fillColorChange", new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		hinote.updateFillColor();
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

    public PropertyChangeListener lookupPropertyListener(String key) {
	return (PropertyChangeListener)actionMap.get(key);
    }

    public ChangeListener lookupChangeListener(String key) {
	return (ChangeListener)actionMap.get(key);
    }
}
