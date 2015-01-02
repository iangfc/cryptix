package AdazPRNG;

import cryptix.Digest;
import alg.SHA512Digest;



/**
 * Each request for nextBytes results in a hashing that then
 * gets recycled into the mix.
 * 
 * This approach is useful when the incoming bytes have variable
 * amounts of entropy, and time demands are uncertain.
 * 
 * @author iang
 */
public class CollectorHasher implements CollectorInterface
{
    
    private final Digest md;
    public final static int LEN = 64;
    private int         input = 0;
    
	private boolean     closed = false;
    public void         close()               { closed = true; md.reset(); }
    public boolean      isClosed()            { return closed; }
    
    
	
	public CollectorHasher()         { md = new SHA512Digest(); }
	
	/**
	 * Collect should be called by a thread in the subclass
	 * so as to push another lump of entropy into the pool.
	 */
	protected void      collect(byte[] b, int o, int l)   { md.update(b, o, l); input += l - o; }
    protected void      collect(byte[] buf)               { collect(buf, 0, buf.length); }
    
    /**
     * Provides random numbers to the mixer.
     * Never blocks.
     * Will take 1 message digest as long as b.length is standard,
     * else will take 1 more, probably.
     * 
     * @return the number of bytes put into b, min(LEN, b.length), left-aligned
     */
    public int          nextBytes(byte[] b)
    {
        int len = LEN;
        if (b.length < len)
            len = b.length;
        
        if (len == LEN) {
            md.doFinal(b, 0);
            md.update(b, 0, LEN);
        } else if (len < LEN) {
            byte[] tmp = new byte[LEN];
            md.doFinal(tmp, 0);
            for (int i = 0; i < b.length; i++) {  // first clean it
                b[ i ] = 0;
            }
            for (int i = 0; i < LEN; i++) {       // then roll bytes in
                b[ i % b.length ] ^= tmp[i];
            }
            Mixer.destroy(tmp);
        }
        input = 0;
        return len;
    }
    
	/** @return the number of available bytes, for calculating how many are required */
	protected int       available()           { return LEN; }
	protected int       size()                { return LEN; }
	
    
    public String toString()
    {
        String s = "Col.Hash ";
        if (isClosed())
            s += "CLOSED ";
        else
            s += input + " / " + LEN;
        return s;
    }
	
}
