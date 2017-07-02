/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.scenegraph;

import java.io.*;

import edu.umd.cs.jazz.io.*;

/**
 * A class that represents a ZNode property.
 * It just encapsulates a (key, value) pair, and supports ZSerialization.
 */
public class ZProperty implements ZSerializable {
    protected String key = null;
    protected Object value = null;
	
    public ZProperty() {
	key = new String();
    }

    public ZProperty(String key, Object value) {
	this.key = key;
	this.value = value;
    }

    public void set(String key, Object value) {
	this.key = key;
	this.value = value;
    }

    public String getKey() {
	return key;
    }

    public Object getValue() {
	return value;
    }

    /////////////////////////////////////////////////////////////////////////
    //
    // Saving
    //
    /////////////////////////////////////////////////////////////////////////

    public void writeObject(ZObjectOutputStream out) throws IOException {
	if ((value != null) && (value instanceof ZSerializable)) {
	    out.writeState("String", "key", key);
	    out.writeState(value.getClass().getName(), "value", value);
	}
    }
	
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	if ((value != null) && (value instanceof ZSerializable)) {
	    out.addObject((ZSerializable)value);
	}
    }

    public void setState(String fieldType, String fieldName, Object fieldValue) {
	if (fieldName.compareTo("key") == 0) {
	    key = (String)fieldValue;
	}
	if (fieldName.compareTo("value") == 0) {
	    value = fieldValue;
	}
    }

    /**
     * Properties get written out if the value is ZSerializable.
     * Else, it is skipped.
     */
    public ZSerializable writeReplace() {
	if ((value != null) && (value instanceof ZSerializable)) {
	    return this;
	} else {
	    return null;
	}
    }
}
