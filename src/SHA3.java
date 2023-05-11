/**
 * FileName: KECCAK.java
 * CreatedOn: April 28, 2023
 *
 * @author ZacInman
 * @version 1.2.050722
 */

/**
 * The SHA-3 standard: Permutation-based hash and extendable-output functions.
 */
class SHA3 {

    static final int c = 512;                                  // SHA-3 capacity
    static final int b = 1600;                                 // Number of bits in the KECCAK-p state
    static final int nr = 24;                                   // KECCAK-p number of rounds
    static final int w = b / 25;                               // KECCAK-p width
    static final int l = (int) (Math.log(w) / Math.log(2));    // Log base 2 of w
    static final int r = b - c;                                // Sponge rate

    /**
     * SHA-3 extendable-output function.
     * Defined as an instance of the KECCAK[512] function.
     * Appends a four-bit suffix to M, for any
     * output length d.
     * @param M the string message.
     * @param d the integer representing the output length.
     * @return the hash output string.
     */
    static String SHAKE256(String M, int d) {  return KECCAK(M.concat("1111"), d); }

    /**
     * KECCAK Sponge.
     * An arbitrary number of input bits are 'absorbed' into
     * the KECCAK-p state, after which an arbitrary number of
     * output bits are 'squeezed' out of its state.
     * Uses the multi-rate padding function pad10*1.
     * The rate of the sponge is equivalent to the number of
     * bits in the function state - the capacity.
     * @param N a bit string.
     * @param d the bit length of the output string.
     * @return a string with length d.
     */
    static String KECCAK(String N, int d)
    {
        String P, S, Z;
        int n;

        // Step 1
        P = N.concat(IF.pad101(r, N.length()));

        // Step 2
        n = P.length() / r;

        // Step 3 and 4 are omitted (Unnecessary)
        // Step 5
        S = "0".repeat(b);

        // Step 6
        for (int i = 0; i < n; i++)
        {
            S = KECCAK_p(IF.xorStrings(S, P.substring(r * i, r * (i + 1)).concat("0".repeat(c))));
        }

        // Step 7
        Z = "";

        // Step 8
        Z = Z.concat(IF.trunc(S, r));

        // Step 9 and 10
        while (d > Z.length())
        {
            S = KECCAK_p(S);
            Z = Z.concat(IF.trunc(S, r));
        }

        return IF.trunc(Z, d);

    }

    /**
     * Performs a set of KECCAK permutations on a string.
     * The global variables (B, L, N, W) control the operation.
     * @param s the string
     * @return the transformed string
     */
    static String KECCAK_p(String s)
    {
        boolean[][][] A = new boolean[5][5][w];     // The state array
        StringBuilder sb = new StringBuilder();     // Helps build the required strings
        String[][] lane = new String[5][5];         // A lane of the state array
        String[] plane = new String[5];             // A plane of the state array

        // Step 1: Convert string to state array
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < w; z++)
                {
                    char c = s.charAt(w * (5 * y + x) + z);

                    A[x][y][z] = c != '0';

                }
            }
        }

        // Step 2: Start round transformations
        for (int i = 12 + (2 * l) - nr; i <= 12 + (2 * l) - 1; i++) {    A = rnd(A, i);  }

        // Step 3: Convert state array to string
        // Create the lane strings
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < w; z++)
                {
                    if (A[x][y][z]) sb.append('1');
                    else sb.append('0');

                }
                lane[x][y] = sb.toString();
                sb = new StringBuilder();
            }
        }

        // Create the plane strings
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++) sb.append(lane[x][y]);

            plane[y] = sb.toString();
            sb = new StringBuilder();
        }

        // Create the string
        for (int y = 0; y < 5; y++) sb.append(plane[y]);

        // Step 4: Return the string
        return sb.toString();
    }

    /**
     * This function constitutes a round.
     * It applies the step mappings theta, rho, pi, chi, and iota, in that order.
     * @param A the state array
     * @param i the round index
     * @return the updated state array
     */
    static boolean[][][] rnd(boolean[][][] A, int i) { return iota(chi(pi(rho(theta(A)))), i); }

    /**
     * The first step mapping for a round of KECCAK-p[1600,24].
     *
     * @param A 5x5x64 array of booleans representing the state.
     * @return the updated state array.
     */
    static boolean[][][] theta(boolean[][][] A) {

        boolean[][] C = new boolean[5][w];                  // Helper array
        boolean[][] D = new boolean[5][w];                  // Helper array
        boolean[][][] R = new boolean[5][5][w];             // The new state array

        // Step 1
        for (int x = 0; x < 5; x++)
        {
            for (int z = 0; z < w; z++)
            {
                C[x][z] = A[x][0][z] ^ A[x][1][z] ^ A[x][2][z] ^ A[x][3][z] ^ A[x][4][z];
            }
        }

        // Step 2
        for (int x = 0; x < 5; x++)
        {
            for (int z = 0; z < w; z++)
            {
                D[x][z] = C[IF.mod(x - 1, 5)][z] ^ C[IF.mod(x + 1,
                        5)][IF.mod(z - 1, w)];
            }
        }

        // Step 3
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < w; z++)
                {
                    R[x][y][z] = A[x][y][z] ^ D[x][z];
                }
            }
        }

        return R;
    }

    /**
     * The second step mapping for a round of KECCAK-p[1600,24].
     *
     * @param A 5x5x64 array of booleans representing the state.
     * @return the updated state array.
     */
    static boolean[][][] rho(boolean[][][] A)
    {
        boolean[][][] R = new boolean[5][5][w];             // The new state array
        int x = 1, y = 0;                                   // Step 2

        // Step 1
        System.arraycopy(A[0][0], 0, R[0][0], 0, w);

        // Step 3
        for (int t = 0; t < 24; t++)
        {
            for (int z = 0; z < w; z++)
            {
                R[x][y][z] = A[x][y][IF.mod(z - (((t + 1) * (t + 2)) / 2), w)];
            }

            //noinspection SuspiciousNameCombination
            x = y;
            y = IF.mod((2 * x) + (3 * y), 5);
        }

        // Step 4
        return R;
    }

    /**
     * The third step mapping for a round of KECCAK-p[1600,24].
     *
     * @param A 5x5x64 array of booleans representing the state.
     * @return the updated state array.
     */
    static boolean[][][] pi(boolean[][][] A)
    {
        boolean[][][] R = new boolean[5][5][w];             // The new state array

        // Step 1
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++)
            {
                System.arraycopy(A[IF.mod(x + (3 * y), 5)][x], 0, R[x][y], 0, w);
            }
        }

        // Step 2
        return R;
    }

    /**
     * The fourth step mapping for a round of KECCAK-p[1600,24].
     *
     * @param A 5x5x64 array of booleans representing the state.
     * @return the updated state array.
     */
    static boolean[][][] chi(boolean[][][] A)
    {
        boolean[][][] R = new boolean[5][5][w];             // The new state array

        // Step 1
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < w; z++)
                {
                    R[x][y][z] = A[x][y][z] ^ ((!A[IF.mod(x + 1, 5)][y][z])
                            && A[IF.mod(x + 2, 5)][y][z]);
                }
            }
        }

        // Step 2
        return R;
    }

    /**
     * The fifth step mapping for a round of KECCAK-p[1600,24].
     *
     * @param A 5x5x64 array of booleans representing the state.
     * @param i round index.
     * @return the updated state array.
     */
    static boolean[][][] iota(boolean[][][] A, int i)
    {
        boolean[][][] R = new boolean[5][5][w];             // The new state array
        StringBuilder RC;                                   // Round constant

        // Step 1
        for (int y = 0; y < 5; y++)
        {
            for (int x = 0; x < 5; x++) System.arraycopy(A[x][y], 0, R[x][y], 0, w);
        }

        // Step 2
        RC = new StringBuilder("0".repeat(w));

        // Step 3
        for (int j = 0; j <= l; j++)    RC.setCharAt((int) Math.pow(2, j) - 1, rc(j + (7 * i)));

        // Step 4
        for (int z = 0; z < w; z++)
        {
            if (RC.charAt(z) == '1') R[0][0][z] = !R[0][0][z];
        }

        // Step 5
        return R;
    }

    /**
     * Round constant method utilized inside iota().
     * @param t an integer.
     * @return bit rc(t) in char format.
     */
    static char rc(int t)
    {
        int u = IF.mod(t, 255);                  // For-loop upper bounds

        // Step 1
        if (u == 0) return '1';

        // Step 2
        StringBuilder R = new StringBuilder("10000000");

        // Step 3
        for (int i = 0; i <= u; i++)
        {

            R.insert(0, "0");
            R.setCharAt(0, (char) (R.charAt(0) ^ R.charAt(8)));
            R.setCharAt(4, (char) (R.charAt(4) ^ R.charAt(8)));
            R.setCharAt(5, (char) (R.charAt(5) ^ R.charAt(8)));
            R.setCharAt(6, (char) (R.charAt(6) ^ R.charAt(8)));

            R = new StringBuilder(IF.trunc(R.toString(), 8));

        }

        // Step 4
        return R.charAt(0);
    }
}