/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.component.*;

/**
 * A test of Swing components within a Jazz ZCanvas.
 * Several examples assumes you are in the jazz/test directory.  You might
 * invoke this test with "java -cp classes;../src/classes SwingJazzTest"
 * from the jazz/test directory.
 *
 * @author Ben B. Bederson
 * @author Lance E. Good
 */
public class SwingJazzExample extends AbstractExample {
    public SwingJazzExample() {
        super("Swing-Jazz example");
    }
    public java.lang.String getExampleDescription() {
        return "This example shows how to embed swing components into Jazz. " +
        "When the mouse is over a swing component you cannot pan and zoom with Jazz " +
        "since all the events are getting delivered to the swing component.";
    }
    public void initializeExample() {
        super.initializeExample();
	ClassLoader loader;
        ZCanvas canvas;

                                // Set up basic frame
        setBounds(100, 100, 400, 400);
        setResizable(true);
        setBackground(null);
        setVisible(true);
        canvas = new ZCanvas();
        getContentPane().add(canvas);
        validate();
	loader = getClass().getClassLoader();

        ZVisualLeaf leaf;
        ZTransformGroup transform;
        ZSwing swing;
        ZSwing swing2;

        // JButton
        JButton button = new JButton("Button");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        swing = new ZSwing(canvas, button);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-500,-500);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // 2nd Copy of JButton
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-450, -450);
        transform.rotate(Math.PI/2);
        transform.scale(0.5);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // Growable JTextArea
	JTextArea textArea = new JTextArea("This is a growable TextArea.\nTry it out!");
        textArea.setBorder(new LineBorder(Color.blue,3));
        swing = new ZSwing(canvas, textArea);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-250,-500);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // Growable JTextField
        JTextField textField = new JTextField("A growable text field");
        swing = new ZSwing(canvas, textField);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(0, -500);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A Slider
        JSlider slider = new JSlider();
        swing = new ZSwing(canvas, slider);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(250, -500);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A Scrollable JTree
        JTree tree = new JTree();
        tree.setEditable(true);
        JScrollPane p = new JScrollPane(tree);
        p.setPreferredSize(new Dimension(150,150));
        swing = new ZSwing(canvas, p);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-500,-250);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A Scrollable JTextArea
        JScrollPane pane = new JScrollPane(new JTextArea("A Scrollable Text Area\nTry it out!"));
        pane.setPreferredSize(new Dimension(150,150));
        swing = new ZSwing(canvas, pane);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.setHasOneChild(true);
        transform.translate(-250,-250);
        transform.addChild(leaf);
                canvas.getLayer().addChild(transform);
        swing2 = swing;

        // A non-scrollable JTextField
        // A panel MUST be created with double buffering off
        JPanel panel = new JPanel(false);
        textField = new JTextField("A fixed-size text field");
        panel.setLayout(new BorderLayout());
        panel.add(textField);
        swing = new ZSwing(canvas, panel);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(0, -250);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A JComboBox
        String[] listItems = {"Summer Teeth", "Mermaid Avenue", "Being There", "A.M."};
        ZComboBox box = new ZComboBox(listItems);
        swing = new ZSwing(canvas, box);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(0, -150);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A panel with TitledBorder and JList
        panel = new JPanel(false);
        panel.setBackground(Color.lightGray);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED),"A JList", TitledBorder.LEFT, TitledBorder.TOP));
        panel.setPreferredSize(new Dimension(200,200));
        Vector data = new Vector();
        data.addElement("Choice 1");
        data.addElement("Choice 2");
        data.addElement("Choice 3");
        data.addElement("Choice 4");
        data.addElement("Choice 5");
        JList list = new JList(data);
        list.setBackground(Color.lightGray);
        panel.add(list);
        swing = new ZSwing(canvas, panel);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(250, -250);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A JLabel
        JLabel label = new JLabel("A JLabel",
				  new ImageIcon(loader.getResource("HCIL-logo.gif")),SwingConstants.CENTER);

        swing = new ZSwing(canvas, label);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-500,0);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // Rotated copy of the Scrollable JTextArea
        leaf = new ZVisualLeaf(swing2);
        transform = new ZTransformGroup();
        transform.setHasOneChild(true);
        transform.translate(-100, 0);
        transform.rotate(Math.PI/2);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // A panel with layout
        // A panel MUST be created with double buffering off
        panel = new JPanel(false);
        panel.setLayout(new BorderLayout());
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        label = new JLabel("A Panel with Layout");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.white);
        panel.setBackground(Color.red);
        panel.setPreferredSize(new Dimension(150,150));
                panel.setBorder(new EmptyBorder(5,5,5,5));
        panel.add(button1,"North");
        panel.add(button2,"South");
        panel.add(label,"Center");
        panel.revalidate();
        swing = new ZSwing(canvas, panel);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(0,0);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // JTable Example
        Vector columns = new Vector();
        columns.addElement("Check Number");
        columns.addElement("Description");
        columns.addElement("Amount");
        Vector rows = new Vector();
        Vector row = new Vector();
        row.addElement("101");
        row.addElement("Sandwich");
        row.addElement("$20.00");
        rows.addElement(row);
        row = new Vector();
        row.addElement("102");
        row.addElement("Monkey Wrench");
        row.addElement("$100.00");
        rows.addElement(row);
        row = new Vector();
        row.addElement("214");
        row.addElement("Ant farm");
        row.addElement("$55.00");
        rows.addElement(row);
        row = new Vector();
        row.addElement("215");
        row.addElement("Self-esteem tapes");
        row.addElement("$37.99");
        rows.addElement(row);
        row = new Vector();
        row.addElement("216");
        row.addElement("Tube Socks");
        row.addElement("$7.45");
        rows.addElement(row);
        row = new Vector();
        row.addElement("220");
        row.addElement("Ab Excerciser");
        row.addElement("$56.95");
        rows.addElement(row);
        row = new Vector();
        row.addElement("319");
        row.addElement("Y2K Supplies");
        row.addElement("$4624.33");
        rows.addElement(row);
        row = new Vector();
        row.addElement("332");
        row.addElement("Tie Rack");
        row.addElement("$15.20");
        rows.addElement(row);
        row = new Vector();
        row.addElement("344");
        row.addElement("Swing Set");
        row.addElement("$146.59");
        rows.addElement(row);
        JTable table = new JTable(rows,columns);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(30);
        TableColumn c = table.getColumn(table.getColumnName(0));
        c.setPreferredWidth(150);
        c = table.getColumn(table.getColumnName(1));
        c.setPreferredWidth(150);
        c = table.getColumn(table.getColumnName(2));
        c.setPreferredWidth(150);
        pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(200,200));
        table.setDoubleBuffered(false);
                swing = new ZSwing(canvas, pane);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(250,0);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // JEditorPane - HTML example
        try {

	    
            final JEditorPane editorPane = new JEditorPane(loader.getResource("csdept.html"));
            editorPane.setDoubleBuffered(false);
            editorPane.setEditable(false);
            pane = new JScrollPane(editorPane);
            pane.setDoubleBuffered(false);
            pane.setPreferredSize(new Dimension(400,400));
            editorPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            editorPane.setPage(e.getURL());
                        }
                        catch (IOException ioe) {
                            System.out.println("Couldn't Load Web Page");
                        }
                    }
                }
            });
            swing = new ZSwing(canvas, pane);
            leaf = new ZVisualLeaf(swing);
            transform = new ZTransformGroup();
            transform.translate(-500,250);
            transform.addChild(leaf);
            canvas.getLayer().addChild(transform);

        }
        catch (IOException ioe) {
            System.out.println("Couldn't Load Web Page");
        }

        // A JInternalFrame with a JSplitPane - a JOptionPane - and a
        // JToolBar
        JInternalFrame iframe = new JInternalFrame("JInternalFrame");
        iframe.getRootPane().setDoubleBuffered(false);
        ((JComponent)iframe.getContentPane()).setDoubleBuffered(false);
        iframe.setPreferredSize(new Dimension(500,500));
        JTabbedPane tabby = new JTabbedPane();
        tabby.setDoubleBuffered(false);
        iframe.getContentPane().setLayout(new BorderLayout());
        JOptionPane options = new JOptionPane("This is a JOptionPane!",
                                              JOptionPane.INFORMATION_MESSAGE,
                                              JOptionPane.DEFAULT_OPTION);
        options.setDoubleBuffered(false);
        options.setMinimumSize(new Dimension(50,50));
        options.setPreferredSize(new Dimension(225,225));
        JPanel tools = new JPanel(false);
        tools.setMinimumSize(new Dimension(150,150));
        tools.setPreferredSize(new Dimension(225,225));
        JToolBar bar = new JToolBar();
        Action letter = new AbstractAction("Big A!",new ImageIcon(loader.getResource("letter.gif"))) {

		public void actionPerformed(ActionEvent e) {}
	    };

        Action hand = new AbstractAction("Hi!",new ImageIcon(loader.getResource("hand.gif"))) {
		public void actionPerformed(ActionEvent e) {}
	    };
        Action select = new AbstractAction("There!",
                                           new ImageIcon(loader.getResource("select.gif"))) {
		public void actionPerformed(ActionEvent e) {}
	    };

        label = new JLabel("A Panel with a JToolBar");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        bar.add(letter);
        bar.add(hand);
        bar.add(select);
        bar.setFloatable(false);
        bar.setBorder(new LineBorder(Color.black,2));
        tools.setLayout(new BorderLayout());
        tools.add(bar,"North");
        tools.add(label,"Center");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,options,tools);
        split.setDoubleBuffered(false);
        iframe.getContentPane().add(split);
        swing = new ZSwing(canvas, iframe);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(0,250);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        JMenuBar menuBar = new JMenuBar();
        ZMenu menu = new ZMenu("File");
        ZMenu sub = new ZMenu("Export");
        JMenuItem gif = new JMenuItem("Funds");
        sub.add(gif);
        menu.add(sub);
        menuBar.add(menu);
        iframe.setJMenuBar(menuBar);

        iframe.setVisible(true);

        // A JColorChooser - also demonstrates JTabbedPane
        JColorChooser chooser = new JColorChooser();
        swing = new ZSwing(canvas, chooser);
        leaf = new ZVisualLeaf(swing);
        transform = new ZTransformGroup();
        transform.translate(-250,850);
        transform.addChild(leaf);
        canvas.getLayer().addChild(transform);

        // Revalidate and repaint
        canvas.revalidate();
        canvas.getDrawingSurface().repaint();
    }
}
