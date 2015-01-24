package cryptix.util.log;

import cryptix.util.ExceptionModel;

/**
 * <p>Simplest collection of debugs into a StringBuffer.
 * If you want them, call getLog().</p>
 * 
 * @author iang
 */
public class TrivialLogger
    implements LoggerInterface
{
    private StringBuffer sb = new StringBuffer();
    
    public String getLog()
    {
        String s = sb.toString();
        sb = new StringBuffer();
        return s;
    }
    
    private static final String
        TRACE = "     . ",
        DEBUG = "debug: ",
        INFO  = "info : ",
        WARN  = "WARN : ",
        ERROR = "ERROR: ",
        FATAL = "FATAL: ";

    private void d(String l, Object s)
    {
        sb.append(l);
        sb.append(s.toString());
        sb.append("\n");
    }
    private void d(String l, Object s, Throwable t)
    {
        d(l, ExceptionModel.toStackTraceString(t));
    }
    
    public void trace(Object s)                 { d(TRACE, s); }
    public void trace(Object s, Throwable t)    { d(TRACE, s, t);  }
    public void debug(Object s)                 { d(DEBUG, s); }
    public void debug(Object s, Throwable t)    { d(DEBUG, s, t);  }
    public void info (Object s)                 { d(INFO , s); }
    public void info (Object s, Throwable t)    { d(INFO , s, t);  }
    public void warn (Object s)                 { d(WARN , s); }
    public void warn (Object s, Throwable t)    { d(WARN , s, t);  }
    public void error(Object s)                 { d(ERROR, s); }
    public void error(Object s, Throwable t)    { d(ERROR, s, t);  }
    public void fatal(Object s)                 { d(FATAL, s);    fatal(); }
    public void fatal(Object s, Throwable t)    { d(FATAL, s, t); fatal(); }
    
    public void fatal()
    {
        System.err.println("EXITING AFTER ERRORS.  LOG TO FOLLOW::::::::::::");
        System.err.println(getLog());
        System.exit(1);
    }
}
