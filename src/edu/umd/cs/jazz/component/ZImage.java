/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.component;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import com.sun.image.codec.jpeg.*;
import com.sun.image.codec.jpeg.JPEGCodec.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import edu.umd.cs.jazz.util.*;

/**
 * <b>ZImage</b> is a graphic object that represents a raster image.
 *
 * <P>
 * <b>Warning:</b> Serialized and ZSerialized objects of this class will not be
 * compatible with future Jazz releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Jazz. A future release of Jazz will provide support for long
 * term persistence.
 *
 * @author  Benjamin B. Bederson
 */
public class ZImage extends ZVisualComponent implements Serializable {
    public static final boolean writeEmbeddedImage_DEFAULT = true;
    protected static final Component staticComponent = new Canvas();

    /**
     * The dimensions of the image.  -1 if not known yet.
     */
    protected int width, height;
    protected transient Image image = null;
    protected ZImageObserver observer;

    /**
     * Embed image in save file, or just it's filename.
     */
    protected boolean writeEmbeddedImage = writeEmbeddedImage_DEFAULT;

    /**
     * FileName of the image, if it came from a file.
     */
    protected transient String fileName = null;

    /**
     * URL of the image, if it came from a URL.
     */
    protected URL url = null;
    
    private static final Component sComponent = new Component() {};
    private static final MediaTracker sTracker = new MediaTracker(sComponent);
    private static int sID = 0;
    /**
     * Translation offset X.
     */
    protected double translateX = 0.0;

    /**
     * Translation offset Y.
     */
    protected double translateY = 0.0;

  
    class ZImageObserver implements ImageObserver, Serializable {        	

	public boolean imageUpdate(Image i, int infoflags, int x, int y, int width, int height) {
	    if (((infoflags & WIDTH) != 0) || ((infoflags & HEIGHT) != 0)) {
		ZImage.this.setDimension(width, height);
		ZImage.this.setLoaded(true);
	    }

	    return false;
	}
    }

    /**
     * Constructs a new ZImage.
     */
    public ZImage() {
        observer = new ZImageObserver();
    }
    
    /**
     * Constructs a new ZImage from an Image.
     */
    public ZImage(Image i) {
	this();
	loadImage(i);
	setImage(i);
    }

    /**
     * Constructs a new ZImage from a file.
     */
    public ZImage(String aFileName) {
	this();
	setImage(aFileName);
    }

    /**
     * Constructs a new ZImage from a URL.
     */
    public ZImage(URL aUrl) {
	this();
	url = aUrl;
	setImage(aUrl);
    }

    /**
     * Constructs a new ZImage from a byte array.
     */
    public ZImage(byte[] bytes) {
	this();
	setImage(bytes);
    }

    /**
     * Returns a clone of this object.
     *
     * @see ZSceneGraphObject#duplicateObject
     */
    protected Object duplicateObject() {
	ZImage newImage = (ZImage)super.duplicateObject();

        newImage.observer = new ZImageObserver();
	
	return newImage;
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
	// the same between fileName and basePath
	// relativeFileName = this many "../.." or "..\.."
	// Then tack on the tail part of fileName.

        // If the files are on different disks, than just write out the absolute path
        // Make an assumption here that if the second character is ':', then it is
        // PC file name, and so the first letter is a disk name.
        if ((fileName.charAt(1) == ':') && (basePath.charAt(1) == ':')) {
	    if (fileName.charAt(0) != basePath.charAt(0)) {
				// Different PC disks
		return fileName;
	    }
	}
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

    /**
     * Set image translation offset X.
     * @param x the X translation.
     */
    public void setTranslateX(double x) {
	translateX = x;
	reshape();
    }

    /**
     * Get the X offset translation.
     * @return the X translation.
     */
    public double getTranslateX() {
	return translateX;
    }

    /**
     * Set image translation offset Y.
     * @param y the Y translation.
     */
    public void setTranslateY(double y) {
	translateY = y;
	reshape();
    }

    /**
     * Get the Y offset translation.
     * @return the Y translation.
     */
    public double getTranslateY() {
	return translateY;
    }

    /**
     * Set the image translation offset to the specified position.
     * @param x the X-coord of translation
     * @param y the Y-coord of translation
     */
    public void setTranslation(double x, double y) {
	translateX = x;
	translateY = y;
	reshape();
    }

    /**
     * Set the image translation offset to point p.
     * @param p The translation offset.
     */
    public void setTranslation(Point2D p) {
	translateX = p.getX();
	translateY = p.getY();
	reshape();
    }

    /**
     * Get the image translation offset.
     * @return The translation offset.
     */
    public Point2D getTranslation() {
	Point2D p = new Point2D.Double(translateX, translateY);
	return p;
    }

    /**
     * Set the image to the one consisting of the specified image.
     * Wait until the image is loaded before returning.
     * @param <code>i</code> the image.
     */
    public boolean setImage(Image i) {
	width = -1;
	height = -1;
	image = i;
	if (image == null) {
	    setDimension(0, 0);
	} else {
	    setDimension(getWidth(), getHeight());
	}

	return isLoaded();
    }

    /**
     * Set the image to the one consisting of the specified bytes.
     * Wait until the image is loaded before returning.
     * @param <code>bytes</code> the bytes of the image.
     */
    public boolean setImage(byte[] bytes) {
	Image im = Toolkit.getDefaultToolkit().createImage(bytes);
  	loadImage(im);
	return setImage(im);
    }

    /**
     * Set the image to the one at the specified URL or filename.
     * Wait until the image is loaded before returning.
     * @param <code>aFileName</code> the URL or file name of the image.
     */
    public boolean setImage(String aFileName) {
        fileName = aFileName;
	Image im = Toolkit.getDefaultToolkit().getImage(fileName);
	loadImage(im);
	return setImage(im);
    }

    /**
     * Set the image to the one at the specified URL.
     * Wait until the image is loaded before returning.
     * @param <code>aURL</code> the URL of the image.
     */    
    public boolean setImage(URL aUrl) {
	url = aUrl;
	Image im = Toolkit.getDefaultToolkit().getImage(url);
  	loadImage(im);
	return setImage(im);
    }

    /**
     * Utility method to wait until the specified image is loaded, and then return.
     */    
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
     */
    protected void computeBounds() {
	Rectangle2D rect = new Rectangle2D.Double();
	rect.setRect(translateX, translateY, getWidth(), getHeight());
	
	bounds.setRect(rect);
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
	if (image != null) {
	    Graphics2D g2 = renderContext.getGraphics2D();
	    boolean translated = false;
	    AffineTransform at = null;
	    if ((translateX != 0.0) || (translateY != 0.0)) {
		at = g2.getTransform();	// save transform
		g2.translate(translateX, translateY);
		translated = true;
	    }
	    g2.drawImage(image, null, observer);
	    if (translated) {
		g2.setTransform(at); // restore transform
	    }
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

    /**
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
	repaint();
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
	reshape();
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     * @see ZDebug#dump
     */
    public String dump() {
	String str = super.dump();
	if (fileName != null) {
	    str += "\n FileName = '" + fileName + "'";
	}
	if (url != null) {
	    str += "\n URL = '" + url + "'";
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

				// image to be loaded as a jar resource:
	  if ((fn.length() > 3) && (fn.substring(0,4).equals("jar:"))) {
	      String jarFileName = fn.substring(4, fn.indexOf('/'));
	      String entryName = fn.substring(fn.indexOf('/')+1);
	      String resourceFileName = "resources/" + jarFileName;
	      URL resourceURL = this.getClass().getClassLoader().getResource(resourceFileName);
	      if (resourceURL == null) {
		  System.out.println("ZImage resource " + resourceFileName + " not found.");
		  return;
	      }
				// turn URL into JarURL
	      String jarResourceName = resourceURL.toString();
	      if ((jarResourceName.length() > 3) && 
		  (!jarResourceName.substring(0,4).equals("jar:"))) {
		  jarResourceName = "jar:" + jarResourceName + "!/";
	      }
	      jarResourceName += entryName;
	      System.out.println("jarResourceName: "+jarResourceName);
	      URL jarResourceURL = null;
	      try {
		  jarResourceURL = new URL(jarResourceName);
	      } catch (java.net.MalformedURLException e) {
		  System.out.println("MalformedURLException: " + jarResourceName);
		  return;
	      }
	      setImage(jarResourceURL);
	  } else {      
	  
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
				// However, if the image is on a different disk, then it
				// is specified as an absolute pathname - so we will check
				// for absolute paths by assuming that if the second character
				// is a ':', then it is a PC disk.
	      if (fn.charAt(1) != ':') {
				// Not absolute, so convert relative pathname
		  String cwd = System.getProperty("user.dir");
		  fileName = getAbsolutePath(cwd+File.separator+fn);
	      } else {
				// Absolute, so don't convert
		  fileName = fn;
	      }
	      setImage(fileName);
	  }
	} else if (fieldName.compareTo("url") == 0) {
	    setImage((URL)fieldValue);
	} else if (fieldName.compareTo("writeEmbeddedImage") == 0) {
	    writeEmbeddedImage = ((Boolean)fieldValue).booleanValue();
	}
    }

    /**
     * Wait while image is loaded.
     * @param image The image.
    */
    public static boolean waitForImage(Image image) {
	int id;
	synchronized(sComponent) { id = sID++; }
	sTracker.addImage(image, id);
	try { sTracker.waitForID(id); }
	catch (InterruptedException ie) { return false; }
	if (sTracker.isErrorID(id)) return false;
	return true;
    }    

    /**
     * Creates a BufferedImage from an Image.
     * @param image The image.
    */
    public static BufferedImage makeBufferedImage(Image image) {
	if (waitForImage(image) == false) return null;
	BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
	Graphics2D g2 = bufferedImage.createGraphics();
	g2.drawImage(image, null, null);
	return bufferedImage;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();

	// if fileName exists then write boolean(true), else write boolean(false)
	if (fileName == null) {
	    out.writeBoolean(false);
	} else {
	    out.writeBoolean(true);
				// write String(fileName):
				// Image filenames should be relative to the directory
				// the .jazz file is being saved in. That should be
				// the current value of the user.dir property.
	    String cwd = System.getProperty("user.dir") + File.separator;
	    String relFileName = getRelativePath(fileName, cwd);
	    out.writeObject(relFileName);

				// if writeEmbeddedImage is true 
				// then write boolean(true) and write object(JPEG)
				// else write boolean(false)
	    if (writeEmbeddedImage) {
		out.writeBoolean(true);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		encoder.encode(makeBufferedImage(image));
	    } else {
		out.writeBoolean(false);
	    }
	}
    }	

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	if (in.readBoolean()) {	// read boolean(); image file exists?
	    fileName = (String)in.readObject();	// read String(fileName)
	    if (in.readBoolean()) { // read boolean(); image file is embedded?
				// read image as JPEG object
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
		image = (Image)decoder.decodeAsBufferedImage();
	    } else {		// image is fileName
				// read image file from disk
				// Convert name-separator character to the one appropriate
				// for current system.
		if (File.separator.equals("/")) {
		    fileName = fileName.replace('\\','/');
		} else {
		    fileName = fileName.replace('/','\\');
		}
	
				// Image filenames are usually saved as relative to the directory
				// the .jazz file is in. That directory should be
				// the current value of the user.dir property.
				// However, if the image is on a different disk, then it
				// is specified as an absolute pathname - so we will check
				// for absolute paths by assuming that if the second character
				// is a ':', then it is a PC disk.
		if (fileName.charAt(1) != ':') {
				// Not absolute, so convert relative pathname
		    String cwd = System.getProperty("user.dir");
		    fileName = getAbsolutePath(cwd+File.separator+fileName);
		}

		image = Toolkit.getDefaultToolkit().getImage(fileName);
		MediaTracker tracker = new MediaTracker(staticComponent);
		tracker.addImage(image, 0);
		try {
		    tracker.waitForID(0);
		}
		catch (InterruptedException exception) {
		    System.out.println("Couldn't load image: " + fileName);
		}
	    }
	    width = getWidth();
	    height = getHeight();
	}

				// BBB: Should deal with jar files and resources
				// BBB: Should read in binary if writeEmbeddedImage is true
    }
}
