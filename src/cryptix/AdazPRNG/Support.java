package cryptix.AdazPRNG;

import java.util.Random;


/**
 * Things to make this work.  Basically the util classes copied from
 * WebFunds so that this package is totally independent of any dependencies
 * so is capable of being released.
 * 
 * @author iang
 *
 */
public class Support
{

    static Random r = new Random( System.currentTimeMillis() );
    
    /** @return example int, between min and max (inclusive) */
    static int exampleInt(int min, int max)
    {
        if (min == max)
            return min;

        if (min > max)  // swap, and carry on
        {
            int x = min;
            min = max;
            max = x;
        }
        long min2 = min;
        long max2 = max;
        long offset = 0;
        if (min2 < 0)   // move numbers to 0..n
        {
            offset = (-min2);
            max2 += offset;
            min2 = 0;
        }
        long l = r.nextLong();
        if (l < 0)
            l = -l;
        int ex = (int) ( ( l % (max2 - min2 + 1) ) + min2 - offset );
        if (ex < min || ex > max)
            throw new RuntimeException("ex out of min,max: "+
              ex+" "+min+","+max+ "  ["+min2+","+max2+"] ("+l+","+offset+")" );
        return ex;
    }
    
    /** @return byte array with len example bytes in it, never null */
    public static byte[] exampleData(int len)
    {
        byte[] b = new byte[len];

        for (int i = len - 1; i >=0; i--)
        {
            b[i] = (byte)r.nextInt();
        }
        return b ;
    }
}
