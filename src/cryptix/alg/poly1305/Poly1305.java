package cryptix.alg.poly1305;

import cryptix.X;

/**
 * This is a working raw Poly1305,
 * which means it is assumes that the encryption
 * of the nonce is already done / out of scope.
 * 
 * @author adalovelace
 */
public class Poly1305 {

	public static final int
	    MAC_LENGTH        = 16,
	    KEY_LENGTH        = 32,    // in this case, the combined mac-secret + cleartext-nonce
	    MAC_SECRET_LEN    = 16,
	    ENC_NONCE_LEN     = MAC_LENGTH;
	
	static final int
	    ROW_LEN           = 17,    // Each 'row' is the block padded by adding 1, so 17 bytes
	    BLOCK_LEN         = 16;    // A message 'chunk' or block is 16 bytes

	static final int[]
	    minusp            = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 252};
	
    static final byte
        R_MASK_LOW_2  = (byte)0x00FC,     // 252, mask the low 2 bits off
        R_MASK_HIGH_4 = (byte)0x000F;     // 15, mask the high 4 bits off
    static final int
        INT_MASK_LOW_2  = (int)0x00FC,     // mask the low 2 bits off
        INT_MASK_HIGH_4 = (int)0x000F,     // mask the high 4 bits off
        INT_BYTE_MASK   = 0x00FF;
    
    
    
    
    static final int[] clampMacSecretToR(final byte[] ms)
    {
        int[] r = new int[ROW_LEN];

        //System.err.println( toString(ms, "ms  "));
        /*
         * Clamp the r mac-secret.
         */
        r[0]  = ms[0]  & INT_BYTE_MASK;
        r[1]  = ms[1]  & INT_BYTE_MASK;
        r[2]  = ms[2]  & INT_BYTE_MASK;
        r[3]  = ms[3]  & INT_MASK_HIGH_4;
        r[4]  = ms[4]  & INT_MASK_LOW_2;
        r[5]  = ms[5]  & INT_BYTE_MASK;
        r[6]  = ms[6]  & INT_BYTE_MASK;
        r[7]  = ms[7]  & INT_MASK_HIGH_4;
        r[8]  = ms[8]  & INT_MASK_LOW_2;
        r[9]  = ms[9]  & INT_BYTE_MASK;
        r[10] = ms[10] & INT_BYTE_MASK;
        r[11] = ms[11] & INT_MASK_HIGH_4;
        r[12] = ms[12] & INT_MASK_LOW_2;
        r[13] = ms[13] & INT_BYTE_MASK;
        r[14] = ms[14] & INT_BYTE_MASK;
        r[15] = ms[15] & INT_MASK_HIGH_4;
        r[16] = 0;
        //System.err.println( toString(r, "r    "));

        return r;
    }

    /**
     * Modifies an existing 16 byte mac secret value to comply
     * with the requirements of the Poly1305 key by clearing
     * required bits in the <code>r</code> bytes.<br>
     * Specifically:
     * <ul>
     * <li>r[3], r[7], r[11], r[15] have top four bits clear (i.e., are {0, 1, . . . , 15})</li>
     * <li>r[4], r[8], r[12] have bottom two bits clear (i.e., are in {0, 4, 8, . . . , 252})</li>
     * </ul>
     * 
     * <p>From the paper, the (full AES) key is defined thusly:</p>
     * 
     * <p>"Poly1305-AES authenticates messages using a 32-byte secret key shared by the message sender
     * and the message receiver. The key has two parts: first, a 16-byte AES key k;
     * second, a 16-byte string r[0],r[1],...,r[15].
     * The second part of the key represents a 128-bit integer r in unsigned little-endian form:
     * i.e., r = r[0] + 2<sup>8</sup>r[1] + . . . + 2<sup>120</sup>r[15]."</p>
     * 
     * <p>"Certain bits of r are required to be 0 r[3],r[7],r[11],r[15]
     * are required to have their top four bits clear
     * (i.e., to be in {0, 1, . . . , 15}),
     * and r[4], r[8], r[12] are required to have their bottom two bits clear
     * (i.e., to be in {0, 4, 8, . . . , 252}).
     * Thus there are 2<sup>106</sup> possibilities for <i>r</i>.
     * In other words, <i>r</i> is required to have the form
     * <blockquote>
     *       r<sub>0</sub>+r<sub>1</sub>+r<sub>2</sub>+r<sub>3</sub>
     * </blockquote>
     * where
     * <blockquote>
     *       r<sub>0</sub>                &isin; {0, 1, 2, 3,...,  2<sup>28</sup> - 1},<br />
     *       r<sub>1</sub>/2<sup>32</sup> &isin; {0, 4, 8, 12,..., 2<sup>28</sup> - 4},<br />
     *       r<sub>2</sub>/2<sup>64</sup> &isin; {0, 4, 8, 12,..., 2<sup>28</sup> - 4}, and<br />
     *       r<sub>3</sub>/2<sup>96</sup> &isin; {0, 4, 8, 12,..., 2<sup>28</sup> - 4}.
     * </blockquote>
     * "</p>
     * 
     * <p>Note that here in the raw Poly1305, we are only concerned with the
     * r part as the mac-secret.  The AES key k[0-15] is not part of this code.</p>
     *
     * @param ms a 16 byte mac-secret value holding <code>r[0] ... r[15]</code>
     */
    static final void clampMacSecret(final byte[] ms)
    {
        /*
         * Clamp the R mac-secret.
         *
         * k[3], k[7], k[11], k[15] have top four bits clear (i.e., are {0, 1, . . . , 15})
         * k[4], k[8], k[12] have bottom two bits clear (i.e., are in {0, 4, 8, . . . , 252}).
         */
        ms[3]  = (byte)(ms[3]  & R_MASK_HIGH_4);
        ms[4]  = (byte)(ms[4]  & R_MASK_LOW_2);
        ms[7]  = (byte)(ms[7]  & R_MASK_HIGH_4);
        ms[8]  = (byte)(ms[8]  & R_MASK_LOW_2);
        ms[11] = (byte)(ms[11] & R_MASK_HIGH_4);
        ms[12] = (byte)(ms[12] & R_MASK_LOW_2);
        ms[15] = (byte)(ms[15] & R_MASK_HIGH_4);
    }

    static final boolean isClamped(byte[] ms)
    {
        boolean notclamped = false;
        notclamped  |= (ms[3]  & R_MASK_HIGH_4) != ms[3];
        notclamped  |= (ms[4]  & R_MASK_LOW_2 ) != ms[4];
        notclamped  |= (ms[7]  & R_MASK_HIGH_4) != ms[7];
        notclamped  |= (ms[8]  & R_MASK_LOW_2 ) != ms[8];
        notclamped  |= (ms[11] & R_MASK_HIGH_4) != ms[11];
        notclamped  |= (ms[12] & R_MASK_LOW_2 ) != ms[12];
        notclamped  |= (ms[15] & R_MASK_HIGH_4) != ms[15];
        
        return ! notclamped;
    }

    /**
     * This is a raw use of Poly1305, which assumes that
     * the nonce is already encrypted.
     * The macsecret is always clamped (because we have to copy it into ints anyway).
     * 
     * @param mac an array to place the calculated MAC into
     * @param macOffset where in mac to write the MAC
     * @param msg
     * @param msgOffset
     * @param msgLen
     * @param ekn is the encrypted nonce eK(n) of 16 bytes, using whatever encryption algorithm
     * @param ms is a mac-secret of 16 bytes, becomes r
     */
    public static void auth(byte[] mac, int macOffset, byte[] msg, int msgOffset, long msgLen, byte[] ekn, byte[] ms)
    {
        if (mac == null || mac.length - macOffset < MAC_LENGTH)
            throw new RuntimeException("Poly1305 mac secret too short");
        if (ekn == null || ekn.length != ENC_NONCE_LEN)
            throw new RuntimeException("Poly1305 encrypted nonce ekn not 16");
        if (ms == null || ms.length != MAC_SECRET_LEN)
            throw new RuntimeException("Poly1305 mac secret r not 16");
        if (msg == null || msg.length - msgOffset < msgLen)
            throw new RuntimeException("Poly1305 msg params oflow");
       
        int[] r = clampMacSecretToR(ms);
        int[] h = new int[ROW_LEN];
        int[] c = new int[ROW_LEN];

        while (msgLen > 0)     // this flow taken from crypto_onetimeauth()
        {
            for (int j = 0; j < ROW_LEN; ++j)    // quicker than reallocating?
                c[j] = 0;

            int i = 0;
            //System.err.print("      ");
            for (i = 0; (i < BLOCK_LEN) && (i < msgLen); ++i) {
            //    System.err.print(", " + (msg[msgOffset + i]&0xff));
                c[i] = msg[msgOffset + i] & INT_BYTE_MASK;
            }
            //System.err.println("");

            c[i] = 1;
            //System.err.println( toString(c, "    c"));
            msgOffset += i;
            msgLen -= i;
            add(h, c);
            //System.err.println( toString(h, "    h"));
            mulmod(h, r);
            //System.err.println( toString(h, "    h"));
        }
        //System.err.println("===================================\n");

        freeze(h);
        //System.err.println( toString(h, "h    "));

        //System.err.println( toString(key, "key   "));
        for (int j = 0; j < 16; ++j)
            c[j] = ekn[j] & INT_BYTE_MASK;

        //System.err.println( toString(c, "c!   "));

        c[ROW_LEN - 1] = 0;
        add(h, c);
        //System.err.println( toString(h, "h    "));

        for (int j = 0; j < MAC_LENGTH; ++j)
            mac[j + macOffset] = (byte)h[j];
    }
    

	
	static String toString(int[] buf, String name) {
	    byte[] bytes = new byte[buf.length];
	    for (int i = 0; i < buf.length; i++) {
	        bytes[i] = (byte) (buf[i] & INT_BYTE_MASK);
	    }
	    return toString(bytes, name);
	}

    static String toString(byte[] buf, String name) {
        String s = name + ": ";
        s += X.data2hex(buf);
        return s;
    }
    
    

    static void add(int[] h, int[] c)
    {
        int u = 0;
        for (int j = 0; j < ROW_LEN; ++j)
        {
            //System.err.print("..... " + j + ": ");
            u += h[j] + c[j];
            //System.err.print("     u " + u + " += hj " + h[j] + " + cj " + c[j]);
            h[j] = u & INT_BYTE_MASK;
            u >>>= 8;
            //System.err.println("      hj " + h[j] + " u " + u);
        }
    }

    static void squeeze(int[] h)
    {
        int u = 0;

        for (int j = 0; j < 16; ++j)
        {
            u += h[j];
            h[j] = u & INT_BYTE_MASK;
            u >>>= 8;
        }

        u += h[16];
        h[16] = u & 3;
        u = 5 * (u >>> 2);

        for (int j = 0; j < 16; ++j)
        {
            u += h[j];
            h[j] = u & INT_BYTE_MASK;
            u >>>= 8;
        }

        u += h[16];
        h[16] = u;
    }

    static void freeze(int[] h)
    {
        int[] horig = new int[ROW_LEN];

        for (int j = 0; j < ROW_LEN; ++j)
            horig[j] = h[j];

        add(h, minusp);

        int negative = (int)(-(h[ROW_LEN-1] >>> 7));

        for (int j = 0; j < ROW_LEN; ++j)
            h[j] ^= negative & (horig[j] ^ h[j]);
    }

    static void mulmod(int[] h, int[] r)
    {
        int[] hr = new int[ROW_LEN];

        for (int i = 0; i < ROW_LEN; ++i)
        {
            int u = 0;

            for (int j = 0; j <= i; ++j)
                u += h[j] * r[i - j];

            for (int j = i + 1; j < ROW_LEN; ++j)
                u += 320 * h[j] * r[i + ROW_LEN - j];

            hr[i] = u;
        }

        for (int i = 0; i < ROW_LEN; ++i)
            h[i] = hr[i];

        squeeze(h);
    }
}
