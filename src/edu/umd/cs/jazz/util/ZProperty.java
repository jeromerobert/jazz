/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.util;

import java.io.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;

/**
 * <b>ZProperty</b> represents a ZNode client property.
 * It just encapsulates a (key, value) pair, and supports ZSerialization.
 */
public class ZProperty implements ZSerializable, Serializable {
    /**
     * The key to this property.
     */
    private Object key = null;

    /*
     * The value of this property.
     */
    private Object value = null;
	
    //****************************************************************************
    //
    //               Constructors
    //
    //***************************************************************************

    /**
     * Create an empty ZProperty.
     */
    public ZProperty() {
    }

    /**
     * Create a ZProperty with the specified (key, value) pair
     * @param key the new property key
     * @param value the new property value
     */
    public ZProperty(Object key, Object value) {
	this.key = key;
	this.value = value;
    }

    /**
     * Duplicates this property by using the copy constructor.
     * See the copy constructor comments for complete information about what is duplicated.
     */
    public Object clone() {
	ZProperty prop = new ZProperty();
	prop.set(key, value);

	return prop;
    }

    /**
     * Set the (key, value) pair stored in this property
     * @param key the new property key
     * @param value the new property value
     */
    public void set(Object key, Object value) {
	this.key = key;
	this.value = value;
    }

    /**
     * Determine the key of this property.
     * @return the property key
     */
    public Object getKey() {
	return key;
    }

    /**
     * Determine the value of this property.
     * @return the property value
     */
    public Object getValue() {
	return value;
    }

    /**
     * Specify the key of this property.
     * @param key the new property key
     */
    public void setKey(Object key) {
	this.key = key;
    }

    /**
     * Specify the value of this property.
     * @param value the new property value
     */
    public void setValue(Object value) {
	this.value = value;
    }

    /**
     * Generate a string that represents this object for debugging.
     * @return the string that represents this object for debugging
     */
    public String toString() {
	return super.toString() + ": Key = " + key + ", Value = " + value;
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
	if ((value != null) && (value instanceof ZSerializable)) {
	    out.writeState(key.getClass().getName(), "key", key);
	    out.writeState(value.getClass().getName(), "value", value);
	}
    }
	
    /**
     * Specify which objects this object references in order to write out the scenegraph properly
     * @param out The stream that this object writes into
     */
    public void writeObjectRecurse(ZObjectOutputStream out) throws IOException {
	if ((value != null) && (value instanceof ZSerializable)) {
	    out.addObject((ZSerializable)value);
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
	if (fieldName.compareTo("key") == 0) {
	    key = fieldValue;
	}
	if (fieldName.compareTo("value") == 0) {
	    value = fieldValue;
	}
    }

    /**
     * Properties get written out if the value is ZSerializable.
     * Else, they are skipped.
     */
    public ZSerializable writeReplace() {
	if ((value != null) && (value instanceof ZSerializable)) {
	    return this;
	} else {
	    return null;
	}
    }
}