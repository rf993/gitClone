/*
 * Copyright Public Record Office Victoria 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2015
 */
package VERSCommon;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * This class creates a generic XML document. It is based on CreateXMLDoc in
 * neoVEO/VEOCreate, which should be replaced by calls to this class eventually.
 */
public class XMLCreator {

    String rootElement;     // root element
    boolean indent;         // if true, indent the elements in the documents
    int level;              // the level of indentation;
    Charset cs;             // converter from String to UTF-8
    FileOutputStream fos;   // underlying file stream for file channel
    FileChannel xml;        // XML document being written
    String classname = "XMLCreator"; // String describing this class for error & logging

    private final static Logger log = Logger.getLogger("versCommon.createXMLDoc");

    /**
     * Creates an XML Document in the specified directory.
     *
     * @param indent if true, indent the elements in the document
     * @throws AppError if this document cannot be created
     */
    public XMLCreator(boolean indent) throws AppError {

        // utilities
        try {
            cs = Charset.forName("UTF-8");
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new AppError(classname, 1, ": Failed to set UTF-8 charset");
        }
        this.indent = indent;
        level = 1;
    }

    /**
     * Start an XML document.
     *
     * This method generates the XML preamble and the root element.
     *
     * @param xmlDoc the XML Document to generate
     * @param rootElement the root element of the XML document
     * @param namespaces string containing any namespace definitions to be used
     * in this document
     * @throws AppError
     */
    public void startXMLDoc(Path xmlDoc, String rootElement, String namespaces) throws AppError {
        String module = "startXMLDoc";

        // remember root element
        this.rootElement = rootElement;

        // open XML document for writing
        try {
            fos = new FileOutputStream(xmlDoc.toString());
        } catch (FileNotFoundException fnfe) {
            throw new AppError(classname, module, 1, "Output VEO file '" + xmlDoc.toString() + "' cannot be opened for writing");
        }
        xml = fos.getChannel();

        // generate XML document up to start of content
        write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n");
        write("<");
        write(rootElement);
        if (namespaces != null) {
            write(" ");
            write(namespaces);
        }
        write(">");
    }

    /**
     * Start a new element - usually an element with subelements
     *
     * @param name the name of the element (must be non null & conform to
     * element name rules)
     * @param attributes any attributes to go in the element
     * @throws AppError
     */
    public void startElement(String name, String attributes, boolean runOn) throws AppError {
        String module = "startElement";
        int i;

        if (!runOn) {
            write("\r\n");
            if (indent) {
                for (i = 0; i < level; i++) {
                    write(" ");
                }
            }
        }
        level++;
        write("<");
        write(name);
        if (attributes != null) {
            write(" ");
            write(attributes);
        }
        write(">");
    }

    /**
     * Include a simple element & value
     *
     * @param name the name of the element (must be non null & conform to
     * element name rules)
     * @param attributes any attributes to go in the element
     * @param value the value of the element
     * @param runOn if true, don't include whitespace between elements
     * @throws AppError
     */
    public void includeElement(String name, String attributes, String value, boolean runOn) throws AppError {
        String module = "includeElement";
        int i;

        if (!runOn) {
            write("\r\n");
            if (indent && !runOn) {
                for (i = 0; i < level; i++) {
                    write(" ");
                }
            }
        }
        write("<");
        write(name);
        if (attributes != null) {
            write(" ");
            write(attributes);
        }
        if (value != null) {
            write(">");
            writeClean(value);
            write("</");
            write(name);
        } else {
            write("/");
        }
        write(">");
    }

    public void endElement(String name, boolean runOn) throws AppError {
        String module = "endElement";
        int i;

        level--;
        if (!runOn) {
            write("\r\n");
            if (indent) {
                for (i = 0; i < level; i++) {
                    write(" ");
                }
            }
        }
        write("</");
        write(name);
        write(">");
    }

    /**
     * Finish and close an XML document.
     *
     * @throws AppError if a fatal error occurred
     */
    public void endXMLDoc() throws AppError {
        String module = "startXMLDoc";

        write("\r\n</");
        write(rootElement);
        write(">");

        try {
            xml.close();
            xml = null;
            fos.close();
            fos = null;
        } catch (IOException ioe) {
            throw new AppError(classname, module, 1, "Failed to close XML document:" + ioe.getMessage());
        }
    }

    /**
     * Low level routine to encode text to UTF-8 and write to the XML document.
     * Warning: this routine assumes that XML special characters are already
     * encoded.
     *
     * @param s string to write to XML document
     * @throws AppError if a fatal error occurred
     */
    public void write(String s) throws AppError {
        String module = "write";

        try {
            xml.write(cs.encode(s));
        } catch (IOException ioe) {
            throw new AppError(classname, module, 1, "Failed writing to XML document: " + ioe.toString());
        }
    }

    /**
     * Low level routine to encode a character to UTF-8 and write to XML
     * document. Warning: this routine assumes that XML special characters are
     * already encoded.
     *
     * @param c character to write to XML document
     * @throws AppError if a fatal error occurred
     */
    public void write(char c) throws AppError {
        String module = "write";
        CharBuffer cb;
        try {
            cb = CharBuffer.allocate(1);
            cb.append(c);
            xml.write(cs.encode(cb));
        } catch (IOException ioe) {
            throw new AppError(classname, module, 2, "Failed writing to XML document:" + ioe.getMessage());
        }
    }

    /**
     * Low level routine to encode an XML value to UTF-8 and write to the XML
     * document. The special characters ampersand, less than, greater than,
     * single quote and double quote are quoted.
     *
     * @param s string to write to XML document
     * @throws VEOError if a fatal error occurred
     */
    public void writeClean(String s) throws AppError {
        String module = "write";
        StringBuilder sb = new StringBuilder();
        int i;
        char c;

        // sanity check
        if (s == null || s.length() == 0) {
            return;
        }

        // quote the special characters in the string
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        try {
            xml.write(cs.encode(sb.toString()));
        } catch (IOException ioe) {
            throw new AppError(classname, module, 2, "Failed writing to XML document" + ioe.getMessage());
        }
    }

    /**
     * Abandon construction of this XML document and free any resources.
     *
     * @param debug true if in debugging mode
     */
    public void abandon(boolean debug) {
        try {
            if (xml != null) {
                xml.close();
            }
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            /* ignore */ }
    }

    /**
     * M A I N
     *
     * Test program to tell if XMLCreator is working
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        XMLCreator cxd;
        Path path;

        try {
            path = FileSystems.getDefault().getPath("Test");
            System.out.println((path.toAbsolutePath()).toString());
            cxd = new XMLCreator(true);
            cxd.startXMLDoc(Paths.get("Test", "VEOContent.xml"), "testDoc", null);
            cxd.write("Testing123");
            cxd.endXMLDoc();
        } catch (AppError e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Complete!");
    }
}
