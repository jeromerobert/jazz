/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZShape</b> is a graphic object that represents a pre-defined java.awt.Shape
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  James Mokwa
 */
public class ZShape extends ZVisualComponent implements ZPenColor, ZFillColor, ZStroke, Serializable {
    static public final Color  penColor_DEFAULT = Color.black;
    static public final Color  fillColor_DEFAULT = Color.white;
    static public final double  penWidth_DEFAULT = 1.0;
    static public final boolean absPenWidth_DEFAULT = false;

    /**
     * Pen color for perimeter of shape
     */
    private Color     penColor  = penColor_DEFAULT;

    /**
     * Pen width of pen color.
     */
    private double     penWidth  = penWidth_DEFAULT;

    /**
     * Specifies if pen width is an absolute specification (independent of camera magnification)
     */
    private boolean    absPenWidth = absPenWidth_DEFAULT;

    /**
     * Fill color for interior of shape.
     */
    private Color     fillColor = fillColor_DEFAULT;

    /**
     * The user defined shape.
     */
    private transient Shape shape;

    /**
     * Stroke for rendering pen color
     */
    private transient Stroke stroke = new BasicStroke((float)penWidth_DEFAULT, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    /**
     * points for flattened version of current shape.
     */
    private double xp[], yp[];

    /**
     * Number of points in flattened shape.
     */
    private int np;

    //****************************************************************************
    //
    //                Constructors
    //
    //***************************************************************************

    /**
     * Constructs a new visual component based on a java.awt.Shape
     * @param aShape a pre-defined shape.
     */
    public ZShape() {
	shape = new GeneralPath();
	reshape();
    }

    /**
     * Constructs a new visual component based on a java.awt.Shape
     * @param aShape a pre-defined shape.
     */
    public ZShape(Shape shape) {
	setShape(shape);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZShape newShape = (ZShape)super.duplicateObject();
	return newShape;	
    }

    //****************************************************************************
    //
    //			Get/Set and pairs
    //
    //***************************************************************************

    /**
     * Return the current shape.
     */
    public Shape getShape() {
	return(shape);
    }

    /**
     * Set the current shape.
     * @param aShape a new shape.
     */
    public void setShape(Shape aShape) {
	shape = aShape;
	computeFlattenedShape();
	reshape();
    }


    /**
     * Recompute the points of  a flattened version of the current shape.
     */
    protected void computeFlattenedShape() {
	PathIterator pi = shape.getPathIterator(null);
	FlatteningPathIterator fpi = new FlatteningPathIterator(pi, 1);
	double points[] = new double[6];

	for (np = 0; !fpi.isDone(); fpi.next()) {
	    np++;
	}

	if (np == 0) {
	    return;
	}

	xp = new double[np];
	yp = new double[np];
	double moveToX = 0;
	double moveToY = 0;
	int i = 0;
	pi = shape.getPathIterator(null);
	fpi = new FlatteningPathIterator(pi, 1);
	while (fpi.isDone() == false) {
	    int type = fpi.currentSegment(points);
	    
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		moveToX = points[0];
		moveToY = points[1];
		xp[i] = moveToX;
		yp[i] = moveToY;
		break;

	    case PathIterator.SEG_LINETO:
		xp[i] = points[0];
		yp[i] = points[1];
		break;

	    case PathIterator.SEG_CLOSE:
		xp[i] = moveToX;
		yp[i] = moveToY;
		break;
	    }
	    i++;
	    fpi.next();
	}
    }

    /**
     * Get the width of the pen used to draw the perimeter of this shape.
     * If the pen width is absolute
     * (independent of magnification), then this returns 0.
     * @return the pen width.
     */
    public double getPenWidth() {
	return penWidth;
    }

    /**
     * Set the width of the pen used to draw the perimeter of this shape.
     * @param width the pen width.
     */
    public void setPenWidth(double width) {
	penWidth = width;
	absPenWidth = false;
	setVolatileBounds(false);
	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	reshape();
    }

    /**
     * Set the absolute width of the pen used to draw the perimeter of this shape.
     * @param width the pen width.
     */
    public void setAbsPenWidth(double width) {
	penWidth = width;
	absPenWidth = true;
	setVolatileBounds(true);
	reshape();
    }

    /**
     * Get the absolute width of the pen used to draw the perimeter of this shape.
     * If the pen width is not absolute
     * (dependent on magnification), then this returns 0.
     * @return the pen width.
     * @see #getPenWidth
     */
    public double getAbsPenWidth() {
	if (absPenWidth) {
	    return penWidth;
	} else {
	    return 0.0d;
	}
    }

    /**
     * Get the stroke used to draw the visual component.
     * @return the stroke.
     */
    public Stroke getStroke() {
	return stroke;
    }

    /**
     * Set the stroke used to draw the visual component.
     * @param stroke the stroke.
     */
    public void setStroke(Stroke stroke) {
	this.stroke = stroke;
	reshape();
    }

    /**
     * Get the pen color of this shape.
     * @return the pen color.
     */
    public Color getPenColor() {
	return penColor;
    }

    /**
     * Set the pen color of this shape.
     * @param color the pen color, or null if none.
     */
    public void setPenColor(Color color) {
	boolean boundsChanged = false;

				// If turned pen color on or off, then need to recompute bounds
	if (((penColor == null) && (color != null)) ||
	    ((penColor != null) && (color == null))) {
	    boundsChanged = true;
	}
	penColor = color;

	if (boundsChanged) {
	    reshape();
	} else {
	    repaint();
	}
    }

    /**
     * Get the fill color of this shape.
     * @return the fill color.
     */
    public Color getFillColor() {
	return fillColor;
    }

    /**
     * Set the fill color of this shape.
     * @param color the fill color, or null if none.
     */
    public void setFillColor(Color color) {
	fillColor = color;

	repaint();
    }

    /**
     * Returns true if the specified rectangle is on the polygon.
     * @param rect Pick rectangle of object coordinates.
     * @param path The path through the scenegraph to the picked node. Modified by this call.
     * @return True if rectangle overlaps object.
     * @see ZDrawingSurface#pick(int, int)
     */
    public boolean pick(Rectangle2D rect, ZSceneGraphPath path) {
	boolean picked = false;

	if (pickBounds(rect)) {
	    if (fillColor != null) {
		picked = ZUtil.intersectsPolygon(rect, xp, yp);
	    }
	    if (!picked && (penColor != null)) {
		double p;
		if (penColor == null) {
		    p = 0.0;
		} else {
		    if (absPenWidth) {
			ZRenderContext rc = getRoot().getCurrentRenderContext();
			double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
			p = penWidth / mag;
		    } else {
			p = penWidth;
		    }
		}
		picked = ZUtil.rectIntersectsPolyline(rect, xp, yp, p);
	    }
	}

	return picked;
    }

    /**
     * Paints this object.
     * <p>
     * The transform, clip, and composite will be set appropriately when this object
     * is rendered.  It is up to this object to restore the transform, clip, and composite of
     * the Graphics2D if this node changes any of them. However, the color, font, and stroke are
     * unspecified by Jazz.  This object should set those things if they are used, but
     * they do not need to be restored.
     *
     * @param <code>renderContext</code> The graphics context to paint into.
     */
    public void render(ZRenderContext renderContext) {
	Graphics2D g2 = renderContext.getGraphics2D();

	if (fillColor != null) {
	    g2.setColor(fillColor);
	    g2.fill(shape);
	}
	if (penColor != null) {
	    if (absPenWidth) {
		double pw = penWidth / renderContext.getCompositeMagnification();
		stroke = new BasicStroke((float)pw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	    }
	    g2.setStroke(stroke);
	    g2.setColor(penColor);
	    g2.draw(shape);
	}
    }

    /**
     * Notifies this object that it has changed and that it should update
     * its notion of its bounding box.  Note that this should not be called
     * directly.  Instead, it is called by <code>updateBounds</code> when needed.
     */
    protected void computeBounds() {
	double xmin, ymin, xmax, ymax;

	bounds.reset();
	if (np == 0) {
	    return;
	}

	xmin = xp[0];
	ymin = yp[0];
	xmax = xp[0];
	ymax = yp[0];
	for (int i=1; i<np; i++) {
	    if (xp[i] < xmin) xmin = xp[i];
	    if (yp[i] < ymin) ymin = yp[i];
	    if (xp[i] > xmax) xmax = xp[i];
	    if (yp[i] > ymax) ymax = yp[i];
	}

				// Expand the bounds to accomodate the pen width
	double p, p2;
	if (penColor == null) {
	    p = 0.0;
	} else {
	    if (absPenWidth) {
		ZRenderContext rc = getRoot().getCurrentRenderContext();
		double mag = (rc == null) ? 1.0f : rc.getCameraMagnification();
		p = penWidth / mag;
	    } else {
		p = penWidth;
	    }
	}
	p2 = 0.5 * p;
	
	xmin -= p2;
	ymin -= p2;
	xmax += p2;
	ymax += p2;

	bounds.setRect(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Write out all of this object's state.
     * @param out The stream that this object writes into
     */
    public void writeObject(ZObjectOutputStream out) throws IOException {
	super.writeObject(out);

	if ((penColor != null) && (penColor != penColor_DEFAULT)) {
	    out.writeState("java.awt.Color", "penColor", penColor);
	}
	if ((fillColor != null) && (fillColor != fillColor_DEFAULT)) {
	    out.writeState("java.awt.Color", "fillColor", fillColor);
	}
	if (absPenWidth != absPenWidth_DEFAULT) {
	    out.writeState("boolean", "absPenWidth", absPenWidth);
	}
	if (getPenWidth() != penWidth_DEFAULT) {
	    out.writeState("double", "penWidth", getPenWidth());
	}

	PathIterator pi = shape.getPathIterator(null);

				// write out winding rule
	out.writeState("int", "windingRule", pi.getWindingRule());

	int type;
	Vector coords;
	double[] coordinates = new double[6];
	while (pi.isDone() == false) {
	    type = pi.currentSegment(coordinates);

				// write out segment coordinates
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		coords = new Vector();
		coords.add(new Double(coordinates[0]));
		coords.add(new Double(coordinates[1]));
		out.writeState("shape", "moveTo", coords);
		break;

	    case PathIterator.SEG_LINETO:
		coords = new Vector();
		coords.add(new Double(coordinates[0]));
		coords.add(new Double(coordinates[1]));
		out.writeState("shape", "lineTo", coords);
		break;
	    case PathIterator.SEG_QUADTO:
		coords = new Vector();
		coords.add(new Double(coordinates[0]));
		coords.add(new Double(coordinates[1]));
		coords.add(new Double(coordinates[2]));
		coords.add(new Double(coordinates[3]));
		out.writeState("shape", "quadTo", coords);
		break;
	    case PathIterator.SEG_CUBICTO:
		coords = new Vector();
		coords.add(new Double(coordinates[0]));
		coords.add(new Double(coordinates[1]));
		coords.add(new Double(coordinates[2]));
		coords.add(new Double(coordinates[3]));
		coords.add(new Double(coordinates[4]));
		coords.add(new Double(coordinates[5]));
		out.writeState("shape", "curveTo", coords);
		break;
	    case PathIterator.SEG_CLOSE:
		out.writeState("shape", "close", 0);
		break;
	    default:
		break;
	    }
	    pi.next();
	}
    }

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

	GeneralPath path = new GeneralPath(shape);
	double c1, c2, c3, c4, c5, c6;
	if (fieldName.compareTo("penColor") == 0) {
	    setPenColor((Color)fieldValue);
	} else if (fieldName.compareTo("fillColor") == 0) {
	    setFillColor((Color)fieldValue);
	} else if (fieldName.compareTo("penWidth") == 0) {
	    if (absPenWidth) {
		setAbsPenWidth(((Double)fieldValue).doubleValue());
	    } else {
		setPenWidth(((Double)fieldValue).doubleValue());
	    }
	} else if (fieldName.compareTo("windingRule") == 0) {
	    path.setWindingRule(((Integer)fieldValue).intValue());
	} else if (fieldName.compareTo("moveTo") == 0) {
	    Vector dim = (Vector)fieldValue;
	    c1 = ((Double)dim.get(0)).doubleValue();
	    c2 = ((Double)dim.get(1)).doubleValue();
	    path.moveTo((float)c1, (float)c2);
	} else if (fieldName.compareTo("lineTo") == 0) {
	    Vector dim = (Vector)fieldValue;
	    c1 = ((Double)dim.get(0)).doubleValue();
	    c2 = ((Double)dim.get(1)).doubleValue();
	    path.lineTo((float)c1, (float)c2);
	} else if (fieldName.compareTo("quadTo") == 0) {
	    Vector dim = (Vector)fieldValue;
	    c1 = ((Double)dim.get(0)).doubleValue();
	    c2 = ((Double)dim.get(1)).doubleValue();
	    c3 = ((Double)dim.get(2)).doubleValue();
	    c4 = ((Double)dim.get(3)).doubleValue();
	    path.quadTo((float)c1, (float)c2, (float)c3, (float)c4);
	} else if (fieldName.compareTo("curveTo") == 0) {
	    Vector dim = (Vector)fieldValue;
	    c1 = ((Double)dim.get(0)).doubleValue();
	    c2 = ((Double)dim.get(1)).doubleValue();
	    c3 = ((Double)dim.get(2)).doubleValue();
	    c4 = ((Double)dim.get(3)).doubleValue();
	    c5 = ((Double)dim.get(4)).doubleValue();
	    c6 = ((Double)dim.get(5)).doubleValue();
	    path.curveTo((float)c1, (float)c2, (float)c3, (float)c4, (float)c5, (float)c6);
	} else if (fieldName.compareTo("close") == 0) {
	    path.closePath();
	}
	shape = path;
	computeFlattenedShape();
	reshape();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
				// write out winding rule
	PathIterator pi = shape.getPathIterator(null);
	out.writeInt(pi.getWindingRule());

				// write out number of segments
	int segCount = 0;
	while (pi.isDone() == false) {
	    segCount++;
	    pi.next();
	}
	out.writeInt(segCount);

	int type;
	double[] coordinates = new double[6];
	pi = shape.getPathIterator(null);
	while (pi.isDone() == false) {
	    type = pi.currentSegment(coordinates);
				// write out segment type
	    out.writeInt(type);

				// write out segment coordinates
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		out.writeDouble(coordinates[0]);
		out.writeDouble(coordinates[1]);
		break;

	    case PathIterator.SEG_LINETO:
		out.writeDouble(coordinates[0]);
		out.writeDouble(coordinates[1]);
		break;
	    case PathIterator.SEG_QUADTO:
		out.writeDouble(coordinates[0]);
		out.writeDouble(coordinates[1]);
		out.writeDouble(coordinates[2]);
		out.writeDouble(coordinates[3]);
		break;
	    case PathIterator.SEG_CUBICTO:
		out.writeDouble(coordinates[0]);
		out.writeDouble(coordinates[1]);
		out.writeDouble(coordinates[2]);
		out.writeDouble(coordinates[3]);
		out.writeDouble(coordinates[4]);
		out.writeDouble(coordinates[5]);
		break;
	    case PathIterator.SEG_CLOSE:
		break;
	    default:
		break;
	    }
	    pi.next();
	}

				// write Stroke
	int cap = (int)((BasicStroke)stroke).getEndCap();
 	out.writeInt(cap);

	int join = (int)((BasicStroke)stroke).getLineJoin();
	out.writeInt(join);
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

				// read winding rule
	int wind = in.readInt();
	GeneralPath path = new GeneralPath(wind);
				// read number of segments
	int segCount = in.readInt();

	int type;
	double c1, c2, c3, c4, c5, c6;
	for (int i=0; i<segCount; i++) {
				// read segment type
	    type = in.readInt();
				// read segment coordinates
	    switch(type) {
	    case PathIterator.SEG_MOVETO:
		c1 = in.readDouble();
		c2 = in.readDouble();
		path.moveTo((float)c1, (float)c2);
		break;

	    case PathIterator.SEG_LINETO:
		c1 = in.readDouble();
		c2 = in.readDouble();
		path.lineTo((float)c1, (float)c2);
		break;
	    case PathIterator.SEG_QUADTO:
		c1 = in.readDouble();
		c2 = in.readDouble();
		c3 = in.readDouble();
		c4 = in.readDouble();
		path.quadTo((float)c1, (float)c2, (float)c3, (float)c4);
		break;
	    case PathIterator.SEG_CUBICTO:
		c1 = in.readDouble();
		c2 = in.readDouble();
		c3 = in.readDouble();
		c4 = in.readDouble();
		c5 = in.readDouble();
		c6 = in.readDouble();
		path.curveTo((float)c1, (float)c2, (float)c3, (float)c4, (float)c5, (float)c6);
		break;
	    case PathIterator.SEG_CLOSE:
		path.closePath();
		break;
	    default:
		break;
	    }
	}
				// read Stroke
	int cap, join;
	cap = in.readInt();
	join = in.readInt();

	stroke = new BasicStroke((float)penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	shape = path;
	computeFlattenedShape();
    }

}
