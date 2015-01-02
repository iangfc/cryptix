package cryptix;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.SecureRandom;

import cryptix.AdazPRNG.CollectorInterface;
import cryptix.AdazPRNG.CollectorJavaRandom;
import cryptix.AdazPRNG.CollectorURandom;
import cryptix.AdazPRNG.ExpansionFunctionInterface;
import cryptix.AdazPRNG.Mixer;
import cryptix.AdazPRNG.StatefulChacha;

/**
 * <p>Initialisation of a single psuedo-random number generator (PRNG)
 * for all of the application.  In PRNG theory, one should be adequate
 * for all, else the theory has a problem ==> fix the practice to
 * meet this theory.</p>
 * 
 * <p>This class provides the following methods:</p
 * 
 * <ul><li>
 *   <p>the one essential method of SecureRandom:</p>
 *   <code>    void nextBytes(byte[] buf)  </code>
 *   <p>It is thus compatible but not directly interchangeable (as final).</p>
 * </li><li>
 *   <p>the essential method for RSA:</p>
 *   <code>    BigInteger probablePrime(int bitLength)    </code>
 *   <p>which is needed to provide the RSA code with all it needs.</p>
 * </li><li>
 *   <p>for convenience, the most popular methods for user code:</p>
 *     <code>
 *       int randomLong();
 *       int randomInt();
 *       int randomInt(int min, int max);
 *     </code>
 *   <p>which generally cover most needs.</p>
 * </li></ul>
 * 
 * @warning requires internal replacement of proxy to SecureRandom
 * 
 * <p>This class is not reviewed.  It is a convenient replacement for
 * Java SecureRandom.
 * In the past, use of Java's cryptography engine (JCE) for any purpose
 * has been fraught with danger (c.f., TrulyRandom, Bitcoin,
 * RSA/BSAFE, 'crypto-strength' backdoor files).
 * As of now, in particular, Java returns
 * a generator from some randomly supplied 3rd party supplier,
 * so no review or statement of reliability is possible.</p>
 * 
 * <p>As Java's class is final, it is not possible to get control of
 * the cryptographic reliability issue without replacing it entirely.
 * This present class represents that replacement, as the last
 * step to replace the usage of JCE in all caller code.</p>
 * 
 * 
 * 
 * <p>FIXME: Needs:</p>
 * <ul><li>
 *     review,
 *   </li><li>
 *     replacement for BigInteger.probablePrime(SecureRandom) mode, and
 *   </li><li>
 *     Need to add a destroy method.
 * </li></ul>
 * @see SensorManager {@link https://developer.android.com/reference/android/hardware/SensorManager.html}
 */
public class PRNG
    implements PsuedoRandomNumberGenerator
{
    private PRNG() { }


    /**
     * <p>
     * SecureRandom will block if the entropy for the system is not
     * set up.  This could happen deep within Sun code.
     * XXX: FreeBSD 4, see rndcontrol(8).
     * XXX: FreeBSD 5, /dev/random no longer blocks AT ALL ...  Need to check.
     * </p>
     */
    private static SecureRandom privSR;

    /**
     * Sort out some standard collectors,
     * ones that start up straight away and are universally applicable.
     */
    private static void init()
    {
        if (privSR != null) return ;
        privSR = getNewSecureRandom();   // only needed for probablePrime

        // check out the old SecureRandomHack?
        
        addCollector( new CollectorJavaRandom() );
//        X.info("added old crappy Java SecureRandom " + privSR);
        
        String osName = System.getProperty("os.name").toLowerCase();
        if (! osName.startsWith("win")) {
            try {
                CollectorInterface c = new CollectorURandom();
                addCollector( c );
                X.info("successfully added CollectorURandom " + c);
            } catch (FileNotFoundException e) {
                X.error("Collector File not found? " + e);
            }
        }
    }
    
    private static SecureRandom getNewSecureRandom()
    {
        long tim = System.nanoTime();
        X.info("Random.getSecureRandom: Init secureRandom ..." +
                " this could take some time.");
        // should this be syncronised?  Probably doesn't matter.
        SecureRandom sr = new SecureRandom();
        long delay = System.nanoTime() - tim;
        X.info("Finished init securerandom -- " + delay + "ns.");
        return sr;
    }
    
    private static Mixer mix = new Mixer();
    public static void   addCollector(CollectorInterface c)   { mix.addCollector(c); }

    private static int numCollectors = 0;
    private static ExpansionFunctionInterface goodSharedPRNG = null;
    
    /**
     *  Get the PRNG.
     *  This is a good or reasonable shared PRNG that is long running.
     *  We should probably have another interface for ultra-sec work
     *  such as making keys.
     */
    public static PsuedoRandomNumberGenerator get()
    {
        init();

        if (goodSharedPRNG == null || mix.numCollectors() > numCollectors) {
            
            X.info("MAKING NEW PRNG " + numCollectors);
            synchronized(PRNG.class) {
                // check if we are still needy, in case of race conditions
                if (goodSharedPRNG == null || mix.numCollectors() > numCollectors) {
                    StatefulChacha prng = new StatefulChacha();
                    byte[] seed = new byte[prng.getSeedLen()];
                    mix.nextBytes(seed);
                    prng.init(seed);
                    prng.nextBytes(seed);    // destroy the seed!
                    
                    if (null != goodSharedPRNG) {
                        goodSharedPRNG.close();
                    }
                    goodSharedPRNG = prng;
                    numCollectors = mix.numCollectors();
                    X.info("MADE NEW PRNG ==> " + mix.toString());
                }
            }
        }
        
        return new PRNG() ;  // just this shell, access internals
    }
    
    

    
//    /**
//     *  @deprecated
//     *  @return the old SecureRandom 
//     */
//    public static SecureRandom getInstance()        { init(); return privSR; }
//    /** synonym of getInstance() for now
//     * @deprecated
//     */
//    public static SecureRandom getSecureRandom()    { return getInstance(); }
    
    /**
     * Prepare a slightly likely prime for RSA.
     * This method only uses a certainty of 1 (as param to BigInteger)
     * which is lowest probability.  The result
     * needs additional testing by the user code.
     * 
     * @param bitLength
     * @return
     */
    @Override
    public BigInteger probablePrime(int bitLength)
    {
        X.warn("PRNG old SecureRandom used to generate BigInteger");
        return new BigInteger(bitLength, 1, privSR);
    }
    
    private static final boolean USE_ADAZ = true;

    @Override
    public void nextBytes(byte[] b)
    {
        if (USE_ADAZ) {
            goodSharedPRNG.nextBytes(b);
            //log.warn("PRNG new ADAZ " + Hex.data2hex(b));
        } else {
            privSR.nextBytes(b);
        }
    }

    
    
    
    
    /**
     * @return a random int between min and max (inclusive, swapped if applicable)
     */
    @Override
    public int randomInt(int min, int max)
    {
        if (min == max)
            return min;

        if (min > max)  // swap, and carry on
        {
            int x = min;
            min = max;
            max = x;
        }
        long min2 = min;
        long max2 = max;
        long offset = 0;
        if (min2 < 0)   // move numbers to 0..n
        {
            offset = (-min2);
            max2 += offset;
            min2 = 0;
        }
        
        /*
         * Note that there is a small bias introduced by modular
         * arithmetic, if the paramaters are not bit-aligned.
         * This is somewhat mitigated by using a randomLong()
         * and assuming the diff (max-min) is small.
         * E.g., consider x = y % 3 where y is a 2 bit random number.
         */
        long l = randomLong();
        if (l < 0)
            l = -l;
        int rand = (int) ( ( l % (max2 - min2 + 1) ) + min2 - offset );
        if (rand < min || rand > max)
            throw new RuntimeException("PRNG2684 rand out of min,max: "+
              rand+" "+min+","+max+ "  ["+min2+","+max2+"] ("+l+","+offset+")" );
        return rand;
    }
    
    final static int BYTE_MASK = 0x00ff;
    
    @Override
    public int randomInt()
    {
        byte[] buf = new byte[4];
        nextBytes(buf);
        int random = 0;
        for (int i = 0; i < 4; i++) {
            random |= buf[i] & BYTE_MASK;
            random <<= 8;
        }
        
        return random;
    }

    @Override
    public long randomLong()
    {
        byte[] buf = new byte[8];
        nextBytes(buf);
        long random = 0;
        for (int i = 0; i < 8; i++) {
            random |= buf[i] & BYTE_MASK;
            random <<= 8;
        }
        
        return random;
    }
}
