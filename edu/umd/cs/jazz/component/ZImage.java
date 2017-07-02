/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.net.URL;

import edu.umd.cs.jazz.scenegraph.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZImage</b> is a graphic object that represents a raster image
 *
 * @author  Benjamin B. Bederson
 */
public class ZImage extends ZVisualComponent {
    public static final boolean writeEmbeddedImage_DEFAULT = true;
    protected static final Component staticComponent = new Canvas();

    protected int width, height;		// Dimensions of image.  -1 if not known yet
    protected Image image = null;
    protected Rectangle2D rect;
    protected ZImageObserver observer;
    protected boolean writeEmbeddedImage = writeEmbeddedImage_DEFAULT;
    protected String fileName = null;
    protected URL url = null;
    
    /**
     * Constructs a new Image.
     */
    public ZImage() {
	rect = new Rectangle2D.Double();
        observer = new ZImageObserver(this);
    }
    
    public ZImage(Image i) {
	this();
	setImage(i);
    }

    /**
     * Constructs a new ZImage that is a duplicate of the reference zimage, i.e., a "copy constructor"
     * @param <code>zi</code> Reference zimage
     */
    public ZImage(ZImage zi) {
	super(zi);
	rect = new Rectangle2D.Double();
        observer = new ZImageObserver(this);
	setImage(zi.getImage());
    }

    public ZImage(String aFileName) {
	this();
	fileName = aFileName;
	setImage(aFileName);
    }

    public ZImage(URL aUrl) {
	this();
	url = aUrl;
	setImage(aUrl);
    }

    public ZImage(byte[] bytes) {
	this();
	setImage(bytes);
    }

    /**
     * Duplicates the current ZImage by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     * @see #ZImage(ZImage)
     */
    public Object clone() {
	return new ZImage(this);
    }

    public boolean setImage(Image i) {
	width = -1;
	height = -1;
	image = i;
	if (image == null) {
	    setDimension(0, 0);
	} else {
	    setDimension(getWidth(), getHeight());
	}
	damage(true);
	return isLoaded();
    }

    public boolean setImage(byte[] bytes) {
	Image im = Toolkit.getDefaultToolkit().createImage(bytes);
  	loadImage(im);
	return setImage(im);
    }
    
    public boolean setImage(String aFileName) {
	fileName = aFileName;
	Image im = Toolkit.getDefaultToolkit().getImage(aFileName);
	loadImage(im);
	return setImage(im);
    }
    
    public boolean setImage(URL aUrl) {
	url = aUrl;
	Image im = Toolkit.getDefaultToolkit().getImage(url);
  	loadImage(im);
	return setImage(im);
    }

    
    protected void loadImage(Image im) {
	MediaTracker tracker = new MediaTracker(staticComponent);
  	tracker.addImage(im, 0);
  	try {
  	    tracker.waitForID(0);
  	}
  	catch (InterruptedException exception) {
  	    System.out.println("Couldn't load image");
  	}
    }
    /**
     * Return the AWT image associated with this image object.
     * @return the AWT image.
     */
    public Image getImage() {
	return image;
    }

    public void setFileName(String aFileName) {
	fileName = aFileName;
    }

    public String getFileName() {
	return fileName;
    }

    public void setUrl(URL aUrl) {
	url = aUrl;
    }

    public URL getUrl() {
	return url;
    }

    /**
     * Specify if this image gets saved by writing the binary
     * image into the file, or if it instead writes the filename
     * of the image, and thus requires that the external image
     * file exists in the same place to reload.
     * @param value true to embed image in file, and false to store a link
     */
    public void setWriteEmbeddedImage(boolean value) {
	writeEmbeddedImage = value;
    }


    /**
     * Determine if this image gets saved by writing the binary
     * image into the file.  Return false if it stores the name
     * of the file that contains the image.
     * @return true to embed image in file
     */
    public boolean getWriteEmbeddedImage() {
	return writeEmbeddedImage;
    }

    /**
     * Notifies this object that it has changed and that it should update 
     * its notion of its bounding box.  Note that this should not be called
     * directly.  Instead, it is called by <code>updateBounds</code> when needed.
     *
     * @see ZNode#getGlobalBounds()
     */
    protected void computeLocalBounds() {
	rect.setRect(0, 0, getWidth(), getHeight());
	
	localBounds.reset(); 
	localBounds.add(rect);
    }

    /**
     * Paints this object.
     * @param <code>g2</code> The graphics context to paint into.
     */
    public void paint(ZRenderContext renderContext) {
	if (image != null) {
	    Graphics2D g2 = renderContext.getGraphics2D();
	    g2.drawImage(image, null, observer);
	}
    }
    
    /**
     * Return width of image.
     * If the width is not yet available, this will return -1, and the
     * observer will be notified of the dimensions later.
     * @return width.
     */
    public int getWidth() {
	if ((width == -1) && (image != null)) {
	    int w;
				// Be careful here because Image.getWidth() can immediately call
				// the observer which in this case will modify width - so don't
				// set width unless we have a valid value.
	    w = image.getWidth(observer);
	    if (w != -1) {
		width = w;
	    }
	}

	return width;
    }

    /**
     * Return height of image.
     * If the height is not yet available, this will return -1, and the
     * observer will be notified of the dimensions later.
     * @return height.
     */
    public int getHeight() {
	if ((height == -1) && (image != null)) {
	    int h;
				// Be careful here because Image.getHeight() can immediately call
				// the observer which in this case will modify height - so don't
				// set height unless we have a valid value.
	    h = image.getHeight(observer);
	    if (h != -1) {
		height = h;
	    }
	}

	return height;
    }

    /*
     * Determines if the image has been loaded yet or not.
     * @return true if loaded
     */
    public boolean isLoaded() {
	boolean loaded;

	if ((width == -1) || (height == -1)) {
	    loaded = false;
	} else {
	    loaded = true;
	}

	return loaded;
    }

    /**
     * Called when the image has been loaded.
     */
    protected void setLoaded(boolean l) {
	damage();
    }

    /**
     * Set the dimensions of the image.  This should only be used
     * by an ImageObserver when the image has been loaded, and the
     * true dimensions of the image have been found.
     * @param <code>w</code> New width of the image
     * @param <code>h</code> New height of the image
     */
    void setDimension(int w, int h) {
	width = w;
	height = h;
	updateBounds();
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	String str = super.toString();
	if (fileName != null) {
	    str += " '" + fileName + "'";
	}
	if (url != null) {
	    str += " '" + url + "'";
	}

	return str;
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

	if (image != null) {
	    if (writeEmbeddedImage) {
		out.writeState("BINARYDATAFOLLOWS", "image", image);
	    } else {
		if (fileName != null) {
		    out.writeState("String", "fileName", fileName);
		} else if (url != null) {
		    out.writeState("URL", "url", url);
		}
	    }
	}
	if (writeEmbeddedImage != writeEmbeddedImage_DEFAULT) {
	    out.writeState("boolean", "writeEmbeddedImage", writeEmbeddedImage);
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

	if (fieldName.compareTo("image") == 0) {
	    byte[] data = (byte[])fieldValue;
	    setImage(data);
	} else if (fieldName.compareTo("fileName") == 0) {
	    setImage((String)fieldValue);
	} else if (fieldName.compareTo("url") == 0) {
	    setImage((URL)fieldValue);
	} else if (fieldName.compareTo("writeEmbeddedImage") == 0) {
	    writeEmbeddedImage = ((Boolean)fieldValue).booleanValue();
	}
    }
}


class ZImageObserver implements ImageObserver {
    
    protected ZImage image;
    
    public ZImageObserver(ZImage i) {
	image = i;
    }
    
    public boolean imageUpdate(Image i, int infoflags, int x, int y, int width, int height) {
	if (((infoflags & ERROR) != 0) || ((infoflags & ABORT) != 0)) {
	    System.out.println("Error creating image " + image);
	} else if (((infoflags & WIDTH) != 0) || ((infoflags & HEIGHT) != 0)) {
	    image.setDimension(width, height);
	    image.setLoaded(true);
	}

	return false;
    }
}
