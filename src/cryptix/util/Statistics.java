package cryptix.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>Help to gather statistics for some operation, such as crypto.</p>
 * 
 * <pre>
 *      assert(code sample == 1000 words);
 *      int n = 1000;
 *      Statistics x = new Statistics(n);
 *      for (int i = 0; i < n; i++) {
 *          long start = System.nanoTime();
 *          doCryptoOp();
 *          x.push( System.nanoTime() - start );
 *      }
 *      System.out.println(" statistics for CryptoOp: " + x.toString());
 * </pre>
 * 
 * <p>Note that by default:</p>
 * <ul><li> the first result to push() is always dropped, </li>
 *     <li> </li>
 * 
 * @see RollingStatistics
 * @author iang
 *
 */
public class Statistics
{
	
    protected long[]  measurands;
    protected int     maxMeasurands()      { return measurands.length; }
    protected int     n;
    public int        samples()            { return n; }
    protected long    deviation = -1;
    protected long    average = -1;
    protected boolean first_blood = true;
    
    /** call with true to not ignore the first measurand. */
    public void setIgnoreFirstMeasureand(boolean ignore){ first_blood = ignore;}
    
    private int factor = 1;
    /** @return the factor by which numbers (ns) are reduced, default 1 */
    public int        getFactor()           { return factor; }
    /** set the factor by which all displayed numbers are divided by */
    public void       setFactor(int f)      { factor = f; }
    private String unit = "ns";
    /** @return the unit string that is displayed in output by toString(), default "ns" */
    public String     getUnit()             { return unit; }
    /** set the display unit string, default is "ns" */
    public void       setUnit(String s)     { unit = "s"; }
    /** set the output from toString() to be in microseconds */
    public void       setMicroseconds()     { setFactor(1000);    setUnit("us"); }
    /** set the output from toString() to be in milliseconds */
    public void       setMilliseconds()     { setFactor(1000000); setUnit("ms"); }
    /** set the output from toString() to be in Seconds */
    public void       setSeconds()          { setFactor(1000000000); setUnit("s"); }
    
    
    
    protected Statistics () { }
    /**
     * 
     * @param maxSamples is the size of the array to allocate,
     *        will break if exceeded (too large doesn't matter).
     */
    public Statistics(int maxSamples)
    {
        measurands = new long[maxSamples];
        n = 0;
    }
    
    
    
    /**
     * We are relying on the JIT to make this efficient.
     * The first measurement is always ignored, as it
     * seems to be wildly longer - probably the JIT in action.
     * 
     * depends on mode, -1== ignore first, 0 ignore 0
     * @param measurand, being the measured amount of nanoseconds of delay in an action in a loop
     */
    public void push(long measurand) {
        /*
         * The first result is perverted by absence of JIT,
         * it can be 5-10 times longer.  The below code backs it out
         * as if it never happened. Depending on the mode.
         */
        if (first_blood) {
            measurands[0] = measurand;
            first_blood = false;
            return;
        }
        measurands[n++] = measurand;
        reset();
    }

    public void reset()
    {
        average = -1;
        deviation = -1;
        reset = false;
    }

    protected boolean reset = false;
    

    protected long average(int start, int end)
    {
        if (start >= end)
            return 0;
        double sum = 0;
        for (int i = start; i < end; i++)
            sum += measurands[i];
        average = Math.round(sum / (end - start));
        return average;
    }
    
    public long average()
    {
        if (average >= 0) return average;
        if (n == 0) { average = 0; return average; }
        
        return average(0, n);
    }

    public long deviation(int start, int end, long av)
    {
        int sample = end - start;
        if (sample <= 1)
            return 0;
        
        double total = 0;      // large numbers are blowing out the long
        for (int i = start; i < end; i++) {
            long diff = measurands[i] - av;  // take the diff from average
            total += diff * diff;            // square the diff
        }
        deviation = Math.round( Math.sqrt(total / (sample - 1)) );
        return deviation;
    }
    
    public long deviation()
    {
        if (deviation >= 0) return deviation;
        if (n == 0) { deviation = 0; return deviation; }
        long av = average();
        
        return deviation = deviation(0, n, av);
    }
    
    int numFailed = 0;
    boolean failed = false;
    public void setFailed(boolean to){ failed = to; }
    public void addFailed(){ numFailed += 1; }
    public void addFailed(int many){ numFailed += many; }
    public int fails(){ return numFailed; }
    
    
    HashSet<byte[]> numUnexpected;
    boolean unexpected = false;
    public void setUnexpected(boolean to){ 
    	numUnexpected = new HashSet<byte[]>();
    	unexpected = to; 
    }
    public void addUnexpected(byte[] unexp)
    { 
    	if(unexp == null){
    		byte[] b = new byte[0];
    		numUnexpected.add(b);
    	}
    	numUnexpected.add(unexp); 
    }
    public int unexpecteds(){ return numFailed; }
    public Set<byte[]> getUnexpected() {return numUnexpected; }
    
    public String toAverage()   { return (average()   / getFactor()) + getUnit(); }
    public String toDeviation() { return (deviation() / getFactor()) + getUnit(); }
    
    public String toString()
    {
        String s =  "Average " + toAverage()
            + " StdDev " + toDeviation()
            + " (" + n + " samples)"; 
        if(failed){
        	s += " Failures: " + numFailed + " : ";
        	float percentage = 0;
        	if (numFailed != 0) {
        		float total      = (float) (n + numFailed);
        		percentage = (((float) numFailed)/total)*100f;
        	}
        	s += percentage + "% ";
        }
        if(unexpected){
        	s += " Unexpected: " + numUnexpected.size();
        	for (Iterator<byte[]> data = numUnexpected.iterator(); data.hasNext();){
    			byte[] d = data.next();	
    			s       += "\n\tunexpected length: "+d.length+ " unexpected: " + cryptix.X.quick(d);
    		}
        	
        }
        return s;
    }
}