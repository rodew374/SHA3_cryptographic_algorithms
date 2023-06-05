/**
 * FileName: Main.java
 * CreatedOn: May 05, 2023
 *
 * @author ZacInman
 * @version 2.0.060223
 */


import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Scanner;

public class Main {

    /**
     * Entry point for the application.
     */
    public static void main(String[] args) throws IOException
    {
        String op;              // The operation to perform

        op = args[0];

        switch (op) {
            case "HASH" -> plainHash(args[1]);
            case "TAG" -> aTag(args[1], args[2]);
            case "ENCRYPT" -> encrypt(args[1], args[2]);
            case "DECRYPT" -> decrypt(args[1]);
            case "KEY" -> keyPair(args[1]);
            case "ECENCRYPT" -> ec_encrypt(args[1], args[2]);
            case "ECDECRYPT" -> ec_decrypt(args[1], args[2]);
            case "SIGN" -> sign(args[1], args[2]);
            case "VERIFY" -> verify(args[1], args[2], args[3]);
            default -> System.out.println("Unknown command.");
        }
    }

    /**
     * Computes an authentication tag.
     * If txt = "-u", user will be prompted for the message.
     * Otherewise, txt is the file where the message is found.
     * @param pw the passphrase
     * @param txt either the file where the message is found or "-u".
     */
    public static void aTag(String pw, String txt) throws IOException
    {
        String m;

        if (txt.equals("-u"))
        {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter message: ");
            m = scan.nextLine();
        }
        else m = new String(readFile(txt));

        String t = DF.KMACXOF256(pw, m, 512, "T");

        System.out.println("t: " + t);
    }

    /**
     * Converts a byte array to a string.
     * @param byteArray the array to be converted
     * @return the string output
     */
    public static String byteArrayToBinaryString(byte[] byteArray) {
        StringBuilder binaryStringBuilder = new StringBuilder();
        for (byte b : byteArray) {
            String binaryString = Integer.toBinaryString(b & 0xFF);
            while (binaryString.length() < 8) {
                binaryString = "0".concat(binaryString);
            }
            binaryStringBuilder.append(binaryString);
        }
        return binaryStringBuilder.toString();
    }

    public static void decrypt(String pw) throws IOException
    {
        String content;
        String[] zct;
        String z;
        String c, t, tprime, m;
        String keka, ke, ka;
        String fileName = "./cryptogram.txt";

        content = new String(readFile(fileName));

        zct = content.split("\n");
        z = zct[0];
        c = zct[1];
        t = zct[2];

        keka = DF.KMACXOF256(z.concat(pw), "", 1024, "S");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2);
        m = IF.xorStrings(DF.KMACXOF256(ke, "", c.length(), "SKE"), c);
        tprime = DF.KMACXOF256(ka, m, 512, "SKA");

        writeFile("./originalMessage.txt", m);

        if (t.equals(tprime)) System.out.println("File successfully decrypted: originalMessage.txt");
        else System.out.println("t' does not equal t. File decryption complete.");
    }

    /**
     * Encrypts a data file symmetrically under a given passphrase.
     * @param pw the passphrase
     * @param fileName the file name to be read
     * @throws IOException if file does not exist
     */
    public static void encrypt(String pw, String fileName) throws IOException
    {
        String z;               // Random binary string
        String keka, ke, ka;
        String t;
        String c;
        String m = new String(readFile(fileName));

        z = random(512);
        keka = DF.KMACXOF256(z.concat(pw), "", 1024, "S");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2);
        c = IF.xorStrings(DF.KMACXOF256(ke, "", m.length(), "SKE"), m);
        t = DF.KMACXOF256(ka, m, 512, "SKA");

        writeFile("./cryptogram.txt", z + "\n" + c + "\n" + t);

        System.out.println("File encrypted: cryptogram.txt");
    }

    /**
     * Helper method to encrypt the elliptic private key.
     *
     * @param pw the passphrase the key is encrypted under.
     * @param key the private key to be encrypted.
     * @return the cryptogram (z, c, t)
     */
    public static String encryptKey(String pw, BigInteger key)
    {
        String z;               // Random binary string
        String keka, ke, ka;
        String t;
        String c;

        z = random(512);
        keka = DF.KMACXOF256(z.concat(pw), "", 1024, "S");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2);
        c = IF.xorStrings(DF.KMACXOF256(ke, "", key.bitLength(), "SKE"), key.toString(2));
        t = DF.KMACXOF256(ka, key.toString(2), 512, "SKA");

        return z + "\n" + c + "\n" + t;
    }

    /**
     * Decrypts a given elliptic-encrypted file from a given passphrase and
     * writes the decrypted data to a file.
     *
     * @param pw the passphrase.
     * @param fileName the elliptic cryptogram.
     * @throws IOException if the cryptogram cannot be found.
     */
    public static void ec_decrypt(String pw, String fileName) throws IOException
    {
        String[] content;
        EC.Point Z, W;
        String c, t, tprime, m;
        String keka, ke, ka;
        BigInteger s;

        content = new String(readFile(fileName)).split("\n");

        Z = new EC.Point(new BigInteger(content[0], 2), new BigInteger(content[1], 2));
        c = content[2];
        t = content[3];

        s = new BigInteger(DF.KMACXOF256(pw, "", 512, "SK"), 2);
        s = new BigInteger("4").multiply(s);

        W = Z.mult(s);

        keka = DF.KMACXOF256(W.x.toString(2), "", 1024, "PK");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2);

        m = IF.xorStrings(DF.KMACXOF256(ke, "", c.length(), "PKE"), c);
        tprime = DF.KMACXOF256(ka, m, 512, "PKA");

        writeFile("./originalMessage-ec.txt", m);

        if (t.equals(tprime)) System.out.println("File successfully decrypted: originalMessage-ec.txt");
        else System.out.println("t' does not equal t. File decryption unsuccessful: originalMessage-ec.txt");
    }

    /**
     * Encrypts data under a given elliptic public key file and writes
     * the ciphertext to a file.
     * If txt = "-u", user will be prompted for the message.
     * Otherwise, txt is the file where the message is found.
     *
     * @param keyFile file where the elliptic public key is found.
     * @param txt the data to be encrypted.
     * @throws IOException if public key file cannot be found.
     */
    public static void ec_encrypt(String keyFile, String txt) throws IOException
    {
        EC.Point W;
        EC.Point Z;
        EC.Point V;
        BigInteger k;
        String c;
        String t;
        String m;
        String keka, ke, ka;

        if (txt.equals("-u"))
        {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter message: ");
            m = scan.nextLine();
        }
        else m = new String(readFile(txt));

        V = EC.O;

        try{
            Scanner scan = new Scanner(new File(keyFile));
            V = new EC.Point(new BigInteger(scan.nextLine(), 2), new BigInteger(scan.nextLine(), 2));
        } catch (FileNotFoundException e)
        {
            System.out.println("Public key file not found.");
            System.exit(0);
        }

        k = new BigInteger(random(512), 2);
        k = new BigInteger("4").multiply(k);

        W = V.mult(k);
        Z = EC.G.mult(k);

        keka = DF.KMACXOF256(W.x.toString(2), "", 1024, "PK");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2);

        c = IF.xorStrings(DF.KMACXOF256(ke, "", m.length(), "PKE"), m);
        t = DF.KMACXOF256(ka, m, 512, "PKA");

        writeFile("./ec-cryptogram.txt", Z.x.toString(2) + "\n" + Z.y.toString(2) + "\n" + c + "\n" + t);

        System.out.println("Message encrypted: ec-cryptogram.txt");
    }

    /**
     * Generates an elliptic key pair from a given passphrase.
     * Writes the public and private keys, each to their own file.
     *
     * @param pw the passphrase
     */
    public static void keyPair(String pw)
    {
        BigInteger s;           // Private Key
        EC.Point V;             // Public Key

        s = new BigInteger(DF.KMACXOF256(pw, "", 512, "SK"), 2);
        s = new BigInteger("4").multiply(s);

        V = EC.G.mult(s);

        writeFile("./public.key", V.x.toString(2) + "\n" + V.y.toString(2));
        System.out.println("Public key generated: public.key");

        writeFile("./private.key", encryptKey(pw, s));
        System.out.println("Private key encrypted: private.key");
    }

    /**
     * Computes a plain cryptographic hash.
     * If txt = "-u", user will be prompted for the message.
     * Otherewise, txt is the file where the message is found.
     * @param txt either the file where the message is found or "-u".
     */
    public static void plainHash(String txt) throws IOException
    {
        String h, m;

        if (txt.equals("-u"))
        {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter message: ");
            m = scan.nextLine();
        }
        else m = new String(readFile(txt));

        h = DF.KMACXOF256("", m, 512, "D");

        System.out.println("h: " + h);
    }

    /**
     * Signs a message from a given passphrase and
     * writes the signature to a file.
     * If txt = "-u", user will be prompted for the message.
     * Otherewise, txt is the file where the message is found.
     *
     * @param pw the passphrase.
     * @param txt the message.
     * @throws IOException if txt is a file and cannot be found.
     */
    public static void sign(String pw, String txt) throws IOException
    {
        String h, m;
        BigInteger s, k, z;
        EC.Point U;

        if (txt.equals("-u"))
        {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter message: ");
            m = scan.nextLine();
        }
        else m = new String(readFile(txt));

        s = new BigInteger(DF.KMACXOF256(pw, "", 512, "SK"), 2);
        s = new BigInteger("4").multiply(s);

        k = new BigInteger(DF.KMACXOF256(s.toString(2), m, 512, "N"), 2);
        k = new BigInteger("4").multiply(k);

        U = EC.G.mult(k);

        h = DF.KMACXOF256(U.x.toString(2), m, 512, "T");
        z = k.subtract(new BigInteger(h).multiply(s)).mod(EC.r);

        writeFile("./signature.txt", h + "\n" + z.toString(2));

        System.out.println("Message signed: signature.txt");
    }

    /**
     * Verifies a message, and it's signature file, under a given public key file.
     *
     * @param keyFile file where the elliptic public key is found.
     * @param fileName file where the message is found.
     * @param signature file where the message's signature is found.
     * @throws IOException if any of the given files could not be found.
     */
    public static void verify(String keyFile, String fileName, String signature) throws IOException
    {
        String[] sigContent, keyContent;
        EC.Point U, V;
        String h, hprime, m;
        BigInteger z;

        m = new String(readFile(fileName));

        sigContent = new String(readFile(signature)).split("\n");
        h = sigContent[0];
        z = new BigInteger(sigContent[1], 2);

        keyContent = new String(readFile(keyFile)).split("\n");
        V = new EC.Point(new BigInteger(keyContent[0], 2), new BigInteger(keyContent[1], 2));

        U = EC.G.mult(z).add(V.mult(new BigInteger(h, 2)));

        hprime = DF.KMACXOF256(String.valueOf(U.x), m, 512, "T");

        if (h.equals(hprime)) System.out.println("Verification complete. Signature accepted.");
        else System.out.println("Verification incomplete. Signature rejected.");
    }

    /**
     * Generates a random number of bit length L utilizing
     * the Java SecureRandom class. Output is a binary string.
     * @param L the requested length of the random number.
     * @return the binary string number.
     */
    public static String random(int L)
    {
        SecureRandom random;
        BigInteger num;
        byte[] bytes;

        random = new SecureRandom();
        bytes = new byte[L / 8];

        random.nextBytes(bytes);

        num = new BigInteger(1, bytes);

        return num.toString(2);
    }

    /**
     * Reads the contents of a file.
     * @param fileName name of the text file to be read.
     * @throws IOException if the file does not exist.
     */
    public static byte[] readFile(String fileName) throws IOException
    {
        return Files.readAllBytes(Paths.get(fileName));
    }

    /**
     * Write a text file.
     *
     * @param fileName the name of the file to be written.
     * @param message the contents of the file to be written.
     */
    public static void writeFile(String fileName, String message)
    {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName))) {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
