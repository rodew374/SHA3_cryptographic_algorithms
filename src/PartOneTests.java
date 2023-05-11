/**
 * FileName: LeftEncodeTest.java
 * CreatedOn: May 05, 2023
 *
 * @author ZacInman
 * @version 1.0.050522
 */

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *  Tests for Part 1 of the project.
 */
public class PartOneTests {


    @Test
    public final void encode8()
    {
        assertEquals("incorrect enc8()", "00010100", byteArrayToBinaryString(new byte[] {IF.enc8(40)}));
    }
    @Test
    public final void leftEncode()
    {
        assertEquals("incorrect left_encode()", "1000000000000000", byteArrayToBinaryString(IF.left_encode(0)));
    }

    @Test
    public final void rightEncode()
    {
        assertEquals("incorrect right_encode()", "0000000010000000", byteArrayToBinaryString(IF.right_encode(0)));
    }

    @Test
    public final void encodeString()
    {
        assertEquals("incorrect right_encode()", "1000000000000000", byteArrayToBinaryString(IF.encode_string(new byte[0])));
    }

    @Test
    public final void truncate()
    {
        assertEquals("error with trunc()", "01000000", IF.trunc("010000000", 8));
    }

    public static String byteArrayToBinaryString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }
}
