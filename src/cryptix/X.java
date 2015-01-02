package cryptix;



/**
 * A collection of things that are important to crypto.
 * In many cases they are duplications of things that are found in
 * WebFunds.util.* but there is an intention to split the crypto off
 * into a separate library.
 * 
 * May need to add more Hex things, and Example stuff.
 * 
 * @author iang
 */
public class X
{
    public static void debug(String s)     { }
    public static void info (String s)     { }
    public static void warn (String s)     { }
    public static void error(String s)     { }
    
    
    
    /**
     * Constant Time Equality of two byte arrays.
     * 
     * Constant time is only reliable to the extent that the arrays
     * are of the same length.
     * (It could be organised to be CT if one of them is null, but that
     * seems like broken code to rely on such a thing, and any trickery
     * might not survive the JIT.)
     * 
     * @param one is a byte array of any length, else null
     * @param two ditto
     * @return true if the arrays are byte/bit-wise equal,
     *         true if both null, false if one only null, false if different lengths
     * @see webfunds.util.Equals which is more comprehensive and adds noisy()
     */
    public static final boolean ctEquals(final byte[] one, final byte[] two)
    {
        if (one == null && two == null)                  return true;
        if (one == null || two == null)                  return false;
        if (one.length != two.length)                    return false;
        
        int xorDiffs = 0;
        
        /*
         * Run through the entire array(s), and XOR them to highlight any
         * differing bits.  This is the contant time bit.
         */
        for (int i = 0; i < one.length; i++) {
            xorDiffs |= ((int)(one[i] ^ two[i])) & 0xff;
        }
        
        /*
         *  
         * Original NaCl C code turns any zero into -1, any bits into zero,
         * flipped those back to zero or -1 respectively,
         * so a definition of 0==equals, -1==differs was returned.
         *      return (1 & (((int)diffBits - 1) >>> 8)) - 1;
         * The problem with this is that we have to turn it into
         * a boolean here, so the CT maths don't gain us anything,
         * and even in C, the caller is likely to do the same thing...
         */
        return 0 == xorDiffs ;
    }
    
    /**
     * 
     * @param one the first array
     * @param p the offset into the first array
     * @param two the second array
     * @param q the offset into the second array
     * @param len the length of byte sequence to check
     * @return true if the sequences are byte-wise equal, or if both are null,
     *         else false:  either is null, or either is too short (arguable) 
     */
    public static final boolean ctEquals(final byte[] one, int p, final byte[] two, int q, int len)
    {
        if (one == null && two == null)            return true;
        if (one == null || two == null)            return false;
        
        if ((one.length - p) < len)                return false;
        if ((two.length - q) < len)                return false;
        
        int xorDiffs = 0;
        
        for (int i = 0; i < len; i++) {
            xorDiffs |= ((int)(one[i + p] ^ two[i + q])) & 0xff;
        }
        
        return 0 == xorDiffs ;
    }


    /**
     *  Equals for parent objects (handles null case).
     *  Works as long as you know at least one is the right Class
     *  (for example, was already type-checked)
     *  and its equals method is well-behaved.
     *  
     *  @warning do not call this with byte arrays, instead use ctEquals()
     *  
     *  @param one, two are objects 
     *  @return true if both null, false if one null,
     *          false if different class, else returns one.equals(two)
     */
    public static boolean equals(Object one, Object two)
    {
        if (one == null && two == null)
            return true;
        if (one == null || two == null)
            return false;
        return one.equals(two);
    }
    
    

    /**
     *  Make a copy of the raw bytes, in constant time per byte.
     *
     *  @param the original bytes (can be null)
     *  @return the bytes copied (null is returned if null passed)
     */
    public static byte[] copy(byte[] buf)
    {
        if (buf == null)
            return null;
        int len = buf.length;
        byte[] temp = new byte[len];
        for (int i = 0; i < len; i++) {
            temp[i] = buf[i];
        }
        return temp;
    }

    /**
     *  Make a copy of the raw bytes, constant time per byte.
     *  On overflow, the remainder will be zero-filled.
     *  
     *  @param the original bytes (can be null)
     *  @param start from where to start copying
     *  @param len how many bytes to copy, maximum
     *  @return an array of len bytes, with those in range copied (null is returned if null passed)
     */
    public static byte[] copy(byte[] buf, int start, int len)
    {
        if (buf == null)
            return null;
        byte[] temp = new byte[len];
        for (int i = 0; i < len; i++) {
            int p = i+start;
            temp[i] = (0 <= p && p < buf.length) ? buf[p] : 0;
        }
        return temp;
    }

    /**
     * <p>Crypto hygiene is important!
     * Writes junk in each element of the buffer,
     * so the secrets die here & now.</p>
     * <p>Tip:  declare any crypto-sensitive secret as volatile.</p>
     * 
     * <p>This call will abort if a population count test fails on the
     * scrunted array.  In part, this indicates that the PRNG is screwed.
     * (Although note that this could occur frequently for a small array
     * e.g., 2 in 256 times for a one byte array...).
     * In other part, it also futzes with the optimiser to stop it
     * deciding to drop the scrunting by actually using the values in
     * some obscure fashion.</p>
     * 
     * @param b any byte array that needs to be scrubbed
     * @return null always so the byte array can be assigned in one line
     */
    public static byte[] destroy(byte[] b)
    {
        if (null == b || 0 == b.length) {
            return null;
        }
        
        final byte[] mask = mask(b.length);
        for (int i = 0; i < b.length; i++) {
            b[i] ^= mask[i];
            mask[i] = 0;
        }
        int popn = 0;
        for (int i = 0; i < b.length; i++) {
            popn += popn(b[i]);
        }
        adversePopulation(popn, b.length);
        return null;
    }
    
    /**
     * Check whether the population of bits is outside some metric.
     * This is both a check of random operation, *and* a false activity
     * to convince the optimiser to leave us alone.
     * It is not perfect, as 0 is also random,
     * at least once every 2^(8*length) times.
     * 
     * @param popn
     * @param length
     */
    public static void adversePopulation(int popn, int length)
    {
        /*
         * What is the metric here, and the action?
         * Let's run with this and see how bad it is.
         */
        if (0 == popn || (8*length) == popn)
            throw new RuntimeException("popn6482 " + popn + "/" + length);
    }

    /**
     * Check whether the population of bits is expected according to
     * some random profile, which should mean about 4 bits per byte.
     * This is both a check of random operation, *and* a false activity
     * to convince the optimiser to leave us alone.
     * It is not perfect, as 0 is also random,
     * at least once every 2^(8*length) times.
     * 
     * @param b is a buffer containing random data
     */
    public static void checkPopulation(byte[] b)
    {
        if (null == b || b.length == 0)
            return ;
        int popn = 0;
        int length = b.length;
        for (int i = 0; i < length; i++) {
            popn += popn(b[i]);
        }
        /*
         * What is the metric here, and the action?
         * Let's run with this and see how bad it is.
         */
        if ( ! (3 * length <= popn || popn <= 5 * length))
            throw new RuntimeException("popn1057 " + popn + "/" + length + " " + X.quick(b));
    }

    
    public static boolean isBadKey(byte[] b)
    {
        if (null == b) return true;
        boolean zero = true;
        for (int i = 0; i < b.length; i++) {
            if (0 != b[i])
                zero = false;
        }
        return zero;
    }
    
    public static byte[] mask(int len)
    {
        final byte[] mask = new byte[ len ]; 
        PRNG.get().nextBytes(mask);
        return mask;
    }

    public static char[] maskChars(int len)
    {
        final byte[] mask = mask(len);
        final char[] mc = new char[len];
        for (int i =0; i < len; i++) {
            mc[i] = (char) mask[i];
        }
        return mc;
    }

    /**
     * Uses reflection to go into the String itself and write over the underlying
     * bytes.  Does this really work?  It writes over the reference, not the
     * bytes.  Needs testing...
     * 
     * @param toBeJunk will be overwritten with some random stuff
     * @return null, always, so can be assigned to the param
     */
    @SuppressWarnings("unused")
    private static String destroy(String toBeJunk)
    {
        if (null == toBeJunk)
            return null;
        int len = toBeJunk.length() * 2;
        if (len < 40)
            len = 40;
        char[] mask = maskChars(len);
        try {
            java.lang.reflect.Field value = String.class.getDeclaredField("value");
            value.setAccessible(true);
            value.set(toBeJunk, mask);
            java.lang.reflect.Field count = String.class.getDeclaredField("count");
            count.setAccessible(true);
            count.set(toBeJunk, mask.length);
        } catch (Exception ignore) {
            ; // ignore
        }
        return null;
    }
    

    /**
     * Population count is a good way of easily checking the
     * randomness of the content.  If the population of set bits
     * is approximately equal to the unset bits, or 50%, then
     * it is probably random.
     * 
     * @return number of bits that are set in the array passed
     */
    public static int popn(byte[] b)
    {
        int bits = 0;
        for (int j = 0; j < b.length; j++) {
            bits += popn(b[j]);
        }
        return bits;
    }

    /**
     * OK, so we could speed this up with a lookup table... whatever.
     * 
     * @return number of bits that are set in the byte passed
     */
    public static int popn(byte b)
    {
        int bits = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 0x01) == 0x01)
                bits++;
            b >>= 1;
        }
        return bits;
    }
    
    /**
     * Useful for showing that a key is likely set up without
     * having to expose the key itself.
     * @param b
     * @return
     */
    public static String population(byte[] b)
    {
        if (null == b)
            return "(null)";
        int bits = popn(b);
        int len = b.length * 8;
        return (100 * bits / len) + "%"; 
    }
    
    /**
     * We need to sometimes see the contents of keys in diags,
     * in order to see if they are properly initialised.
     * But this compromises key material into the logs.
     * This display gives the essentials and reduces compromise
     * as much as possible.
     * Currently, it compromises the length
     * and a population count.
     * @param b
     * @return length and a population count
     */
    public static String keyPeek(byte[] b)
    {
        if (null == b)
            return "(null)";
        return "[" + b.length + "] " + population(b);
    }

    

    private static final char[] HEX_DIGITS = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };
    
    /**
     * Returns the the hex digit corresponding to the argument.
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If the argument is not in range [0,15];
     */
    private static char toHexDigit(int i)
    {
        return HEX_DIGITS[i];
    }

    private static byte toDataNibble(char c)
    {
        if (('0' <= c) && (c <= '9' ))
            return (byte)((byte)c - (byte)'0');
        else if (('a' <= c) && (c <= 'f' ))
            return (byte)((byte)c - (byte)'a' + 10);
        else if (('A' <= c) && (c <= 'F' ))
            return (byte)((byte)c - (byte)'A' + 10);
        else
            return -1;
    }

    
    private static void appendhex(StringBuffer buf, byte b) {

        buf.append(toHexDigit((b>>>4)&0x0F));
        buf.append(toHexDigit((b    )&0x0F));
    }

    /**
     *  Must not change the bytes, many Immutable classes rely on this.
     *  Note that null is returned as the string NULL == "<NULL>".
     */
    private static void data2hex(StringBuffer buf, byte[] data, int start, final int len)
    {
        for (int pos = start; pos < start+len; pos++) {
            appendhex(buf, data[pos]);
        }
    }

    static final String NULL = "<null>";

    /**
     *  Must not change the bytes, many Immutable classes rely on this.
     *  Note that null is returned as the string NULL == "<NULL>".
     */
    public static String data2hex(byte[] data)
    {
        if (data == null)
            return NULL;

        int len = data.length;
        StringBuffer buf = new StringBuffer(len*2);
        data2hex(buf, data, 0, len);
//        for (int pos = 0; pos < len; pos++) {
//            appendhex(buf, data[pos]);
////            buf.append(toHexDigit((data[pos]>>>4)&0x0F));
////            buf.append(toHexDigit((data[pos]    )&0x0F));
//        }
        return buf.toString();
    }
    

    /**
     * @return a byte array converted to hex, truncated beyond len
     *         and a small fraction added at the end.
     */
    public static String quick(byte[] b, int startlen)
    {
        if (b == null || startlen < 0)
            return NULL;
        if (b.length < startlen)
            startlen = b.length;
        StringBuffer buf = new StringBuffer(2 * (b.length - startlen) + 10);
        buf.append("[" + b.length + "] ");
        String middle = "..";
        int endlen = startlen / 5;
        if (0 == endlen)
            endlen = 1;
        if (endlen > 5)
            endlen = 5;
        if (b.length <= startlen) {
            endlen = 0;
            middle = ".";
        }
        
//        byte[] end = new byte[endlen];
//        byte[] start = new byte[startlen];

        if (b.length < (startlen + (middle.length() / 2) + endlen)) {
            data2hex(buf, b, 0, b.length);
//            return data2hex(b);
        } else {
            data2hex(buf, b, 0, startlen);
            buf.append(middle);
            data2hex(buf, b, b.length - endlen, endlen);
        }
        
        return buf.toString();

//        int i, j;
//        for (j = 0; j < start.length; j++)
//            start[j] = b[j];
//
//        for (j = 0, i = b.length - end.length; i < b.length; j++, i++)
//            end[j] = b[i];
//
//        return data2hex(start) + middle + data2hex(end);
    }

    /**
     * @return a byte array converted to hex, and truncated beyond some
     *         reasonable length, long enough to avoid debugging collisions.
     */
    public static String quick(byte[] b)
    {
        return quick(b, 8);
    }
    

    /*
     *  <p>
     *  Create a binary byte array with the hex characters converted.
     *  </p><p>
     *  Unchecked, GIGO.  Odd length sequences - undefined.
     *  </p>
     *
     *  @return always returns a byte array with the binary conversion,
     *          never returns null (empty on null argument)
     *  @see hex2dataOrNull for a checked method
     */
    public static byte[] hex2data(String str)
    {
        if (str == null)
            return new byte[0] ;

        int len    = str.length();    // probably should check length
        char hex[] = str.toCharArray();
        byte[] buf = new byte[len/2];
        for (int pos = 0; pos < len / 2; pos++)
        {
            byte hi = toDataNibble(hex[2*pos]);
            byte lo = toDataNibble(hex[2*pos + 1]);

            buf[pos] = (byte)( ((hi << 4) & 0xF0) | ( lo & 0x0F) );
        }

        return buf;
    }
}
