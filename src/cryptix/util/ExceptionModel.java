package cryptix.util;

/**
 *  This exception base class can be inherited by all Exception Groups
 *  in order to add a number to the basic Exception model.
 *  The number can be used variously.  It was originally the SOX error number.
 *  
 *  The point of the number is to provide an easy basis for subclassing
 *  the exception and quick handling.  For example, most areas have a
 *  variant of LATER, so this can be handled with a method that checks
 *  the number.  See example below.
 *  
 *  @see webfunds.util.ExceptionModel for original
 */
public class ExceptionModel
    extends Exception
{
    protected int                number;
    public int                   getNumber()         { return number; }

    /** extend this? */
    public static boolean        isModelException(int num) { return num == UNKNOWN; }
    /** extend this */
    public boolean               isLater()           { return false; }

    /**
     *  Internal errors detected - this means the number wasn't
     *  allocated to anything, so it was a basic non-local exception
     *  that was thrown.
     */
    public static final int      UNKNOWN             = -1;
    public boolean               isUnknown()         { return getNumber() == UNKNOWN; }

    protected String getUnknownString(String e)
    {
        return e + " from " + getClass().getName();
    }
    /**
     * Extend this.
     * @param e
     * @return
     */
    protected String getErrnoString(String e)
    {
        return "(" + number + ") " + ((e == null) ? getUnknownString("'not known'") : e);
    }
    /**
     *  Subclass should extend this.
     *  All this one does is return the number and say it is unknown.
     */
    public String getErrnoString()
    {
        if (number == UNKNOWN)
            return getErrnoString( getUnknownString("unknown") );

        return getErrnoString( getMessage() );
    }
    
    protected String combine(String primary, String secondary)
    {
        if (null == primary && secondary == null)      return "(null2)";
        if (null == primary) {
            if ("".equals(secondary))                  return "(empty2)";
            else                                       return secondary;
        }
        if (null == secondary) {
            if ("".equals(primary))                    return "(empty1)";
            else                                       return primary;
        }
        if (primary.indexOf(secondary) >= 0)           return primary;
        if (secondary.indexOf(primary) >= 0)           return secondary;
        
        return primary + " (2: '" + secondary + "')";
    }

    /**
     *  General purpose printout of the basic error recorded in a Throwable.
     *  Will defer to getErrnoString() if it is one of our ExceptionModels.
     *  If not, will chase the Cause chase, if there. 
     */
    public static String getErrorString(Throwable t)
    {
        if (null == t)
            throw new RuntimeException("EMUL4832 null==t");
        if (t instanceof ExceptionModel) {
            return ((ExceptionModel)t).getErrnoString();
        }
        String s = t.getMessage() + "  {" + t.getClass().getName() + "}";
        Throwable x = t.getCause();
        if (null != x)
            s += "\n   <== " + getErrorString(x);    // XXX Recursion!
        return s;
    }

    
    
    public String toStackTraceString(int l)              { return toStackTraceString(l, this); }
    public String toStackTraceString()                   { return toStackTraceString(5); }
    public static String toStackTraceString(Throwable t) { return toStackTraceString(0, t); }

    public static String toStackTraceString(int num_lines, Throwable t)
    {
        if (null == t)
            return "  (no cause, t == null)";
        String s = "";
        Throwable cause = t.getCause();
        if (cause != null) {
            s += "\n" + toStackTraceString(num_lines, cause)   // XXX: recursion!
                + "\n\n Which then caused: \n";
        } else {
            s += "  (CAUSAL EX)";
        }
        
        s += toOneStackTraceString(num_lines, t);
        return s;
    }
    
    private static String toOneStackTraceString(int num_lines, Throwable t)
    {
        String s = " " + t.getClass().getName() + " message: " + t.getMessage();
        StackTraceElement[] els = t.getStackTrace();
        s += toStackTraceString(num_lines, els);
        return s;
    }
    
    public static String toStackTraceString(int num_lines, StackTraceElement[] els)
    {
        String s = "";
        int len;
        if (null == els) {
            len = 0;
            s = "ug.  No stack trace available?";
        } else {
            if (num_lines <= 0)
                num_lines = els.length;
            if (els.length < num_lines)
                len = els.length;
            else
                len = num_lines;
        }
        
        for (int i = 0; i < len; i++) {
            s += "\n    => " + els[i];
        }
        return s;
    }
    
    /**
     *  It seems that this gets called multiple times in construction
     *  of an Exception from another exception.  So the message grows...
     *
     *  @return the string version
     */
    public String toString()
    {
        String s = "";
        String msg = super.getMessage();
        if (msg == null)
            msg = "";

        if (number > 0) {
            String num_part = getErrnoString();
            if ((num_part!= null) && msg.indexOf(num_part) < 0)   // only if not there already
            {
                s += num_part;               // add (n) explanation
                if (msg.length() > 0)        // if there is a message...
                    s += ": ";               // add a separator
            }
        } else if (number < 0) {
            s += "<" + number + " ?> ";
        }
        s += msg;
        return s;
    }



    /**
     *  @param num is an error code, undefined here,
     *  extending classes to define
     */
    public ExceptionModel(int num, String msg)
    {
        super(msg);
        number = num;
    }

    /**
     *  @param num is an error code, undefined here,
     *  extending classes to define
     */
    public ExceptionModel(Exception ex)
    {
        super(ex);
        number = UNKNOWN;
    }

    /**
     *  @param num is an error code, undefined here,
     *  extending classes to define
     */
    public ExceptionModel(ExceptionModel ex)
    {
        super(ex);
        number = ex.getNumber();
    }

    /**
     *  @param num is an error code
     *  @param ex is the original cause
     */
    public ExceptionModel(int num, ExceptionModel ex)
    {
        super(ex);
        number = num;
    }

    /**
     *  @param num is an error code, undefined here,
     *  extending classes to define
     */
    public ExceptionModel(String s, ExceptionModel ex)
    {
        super(s, ex);
        number = ex.getNumber();
    }
    
    
    /**
     * Experimental.  Why can't we carry Throwables and Exceptions?
     */
    public ExceptionModel(String s, int n, Throwable t) { super(s, t); number = n;  }
    public ExceptionModel(String s,        Throwable t) { super(s, t); number = -1; }
    public ExceptionModel(                 Throwable t) { super(   t); number = -1; }
}
