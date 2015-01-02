package AdazPRNG;

import java.security.SecureRandom;

import webfunds.util.log.Logger;


/**
 * This collector grabs the old Java JRE SecureRandom class.
 * The Java class is not recommended because it is secret,
 * unauditable and has been buggy.  People have lost money.
 * 
 * It might be used if there is a desire to show that the
 * use of this system is no worse than SecureRandom, while
 * bedding in the other Collectors.
 * 
 * @author iang
 */
public class CollectorJavaRandom
    extends CollectorAbstract
    implements CollectorInterface
{
    private static final Logger log =  Logger.getInstance("oldJavaSR");

    /**
     * <p>
     * NB: this is a very old note, probably out of date:
     * 
     * SecureRandom will block if the entropy for the system is not
     * set up.  This could happen deep within Sun code.
     * FreeBSD 4, see rndcontrol(8).
     * FreeBSD 5, /dev/random no longer blocks AT ALL ...  Need to check.
     * </p>
     */
    private static SecureRandom privSR;

    private static SecureRandom getNewSecureRandom()
    {
        long tim = System.nanoTime();
        log.info("Random.getSecureRandom: Init secureRandom ..." +
                " this could take some time.");
        // should this be syncronised?  Probably doesn't matter.
        SecureRandom sr = new SecureRandom();
        long delay = System.nanoTime() - tim;
        log.info("Finished init securerandom -- " + delay + "ns.");
        return sr;
    }
    
	@Override public void close()    { }
    
	/**
	 * Opens up a single SR for all uses.
	 */
	public CollectorJavaRandom()
	{
        if (null == privSR) {   // it's a static
            privSR = getNewSecureRandom();
        }
	}
	
    /**
     * This might block if the implementation of SR reads from
     * /dev/random and we are on a linux machine.
     * Another good reason not to use this Collector...
     */
    @Override
    public int nextBytes(byte[] buf)
    {
        privSR.nextBytes(buf);
        return buf.length;
    }
    
    
    
    
    public String toString()
    {
        return "Collector(SecureRandom) deprecated " + super.toString();
    }

    public static String selfTest()
    {
        return selfTest(3);
    }

    public static String selfTest(int num)
    {
        CollectorInterface c =  new CollectorJavaRandom();
        try {
            selfTest(c, 100);
        } finally {
            c.close();
        }
        return trace.toString();
    }

    public static void main(String[] args)
    {
        String s = selfTest(10);
        System.out.println(s);
    }
}
