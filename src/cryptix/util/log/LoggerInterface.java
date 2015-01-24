package cryptix.util.log;

/**
 * <p>Presents a logging interface that approximates the log4j one
 * with particular emphasis on methods used by WebFunds.</p>
 * 
 * http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Logger.html
 *   
 * @author ada
 */
public interface LoggerInterface
{
    public void trace(Object message);
    public void debug(Object message);    // webfunds only
    public void info(Object message);     // webfunds only
    public void warn(Object message);     // webfunds only
    public void error(Object message);    // webfunds only
    public void fatal(Object message);

    public void trace(Object message, Throwable t);
    public void debug(Object message, Throwable t);
    public void info(Object message, Throwable t);
    public void warn(Object message, Throwable t);
    public void error(Object message, Throwable t);
    public void fatal(Object message, Throwable t);
    
    
}
