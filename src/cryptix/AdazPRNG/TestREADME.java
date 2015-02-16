package cryptix.AdazPRNG;

import java.io.FileNotFoundException;

import cryptix.X;
import cryptix.util.Statistics;


/**
 * This is an example class for the RNG, for the purposes of
 * reading.  It also includes some whole-of-RNG tests.
 * 
 * These steps:
 * 
 * <ol><li>Set up the mixer internally,</li>
 *     <li>adds in a few package collectors based on /dev/random,
 *     <li>gets the next seed of the mixer, which collects from
 *         each of its Collectors and munges them together, and finally
 *     <li>makes a PRNG off the seed from the mixer.</li></ol>
 * 
 * <p>
 * <b> This is an EXAMPLE only, and is not set up for use.</b>
 * I.e., it doesn't have enough collectors to make it useable,
 * and those that are added are correlated or dodgy.
 * For this reason, the class has private methods, you must
 * read, understand and copy and change.
 * </p>
 * 
 * TODO:
 * <ul>
 * <li>Review</li>
 * </ul>
 * @author Ada
 *
 */
public class TestREADME
{
	private static CollectorInterface[] collectors = new CollectorInterface[3];

	private static MixerInterface mix = new Mixer();

	private static boolean iAmInitialised = false;

	/**
	 * Open up the mixer and give it a handful of Collectors.
	 * You do this once, in static private space, as one mixer
	 * is good for all uses, and is considered better than separate
	 * mixers.
	 * 
	 * This is private because it is an example only,
	 * it isn't good enough, not even for government work.
	 */
	private static void init()
	{	
		if (iAmInitialised) return;

        /*
         *  You need to add more collectors than these three below.
         */
		try {
            collectors[0] = new CollectorURandom();     // recommended
            collectors[1] = new CollectorDevRandom();   // Not Recommended.
        } catch (FileNotFoundException e) {
            throw new RuntimeException("no /dev/urandom? ", e);
        }
		
		collectors[2] = new CollectorJavaRandom();      // don't use this one!!!
		
		for(int i = 0; i < collectors.length; i++) {
		    if (null != collectors[i])
			    mix.addCollector(collectors[i]);
		}

		iAmInitialised = true;
	}

    static String selfTestMixer(int num)
    {
        TestREADME.init();
        int size = 1024;
        final int expectedPopn = 8 * size / 2;
        //final int averagePopnMax = 1, deviationMax = 4; // for 8, 100
        final int averagePopnMax = 11, deviationMax = 52; // for 1024, 100

        String s = "";
        s += "Test of Population Count over " + num + " runs with seeds of " + size + " bytes.";
        s += "\n    Expected average " + expectedPopn + " diff average Max " + averagePopnMax + " deviation max " + deviationMax;
        String extra = "";
        String last = "";

        Statistics sss = new Statistics(num);
        for(int i = 0; i < num; i++) {

            byte[] b = new byte[size];
            mix.nextBytes(b);
            long p   = X.popn(b);
            last = i + ": " + p + " " + X.quick(b, 32);
            extra += "\n" + last;
            sss.push(p);
        }
        int average = (int)sss.average();
        int diff = average - expectedPopn;
        if (diff < 0) diff = -diff;
        if (diff > averagePopnMax || sss.deviation() > deviationMax) {
            System.err.println(extra);
            throw new RuntimeException("\n" + s + "\n" + "out of bounds: " + sss.toString());
        }
        
        s = "Complete: " + s + "\n    Measured " + sss.toString() + "\n    Last Run: " + last + "\n";
        return s;
    }
    
    /**
     * Do a test over all staged -- Collectors -> Mixer -> Expansion.
     * 
     * @param num
     * @return
     */
	static String selfTestAll(int num)
	{
		TestREADME.init();
		
		String s = "";
		s += "Test of Population Count over " + num + " runs with PRNG";
		String last = "";

		final int inner = 10;
		Statistics sss = new Statistics(num * inner);
		for(int i = 0; i < num; i++){

	        StatefulChacha prng = new StatefulChacha();

	        byte[] seed = new byte[prng.getSeedLen()];
	        mix.nextBytes(seed);   // XX no, this is wrong.... 32 not 64
	        prng.init(seed);
	        
	        int numRandoms = 1;
            
	        for (int j = 0; j < inner; j++) {
	            byte[] random = new byte[numRandoms];
	            prng.nextBytes(random);
	            numRandoms = (0x00ff & random[0]) + 1;      // self-referential...
	            
	            long p   = X.popn(random);
	            last = i + ": " + p + " " + X.quick(random, 32);
	            //			extra += "\n" + last;
	            sss.push(p);
	        }
	        prng.close();
		}
		
		s = "Complete: " + s + "\n    Measured " + sss.toString() + "\n    Last Run: " + last + "\n";
		return s;
	}
	
	public static String selfTest()
	{
	    String s = "";
	    s += selfTestMixer(100);
	    s += selfTestAll(100);  // XX won't work!!! 32 not 64
	    return s;
	}

	/**
	 * README main to show how to use this system.
	 * 
	 * @param args
	 * @throws Exception if a FileNotFound hits the device Collectors
	 */
	public static void main(String[] args)
	    throws Exception
	{
	    System.out.println("\nEntFIFO:     ");
	    System.out.println(EntropyFIFO.selfTest() + "\n");
        System.out.println("\nCollectors:  ");
        System.out.println(CollectorAbstract.selfTest() + "\n");
		System.out.println("\nMixer:       ");
        System.out.println(Mixer.selfTest() + "\n");
        System.out.println("\nPRNG:        ");
        System.out.println(StatefulChacha.selfTest() + "\n");
        System.out.println("Whole Lot:   ");
        System.out.println(selfTest() + "\n");
		
		/*
		 *  The steps to copy:
		 *  
		 *  Call init() once only, set up the mixer.
		 *  Although it can be closed, it does not need to be
		 *  closed, especially if the application wants diverse PRNGs
		 *  in different places in the program.
		 */
		init();
		
		/*
		 *  You can have as many PRNGs as you want.
		 *  Best is to try and delay the request until needed,
		 *  hoping that each of the Collectors has had a chance
		 *  to do their magic (only an issue with some Collectors).
		 *  
		 *  Create it, give it a seed.
		 */
		StatefulChacha prng = new StatefulChacha();
		byte[] seed = new byte[ prng.getSeedLen() ];
		mix.nextBytes(seed);
		prng.init(seed);
		int len = (0x00FF & seed[0]) + 1;           // self-referential...
		
		/*
		 *  And now suck 'em out!
		 *  Use it abuse it!
		 */
		for (int i = 0; i < 10; i++) {
		    byte[] myRandoms = new byte[len];
		    prng.nextBytes(myRandoms);
            len = (0x00FF & myRandoms[0]) + 1;       // self-referential...
		    System.out.println("My randoms:\n" + X.data2hex(myRandoms));
		}
		
		/*
		 *  Close the PRNG when you are finished with it,
		 *  so as to remove the seed and firewall leakage.
		 *  
		 *  If you want another, just ask.
		 */
		prng.close();
	}

}
