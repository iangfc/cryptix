package cryptix.AdazPRNG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



/**
 * This collector just grabs what is required from /dev/urandom.
 * It should not ever block, and the device should feed as much
 * as desired at high speed.  This is because the system's urandom
 * device is a PRNG backed by an internal entropy collector/mixer
 * system just like this system.
 * 
 * This is the one that should be used, not CollectorDevRandom,
 * see the discussion there.
 * 
 * @author ada
 */
public class CollectorURandom
    extends CollectorAbstract
    implements CollectorInterface
{
    /**
     * The standard device name for the random provider under
     * most forms of MacOSX, Linux, BSD.
     */
	public static final String URANDOM = "/dev/urandom";
	
	private final FileInputStream access;

    @Override
    public void close()
    {
        try {
            access.close();
        } catch (IOException e) {
        }
    }
	
    /**
     * Opens up the URANDOM device and uses that as the source
     * for this collector.
     * 
     * @throws FileNotFoundException
     */
	public CollectorURandom()
	    throws FileNotFoundException
	{
	    super();   // no pool
        access = new FileInputStream(URANDOM);
	}

	/**
	 * Open up a file f and read it as a random source.
	 * This may be useful where there is a hardware random
	 * number generator available under a different device
	 * name that emulates the non-blocking high-speed
	 * contract of URANDOM
	 * 
	 * Obviously, if a static data file is supplied, there
	 * will be no new entropy entering the system.
	 * 
	 * @param f
	 * @throws FileNotFoundException
	 */
    public CollectorURandom(File f)
        throws FileNotFoundException
    {
        super();   // no pool
        access = new FileInputStream(f);
    }
	
    /**
     * Because /dev/urandom does not block, no point in
     * saving randoms -- just grab it from the file/device
     * and return.
     * Note that this method does a simple FIS.read, so
     * if the file does block (breaking it's contract)
     * then we block too!
     * 
     * @param buf to place the bytes in, always filled
     * @return the number of bytes read, should always be buf.length
     */
    @Override
    public int nextBytes(byte[] buf)
    {
        int i = 0;
        int off = 0;
        //byte[] buf = new byte[len];
        int len = buf.length;
        do {
            try {
                i = access.read(buf, off, len);
            } catch (IOException e) {
                throw new RuntimeException("rand7682 " + e);
            }

            len -= i;
            off += i;
        } while (len > 0);   // should never go round more than once
        
        if (len != 0)        // should never happen
            throw new RuntimeException("rand8745 " + len + " " + i + " " + off);
        return buf.length;
    }
    

    
    public String toString()
    {
        return "Collector URandom " + super.toString();
    }


    public static String selfTest()
    {
        return selfTest(3);
    }

    public static String selfTest(int num)
    {
        CollectorInterface c = null;
        try {
            c = new CollectorURandom();
            selfTest(c, 100);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FNFE " + e);
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
