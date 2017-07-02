/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * <b>FileClassLoader</b> is a simple class loader which will load
 * class from a file on disk.  You must instantiate this class by
 * specifying the "root" directory that contains the .class files
 * (to be used in case any ancillary .class files are loaded).
 * Then, a class can be loaded either by specifying a filename
 * or a class name (which will be looked for in the classpath).
 *
 * @author  Benjamin B. Bederson
 */
public class FileClassLoader extends ClassLoader {

    private String root;

    public FileClassLoader(String rootDir) {
	if (rootDir == null){
	    throw new IllegalArgumentException("Null root directory");
	}
	root = rootDir;
    }

    protected Class loadClass(String name, boolean resolve) 
	throws ClassNotFoundException {

				// Since all support classes of loaded class use same class loader
				// must check subclass cache of classes for things like Object
	Class c = findLoadedClass(name);
	if (c == null) {
	    try {
		c = findSystemClass(name);
	    } catch (Exception e) {
	    }
	}

	if (c == null) {
	    String fileName;
	    if (name.endsWith(".class")) {
				// Convert filename to classname
		fileName = name;
		int index = fileName.lastIndexOf(File.separatorChar);
		name = fileName.substring(index + 1, fileName.length() - 6);    // Cut off trailing '.class'
	    } else {
				// Convert class name argument to filename (if not already a filename)
		fileName = name.replace('.', File.separatorChar) + ".class";
	    }

	    try {
		byte data[] = loadClassData(fileName);
		c = defineClass(name, data, 0, data.length);
		if (c == null) {
		    throw new ClassNotFoundException(name);
		}
	    } catch (IOException e) {
		throw new ClassNotFoundException("Error reading file: " + fileName);
	    }
	}
	if (resolve) {
	    resolveClass(c);
	}
	return c;
    }

    protected byte[] loadClassData(String fileName) throws IOException {
				// Create a file object relative to directory provided
	File f = new File(root, fileName);

				// Get size of class file
	int size = (int)f.length();

				// Reserve space to read
	byte buff[] = new byte[size];

				// Get stream to read from
	FileInputStream fis = new FileInputStream(f);
	DataInputStream dis = new DataInputStream(fis);

				// Read in data
	dis.readFully(buff);

				// close stream
	dis.close();

				// return data
	return buff;
    }
}
