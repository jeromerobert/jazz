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