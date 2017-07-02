package edu.umd.cs.jazztest.iotest;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.io.*;
import java.io.*;

/**
 * Use this class to help test saving objects.
 * @author: Jesse
 */
public class FileSavingSimulator {
    public static Serializable doSerialize(Serializable aSerializable) throws Exception {
        File f = new File("temp");
	try {
	    ObjectOutputStream out = null;
	    ObjectInputStream in = null;
	    
	    out = new ObjectOutputStream(new FileOutputStream(f));
	    out.writeObject(aSerializable);
	    out.close();
	    
	    in = new ObjectInputStream(new FileInputStream(f));
	    aSerializable = (Serializable) in.readObject();
	    in.close();
	}
	finally {
	    f.delete();
	}
	
        return aSerializable;
    }

    public static ZSerializable doZSerialize(ZSerializable aZSerializable) throws Exception {
        File f = new File("tempz");
	try {
	    ZObjectOutputStream out;
	    FileInputStream fin;
	    FileOutputStream fout;

	    fout = new FileOutputStream(f);
	    out = new ZObjectOutputStream(fout);
	    out.writeObject(aZSerializable);
	    out.flush();
	    out.close();
	    fout.close();
	    
	    ZParser parser = new ZParser();
	    parser = new ZParser();
	    fin = new FileInputStream(f);
	    aZSerializable = (ZSerializable) parser.parse(fin);
	    fin.close();
	}
	finally {
	    f.delete();
	}

        return aZSerializable;
    }
}
