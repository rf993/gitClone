/*
 * Copyright Public Record Office Victoria 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2015
 */
package VERSCommon;

/**
 * This class represents a fatal error that occurred when constructing a VEO.
 * A fatal error is one that means it is not possible to continue to process any
 * VEOs and the program needs to terminate. 
 */
public class VEOFatal extends VEOError {

    /**
     * Construct a new VEOFatal with appropriate error message
     *
     * @param s the error message to return
     */
    /*
    public VEOFatal(String s) {
        super(s);
    }
    */

    /**
     * Construct a new VEOFatal from a class constructor giving error number.
     * This constructor should be used when it is desired to test that this
     * error is handled correctly.
     *
     * @param errno unique error number
     * @param s error message
     */
    /*
    public VEOFatal(int errno, String s) {
        super(s);
        this.errno = errno;
    }
    */

    /**
     * Construct a new VEOFatal from a class constructor giving error number
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     */
    public VEOFatal(String classname, int errno, String s) {
        super(classname, errno, s);
    }
    
    /**
     * Construct a new VEOFatal from a class constructor giving error number and exception
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     * @param e an exception that has caused the error (may be null)
     */
    public VEOFatal(String classname, int errno, String s, Exception e) {
        super(classname, errno, s, e);
    }

    /**
     * Construct a new VEOFatal from a particular method giving error number
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     */
    public VEOFatal(String classname, String method, int errno, String s) {
        super(classname, method, errno, s);
    }

    /**
     * Construct a new VEOFatal from a particular method giving error number and exception
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     * @param e an exception that has caused the error (may be null)
     */
    public VEOFatal(String classname, String method, int errno, String s, Exception e) {
        super(classname, method, errno, s, e);
    }
}

