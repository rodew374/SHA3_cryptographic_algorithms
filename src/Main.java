/**
 * FileName: Main.java
 * CreatedOn: May 05, 2023
 *
 * @author ZacInman
 * @version 1.0.050522
 */


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
        byte[] m;               // The message byte array
        String op;              // The operation to perform
        String h;               // Cryptographic hash
        String pw;              // Passphrase
        String z;               // Random binary string
        String keka, ke, ka;
        String t;
        String c;

        op = args[0];

        switch (op)
        {
            case "HASH":
                plainHash(args[1]);
                break;

            case "TAG":
                aTag(args[1], args[2]);
                break;

            case "ENCRYPT":
                encrypt(args[1], args[2]);
                break;

            case "DECRYPT":
                decrypt(args[1], args[2]);
                break;

            default:
                System.out.println("Unknown command.");
                break;
        }
        // Computing cryptographic hash
        h = DerivedFunctions.KMACXOF256("", new String(m), 512, "D");

        // Computing authentication tag
        t = DerivedFunctions.KMACXOF256(pw, new String(m), 512, "T");

        // Symmetrical encryption
        z = random(512);
        keka = DerivedFunctions.KMACXOF256(z.concat(pw), "", 1024, "S");
        ke = keka.substring(0, keka.length() / 2);
        ka = keka.substring(keka.length() / 2, keka.length());
        c = InternalFunctions.xorStrings(DerivedFunctions.KMACXOF256(ke, "", m.length, "SKE"), new String(m));
        t = DerivedFunctions.KMACXOF256(ka, new String(m), 512, "SKA");

        // Symmetrical Decryption
    }

    public static void plainHash(String m)
    {

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
     * Reads the contents of a file into the message.
     * @param fileName name of the text file to be read.
     * @throws IOException if the file does not exist.
     */
    public static byte[] readMessage(String fileName) throws IOException
    {
        return Files.readAllBytes(Paths.get(fileName));
    }
}
