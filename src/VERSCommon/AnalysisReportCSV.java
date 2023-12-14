/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERSCommon;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * This class reports the results of a V2 or V3 Analysis as a CSV file.
 * 
 * 20231120 1.00 Created from class in neoVEO/V3Analysis
 * 20231214 1.01 Added proper quoting of values as per RFC4180
 *
 * @author Andrew
 */
public class AnalysisReportCSV {
    FileOutputStream fos;
    OutputStreamWriter osw;
    BufferedWriter csvReportW;

    /**
     * Constructor.
     * 
     * @param outputDir directory where to create the CSV file (cannot be null)
     * @param runDateTime the current data/time (cannot be null)
     * @throws IOException 
     */
    public AnalysisReportCSV(Path outputDir, String runDateTime) throws IOException {
        assert outputDir != null;
        assert runDateTime != null;
        
        Path p = outputDir.resolve("Results-" + runDateTime.replaceAll(":", "-") + ".csv");
        try {
            fos = new FileOutputStream(p.toFile());
        } catch (FileNotFoundException fnfe) {
            throw new IOException("Failed attempting to open the TSV report file: " + fnfe.getMessage());
        }
        osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
        csvReportW = new BufferedWriter(osw);
    }

    /**
     * Output a collection of errors and warnings to the CSV file
     *
     * @param veo The veo being tested (cannot be null)
     * @param errors The errors generated (cannot be null)
     * @param warnings The warnings generated (cannot be null)
     * @throws java.io.IOException if something went wrong
     */
    public void write(Path veo, ArrayList<VEOFailure> errors, ArrayList<VEOFailure> warnings) throws IOException {
        int i;

        assert veo != null;
        assert errors != null;
        assert warnings != null;
        
        if (csvReportW == null) {
            throw new IOException("Attempting to write to TSV report file before it was opened");
        }
        try {
            if (errors.isEmpty() && warnings.isEmpty()) {
                writeCSVReportLine(veo, null, false);
            } else {
                for (i = 0; i < errors.size(); i++) {
                    writeCSVReportLine(veo, errors.get(i), true);
                }
                for (i = 0; i < warnings.size(); i++) {
                    writeCSVReportLine(veo, warnings.get(i), false);
                }
            }
            csvReportW.flush();
        } catch (IOException ioe) {
            throw new IOException("Failed writing to TSV report. Cause: " + ioe.getMessage());
        }
    }

    /**
     * Write a line in the CSV Report
     *
     * @param veo the VEO being reported on (cannot be null)
     * @param error true if an error occurred, false if it is a warning
     * @param ve the error/warning that occurred
     * @throws IOException
     */
    private void writeCSVReportLine(Path veo, VEOFailure ve, boolean error) throws IOException {
        assert veo != null;
        outputCSVValue(veo.getFileName().toString(), ",");
        if (ve != null) {
            if (error) {
                outputCSVValue("Error", ",");
            } else {
                outputCSVValue("Warning", ",");
            }
            outputCSVValue(ve.getFailureId(), ",");
            outputCSVValue(ve.getMessage(), ",");
        } else {
            outputCSVValue("OK", ",");
            outputCSVValue("", ",");
            outputCSVValue("", ",");
        }
        outputCSVValue(veo.toString(), "\n");
    }

    /**
     * Output a CSV value, respecting the restrictions of RFC 4180. Specifically
     * values that contains a comma, double quote, new line, or carriage
     * return must be enclosed in double quotes. Any value enclosed in double
     * quotes must have the comma and double quote characters quoted with a
     * preceding double quote. Lines must end MS-DOS style with a '\r\n'.
     * 
     * @param s the string value
     * @param delim the delimiter to write after the value (comma or new line)
     */
    private void outputCSVValue(String s, String delim) throws IOException {
        StringBuilder sb = new StringBuilder();
        int i;
        char c;

        if (s != null) {
            if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
                sb.append("\"");
                for (i = 0; i < s.length(); i++) {
                    c = s.charAt(i);
                    switch (c) {
                        case ',':
                            sb.append("\",");
                            break;
                        case '\"':
                            sb.append("\"\"");
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                sb.append("\"");
                csvReportW.write(sb.toString());
            } else {
                csvReportW.write(s);
            }
        }
        if (delim.equals("\n")) {
            csvReportW.write("\r");
        }
        csvReportW.write(delim);
    }

    /**
     * Close the CSV file
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        try {
            if (csvReportW != null) {
                csvReportW.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (fos != null) {
                fos.close();
            }
        } catch (IOException ioe) {
            throw new IOException("Failed to close the TSV report: {0}" + ioe.getMessage());
        }
    }

}
