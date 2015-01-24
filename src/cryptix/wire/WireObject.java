package cryptix.wire;


import java.io.IOException;


/**
 * Every object that passes across the wire implements this Interface.
 * 
 * As well as the Interface elements, there are also these parts needed
 * for the full Ouroboros cycle of testing:
 * 
 * 
 * @see WireObjectAbstract for an easy base.
 */
public interface WireObject
{

    /**
     * Every class must implement a single wiretype that its
     * instance makes available for Typed wire transmission.
     * 
     * @return the wiretype that precedes the packet for Typed output
     */
    public int getWireType();
    
    /**
     * Signature of ctor/1 taking a WireInputStream argument.
     * To match wireEncode() there should be a wireDecode() method.
     * However, this would also require an empty constructor,
     * and two user-level calls to implement the single action.
     * To reduce the calls to one and to narrow down the opportunity
     * for errors, only one call to a constructor that does the
     * wireDecode job is better.
     * Unfortunately this is harder to show in the Interface,
     * thus resulting in the below gobbledegook: 
     */
    static final Class<?>[] WIRE_CTOR_SIG = new Class[] { WireInputStream.class };

    // this is complicated.
    // wireEncode(WireOutputStream) is what is implemented.
    // There appears no easy way to extend but narrow this method.
    public void wireEncode(WireOutputStream wos) throws IOException;



    /*
     *  Create an example object with a randomly chosen set of internal data.
     *  Each succeeding call to example() returns a different one.
     * 
     *  public static Thing example() { return new Thing(...); }
     */
    
    /*
     *  Return the approximate work factor required to test this
     *  object.  0 means no change to normal.  -1 means it is slower
     *  by an order of 2.  +1 means it is faster by an order of 2.
     *  public static int testFactor() { return 0; }
     *  Experimental - not yet done.
     */

    /*
     * Each must implement an equals, where equality is defined
     * according to wire transmission.  That is, two objects are
     * equal if they have the same data as transmitted from one
     * node to another over the wire.  This means that any transient
     * data stored that is not part of the wire transmission information
     * is also not included in equality.  E.g., statistics or endpoint
     * info.
     */
	public boolean equals(java.lang.Object obj);

	/**
	 * For debugging, all WireObjects include a contents description.
	 * @return a string describing the wire info in brief.
	 */
	public String toString();
}
