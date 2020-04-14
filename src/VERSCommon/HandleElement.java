/*
 * Copyright Public Record Office Victoria 2018
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2018
 */
package VERSCommon;

import java.nio.file.Path;

/**
 * Instructions from {@link XMLConsumer} to tell an {@link XMLParser} what needs
 * is to be done with the contents of the element. The consumer has three
 * options:
 * <ul>
 * <li>Capture the element value as a string. There is a suboption to indicate
 * that this value is Base64 encoded and to decode it while writing to the file.
 * <li>Capture the element value directly into a file. This is useful is the
 * value is a lengthy value. XML quoted text is as XML quoted text (e.g.
 * &amp;gt; is output as &amp;gt;). There is a suboption to indicate that this
 * value is Base64 encoded and to decode it while writing to the file.
 * <li>Capture the entire element (including the start and end tags, and
 * subelements) as String. In this case XML special characters in the value
 * content are captured in their quoted form (e.g. &amp; is output as &amp;gt;)
 * so that the element is still valid XML.
 * </ul>
 */
public class HandleElement {       
    static final public int ELEMENT_TO_STRING = 0; // capture entire element as a string
    static final public int VALUE_TO_STRING = 1;   // capture value as a string
    static final public int VALUE_TO_FILE = 2;     // capture value in a file
    private final int action;           // what to do (i.e. the above 3 actions)
    private final boolean decodeBase64; // if true, value should be a Base64 and it is to be decoded
    private final Path outputFile;      // if isReturnValue is false, the file to create to hold the value

    /**
     * Simple instruction - capture an ordinary value or element as a string
     * 
     * @param action What to capture
     */
    public HandleElement(int action) {
        this.action = action;
        decodeBase64 = false;
        outputFile = null;
    }
    
    /**
     * Simple instruction - capture an Base64 encoded value (to a file or a
     * string), or a value to a file
     * 
     * @param action what to do (capture a value)
     * @param decodeBase64 true if value is Base64 encoded
     * @param outputFile  file to put value in (if capturing to a file)
     */
    public HandleElement(int action, boolean decodeBase64, Path outputFile) {
        this.action = action;
        this.decodeBase64 = decodeBase64;
        this.outputFile = outputFile;      
    }

    /**
     * What is the action the Consumer requested
     *
     * @return the action
     */
    public int actionRequested() {
        return action;
    }

    /**
     * Is the value to be decoded from Base64?
     *
     * @return true if the value is to be decoded
     */
    public boolean isDecodeBase64() {
        return decodeBase64;
    }

    /**
     * What file is the value to be written to?
     *
     * @return the Path of the file to hold the value
     */
    public Path getOutputFile() {
        return outputFile;
    }
}
