/**
 * FileName: DerivedFunctions.java
 * CreatedOn: May 07, 2023
 *
 * @author ZacInman
 * @version 1.0.050723
 */

/**
 * Functions derived from the SHA-3 Standard.
 */
public class DF {

    /**
     * 256-bit security. Only reasonably supports input and output string lengths
     * that are whole bytes. Defined in terms of SHAKE256 and KECCAK[512].
     * @param X the main input String consisting of any length, including zero.
     * @param L an integer representing the requested output length in bits.
     * @param N a function-name bit string.
     * @param S a customization bit string. Defines a variant of the function.
     * @return a string with length L.
     */
    static String cSHAKE256(String X, int L, String N, String S)
    {
        if (N.equals("") & S.equals("")) return SHA3.SHAKE256(X, L);

        return SHA3.KECCAK(IF.bytepad(IF.encode_string(N).concat(IF
                .encode_string(S)), 136).concat(X).concat("00"), L);
    }

    /**
     * This KECCAK Message Authentication Code algorithm is a
     * keyed hash function based on KECCAK. Provides variable length
     * output.
     * @param K a key bit string of any length, including zero.
     * @param X the main input String of any length, including zero.
     * @param L an integer representing the requested output length in bits.
     * @param S a customization bit string. Defines a variant of the function.
     * @return a string with length L.
     */
    static String KMACXOF256(String K, String X, int L, String S)
    {
        String newX = IF.bytepad(IF.encode_string(K), 136).concat(X)
                .concat(IF.right_encode(0));

        return cSHAKE256(newX, L, "KMAC", S);
    }
}
