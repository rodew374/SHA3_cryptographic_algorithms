/**
 * FileName: LeftEncodeTest.java
 * CreatedOn: May 05, 2023
 *
 * @author ZacInman
 * @version 1.0.050523
 */

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *  Tests for Part 1 of the project.
 */
public class PartOneTests {

    @Test
    public final void leftEncode()
    {
        assertEquals("incorrect left_encode()", "1000000000000000", IF.left_encode(0));
    }

    @Test
    public final void rightEncode()
    {
        assertEquals("incorrect right_encode()", "0000000010000000", IF.right_encode(0));
    }

    @Test
    public final void encodeString()
    {
        assertEquals("incorrect right_encode()", "1000000000000000", IF.encode_string(""));
    }

    @Test
    public final void truncate()
    {
        assertEquals("error with trunc()", "01000000", IF.trunc("010000000", 8));
    }
}
