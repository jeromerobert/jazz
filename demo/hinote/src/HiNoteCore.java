/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.awt.datatransfer.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.extras.svg.ZSVG;

/**
 * <b>HiNoteCore</b> implements the core of HiNote that
 * is shared by the application and the applet.
 *
 * @author  Benjamin B. Bederson
 */
public class HiNoteCore implements ClipboardOwner {
    static protected String windowsClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    static protected String metalClassName   = "javax.swing.plaf.metal.MetalLookAndFeel";
    static protected String motifClassName   =  "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

                // Look & Feel types
    final static public int WINDOWS_LAF = 1;
    final static public int METAL_LAF   = 2;
    final static public int MOTIF_LAF   = 3;

    static final protected double MAX_ITEM_MAG = 10;
    static final protected int   ANIMATION_TIME = 1000;

                // Event modes
    static final public int PAN_MODE       = 1;
    static final public int LINK_MODE      = 2;
    static final public int POLYGON_MODE  = 3;
    static final public int RECTANGLE_MODE = 4;
    static final public int TEXT_MODE      = 5;
    static final public int SELECTION_MODE = 6;
    static final public int POLYLINE_MODE  = 7;
    static final public int ELLIPSE_MODE  = 8;
    static final public int COLORPICKER_MODE  = 9;

    protected ZCanvas                 canvas;
    protected ZRoot           root;
    protected ZCamera                 camera;
    protected ZDrawingSurface         surface;
    protected ZLayerGroup             layer;

    protected CmdTable                cmdTable;
    protected JToolBar                toolBar;
    protected ZPanEventHandler        panEventHandler;
    protected ZoomEventHandler        zoomEventHandler;
    protected ZLinkEventHandler       linkEventHandler;
    protected ZEventHandler           keyboardNavEventHandler;
    protected ZEventHandler           squiggleEventHandler;
    protected EllipseEventHandler     ellipseEventHandler;
    protected ZEventHandler           polygonEventHandler;
    protected TextEventHandler        textEventHandler;
    protected ZEventHandler           rectEventHandler;
    protected ZEventHandler           selectionEventHandler;
    protected ZEventHandler           activeEventHandler=null;
    protected ZEventHandler           colorPickerEventHandler;
    protected ZSceneGraphTreeView     treeView;
    protected int                     currentEventHandlerMode = PAN_MODE;
    protected Cursor                  crosshairCursor = null;
    protected Cursor                  colorpickerCursor = null;

    protected String                  currentFileName = null;
    protected ZParser                 parser = null;
    protected ArrayList               copyBuffer = null;     // A Vector of nodes
    protected File                    prevFile = new File(".");
    protected ColorSelector           colorComponent;
    protected Color                   penColor = Color.black;
    protected Color                   fillColor = Color.white;
    protected double                  penWidth = 4;
    protected Stroke                  penStroke;
    protected FontChooser             fontChooser;
    protected Font                    font = new Font("Arial", Font.PLAIN, 40);
    protected AffineTransform         pasteDelta = null;
    protected AffineTransform         pasteViewTransform = null;
    protected JColorChooser           penColorChooser;
    protected JDialog                 penColorDialog = null;
    protected JDialog                 fontDialog = null;
    protected JColorChooser           fillColorChooser;
    protected JDialog                 fillColorDialog = null;
    protected PenSelector             penWidthButton;
    protected Clipboard               clipboard = null;
    protected AbstractButton          panButton = null;
    protected String                  jazzFilterDescription = "Jazz Custom Format files";
    protected String                  jazzExtension = "jazz";
    protected String                  jazzbFilterDescription = "Jazz Java-Serialized files";
    protected String                  jazzbExtension = "jazzb";
    protected String                  SVGFilterDescription = "SVG files";
    protected String                  SVGExtension = "svg";
    protected JCheckBox               embed;
    protected JComboBox               fileChooserSelectionCB;
    protected JFileChooser            fileChooser;
    protected javax.swing.filechooser.FileFilter lastFilter = null;


    public HiNoteCore(Container container, ZCanvas canvas) {
        cmdTable = new CmdTable(this);
        copyBuffer = new ArrayList();
        pasteDelta = new AffineTransform();
        pasteViewTransform = new AffineTransform();

                    // Create the tool palette
        toolBar = createToolBar();
        container.add(toolBar, BorderLayout.NORTH);

                    // Extract the basic elements of the scenegraph
        this.canvas = canvas;
        surface = canvas.getDrawingSurface();
        camera = canvas.getCamera();
        root = canvas.getRoot();
        layer = canvas.getLayer();

                    // Add a selection layer
        ZLayerGroup selectionLayer = new ZLayerGroup();
        getRoot().addChild(selectionLayer);
        getCamera().addLayer(selectionLayer);

                    // Create some basic event handlers
        initEventHandlers(canvas.getCameraNode(), canvas, selectionLayer);
        zoomEventHandler.setActive(true);
        activeEventHandler = null;
        setEventHandler(PAN_MODE);

                    // get ownership of clipboard, so we will be notified
                    // if we lose it. Note Applets can't access system
                    // clipboard.
        try {
            clipboard = canvas.getToolkit().getSystemClipboard();
        } catch (java.security.AccessControlException e) {}
        if (clipboard != null) {
            getOwnershipClipboard();
        }
    }

    ///////////////////////////////////////////////////////////////
    //
    // Some utility methods
    //
    ///////////////////////////////////////////////////////////////

    /*
     * Initialize all the event handlers for hinote.
     */
    public void initEventHandlers(ZNode cameraNode, ZCanvas canvas, ZLayerGroup selectionLayer) {
        textEventHandler =        new TextEventHandler(this, cameraNode, canvas);
        squiggleEventHandler =    new SquiggleEventHandler(this, cameraNode);
        polygonEventHandler =     new PolygonEventHandler(this, cameraNode);
        ellipseEventHandler =     new EllipseEventHandler(this, cameraNode);
        rectEventHandler =        new RectEventHandler(this, cameraNode);
        linkEventHandler =        new ZLinkEventHandler(cameraNode, canvas);
        selectionEventHandler =   new ZCompositeSelectionHandler(cameraNode, canvas, selectionLayer);
        panEventHandler =         new PanEventHandler(cameraNode, canvas);
        zoomEventHandler =        new ZoomEventHandler(cameraNode);
        colorPickerEventHandler = new ColorPickerEventHandler(this, cameraNode);

        if (keyboardNavEventHandler != null) {
            keyboardNavEventHandler.setActive(false);
        }
        keyboardNavEventHandler = new ZNavEventHandlerKeyBoard(cameraNode, canvas);
    }

    /*
     * Get the ownership of the system clipboard, so we will be notified if
     * any other process writes into it.
     */
    public void getOwnershipClipboard() {
        if (clipboard != null) {
            String tempString = "";
            Transferable clipboardContent = clipboard.getContents (this);
            if ((clipboardContent != null) &&
            (clipboardContent.isDataFlavorSupported (DataFlavor.stringFlavor))) {
                try {
                    tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
                }
                catch (Exception e) {
                    e.printStackTrace ();
                }
            }
            StringSelection fieldContent = new StringSelection (tempString);
            clipboard.setContents (fieldContent, HiNoteCore.this);
        }
    }

    /**
     * This method is called when another process writes data to the system clipboard.
     * Clear the local copyBuffer, so when a 'paste' is done the clipboard contents are
     * used.
     */
    public void lostOwnership (Clipboard parClipboard, Transferable parTransferable) {
        copyBuffer.clear();
    }

    /**
     * Set the Swing look and feel.
     * @param laf The look and feel, can be WINDOWs_LAF, METAL_LAF, or MOTIF_LAF
     */
    static public void setLookAndFeel(int laf, Component component) {
        try {
            switch (laf) {
            case WINDOWS_LAF:
                UIManager.setLookAndFeel(windowsClassName);
                break;
            case METAL_LAF:
                UIManager.setLookAndFeel(metalClassName);
                break;
            case MOTIF_LAF:
                UIManager.setLookAndFeel(motifClassName);
                break;
            default:
                UIManager.setLookAndFeel(metalClassName);
            }
            SwingUtilities.updateComponentTreeUI(component);
        }
        catch (Exception exc) {
            System.err.println("Error loading L&F: " + exc);
        }
    }

    /**
     * Set the pen width of all the selected nodes to current penWidth.
     */
    public void updatePenWidth() {
        penWidth = penWidthButton.getPenWidth();
        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            setPenWidth(node);
        }
    }

    /**
     * Set the pen width of this node if it is a leaf node,
     * or all leaf nodes under the node.
     *<code>@param node</code> a node.
     */
    public void setPenWidth(ZNode node) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                setPenWidth(children[i]);
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZStroke) {
                ((ZStroke)vc).setPenWidth(penWidth);
            }
        }
    }

    /**
     * Update the Pen Color of all selected nodes with pen ColorChooser color.
     */
    public void updatePenColor() {
        updatePenColor(penColorChooser.getColor());
    }

    /**
     * Update the Pen Color of all selected nodes.
     *<code>@param color</code> a color.
     */
    public void updatePenColor(Color color) {
        penColor = color;
        colorComponent.setPenColor(penColor);

        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            setPenColor(node);
        }
    }

    /**
     * Set the pen color of this node if it is a leaf node,
     * or all leaf nodes under the node.
     *<code>@param node</code> a node.
     */
    public void setPenColor(ZNode node) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                setPenColor(children[i]);
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZPenPaint) {
                ((ZPenPaint)vc).setPenPaint(penColor);
            }
        }
    }


    /**
     * Update the Fill Color of all selected nodes with fill ColorChooser color.
     */
    public void updateFillColor() {
        updateFillColor(fillColorChooser.getColor());
    }

    /**
     * Update the Fill Color of all selected nodes.
     *<code>@param color</code> a color.
     */
    public void updateFillColor(Color color) {
        fillColor = color;
        colorComponent.setFillColor(fillColor);

        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            setFillColor(node);
        }
    }

    /**
     * Set the fill color of this node if it is a leaf node,
     * or all leaf nodes under the node.
     *<code>@param node</code> a node.
     */
    public void setFillColor(ZNode node) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                setFillColor(children[i]);
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZFillPaint) {
                ((ZFillPaint)vc).setFillPaint(fillColor);
            }
        }
    }


    /**
     * Update the Font of all selected text nodes with fill FontChooser font.
     */
    public void updateFont() {
        updateFont(fontChooser.getFont());
    }

    /**
     * Update the font of all selected text nodes.
     *<code>@param aFont</code> a font.
     */
    public void updateFont(Font aFont) {
        font = aFont;

        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            setFont(node);
        }
    }

    /**
     * Set the font of this node if it is a leaf text node,
     * or all leaf nodes under the node.
     *<code>@param node</code> a node.
     */
    public void setFont(ZNode node) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                setFont(children[i]);
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZText) {
                Font currFont = ((ZText)vc).getFont();
                String currStr = ((ZText)vc).getText();
                ZTransformGroup tg = node.editor().getTransformGroup();
                int newSize = (int)(font.getSize() / (camera.getMagnification() * tg.getScale()));
                Font newFont = new Font(font.getName(), font.getStyle(), newSize);
                ((ZText)vc).setFont(newFont);
            }
        }
    }

    /**
     * Returns a Font if all selected text objects use the same font,
     * otherwise returns null.
     */
    public Font getSelectedFont() {
        Font baseFont = null;
        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            baseFont = getFont(node, baseFont, true);
        }
        return baseFont;
    }

    /**
     * Get the font of this node if it is a leaf text node,
     * or all leaf nodes under the node. Return null all fonts found are not
     * identical.
     *<code>@param node</code> a node.
     *<code>@param baseFont</code> first font found, compare all others to this.
     *<code>@param singleFont</code> true if all fonts found so far are identical.
     */
    public Font getFont(ZNode node, Font baseFont, boolean singleFont) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                getFont(children[i], baseFont, singleFont);
                if (!singleFont) {
                    return null;
                }
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZText) {
                Font font = ((ZText)vc).getFont();
                if (baseFont == null) {
                    baseFont = font;
                }
                if (! font.equals(baseFont)) {
                    singleFont = false;
                    return null;
                }
            }
        }

        if (!singleFont) {
            return null;
        } else {
            return baseFont;
        }
    }

    /**
     * Create a font chooser panel if one is not already visible.
     * If all selected text fields have the same font, initialize chooser
     * to that font, otherwise start with current hinote font.
     */
    public void fontChooser() {
        if ((fontChooser == null) || (! fontChooser.isShowing())) {
            Font selectedFont = getSelectedFont();
            JFrame fontChooserFrame = new JFrame();
            if (selectedFont != null) {
                font = selectedFont;
            }
            fontChooser = new FontChooser(fontChooserFrame, font);
            fontChooser.addPropertyChangeListener(cmdTable.lookupPropertyListener("fontComponent"));
            fontChooser.setVisible(true);
        }
    }

    /**
     * User has changed font info via Font chooser. Update selected text fields and
     * current hinote font.
     */
    public void chooseFonts() {
        font = fontChooser.getFont();
        updateFont(font);
    }

    /**
     * Respond to a ColorSelector selection: Create a swing color chooser dialog
     * to allow the user to set pen or fill colors of selected objects, or swap
     * those two colors. Only one fill or pen color chooser can be visible at any time.
     */
    public void chooseColors() {
        int index = colorComponent.getSelectedIndex();

        if (index == colorComponent.PENCOLORSELECTED) {
            updatePenColor(colorComponent.getPenColor());
        } else if (index == colorComponent.FILLCOLORSELECTED) {
            updateFillColor(colorComponent.getFillColor());
        } else if (index == colorComponent.FLIPSELECTED) {
            Color tmp = penColor;
            penColor = fillColor;
            fillColor = tmp;
            updatePenColor(penColor);
            updateFillColor(fillColor);
        } else if ((index == colorComponent.PENCOLORCHANGE) &&
                  ((penColorDialog == null) || (!penColorDialog.isShowing()))) {
            if (penColor == null) {
                colorComponent.setPenColor(Color.black);
                penColor = Color.black;
            }
            penColorChooser = new JColorChooser(penColor);
            penColorChooser.getSelectionModel().
            addChangeListener(cmdTable.lookupChangeListener("penColorChange"));

            penColorDialog = penColorChooser.createDialog(canvas.getParent(), "Select Pen Color", false, penColorChooser, null, null);
            penColorDialog.show();
                // User clicked on "Fill Color" segment of ColorSelector
        } else if ((index == colorComponent.FILLCOLORCHANGE) &&
                  ((fillColorDialog == null) || (! fillColorDialog.isShowing()))) {
            if (fillColor == null) {
                colorComponent.setFillColor(Color.white);
                fillColor = Color.white;
            }
            fillColorChooser = new JColorChooser(fillColor);
            fillColorChooser = new JColorChooser(fillColor);
            fillColorChooser.getSelectionModel().
            addChangeListener(cmdTable.lookupChangeListener("fillColorChange"));

            fillColorDialog = fillColorChooser.createDialog(canvas.getParent(), "Select Fill Color", false, fillColorChooser, null, null);
            fillColorDialog.show();
        }
    }

    /**
     * Create the main HiNote toolBar.
     */
    JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        JToggleButton button;
        URL resource;

        ButtonGroup group = new ButtonGroup();

                    // Pan Button
        resource = this.getClass().getClassLoader().getResource("resources/hand.gif");
        JToggleButton pan = new JToggleButton(new ImageIcon(resource), false);
        pan.setToolTipText("Pan and follow links");
        pan.setText(null);
        pan.setPreferredSize(new Dimension(34, 30));
        pan.setSelected(true);
        pan.addActionListener(cmdTable.lookupAction("pan"));
        group.add(pan);
        toolBar.add(pan);
        panButton = pan;

                    // Select button
        resource = this.getClass().getClassLoader().getResource("resources/select.gif");
        JToggleButton select = new JToggleButton(new ImageIcon(resource), false);
        select.setToolTipText("select");
        select.setText(null);
        select.setPreferredSize(new Dimension(34, 30));
        select.addActionListener(cmdTable.lookupAction("select"));
        group.add(select);
        toolBar.add(select);

                    // Link button
        resource = this.getClass().getClassLoader().getResource("resources/link.gif");
        JToggleButton link = new JToggleButton(new ImageIcon(resource), false);
        link.setToolTipText("link");
        link.setText(null);
        link.setPreferredSize(new Dimension(34, 30));
        link.addActionListener(cmdTable.lookupAction("link"));
        group.add(link);
        toolBar.add(link);

                    // Polyline button
        resource = this.getClass().getClassLoader().getResource("resources/drawing.gif");
        JToggleButton polyline = new JToggleButton(new ImageIcon(resource), false);
        polyline.setToolTipText("polyline");
        polyline.setText(null);
        polyline.setPreferredSize(new Dimension(34, 30));
        polyline.addActionListener(cmdTable.lookupAction("polyline"));
        group.add(polyline);
        toolBar.add(polyline);

                    // Polygon button
        resource = this.getClass().getClassLoader().getResource("resources/polygon.gif");
        JToggleButton polygon = new JToggleButton(new ImageIcon(resource), false);
        polygon.setToolTipText("polygon");
        polygon.setText(null);
        polygon.setPreferredSize(new Dimension(34, 30));
        polygon.addActionListener(cmdTable.lookupAction("polygon"));
        group.add(polygon);
        toolBar.add(polygon);

                    // Ellipse button
        resource = this.getClass().getClassLoader().getResource("resources/ellipse.gif");
        JToggleButton ellipse = new JToggleButton(new ImageIcon(resource), false);
        ellipse.setToolTipText("ellipse");
        ellipse.setText(null);
        ellipse.setPreferredSize(new Dimension(34, 30));
        ellipse.addActionListener(cmdTable.lookupAction("ellipse"));
        group.add(ellipse);
        toolBar.add(ellipse);

                    // Rectangle button
        resource = this.getClass().getClassLoader().getResource("resources/rect.gif");
        JToggleButton rectangle = new JToggleButton(new ImageIcon(resource), false);
        rectangle.setToolTipText("rectangle");
        rectangle.setText(null);
        rectangle.setPreferredSize(new Dimension(34, 30));
        rectangle.addActionListener(cmdTable.lookupAction("rectangle"));
        group.add(rectangle);
        toolBar.add(rectangle);

                    // Text button
        resource = this.getClass().getClassLoader().getResource("resources/letter.gif");
        JToggleButton text = new JToggleButton(new ImageIcon(resource), false);
        text.setToolTipText("text");
        text.setText(null);
        text.setPreferredSize(new Dimension(34, 30));
        text.addActionListener(cmdTable.lookupAction("text"));
        group.add(text);
        toolBar.add(text);

                    // Pen Width Menu button
        resource = this.getClass().getClassLoader().getResource("resources/penwidth.gif");
        penWidthButton = new PenSelector(createPenWidthMenu(), new ImageIcon(resource));
        penWidthButton.addPropertyChangeListener(cmdTable.lookupPropertyListener("penComponent"));
        penWidthButton.setText(null);
        penWidthButton.setPreferredSize(new Dimension(34, 30));
        penWidthButton.setToolTipText("Select line width");
        toolBar.add(penWidthButton);

                    // Color picker button
        resource = this.getClass().getClassLoader().getResource("resources/colorpicker.gif");
        JToggleButton colorPicker = new JToggleButton(new ImageIcon(resource), false);
        colorPicker.setToolTipText("color picker");
        colorPicker.setText(null);
        colorPicker.setPreferredSize(new Dimension(34, 30));
        colorPicker.addActionListener(cmdTable.lookupAction("colorPicker"));
        group.add(colorPicker);
        toolBar.add(colorPicker);

                    // Pen and Fill color chooser button
        colorComponent = new ColorSelector(penColor, fillColor);
        colorComponent.addPropertyChangeListener(cmdTable.lookupPropertyListener("colorComponent"));
        colorComponent.setPreferredSize(new Dimension(47, 32));
        colorComponent.setToolTipText("Color Chooser");
        toolBar.add(colorComponent);

        return toolBar;
    }

    /**
     * Create a popup menu for selecting line width.
     */
    public JPopupMenu createPenWidthMenu() {
        JPopupMenu popupMenu = new JPopupMenu("PenWidth");
        JMenuItem menuItem[] = new JMenuItem[6];
        int ptSize;
                    // generate menu items and their icons:
                    //   Text is i + "pt"
                    //   Icon is line of thicknesses i
        for (int i=0; i<6; i++) {
            ptSize = i+1;
            Integer pt = new Integer(ptSize);
            menuItem[i] = new JMenuItem(pt.toString() + "pt", new LineIcon(ptSize));
            popupMenu.add(menuItem[i]);
        }
        JMenuItem more = new JMenuItem("More Pen Widths...");
        popupMenu.add(more);

        return popupMenu;
    }

    /**
     * Load the images to be used by the toolbar
     * Images found via the java resources method
     */
    public void loadToolbarImageCursors() {
        URL resource;
        resource = this.getClass().getClassLoader().getResource("resources/crosshair.gif");
        Image crosshairImage = canvas.getToolkit().getImage(resource);
        setCrossHairCursorImage(crosshairImage);

        resource = this.getClass().getClassLoader().getResource("resources/colorpickercursor.gif");
        Image colorpickerImage = canvas.getToolkit().getImage(resource);
        setColorPickerCursorImage(colorpickerImage);
    }

    /**
     * Get a JarURLConnection to a jarFile via java resources.
     *<code>@param jarFileName</code> the jar file.
     */
    JarURLConnection getJarConnection(String jarFileName) {
        //String resourceFileName = "resources/" + jarFileName;
        String resourceFileName = jarFileName;

        URL resourceURL = this.getClass().getClassLoader().getResource(resourceFileName);

                    // turn URL into JarURL
            String jarResourceName;
            if (resourceURL != null) {
                jarResourceName = resourceURL.toString();
            } else {
                                    // "java -jar" always returns null resourceURL
                                    // why? I must specify path name
                    // ie look for help.jar in current dir:
                jarResourceName = "jar:file:help.jar!/";
            }

        if ((jarResourceName.length() > 3) &&
            (!jarResourceName.substring(0,4).equals("jar:"))) {
            jarResourceName = "jar:" + jarResourceName + "!/";
        }

        //System.out.println("jarResourceName: "+jarResourceName);
        URL jarResourceURL = null;
        try {
            jarResourceURL = new URL(jarResourceName);
        } catch (java.net.MalformedURLException e) {
            System.out.println("MalformedURLException: " + jarResourceName);
            return null;
        }
                    // get jar connection
        JarURLConnection jarConnection = null;
        try {
            jarConnection = (JarURLConnection)jarResourceURL.openConnection();
        } catch (IOException e) {
            return null;
        }
        return jarConnection;
    }


    /**
     * Get the help menu data from the manifest of help.jar.
     * @return a Map whose key is the help menu item, and whose value are
     * attributes of the item.
     */
    public Map getHelpMap() {
        JarURLConnection helpJarConnection = getJarConnection("help.jar");
        if (helpJarConnection == null) {
            return null;
        }
        Manifest manifest = null;
        try {
            manifest = helpJarConnection.getManifest();
        } catch (IOException e) {
            return null;
        }
        return manifest.getEntries();
    }

    /**
     * Load a .jazz help file into HiNote from a jar file.
     * <code>@param fileName</code> The name of a .jazz file.
     * <code>@param jarFile</code> The jar file.
     */
    public void loadJazzFile(String fileName, String jarFileName) {
        JarURLConnection jarConnection = getJarConnection(jarFileName);

                    // get inputStream on jar file entry, and it's
                    // uncompressed size (contentLength)
        int contentLength = -1;
        InputStream inStream = null;
        JarFile jarFile = null;
        try {
            jarFile = jarConnection.getJarFile();
            ZipEntry zipEntry = jarFile.getEntry(fileName);
            if (zipEntry == null) {
                System.out.println(fileName + " not found in jar file " + jarFileName);
                return;
            }
            contentLength = (int)zipEntry.getSize();
            inStream = jarFile.getInputStream(zipEntry);
        } catch (IOException e) {
            System.out.println("jarFile error: " + e);
        }
        if (contentLength < 0) {
            System.out.println("Invalid Size: " +  contentLength + " for " + fileName + " in jar file " + jarFileName);
            return;
        }

        // Create a label and progress bar
        JFrame pbFrame = new JFrame();
        pbFrame.setTitle("Progress Bar");
        pbFrame.setSize(310, 130);
        pbFrame.setLocation(250, 250);
        pbFrame.setBackground(Color.gray);
        pbFrame.setVisible(true);

        JPanel pbPanel = new JPanel();
        pbPanel.setPreferredSize(new Dimension(310, 330));
        Container pbContainer = pbFrame.getContentPane();
        pbContainer.add(pbPanel);

        JLabel pbLabel = new JLabel();
        pbLabel.setPreferredSize(new Dimension(280, 24));
        pbPanel.add(pbLabel);

        JProgressBar progress = new JProgressBar(0, 20);
        progress.setPreferredSize(new Dimension(300, 20));
        progress.setValue(0);
        progress.setBounds(20, 35, 260, 20);
        pbPanel.add(progress);

                    // update progressbar 20 times
        int pbValue = 0;
        int k = contentLength/20;

                    // There are problems parsing network InputStreams.
                    // Copy entire jar entry into ByteArrayInputStream
        byte buf[] = new byte[contentLength];
        try {
            for (int j=0; j<contentLength; j++) {
                buf[j] = (byte)inStream.read();
                if (j % k == 0) {
                    pbLabel.setText("Loading application " + fileName);
                    Rectangle pbLabelRect = pbLabel.getBounds();
                    pbLabelRect.x = 0;
                    pbLabelRect.y = 0;
                    pbLabel.paintImmediately(pbLabelRect);

                    progress.setValue(pbValue++);
                    Rectangle progressRect = progress.getBounds();
                    progressRect.x = 0;
                    progressRect.y = 0;
                    progress.paintImmediately(progressRect);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException reading jarFile: " +  jarFileName + " entry: " + fileName + " " + e);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        HiNoteBufferedInputStream bufferedInputStream = new HiNoteBufferedInputStream(bais, contentLength);
        pbLabel.setText("Parsing scenegraph...");
        openStream(bufferedInputStream, fileName);
        pbFrame.setVisible(false);
        getCanvas().requestFocus();
    }

    /**
     * Set the image to be used by the crosshair cursor.
     */
    public void setCrossHairCursorImage(Image image) {
        crosshairCursor = canvas.getToolkit().createCustomCursor(image, new Point(8, 8), "Crosshair Cursor");
    }

    /**
     * Set the image to be used by the colorpicker cursor.
     */
    public void setColorPickerCursorImage(Image image) {
        colorpickerCursor = canvas.getToolkit().createCustomCursor(image, new Point(4, 27), "Colorpicker Cursor");
    }

    public void setToolBar(boolean show) {
        toolBar.setVisible(show);
        toolBar.getParent().validate();
    }

    /**
     * Set the rendering quality.
     * Values are ZRenderContext.RENDER_QUALITY_LOW, ZRenderContext.RENDER_QUALITY_MEDIUM and
     * ZRenderContext.RENDER_QUALITY_HIGH
     */
    public void setRenderQuality(int qualityRequested) {
        surface.setRenderQuality(qualityRequested);
    }

    /**
     * Determine the current render quality of this app.
     */
    public int getRenderQuality() {
        return surface.getRenderQuality();
    }

    /**
     * Return the camera associated with the primary surface.
     * @return the camera
     */
    public ZCamera getCamera() {
        return camera;
    }

    /**
     * Return the surface.
     * @return the surface
     */
    public ZDrawingSurface getDrawingSurface() {
        return surface;
    }

    /**
     * Return the root of the scenegraph.
     * @return the root
     */
    public ZRoot getRoot() {
        return root;
    }

    /**
     * Return the "layer".  That is, the single node that
     * the camera looks onto to start.
     * @return the node
     */
    public ZLayerGroup getLayer() {
        return layer;
    }

    /**
     * Return the canvas that the surface is attached to.
     * @return the canvas
     */
    public ZCanvas getCanvas() {
        return canvas;
    }

    /**
     * Return the pan event handler.
     * @return the pan event handler.
     */
    public ZPanEventHandler getPanEventHandler() {
        return panEventHandler;
    }

    /**
     * Return the zoom event handler.
     * @return the zoom event handler.
     */
    public ZoomEventHandler getZoomEventHandler() {
        return zoomEventHandler;
    }

    public CmdTable getCmdTable() {
        return cmdTable;
    }

    public ZLayerGroup getDrawingLayer() {
        return getLayer();
    }

    ///////////////////////////////////////////////////////////////
    //
    // Basic functionality methods that are prinicipaly called
    // from the menubar.
    //
    ///////////////////////////////////////////////////////////////

    public void newView() {
        JFrame frame = new JFrame();
        ZCanvas canvas = new ZCanvas(getRoot(), getLayer());
        frame.setBounds(200, 100, 400, 400);
        frame.setResizable(true);
        frame.setBackground(null);
        frame.setVisible(true);
        frame.getContentPane().add(canvas);
        frame.validate();
        final ZCamera camera = canvas.getCamera();
                    // Copy visible layers from primary camera to new camera
        ZLayerGroup[] layers = getCamera().getLayers();
        for (int i=0; i<layers.length; i++) {
            camera.addLayer(layers[i]);
        }

                    // Make camera in new window look at same place as current window
        camera.center(this.camera.getViewBounds(), 0, canvas.getDrawingSurface());

                    // Use same render quality as current window
        canvas.getDrawingSurface().setRenderQuality(getRenderQuality());

                    // Use our own pan event handler so we can follow hyperlinks
        canvas.getPanEventHandler().setActive(false);
        new PanEventHandler(canvas.getCameraNode(), canvas).setActive(true);
        new ZNavEventHandlerKeyBoard(canvas.getCameraNode(), canvas).setActive(true);

                    // Don't want application to exit when secondary windows are closed
                    // Instead, just remove surface and camera
        WindowListener windowListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                camera.removeLayer(HiNoteCore.this.getLayer());
                ZNode[] parents = camera.getParents();
                for (int i=0; i<parents.length; i++) {
                   parents[i].getParent().removeChild(parents[i]);
                }
            }
        };
        frame.addWindowListener(windowListener);
    }

    /**
     * Create a fullscreen view of the scene.
     * Press the 'Esc' key to close it.
     */
    public void fullScreen() {

	// find parent for the new JWindow.
	Container p = canvas.getParent();
	while (!(p instanceof JFrame) && p != null) {
	    p = p.getParent();
	}

        final JWindow window = new JWindow((JFrame)p);
        ZCanvas canvas = new ZCanvas(getRoot(), getLayer());
        Dimension screenSize = window.getToolkit().getScreenSize();
        window.setLocation(0, 0);
        window.setSize(screenSize);
        window.setBackground(null);
        window.getContentPane().add(canvas);
        final ZCamera camera = canvas.getCamera();

        window.setVisible(true);
	window.requestFocus();
        canvas.requestFocus();

                    // Make camera in new window look at same place as current window
        camera.center(this.camera.getViewBounds(), 0, camera.getDrawingSurface());

                    // Use same render quality as current window
        camera.getDrawingSurface().setRenderQuality(getRenderQuality());

                    // Use our own pan event handler so we can follow hyperlinks
        canvas.getPanEventHandler().setActive(false);
        new PanEventHandler(canvas.getCameraNode(), canvas).setActive(true);

                    // Make escape key close window
        ZEventHandler keyEventHandler = new ZNavEventHandlerKeyBoard(canvas.getCameraNode(), canvas) {
            public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                camera.removeLayer(HiNoteCore.this.getLayer());
                ZNode[] parents = camera.getParents();
                for (int i=0; i<parents.length; i++) {
                    parents[i].getParent().removeChild(parents[i]);
                }
                window.dispose();
            }
            }
        };
        keyEventHandler.setActive(true);
    }

    /**
     * Change the camera to the identity view - i.e., home.
     */
    public void goHome() {
        getCamera().animate(new AffineTransform(), ANIMATION_TIME, getDrawingSurface());
    }

    public void openStream(InputStream inStream, String fileName) {
        openStream(inStream, fileName, null);
    }

    public void openStream(InputStream inStream, String fileName, File file) {
        String extension = null;
        int i = fileName.lastIndexOf('.');
        if(i > 0 && i < fileName.length()-1) {
            extension = fileName.substring(i+1).toLowerCase();
        }

        if (inStream != null) {
            if (extension.equals(jazzExtension)) {
                if (parser == null) {
                    parser = new ZParser();
                }
                try {
                    root = (ZRoot)parser.parse(inStream);
                    buildScene(root, canvas);
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Invalid file format");
                }
            } else if (extension.equals(jazzbExtension)) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(inStream);
                    root = (ZRoot)ois.readObject();
                    buildScene(root, canvas);
                    ois.close();
                } catch (InvalidClassException e) {
                    System.out.println("error processing jazzb ObjectInputStream: InvalidClassException: " + e.getMessage());
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    System.out.println("error processing jazzb ObjectInputStream: StreamCorruptedException: " + e.getMessage());
                    e.printStackTrace();
                } catch (OptionalDataException e) {
                    e.printStackTrace();
                    System.out.println("error processing jazzb ObjectInputStream: OptionalDataException: " + e.getMessage());
                } catch ( ClassNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("error processing jazzb ObjectInputStream:  ClassNotFoundException: " + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("error processing jazzb ObjectInputStream: IOException: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error processing jazzb ObjectInputStream: Exception: " + e.getMessage());
                }
            } else if(extension.equals(SVGExtension)) {
                // clear screen
                if(file == null) {
                    System.out.println("Cannot load SVG file !");
                    return;
                }

                canvas.getLayer().removeAllChildren();
                try {
                    ZSVG svg = new ZSVG();
                    // loading svg file into ZGroup
                    ZGroup group = svg.read(file);
                    // add the group into canvas
                    canvas.getLayer().addChild(group);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("error processing SVG file: Exception: " + e.getMessage());
                }

            }
        }
    }

    public void open() {
        // Pathnames are to be stored in jazz files relative to
        // current jazz save directory. Do a temporary 'set cwd'
        // so the readObject methods can change filenames to
        // be relative to this new cwd
        File file = getJazzFile();
        if (file != null) {
            String cwd = System.getProperty("user.dir");
            System.setProperty("user.dir",file.getParent());
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(currentFileName);
            }
            catch (java.io.FileNotFoundException e) {
                System.out.println("File " + currentFileName + " not found.");
                return;
            }

            openStream(inStream, currentFileName, file);
            // restore the cwd
            System.setProperty("user.dir", cwd);
        }
    }

    public File getJazzFile() {
        ExtensionFileFilter filter[] = new ExtensionFileFilter[4];
        filter[0] = new ExtensionFileFilter();
        filter[0].addExtension(jazzbExtension);
        filter[0].addExtension(jazzExtension);
        filter[0].addExtension(SVGExtension);
        filter[0].setDescription("All Jazz files");

        filter[1] = new ExtensionFileFilter();
        filter[1].addExtension(jazzExtension);
        filter[1].setDescription(jazzFilterDescription);

        filter[2] = new ExtensionFileFilter();
        filter[2].addExtension(jazzbExtension);
        filter[2].setDescription(jazzbFilterDescription);

        filter[3] = new ExtensionFileFilter();
        filter[3].addExtension(SVGExtension);
        filter[3].setDescription(SVGFilterDescription);

        File file = QueryUserForFile(filter, "Open");
        if (file != null) {
            currentFileName = file.getAbsolutePath();
            String extension = null;
            int i = currentFileName.lastIndexOf('.');
            if (i > 0 && i < currentFileName.length()-1) {
                extension = currentFileName.substring(i+1).toLowerCase();
            }

                    // if no filename extension given by user,
                    // add one if "Jazz Custom" or "Java-Serialized"
                    // was selected. If "All Jazz Files" was selected,
                    // don't add a selection.
            if (extension == null) {
                if (lastFilter != null) {
                    if (lastFilter.getDescription().indexOf("Custom") > -1) {
                        currentFileName = currentFileName + "." + jazzExtension;
                    }
                    if (lastFilter.getDescription().indexOf("Serialized") > -1) {
                        currentFileName = currentFileName + "." + jazzbExtension;
                    }
                    if (lastFilter.getDescription().indexOf("SVG") > -1) {
                        currentFileName = currentFileName + "." + SVGExtension;
                    }
                }
            }
        }
        return file;
    }

    public void buildScene(ZRoot root, ZCanvas canvas) {
        ZNode[] children = root.getChildren();
        layer = (ZLayerGroup)children[0];
        ZVisualLeaf cameraNode = (ZVisualLeaf)children[1];
        camera = (ZCamera)cameraNode.getFirstVisualComponent();
        ZLayerGroup selectionLayer = (ZLayerGroup)children[2];

        canvas.setNavEventHandlersActive(true);
        canvas.setLayer(layer);
        canvas.setCamera(camera, cameraNode);
        canvas.setRoot(root);
        canvas.setNavEventHandlersActive(false);

        root.addChild(selectionLayer);
        ((ZCompositeSelectionHandler)selectionEventHandler).setMarqueeLayer(selectionLayer);

        initEventHandlers(cameraNode, canvas, selectionLayer);
        zoomEventHandler.setActive(true);
        activeEventHandler = null;
        panButton.doClick();

        canvas.getDrawingSurface().repaint();
    }

    /**
     * Set the writeEmbeddedImage flag for all nodes under this node.
     */
    public void setEmbeddedImages(ZNode node, boolean flag) {
        if (node instanceof ZGroup) {
            ZNode[] children = ((ZGroup)node).getChildren();
            for (int i=0; i<children.length; i++) {
                setEmbeddedImages(children[i], flag);
            }
        }

        if (node instanceof ZVisualLeaf) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if (vc instanceof ZImage) {
                ((ZImage)vc).setWriteEmbeddedImage(flag);
            }
        }
    }

    public void save() {
        linkEventHandler.hideVisibleLinksAndHighlight();
        ZSelectionManager.unselectAll(getRoot());

        if (currentFileName == null) {
            ExtensionFileFilter filter[] = new ExtensionFileFilter[2];
            filter[0] = new ExtensionFileFilter();
            filter[0].addExtension(jazzExtension);
            filter[0].setDescription(jazzFilterDescription);

            filter[1] = new ExtensionFileFilter();
            filter[1].addExtension(jazzbExtension);
            filter[1].setDescription(jazzbFilterDescription);

            File file = QueryUserForFile(filter, "Save");
            canvas.repaint();

            if (file != null) {
                currentFileName = file.getAbsolutePath();
            }
        }

        if (currentFileName != null) {
            String extension = null;
            int i = currentFileName.lastIndexOf('.');
            if ((i > 0) && (i < currentFileName.length()-1) &&
                (i > currentFileName.lastIndexOf(File.separatorChar))) {
                extension = currentFileName.substring(i+1).toLowerCase();
            }

            if (extension == null) {
                if (lastFilter != null) {
                        // set extension to .jazz or .jazzb as chosen by user
                    if (lastFilter.getDescription().indexOf("Custom") > -1) {
                        extension = jazzExtension;
                    } else {
                        extension = jazzbExtension;
                    }
                }
                currentFileName = currentFileName + "." + extension;
            }

            // Pathnames are to be stored in jazz files relative to
            // current jazz save directory. Do a temporary 'set cwd'
            // so the writeObject methods can change filenames to
            // be relative to this new cwd
            String cwd = System.getProperty("user.dir");
            File cfn = new File(currentFileName);
            System.setProperty("user.dir",cfn.getParent());

            try {
                FileOutputStream fos =  new FileOutputStream(currentFileName);
                if (extension.equals(jazzExtension)) {
                    // write .jazz file
                    ZObjectOutputStream out = new ZObjectOutputStream(fos);
                    out.writeObject(getRoot());
                   out.close();

                } else if (extension.equals(jazzbExtension)) {

                        // set WriteEmbeddedImage flag on images to what user
                        // chose in fileChooser dialog
                    if (embed.isSelected()) {
                        setEmbeddedImages(getRoot(), true);
                    } else {
                        setEmbeddedImages(getRoot(), false);
                    }

                        // write .jazzb file
                    ObjectOutputStream out = new ObjectOutputStream(fos);
                    out.writeObject(getRoot());
                    out.close();
                }
                fos.close();
            } catch (Exception exception) {
                System.out.println(exception);
            }
            // restore the cwd
            System.setProperty("user.dir",cwd);
        }
    }

    public void saveAs() {
        currentFileName = null;
        save();
    }

    public void printScreen() {
        surface.printSurface();
    }

    /**
     * Checkbox "embed images in save file"
     * enabled for .jazzb files only
     */
    public void enableEmbedCheckBox() {
        javax.swing.filechooser.FileFilter ff = fileChooser.getFileFilter();
        if (ff.getDescription().indexOf("Java-Serialized") > -1) {
            embed.setEnabled(true);
        } else {
            embed.setSelected(false);
            embed.setEnabled(false);
        }
    }

    public File QueryUserForFile(javax.swing.filechooser.FileFilter[] filter, String approveText) {
        File file = null;
        fileChooser = new JFileChooser(prevFile);

        if (filter != null) {
            javax.swing.filechooser.FileFilter currentFilter = null;
            for (int f=0; f < filter.length; f++) {
                if (filter[f] != null) {
                    fileChooser.addChoosableFileFilter(filter[f]);
                    if ((lastFilter != null) &&
                        (lastFilter.getDescription().equals(filter[f].getDescription()))) {
                        currentFilter = filter[f];
                    }
                }
            }

            javax.swing.filechooser.FileFilter allFilesFilter = fileChooser.getAcceptAllFileFilter();
            if (allFilesFilter != null) {
                boolean yn = fileChooser.removeChoosableFileFilter(allFilesFilter);
            }

                    // default filter is last that user selected
            if (currentFilter != null) {
                fileChooser.setFileFilter(currentFilter);
            } else {
                fileChooser.setFileFilter(filter[0]);
            }
        }
        if (approveText.equals("Save")) {
            JPanel cb = new JPanel();
            cb.setLayout(new FlowLayout(FlowLayout.LEFT));

                    // Checkbox "embed images in save file"
                    // enabled for .jazzb files only
            embed = new JCheckBox("Embed images in save file");
            if (fileChooser.getFileFilter().getDescription().indexOf("Java-Serialized") > -1) {
                embed.setEnabled(true);
            } else {
                embed.setEnabled(false);
            }
            cb.add(embed);
            fileChooser.add(cb);

            fileChooser.addPropertyChangeListener(cmdTable.lookupPropertyListener("fileChooserSaveSelection"));
        }

        int retval = fileChooser.showDialog(getCanvas(), approveText);
        if (retval == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            prevFile = file;
            lastFilter = fileChooser.getFileFilter();
        }

        return file;
    }

    public File QueryUserForFile(javax.swing.filechooser.FileFilter filter, String approveText) {
        File file = null;
        fileChooser = new JFileChooser(prevFile);

        if (filter != null) {
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);
        }
        int retval = fileChooser.showDialog(getCanvas(), approveText);
        if (retval == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            prevFile = file;
        }

        return file;
    }

    /**
     * Merge a new sceneGraph into current sceneGraph
     * @param mergeRoot root of sceneGraph to be merged.
     */
    public void mergeScene(ZRoot mergeRoot) {
                    // Get the layer to be merged
        ZNode[] mergeChildren = mergeRoot.getChildren();
        int numMergeChildren = mergeRoot.getNumChildren();
        ZLayerGroup mergeLayer = (ZLayerGroup)mergeChildren[0];

                    // Get the current layer
        ZRoot root = canvas.getRoot();
        ZNode[] children = root.getChildren();
        ZLayerGroup layer = (ZLayerGroup)children[0];
        ZVisualLeaf cameraNode = (ZVisualLeaf)children[1];
        camera = (ZCamera)cameraNode.getFirstVisualComponent();

        ZTransformGroup transform;
        for ( int i = 0; i < numMergeChildren; i++ ) {
            if (mergeChildren[i] instanceof ZLayerGroup) {
                mergeLayer = (ZLayerGroup) mergeChildren[i];
                if (mergeLayer.getNumChildren() != 0 ) {

                    // Set transform of merged Layers
                    transform = mergeLayer.editor().getTransformGroup();
                    transform.setTransform(getCamera().getInverseViewTransform());

                    // Add another merge layer under the current layer
                    getDrawingLayer().addChild(transform);
                }
            }
        }

                    // Get the merge layers the merge camera looked at
        ZVisualLeaf mergeCameraNode = (ZVisualLeaf) mergeChildren[1];
        ZCamera mergeCamera = (ZCamera) mergeCameraNode.getFirstVisualComponent();
        int numMergeLayers = mergeCamera.getNumLayers();
        ZLayerGroup[] mergeLayerGroup = mergeCamera.getLayers();

                // Add those merge layers onto the current camera
        for ( int i = 0; i < numMergeLayers; i++ ) {
            if (mergeLayerGroup[i] instanceof ZLayerGroup) {
                mergeLayer = (ZLayerGroup) mergeLayerGroup[i];
                if (mergeLayer.getNumChildren() != 0 )
                    camera.addLayer(mergeLayer);
            }
        }

        canvas.getDrawingSurface().repaint();
    }

    public void insertFile() {
        // Pathnames are to be stored in jazz files relative to
        // current jazz save directory. Do a temporary 'set cwd'
        // so the readObject methods can change filenames to
        // be relative to this new cwd
        File file = getJazzFile();
        if (file != null) {
            String cwd = System.getProperty("user.dir");
            System.setProperty("user.dir",file.getParent());
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(currentFileName);
            }
            catch (java.io.FileNotFoundException e) {
                System.out.println("File " + currentFileName + " not found.");
                return;
            }

            String extension = null;
            int i = currentFileName.lastIndexOf('.');
            if(i > 0 && i < currentFileName.length()-1) {
                extension = currentFileName.substring(i+1).toLowerCase();
            }

            if (inStream != null) {
                if (extension.equals(jazzExtension)) {
                    if (parser == null) {
                        parser = new ZParser();
                    }
                    try {
                        root = (ZRoot)parser.parse(inStream);
                        mergeScene(root);
                    } catch (ParseException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Invalid file format");
                    }
                } else if (extension.equals(jazzbExtension)) {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(inStream);
                        root = (ZRoot)ois.readObject();
                        mergeScene(root);
                        ois.close();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else if(extension.equals(SVGExtension)) {
                    try {
                        ZSVG svg = new ZSVG();
                        ZGroup group = svg.read(file);
                        canvas.getLayer().addChild(group);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            // restore the cwd
            System.setProperty("user.dir", cwd);
        }
    }

    public void insertImage() {
        File file = QueryUserForFile((javax.swing.filechooser.FileFilter[])null, "Open");
        if (file != null) {
            java.awt.Image ji = getCanvas().getToolkit().getImage(file.getAbsolutePath());
            MediaTracker tracker = new MediaTracker(getCanvas());
            tracker.addImage(ji, 0);
            try {
                tracker.waitForID(0);
            }
            catch (InterruptedException exception) {
                System.out.println("Couldn't load image: " + file);
            }
            ZImage zi = new ZImage(ji);
            zi.setFileName(file.getAbsolutePath());
            zi.setWriteEmbeddedImage(false);
            ZVisualLeaf leaf = new ZVisualLeaf(zi);
            ZTransformGroup transform = leaf.editor().getTransformGroup();
            transform.setTransform(getCamera().getInverseViewTransform());
            getDrawingLayer().addChild(transform);
        }
    }

    public void cut() {
        ZNode node;
        ZNode handle;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        copyBuffer.clear();
        String text = null;
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            unselect(node);
            handle = node.editor().getTop();
            getDrawingLayer().removeChild(handle);
            copyBuffer.add(handle);

            // copy text to system clipboard
            if (clipboard != null) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if ((node instanceof ZVisualLeaf) && (vc instanceof ZText)) {
                if (text != null) {
                    text += "\n" + ((ZText)vc).getText();
                } else {
                    text = ((ZText)vc).getText();
                }
            }
            }
        }

        if ((clipboard != null) && (text != null)) {
            StringSelection fieldContent = new StringSelection (text);
            clipboard.setContents (fieldContent, HiNoteCore.this);
        }
                    // Store info so we know where to paste
        pasteDelta.setToIdentity();
        pasteViewTransform = getCamera().getViewTransform();
    }


    public void copy() {
        ZNode node;
        ZNode copy;
        ZNode handle;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(camera);

        copyBuffer.clear();
        String text = null;
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            unselect(node);
            handle = node.editor().getTop();
            copy = (ZNode)handle.clone();
            copyBuffer.add(copy);
            select(node);

            // copy text to system clipboard
            if (clipboard != null && (node instanceof ZVisualLeaf)) {
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if ((node instanceof ZVisualLeaf) && (vc instanceof ZText)) {
                if (text != null) {
                    text += "\n" + ((ZText)vc).getText();
                } else {
                    text = ((ZText)vc).getText();
                }
            }
            }
        }
        if ((clipboard != null) && (text != null)) {
            StringSelection fieldContent = new StringSelection (text);
            clipboard.setContents (fieldContent, HiNoteCore.this);
        }
                    // Store info so we know where to paste
        pasteDelta.setToIdentity();
        pasteViewTransform = camera.getViewTransform();
    }

    public void paste() {
        ZCamera camera = getCamera();
        ZGroup layer = getDrawingLayer();
        ZNode node;
        ZNode copy;
        ZTransformGroup transform;


        AffineTransform currentTransform = camera.getViewTransform();
        if (pasteViewTransform.equals(currentTransform)) {
                    // Update paste delta
            pasteDelta.translate(10.0, 10.0);
        } else {
                    // Update paste position relative to current camera if it has changed
            try {
                AffineTransform newDelta = currentTransform.createInverse();
                newDelta.concatenate(pasteViewTransform);
                newDelta.concatenate(pasteDelta);
                pasteDelta = newDelta;
                pasteViewTransform = currentTransform;
            } catch (NoninvertibleTransformException e) {
            }
        }

        unselectAll(camera);
        ZSceneGraphEditor editor;
        // if local clipboad (copyBuffer) is empty, try system clipboard
        if (clipboard != null) {
            if (copyBuffer.isEmpty()) {
                Transferable clipboardContent = clipboard.getContents (this);

                if ((clipboardContent != null) &&
                    (clipboardContent.isDataFlavorSupported (DataFlavor.stringFlavor))) {
                    try {
                        String tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
                        ZText textComp = new ZText(tempString);
                        textComp.setFont(font);
                        textComp.setPenColor(penColor);
                        textComp.setGreekThreshold(15);
                        ZVisualLeaf textNode = new ZVisualLeaf(textComp);
                        ZNode handle = ((ZNode)textNode).editor().getTop();
                        copyBuffer.add(handle);
                    }
                    catch (Exception e) {
                        e.printStackTrace ();
                    }
                }
            }
        }

        for (Iterator i=copyBuffer.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            copy = (ZNode)node.clone();
            editor = copy.editor();

            transform = editor.getTransformGroup();
            transform.preConcatenate(pasteDelta);
            layer.addChild(editor.getTop());
            select(copy);
        }

        if (clipboard != null) {
            getOwnershipClipboard();
        }
    }

    public void raise() {
        ZNode node;
        ZNode handle;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            handle = node.editor().getTop();
            handle.raise();
        }
    }

    public void lower() {
        boolean first = true;
        ZNode node;
        ZNode prev = null;
        ZNode handle;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            handle = node.editor().getTop();
                    // Need to be careful here to make sure that the relative
                    // order of several lowered objects stays the same
            if (first) {
                first = false;
                handle.lower();
            } else {
                handle.raiseTo(prev);
            }
            prev = handle;
        }
    }

    public void setMinMag() {
        ZNode node;
        ZFadeGroup fade;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(camera);

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            fade = node.editor().getFadeGroup();
            fade.setFadeType(ZFadeGroup.CAMERA_MAG);
            fade.setMinMag(camera.getMagnification());
        }
    }

    public void setMaxMag() {
        ZNode node;
        ZFadeGroup fade;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(camera);

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            fade = node.editor().getFadeGroup();
            fade.setFadeType(ZFadeGroup.CAMERA_MAG);
            fade.setMaxMag(camera.getMagnification());
        }
    }

    public void clearMinMaxMag() {
        ZNode node;
        ZFadeGroup fade;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(camera);

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            fade = node.editor().getFadeGroup();
            fade.setFadeType(ZFadeGroup.CAMERA_MAG);
            fade.setMinMag(0);
            fade.setMaxMag(-1);
        }
    }

    public void makeSticky(int constraintType) {
        ZNode node;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(camera);

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            unselect(node);
            ZStickyGroup.makeSticky(node, camera, constraintType);
            select(node);
        }
    }

    public void makeUnSticky() {
        ZNode node;
        ZCamera camera = getCamera();
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());
        ZSceneGraphEditor editor;

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            editor = node.editor();

            if (editor.hasStickyGroup()) {
                unselect(node);
                ZStickyGroup.makeUnSticky(node);
                select(node);
            }
        }
    }

    public void select(ZNode node) {
        ZSelectionManager.select(node);
    }

    public void unselect(ZNode node) {
        ZSelectionManager.unselect(node);
    }

    public void selectAll() {
        ZGroup layer = getLayer();
        ZNode[] children = layer.getChildren();
        ZNode child;

        for (int i=0; i<children.length; i++) {
            child = children[i].editor().getNode();
            select(child);
        }
    }

    public void unselectAll(ZCamera camera) {
        ZSelectionManager.unselectAll(camera);
    }

    public void delete() {
        ZNode node;
        ZNode handle;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

                    // Pressing the delete key while editing a text field
                    // should just delete a character, not the whole node
        if (selection.size() == 1) {
            node = (ZNode)selection.get(0);
            ZVisualComponent vc = ((ZVisualLeaf)node).getFirstVisualComponent();
            if ((node instanceof ZVisualLeaf) && (vc instanceof ZText)) {
                if (((ZText)vc).getEditable()) {
                    return;
                }
            }
        }

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            handle = node.editor().getTop();
            handle.getParent().removeChild(handle);
        }
    }

    /**
     * Group all selected nodes. If other nodes are part of other groups,
     * the structure is preserved.
     */
    public void group() {
        ZNode node;
        ZNode handle;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

                    // Not enough nodes to group
        if (selection.size() <= 1) {
            return;
        }


                    // Create a new group node, and put it under the layer node
            ZGroup group = new ZGroup();
        group.putClientProperty("group", group);
        layer.addChild(group);
        group.setChildrenPickable(false);
        group.setChildrenFindable(false);

                    // Move nodes to be grouped to the new group
        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();
            unselect(node);
            handle = node.editor().getTop();
            handle.setParent(group);
        }
        select(group);
    }

    /**
     * Ungroup all selected groups, and transform each group member by the group's transform
     * so that after they are ungrouped they don't move in global coordinates.
     */
    public void ungroup() {
        ZNode node;
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        if (selection.isEmpty()) {
            return;
        }

        ZGroup group;
        ZTransformGroup childTransform;
        ZNode handle;
        ZNode[] children;

        for (Iterator i=selection.iterator(); i.hasNext();) {
            node = (ZNode)i.next();

            if (node.getClientProperty("group") != null) {
                group = (ZGroup)node;
                children = group.getChildren();
                for (int j=0; j<children.length; j++) {
                    children[j].reparent(layer);
                    select(children[j].editor().getNode());
                }

                handle = group.editor().getTop();
                layer.removeChild(handle);
            }
        }
    }

    public void indexGroup() {
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        if (selection.size() != 1) {
            System.out.println("RTree index error1: You can only index a single group of nodes");
            return;
        }

        if (! (selection.get(0) instanceof ZGroup)) {
            System.out.println("RTree index error2: You can only index a single group of nodes");
            return;
        }

        ZGroup group = (ZGroup)selection.get(0);
        if (group.editor().hasSpatialIndexGroup()) {
            System.out.println("RTree index error: nodes already indexed.");
            return;
        }
        ZSpatialIndexGroup index = group.editor().getSpatialIndexGroup();
    }

    public void unIndexGroup() {
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        if (selection.size() != 1) {
            System.out.println("RTree index error1: You can only unIndex a single group");
            return;
        }

        if (! (selection.get(0) instanceof ZGroup)) {
            System.out.println("RTree index error2: You can only index a single group");
            return;
        }

        ZGroup group = (ZGroup)selection.get(0);
        if (! group.editor().hasSpatialIndexGroup()) {
            System.out.println("RTree index error: nodes not indexed.");
            return;
        }

        if (! group.editor().removeSpatialIndexGroup()) {
            System.out.println("unidexing failed on node: " + group);
        }
    }

    public void dumpRtree() {
        ArrayList selection = ZSelectionManager.getSelectedNodes(getCamera());

        if (selection.size() != 1) {
            System.out.println("dumpRtree error: Select a single node that is part of an index group.");
            return;
        }

        ZNode selectedNode = (ZNode)selection.get(0);
        ZNode topGroup = selectedNode.editor().getTop();
        if ( !(topGroup.getParent() instanceof ZGroup)) {
            System.out.println("dumpRtree error: selected node not part of a group.");
            return;
        }

        ZGroup group = topGroup.getParent();

        if (! group.editor().hasSpatialIndexGroup()) {
            System.out.println("dumpRtree error: selected node not indexed.");
            return;
        }

        ZSpatialIndexGroup ig = group.editor().getSpatialIndexGroup();
        ig.displayTree("RTree");
    }

    public void setEventHandler(int newEventHandlerMode) {
                    // First, exit old event handler mode
        switch (currentEventHandlerMode) {
            case TEXT_MODE:
                keyboardNavEventHandler.setActive(true);
                break;
            case SELECTION_MODE:
                keyboardNavEventHandler.setActive(true);
                break;
            default:
        }

                    // Then, deactivate old event handler
        if (activeEventHandler != null) {
            activeEventHandler.setActive(false);
        }

                    // Set up new event handler mode
        switch (newEventHandlerMode) {
            case PAN_MODE:
                activeEventHandler = panEventHandler;
                getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                keyboardNavEventHandler.setActive(true);
                break;
            case LINK_MODE:
                activeEventHandler = linkEventHandler;
                getCanvas().setCursor(crosshairCursor);
                keyboardNavEventHandler.setActive(true);
                break;
            case POLYLINE_MODE:
                activeEventHandler = squiggleEventHandler;
                getCanvas().setCursor(crosshairCursor);
                keyboardNavEventHandler.setActive(true);
                break;
            case POLYGON_MODE:
                activeEventHandler = polygonEventHandler;
                getCanvas().setCursor(crosshairCursor);
                keyboardNavEventHandler.setActive(true);
                break;
            case ELLIPSE_MODE:
                activeEventHandler = ellipseEventHandler;
                getCanvas().setCursor(crosshairCursor);
                keyboardNavEventHandler.setActive(true);
                break;
            case RECTANGLE_MODE:
                activeEventHandler = rectEventHandler;
                getCanvas().setCursor(crosshairCursor);
                keyboardNavEventHandler.setActive(true);
                break;
            case TEXT_MODE:
                keyboardNavEventHandler.setActive(false);
                activeEventHandler = textEventHandler;
                getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                break;
            case SELECTION_MODE:
                keyboardNavEventHandler.setActive(false);
                activeEventHandler = selectionEventHandler;
                getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                break;
            case COLORPICKER_MODE:
                colorPickerEventHandler.setActive(true);
                activeEventHandler = colorPickerEventHandler;
                getCanvas().setCursor(colorpickerCursor);
                break;
            default:
                activeEventHandler = null;
                currentEventHandlerMode = 0;
        }
                    // Finally, activate new event handler
        if (activeEventHandler != null) {
            activeEventHandler.setActive(true);
            currentEventHandlerMode = newEventHandlerMode;
        }
    }

    public void showTreeView() {
        if (treeView == null) {
            treeView = new ZSceneGraphTreeView(canvas);
            treeView.addWindowListener(new WindowAdapter() {
                public void windowDeactivated(WindowEvent e){
                    treeView = null;
                }
                public void windowClosed(WindowEvent e) {
                    treeView = null;
                }
            });

            treeView.pack();
            treeView.show();
        }
    }

    public int getCurrentHandlerMode() {
        return currentEventHandlerMode;
    }
}