/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
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
        setIconImage(loadFrameIcon());
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
    public Map getHelpMap() {
        return hinote.getHelpMap();
    }
    private Image loadFrameIcon() {
        Toolkit toolkit= Toolkit.getDefaultToolkit();
        try {
            java.net.URL url= getClass().getResource("resources/jazzlogo.gif");
            return toolkit.createImage((ImageProducer) url.getContent());
        } catch (Exception ex) {
        }
        return null;
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
    static public void main(String s[]) {
        new HiNote();
    }
}