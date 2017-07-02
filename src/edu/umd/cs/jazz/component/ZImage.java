/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;

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
	writeEmbeddedImage = zi.writeEmbeddedImage;
	fileName = new String(zi.fileName);
	try {
	    url = new URL(zi.url, null);
	} catch (MalformedURLException e) {
	    url = null;
	}
	setImage(zi.getImage());
    }

    public ZImage(String aFileName) {
	this();
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

    /**
     * Given a directory path plus some path relative to it,
     * return a direct absolute path.
     * eg "/a/b/c/" + "../../e/f/g.ext" yields: /a/e/f/g.ext
     * @param<code>path</code> the direcory path to be simplified.
     */
    public String getAbsolutePath(String path) {
	char ps = File.separatorChar;
	// absolute path: "/.." is ileagal
	if ((path.length() > 2) && path.substring(0,3).equals(ps+"..")) {
	    System.out.println("illegal path: "+path);
	    return null;
	}

	// remove substrings of "someDirectory/.." from the path
	for (int c=0; c<path.length(); c++) {
	    if ((path.length() > c+5) && (path.substring(c, c+3).equals(".."+ps))) {
		// get position of previous separator
		int pps = path.lastIndexOf(ps,c-2);
		path = getAbsolutePath(path.substring(0, pps+1)+path.substring(c+3));
		return path;
	    }
	}
	return path;
    }

    /**
     * Returns the directory path of a file name, relative to 
     * another absolute path basePath.
     * @param <code>fileName</code> absolute path of a file.
     * @param <code>baseName</code> absolute base path.
     */
    public String getRelativePath(String fileName, String basePath) {
	// Make a relative path: count how many subdirectories are
	// the same between aFileName and basePath
	// relativeFileName = this many "../.." or "..\.."
	// Then tack on the tail part of aFileName.

	boolean ignoreCase = File.separator.equals("/") ? false : true;

	char sc = File.separatorChar;
	while ((fileName.indexOf(sc) != fileName.lastIndexOf(sc)) &&
	       (basePath.indexOf(sc) != basePath.lastIndexOf(sc))) {
	    int subDir = basePath.indexOf(File.separator, 1);
	    if (basePath.regionMatches(ignoreCase, 0, fileName, 0, subDir)) {
		basePath = basePath.substring(subDir);
		fileName = fileName.substring(subDir);
	    } else {
		break;
	    }
	}
	int sepCnt = 0;
	String upDirs1 = "";
	for (int i=0; i<basePath.length(); i++) {
	    if (basePath.charAt(i) == sc) {
		sepCnt++;
		if (sepCnt == 1) {
		    continue;
		}
		upDirs1 += ".."+File.separator;
	    }
	}
	// remove the first character of the filename if it is File.separator
	if ((fileName.length() > 1) && (fileName.charAt(0) == sc)) {
	    fileName = fileName.substring(1);
	}
	// append the ../ 's to the filename
	return(upDirs1 + fileName);
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

    /**
     * loads an image
     * @param <code>aFileName</code> the file name of the image.
     */
    public boolean setImage(String aFileName) {
        fileName = aFileName;
	Image im = Toolkit.getDefaultToolkit().getImage(fileName);
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
  	    System.out.println("Couldn't load image: " + fileName);
  	}
    }

    /**
     * Return the AWT image associated with this image object.
     * @return the AWT image.
     */
    public Image getImage() {
	return image;
    }

    /**
     * Sets the filename of the image.  This does not change the image,
     * but rather just stores the filename for future access.
     * @param <code>aFileName</code> the file name.
     */
    public void setFileName(String aFileName) {
      fileName = aFileName;
    }

    /**
     * Return the filename associated with this image object.
     * @return the filename.
     */
    public String getFileName() {
	return fileName;
    }

    /**
     * Sets the URL of the image.  This does not change the image,
     * but rather just stores the URL for future access.
     * @param <code>aURL</code> the file name.
     */
    public void setUrl(URL aUrl) {
	url = aUrl;
    }

    /**
     * Return the URL associated with this image object.
     * @return the URL.
     */
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

	if (fileName != null) {
				// Image filenames should be relative to the directory
				// the .jazz file is being saved in. That should be
				// the current value of the user.dir property.
	    String cwd = System.getProperty("user.dir") + File.separator;
	    String relFileName = getRelativePath(fileName, cwd);
	    out.writeState("String", "fileName", relFileName);
	} else if (url != null) {
	    out.writeState("URL", "url", url);
	}
	if (writeEmbeddedImage != writeEmbeddedImage_DEFAULT) {
	    out.writeState("boolean", "writeEmbeddedImage", writeEmbeddedImage);
	}
	if ((image != null) && writeEmbeddedImage) {
	    out.writeState("BINARYDATAFOLLOWS", "image", image);
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
	  String fn = (String)fieldValue;

	  // Convert name-separator character to the one appropriate
	  // for current system.
	  if (File.separator.equals("/")) {
	    fn = fn.replace('\\','/');
	  } else {
	    fn = fn.replace('/','\\');
	  }

		  // Image filenames are saved as relative to the directory
		  // the .jazz file is in. That directory should be
		  // the current value of the user.dir property.
	  String cwd = System.getProperty("user.dir");
	  fileName = getAbsolutePath(cwd+File.separator+fn);
	  setImage(fileName);
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
	if (((infoflags & WIDTH) != 0) || ((infoflags & HEIGHT) != 0)) {
	    image.setDimension(width, height);
	    image.setLoaded(true);
	}

	return false;
    }
}
