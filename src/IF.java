/**
 * FileName: InternalFunctions.java
 * CreatedOn: April 28, 2023
 *
 * @author ZacInman
 * @version 1.2.050522
 */

/**
 * Internal functions.
 */
class IF
{

    /**
     * Prepends the left-encoding of an integer n to the
     * input string s. The result is then padded with zeros
     * until it is a byte string with length in bytes is a
     * multiple of n. Intended to be used on a previously
     * encoded string.
     * @param s the byte string.
     * @param n the integer to be encoded and prepended.
     * @return the encoded byte string.
     */
    static String bytepad(String s, int n)
    {
        String bin;

        bin = left_encode(n).concat(s);

        while ((bin.length() % 8) != 0) bin = bin.concat("0");

        while (((bin.length() / 8) % n) != 0) bin = bin.concat("00000000");

        return bin;
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
     * Encodes a bit string. The output is the left_encode of the
     * length of the string parsed with the string itself.
     * If the string is not byte-oriented, the result will not be either.
     * @param s the string to be encoded.
     * @return the encoded string.
     */
    static String encode_string(String s) { return left_encode(s.length()).concat(s);   }

    /**
     * Encodes an integer as a byte string, starting with the number of bytes required
     * to represent the binary number and ending with the binary number. The number of
     * bytes required as well as the binary number are both reversed before being parsed.
     * @param n the integer to be encoded.
     * @return the byte string encryption of the integer.
     */
    static String left_encode(int n)
    {
        String bin;             // Binary string representation of the number
        String len;             // Length of the byte string

        bin = intToString(n);
        bin = reverse(bin);

        len = intToString(bin.length() / 8);
        len = reverse(len);

        return len.concat(bin);
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
     * Generates padding in the form of a string. The asterisk indicates that the
     * zero bit is either omitted or repeated as necessary.
     * @param x positive integer
     * @param m non-negative integer
     * @return string such that the length is a positive multiple of x.
     */
    static String pad101(int x, int m)
    {
        int j = mod(-m-2, x);

        return "1".concat("0".repeat(j)).concat("1");
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
     * @param n the integer to be encoded.
     * @return the byte string encryption of the integer.
     */
    static String right_encode(int n)
    {
        String bin;             // Binary string representation of the number
        String len;             // Length of the byte string

        bin = intToString(n);
        bin = reverse(bin);

        len = intToString(bin.length() / 8);
        len = reverse(len);

        return bin.concat(len);
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
     * @param s1 first string.
     * @param s2 second string.
     * @return the first and second string XORed together.
     */
    static String xorStrings(String s1, String s2)
    {
        byte[] b1;              // Byte array of s1
        byte[] b2;              // Byte array of s2
        byte[] xor;             // s1 ^ s2 byte array

        b1 = s1.getBytes();
        b2 = s2.getBytes();
        xor = new byte[Math.min(b1.length, b2.length)];

        for (int i = 0; i < xor.length; i++)
        {
            xor[i] = (byte) (b1[i] ^ b2[i]);
        }

        return new String(xor);
    }
}
