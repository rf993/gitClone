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
import java.util.logging.Level;

/**
 * This class reports the results of a V2 or V3 Analysis as a CSV file
 *
 * @author Andrew
 */
public class AnalysisReportCSV {
    FileOutputStream fos;
    OutputStreamWriter osw;
    BufferedWriter csvReportW;

    public AnalysisReportCSV(Path outputDir, String runDateTime) throws IOException {
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
     * @param veo The veo being tested
     * @param errors The errors generated
     * @param warnings The warnings generated
     * @throws java.io.IOException if something went wrong
     */
    public void write(Path veo, ArrayList<VEOFailure> errors, ArrayList<VEOFailure> warnings) throws IOException {
        int i;
        
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
            throw new IOException("Failed writing to TSV report. Cause: "+ioe.getMessage());
        }
    }
    

    /**
     * Write a line in the TSV Report
     *
     * @param veo the VEO being reported on
     * @param error true if an error occurred, false if it is a warning
     * @param ve the error/warning that occurred
     * @throws IOException
     */
    public void writeCSVReportLine(Path veo, VEOFailure ve, boolean error) throws IOException {
        csvReportW.write(veo != null ? veo.getFileName().toString() : "");
        csvReportW.write(',');
        if (ve != null) {
            if (error) {
                csvReportW.write("Error,");
            } else {
                csvReportW.write("Warning,");
            }
            csvReportW.write(ve.getFailureId());
            csvReportW.write(',');
            csvReportW.write(ve.getMessage());
        } else {
            csvReportW.write("OK,,");
        }
        csvReportW.write(',');
        csvReportW.write(veo != null ? veo.toString() : "");
        csvReportW.write("\n");
    }

    /**
     * Close the CSV file
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
            throw new IOException("Failed to close the TSV report: {0}"+ioe.getMessage());
        }
    }

}
