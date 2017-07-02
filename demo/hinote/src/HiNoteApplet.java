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
        JMenu filesFromServerMenu = createAppletFileListMenu();
        if (filesFromServerMenu != null) {
            menubar.add(filesFromServerMenu);
        }
        setJMenuBar(menubar);

        JPanel logoPanel = createLogoPanel();
        getContentPane().add(logoPanel, BorderLayout.SOUTH);

                    // Deactivate ZCanvas event handlers since HiNote makes its own
        canvas.setNavEventHandlersActive(false);

                    // Load toolbar images and cursors
        hinote.loadToolbarImageCursors();

                    // Set tool bar visible depending upon isToolBarShowing parameter.
        String isToolBarShowing = getParameter("isToolBarShowing");
        if (isToolBarShowing != null && isToolBarShowing.equalsIgnoreCase("false")) {
            hinote.setToolBar(false);
        }

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
    /**
     * Create menu of .jazzb (.jazz files will not work) files that live on the server
     * and can be opened by this applet.This menu is configured by supplying paramaters
     * in the OBJECT tag used to embed the applet. The following parameters must all
     * or none, be included.
     *
     *      PARAM NAME="serverJazzFilesCount" VALUE = "2"
     *      PARAM NAME="serverJazzFilesMenuTitle" VALUE = "Test Files"
     *      PARAM NAME="serverJazzFile0" VALUE = "myfile.jazzb"
     *      PARAM NAME="serverJazzFile1" VALUE = "myotherfile.jazzb"
     *
     * These example tags say that there are two files that we want the applet to open.
     * The the title of the menu to open these files should be "Test Files".
     * That the first file is myfile.jazzb.
     * That the second file is myotherfile.jazzb.
     *
     * These files should be located in the same directory that the applets .jar file is located.
     */
    protected JMenu createAppletFileListMenu() {
        JMenu result = null;
        String serverJazzFilesCount = getParameter("serverJazzFilesCount");
        if (serverJazzFilesCount != null) {
            result = new JMenu(getParameter("serverJazzFilesMenuTitle"));

            int userFileCount = Integer.parseInt(serverJazzFilesCount);
            for (int i = 0; i < userFileCount; i++) {
                final String fileName = getParameter("serverJazzFile" + i);
                result.add(new AbstractAction(fileName) {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URL u = new URL(getCodeBase(), fileName);
                            InputStream  in = u.openStream();
                            hinote.openStream(in, fileName);
                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            }
        }
        return result;
    }
}