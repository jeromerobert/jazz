/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/**
 * <b>FontChooser</b>allows users to pick a font by name, size, and style.
 * This dialog builds an AttributeSet suitable for use with JTextPane.
 *
 * @author  James J. Mokwa
 */
public class FontChooser extends JDialog implements ActionListener {

    JComboBox fontName;
    JTextField fontSize;
    JLabel previewLabel;
    SimpleAttributeSet attributes;
    Font newFont;

    public FontChooser(Frame parent, Font currentFont) {
	super(parent, "Font Chooser", false);
	setSize(500, 210);
	attributes = new SimpleAttributeSet();

				// make sure that any way they cancel the window works
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		closeAndCancel();
	    }
	});

	// Set up user interface
	Container c = getContentPane();
    
	JPanel fontPanel = new JPanel();

				// Get all available fonts from local system.
	String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	
	fontName = new JComboBox(availableFonts);

				// set scrollbar to current font name
	fontName.setSelectedIndex(1);
	if (currentFont != null) {
	    String currentFontName = currentFont.getName();
	    for (int i=0; i<availableFonts.length; i++) {
		if (availableFonts[i].equals(currentFontName)) {
		    fontName.setSelectedIndex(i);
		    break;
		}
	    }
	}

	fontName.addActionListener(this);

	int size = 40;
	if (currentFont != null) {
	    size = currentFont.getSize();
	}
	Integer iSize = new Integer(size);
	fontSize = new JTextField(iSize.toString(), 4);
	fontSize.setHorizontalAlignment(SwingConstants.RIGHT);
	fontSize.addActionListener(this);

	fontPanel.add(fontName);
	fontPanel.add(new JLabel(" Size: "));
	fontPanel.add(fontSize);

	c.add(fontPanel, BorderLayout.NORTH);
    
	JPanel previewPanel = new JPanel(new BorderLayout());
	previewLabel = new JLabel("Here's a sample of this font.");
	previewPanel.add(previewLabel, BorderLayout.CENTER);

	// Add in the Ok, Apply and Cancel buttons for our dialog box
	JButton okButton = new JButton("Ok");
	okButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		fontSize.postActionEvent();
		apply();
		closeAndSave();
	    }
	});

	JButton applyButton = new JButton("Apply");
	applyButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		fontSize.postActionEvent();
		apply();
	    }
	});

	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		closeAndCancel();
	    }
	});

	JPanel controlPanel = new JPanel();
	controlPanel.add(okButton);
	controlPanel.add(applyButton);
	controlPanel.add(cancelButton);
	previewPanel.add(controlPanel, BorderLayout.SOUTH);

				// Give the preview label room to grow.
	previewPanel.setMinimumSize(new Dimension(100, 100));
	previewPanel.setPreferredSize(new Dimension(100, 100));

	c.add(previewPanel, BorderLayout.SOUTH);
    }

				// Something in the font changed: 
                                // make a new font for the preview label
    public void actionPerformed(ActionEvent ae) {
				// Check the name of the font
	if (!StyleConstants.getFontFamily(attributes)
	    .equals(fontName.getSelectedItem())) {
	    StyleConstants.setFontFamily(attributes,
					 (String)fontName.getSelectedItem());
	}

				// Check the font size
	if (StyleConstants.getFontSize(attributes) != 
	    Integer.parseInt(fontSize.getText())) {
	    StyleConstants.setFontSize(attributes, 
				       Integer.parseInt(fontSize.getText()));
	}

				// Update the preview label
	updatePreviewFont();

				// notify hinote
	newFont = previewLabel.getFont();
	firePropertyChange("fontComponentSelection", null, null);
    }

				// Get the appropriate font from the attributes object
				// and update the preview label
    protected void updatePreviewFont() {
	String name = StyleConstants.getFontFamily(attributes);
	boolean bold = StyleConstants.isBold(attributes);
	boolean ital = StyleConstants.isItalic(attributes);
	int size = StyleConstants.getFontSize(attributes);

	Font f = new Font(name, (bold ? Font.BOLD : 0) +
			  (ital ? Font.ITALIC : 0), size);
	previewLabel.setFont(f);
    }

    public Font getFont() { return newFont; }

    public AttributeSet getAttributes() { return attributes; }

    public void apply() {
				// notify hinote that the font changed
	newFont = previewLabel.getFont();
	firePropertyChange("fontComponentSelection", null, null);
    }

    public void closeAndSave() {
				// save font & color information
	newFont = previewLabel.getFont();

	// and then close the window
	setVisible(false);
    }

    public void closeAndCancel() {
				// erase any font information and then close the window
	newFont = null;
	setVisible(false);
    }
}
