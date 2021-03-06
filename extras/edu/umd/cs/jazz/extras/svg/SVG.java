/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */

package edu.umd.cs.jazz.extras.svg;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import java.util.Vector;
import java.io.*;

/**
 *   Loading SVG file and parsing it
 *   returning a svg node by requesting
 */
public class SVG {

    protected static Document document;

    protected org.w3c.dom.Node root = null;
    protected GNode groot = null;
    protected org.w3c.dom.Node currentNode = null;
    protected String url;

    public SVG() {
    }

    public GNode getRoot() {
        return groot;
    }

    public void loadFromStream(InputStream is) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // cannot decide location from inputstream
        url = null;

        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           document = builder.parse(is);
           root = document.getDocumentElement();
           groot = makeNode(root, null);
           currentNode = root;

        } catch (SAXParseException spe) {
           // Error generated by the parser
           System.out.println ("\n** Parsing error"
              + ", line " + spe.getLineNumber ()
              + ", uri " + spe.getSystemId ());
           System.out.println("   " + spe.getMessage() );

           // Use the contained exception, if any
           Exception  x = spe;
           if (spe.getException() != null)
               x = spe.getException();
           x.printStackTrace();
        } catch (SAXException sxe) {
           // Error generated by this application
           // (or a parser-initialization error)
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
        }
    }

    public void loadFromURI(String uri) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

/*
        if(uri == null) {
            throw new IOException("Null URL: Cannot load data");
        }
        url = uri;

        int slash = url.lastIndexOf("/");
        int backslash = url.lastIndexOf("\\");

        if(slash < 0 && backslash < 0) {
            throw new IOException("Bad URL: Cannot load data");
        }
        int lastIndex = Math.max(slash, backslash);
        url = url.substring(0, lastIndex);
*/
        url = uri;

        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           document = builder.parse(uri);
           root = document.getDocumentElement();
           groot = makeNode(root, null);
           currentNode = root;

        } catch (SAXParseException spe) {
           System.out.println ("\n** Parsing error"
              + ", line " + spe.getLineNumber ()
              + ", uri " + spe.getSystemId ());
           System.out.println("   " + spe.getMessage() );
           Exception  x = spe;
           if (spe.getException() != null)
               x = spe.getException();
           x.printStackTrace();
        } catch (SAXException sxe) {
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
           ioe.printStackTrace();
        }
    }

    public void loadFromFile(File file) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
/*
        if(file == null) {
            throw new IOException("Null file: Cannot load data");
        }
        if(!file.isFile()) {
            throw new IOException("Bad file name: " + file.getPath());
        }
        url = "file:" + file.getParentFile().getAbsolutePath();
*/
        url = "file:" + file.getAbsolutePath();

        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           document = builder.parse(file);
           root = document.getDocumentElement();
           groot = makeNode(root, null);
           currentNode = root;

        } catch (SAXParseException spe) {
           System.out.println ("\n** Parsing error"
              + ", line " + spe.getLineNumber ()
              + ", uri " + spe.getSystemId ());
           System.out.println("   " + spe.getMessage() );
           Exception  x = spe;
           if (spe.getException() != null)
               x = spe.getException();
           x.printStackTrace();
        } catch (SAXException sxe) {
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
           ioe.printStackTrace();
        }
    }

    public Node findNode(String id) {
        return findNode(root, id);
    }

    public Node findNode(Node node, String id) {
        org.w3c.dom.NodeList nodeList = node.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            org.w3c.dom.Node child = nodeList.item(i);
            int type =  child.getNodeType();

            if(type == ELEMENT_TYPE && child.getNodeName().equals(id)) {
                // found!
                return child;
            } else if(type == ELEMENT_TYPE) {
                return findNode(child, id);
            }
        }
        return null;
    }

    public GNode getNextNode(Vector GNodes) {
        org.w3c.dom.Node node;
        GNode result = null;
        boolean noChild = true;


        if(currentNode == null) {
            // it means that this class is not initialized...
            // return null..
            return null;
        }
        // first go down and record path
        org.w3c.dom.NodeList nodeList = currentNode.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++) {
            org.w3c.dom.Node child = nodeList.item(i);
            int type =  child.getNodeType();

            if(type == ELEMENT_TYPE && !child.getNodeName().startsWith("#")) {
                noChild = false;
                currentNode = child;
                result = makeNode(child, GNodes);
                break;
            }
        }

        if(noChild) {
            // if there is no more child
            // find sibling
            node = currentNode.getNextSibling();

            while(node == null) {
                node = currentNode.getParentNode();
                if(node == root) {
                    return null;
                }
                node = node.getNextSibling();
            }

            if(node != null) {
                currentNode = node;
                if(node.getNodeName().startsWith("#")) {
                    result = getNextNode(GNodes);
                } else {
                    result = makeNode(node, GNodes);
                }
            }
        }
        return result;
    }
    protected GNode makeNode(Node node, Vector GNodes) {
        GNode gnode = null;
        GNode parent = null;

        // This assumes that every node is uncovered after parent
        // node is found

        if(GNodes != null) {
            for(int i=0;i < GNodes.size();i++) {
                GNode pgnode = (GNode)GNodes.elementAt(i);
                if(node.getParentNode() == pgnode.node) {
                    parent = pgnode;
                    break;
                }
            }
        }

        if(node.getNodeName().equalsIgnoreCase("svg")) {
                gnode = new GSVG(node, parent);
                // set url form root node....
                // then the children will copy the value from it
                gnode.setLocation(url);
        } else if(node.getNodeName().equalsIgnoreCase("g")) {
                gnode = new GG(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("text")) {
                gnode = new GText(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("tspan")) {
                gnode = new GText(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("tref")) {
                gnode = new GNode(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("rect")) {
                gnode = new GRect(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("line")) {
                gnode = new GLine(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("circle")) {
                gnode = new GCircle(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("polygon")) {
                gnode = new GPolygon(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("polyline")) {
                gnode = new GPolyline(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("ellipse")) {
                gnode = new GEllipse(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("image")) {
                gnode = new GImage(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("defs")) {
                gnode = new GNode(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("symbol")) {
                gnode = new GSymbol(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("use")) {
                gnode = new GUse(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("path")) {
                gnode = new GPath(node, parent);
        //
        //
        //      Not implemented Yet
        //
        //
        } else if(node.getNodeName().equalsIgnoreCase("style")) {
                gnode = new GNode(node, parent);
        } else if(node.getNodeName().equalsIgnoreCase("clipPath")) {
                gnode = new GNode(node, parent);
        } else {
                gnode = new GNode(node, parent);
                if(!node.getNodeName().startsWith("#") && !node.getNodeName().equalsIgnoreCase("data")) {
                    System.out.println("Unresolved node: "+node.getNodeName());
                }
        }

        return gnode;
    }

    static final String[] typeName = {
        "none",         "Element",      "Attr",        "Text",        "CDATA",
        "EntityRef",    "Entity",       "ProcInstr",   "Comment",     "Document",
        "DocType",      "DocFragment",  "Notation",
    };

    static final int ELEMENT_TYPE =   1;
    static final int ATTR_TYPE =      2;
    static final int TEXT_TYPE =      3;
    static final int CDATA_TYPE =     4;
    static final int ENTITYREF_TYPE = 5;
    static final int ENTITY_TYPE =    6;
    static final int PROCINSTR_TYPE = 7;
    static final int COMMENT_TYPE =   8;
    static final int DOCUMENT_TYPE =  9;
    static final int DOCTYPE_TYPE =  10;
    static final int DOCFRAG_TYPE =  11;
    static final int NOTATION_TYPE = 12;

}