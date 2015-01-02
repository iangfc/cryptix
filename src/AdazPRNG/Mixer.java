package AdazPRNG;

import java.util.Arrays;
import java.util.HashSet;

import cryptix.Digest;
import alg.SHA512Digest;
import cryptix.X;

/**
 * @see {#link package-info} for Design Logic.
 * 
 * Because the Mixer is the critical part in the RNG, it is obsessive
 * about testing.  There's no way to prove objectively that the Mixer is
 * correct.  Hence, lots of basic tests.
 * 
 * 
 * @author Ada
 */
public class Mixer
    implements MixerInterface
{
	private HashSet<CollectorInterface> collector_set = new HashSet<CollectorInterface>();
	
	public void addCollector( CollectorInterface c){ collector_set.add(c); }
    private CollectorInterface[] asArray() { return collector_set.toArray(new CollectorInterface[0]); }
    public int numCollectors()             { return collector_set.size(); }

    public void close()
    {
        CollectorInterface[] cis = asArray();
        for (int i = 0; i < cis.length; i++) {
            CollectorInterface ci = cis[i];
            ci.close();
        }
    }
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        b.append("Mixer: " + numCollectors() + " -- ");
        CollectorInterface[] cis = asArray();
        for (int i = 0; i < cis.length; i++) {
            b.append(i + ": " + cis[i].toString());
        }
        return b.toString();
    }
    
	
	/**
	 * This Mixer's internal block is aligned with SHA512 or 64 bytes blocks
	 * (BLOCKLEN).  If a request is made for longer lengths, it will invoke the
	 * cycle again, and take a bit longer.
	 * This should never happen if used for the standard PRNGs as they only
	 * need that amount of seed.
	 * 
	 * @param callerBuf will be filled with the next mixed bytes,
	 *        must be multiple of BLOCKLEN
	 */
	public void nextBytes(byte[] callerBuf) {
		Digest md = getWhitener();
	    nextBytes(callerBuf, md);
	}

	/*
	 * Uses a BlankWhitener which does nothing, so the
	 * result can be tested against various expected patterns.
	 */
    private void nextBytesTEST(byte[] callerBuf) {
        Digest md = getBlankWhitener();
        nextBytes(callerBuf, md);
    }
    
    private final byte[] reuseBlock = new byte[BLOCKLEN];
	
    private void nextBytes(byte[] callerBuf, Digest md) {
        CollectorInterface[] cis = asArray();
        
        int q = 0;
        while (q < callerBuf.length) {
        
            for (int c = 0; c < cis.length; c++) {
                CollectorInterface ci = cis[c];
                
                /*
                 * What is the best interface for the collector?
                 * nextBytes(int len) or nextBytes(byte[] buf)?
                 * It's partly about attack surface, partly about
                 * software engineering.
                 * 
                 * len causes fresh buffers so existing collected bytes
                 * are not seen by collectors, but those buffers can't
                 * be cleaned b/c of JIT, and leak into the GC.
                 * Whereas, byte[] can clean each buf with each rotation,
                 * but shares each previous one.
                 * 
                 * Well, we should trust the collectors.  Use byte[] and
                 * clean up the buf at the end in a once-of.
                 * 
                 * The real solution here is to clean bufs, but we have
                 * no easy way to do this, see the step at the end.
                 */
                int qtyCollected = ci.nextBytes(reuseBlock);
                log2("ci " + c + " --> " + X.quick(reuseBlock));
                md.update(reuseBlock, 0, qtyCollected);
            }

            finalise(md, callerBuf, q);
            q += BLOCKLEN;
        }
        
        destroy(reuseBlock, md);  // do my own internal expensive clean!
        
        return ;
    }
	
	public static final int BLOCKLEN = 64; // happens to be ChaCha and SHA512 blocklen
	
	private Digest getWhitener() {
	    return new SHA512Digest();
	}
	
	private class BlankDigest extends SHA512Digest {
	    private byte[] dummy = new byte[BLOCKLEN];
	    private int q = 0;
	    @Override
        public void update(byte[] plaintext, int offset, int len) {
            for (int i = offset; i < plaintext.length; i++) {
                dummy[q++] ^= plaintext[i];
                //System.err.println("Q " + (q-1) + " dummy[Q] " + dummy[q-1]);
                if (q >= BLOCKLEN) {
                    q = 0;
                }
            }
        }
	    @Override
	    public void update(byte x) {
	        dummy[q++] ^= x;
	        //System.err.println("q " + (q-1) + " dummy[q] " + dummy[q-1]);
            if (q >= BLOCKLEN) {
                q = 0;
            }
	    }
	    public int doFinal(byte[] digest, int offset) {
	        System.arraycopy(dummy, 0, digest, offset, BLOCKLEN);
	        return 0;
	    }
	    public void reset() {
	        dummy = new byte[BLOCKLEN];
	        q = 0;
	    }
	}

    private Digest getBlankWhitener() {
        Digest md = new BlankDigest();
        return md;
    }
	
	
	/**
	 * Finalise the MD into the buf, starting at offset.
	 * If the block lengths are unaligned and the buf is smaller,
	 * it will rotate back to the beginning (0) and XOR the remaining
	 * bytes in.
	 * 
	 * @param md
	 * @param buf
	 * @param offset
	 * @return
	 */
	private void finalise(Digest md, byte[] buf, int offset) {
	    if (offset > buf.length)    throw new RuntimeException("ywjf2849 " + offset + "/" + buf.length);
	    
	    if (buf.length - offset >= BLOCKLEN) { // md fits in remaining buf
	        md.doFinal(buf, offset);
	        return ;                           // no funny biz, no cleanup
	    }
	    
	    md.doFinal(reuseBlock, 0);
	    //System.err.println("doFinal: " + Hex.quick(full_digest, BLOCKLEN));
        
	    int copyLen = buf.length - offset;
	    if (copyLen > BLOCKLEN) {
	        copyLen = BLOCKLEN;
	    }
	    int p = offset;
        /* copy in final whitened result so caller cannot see intermediates */
	    for (int i = 0; i < BLOCKLEN; i++, p++) {
	        if (p >= buf.length)
	            p = 0;
	        buf[p] ^= reuseBlock[i];
	    }
	}

	/**
	 * Clunky but workable destroy() method that is independent
	 * of all other code.  It uses a new SHA512 to hash the byte
	 * array and thus render it non-revealable.
	 * 
	 * Note that this method does not return null, as that would be
	 * a clue to JIT that this method doesn't provide a readable
	 * result.
	 * 
	 * @param buf that has to be destroyed.
	 */
    static void destroy(byte[] buf) { destroy(buf, new SHA512Digest()); }

	/*
	 * It's very hard to destroy the bytes because the JIT will
	 * drop anything it can show is worthless.
	 * Here, hash the buffer to destroy the seed,
	 * and count the population to make sure the JIT
	 * doesn't drop the useless step.
	 * 
	 * This destroy() method results in less dependencies,
	 * but because of the locally limited availability of assets,
	 * requires a run of SHA512, at least once.
	 * 
	 * This assumes that SHA512 doesn't leak state :(
	 */
    private static void destroy(byte[] buf, Digest md) {
        if (0 > buf.length) throw new RuntimeException("ywjf2849");
        
        int chaos = 10;
        do {
            int p = 0;
            while (p < buf.length) {
                if (buf.length - p >= BLOCKLEN) {
                    md.update(buf, p, BLOCKLEN);
                    md.doFinal(buf, 0);
                    p += BLOCKLEN;
                } else {
                    byte[] tmp = new byte[BLOCKLEN];
                    int length = buf.length - p;
                    md.update(buf, p, length);
                    md.doFinal(tmp, 0);
                    System.arraycopy(tmp, 0, buf, p, length);
                    p += length;
                }
            }
            
            /*
             * There is a tiny chance that the thing isn't random,
             * so the below test fails.  Just run it again.
             * This only happens with very small byte arrays.
             */
        } while ( ! isRandom(buf) && chaos-- > 0) ;
        if (0 == chaos) {
            throw new RuntimeException("chaos! " + X.data2hex(buf));
        }
    }
    
    private static boolean isRandom(byte[] buf) {
        int popn = popn(buf);
        return (3 * buf.length <= popn) && (popn <= 5 * buf.length) ;
    }

    private static void checkRandom(byte[] buf) {
        int popn = popn(buf);
        if ( ! (3 * buf.length <= popn && popn <= 5 * buf.length))
            throw new RuntimeException("not random buf " + popn + " ==> " + X.data2hex(buf));;
    }

    /**
     * @return number of bits that are set in the byte[] passed
     */
    private static int popn(byte[] buf)
    {
        int popn = 0;
        for (int i = 0; i < buf.length; i++) {
            popn += popn(buf[i]);
        }
        return popn;
    }
    
    /**
     * OK, so we could speed this up with a lookup table... whatever.
     * 
     * @return number of bits that are set in the byte passed
     */
    private static int popn(byte b)
    {
        int bits = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 0x01) == 0x01)
                bits++;
            b >>= 1;
        }
        return bits;
    }
    
	
	
	
/////////////////// TEST CASES ////////////////////////////////	
	
	private static String d = "";
	private static void log(String s) {
	    d += "\n" + s;
	}
    private static void log2(String s) {
        if (!isSelfTest) {
            d += "\n" + s;
        }
    }
    
    private static void testUtils() {
        byte[] blah = new byte[BLOCKLEN];
        if (0 != popn(blah))
            throw new RuntimeException("IS random blah?? " + X.data2hex(blah));
        if (isRandom(blah))
            throw new RuntimeException("IS random blah?? " + X.data2hex(blah));
        destroy(blah, new SHA512Digest());
        //System.err.println("blah " + Hex.data2hex(blah));
        checkRandom(blah);
        final String shaEmptyString = "7be9fda48f4179e611c698a73cff09faf72869431efee6eaad14de0cb44bbf66503f752b7a8eb17083355f3ce6eb7d2806f236b25af96a24e22b887405c20081";
        final byte[] shaEmpty = X.hex2data(shaEmptyString);
        if (! Arrays.equals(blah, shaEmpty))
            throw new RuntimeException("SHA512 not working\nblah: " + X.data2hex(blah) + "\nthis: " + shaEmptyString);
    }

    private static void testBadCollector(CollectorInterface c, byte b)
    {
        for (int i = 0; i < 10; i++) {
            int len = Support.exampleInt(1,129);
            byte[] buf = new byte[len];
            int got = c.nextBytes( buf );
            if (got != len) {
                throw new RuntimeException("len " + len + " != " + got + " got");
            }
            for (int j = 0; j < len; j++) {
                if (b != buf[j]) {
                    throw new RuntimeException("c not " + b + "\n" + X.data2hex(buf) + "\n" + c);
                }
            }
        }
    }

    public static void testInternalCollectors() {
        CollectorInterface c;
        c = new FixedCollector((byte)0);
        testBadCollector(c, (byte)0);
        c = new FixedCollector((byte)'5');
        testBadCollector(c, (byte)'5');
        
        for (int i = 0; i < 10; i++) {
            byte b = (byte) Support.exampleInt(0,255);
            c = new FixedCollector(b);
            testBadCollector(c, b);
        }
        
        log2("=======================");
        log("testInternalCollectors - ok.");
    }
    
	/**
	 * Tests numCollectors together into a mix, where one is an
	 * 'x' FixedCollector and the others are '0' FixedCollectors.
	 * @param numCollectors
	 * @param x
	 */
	public static String testSingleInt(final int numCollectors, byte x)
	{
		String s = "single: ";
		//@SuppressWarnings("resource")
        Mixer mix = new Mixer();
		CollectorInterface[] collectors = new CollectorInterface[numCollectors];
		final int blockLen = BLOCKLEN;
    	
		int lucky = Support.exampleInt(0, collectors.length-1);
    	for(int i = 0; i < collectors.length; i++) {
    		if (lucky == i) {
            	collectors[i] = new FixedCollector((byte)x);
    		} else {
            	collectors[i] = new FixedCollector((byte)0);
    		}
    		mix.addCollector(collectors[i]);
    	}
    	
    	byte[] buf = new byte[blockLen];
    	mix.nextBytesTEST(buf);
    	log2("sSI " + X.quick(buf, blockLen));
    	if (! allBytes(buf, x)) {
    	    mix.close();
    		throw new RuntimeException("simple test failed, bytes are not all " + x + "\n" +
    				"blen: "+buf.length +"\tb: " + s);
    	}
    	
    	sanityCheck(mix, buf);
    	
    	return s;
    }
	
	private static void sanityCheck(Mixer mix, byte[] buf)
	{
	    mix.nextBytes(buf);
        mix.close();                // can't now diag in Panic below...
        checkRandom(buf);
        log2("\n    sanity: " + X.quick(buf));
	}
	
	static byte ex() { return (byte) Support.exampleInt(0, 255); }
	
//	static byte takeBits(byte x)
//	{
//	    int popn = X.popn(x);
//	    return x;
//	}

	/*
	 * @return a byte array such that an XOR of all bytes
	 * in the array will be 0xFF
	 */
    private static byte[] getBytesForOne(int len)
    {
        byte[] bytes = new byte[len];
        if (1 == len) {
            bytes[0] = (byte)255;
            return bytes;
        }

        int bitPosition = 1;
        for (int b = 0; b < 8; b++) {
            int index = Support.exampleInt(0, len-1);
            bytes[index] |= (0x00FF & bitPosition);
            bitPosition <<= 1;
        }
        
//        for (int i = 0; i < len; i++) {
//            
//            byte y = takeBits(left);
//            left &= ~y;
//            bytes[i] = y;
//        }
//        
//        // hack - only puts numbers in 2 of the collectors.
//        byte x = ex();        
//        bytes[0] = x;
//        byte left = (byte) (0x00FF & ~x);
//        
//        for (int i = 1; i < (len-1); i++) {
//            byte y = takeBits(left);
//            left &= ~y;
//            bytes[i] = y;
//        }
//        
//        bytes[len-1] = left;
        return bytes;
    }
    
    static void getCollectorsForFF(CollectorInterface[] collectors)
    {
        int len = collectors.length;
        byte[] bytes = getBytesForOne(len);
        for (int i = 0; i < len; i++) {
            log2("  -- " + i + " -- " + bytes[i]);
            collectors[i] = new FixedCollector( bytes[i] );
        }
    }
    
    static void testXORIntegerCollectorsToFF(final int numMixers)
    {
        if (0 >= numMixers) throw new RuntimeException("apmx7284 " + numMixers + " numMixers<=0");
        
        Mixer mix = new Mixer();
        CollectorInterface[] collectors = new CollectorInterface[numMixers];
        final int blockLen = BLOCKLEN;
        
        getCollectorsForFF(collectors);
        for(int i = 0; i < collectors.length; i++) {
            mix.addCollector(collectors[i]);
        }
        
        byte[] buf = new byte[blockLen];
        mix.nextBytesTEST(buf);
        log2("tXOR " + X.quick(buf, blockLen));
        
        if (! allBytes(buf, (byte)0x00FF)) {
            mix.close();
            throw new RuntimeException("XOR test failed, bytes are not all 0x00FF\n" +
                    "buf: " + X.quick(buf));
        }
        
        sanityCheck(mix, buf);
    }
	
    public static void testSingleIntCollectors() {
        //System.err.println("\n\n====================================2");
        for (int i = 0; i < 4; i++) {
             testSingleInt(i, (byte)0);
        }
        log2("Zero test 0-3 success.  ");
        
        for (int i = 1; i <= 4; i++) {
            //System.err.println("-----------------------------2." + i);
            testSingleInt(i, (byte)1);
        }
        log2("One test 1-4 success.  ");
        for (int i = 1; i <= 4; i++) {
            //System.err.println("-----------------------------2." + i);
            testSingleInt(i, (byte)Support.exampleInt(0, 255));
        }
        log2("Random test 1-4 success.  " +
             "\n===============================\n");
    }

    
    public static void testXORIntegerCollectorsToFF() {
        //System.err.println("\n\n====================================2");
        int qC = 5;
        int num = 100;
        for (int len = 1; len <= qC; len++) {
            for (int i = 0; i < num; i++) {
                log2("go " + len + " at " + i);
                testXORIntegerCollectorsToFF(len);
            }
        }
        log2("XOR test 0-"+qC+" * "+num+" success.  " +
             "\n===============================\n");
    }
    
    private static boolean isSelfTest = true;
	public static String selfTest() {
	    String s = "";
        testUtils();
        s += " utils; ";
		testInternalCollectors();
		s += " internal; ";
		testSingleIntCollectors();
		s += " singleInt; ";
        testXORIntegerCollectorsToFF();
        s += " XOR; ";
    	return s;
    }

	public static boolean allBytes(byte[] buf, byte x) {
		for (int i = 0; i < buf.length; i++) {
			if (buf[i] != x)
				return false;
		}
		return true;
	}
	
    public static void main(String[] args){
        isSelfTest = false;
        try {
            String s = selfTest();
            System.out.println(d);
    	    System.out.println("Mixer Test: " + s);
        } catch (Throwable t) {
            System.err.println(d);
            System.err.println("EX: " + t);
        }
    }
    
    


	/** Collector that always returns b */
	private static class FixedCollector implements CollectorInterface {
	    private final byte b;
		public FixedCollector(byte b) { this.b = b; }
//		public byte[] nextBytes(int len) {
//		    byte[] buf = new byte[len];
//            for (int i = 0; i < buf.length; i++)
//                buf[i] = b;
//            return buf;
//        }
        public int nextBytes(byte[] buf) {
            for (int i = 0; i < buf.length; i++)
                buf[i] = b;
            return buf.length;
        }
		public void close()      {  }
		public String toString() { return "FC b=" + b; }
	}
	
// /** Collector that always returns null. */
//  @SuppressWarnings("unused")
//    private static class NullCollector extends CollectorAbstract {
//      public NullCollector(int size) { super(size); }
//      protected void collect(byte[] buf) {
//          for (int i = 0; i < buf.length; i++)
//              buf[i] = (byte) 0x00; // 0x0a;
//          super.collect(buf);
//      }
//      public String toString() { return "Null: " + 0; }
//  }
//	@SuppressWarnings("unused")
//    private static class FalseCollector extends CollectorAbstract {
//		final int byteMe;
//		public FalseCollector(int size, int pattern) { super(size); byteMe = pattern; }
//		public void nextBytes(byte[] buf) {
//			for (int i = 0; i < buf.length; i++)
//				buf[i] = (byte) byteMe;
//			System.err.println("FCxx: "+ byteMe +"\tb: " + Hex.data2hex(buf));
//			super.collect(buf);
//		}
//		public String toString() { return "False: " + byteMe; }
//	}
	
}
