/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import edu.umd.cs.jazz.*;

/**
 * <b>ZObjectReferenceTable</b> helps to manage the references between objects within the
 * scenegraph when a portion of the scenegraph is duplicated with clone().
 * It maintains the relationship between all cloned objects, and the original
 * objects they were cloned from.  Then, after all objects have been duplicated, each
 * object's updateObjectReferences method is called. This method takes a
 * ZObjectReferenceTable object as a parameter. The object's
 * updateObjectReferences method can then use the getNewObjectReference
 * method from this object to get updated references to objects that have
 * been duplicated in the new sub-graph. If a match is found, a
 * reference to the corresponding object in the newly cloned sub-graph is
 * returned. If no corresponding reference is found, a
 * ZDanglingReferenceException is thrown.
 * <P>
 * ZObjectReferenceTable is a singleton, which means that there can only be
 * one instance of it in the entire run-time system.  Rather than call the
 * constructor, use the getInstance() method to access the single instance.
 *
 * @author Ben Bederson 
 */
public class ZObjectReferenceTable implements Serializable {
    /**
     * The single instance of this table.
     */
    static private ZObjectReferenceTable instance = null;

    /**
     * The internal table that keeps the mapping between original and cloned objects.
     */
    private Hashtable table = null;

    /**
       Implements singleton for this class.  Always returns the same instance of ZObjectReferenceTable.
     */
    static public ZObjectReferenceTable getInstance() {
	if (instance == null) {
				// If this is the first time getInstance is called
	    instance = new ZObjectReferenceTable();
	}
	return instance;
    }

    /**
     * Constructor for new empty table.
     */
    protected ZObjectReferenceTable() {
	table = new Hashtable();
    }

    /**
     * Adds an original/cloned object pair to the table.
     * @param orig The original object
     * @param copy The copy of the original object
     */
    public void addObject(ZSceneGraphObject orig, ZSceneGraphObject copy) {
	table.put(orig, copy);
    }

    /**
     * Resets the table, removing all entries from it.
     */
    public void reset() {
	table.clear();
    }

    /**
     * Goes through all the original objects in the table, and
     * notifies them to update their internal references, passing in a reference
     * to this table so it can be queried for original/new object mappings.
     */
    public void updateObjectReferences() {
	ZSceneGraphObject obj;
	for (Enumeration e = table.elements() ; e.hasMoreElements() ;) {
	    obj = (ZSceneGraphObject)e.nextElement();
	    obj.updateObjectReferences(this);
	}
    }

    /**
     * This method is used in conjunction with the clone() method. It can
     * be used by the updateNodeReferences() method to see if a node that is
     * being referenced has been duplicated in the new cloned sub-graph.
     * An object's updateObjectReferences() method would use this method by
     * calling it with the reference to the old (existed before the clone
     * operation) object. If the object has been duplicated in the clone
     * sub-graph, the corresponding object in the cloned sub-graph is
     * returned. If no corresponding reference is found, a
     * ZDanglingReferenceException is thrown.
     * @param origObj The reference to the object in the original sub-graph.
     */
    public ZSceneGraphObject getNewObjectReference(ZSceneGraphObject origObj) throws ZDanglingReferenceException {
	ZSceneGraphObject newObj = (ZSceneGraphObject)table.get(origObj);
	if (newObj == null) {
	    throw new ZDanglingReferenceException(origObj);
	}

	return newObj;
    }
}
