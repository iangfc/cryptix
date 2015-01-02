package AdazPRNG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * <p>A collector for the Linux /dev/random, only useful for Linux.</p>
 * 
 * <p><b>On Linux only</b>, the device /dev/random is a blocking provider
 * of entropy.  It should represent the best efforts of the host
 * platform to deliver good fresh entropy.  Assuming it is all
 * in order this should be good, but the devil is in the details.
 * As the device blocks if there isn't enough entropy, this
 * collector keeps a stock in pool, and fills up after it is
 * consumed.</p>
 * 
 * <p>Under heavy multi-threaded usage, this Collector pool can
 * be exhausted faster than the thread can fill it, leading to
 * the Collector returning empty arrays.  It goes to some extent
 * to mitigate this behaviour, but in all probability, the
 * system /dev/random probably outperforms it.</p>
 * 
 * <h2>Discussion.</h2>
 * 
 * <p>Note that this old model was disposed of a long time ago by
 * the BSD variants, which made /dev/random a synonym (link)
 * for /dev/urandom.  This was because reasonable software
 * engineering showed that the use of the older blocking contract
 * to provide "pure" entropy was no longer a good decision.  It
 * might be noticed that this package follows the BSD design
 * decision after a lot of analysis.</p>
 * 
 * <p>Actually, even on Linux, the random device is no longer pure
 * entropy.  It draws from the same pool as the urandom, but
 * it measures the amount of entropy available and blocks until
 * the buffer is filled with what it believes is good entropy.
 * In contrast, urandom takes from the
 * same pool, and switches to a PRNG when the entropy is exhausted.</p>
 * 
 * <p>So, in essence, it is probably a waste of time to use this
 * Collector, because we'd be asking for strong entropy in a
 * system that is designed to not supply strong entropy but an
 * estimate only of strong entropy.</p>
 * 
 * @warning on *BSD, the /dev/random is a non-blocking PRNG device,
 *       linked with /dev/urandom so this distinction is meaningless
 *       and there is no point in using this class, use DevURandom
 * @see DevURandom
 * @see Linux random(4)
 * @author ada
 * @author iang
 */
public class CollectorDevRandom
    extends CollectorAbstract
    implements CollectorInterface
{
    public static final String DEV_RANDOM = "/dev/random";
    
	public  static int      POOL_SZ = 640;
	
	private final FileInputStream access;
	private final Thread          t;
	
	public CollectorDevRandom()
	    throws FileNotFoundException
	{
	    super( POOL_SZ );
	    buf = new byte[ POOL_SZ ];
        access = new FileInputStream(DEV_RANDOM);
		
        t = new Thread(new TopUp(), "cDevRand");
        t.setDaemon(true);
        t.start();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {    // this is my wakup call
            trace.append("  !");
        }
	}
	
	public synchronized void close()
	{
	    super.close();            // sets isClosed()
        t.interrupt();
	    try {
            access.close();
        } catch (IOException e) {
        }
	}
	
	/** how long to sleep if the pool is exhausted, in millisecs */
	public static final int EXHAUST_SLEEP = 1;
	/**
	 * How many times to sleep, in a loop, until giving up and returning empty.
	 * This is tuned at 3 so that the mainTest is 'unlikely' to fail,
	 * and the ordinary self-Test should never ever hit this.
	 * Set to 1 for regular fails.
	 */
	public static final int EXHAUST_ATTEMPTS = 13;
	
    /**
     * Provides unused pink entropy to the mixer.
     * May provide less than amount requested in buf.
     *
     * In an attempt to be nice, the call will wait up to
     * 3 times * 1ms in order for the poller to collect some
     * entropy, if it is detected that there isn't sufficient.
     * However, under massively multi-threaded banging, this
     * isn't sufficient to provide enough space for the
     * poller to wake up from its interrupt and fight it
     * out with the caller threads.  Under such conditions
     * don't use this Collector, something else will be
     * needed.
     * 
     * After this method is called, the entropy poller
     * is interrupted to re-full the pipe.
     * 
     * @param buf length may be less than requested
     */
    public synchronized int nextBytes(byte[] buf)
    {
        int limit = buf.length > size() ? size() : buf.length;
        for (int i = 0; i < EXHAUST_ATTEMPTS; i++) {
            if (super.available() >= limit) {
                break ;
            }
            try {
                if (i > 0)
                    trace.append("+" + i);
                Thread.sleep(EXHAUST_SLEEP);
            } catch (InterruptedException e) {    // shouldn't happen
                trace.append("  ?");
            }
        }
        int numbytes = super.nextBytes(buf);
        doTopUp();
        return numbytes;
    }
	

    /*
     * We want to topup any lost bytes.
     */
	private void doTopUp()
	{
	    trace.append(" bing ");
	    t.interrupt();
	}
	
	private final byte[] buf;

	/**
	 * this works in a thread so as to
	 * fill pool with entropy from /dev/random.
	 */
	private int topup()
	{
	    if (mainTest) {
            trace.append("   BING ");
	    }
		final int fullLength = POOL_SZ - available();
		//byte[] buf = new byte[len];

		int i   = 0;
		int off = 0;
		int len = fullLength;
		while (len > 0){
			try {
			    i = access.read(buf, off, len);
				if (mainTest || i != len) {
				    trace.append(" read " + i + " of " + len);
			    }
			} catch (IOException e) {
			    if (! isClosed()) {
				    throw new RuntimeException("read of /dev/random failed", e);
			    }
			}
			len -= i;
			off += i;
		}
		trace.append(" COLLECT " + off + " of " + buf.length + "\n");
		try {
            collect(buf, 0, fullLength);
		} catch (Throwable t) {
		    if (mainTest) {
		        System.err.println("EFiFO TRACE " + mouthful(EntropyFIFO.trace));
                System.err.println("Collector TRACE " + mouthful(trace));
		    }
            throw t;
		}
		return off;
	}
	/**
	 * Asks for new random data from /dev/random
	 */
	private class TopUp extends Thread{
	    
		public void run(){
		    while (! isClosed()) {
                topup();
                try {
                    Thread.sleep(100000000);
                } catch (InterruptedException e) {    // this is my wakup call
                    trace.append("  @");
                }
		    }
		    trace.append("\n      ***** CLOSED ******\n");
		}
	}
	
	

    public String toString() { return "Collector: " + DEV_RANDOM + " " + super.toString(); }
    
    public static String selfTest()
    {
        CollectorDevRandom rc = null;
        String s = "";
        /*
         * These are set to very long for mainTest as the
         * threading can block on the read and trigger short
         * reads, we want the tests to pick them up.
         */
        int zeroTnum = mainTest ? 1000 : 10;   // number of cycles
        int highT    = mainTest ? 40 : 10;     // number of threads

        try {
            rc = new CollectorDevRandom();
            selfTest(rc, zeroTnum, trace);
            s += "  straight " + zeroTnum + ";  ";
        } catch (Throwable e) {
            System.err.println(e + " -- " + s);
            System.err.println("TRACE TO FOLLOW:");
            System.err.println( mouthful(trace) );
            System.exit(2);
        }
        
        try {
            trace.delete(0, trace.length());
            s += selfTestThreads(rc, 2);
            s += "  low Threads 2;  ";
            trace.delete(0, trace.length());
            s += selfTestThreads(rc, highT);
            s += "  High Threads " + highT + ";  ";
        } catch (Throwable e) {
            System.err.println(e + " -- " + s);
            System.exit(2);
        }
        
        rc.close();
        return s;
    }

	public static void main(String[] args)
	{
	    mainTest = true;
		String s = selfTest();
		System.out.println(s + "\n");
	}
}
