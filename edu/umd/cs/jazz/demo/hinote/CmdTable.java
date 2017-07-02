/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.demo.hinote;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import javax.swing.*;

import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.util.*;

public class CmdTable  {
    protected Hashtable actionMap;
    protected HiNoteCore hinote;
    
    public CmdTable(HiNoteCore hn) {
	hinote = hn;
	actionMap = new Hashtable();

	actionMap.put("using hinote", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.helpUsing();
	    }
	});

	actionMap.put("about jazz", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.helpAbout();
	    }
	});

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
		hinote.saveas();
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
		hinote.makeSticky();
	    }
	});

	actionMap.put("stickyz", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.makeStickyZ();
	    }
	});

	actionMap.put("unsticky", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.makeUnSticky();
	    }
	});

	actionMap.put("delete", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.deleteSelected();
	    }
	});

	actionMap.put("select all", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hinote.getDrawingLayer().selectAll(hinote.getCamera());
		hinote.getSurface().restore();
	    }
	});

	actionMap.put("toolbar", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		boolean show = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
		hinote.setToolBar(show);
	    }
	});

	actionMap.put("anti-aliasing", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		boolean high = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
		hinote.setRenderQuality(high);
	    }
	});

	actionMap.put("dump scenegraph", new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("----------------------------------------------------"); 
		ZDebug.dump(hinote.getRoot());
		System.out.println("----------------------------------------------------");
	    }
	});

	actionMap.put("debug paint", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.setDebug(ZDebug.DEBUG_PAINT);
		} else {
		    ZDebug.setDebug(ZDebug.DEBUG_NONE);
		}
	    }
	});

	actionMap.put("debug damage", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.setDebug(ZDebug.DEBUG_DAMAGE);
		} else {
		    ZDebug.setDebug(ZDebug.DEBUG_NONE);
		}
	    }
	});

	actionMap.put("debug time", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.setDebug(ZDebug.DEBUG_TIME);
		} else {
		    ZDebug.setDebug(ZDebug.DEBUG_NONE);
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
		ZSurface surface = hinote.getSurface();
		if (surface != null) {
		    surface.repaint();
		}
	    }
	});

	actionMap.put("show rgn mgmt", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    ZDebug.setDebugRegionMgmt(true);
		} else {
		    ZDebug.setDebugRegionMgmt(false);
		}
	    }
	});

	actionMap.put("double buffer", new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		ZBasicComponent component = hinote.getComponent();
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    RepaintManager.currentManager(component).setDoubleBufferingEnabled(true);
		} else {
		    RepaintManager.currentManager(component).setDoubleBufferingEnabled(false);
		}
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
