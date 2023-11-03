/*
 * Copyright Public Record Office Victoria 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 August 2023
 */
package VERSCommon;

/**
 * This class represents a problem found when processing a VEO. The problem was
 * not sufficiently severe to stop processing the VEO. If processing the VEO
 * cannot be continued (but other VEOs can be processed), a VEOError (or
 * AppError) should be thrown. If the problem is so severe that processing
 * should be terminated, a VEOFatal (or AppFatal) should be thrown.
 * 
 * A failure has four pieces of information.
 * 
 * The failureId uniquely identifies this failure from all other failures the
 * application can generate and can be used as an index to the possible
 * problems, their cause, and what code generated it. The failureId is
 * composed of the className, method within class (optional), and a number.
 * The method is not present if the failure was generated by the class
 * constructor.
 * 
 * The location identifies where the error was found within the VEO. This might
 * be a file (e.g. which XML file), or a particular instance of a data
 * structure (e.g. a particular Information Object).
 * 
 * The message describes the failure, and the optional exception is included
 * if the failure was caused by catching an exception.
 * 
 * A primary use of this class is to standardise the error messages generated
 * by an application. To this end, static methods are provided to generate
 * the standard messages. This can be used if collecting a VEOFailure is not
 * useful (e.g. for logging or for generating a VEOError, AppError, VEOFatal,
 * or AppFatal
 */
public class VEOFailure {
    String failureId;   // unique identifier for type of failure
    String location;    // location of error in VEO (e.g. signature) (may be null)
    String message;     // message associated with the failure
    Exception e;        // exception that caused the failure (may be null)
    
    /**
     * Construct a new VEOFailure. Used from a constructor
      
     * @param classname name of class generating the failure (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param message failure message
     */
    public VEOFailure(String classname, int errno, String message) {
        assert classname != null;
        assert errno > 0;
        assert message != null;
        internal(classname, null, errno, null, message, null);
    }
    
    /**
     * Construct a new VEOFailure. Used from a constructor when generating
     * from an exception.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param message error message
     * @param e the exception
     */
    public VEOFailure(String classname, int errno, String message, Exception e) {
        assert classname != null;
        assert errno > 0;
        assert message != null;
        assert e != null;
        internal(classname, null, errno, null, message, e);
    }

    /**
     * Construct a new VEOFailure. Used from a method.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param message a string describing the error (must not be null)
     */
    public VEOFailure(String classname, String method, int errno, String message) {
        assert classname != null;
        assert method != null;
        assert errno > 0;
        assert message != null;
        internal(classname, method, errno, null, message, null);
    }

    /**
     * Construct a new VEOFailure. Used from a method when generating from an
     * exception.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating failure within the class (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param message a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (must not be null)
     */
    public VEOFailure(String classname, String method, int errno, String message, Exception e) {
        assert classname != null;
        assert method != null;
        assert errno > 0;
        assert message != null;
        assert e != null;
        internal(classname, method, errno, null, message, e);
    }
    
    /**
     * Construct a new VEOFailure. Used from a constructor when necessary to
     * identify the location in the VEO.
     *
     * @param classname name of class generating the failure (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param message failure message
     */
    public VEOFailure(String classname, int errno, String location, String message) {
        assert classname != null;
        assert errno > 0;
        assert location != null;
        assert message != null;
        internal(classname, null, errno, location, message, null);
    }
    
    /**
     * Construct a new VEOFailure. Used from a constructor when necessary to
     * identify the location in the VEO & exception.
     * 
     * @param classname name of class generating the failure (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param message error message
     * @param e the exception
     */
    public VEOFailure(String classname, int errno, String location, String message, Exception e) {
        assert classname != null;
        assert errno > 0;
        assert location != null;
        assert message != null;
        assert e != null;
        internal(classname, null, errno, location, message, e);
    }

    /**
     * Construct a new VEOFailure. Used from a method when necessary to
     * identify the location in the VEO.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param message a string describing the error (must not be null)
     */
    public VEOFailure(String classname, String method, int errno, String location, String message) {
        assert classname != null;
        assert method != null;
        assert errno > 0;
        assert location != null;
        assert message != null;
        internal(classname, method, errno, location, message, null);
    }

    /**
     * Construct a new VEOFailure. Used from a method when necessary to
     * identify the location in the VEO & exception.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating failure within the class (must not be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param message a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (must not be null)
     */
    public VEOFailure(String classname, String method, int errno, String location, String message, Exception e) {
        assert classname != null;
        assert method != null;
        assert errno > 0;
        assert location != null;
        assert message != null;
        assert e != null;
        internal(classname, method, errno, location, message, e);
    }
    
    /**
     * Internal common code for constructing a failure
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating failure within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param s a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (may be null)
     *
     */ 
    private void internal(String classname, String method, int errno, String location, String s, Exception e) {
        assert classname != null;
        assert errno > 0;
        assert s != null;
        failureId = constFailureId(classname, method, errno);
        if (location != null && (location.equals("") || location.trim().equals(" "))) {
            location = null;
        }
        this.location = location;
        this.message = s;
        this.e = e;
    }
    
    /**
     * Construct a standard format failure message.
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating failure within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param s a string describing the failure (must not be null)
     * @return the standard format failure
     */
    public static String getMessage(String classname, String method, int errno, String s) {
        return failureMesg(constFailureId(classname, method, errno), null, s, null);
    }
    
    /**
     * Construct a standard format failure message. This static method is used when
     * an Exception is not required to be thrown - e.g. we are just logging
     * the message
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param s a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (may be null)
     * @return the standard format failure
     */
    public static String getMessage(String classname, String method, int errno, String s, Exception e) {
        return failureMesg(constFailureId(classname, method, errno), null, s, e);
    }
    
    /**
     * Construct a standard format failure message
     * 
     * @param classname name of class and method generating the failure (must not be null)
     * @param method method generating failure within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param s a string describing the failure (must not be null)
     * @return the standard format failure
     */
    public static String getMessage(String classname, String method, int errno, String location, String s) {
        return failureMesg(constFailureId(classname, method, errno), location, s, null);
    }
    
    /**
     * Construct a standard format failure message. This static method is used when
     * an Exception is not required to be thrown - e.g. we are just logging
     * the message
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param method method generating error within the class (may be null)
     * @param errno unique failure number (must be > 0)
     * @param location the location in the VEO where the error occurred
     * @param s a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (may be null)
     * @return the standard format failure
     */
    public static String getMessage(String classname, String method, int errno, String location, String s, Exception e) {
        return failureMesg(constFailureId(classname, method, errno), location, s, e);
    }
    
    /**
     * Construct a standard format failure message.
     * 
     * @return the standard format failure
     */
    public String getMessage() {
        return failureMesg(failureId, location, message, e);
    }
    
    /**
     * Turn the classname, method, and failno into a unique identifier for this failure
     * 
     * @param classname name of class and method generating the error (must not be null)
     * @param method method generating error within the class (may be null)
     * @param failno unique error number (must be > 0)
     * @return 
     */
    private static String constFailureId(String classname, String method, int failno) {
        StringBuilder sb = new StringBuilder();
        
        assert (classname != null & !classname.equals(""));
        assert (failno > 0);
        sb.append(classname);
        if (method != null) {
            sb.append(".");
            sb.append(method);
        }
        sb.append("(");
        if (failno > 0) {
            sb.append(failno);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Construct a standard format failure message
     *
     * @param failureId unique identifier for the failure
     * @param location the location in the VEO where the error occurred
     * @param s a string describing the failure (must not be null)
     * @param e an exception that has caused the failure (may be null)
     * @return the standard format error
     */
    private static String failureMesg(String failureId, String location, String s, Exception e) {
        StringBuilder sb = new StringBuilder();

        assert (s != null && ! s.equals("") && !s.trim().equals(" "));
        if (location != null) {
            sb.append(location);
            sb.append(": ");
        }
        sb.append(s);
        if (e != null) {
            sb.append(". Cause is: ");
            sb.append(e.getMessage());
        }
        sb.append(" (");
        sb.append(failureId);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Gets the failure identifier to uniquely identify the failure.
     *
     * @return the error number
     */
    public String getFailureId() {
        return failureId;
    }
}
