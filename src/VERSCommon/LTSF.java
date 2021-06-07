/*
 * Copyright Public Record Office Victoria 2020
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 */
package VERSCommon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * This class maintains a database of valid long term sustainable formats.
 * Currently the list of formats is maintained in PROS 19/05 S3 "Long Term
 * Sustainable Formats". The valid formats are read from a text file. The lines
 * in this file are either empty, start with an '!' (a comment), or represents a
 * long term sustainable format. Each LTSF line contains a list of semicolon
 * separated file extensions (including the leading '.'), optionally followed by
 * a tab and a list of equivalent comma separate MIME types.
 *
 * The MIME types are only used as an option in VERS V2.
 */
public class LTSF {

    ArrayList<String> ltsf;
    ArrayList<String> ltsfMime;
    String classname = "LTSF";

    /**
     * Read a file containing a list of accepted long term preservation formats.
     *
     * @param formats the directory in which the file is to be found
     * @throws VEOFatal if the file could not be read
     */
    public LTSF(Path formats) throws VEOFatal {
        String method = "LTSF";
        FileReader fr;
        BufferedReader br;
        String s;
        String tokens1[], tokens2[];
        int i;

        ltsf = new ArrayList<>();
        ltsfMime = new ArrayList<>();

        // open validLTPF.txt for reading
        fr = null;
        br = null;
        try {
            fr = new FileReader(formats.toFile());
            br = new BufferedReader(fr);

            // go through validLTPF.txt line by line, copying patterns into hash map
            // ignore lines that do begin with a '!' - these are comment lines
            while ((s = br.readLine()) != null) {
                s = s.trim();
                if (s.length() == 0 || s.charAt(0) == '!') {
                    continue;
                }

                // split the line at a tab (if present). The first bit is a list
                // of file extensions, the second bit (if present) is a list of
                // MIME types
                tokens1 = s.split("\t");
                if (tokens1.length > 0) {
                    tokens2 = tokens1[0].split(";");
                    for (i = 0; i < tokens2.length; i++) {
                        s = tokens2[i].trim().toLowerCase();
                        ltsf.add(s);
                    }
                    if (tokens1.length > 1) {
                        tokens2 = tokens1[1].split(";");
                        for (i = 0; i < tokens2.length; i++) {
                            s = tokens2[i].trim().toLowerCase();
                            ltsfMime.add(s);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new VEOFatal(classname, method, 2, "Failed to open LTSF file '" + formats.toAbsolutePath().toString() + "' due to: " + e.getMessage());
        } catch (IOException ioe) {
            throw new VEOFatal(classname, method, 1, "Unexpected error: " + ioe.toString());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    /* ignore */ }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    /* ignore */ }
            }
        }
    }
    
    /**
     * Free resouces associated with instance
     */
    public void free() {
        if (ltsf != null) {
            ltsf.clear();
            ltsf = null;
        }
        if (ltsfMime != null) {
            ltsfMime.clear();
            ltsfMime = null;
        }
    }
    /**
     * Generate string representation...
     * @return
     */
    @Override
    public String toString() {
        int i;
        StringBuilder sb;

        sb = new StringBuilder();
        sb.append("File extensions: ");
        if (ltsf.isEmpty()) {
            sb.append("none");
            sb.append("\n");
        } else {
            for (i = 0; i < ltsf.size(); i++) {
                sb.append("'");
                sb.append(ltsf.get(i));
                sb.append("'");
            }
            sb.append("\n");
        }
        if (ltsfMime.isEmpty()) {
            sb.append("none");
            sb.append("\n");
        } else {
            for (i = 0; i < ltsfMime.size(); i++) {
                sb.append("'");
                sb.append(ltsfMime.get(i));
                sb.append("'");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Is this format contained in the list of long term sustainable formats? V2
     * sustainable formats are the same as V3, except they can also be expressed
     * as MIME formats. WARNING. The format must be passed in as lower case
     * only.
     *
     * @param format format to be checked
     * @return
     */
    public boolean isV2LTSF(String format) {
        int i;
        String s;

        // check file extenstions
        if (isV3LTSF(format)) {
            return true;
        }

        // check MIME file types
        for (i = 0; i < ltsfMime.size(); i++) {
            s = ltsfMime.get(i);
            if (format.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is this format a valid V3 long term sustainable format? Valid V3 LTSF are
     * file extensions. WARNING. The format must be passed in as lower case
     * only.
     *
     * @param format format to be checked
     * @return true if a valid V3 LTSF
     */
    public boolean isV3LTSF(String format) {
        int i;
        String s;

        // go through list of file extensions
        for (i = 0; i < ltsf.size(); i++) {
            s = ltsf.get(i);
            if (format.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
