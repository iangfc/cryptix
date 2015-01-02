package cryptix.AdazPRNG;

import cryptix.X;
import webfunds.util.Statistics;



/**
 * This class provides a pool and a test framework.
 * 
 * The pool is only useful if there is a need for caching, and
 * the input is already near-random or distributed, and does not
 * need further hashing.
 * 
 * @author ada
 */
public abstract class CollectorAbstract implements CollectorInterface
{
    
	private final EntropyFIFO pool;
	private boolean     closed = false;
    /** This method is safe call even if the pool was not allocated. */
    public void         close()               { closed = true; if (null != pool) pool.close(); }
    public boolean      isClosed()            { return closed; }
    
    
	
	public CollectorAbstract(int size) { pool = new EntropyFIFO(size); }
	/** Does not allocate a pool, methods in this class must not be called  */
	public CollectorAbstract()         { pool = null; }
	
	/**
	 * Collect should be called by a thread in the subclass
	 * so as to push another lump of entropy into the pool.
	 */
	protected void      collect(byte[] buf)   { pool.push(buf, 0, buf.length); }
	protected void      collect(byte[] b, int o, int l)   { pool.push(b, o, l); }

    /**
     * Provides unused pink random numbers to the mixer.
     * Never blocks.
     * @return the length read in, may be less than b.length, left-aligned
     */
    public int          nextBytes(byte[] b)   { return pool.nextBytes(b); }
    
	/** @return the number of available bytes, for calculating how many are required */
	protected int       available()           { return pool.available(); }
	protected int       size()                { return pool.size(); }
	
    
    public String toString()
    {
        String s = "";
        if (isClosed()) s += "CLOSED ";
        if (null != pool) s += pool.toString();
        return s;
    }
	
    
    
    

//////////////////////////////////////////////////////////////
//
//  SELF testing code.  This is typically stored in the same
//  class file so as to give the reader no excuse but to run
//  it on any changes...
//
    
	protected final static StringBuffer trace = new StringBuffer();
    protected static void selfTest(CollectorInterface c, int num) { selfTest(c, num, trace); }
    
	protected static void selfTest(CollectorInterface c, int num, StringBuffer t)
	{
	    int size = 1;
        for (int i = 0; i < num; i++) {  // mainly, test that the numbers look random
            int q = 100;
            Statistics sss = new Statistics(q);
            
            for (int j = 0; j < q; j++) {
                if (testFailed >= 0)
                    return ;
                
                byte[] r = new byte[size];
                int qty = c.nextBytes(r);
                if (mainTest && null != t)
                    t.append("run " + j + ": " + size + " got " + qty + "\n");
                long p = X.popn(r);
                if (mainTest && null != t) {
                    t.append("run " + i + ": " + p + " " + (0==qty ? "/ZERO" : (p / qty)) + " bpb"
                             + X.quick(r) +"\n");
                }
                X.checkPopulation(r);
                sss.push(p);
                if (qty != size && !acceptShortBytes) {
                    /*
                     *  XXX actually this is too harsh.
                     *  A collector is always allowed to return a short measure.
                     *  However, we know the 3 collectors listed should be good
                     *  for it.
                     */
                    String s = c.toString() + "\n" +
                            qty + " qty != size " + size +
                            " (" + i + ", " + j +") " +
                            "trace=" + trace.length() + " t=" + t.length() + 
                            "\n   stats -- " + sss.toString() +
                            "";
                    throw new RuntimeException(s);
                }
                
                size = (r.length == 0) ? 30 : (0x00FF & r[0]); 
            }
            if (mainTest && null != t) {
                String s = "Complete: Expected " + (size * 8 / 2) + " versus " + sss.toString() + "\n";
                t.append(s);
            }
        }
	}
	
    static int testFailed = -1;
    /**
     * Set this to true if there is a reasonable chance that your extending
     * Collector will return less than the bytes requested, so that testing
     * does not aggressively throw Exceptions when it is detected.
     * A Collector can always return less; but the tests are more aggressive.
     */
    protected static boolean acceptShortBytes = false;

    protected static class CollectorHammer extends Thread {
        private CollectorInterface rc;
        private StringBuffer       chTrace = new StringBuffer();
        private int                i;
        CollectorHammer(CollectorInterface rc, int i)   { this.rc = rc; this.i = i; }
        public String toString()      { return "CollectorHammer #" + i; }
        public void           run()
        {
            try {
                selfTest(rc, 30, chTrace);
            } catch (Throwable thrown) {
                testFailed = i;
                thrown.printStackTrace();
                if (mainTest) {
                    System.err.println("Exception by TRACE\n");
                    System.err.println( mouthful(chTrace) );
                }
            } finally {
                // no close as it interferes with other thread!!!
            }
        }
    }
    
    public static String mouthful(StringBuffer sb)
    {
        int length = sb.length();
        int start = 0;
        if (length > 10000) {      // too sodding long, shrink it!
            start = length - 5000;
        }
        System.err.println(" start=" + start + "; len=" + length);
        return sb.substring(start, length);
    }
    
    protected static String hammerThreads(Thread[] ts)
    {
        for (int i = 0; i < ts.length; i++) {
            ts[i].setDaemon(false);     // don't block until done
            ts[i].start();
        }
        for (int i = 0; i < ts.length; i++) {
            try {
                ts[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException("IntEx " + e);
            }
        }
        
        if (testFailed >= 0) {
            throw new RuntimeException("Hammer Threads Test FAILED #" + testFailed + " of " + ts.length);
        }
        
        StringBuffer b = new StringBuffer();
        b.append("Hammer Threads " + ts.length + " threads run, succeeded.");
//        for(int i = 0; i < ts.length; i++) {
//            b.append( "THREAD " + i + ":\n" );
//            b.append( ts[i].toString() );
//        }
        return b.toString();
    }
    
    protected static String selfTestThreads(CollectorInterface c, int num)
    {
        Thread[] ts = new Thread[num];
        for (int i = 0; i < num; i++) {  // mainly, test that the threading doesn't break
            ts[i] = new CollectorHammer(c, i);
        }
        return hammerThreads(ts);
    }
    
    static final int NUM_COLLECTORS = 3;
    static CollectorInterface getCollector(int i)
        throws Exception
    {
        switch (i) {
        case 0:         return new CollectorURandom();
        case 1:         return new CollectorDevRandom();
        case 2:         return new CollectorJavaRandom();
        
        /* if adding collectors, fix up the NUM_COLLECTORS above */
        
        default: throw new RuntimeException("no such collector: " + i);
        }
    }
    
    /**
     * This test is only for testing the Collector system,
     * not for testing individual collectors.  That task
     * belongs elsewhere.  Only add easily working collectors
     * above to the getCollector() list.
     * @return
     * @throws Exception
     */
    public static String selfTest()
        throws Exception
    {
        StringBuffer s = new StringBuffer();
        s.append("Collectors selfTest:\n   ");
        for (int i = 0; i < NUM_COLLECTORS; i++) {
            CollectorInterface x = getCollector(i);
            s.append(x.toString() + "\n   ");
            selfTest(x, 100, new StringBuffer());
            x.close();
        }
        return s.toString();
    }
    
    protected static boolean mainTest = false;
    
    public static void main(String[] args)
        throws Exception
    {
        mainTest = true;             // more diags
        String s = selfTest();
        System.out.println(s);
    }
}
