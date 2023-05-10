/**
 * FileName: InternalFunctions.java
 * CreatedOn: April 28, 2023
 *
 * @author ZacInman
 * @version 1.2.050522
 */

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Internal functions.
 */
class IF
{
    /**
     * Appends two bits to the end of a byte array.
     * @param arr the byte array
     * @param b the two bits to be appended
     * @return the resultant byte array
     */
    static byte[] append2bits(byte[] arr, byte b)
    {
        byte[] out = new byte[arr.length + 1];

        System.arraycopy(arr, 0, out, 0, arr.length);
        out[arr.length] = (byte) ((b & 0x03) << 6);

        return out;
    }

    /**
     * Appends two bits to the end of a byte array.
     * @param arr the byte array
     * @param b the two bits to be appended
     * @return the resultant byte array
     */
    static byte[] append4bits(byte[] arr, byte b)
    {
        byte[] out = new byte[arr.length + 1];

        System.arraycopy(arr, 0, out, 0, arr.length);
        out[arr.length] = (byte) ((b & 0x0F) << 4);

        return out;
    }
    /**
     * Prepends the left-encoding of an integer w to the
     * input string X. The result is then padded with zeros
     * until it is a byte string with length in bytes is a
     * multiple of w. Intended to be used on a previously
     * encoded string.
     * @param X the byte string.
     * @param w the integer to be encoded and prepended.
     * @return the encoded byte string.
     */
    static byte[] bytepad(byte[] X, int w)
    {
        byte[] wenc;                // w left-encoded
        byte[] z;                   // The byte[] output

        // Left encode w
        wenc = left_encode(w);

        // wenc || X
        z = new byte[w * ((wenc.length + X.length + w - 1) / w)];

        System.arraycopy(wenc, 0, z, 0, wenc.length);
        System.arraycopy(X, 0, z, wenc.length, X.length);

        // pad z with zeros until its length is a multiple of w
        for (int i = wenc.length + X.length; i < z.length; i++) z[i] = (byte) 0;

        return z;
    }

    /**
     * Concatenates two byte arrays together.
     * @param b1 byte array 1
     * @param b2 byte array 2
     * @return the resultant byte array
     */
    static byte[] concatByteArrays(byte[] b1, byte[] b2)
    {
        byte[] out;

        out = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, out, 0, b1.length);
        System.arraycopy(b2, 0, out, b1.length, b2.length);

        return out;
    }

    /**
     * Converts an integer into a binary string.
     * The length of the string is a multiple of 8.
     * @param n the integer to be converted.
     * @return the binary string representation of the number.
     */
    static String intToString(int n)
    {
        String bin;             // Binary string representation of n
        int pad;                // Amount of padding needed to ensure number is a multiple of 8

        bin = Integer.toBinaryString(n);
        pad = 8 - (bin.length() % 8);

        if (pad < 8) bin = "0".repeat(pad) + bin;

        return bin;
    }

    /**
     * Returns the byte encoding of an integer i.
     * i must be in the range 0 to 255. Bit zero is
     * the low-order bit of the byte.
     * @param i the integer to be encoded.
     * @return the byte encoding of the integer.
     */
    static byte enc8(int i)
    {
        byte b =  (byte) (i & 0xff);
        byte z = 0;

        for (int j = 0; j < 8; j++)
        {
            z <<= 1;
            z |= (b & 1);
            b >>= 1;
        }

        return z;
    }

    /**
     * Encodes a bit string. The output is the left_encode of the
     * length of the string parsed with the string itself.
     * If the string is not byte-oriented, the result will not be either.
     * @param X the string to be encoded.
     * @return the encoded string.
     */
    static byte[] encode_string(byte[] X)
    {
        byte[] pre;                // Prefix - X.length left-encoded
        byte[] z;                  // The byte[] output

        // Left-encode the number of bits required by X
        pre = left_encode(X.length * 8);

        // pre || X
        z = new byte[pre.length + X.length];

        System.arraycopy(pre, 0, z, 0, pre.length);
        System.arraycopy(X, 0, z, pre.length, X.length);

        return z;
    }

    /**
     * Encodes an integer as a byte string, starting with the number of bytes required
     * to represent the binary number and ending with the binary number. The number of
     * bytes required as well as the binary number are both reversed before being parsed.
     * @param x the integer to be encoded.
     * @return the byte string encryption of the integer.
     */
    static byte[] left_encode(int x)
    {
        int n;                          // Length of the byte string
        byte[] bytes;                   // Base-256 encoding of x
        byte[] z;                       // right_encode output

        // Compute n
        n = (int) Math.max(Math.ceil((Math.log(x) / Math.log(2)) / 8), 1);

        // Convert x to base-256 encoding
        bytes = new BigInteger(String.valueOf(x)).toByteArray();

        // Initialize the output
        z = new byte[bytes.length + 1];

        // Set the first byte to n
        z[0] = enc8(n);

        // Encode each digit of x
        for (int i = 1; i <= n; i++)    z[i] = enc8(bytes[i - 1]);

        return z;
    }

    /**
     * Returns integer 'num' within the range 0 to ('modulus' - 1).
     * @param num an integer
     * @param modulus the upper bounds of the range
     * @return an integer within the range.
     */
    static int mod(int num, int modulus)
    {
        if (num < 0)
        {
            while (num < 0) num += modulus;

            return num;
        }

        return num % modulus;
    }

    /**
     * A multi-rate padding function: pad10*1.
     * Generates padding in the form of a byte string. The asterisk indicates that the
     * zero bit is either omitted or repeated as necessary.
     * @param x positive integer
     * @param m non-negative integer
     * @return byte string such that the length is a positive multiple of x.
     */
    static byte[] pad101(int x, int m)
    {
        int j;                      // The number of zeros needed
        byte[] p;                   // The padding generated

        // Calculate the number of zeros needed
        j = mod(-m-2, x);

        // Initialize the byte[] for the padding
        p = new byte[j / 8 + 2];

        // Set the first and last bit to '1'
        p[0] = (byte) 0x80;
        p[p.length - 1] |= (byte) 0x01;

        return p;
    }

    /**
     * Reverse the direction of a string.
     * @param s the string to be reversed.
     * @return the reversed string.
     */
    private static String reverse(String s) { return new StringBuilder(s).reverse().toString(); }

    /**
     * Encodes an integer as a byte string, starting with the binary number and ending
     * with the number of bytes required to represent the binary number. The number of
     * bytes required as well as the binary number are both reversed before being parsed.
     * @param x the integer to be encoded.
     * @return the byte string encryption of the integer.
     */
    static byte[] right_encode(int x)
    {
        int n;                          // Length of the byte string
        byte[] bytes;                   // Base-256 encoding of x
        byte[] z;                       // right_encode output

        // Compute n
        n = (int) Math.max(Math.ceil((Math.log(x) / Math.log(2)) / 8), 1);

        // Convert x to base-256 encoding
        bytes = new BigInteger(String.valueOf(x)).toByteArray();

        // Initialize the output
        z = new byte[bytes.length + 1];

        // Set the last byte to n
        z[bytes.length] = enc8(n);

        // Encode each digit of x
        for (int i = 0; i < n; i++)    z[i] = enc8(bytes[i]);

        return z;
    }

    static byte[] toByteArray(String s)
    {
        BitSet bitset = new BitSet(s.length());

        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '1') bitset.set(i);
        }

        return bitset.toByteArray();
    }

    /**
     * Truncates a string at the specified index.
     * The new string is comprised of s[0] to s[i - 1].
     * @param s the string to be truncated.
     * @param i the index at which the string will be truncated.
     * @return the truncated string.
     */
    static byte[] trunc(byte[] s, int i)
    {
        byte[] t;           // Truncated byte array

        t = Arrays.copyOfRange(s, 0, i);

        return t;
    }

    /**
     * Truncates a string at the specified index.
     * The new string is comprised of s[0] to s[i - 1].
     * @param s the string to be truncated.
     * @param i the index at which the string will be truncated.
     * @return the truncated string.
     */
    static String trunc(String s, int i) {  return s.substring(0, i);   }

    /**
     * XORs to strings together.
     * @param b1 first string.
     * @param b2 second string.
     * @return the first and second string XORed together.
     */
    static byte[] xorByteArrays(byte[] b1, byte[] b2)
    {
        byte[] xor;             // b1 ^ b2 byte array

        xor = new byte[Math.min(b1.length, b2.length)];

        for (int i = 0; i < xor.length; i++)
        {
            xor[i] = (byte) (b1[i] ^ b2[i]);
        }

        return xor;
    }
}
