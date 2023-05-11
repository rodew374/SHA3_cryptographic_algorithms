/**
 * FileName: Main.java
 * CreatedOn: May 05, 2023
 *
 * @author ZacInman
 * @version 1.0.050522
 */


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

/**
 *
 */
public class Main {

    public static void main(String[] args) throws IOException
    {
        String op;              // The operation to perform

        op = args[0];

        switch (op) {
            case "HASH" -> plainHash(args[1]);
            case "TAG" -> aTag(args[1], args[2]);
            case "ENCRYPT" -> encrypt(args[1], args[2]);
            case "DECRYPT" -> decrypt(args[1]);
            default -> System.out.println("Unknown command.");
        }
    }

    /**
     * Computes a plain cryptographic hash.
     * @param fileName the file where the message is found.
     */
    public static void plainHash(String fileName) throws IOException
    {
        String m = new String(readFile(fileName));

        String h = DF.KMACXOF256("", m, 512, "D");

        System.out.println("h: " + h);
    }

    /**
     * Computes an authentication tag.
     * @param pw the passphrase
     * @param fileName the file where the message is found.
     */
    public static void aTag(String pw, String fileName) throws IOException
    {
        String m = new String(readFile(fileName));

        String t = DF.KMACXOF256(pw, m, 512, "T");

        System.out.println("t: " + t);
    }

    /**
     * Encrypts a byte array symmetrically under a given passphrase.
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

        writeFile(z, c, t);

        System.out.println("File encrypted: cryptogram.txt");
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

        try {   Files.write(Paths.get("./originalMessage.txt"), m.getBytes());    }

        catch (IOException e) {  throw new RuntimeException(e);      }

        if (t.equals(tprime)) System.out.println("File successfully decrypted: originalMessage.txt");
        else System.out.println("t' does not equal t. File decryption complete.");


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
     * Write a text file called "cryptogram"
     * @param z random number
     * @param c cipher text?
     * @param t tag?
     */
    public static void writeFile(String z, String c, String t)
    {
        String outFile = "./cryptogram.txt";

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(z.getBytes());
            outputStream.write('\n');
            outputStream.write(c.getBytes());
            outputStream.write('\n');
            outputStream.write(t.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
