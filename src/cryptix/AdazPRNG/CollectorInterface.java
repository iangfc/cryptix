package cryptix.AdazPRNG;

import java.io.Closeable;

/**
 * A Collector that the Mixer pulls from when it needs new
 * RNs.
 * 
 * Q: Is a push collector (collector calls mixer and gives data)
 * or a pull model (mixer calls, pulling data from collector) better?
 * Currently we use a pull model (Mixer pulls from Collector).
 * 
 * A PUSH model assumes that the Collectors are active,
 * and it forces the Mixer to hold state as well as it must
 * cache the stuff that is pushed for the time it is needed.
 * In contrast, a PULL model allows the Mixer to be entirely
 * passive, and it allows the Collectors a choice of passive
 * or active.  Design-wise this removes large swathes of
 * complexity.
 * 
 * @author ada
 *
 */
public interface CollectorInterface
    extends Closeable
{
    /**
     * Pull array of length buf.length from the Collector.
     * Size is not guaranteed, Collectors are free to return less,
     * but they must return quickly.
     * 
     * This buffer must be assigned.  Caller should zero it first
     * to preserve privacy of other Collectors, but each Collector
     * should be verified and audited not to do anything with it.
     * See notes in Mixer.
     * 
     * @param byte[] buf is the buffer to fill
     * @return the number of bytes actually collected, starting from 0
     */
    public int nextBytes(byte[] buf);

	/**
	 * Mostly for those Collectors who have threads?
	 */
    public void close();
	
}
