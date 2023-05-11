/**
 * FileName: DerivedFunctions.java
 * CreatedOn: May 07, 2023
 *
 * @author ZacInman
 * @version 1.0.050722
 */
/**
 *
 */
public class DerivedFunctions {

    /**
     * 256-bit security. Only reasonably supports input and output string lengths
     * that are whole bytes. Defined in terms of SHAKE256 and KECCAK[512].
     * @param X the main input String consisting of any length, including zero.
     * @param L an integer representing the requested output length in bits.
     * @param N a function-name bit string.
     * @param S a customization bit string. Defines a variant of the function.
     * @return a string with length L.
     */
    static byte[] cSHAKE256(byte[] X, int L, byte[] N, byte[] S)
    {
        if ((N.length == 0) & (S.length == 0)) return SHA3.SHAKE256(X, L);

        byte[] Nenc;                // N encoded
        byte[] Senc;                // S encoded
        byte[] strenc;              // encode_string(N) || encode_string(S)
        byte[] sp;                  // strenc padded
        byte[] newX;                // X || 00
        byte[] newN;                // new String to be passed to KECCAK

        // Encode string N and S
        Nenc = IF.encode_string(N);
        Senc = IF.encode_string(S);

        // N || S
        strenc = new byte[Nenc.length + Senc.length];
        System.arraycopy(Nenc, 0, strenc, 0, Nenc.length);
        System.arraycopy(Senc, 0, strenc, Nenc.length, Senc.length);

        // Pad the encoded string (N || S)
        sp = IF.bytepad(strenc, 136);

        // X || 00
        newX = IF.append2bits(X, (byte) 0);

        // Create binary string for the call to KECCAK
        newN = new byte[sp.length + newX.length];
        System.arraycopy(sp, 0, newN, 0, sp.length);
        System.arraycopy(newX, 0, newN, sp.length, newX.length);

        return SHA3.KECCAK(newN, L);

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
    static byte[] KMACXOF256(byte[] K, byte[] X, int L, byte[] S)
    {
        byte[] prefix;                  // K encoded and padded
        byte[] zenc;                    // right_encode(0)
        byte[] suffix;                  // X || right_encode(0)
        byte[] newX;                    // Input to cSHAKE256

        prefix = IF.bytepad(IF.encode_string(K), 136);

        // right_encode(0)
        zenc = IF.right_encode(0);

        // X || zenc
        suffix = new byte[X.length + zenc.length];
        System.arraycopy(X, 0, suffix, 0, X.length);
        System.arraycopy(zenc, 0, suffix, X.length, zenc.length);

        // Compute the newX for cSHAKE256
        newX = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, newX, 0, prefix.length);
        System.arraycopy(suffix, 0, newX, prefix.length, suffix.length);

        return cSHAKE256(newX, L, "KMAC".getBytes(), S);
    }
}
