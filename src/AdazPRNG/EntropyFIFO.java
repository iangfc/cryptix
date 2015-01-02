package AdazPRNG;

import java.io.Closeable;
import java.util.Arrays;

import cryptix.X;

/**
 * A byte cache that allows push and pull of random numbers
 * in a FIFO arrangement.
 * 
 * The FIFO arrangement creates
 * one fixed internal array, so that temporary copies aren't spawned.
 * It has the added advantage that older data is overwritten,
 * which provides a clear destruction of local copy of used-up
 * entropy
 * (NB. It is very difficult to organise data destruction
 * in Java because the JIT
 * optimises out any assignments of data that are not used/read).
 * 
 * Relevant class methods are synchronized, because the user class
 * is typically filling in with one thread and reading from another.
 * 
 * @author ada
 * @author iang
 */
public class EntropyFIFO
    implements Closeable
{
    public int             size()                   { return entropy.length; }
    private int            available                = 0;
    public int             available()              { return available; }
    private final byte[]   entropy;
    public void            close()                  { Mixer.destroy(entropy); }

    /* non-deterministic cleaner byte of zero, checked by tests */
    private static final byte SPLAT = 0;
    private int            writeP = 0, readP = 0;
    private boolean        full = false, empty = true;
    
    
    
    public EntropyFIFO(int arraySize) {
        entropy = new byte[arraySize];
    }

    
    /**
     * Puts some collected bytes into the buf, starting from 0.
     * If number placed, len, is less than the buf size, then the
     * remaining bytes after len are not touched.
     * 
     * @param buf is where to place the bytes, must not be null
     * @return the number of bytes written into buf, starting from zero
     */
    public synchronized int nextBytes(byte[] buf)
    {
        int len = buf.length;
        if(len > available())
            len = available();
        if (empty) {
            return 0;
        }
        for(int i = 0; i < len; i++){
            buf[i] = entropy[readP];
            /*
             *  Need to trick JIT into not dropping the following
             *  cleanup statement.
             *  How to do that is tricky, the only way is to really use
             *  the information in some sense.  We could for example
             *  use the higher-layer strategy of hashing this byte and
             *  then counting the population bytes but that is pretty
             *  expensive.  Need more data...
             */
            entropy[readP] = SPLAT;
            readP++;
            if (readP >= size()) {
                readP = 0;
            }
            full = false;
            if (readP == writeP) {    // oops, caught up to writer, empty!
                empty = true;
                break ;
            }
        }
        resetSize();
        if (CollectorAbstract.mainTest) {
            if (trace.length() > 100000) {  // shrink buffer in huge tests
                trace.delete(0, 50000);
                trace.trimToSize();
            }
            trace.append("    @ pull " + X.quick(buf, len) + "\n      " + toDeepString());
        }
        return len;
    }

    public void push(byte[] b) { push(b, 0, b.length); }
    
    /**
     * Pushes the specified bytes into the FIFO.  This method allows
     * the caller to directly use a source array without creating
     * another, so reducing the spread of copies.
     * 
     * If offset or count overflow the passed b, will thrown IllegalArgumentException
     * @param b
     * @param offset
     * @param count
     */
    public synchronized void push(byte[] b, int offset, int count) {
        if (b.length == 0)
            return;
        if (0 > offset || offset >= b.length || 0 > count || offset + count > b.length)
            throw new IllegalArgumentException(" len " + b.length + " off " + offset + " count " + count) ;
        if (count > size() - available())
            count = size() - available();
        if (CollectorAbstract.mainTest)
            trace.append("    * push " + X.quick(b, count) + "\n      " + toDeepString());
        
        for (int i = 0; i < count; i++) {
            /*
             * Hopefully, this check will cause the JIT not to drop
             * the write of SPLAT into the spot.  However, what it
             * cannot force is the timely writing;  the JIT could
             * defer it as long as it likes... Especially in a
             * threaded context.
             */
            if (SPLAT != entropy[writeP]) {
                throw new RuntimeException(i + "th not SPLAT at " + writeP + "\n" + b.length + " o " + offset + " c " + count);
            }
            entropy[writeP] = b[ offset + i ];
            writeP++;
            if (writeP >= size()) {         // roll around
                writeP = 0;
            }
            if (writeP == readP) {          // nice, caught up, full!
                full = true;
            }
            empty = false;
        }
        resetSize();
        if (CollectorAbstract.mainTest)
            trace.append("      pushed\n      " + toDeepString());
    }
    
    private void resetSize() {
        if (full) {
            available = size();
        } else if (empty) {
            available = 0;
        } else if (readP < writeP) {   // write is leading, so gap backwards
            available = writeP - readP;
        } else if (writeP < readP) {   // write trails, so gap wraparound
            available = size() - (readP - writeP);
        }
    }
    
    public String toString() {
        return "EnFiFo " + available() + " (" + size() + ")";
    }
    private String toDeepString() {          // for testing only!
        String s = toString();
        s += "   " + (full ? "FULL" : "*") + (empty ? "EMPTY" : "/");
        s += " [r=" + readP + ", w=" + writeP + "]\n";
        s += "      " + X.data2hex(entropy) + "\n";
        return s;
    }
    
    // no equals() or hashCode() as that would leak info!

    
    
    
//////////////////////////////////////////////////////////////
//
//  SELF testing code.  This is typically stored in the same
//  class file so as to give the reader no excuse but to run
//  it on any changes...
//
    
    final static StringBuffer trace = new StringBuffer();
    
    /* has been filled by the test routine with a pattern */
    protected static void isPatterned(EntropyFIFO x)
    {
        byte spot = 0;
        for (int k = 0; k < x.entropy.length; k++) {
            if (spot++ != x.entropy[k]) {
                throw new RuntimeException(k + "th spot didn't count up!\n" +
                          x.toDeepString());
            }
        }
    }

    /* has been cleaned in the normal process of reading out data */
    protected static void isSplat(EntropyFIFO x)
    {
        for (int k = 0; k < x.entropy.length; k++) {
            if (SPLAT != x.entropy[k]) {
                throw new RuntimeException(k + "th spot didn't get cleaned!\n" +
                          x.toDeepString());
            }
        }
    }
    
    /**
     * Fill up the FIFO with a pattern and then test that it is there.
     * @param z the FIFO to fill
     */
    protected static void fillTest(EntropyFIFO z)
    {
        byte spot = 0;
        int j = 0;
        while (j < z.size()) {
            int ex = Support.exampleInt(0, z.size() - z.available());
            byte[] tmp = new byte[ex];
            for (int i = 0; i < ex; i++) {
                tmp[i] = (byte) (0x00FF & (spot++));
            }
            z.push(tmp);
            trace.append("\n  " + j + "+" + ex + ": " + z.toDeepString());
            j += ex;
        }
        isPatterned(z);
    }

    /**
     * Drain out the FIFO and then test that it was cleaned.
     * @param z the FIFO to drain
     */
    protected static void emptyTest(EntropyFIFO z)
    {
        while (z.available() > 0) {
            int ex = Support.exampleInt(0, z.available());
            byte[] tmp = new byte[ex];
            z.nextBytes(tmp);
            trace.append("\n  " + ex + ": " + z.toDeepString());
        }
        isSplat(z);
    }
    
    /** run the fillTest many times */
    protected static String fillTest(int num)
    {

        for (int j = 0; j < num; j++) {
            int size = Support.exampleInt(1, 300);
            trace.delete(0, trace.length());
            trace.append("fillTest() " + j + " size " + size);
            EntropyFIFO x =  new EntropyFIFO(size);
            fillTest(x);
            emptyTest(x);
            x.close();
        }
        return " fill; ";
    }
    
    protected static String copyTest(int num)
    {
        trace.delete(0, trace.length());
        trace.append("copyTest() ");
        int size = Support.exampleInt(1, 400);

        EntropyFIFO x =  new EntropyFIFO(size);
        fillTest(x);
        trace.append( " filled... ");
        EntropyFIFO y =  new EntropyFIFO(size);
        for (int j = 0; j < num; j++) {
            int ex = Support.exampleInt(0, x.size());
            byte[] tmp = new byte[ex];
            int len = x.nextBytes(tmp);
            y.push(tmp, 0, len);
            trace.append("\n" + len + " " + y.toDeepString());
            //trace.append(j + "+" + ex + ": " + x.toDeepString());
        }
        isSplat(x);
        isPatterned(y);
        emptyTest(y);
        x.close();
        y.close();
        return " copy; ";
    }
    
    
    
    private static void hammerTest(EntropyFIFO x, int myIndex, int maxIndex, StringBuffer t) {
        byte[] buf = new byte[ x.size() + 2 ];
        Arrays.fill(buf, (byte)myIndex);
        for (int i = 0; i < 100; i++) {   // spin around, and push OR pull each time
            if (testFailed) return ;
            
            int qty = Support.exampleInt(0, buf.length);
            int avail = x.available();
            t.append(i + ": ");
            if (Support.exampleInt(0, 1) == 0) {
//                if (CollectorAbstract.mainTest)
//                    System.err.print((char)(myIndex+'a'));
                t.append("Push " + qty);
                x.push(buf, 0, qty);
            } else {
//                if (CollectorAbstract.mainTest)
//                    System.err.print((char)(myIndex+'A'));
                byte[] tmp = new byte[qty];
                int got = x.nextBytes(tmp);
                String s = "Pull " + got + "(" + qty + ") ";
                for (int j = 0; j < got; j++) {
                    if (! (1 <= tmp[j] && tmp[j] <= maxIndex)) {
                        trace.append("\n========FAILED=========");
                        throw new RuntimeException("Thread " + myIndex + ": " +
                                j +"th byte " + tmp[j] + " not hammered! " +
                                s +
                                " on run " + i + " of hammer " + myIndex + " of " + maxIndex +
                                "\n" + X.data2hex(tmp));    
                    }
                }
                t.append(s);
                if (qty <= avail && qty != got) {
                    int avail2 = x.available();
                    t.append("\npotential phase error! " + avail + " => " + got + " then " + avail2 + "\n");
                }
            }
        }
    }

    private static class EntropyHammer extends Thread {
        private EntropyFIFO   x;
        private int           f, m;
        private StringBuffer  t          = new StringBuffer();
        EntropyHammer(EntropyFIFO x, int f, int m)              { this.x = x; this.f = f; this.m = m; }
        public String         toString() { return f + ": " + t.toString(); }
        public void           run()
        {
            try {
                hammerTest(x, f, m, t);
            } catch (Throwable ttt) {
                ttt.printStackTrace();
                System.err.println("Exception by " + x.toDeepString() + "\n" + t.toString());
                testFailed = true;
            } finally {
                // no close here as it interferes with other thread!!!
            }
        }
    }

    private static String selfTestThreads(EntropyFIFO x, int numThreads)
    {
        Thread[] ts = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {  // mainly, test that the threading doesn't break
            ts[i] = new EntropyHammer(x, i+1, numThreads+1);
        }
        return CollectorAbstract.hammerThreads(ts);
    }

    static boolean testFailed = false;
    
    /**
     * Create numThreads with one shared FIFO of size.
     * Each thread hammers at it pushing and pulling.
     */
    private static String threadTest(int size, int numThreads)
    {
        trace.delete(0, trace.length());
        String s = "threadTest(size=" + size + ", t=" + numThreads + ") ";
        EntropyFIFO x = new EntropyFIFO(size);
        try {
            //selfTest(rc);
            String ss = selfTestThreads(x, numThreads);
            if (testFailed) {
                s += "\n\n" + ss + "\n\nTEST FAILED, main trace follows\n\n"
                  + CollectorAbstract.mouthful(trace)
                  + "\n\n===============\n\n";
            }
            return s;
        } finally {
            x.close(); // no close until all threads dead.
        }
    }
    
    public static String selfTest()
    {
        StringBuffer s = new StringBuffer();
        s.append("EntropyFIFO: ");
        int fillers = CollectorAbstract.mainTest ? 200 : 20;
        int copies  = CollectorAbstract.mainTest ? 200 : 20;
        int lowT    = CollectorAbstract.mainTest ? 1000 : 100;
        int highT   = CollectorAbstract.mainTest ? 1000 : 20;
        
        try {
            s.append( fillTest(fillers) );
            s.append( copyTest(copies) );
            s.append( threadTest(lowT, 2) );
            s.append( threadTest(highT, 30) );
        } catch (Throwable t) {
            System.err.println("trace: " + CollectorAbstract.mouthful(trace));
            t.printStackTrace();
        }
        return s.toString();
    }
    
    public static void main(String[] args) {
        CollectorAbstract.mainTest = true;
        String s = selfTest();
        System.out.println(s);
    }
}
