package cryptix;

/** 
 * <p>This interface provides the one essential method of SecureRandom:
 * <code>    void nextBytes(byte[] buf)  </code>
 * It is thus compatible but not directly interchangeable.
 * As SecureRandom is final, the whole architecture must be replaced.</p>
 * </p>
 */
public interface PsuedoRandomNumberGenerator
{
    public void       nextBytes(byte[] b);
    
    
    // these should go into another interface eg ConvenientPRNG
    // but cannot until we can separate out probablePrime().
    
    public int        randomInt(int i, int j);
    public int        randomInt();
    public long       randomLong();
}
