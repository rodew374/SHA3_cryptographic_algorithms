/**
 * FileName: LeftEncodeTest.java
 * CreatedOn: June 02, 2023
 *
 * @author ZacInman
 * @version 1.0.060223
 */

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Tests for Part 2 of the project.
 */
public class PartTwoTests
{
    BigInteger FOUR = new BigInteger("4");

    @Test
    public final void multByZero()
    {
        assertEquals(EC.O, EC.G.mult(BigInteger.ZERO),"0 * G == O");
    }
    @Test
    public final void multByOne()
    {
        assertEquals(EC.G, EC.G.mult(BigInteger.ONE), "1 * G == G");
    }

    @Test
    public final void sumOfNegation()
    {
        assertEquals(EC.O, EC.G.add(EC.G.negate()), "G + (-G) == O");
    }

    @Test
    public final void doublePoint()
    {
        assertEquals(EC.G.add(EC.G), EC.G.mult(EC.TWO), "2 * G == G + G");
    }

    @Test
    public final void multByFour()
    {
        assertEquals(EC.G.mult(FOUR), EC.G.mult(EC.TWO).mult(EC.TWO), "4 * G == 2 * (2 * G)");
    }

    @Test
    public final void nonZeroScalarMult()
    {
        assertNotEquals(EC.G.mult(FOUR), EC.O, "4 * G != O");
    }

    @Test
    public final void multByR()
    {
        assertEquals(EC.G.mult(EC.r), EC.O, "r * G == O");
    }

    @Test
    public final void randScalar()
    {
        for (int i = 0; i < 50; i++)
        {
            BigInteger k = randomInt();
            String message = "k: " + k;
            assertEquals(EC.G.mult(k), EC.G.mult(k.mod(EC.r)), "k * G == (k mod r) * G\n" + message);
        }
    }

    @Test
    public final void distributivity1()
    {
        for (int i = 0; i < 50; i++)
        {
            BigInteger k = randomInt();
            String message = "k: " + k;
            assertEquals(EC.G.mult(k.add(BigInteger.ONE)), EC.G.mult(k).add(EC.G), "(k + 1) * G == (k * G) + G\n" + message);
        }
    }

    @Test
    public final void distributivity2()
    {
        for (int i = 0; i < 50; i++)
        {
            BigInteger k = randomInt();
            BigInteger t = randomInt();
            String message = "k: " + k + "\nt: " + t;
            assertEquals(EC.G.mult(k.add(t)), EC.G.mult(k).add(EC.G.mult(t)), "(k + t) * G == (k * G) + (t * G)\n" + message);
        }
    }

    @Test
    public final void associativity()
    {
        for (int i = 0; i < 50; i++)
        {
            BigInteger k = randomInt();
            BigInteger t = randomInt();
            EC.Point P = EC.G.mult(k);
            String message = "k: " + k + "\nt: " + t;
            assertAll(message,
                    () -> assertEquals(P.mult(t), EC.G.mult(k).mult(t), "(t * P) == t * (k * G)"),
                    () -> assertEquals(EC.G.mult(k).mult(t), EC.G.mult(k.multiply(t).mod(EC.r)), "t * (k * G) == (k * t mod r) * G"),
                    () -> assertEquals(P.mult(t), EC.G.mult(k.multiply(t).mod(EC.r)), "(t * P) == (k * t mod r) * G")
            );
        }
    }

    /**
     * @return Random BigInteger within the range [1, p - 1]
     */
    public final BigInteger randomInt()
    {
        return new BigInteger(EC.p.bitLength(), new java.security.SecureRandom()).mod(EC.p.subtract(BigInteger.ONE))
                .add(BigInteger.ONE);
    }
}
