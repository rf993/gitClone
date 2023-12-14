/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERSCommon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class sorts VEOs into a subdirectory structure depending on the errors
 * and warnings produced when analysing them.
 * 
 * 20231120 1.00 Created from class in neoVEO/V3Analysis
 *
 * @author Andrew
 */
public class AnalysisClassifyVEOs {
    private final static Logger LOG = Logger.getLogger("VERSCommon.AnalysisSortVEOs");
    Path classifyVEOsDir; // root directory into which the errors/warnings are classified

    public AnalysisClassifyVEOs(Path outputDir, String runDateTime) throws IOException {
        classifyVEOsDir = outputDir.resolve("Run-" + runDateTime.replaceAll(":", "-"));
        try {
            Files.createDirectory(classifyVEOsDir);
        } catch (IOException ioe) {
            throw new IOException("Failed to create VEO classification directory '" + classifyVEOsDir.toString() + "': " + ioe.getMessage());
        }

    }

    /**
     * Classify the VEO into the appropriate outcome directories. If there are
     * no warnings or errors, the VEO is put in the OK-NoProblems directory
     * 
     * @param veo the VEO
     * @param errors the list of errors that occurred in processing the VEO
     * @param warnings the list of warnings that occurred in processing the VEO
     */
    public void classifyVEO(Path veo, ArrayList<VEOFailure> errors, ArrayList<VEOFailure> warnings) {
        int i;
        
        // if no errors or warnings, put in 'NoProblems' directory
        if (errors.isEmpty() && warnings.isEmpty()) {
            linkVEOinto(veo, classifyVEOsDir, "OK-NoProblems", null);
        }

        // go through errors
        for (i = 0; i < errors.size(); i++) {
            linkVEOinto(veo, classifyVEOsDir, "E-" + errors.get(i).getFailureId(), errors.get(i).getMessage());
        }

        // go through warnings
        for (i = 0; i < warnings.size(); i++) {
            linkVEOinto(veo, classifyVEOsDir, "W-" + warnings.get(i).getFailureId(), warnings.get(i).getMessage());
        }
    }

    /**
     * Hard link the given VEO into the specified classDir directory in the
     * outputDir directory.
     *
     * @param veo the path of the VEO being tested
     * @param outputDir the directory in which to build the classification
     * @param classDir the error identification that occurred
     * @param mesg a text description of this error
     */
    private void linkVEOinto(Path veo, Path outputDir, String classDir, String mesg) {
        Path pd, source;
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;

        // get class directory, creating if necessary & add the description of the error
        pd = outputDir.resolve(classDir);
        if (!Files.exists(pd)) {
            try {
                Files.createDirectories(pd);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Failed creating class directory ''{0}'': {1}", new Object[]{pd.toString(), ioe.getMessage()});
                return;
            }
            if (mesg != null) {
                try {
                    fos = new FileOutputStream(pd.resolve("ErrorMessage.txt").toFile());
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);
                    bw.write("A typical message for this class of error/warning is: \n\n");
                    bw.write(mesg);
                    bw.close();
                    osw.close();
                    fos.close();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Failed creating description of error: {0}", ioe.getMessage());
                    return;
                }
            }
        }
        if (!Files.isDirectory(pd)) {
            LOG.log(Level.WARNING, "Class directory ''{0}'' exists, but is not a directory", pd.toString());
            return;
        }

        source = pd.resolve(veo.getFileName());

        // test to see if link already exists
        if (Files.exists(source)) {
            return;
        }

        // hard link VEO into class directory
        try {
            Files.createLink(source, veo);
        } catch (IOException | UnsupportedOperationException ioe) {
            if (ioe.getMessage().trim().endsWith("Incorrect function.")) {
                LOG.log(Level.WARNING, "Failed linking ''{0}'' to ''{1}'': Might be because the file system containing the output directory is not NTSF", new Object[]{pd.resolve(veo.getFileName()), veo.toString()});
            } else if (ioe.getMessage().trim().endsWith("different disk drive.")) {
                LOG.log(Level.WARNING, "Failed linking ''{0}'' to ''{1}'': Might be because the VEOs and the output directory are on different file systems", new Object[]{pd.resolve(veo.getFileName()), veo.toString()});
            } else {
                LOG.log(Level.WARNING, "Failed linking ''{0}'' to ''{1}'': {2}", new Object[]{pd.resolve(veo.getFileName()), veo.toString(), ioe.getMessage()});
            }
        }
    }

    /**
     * Rename the classification directories to include the count of the VEOs
     * contained within them.
     * 
     * @throws IOException If the renaming didn't work
     */
    public void includeCount() throws IOException {
        DirectoryStream<Path> ds, ds1;
        String s;
        int i;

        try {
            ds = Files.newDirectoryStream(classifyVEOsDir);
            for (Path entry : ds) {
                ds1 = Files.newDirectoryStream(entry);
                i = 0;
                for (Path v : ds1) {
                    s = v.getFileName().toString().toLowerCase();
                    if (s.endsWith(".veo.zip") || s.endsWith(".veo")) {
                        i++;
                    }
                }
                s = entry.getFileName().toString();
                if (s.startsWith("E-")) {
                    s = "E-" + i + "-" + s.substring(2);
                    Files.move(entry, classifyVEOsDir.resolve(s));
                } else if (s.startsWith("W-")) {
                    s = "W-" + i + "-" + s.substring(2);
                    Files.move(entry, classifyVEOsDir.resolve(s));
                } else if (s.startsWith("OK")) {
                    s = "OK-" + i + "-" + s.substring(3);
                    Files.move(entry, classifyVEOsDir.resolve(s));
                }
            }
        } catch (IOException ioe) {
            throw new IOException("Failed renaming a classification directory: " + ioe.getMessage());
        }
    }
}
