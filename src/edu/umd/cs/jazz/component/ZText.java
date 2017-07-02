/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.font.GlyphMetrics;
import java.io.*;
import java.util.*;
import java.awt.font.TextAttribute;
import java.text.*;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

// bug note:
//   Some editing commands are intercepted by hinote shortcuts
//   Example: C-p should move curser forward, not popup a "Print" dialog box.

/**
 * ZText creates a visual component to support text. Multiple lines can
 * be entered, and basic editing is supported. A caret is drawn,
 * and can be repositioned with mouse clicks.
 */
public class ZText extends ZVisualComponent {

    /**
     * The low quality graphic2D render context: not antiAliased, and
     * does not use FractionalMetrics.
     */
    static protected final FontRenderContext LOW_QUALITY_FONT_CONTEXT = new FontRenderContext(null, false, false);

    /**
     * The high quality graphic2D render context: AntiAliased, and
     * uses FractionalMetrics.
     */
    static protected final FontRenderContext HIGH_QUALITY_FONT_CONTEXT = new FontRenderContext(null, true, true);

    /**
     * Below this magnification render text as 'greek'.
     */
    static protected final float   DEFAULT_GREEK_THRESHOLD = 5.5f;

    /**
     * Default color of text rendered as 'greek'.
     */
    static protected final Color   DEFAULT_GREEK_COLOR = Color.gray;

    /**
     * Default font name of text.
     */
    static protected final String  DEFAULT_FONT_NAME = "Helvetica";

    /**
     * Default font style for text.
     */
    static protected final int     DEFAULT_FONT_STYLE = Font.PLAIN;

    /**
     * Default font size for text.
     */
    static protected final int     DEFAULT_FONT_SIZE = 12;

    /**
     * Default font for text.
     */
    static protected final Font    DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, DEFAULT_FONT_STYLE, DEFAULT_FONT_SIZE);

    /**
     * Default color for text.
     */
    static protected final Color   DEFAULT_PEN_COLOR = Color.black;

    /**
     * Default background color for text.
     */
    static protected final Color   DEFAULT_BACKGROUND_COLOR = null;

    /**
     * Default caret color for text.
     */
    
    static protected final Color   DEFAULT_CARET_COLOR = Color.red;

    /**
     * Default specifying if text is editable.
     */
    static protected final boolean DEFAULT_EDITABLE = false;

    /**
     * Default text when new text area is created.
     */
    static protected final String  DEFAULT_TEXT = "";

    /**
     * Below this magnification text is rendered as greek.
     */
    protected float             greekThreshold = DEFAULT_GREEK_THRESHOLD;

    /**
     * Color for greek text.
     */
    protected Color             greekColor = DEFAULT_GREEK_COLOR;

    /**
     * Current pen color.
     */
    protected Color             penColor  = DEFAULT_PEN_COLOR;

    /**
     * Background color for text.
     */
    protected Color             backgroundColor = DEFAULT_BACKGROUND_COLOR;

    /**
     * Current caret color.
     */
    protected Color             caretColor = DEFAULT_CARET_COLOR;

    /**
     * Character position of caret within the current line.
     */

    protected int                caretPos = 0;
    /**
     * Line number of caret - current line.
     */

    protected int                caretLine = 0;
    /**
     * X coordinate of caret relative to its coordinate frame.
     */

    protected double             caretX = 0.0;
    /**
     * Y coordinate of caret relative to its coordinate frame.
     */

    protected double             caretY = 0.0;
    /**
     * Drawn shape of the caret.
     */

    protected Line2D             caretShape = new Line2D.Float();

    /**
     * Current text font.
     */
    protected Font               font = DEFAULT_FONT;
    
    /**
     * Each vector element is one line of text.
     */
    protected Vector            lines = new Vector();

    /**
     * Specifies if text is editable.
     */
    protected boolean	        editable = DEFAULT_EDITABLE;

    /**
     * The previously used font render context (i.e., from the last render).
     */
    protected FontRenderContext prevFRC = null;

    /**
     * jkd version <= 1.2.1 has a bug: font.getStringBounds() gives the
     * bounds of a space " " as zero.
     */
    protected boolean boundsBug = false;

    /**
     * Default constructor for ZText.
     */
    public ZText() {
	this("", DEFAULT_FONT);
    }

    /**
     * ZText constructor with initial text.
     * @param <code>str</code> The initial text.
     */
    public ZText(String str) {
	this(str, DEFAULT_FONT);
    }
    
    /**
     * ZText constructor with initial text and font.
     * @param <code>str</code> The initial text.
     * @param <code>font</code> The font for this ZText component.
     */
    public ZText(String str, Font font) {
	if ((System.getProperty("java.version").equals("1.2")) ||
	    (System.getProperty("java.version").equals("1.2.1"))) {
	    boundsBug = true;
	}

	setText(str);
	this.font = font;
	
	updateBounds();
    }
    
    /**
     * Constructs a new ZText that is a copy of the specified component (i.e., a "copy constructor").
     * @param <code>tf</code> The ZText object to make a deep copy of.
     */
    public ZText(ZText tf) {
	super();
				// Do a deep copy of vector of strings
	if ((System.getProperty("java.version").equals("1.2")) ||
	    (System.getProperty("java.version").equals("1.2.1"))) {
	    boundsBug = true;
	}

	String line;
	lines = new Vector(tf.lines.size());
	for (Iterator i = tf.lines.iterator() ; i.hasNext() ; ) {
	    line = (String)i.next();
	    lines.add(new String(line));
	}

	this.font = tf.getFont();
    }

    /**
     * Duplicates the current ZTextField by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     *
     * @see #ZText(ZText)
     */
    public Object clone() {
	return new ZText(this);
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Returns the current pen color.
     */
    public Color getPenColor() {return penColor;}

    /**
     * Sets the current pen color.
     * @param <code>color</code> use this color.
     */
    public void setPenColor(Color color) {
	penColor = color;
	damage();
    }   

    /**
     * Returns the current background color.
     */
    public Color getBackgroundColor() {return backgroundColor;}

    /**
     * Sets the current background color.
     * @param <code>color</code> use this color.
     */
    public void setBackgroundColor(Color color) {
	backgroundColor = color;
	damage();
    }   

    /**
     * Returns the current caret color.
     */
    public Color getCaretColor() {return caretColor;}

    /**
     * Sets the current caret color.
     * @param <code>color</code> use this color.
     */
    public void setCaretColor(Color color) {
	caretColor = color;
	damage();
    }   

    /**
     * Returns the current greek threshold. Below this magnification
     * text is rendered as 'greek'
     */
    public float getGreekThreshold() {return greekThreshold;}

    /**
     * Sets the current greek threshold. Below this magnification
     * text is rendered as 'greek'
     * @param <code>threshold</code> compared to renderContext magnification.
     */
    public void setGreekThreshold(float threshold) {
	greekThreshold = threshold;
	damage();
    }   

    /**
     * Determines if this text is editable.
     */
    public boolean getEditable() {return editable;}

    /**
     * Specifies whether this text is editable.
     * @param <code>editable</code> true or false.
     */
    public void setEditable(boolean editable) {
	this.editable = editable;
	damage();
    }   

    /**
     * Returns the current font.
     */
    public Font getFont() {return font;}
    
    /**
     * Return the text within this text component.
     * Multline text is returned as a single string
     * where each line is separated by a newline character.
     * Single line text does not have any newline characters.
     */
    public String getText() {
	String line;
	String result = new String();
	int lineNum = 0;

	for (Iterator i = lines.iterator() ; i.hasNext() ; ) {
	    if (lineNum > 0) {
		result += '\n';
	    }
	    line = (String)i.next();
	    result += line;
	    lineNum++;
	}

	return result;
    }

    /**
       Returns the character position of the caret, within the current line.
    */
    public int getCaretPos() {
	return caretPos;
    }

    /**
     * Returns the current line.
     */
    public int getCaretLine() {
	return caretLine;
    }

    /**
     * Sets the font for the text.
     * <p>
     * <b>Warning:</b> Java has a serious bug in that it does not support very small 
     * fonts.  In particular, fonts that are less than about a pixel high just don't work.
     * Since in Jazz, it is common to create objects of arbitrary sizes, and then scale them,
     * an application can easily create a text object with a very small font by accident.
     * The workaround for this bug is to create a larger font for the text object, and 
     * then scale the node down correspondingly.
     * @param <code>aFont</code> use this font.
     */
    public void setFont(Font aFont) {
	font = aFont;
	damage(true);
    }

    /**
     * Sets the text of this visual component to str. Multiple lines
     * of text are separated by a newline character.
     * @param <code>str</code> use this string.
     */
    public void setText(String str) {
	int pos = 0;
	int index;
	boolean done = false;
	lines = new Vector();
	do {
	    index = str.indexOf('\n', pos);
	    if (index == -1) {
		lines.addElement(str);
		done = true;
	    } else {
		lines.addElement(str.substring(0, index));
		str = str.substring(index + 1);
	    }
	} while (!done);

	damage(true);
    }

    /**
     * Adds a character before the caret position.
     * @param <code>c</code>Character to add.
     */
    public void addChar(char c) {
	String frontHalf = ((String)lines.elementAt(caretLine)).substring(0, caretPos);
	String backHalf = ((String)lines.elementAt(caretLine)).substring(caretPos);

	lines.setElementAt(frontHalf + c + backHalf, caretLine);
	caretPos++;
	damage(true);
    }

    /**
     * Creates a new line of text, splitting the current line at the caret
     * position.
     */
    public void addEnterChar() {
	String frontHalf = ((String)lines.elementAt(caretLine)).substring(0, caretPos);
	String backHalf = ((String)lines.elementAt(caretLine)).substring(caretPos);

	lines.setElementAt(frontHalf, caretLine);
	caretLine++;
	lines.insertElementAt(backHalf, caretLine);
	caretPos = 0;
	damage(true);
    }

    /**
     * Deletes the character after the caret position.
     */
    public void deleteChar() {
	String currentLine = (String)lines.elementAt(caretLine);
	if (caretPos == currentLine.length()) {
				// At end of line, so merge with next line
	    if (caretLine < (lines.size() - 1)) {
		lines.setElementAt(currentLine + (String)lines.elementAt(caretLine + 1), caretLine);
		lines.removeElementAt(caretLine + 1);
	    }
	} else {
	    String frontHalf = currentLine.substring(0, caretPos);
	    String backHalf = currentLine.substring(caretPos + 1);
	    
	    lines.setElementAt(frontHalf + backHalf, caretLine);
	}
	damage(true);
    }

    /**
     * Deletes the character before the caret position.
     */
    public void deleteCharBeforeCaret() {
	if ((caretPos > 0) || (caretLine > 0)) {
	    setCaretPos(getCaretPos() - 1);
	    deleteChar();
	}
    }

    /**
     * Deletes from the caret position to the end of line.
     * If caret is at the end of the line, joins current line to the next.
     */
    public void deleteToEndOfLine() {
	if (caretPos == ((String)lines.elementAt(caretLine)).length()) {
				// Delete carriage return at end of line
	    deleteChar();
	} else {
				// Else, Delete to end of line
	    String frontHalf = ((String)lines.elementAt(caretLine)).substring(0, caretPos);
	    lines.setElementAt(frontHalf, caretLine);
	    damage(true);
	}
    }

    /**
     * Sets the caretLine to line, if it exists.
     *  @param <code>line</code>Line number to use. Count starts
     * with zero.
     */
    public void setCaretLine(int line) {
	if (line < 0) {
	    caretLine = 0;
	} else if (line >= lines.size()) {
	    caretLine = lines.size()-1;
	} else {
	    caretLine = line;
	}

	// new line may be too short for current caret position
	setCaretPos(getCaretPos());
	damage();
    }


    /**
     * Set the caret this character position in the current line.
     *  @param <code>cp</code>Character position to use, starts with zero.
     */
    public void setCaretPos(int cp) {
	if (cp < 0) {
	    if (caretLine > 0) {
		caretLine--;
		caretPos = ((String)lines.elementAt(caretLine)).length();
	    } else {
		caretPos = 0;
	    }
	} else if (cp > ((String)lines.elementAt(caretLine)).length()) {
	    if (caretLine < (lines.size() - 1)) {
		caretLine++;
		caretPos = 0;
	    } else {
		caretPos = ((String)lines.elementAt(caretLine)).length();
	    }
	} else {
	    caretPos = cp;
	}
	damage();
    }
    
    /**
     * Set caret position to character closest to specified point (in object coords)
     *  @param <code>pt</code> object coordinates of a mouse click.
     */
    public void setCaretPos(Point2D pt) {
	LineMetrics lm = font.getLineMetrics((String)lines.elementAt(0), prevFRC);
	double height = lm.getHeight();
	double desc = lm.getDescent();
	caretLine = (int)((pt.getY()-desc)/height);
	if (pt.getY() < desc) caretLine = 0;
	if (caretLine >= lines.size()) caretLine = lines.size() - 1;

	// set caret to beginning of line
	if (pt.getX() < 6) {
	    caretPos = 0;
	    damage();
	    return;
	}
 	Rectangle2D bounds = null;
	double strWid;
	String textLine = (String)lines.elementAt(caretLine);
	String substr;
	caretPos = textLine.length();
	for (int ch=0; ch <= textLine.length(); ch++) {
	    substr = textLine.substring(0,ch);
	    bounds = font.getStringBounds(substr, prevFRC);
	    strWid = bounds.getWidth();

	    // jkd version <= 1.2.1 bug:
	    // bounds of a space " " returned as zero
	    if (boundsBug) {
		if ((substr != null) && (substr.length() > 0) &&
		    (substr.charAt(substr.length()-1) == ' ')) {
		    strWid += font.getStringBounds("t", prevFRC).getWidth();
		}
	    }

	    if (strWid > pt.getX()) {
		caretPos = ch;
		break;
	    }
	}
	damage();
    }

    //****************************************************************************
    //
    //	Keyboard event handler
    //
    //***************************************************************************

    /** Processes keyboard events. Implements basic text editing
     * and cursor movement.
     * @param <code>e</code> keyboard event object.
     */
    public void keyPressed(KeyEvent e) {
	int keyCode = e.getKeyCode();
	char keyChar = e.getKeyChar();

				// Skip modifier key movement
	if ((keyCode == KeyEvent.VK_SHIFT) ||
	    (keyCode == KeyEvent.VK_CONTROL) ||
	    (keyCode == KeyEvent.VK_ALT) ||
	    (keyCode == KeyEvent.VK_META)) {
	    return;
	}

	if (e.isControlDown()) {
				// Control key down
	    if (keyCode == KeyEvent.VK_A) {
				// Ctrl-A (beginning of line)
		setCaretPos(0);
	    } else if (keyCode == KeyEvent.VK_B) {
				// Ctrl-B (back one character)
		setCaretPos(getCaretPos() - 1);
	    } else if (keyCode == KeyEvent.VK_D) {
				// Ctrl-D (delete character before caret)
		deleteChar();
	    } else if (keyCode == KeyEvent.VK_E) {
				// Ctrl-E (end of line)
		setCaretPos(((String)lines.elementAt(caretLine)).length());
	    } else if (keyCode == KeyEvent.VK_F) {
				// Ctrl-F (forward one character)
		setCaretPos(getCaretPos() + 1);
	    } else if (keyCode == KeyEvent.VK_K) {
				// Ctrl-K (delete from caret to end of line)
		deleteToEndOfLine();
	    } else if (keyCode == KeyEvent.VK_N) {
				// Ctrl-N (move caret to next line)
		setCaretLine(getCaretLine() + 1);
	    } else if (keyCode == KeyEvent.VK_P) {
				// Ctrl-P (move caret to previous line)
		setCaretLine(getCaretLine() - 1);
	    }
	} else if (e.isAltDown()) {
				// Alt key down
	    if (keyCode == KeyEvent.VK_B) {
				// Alt-B (back one word)
		setCaretPos(getCaretPos() - 5);
	    } else if (keyCode == KeyEvent.VK_F) {
				// Alt-F (forward one word)
		setCaretPos(getCaretPos() + 5);
	    }
	} else {
				// no modifiers down
	    if (keyCode == KeyEvent.VK_LEFT) {
				// LEFT (back one character)
		setCaretPos(getCaretPos() - 1);
	    } else if (keyCode == KeyEvent.VK_RIGHT) {
				// RIGHT (forward one character)
		setCaretPos(getCaretPos() + 1);
	    } else if (keyCode == KeyEvent.VK_UP) {
				// UP (up one line)
		setCaretLine(getCaretLine() - 1);
	    } else if (keyCode == KeyEvent.VK_DOWN) {
				// DOWN (down one line)
		setCaretLine(getCaretLine() + 1);
	    } else if (keyCode == KeyEvent.VK_HOME) {
				// HOME (beginning of line)
		setCaretPos(0);
	    } else if (keyCode == KeyEvent.VK_END) {
				// END (beginning of line)
		setCaretPos(((String)lines.elementAt(caretLine)).length());
	    } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
				// BACKSPACE (delete character before caret)
		deleteCharBeforeCaret();
	    } else if (keyCode == KeyEvent.VK_DELETE) {
				// DELETE (delete character after caret)
		deleteChar();
	    } else if (keyCode == KeyEvent.VK_ENTER) {
		addEnterChar();      // ENTER (add a line to text array)
	    } else {
				// Else, add a character
		addChar(keyChar);
	    }
	}
    }

    /**
     * Determines if the text should be rendered as text or greek.
     * @param <code>renderContext</code> Contains information about current render.
     */
    public void paint(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	if (!lines.isEmpty()) {
				// If font too small and not antialiased, then greek
	    float renderedFontSize = font.getSize() * renderContext.getCompositeMagnification();
				// BBB: HACK ALERT - July 30, 1999
				// This is a workaround for a bug in Sun JDK 1.2.2 where
				// fonts that are rendered at very small magnifications show up big!
				// So, we render as greek if requested (that's normal)
				// OR if the font is very small (that's the workaround)
	    if ((renderedFontSize < 0.5f) ||
		(renderedFontSize < greekThreshold) && (renderContext.getGreekText())) {
		paintAsGreek(renderContext);
	    } else {
		paintAsText(renderContext);
	    }
	}

	prevFRC = g2.getFontRenderContext();
    }
    
    /**
     * Paints this object as greek.
     * @param <code>renderContext</code> The graphics context to paint into.
     */
    public void paintAsGreek(ZRenderContext renderContext) {
	    Graphics2D g2 = renderContext.getGraphics2D();
	    	    
	    if (greekColor != null) {
		g2.setColor(greekColor);
		Rectangle2D rect = new Rectangle2D.Float((float)localBounds.getX(), (float)localBounds.getY(),
							 (float)localBounds.getWidth(), (float)localBounds.getHeight());
		g2.fill(rect);
	    }
    }

    /**
     * Paints this object normally (show it's text).
     * Note that the entire text gets rendered so that it's upper
     * left corner appears at the origin of this local object.
     * @param <code>renderContext</code> The graphics context to paint into.
     */
    public void paintAsText(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();
	if (backgroundColor != null) {
	    g2.setColor(backgroundColor);
	    Rectangle2D rect = new Rectangle2D.Float((float)localBounds.getX(), (float)localBounds.getY(),
						     (float)localBounds.getWidth(), (float)localBounds.getHeight());
	    g2.fill(rect);
	}

				// Get current font metrics information for multi-line and caret layout
	FontRenderContext frc = g2.getFontRenderContext();

				// Render each line of text
				// Note that the entire text gets rendered so that it's upper left corner
				// appears at the origin of this local object.
	g2.setColor(penColor);
	g2.setFont(font);
						 
	int lineNum = 0;
	String line;
	LineMetrics lm;
	GlyphVector gv;
	GlyphMetrics gm;
	float x, y;
	long startTime=0, endTime=0;
	int gLength;

	String character;
	Rectangle2D charBounds, lineSoFarBounds;

	for (Iterator i = lines.iterator() ; i.hasNext() ; ) {
	    line = (String)i.next();
	    lm = font.getLineMetrics(line, frc);
	    y = lm.getAscent() + (lineNum * lm.getHeight());

	    // accurateSpacing: draw a line of text one character at a time,
	    // accurately positioned via GlyphMetric.getAdvance()
	    if (renderContext.getAccurateSpacing()) {
		x = 0.0f;
		gv = font.createGlyphVector(frc, line);
		gLength = gv.getNumGlyphs();

		// draw each character in the line
		for (int j=0; j<gLength; j++) {
		    g2.drawString(line.substring(j,j+1), x, y);
		    gm = gv.getGlyphMetrics(j);
		    x += gm.getAdvance();
		}
	    } else {
		g2.drawString(line, 0, y);
	    }
	    lineNum++;
 	}

				// Draw the caret
	if (editable) {
	    caretX = 0;
	    String textLine = (String)lines.elementAt(caretLine);
	    lm = font.getLineMetrics(textLine, frc);
	    if (caretPos > 0) {
		if ((boundsBug) && (textLine.substring(0,caretPos).endsWith(" "))) {
		    caretX = (float)font.getStringBounds((textLine.substring(0, caretPos-1))+'t', frc).getWidth();
		} else {
		    caretX = (float)font.getStringBounds(textLine, 0, caretPos, frc).getWidth();
		}
	    }
	    caretY = lm.getAscent() + (caretLine * lm.getHeight());

	    g2.setColor(caretColor);
	    g2.setStroke(new BasicStroke(2.0f / renderContext.getCompositeMagnification(),
					 BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
	    caretShape.setLine(caretX, caretY, caretX, (caretY - lm.getAscent()));
	    g2.draw(caretShape);
	}

    }

    /**
     * Notifies this object that it has changed and that it
     * should update its notion of its bounding box.
     */
    protected void computeLocalBounds() {
 	Rectangle2D bounds = null;
 	Rectangle2D bugBounds = null;
	float lineWidth;
	float maxWidth = 0.0f;
	float maxHeight = 0.0f;
	float height;
	FontRenderContext frc = LOW_QUALITY_FONT_CONTEXT;

				// We want to find the greatest bounds of the text in
				// low and high quality, so we get the max width and height
				// of a line checking both quality levels.
	for (int loop=0; loop<2; loop++) {
	    height = 0.0f;
				// First, check width
	    if (!lines.isEmpty()) {
		String line;
		LineMetrics lm;
		int lineNum = 0;
		for (Iterator i = lines.iterator() ; i.hasNext() ; ) {
		    line = (String)i.next();
		    lm = font.getLineMetrics(line, frc);
		    
				// Find the longest line in the text
		    bounds = font.getStringBounds(line, frc);
		    lineWidth = (float)bounds.getWidth();

		    if ((boundsBug) && (line.endsWith(" ")))
			lineWidth = (float)font.getStringBounds((line.substring(0, line.length()-1))+'t', frc).getWidth();
		    
		    if (lineWidth > maxWidth) {
			maxWidth = lineWidth;
		    }
				// Find the heighest line in the text
		    if (lineNum == 0) {
			height += lm.getAscent() + lm.getDescent();
		    } else {
			height += lm.getHeight();
		    }

		    lineNum++;
		}
	    } else {
				// If no text, then we want to have the bounds of a space character, 
				// so get those bounds here
		if (boundsBug)
		    bounds = font.getStringBounds("t", frc);
		else
		    bounds = font.getStringBounds(" ", frc);
		maxWidth = (float)bounds.getWidth();
		height = (float)bounds.getHeight();
	    }

	    if (maxHeight < height) {
		maxHeight = height;
	    }

	    frc = HIGH_QUALITY_FONT_CONTEXT;
	}

				// BBB: The following is a terrible hack to avoid a fairly
				// serious conceptual problem.  We calculate static bounds in global based
				// on the coords font size.  However, Java actually renders fonts differently
				// depending on the current graphics transform.  So, our bounds are wrong
				// when you zoom.  For now, we just make our static 10% bigger to accomodate
				// text that is rendered bigger than our calculated bounds.

	//if (accurateSpacing)
	//maxWidth *= 1.1f;

				// Finally, set the bounds of this text
	localBounds.setRect(0, 0, maxWidth, maxHeight);
    }

    static public void main(String[] args) {
	ZBasicFrame app = new ZBasicFrame(true);
	ZSurface surface = app.getSurface();
	ZText text = new ZText();
	text.setText("Now is the time for all good men to come to the aid of their party\nNow is the time for all good men to come to the aid of their party\nNow is the time for all good men to come to the aid of their party\nNow is the time for all good men to come to the aid of their party\nNow is the time for all good men to come to the aid of their party\n");
	ZNode node = new ZNode(text);
	node.getTransform().translate(10, 10);
	app.getLayer().addChild(node);

	int quality = ZSurface.RENDER_QUALITY_MEDIUM;
	if (args.length > 0) {
	    if (args[0].equals("low")) {
		quality = ZSurface.RENDER_QUALITY_LOW;
	    } else if (args[0].equals("medium")) {
		quality = ZSurface.RENDER_QUALITY_MEDIUM;
	    } else if (args[0].equals("high")) {
		quality = ZSurface.RENDER_QUALITY_HIGH;
	    }
	}

	surface.setRenderQuality(quality);
 
	int fSize = 20;
	Font font = new Font("Helvetica", Font.PLAIN, fSize);
	node.getTransform().scale(1.0f / fSize);
	text.setFont(font);
	text.setGreekThreshold(0.0f);
	long accTime = 0;
	long startTime = System.currentTimeMillis();
	int reps = 300;
 
	for (int i=0; i<reps; i++) {
	    node.getTransform().scale(1.01f);
	    surface.restore(true);
	}
 
	long endTime = System.currentTimeMillis();
	accTime = endTime - startTime;
 
	System.out.println("time (" + quality + "): "  + (accTime / reps) + " ms/render");
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Set some state of this object as it gets read back in.
     * After the object is created with its default no-arg constructor,
     * this method will be called on the object once for each bit of state
     * that was written out through calls to ZObjectOutputStream.writeState()
     * within the writeObject method.
     * @param fieldType The fully qualified type of the field
     * @param fieldName The name of the field
     * @param fieldValue The value of the field
     */
    public void setState(String fieldType, String fieldName, Object fieldValue) {
	super.setState(fieldType, fieldName, fieldValue);

	if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
	} else if (fieldName.compareTo("backgroundColor") == 0) {
	    setBackgroundColor((Color)fieldValue);
	} else if (fieldName.compareTo("caretColor") == 0) {
	    setCaretColor((Color)fieldValue);
	} else if (fieldName.compareTo("font") == 0) {
	    setFont((Font)fieldValue);
	} else if (fieldName.compareTo("editable") == 0) {
	    setEditable(((Boolean)fieldValue).booleanValue());
	} else if (fieldName.compareTo("text") == 0) {
	    setText((String)fieldValue);
	}
    }

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out); 

	if ((penColor != null) && (penColor != DEFAULT_PEN_COLOR)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}
	if ((backgroundColor != null) && (backgroundColor != DEFAULT_BACKGROUND_COLOR)) {
	    out.writeState("java.awt.Color", "backgroundColor", backgroundColor);
	}
	if ((caretColor != null) && (caretColor != DEFAULT_CARET_COLOR)) {
	    out.writeState("java.awt.Color", "caretColor", caretColor);
	}
	if (getFont() != DEFAULT_FONT) {
	    out.writeState("java.awt.Font", "font", getFont());
	}
	if (getEditable() != DEFAULT_EDITABLE) {
	    out.writeState("boolean", "editable", getEditable());
	}
	if (getText() != DEFAULT_TEXT) {
	    out.writeState("String", "text", getText());
	}
    }
}
