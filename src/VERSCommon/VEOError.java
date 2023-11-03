/*
 * Copyright Public Record Office Victoria 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2015
 */
package VERSCommon;

/**
 * This class represents a non fatal error produced when constructing a VEO.
 * This VEO should be aborted and cleaned up, but further VEOs may be attempted.
 */
public class VEOError extends Exception {

    String errorId; // unique identifier for type of error
    int errno;  // error number for automated processing

    /**
     * Construct a new VEOError with appropriate error message. This constructor
     * should be used when it is not feasible to test if this error has occurred
     * (e.g. an obscure exception is thrown).
     *
     * @param s the error message to return
     */
    public VEOError(String s) {
        super(s);
        errno = -1;
    }

    /**
     * Construct a new VEOError from a class constructor giving error number.
     * This constructor should be used when it is desired to test that this
     * error is handled correctly.
     *
     * @param errno unique error number
     * @param s error message
     */

    public VEOError(int errno, String s) {
        super(s);
        this.errno = errno;
    }

    
    /**
     * Construct a new VEOError using a VEOFailure
     *
     * @param vf the VEOFailure
     */
    public VEOError(VEOFailure vf) {
        super(vf.getMessage());
        errorId = vf.getFailureId(); // has to be done twice because you need to call super() first
        this.errno = 0;
    }

    /**
     * Construct a new VEOError from a class constructor giving error number
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param errno unique error number (must be > 0)
     * @param s the error message
     */
    public VEOError(String classname, int errno, String s) {
        super(errMesg(constErrorId(classname, null, errno), s, null));
        errorId = constErrorId(classname, null, errno); // has to be done twice because you need to call super() first
        this.errno = errno;
    }

    /**
     * Construct a new VEOError from a class constructor giving error number and
     * an exception
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param errno unique error number (must be > 0)
     * @param s error message
     * @param e the exception
     */
    public VEOError(String classname, int errno, String s, Exception e) {
        super(errMesg(constErrorId(classname, null, errno), s, e));
        errorId = constErrorId(classname, null, errno); // has to be done twice because you need to call super() first
        this.errno = errno;
    }

    /**
     * Construct a new VEOError from a particular method giving error number
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     */
    public VEOError(String classname, String method, int errno, String s) {
        super(errMesg(constErrorId(classname, method, errno), s, null));
        errorId = constErrorId(classname, method, errno); // has to be done twice because you need to call super() first
        this.errno = errno;
    }

    /**
     * Construct a new VEOError from a particular method giving error number and
     * an exception
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     * @param e an exception that has caused the error (may be null)
     */
    public VEOError(String classname, String method, int errno, String s, Exception e) {
        super(errMesg(constErrorId(classname, method, errno), s, e));
        errorId = constErrorId(classname, method, errno); // has to be done twice because you need to call super() first
        this.errno = errno;
    }

    /**
     * Construct a standard format error message. This static method is used
     * when an Exception is not required to be thrown - e.g. we are just logging
     * the message
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     * @return the standard format error
     */
    public static String errMesg(String classname, String method, int errno, String s) {
        return errMesg(constErrorId(classname, method, errno), s, null);
    }

    /**
     * Construct a standard format error message. This static method is used
     * when an Exception is not required to be thrown - e.g. we are just logging
     * the message
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @param s a string describing the error (must not be null)
     * @param e an exception that has caused the error (may be null)
     * @return the standard format error
     */
    public static String errMesg(String classname, String method, int errno, String s, Exception e) {
        return errMesg(constErrorId(classname, method, errno), s, e);
    }

    /**
     * Construct a standard format error message
     *
     * @param errorId unique identifier for the error
     * @param s a string describing the error (must not be null)
     * @param e an exception that has caused the error (may be null)
     * @return the standard format error
     */
    private static String errMesg(String errId, String s, Exception e) {
        StringBuilder sb = new StringBuilder();

        assert (s != null && !s.equals("") && !s.trim().equals(" "));
        sb.append(s);
        if (e != null) {
            sb.append(". Cause is: ");
            sb.append(e.getMessage());
        }
        sb.append(" (");
        sb.append(errId);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Turn the classname, method, and errno into a unique identifier for this
     * error
     *
     * @param classname name of class and method generating the error (must not
     * be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique error number (must be > 0)
     * @return
     */
    private static String constErrorId(String classname, String method, int errno) {
        StringBuilder sb = new StringBuilder();

        assert (classname != null & !classname.equals(""));
        assert (errno > 0);
        sb.append(classname);
        if (method != null) {
            sb.append(".");
            sb.append(method);
        }
        sb.append("(");
        if (errno != -1) {
            sb.append(errno);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Gets the error identifier to uniquely identify the error.
     *
     * @return the error number
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Gets the error number to uniquely identify the error. This is deprecated
     * as it is not a unique id and will be removed.
     *
     * @return the error number
     */
    /*
    public int getId() {
        return errno;
    }
     */
}
