/**
 * FileName: EC.java
 * CreatedOn: June 01, 2023
 *
 * @author ZacInman
 * @version 1.0.060123
 */

import java.math.BigInteger;

/**
 * Implementation of an Ed448-Goldilocks curve (Edwards curve).
 */
public class EC
{
    /**
     * The BigInteger constant 2.
     */
    static final BigInteger TWO = new BigInteger("2");
    /**
     * The edwards curve constant.
     */
    static final BigInteger d = new BigInteger("-39081");
    /**
     * Prime number representing the curve's finite field.
     */
    static final BigInteger p = TWO.pow(448).subtract(TWO.pow(224)).subtract(BigInteger.ONE);
    /**
     * Constant needed to calculate the number of points on the curve.
     */
    static final BigInteger r = TWO.pow(446).subtract(new BigInteger("138180668098951153520073867485154" +
                                                                                "26880336692474882178609894547503885"));
    /**
     * The number points on the curve.
     */
    static final BigInteger n = r.multiply(new BigInteger("4"));
    /**
     * A special point known as the curves public generator.
     * The x coordinate is BigInteger 8, and the y coordinate is a
     * unique even number.
     */
    static final Point G = new Point(new BigInteger("8"), false);
    /**
     * Point at "infinity" aka neutral element of addition.
     */
    static final Point O = new Point();

    static class Point
    {
        /**
         * This point's x coordinate.
         */
        final BigInteger x;
        /**
         * This point's y coordinate.
         */
        final BigInteger y;

        /**
         * Constructs the neutral point, i.e., the point at "infinity".
         */
        Point()
        {
            this(BigInteger.ZERO, BigInteger.ONE);
        }

        /**
         * Constructs a new point from two BigInteger coordinates.
         *
         * @param x BigInteger x coordinate
         * @param y BigInteger y coordinate
         */
        Point(BigInteger x, BigInteger y)
        {
            this.x = x;
            this.y = y;
        }

        /**
         * Constructs a new point on the curve from BigInteger
         * coordinate x and the desired least significant bit
         * of the y coordinate. The y coordinate can be null if
         * there is not a point on the curve given the x coordinate.
         *
         * @param x BigInteger x coordinate
         * @param ylsb desired least significant bit of the y
         *             coordinate: true == 1, false == 0
         */
        Point(BigInteger x, boolean ylsb)
        {
            BigInteger num;             // Radicand numerator
            BigInteger den;             // Radicand denominator
            BigInteger v;               // Radicand

            num = BigInteger.ONE.subtract(x.modPow(TWO, p)).mod(p);
            den = BigInteger.ONE.add(d.negate().multiply(x.modPow(TWO, p))).mod(p);

            v = num.multiply(den.modInverse(p)).mod(p);

            y = IF.sqrt(v, p, ylsb).mod(p);
            this.x = x;
        }

        /**
         * Edwards point addition formula.
         * Adds the point P to this point.
         *
         * @param P the point to be added to this point.
         * @return the resulting point from the addition.
         */
        Point add(Point P)
        {
            BigInteger newx, newy;          // Coordinates for the resulting point
            BigInteger numx, numy;          // Resulting point numerators
            BigInteger denx, deny;          // Resulting point denominators
            BigInteger dxxyy;               // All coordinates multiplied with d
            BigInteger xy;                  // x1 multiplied with y2
            BigInteger yx;                  // y1 multiplied with x2
            BigInteger xx;                  // X coordinates multiplied together
            BigInteger yy;                  // Y coordinates multiplied together

            xy = this.x.multiply(P.y);
            yx = this.y.multiply(P.x);
            xx = this.x.multiply(P.x);
            yy = this.y.multiply(P.y);
            dxxyy = d.multiply(xx.multiply(yy));

            numx= xy.add(yx);
            denx = BigInteger.ONE.add(dxxyy);
            numy = yy.subtract(xx);
            deny = BigInteger.ONE.subtract(dxxyy);

            newx = numx.multiply(denx.modInverse(p)).mod(p);
            newy = numy.multiply(deny.modInverse(p)).mod(p);

            return new Point(newx, newy);

//            BigInteger x1 = x;
//            BigInteger y1 = y;
//            BigInteger x2 = P.x;
//            BigInteger y2 = P.y;
//
//            BigInteger A = (y1.subtract(x1)).multiply(y2.subtract(x2)).mod(p);
//            BigInteger B = (y1.add(x1)).multiply(y2.add(x2)).mod(p);
//            BigInteger C = d.multiply(x1).multiply(x2).multiply(BigInteger.valueOf(2)).mod(p);
//            BigInteger D = B.subtract(A).mod(p);
//            BigInteger E = B.add(A).mod(p);
//
//            BigInteger x3 = D.multiply(E).mod(p);
//            BigInteger y3 = D.multiply(E).multiply(C).mod(p);
//
//            return new Point(x3, y3);
        }

        /**
         * Compare this point with object o for equality.
         *
         * @param o object to which this point is to be compared.
         * @return true IFF the object is a point with coordinates equivalent to this point's coordinates.
         */
        @SuppressWarnings("SuspiciousNameCombination")
        public boolean equals(Object o)
        {
            if (this == o)  return true;
            if (o == null || getClass() != o.getClass())    return false;

            Point P = (Point) o;

            return (this.x.equals(P.x) && this.y.equals(P.y));
        }

        /**
         * Multiplies this point by a scalar using an
         * exponentiation algorithm.
         * s * P = P + P + ... + P (s times)
         *
         * @param s the scalar.
         * @return the product point
         */
        Point mult(BigInteger s)
        {
            Point V = O;                    // Initialize with the neutral element
            Point T = this;

            while (s.compareTo(BigInteger.ZERO) > 0)
            {
                if (s.testBit(0))   V = T.add(V);

                T = T.add(T);
                s = s.shiftRight(1);
            }

            return V;
        }

        /**
         * The opposite of point (x,y).
         *
         * @return point (-x,y).
         */
        Point negate()
        {
            return new Point(x.negate(), y);
        }

        public String toString()
        {
            return "(" + this.x + ", " + this.y + ")";
        }
    }
}
