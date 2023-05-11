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
import java.util.Arrays;

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
        byte[] m = readFile(fileName);

        byte[] h = DerivedFunctions.KMACXOF256(new byte[0], m, 512, "D".getBytes());

        System.out.println("h: " + byteArrayToBinaryString(h));
    }

    /**
     * Computes an authentication tag.
     * @param pw the passphrase
     * @param fileName the file where the message is found.
     */
    public static void aTag(String pw, String fileName) throws IOException
    {
        byte[] m = readFile(fileName);

        byte[] t = DerivedFunctions.KMACXOF256(pw.getBytes(), m, 512, "T".getBytes());

        System.out.println("t: " + byteArrayToBinaryString(t));
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
        byte[] keka, ke, ka;
        byte[] t;
        byte[] c;
        byte[] m = readFile(fileName);

        z = random(512);
        keka = DerivedFunctions.KMACXOF256(z.concat(pw).getBytes(), new byte[0], 1024, "S".getBytes());
        ke = Arrays.copyOfRange(keka, 0, keka.length / 2);
        ka = Arrays.copyOfRange(keka, keka.length / 2, keka.length);
        c = IF.xorByteArrays(DerivedFunctions.KMACXOF256(ke, new byte[0], m.length, "SKE".getBytes()), m);
        t = DerivedFunctions.KMACXOF256(ka, m, 512, "SKA".getBytes());

        writeFile(z.getBytes(), c, t);

        System.out.println("File encrypted: cryptogram.txt");
    }

    public static void decrypt(String pw) throws IOException
    {
        String content;
        String[] zct;
        String z;
        byte[] c, t, tprime, m;
        byte[] keka, ke, ka;
        String fileName = "./cryptogram.txt";

        content = new String(readFile(fileName));

        zct = content.split("\n");
        z = zct[0];
        c = zct[1].getBytes();
        t = zct[2].getBytes();

        keka = DerivedFunctions.KMACXOF256(z.concat(pw).getBytes(), new byte[0], 1024, "S".getBytes());
        ke = Arrays.copyOfRange(keka, 0, keka.length / 2);
        ka = Arrays.copyOfRange(keka, keka.length / 2, keka.length);
        m = IF.xorByteArrays(DerivedFunctions.KMACXOF256(ke, new byte[0], c.length, "SKE".getBytes()), c);
        tprime = DerivedFunctions.KMACXOF256(ka, m, 512, "SKA".getBytes());

        try {   Files.write(Paths.get("./originalMessage.txt"), m);    }

        catch (IOException e) {  throw new RuntimeException(e);      }

        if (Arrays.equals(t, tprime)) System.out.println("File successfully decrypted: originalMessage.txt");
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
    public static void writeFile(byte[] z, byte[] c, byte[] t)
    {
        String outFile = "./cryptogram.txt";

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(z);
            outputStream.write('\n');
            outputStream.write(c);
            outputStream.write('\n');
            outputStream.write(t);
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
