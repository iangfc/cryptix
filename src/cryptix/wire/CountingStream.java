package cryptix.wire;

/**
 * Count the number of bytes going through the read() or write()
 * byte operations inside the stream.
 * 
 * EXPERIMENTAL -- needed for calculating the nonced MACs.
 * 
 * @author iang
 *
 */
public interface CountingStream
{
    /**
     * The stream position is initialised to zero, but can be set
     * at any time to any other value.
     */
    public void         setPosition(long x) ;
    /**
     * 
     * @return the file position or stream position,
     *         else -1 if unsupported
     */
    public long         getPosition()       ; 

}
