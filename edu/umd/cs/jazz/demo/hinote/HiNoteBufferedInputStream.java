/**
 * Copyright 1998-1999 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazz.demo.hinote;

import java.io.*;

/**
 * <b>HiNoteBufferedInputStream</b> is a very basic stream buffering mechanism
 * to allow skip() in reverse up to the buffer size.
 *
 * @author  Benjamin B. Bederson
 */
public class HiNoteBufferedInputStream extends BufferedInputStream {
    public HiNoteBufferedInputStream(InputStream in) {
	super(in);
    }

    public HiNoteBufferedInputStream(InputStream in, int size) {
	super(in, size);
    }

    public long skip(long n) throws IOException {
	long c;
	if (n < 0) {
	    pos += n;
	    c = n;
	    if (pos < 0) {
		c -= pos;
		pos = 0;
	    }
	} else {
	    c = super.skip(n);
	}
	return c;
    }
}
