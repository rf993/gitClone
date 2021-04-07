/*
 * Copyright Public Record Office Victoria 2005, 2015
 * Licensed under the CC-BY license http://creativecommons.org/licenses/by/3.0/au/
 * Author Andrew Waugh
 * Version 1.0 February 2015
 */
package VERSCommon;

/**
 * This class encapsulates public key cryptography information about a user. The
 * information is read in from a PFCS#12 file (typically having the file
 * extension .pfx)
 */
import java.security.*;
import java.security.cert.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * This class encapsulates information about a user (primarily key information).
 * The information is read in from a PFCS#12 file (file extension .pfx)
 */
public final class PFXUser {

    static String classname = "PFXUser";
    KeyStore ks;
    char[] password;
    String alias;
    java.security.cert.Certificate[] certificateChain;
    PublicKey pubKey;
    PrivateKey priKey;
    String pfxFile;

    String userId;
    String userDesc;
    String keyPhrase;
    private final static Logger log = Logger.getLogger("veocreate.PFXUser");

    /**
     * Open a PFX file and read the contents
     *
     * @param pfxfile The PFX file to open
     * @param passwd The password that secures the PFX file
     * @throws VEOFatal if an error occurs in processing the PFX file
     */
    public PFXUser(String pfxfile, String passwd) throws VEOFatal {
        FileInputStream fis;
        Enumeration<String> aliases;
        int i;

        // sanity check
        if (pfxfile == null) {
            throw new VEOFatal(classname, 1, "Passed PFX file is null");
        }
        if (passwd == null) {
            throw new VEOFatal(classname, 2, "Passed password is null");
        }

        // get PKCS12 keystore
        try {
            ks = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            throw new VEOFatal(classname, 3, "Failed to open PKCS12 keystore: " + e.getMessage());
        }

        // open pfx (pkcs12) file
        this.pfxFile = pfxfile;
        fis = null;
        try {
            fis = new FileInputStream(pfxfile);
        } catch (FileNotFoundException e) {
            throw new VEOFatal(classname, 4, "PFX file '" + pfxfile + "' was not found");
        }

        // copy password to array of characters
        password = new char[passwd.length()];
        for (i = 0; i < passwd.length(); i++) {
            password[i] = passwd.charAt(i);
        }

        // extract details from PFX file
        try {
            ks.load(fis, password);
        } catch (IOException e) {
            if (e.toString().contains("failed to decrypt safe contents entry")) {
                throw new VEOFatal(classname, 5, "Error loading PFX file - the probable cause is an incorrect password");
            }
            throw new VEOFatal(classname, 6, "failed to load PFX file: " + e.getMessage());
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new VEOFatal(classname, 7, "failed to load PFX file: " + e.getMessage());
        }

        // check to see how many private keys this PKCS file holds
        try {
            aliases = ks.aliases();
            i = 0;
            while (aliases.hasMoreElements()) {
                i++;
                aliases.nextElement();
            }
            if (i == 0) {
                throw new VEOFatal(classname, 8, "PFXUser(): No private key in PFX file");
            }
            if (i > 1) {
                log.log(Level.WARNING, "More than one private key in PFX file. First key will be used.");
            }
            aliases = ks.aliases();
            alias = aliases.nextElement();
        } catch (KeyStoreException e) {
            throw new VEOFatal(classname, 9, "Keystore exception: " + e.getMessage());
        }

        // get first private key
        try {
            aliases = ks.aliases();
            alias = aliases.nextElement();
            if (!ks.isKeyEntry(alias)) {
                throw new VEOFatal(classname, 10, "PFX file does not contain a key entry as the first entry");
            }
        } catch (KeyStoreException e) {
            throw new VEOFatal(classname, 11, "Keystore exception: " + e.getMessage());
        }

        // get the certificate chain
        try {
            certificateChain = ks.getCertificateChain(alias);
        } catch (KeyStoreException e) {
            throw new VEOFatal(classname, 11, "Keystore exception: " + e.getMessage());
        }

        // get the public and private key
        try {
            priKey = (PrivateKey) ks.getKey(alias, password);
        } catch (KeyStoreException |
                NoSuchAlgorithmException |
                UnrecoverableKeyException e) {
            throw new VEOFatal(classname, 12, "Failed to retrieve private key from PFX file: " + e.getMessage());
        }
        pubKey = (getX509Certificate().getPublicKey());
    }

    /**
     * Get String representation of user details.
     *
     * @return String representation of user details
     */
    @Override
    public String toString() {
        StringBuffer sb;
        int i;

        sb = new StringBuffer();
        sb.append(pubKey.toString());
        sb.append("\n");
        sb.append(priKey.toString());
        sb.append("\n");
        sb.append("Certificate Chain:\n");
        for (i = 0; i < certificateChain.length; i++) {
            sb.append(getX509CertificateFromChain(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Return the pfx file that was read
     * @return the file name that was originally passed in
     */
    public String getFileName() {
        return pfxFile;
    }
    
    /**
     * Return the certificate.
     *
     * @return a byte array containing the certificate
     */
    public byte[] getCertificate() {
        byte[] b;

        b = null;
        try {
            b = certificateChain[0].getEncoded();
        } catch (CertificateEncodingException e) {
            log.log(Level.WARNING, "PFXUser.getCertificate(){0} {1}", new Object[]{e.toString(), e.getMessage()});
        }
        return b;
    }

    /**
     * Get the first X509 Certificate.
     *
     * @return an X509 certificate
     */
    public X509Certificate getX509Certificate() {
        return (X509Certificate) certificateChain[0];
    }

    /**
     * Return the certificate chain as a vector
     *
     * @return certificate chain as an ArrayList
     */
    public ArrayList<byte[]> getCertificateChain() {
        ArrayList<byte[]> al;
        int i;

        al = new ArrayList<>();
        for (i = 0; i < certificateChain.length; i++) {
            al.add(getCertificateFromChain(i));
        }
        return al;
    }

    /**
     * Return the size of the certificate chain
     *
     * @return an integer
     */
    public int getCertificateChainLength() {
        return certificateChain.length;
    }

    /**
     * Return an element from the certificate chain.
     *
     * @param i the index of the certificate in the chain (first = 0)
     * @return a byte array containing the certificate
     */
    public byte[] getCertificateFromChain(int i) {
        byte[] b;

        if (i >= certificateChain.length) {
            return null;
        }
        b = null;
        try {
            b = certificateChain[i].getEncoded();
        } catch (CertificateEncodingException e) {
            log.log(Level.WARNING, "PFXUser(){0} {1}", new Object[]{e.toString(), e.getMessage()});
        }
        return b;
    }

    /**
     * Return an element from the certificate chain as an X509 Certificate.
     *
     * @param i the index of the certificate in the chain (first = 0)
     * @return a byte array containing the certificate
     */
    public X509Certificate getX509CertificateFromChain(int i) {
        if (i >= certificateChain.length) {
            return null;
        }
        return (X509Certificate) certificateChain[i];
    }

    /**
     * Return the public and private keys.
     *
     * @return a KeyPair containing the two keys
     */
    public KeyPair getKeyPair() {
        return new KeyPair(pubKey, priKey);
    }

    /**
     * Return the private key.
     *
     * @return the PrivateKey
     */
    public PrivateKey getPrivate() {
        return priKey;
    }

    /**
     * Return the public key.
     *
     * @return the PublicKey
     */
    public PublicKey getPublic() {
        return pubKey;
    }

    /**
     * Get user description.
     *
     * @return a string describing the user
     */
    public String getUserDesc() {
        return userDesc;
    }

    /**
     * Check key phrase.
     *
     * @param s the KeyPhrase to check
     * @return always returns true
     */
    public boolean checkKeyPhrase(String s) {
        return true;
    }

    /**
     * Get user name.
     *
     * @return not implemented. Always returns "not specified"
     */
    public String getUserId() {
        return "not specified";
    }

    /**
     * Get vector of OIDs for user's distinguished name. Not implemented. Will
     * cause program to fail if called.
     *
     * @return not implemented
     */
    public ArrayList<String> getNameOID() {
        log.log(Level.WARNING, "PFXUser.getNameOID() not implemented");
        return null;
    }

    /**
     * Get vector of values for user's distinguished name. Not implemented. Will
     * cause program to fail if called.
     *
     * @return not implemented
     */
    public ArrayList<String> getNameValue() {
        log.log(Level.WARNING, "PFXUser.getNameValue() not implemented");
        return null;
    }

    /**
     * Generate a new user object
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        PFXUser pfxu;

        if (args.length != 1) {
            System.err.println("Usage: User <file>");
            System.exit(-1);
        }
        try {
            pfxu = new PFXUser(args[0], null);
            System.out.println(pfxu.toString());
        } catch (VEOFatal e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
    }
}
