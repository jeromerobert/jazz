package edu.umd.cs.jazz.io;

import java.io.*;

public class ZExtendedInputStream {
    protected InputStream stream; 
    protected long filePosition = 0;
    
    
    public ZExtendedInputStream(InputStream stream) {
	this.stream = stream;
    }

    public void setFilePosition(long n) throws IOException {
	long tmp = n - filePosition;
	stream.skip(tmp);
	filePosition += tmp;
    }

    public long  getFilePosition() {
	return filePosition;
    }

    public long skip(long n) throws IOException {
	long result = stream.skip(n);
	filePosition += n;

	return result;
    }

    public int read() throws IOException {
	int result = stream.read();
	filePosition ++;

	return result;
    }
    
    public int read(byte b[]) throws IOException {
	int result = stream.read(b);
	filePosition += result;

	return result;
    }
    
    public int read(byte b[], int off, int len) throws IOException {
    int result = stream.read(b, off, len);
	filePosition += result;

	return result;
    }
    
    public int available() throws IOException {
	return stream.available();
    }
    
    public void close() throws IOException {
	stream.close();
    }
    public synchronized void mark(int readlimit) {
	stream.mark(readlimit);
    }
    public synchronized void reset() throws IOException {
	stream.reset();
    }
    public boolean markSupported() {
	return stream.markSupported();
    }

    static public final void main(String[] args) {
	try {
	    java.io.FileInputStream f = new java.io.FileInputStream("test2.jazz");
	    ZExtendedInputStream s = new ZExtendedInputStream(f);
	    
	    s.setFilePosition(807);
	    byte[] buf = new byte[16];
	    s.read(buf, 0, 16);
	    System.out.println(new String(buf));
	    
	}
	catch (Exception e) {
	    System.out.println(e);
	    
	}
	    
    }
}

