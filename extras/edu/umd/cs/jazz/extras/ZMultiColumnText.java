package edu.umd.cs.jazz.extras;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.font.GlyphMetrics;
import java.io.*;
import java.util.*;
import java.awt.font.TextAttribute;
import java.text.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;
import edu.umd.cs.jazz.component.*;

/**
 * ZFitText fits a block of text into a rectangular area. The text is scaled and broken into multiple
 * columns to minimize wasted space. Contributed to Jazz by Jean-Daniel and Frederic Jourdan,
 * Ecole des Mines de Nantes, France and France Telecom Research & Development.
 */
public class ZMultiColumnText extends ZVisualComponent implements ZPenColor, ZPenPaint, Serializable {

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
    static protected final double   DEFAULT_GREEK_THRESHOLD = 5.5;

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
     * Default text when new text area is created.
     */
    static protected final String  DEFAULT_TEXT = "";

    /**
     * Below this magnification text is rendered as greek.
     */
    protected double             greekThreshold = DEFAULT_GREEK_THRESHOLD;

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
     * Current text font.
     */
    protected Font               font = DEFAULT_FONT;

    /**
     * Each vector element is one line of text.
     */
    protected ArrayList            lines = new ArrayList();


    /**
     * The number of columns to break the text lines into. A value of -1
     * (the default behavior) breaks the text into the optimal number of
     * columns wasting the least amount of space.
     */
    protected int               numColumns = -1;

    /**
     * The previously used font render context (i.e., from the last render).
     */
    protected transient FontRenderContext prevFRC = null;

    /**
     * jdk version <= 1.2.1 has a bug: font.getStringBounds() gives the
     * bounds of a space " " as zero.
     */
    protected boolean boundsBug = false;

    /**
     * Natural dimensions of the text bounding box
     */
    transient protected ZDimension naturalDimension;
    transient protected double avgCharSize;

    transient protected Rectangle2D rectangle;

    public ZMultiColumnText() {
        this("", DEFAULT_FONT, new Rectangle2D.Double());
    }

    public ZMultiColumnText(String str, Rectangle2D r) {
        this(str, DEFAULT_FONT, r);
    }

    public ZMultiColumnText(String str, Font font, Rectangle2D r) {
        if ((System.getProperty("java.version").equals("1.2")) ||
            (System.getProperty("java.version").equals("1.2.1"))) {
            boundsBug = true;
        }
        setRect((Rectangle2D)r.clone());

        setText(str);
        this.font = font;

        reshape();
    }

    protected Object duplicateObject() {
        ZMultiColumnText newMultiColumnText = (ZMultiColumnText)super.duplicateObject();
        newMultiColumnText.lines = (ArrayList)lines.clone();
        return newMultiColumnText;
    }
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
        repaint();
    }

    /**
     * Returns the current pen paint.
     */
    public Paint getPenPaint() {
        return penColor;
    }

    /**
     * Sets the current pen paint.
     * @param <code>aPaint</code> use this paint.
     */
    public void setPenPaint(Paint aPaint) {
        penColor = (Color)aPaint;
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
        repaint();
    }

    /**
     * Returns the current greek threshold. Below this magnification
     * text is rendered as 'greek'.
     */
    public double getGreekThreshold() {return greekThreshold;}

    /**
     * Sets the current greek threshold. Below this magnification
     * text is rendered as 'greek'.
     * @param <code>threshold</code> compared to renderContext magnification.
     */
    public void setGreekThreshold(double threshold) {
        greekThreshold = threshold;
        repaint();
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
        reshape();
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
        lines = new ArrayList();
        do {
            index = str.indexOf('\n', pos);
            if (index == -1) {
                lines.add(str);
                done = true;
            } else {
                lines.add(str.substring(0, index));
                str = str.substring(index + 1);
            }
        } while (!done);

        reshape();
    }

    void computeSize(ZRenderContext renderContext) {
        if (naturalDimension != null || lines.isEmpty())
            return;

        Graphics2D g2 = renderContext.getGraphics2D();
        FontRenderContext frc = g2.getFontRenderContext();
        double max_width = 0;
        int max_chars = 0;
        double max_height = 0;

        for (Iterator i = lines.iterator() ; i.hasNext() ; ) {
            String line = (String)i.next();
            Rectangle2D rect = font.getStringBounds(line, frc);
            if (max_width < rect.getWidth()) {
                max_width = rect.getWidth();
                max_chars = line.length();
            }
            max_height = Math.max(rect.getHeight(), max_height);
        }
        //System.out.println("max_width: "+max_width);
        //System.out.println("max_height: "+max_height);
        naturalDimension = new ZDimension(max_width, max_height);
        avgCharSize = max_width / max_chars;
    }

    /**
     * Returns the number of columns to break the text lines into. A value of -1
     * (the default behavior) breaks the text into the optimal number of
     * columns wasting the least amount of space.
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Sets the number of columns to break the text lines into. A value of -1
     * (the default behavior) breaks the text into the optimal number of
     * columns wasting the least amount of space.
     */
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        repaint();
    }

    /**
     * Renders the text object
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param <code>renderContext</code> Contains information about current render.
     */
    public void render(ZRenderContext renderContext) {
        Graphics2D g2 = renderContext.getGraphics2D();
        AffineTransform at = null;
        boolean translated = false;
        if (!lines.isEmpty()) {
            computeSize(renderContext);
            double width = bounds.getWidth();
            double height = bounds.getHeight();
            //System.out.println("width: "+width);
            //System.out.println("height: "+height);

            double lines_heigth = (naturalDimension.getHeight() * lines.size());
            double l =  naturalDimension.getWidth() * height / lines_heigth;
            //System.out.println("l: "+l);
            double columns = (1 + Math.sqrt(1+4*(width / l))) / 2;
            int cols = (int)Math.floor(columns);

            // Manually overide the number of columns if numColumns is
            // is not set to -1.
            if (numColumns != -1) {
                cols = numColumns;
            }

            double zoom = Math.min(width / (cols*naturalDimension.getWidth()),
                                   height / (lines_heigth / cols));
            //System.out.println("Columns: "+columns);
            //System.out.println("Zoom: "+zoom);

            double renderedFontSize = font.getSize() * renderContext.getCompositeMagnification() * zoom;
                                // BBB: HACK ALERT - July 30, 1999
                                // This is a workaround for a bug in Sun JDK 1.2.2 where
                                // fonts that are rendered at very small magnifications show up big!
                                // So, we render as greek if requested (that's normal)
                                // OR if the font is very small (that's the workaround)
            boolean isGreek = false;
            if ((renderedFontSize < 0.5) ||
                (renderedFontSize < greekThreshold) && (renderContext.getGreekText())) {
                if (greekColor != null) {
                    g2.setColor(greekColor);
                    g2.fill(rectangle);
                }
                isGreek = true;
            } else {
                if (backgroundColor != null) {
                    g2.setColor(backgroundColor);
                    g2.fill(rectangle);
                }
            }
            AffineTransform saveTransform = g2.getTransform();
            g2.translate(rectangle.getX(), rectangle.getY());
            g2.scale(zoom, zoom);
            double max_height = naturalDimension.getHeight() *
                lines.size() / cols;
            FontRenderContext frc = g2.getFontRenderContext();

                                // Render each line of text
                                // Note that the entire text gets rendered so that it's upper left corner
                                // appears at the origin of this local object.
            g2.setColor(penColor);
            g2.setFont(font);

            int lineNum = 0;
            String line;
            LineMetrics lm;
            double x, y;

            Rectangle2D charBounds = new Rectangle2D.Double();

            for (Iterator i = lines.iterator() ; i.hasNext() ; ) {
                line = (String)i.next();
                if (isGreek)
                    y = naturalDimension.getHeight() * lineNum;
                else {
                    lm = font.getLineMetrics(line, frc);
                    y = lm.getAscent() + (lineNum * lm.getHeight());
                }
                if (y >= max_height) {
                    y = 0;
                    lineNum = 0;
                    g2.translate(width / (zoom * cols) , 0);
                }
                if (isGreek) {
                    charBounds.setRect(0, y,
                                       avgCharSize * line.length(),
                                       naturalDimension.getHeight());
                    g2.fill(charBounds);
                }
                else {
                    g2.drawString(line, 0, (float)y);
                }

                lineNum++;
            }
            g2.setTransform(saveTransform);
        }

        prevFRC = g2.getFontRenderContext();
    }

    public Collection getHandles() {
        Collection result = new ArrayList(8);

        // North
        result.add(new ZHandle(ZBoundsLocator.createNorthLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                if (rectangle.getHeight() - dy < 0) {
                    dy = dy - Math.abs(rectangle.getHeight() - dy);
                }
                setRect(rectangle.getX(),
                        rectangle.getY() + dy,
                        rectangle.getWidth(),
                        rectangle.getHeight() - dy);

                super.handleDragged(dx, dy, e);
            }
        });

        // East
        result.add(new ZHandle(ZBoundsLocator.createEastLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                setRect(rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getWidth() + dx,
                        rectangle.getHeight());

                super.handleDragged(dx, dy, e);
            }
        });

        // West
        result.add(new ZHandle(ZBoundsLocator.createWestLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                if (rectangle.getWidth() - dx < 0) {
                    dx = dx - Math.abs(rectangle.getWidth() - dx);
                }
                setRect(rectangle.getX() + dx,
                        rectangle.getY(),
                        rectangle.getWidth() - dx,
                        rectangle.getHeight());

                super.handleDragged(dx, dy, e);
            }
        });

        // South
        result.add(new ZHandle(ZBoundsLocator.createSouthLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                setRect(rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getWidth(),
                        rectangle.getHeight() + dy);

                super.handleDragged(dx, dy, e);
            }
        });

        // North West
        result.add(new ZHandle(ZBoundsLocator.createNorthWestLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                if (rectangle.getWidth() - dx < 0) {
                    dx = dx - Math.abs(rectangle.getWidth() - dx);
                }
                if (rectangle.getHeight() - dy < 0) {
                    dy = dy - Math.abs(rectangle.getHeight() - dy);
                }
                setRect(rectangle.getX() + dx,
                        rectangle.getY() + dy,
                        rectangle.getWidth() - dx,
                        rectangle.getHeight() - dy);

                super.handleDragged(dx, dy, e);
            }
        });

        // South West
        result.add(new ZHandle(ZBoundsLocator.createSouthWestLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                if (rectangle.getWidth() - dx < 0) {
                    dx = dx - Math.abs(rectangle.getWidth() - dx);
                }
                setRect(rectangle.getX() + dx,
                        rectangle.getY(),
                        rectangle.getWidth() - dx,
                        rectangle.getHeight() + dy);

                super.handleDragged(dx, dy, e);
            }
        });

        // North East
        result.add(new ZHandle(ZBoundsLocator.createNorthEastLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                if (rectangle.getHeight() - dy < 0) {
                    dy = dy - Math.abs(rectangle.getHeight() - dy);
                }
                setRect(rectangle.getX(),
                        rectangle.getY() + dy,
                        rectangle.getWidth() + dx,
                        rectangle.getHeight() - dy);

                super.handleDragged(dx, dy, e);
            }
        });

        // South East
        result.add(new ZHandle(ZBoundsLocator.createSouthEastLocator(this)) {
            public void handleDragged(double dx, double dy, ZMouseEvent e) {
                setRect(rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getWidth() + dx,
                        rectangle.getHeight() + dy);

                super.handleDragged(dx, dy, e);
            }
        });

        return result;
    }

    /**
     * Return the rectangle.
     * @return rectangle.
     */
    public Rectangle2D getRect() {
        return rectangle;
    }

    /**
     * Sets location and size of the rectangle.
     * @param <code>x</code> X-coord of top-left corner
     * @param <code>y</code> Y-coord of top-left corner
     * @param <code>width</code> Width of rectangle
     * @param <code>height</code> Height of rectangle
     */
    public void setRect(double x, double y, double width, double height) {
        if (width < 0) {
            width = 0;
        }

        if (height < 0) {
            height = 0;
        }

        getRect().setRect(x, y, width, height);
        reshape();
    }

    /**
     * Sets coordinates of rectangle.
     * @param <code>r</code> The new rectangle coordinates
     */
    public void setRect(Rectangle2D r) {
        rectangle = r;
        reshape();
    }

    protected void computeBounds() {
        bounds.reset();
        bounds.setRect(rectangle);
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
        } else if (fieldName.compareTo("font") == 0) {
            setFont((Font)fieldValue);
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
        if (getFont() != DEFAULT_FONT) {
            out.writeState("java.awt.Font", "font", getFont());
        }
        if (getText() != DEFAULT_TEXT) {
            out.writeState("String", "text", getText());
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        prevFRC = new FontRenderContext(null, true, true);
    }
}