package cryptix;

import java.math.BigInteger;


/** 
 * <p>This interface provides the one essential method of SecureRandom:
 * <code>    void nextBytes(byte[] buf)  </code>
 * It is thus compatible but not directly interchangeable.
 * As SecureRandom is final, the whole architecture must be replaced.</p>
 * 
 * <p>This class also provides the essential method for RSA generation:
 * <code>    BigInteger probablePrime(int bitLength)    </code>
 * which is needed to provide the RSA code with all it needs.</p>
 */
public interface PsuedoRandomNumberGenerator
{
    public void       nextBytes(byte[] b);
    
    
    // these should go into another interface eg ConvenientPRNG
    // but cannot until we can separate out probablePrime().
    
    /**
     * Prepare a slightly likely prime for RSA.
     * This method only uses a certainty of 1 (as param to BigInteger)
     * which is lowest probability.  The result
     * needs additional testing by the user code.
     * 
     * @param bitLength
     * @return
     */
    public BigInteger probablePrime(int bitLength);
    

    public int        randomInt(int i, int j);
    public int        randomInt();
    public long       randomLong();
}
