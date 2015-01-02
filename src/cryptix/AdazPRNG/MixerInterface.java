package cryptix.AdazPRNG;

import java.io.Closeable;


/**
 * A mixer is an aggregator of the output from multiple Collectors.
 * 
 * An implementation should do these things, in general:
 * 
 * <ul>
 *  <li> hash & whiten the output so as to preserve it from
 *       look-back attacks.</li>
 *  <li> protect its pool, if used, from easy access.</li>
 *  <li> protect its pool from wind-forward and wind-back attacks.</li>
 * </ul>
 *  
 * 
 * @author ada
 *
 */

public interface MixerInterface
    extends Closeable
{
    /**
     * Provide a set of bytes suitable for seeding a PRNG.
     * Should never block, but successive calls will likely
     * exhaust the entropy so far collected.
     * @param buf
     */
	public void nextBytes(byte[] buf);
	
	/**
	 * Add a new collector into the mix.
	 * Call this with as many Collectors as you can.
	 * @param c
	 */
	public void addCollector(CollectorInterface c);
}