package cryptix.wire;

import cryptix.util.ExceptionModel;

/**
 * This exception is thrown for Wire failures.
 */
public class WireException
    extends ExceptionModel      // for compatibility, short term?
{
    public static final int
       WIRE_BAD_VERSION   = 205,
       WIRE_WRONG_TYPE    = 224;     // XXX: (SOX) asked for a particular WT

    public static final String
       WIRE_WRONG_TYPE_S  = "The WireType expected was not found";
 
    public WireException(String m)            { super(WIRE_WRONG_TYPE, m); }
    public WireException(int errno, String m) { super(errno, m); }
    public WireException(int errno)           { super(errno, ""); }
    public WireException(Exception x)         { super(x); }
    
    public WireException(int g, int n)     { super(WIRE_BAD_VERSION, "Expect v " + n + " got: " + g); }
    public WireException(int g,int m,int n){ super(WIRE_BAD_VERSION, "Expect v "+m+"-"+n+" got: "+g); }
}
