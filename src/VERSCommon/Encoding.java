/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VERSCommon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
 *
 * @author Andrew
 */
public class Encoding {

    public Encoding() {

    }

    /**
     * Convert a file from one encoding to the other. In addition to transcoding
     * the characters, it replaces the XML prefix (i.e. the line starting
     * <?xml...>
     *
     * @param inCharEnc the input character encoding
     * @param in the file to be converted
     * @param out the converted file
     * @throws IOException
     */
    public void convertXML(String inCharEnc, Path in, Path out) throws IOException {
        ByteArrayInputStream bais;
        byte[] ba = new byte[2];
        FileOutputStream fos;
        OutputStreamWriter osw;
        BufferedWriter bw;
        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        String s;
        
        // test to see what encoding the in XML file actually is...
        fis = new FileInputStream(in.toFile());
        fis.read(ba, 0 , 2);
        if (ba[0] == 60 && ba[1] == 63) {
            System.out.println("UTF8 (or ASCII)");
        } else if (ba[0] == -2 && ba[1] == -1) {
            System.out.println("UTF16 BE BOM");
        }
        System.out.println("byte 0: "+ba[0]+" byte 1: "+ba[1]);
        fis.close();
        

        System.out.println("Converting to " + inCharEnc + " from:" + in.toString() + " resulting in: " + out.toString());
        fis = new FileInputStream(in.toFile());
        isr = new InputStreamReader(fis, inCharEnc);
        br = new BufferedReader(isr);
        fos = new FileOutputStream(out.toFile());
        if (inCharEnc.equals("UTF-8")) {
            osw = new OutputStreamWriter(fos, "UTF-16");
        } else {
            osw = new OutputStreamWriter(fos, "UTF-8");
        }
        bw = new BufferedWriter(osw);
        while ((s = br.readLine()) != null) {
            if (s.startsWith("<?xml")) {
                if (inCharEnc.equals("UTF-8")) {
                    s = "<?xml version=\"1.0\" encoding=\"UTF-16\" standalone=\"no\" ?>";
                } else {
                    s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>";
                }

            }
            bw.write(s);
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
        Encoding enc = new Encoding();
        Path from, to;

        try {
            if (args.length != 3 || (args[0].compareToIgnoreCase("to-utf8") != 0 && args[0].compareToIgnoreCase("to-utf16") != 0)) {
                throw new Exception("Usage: encoding [to-utf8|to-utf16] infile outfile");
            }
            from = Paths.get(args[1]);
            to = Paths.get(args[2]);
            if ((Files.isDirectory(from) && !Files.isDirectory(to)) || (!Files.isDirectory(from) && Files.isDirectory(to))) {
                throw new Exception("Infile and outfile must be both files or directories");
            }
            if (args[0].compareToIgnoreCase("to-utf8") == 0) {
                enc.convert("UTF-16", from, to);
            } else {
                enc.convert("UTF-8", from, to);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Complete!");
    }
}
