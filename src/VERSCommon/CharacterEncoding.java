/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERSCommon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This utility class converts XML documents in UTF-8 to UTF-16 or vice versa,
 * including converting the XML preamble (if any). The usage is: 'to-utf16'
 * utf8File.xml utf16File.xml or 'to-utf8' utf16File.xml utf8File.xml
 * Alternatively, you can convert a whole directory of utf8 or utf16 files to an
 * equivalent directory.
 *
 * Note, that it doesn't check to see if the file is actually what you think it
 * is.
 *
 * 20231129 1.0 First version
 * 20231204 1.1 Cleaned up code & looked at input file to determine char encoding
 *
 * @author Andrew
 */
public class CharacterEncoding {

    private final static Logger LOG = Logger.getLogger("VERSCommon.CharacterEncoding");

    public CharacterEncoding() {

    }

    /**
     * Convert a file from one encoding to the other. In addition to transcoding
     * the characters, it replaces the XML prefix (i.e. the line starting
     * <?xml...>) if one is found
     *
     * @param outCharEnc the desired output character encoding
     * @param in the file to be converted
     * @param out the converted file
     * @throws IOException
     */
    public void convertXML(String outCharEnc, Path in, Path out) throws IOException {
        byte[] ba = new byte[3];
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;
        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        String s;
        String guessInCoding;

        // Make a best guess as to what encoding the in file actually is...
        // This is rough as guts, but should work for XML files.
        fis = new FileInputStream(in.toFile());
        fis.read(ba, 0, 3);
        // System.out.println(ba[0] + " " + ba[1] + " " + ba[2]);
        if (ba[0] > 31 && ba[0] < 128) { // i.e. first character is a printable ASCII character
            guessInCoding = "UTF-8";
            LOG.log(Level.INFO, "{0} (Guess UTF 8)->{1} ({2})", new Object[]{in.toString(), out.toString(), outCharEnc});
        } else if (ba[0] == 0 || ba[1] == 0) { // no BOM, assuming the character is something likely such as a space, new line, or '<'
            guessInCoding = "UTF-16";
            LOG.log(Level.INFO, "{0} (Guess UTF 16 (no BOM))->{1} ({2})", new Object[]{in.toString(), out.toString(), outCharEnc});
        } else if (ba[0] == -1 || ba[1] == -2) { // with LE BOM, first two bytes will be 'FFFE'
            guessInCoding = "UTF-16";
            LOG.log(Level.INFO, "{0} (Guess UTF 16 LE (BOM))->{1} ({2})", new Object[]{in.toString(), out.toString(), outCharEnc});
        } else if (ba[1] == -2 || ba[1] == -1) { // with BE BOM, first two bytes will be 'FEFF'
            guessInCoding = "UTF-16";
            LOG.log(Level.INFO, "{0} (Guess UTF 16 BE (BOM))->{1} ({2})", new Object[]{in.toString(), out.toString(), outCharEnc});
        } else if (ba[0] == -17 && ba[1] == -69 && ba[2] == -65) { // UTF-8 BOM - first three bytes will be 'EFBBBF'
            guessInCoding = "UTF-8";
            LOG.log(Level.INFO, "{0} (Guess UTF 8 (BOM))->{1} ({2})", new Object[]{in.toString(), out.toString(), outCharEnc});
        } else { // give up
            LOG.log(Level.INFO, "{0}: Unrecognisable character encoding, not converting", new Object[]{in.toString()});
            return;
        }
        fis.close();

        // convert...
        fis = new FileInputStream(in.toFile());
        isr = new InputStreamReader(fis, guessInCoding);
        br = new BufferedReader(isr);
        fos = new FileOutputStream(out.toFile());
        osw = new OutputStreamWriter(fos, outCharEnc);
        bw = new BufferedWriter(osw);
        while ((s = br.readLine()) != null) {
            if (s.startsWith("<?xml")) {
                bw.write("<?xml version=\"1.0\" encoding=\"" + outCharEnc + "\" standalone=\"no\" ?>");
            } else {
                bw.write(s);
            }
            bw.write("\n");
        }
        bw.close();
        osw.close();
        fos.close();
        br.close();
        isr.close();
        fis.close();
    }

    /**
     * Traverse a directory tree, converting all the files found. The tree
     * structure and file names in the output is the same as in the input. Files
     * that are NOT text files are not converted (text files are those defined
     * as ending in .txt, .xml, or .veo
     *
     * @param inCharEnc the input character encoding
     * @param in the input file or directory
     * @param out the output file or directory
     * @throws IOException
     */
    public void convert(String inCharEnc, Path in, Path out) throws IOException {
        String filename;

        if (Files.isDirectory(in)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(in)) {
                for (Path entry : stream) {
                    filename = entry.getFileName().toString().toLowerCase();
                    if (filename.endsWith(".veo") || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                        convert(inCharEnc, entry, out.resolve(entry.getFileName()));
                    }
                }
            }
        } else {
            convertXML(inCharEnc, in, out);
        }

    }

    /**
     * Main program. The command line args are 0: either 'to-utf8' or 'to-utf16'
     * 1: the input file or directory, 2: the output file or directory.
     *
     * @param args
     */
    public static void main(String[] args) {
        CharacterEncoding enc = new CharacterEncoding();
        Path from, to;

        try {
            // set up the console handler for log messages and set it to output anything
            System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
            Handler[] hs = LOG.getHandlers();
            for (Handler h : hs) {
                h.setLevel(Level.FINEST);
                h.setFormatter(new SimpleFormatter());
            }
            LOG.setLevel(Level.FINEST);
            
            // check usage
            if (args.length != 3 || (args[0].compareToIgnoreCase("to-utf8") != 0 && args[0].compareToIgnoreCase("to-utf16") != 0)) {
                throw new Exception("Usage: encoding [to-utf8|to-utf16] infile outfile");
            }
            
            // get files and directories
            from = Paths.get(args[1]);
            to = Paths.get(args[2]);
            if ((Files.isDirectory(from) && !Files.isDirectory(to)) || (!Files.isDirectory(from) && Files.isDirectory(to))) {
                throw new Exception("Infile and outfile must be both files or directories");
            }
            
            // and... convert!
            if (args[0].compareToIgnoreCase("to-utf8") == 0) {
                enc.convert("UTF-8", from, to);
            } else {
                enc.convert("UTF-16", from, to);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Complete!");
    }
}
