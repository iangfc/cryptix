package cryptix.wire;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;

import cryptix.util.log.TrivialLogger;


/**
 * <p>Stream capable of reading data in Wire Format.</p>
 *
 * <p>By default, this stream expects values it reads off the wire to 
 * be individually typed. As a result, values read from the stream are
 * polymorphic by default and they can be null. When space conservation 
 * or compatibility issues dictate otherwise, the programmer can force
 * reading of untyped values.</p>
 *
 * <p>All this polymorphic flexibility is a pain in the butt when it forces
 * the programmer to perform explicit type and null checking for every value
 * read. To address this, three families of self-documenting reader methods
 * exist which provide stream access with built-in checking for expected
 * type and nullness:</p>
 *
 * <ul>
 * <li>readTypedXXXOrNull (any type, or null)</li>
 * <li>readTypedXXX (any type, but NOT null)</li>
 * <li>readUntypedXXX</li>
 * </ul>
 *
 * <p>Each of these methods throws WireException when the object read
 * is not what is requested by the programmer. If the method names are 
 * well-chosen this pretty much yields a DWIM interface.</p>
 * 
 * <p>TODO:<ul>
 * <li>Then, drop the DataInputStream.</li>
 * <li>Dropped the older non-used types:  Boolean, Signed, Int, Long.  DONE.</li>
 * <li>Moved it to cryptix.wire. DONE.</li>
 * </ul></p>
 *
 * @author  Jeroen C. van Gelderen
 * @author  Edwin Woudt
 * @author  iang
 * @author  based on earlier formats by Erwin, Mike & Gary
 */
public class WireInputStream
    implements Closeable, CountingStream
{
    /**
     *      Internally, _dis was a DataInputStream.
     *      Changed to DataInput, which is the interface.
     *      should make no difference.......
     *      _is is added as a hack to record the underlying
     *      stream, so we can get at the position.
     *      (When we drop the DataInput interface we should
     *      be able to deal directly with the underlying.)
     */
    private /*final*/ DataInput _dis;
    private           Object    _is;   // saved in case needed later
    

    // and what?
    private static TrivialLogger _l = new TrivialLogger();
    
    /**
     *  Package local Methods used by TestWireStreams
     */
    public void setInputStream(InputStream is)
    {
        if (_dis != null)
            throw new IllegalStateException("dis");
        _is = is;
        _dis = new DataInputStream(is);
    }
    public WireInputStream() {}
    

    public WireInputStream(InputStream is)  { _is = is; _dis = new DataInputStream(is); }

    public WireInputStream(DataInputStream dis) { _dis = dis; }
    
    /**
     * Added to cope with RandamAccessFile.
     * The underlying type is kept as DataInput with this Constructor.
     * There are slight differences between DataInputStream and DataInput.
     * @see readUntypedBytes(byte[])
     * @param dis
     */
    public WireInputStream(DataInput dis) { _dis = dis; _is = dis; }

    public WireInputStream(byte[] buf)
    {
        if (buf == null)
            throw new IllegalArgumentException("wuf290");
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        _is = bais;
        _dis = new DataInputStream(bais);
    }

    

    /**
     * Only works if the underlying stream supports it.
     */
    public void setPosition(long x)
    {
        if (_is != null) {
            if (_is instanceof CountingStream)
                ((CountingStream)_is).setPosition(x);
            //if (_is instanceof RandomAccessFile)
            //    ((RandomAccessFile)_is).xxx
        }
    }
    /**
     * Only works if the underlying stream supports it.
     * @return the current position of the FilePointer or stream counter
     *         else -1 if unsupported
     */
    public long getPosition()
    {
        if (_is != null) {
            if (_is instanceof CountingStream)
                return ((CountingStream)_is).getPosition();
            if (_is instanceof RandomAccessFile)
                try {
                    return ((RandomAccessFile)_is).getFilePointer();
                } catch (IOException e) {
                    _l.debug("wist4381", e);
                    return -1;
                }
            _l.debug("wist9236 " + _is.getClass().getName());
            return -1;
        }

        _l.debug("wist5748 null");
        return -1;
    }
    
    @Override public void close()
    {
        try {
            if (_dis instanceof Closeable)
                ((Closeable)_dis).close();
        } catch (IOException ex) {
            _l.debug("IOex on close()", ex);
            //throw new Panic("wosi4183", ex);
        }
    }

    /**
     * A thread-safe, NPE-safe close().
     * Delete this version if/after Java7 is rolled onto Androids/Servers.
     * @param c
     * @param which
     * @return null always, to assign and drop the reference to c
     */
    public static WireInputStream close(WireInputStream c)
    {
        if (null != c) {
            try {
                c.close();
            } catch (NullPointerException ex) {    // ignore race conditions
            }
        }
        return null;
    }

    
    
    
    
    /**
     *  Note that this is different to the other call with same name.
     *  Read some bytes and return how many.  For this call to work,
     *  the object must be instantiated with a real DataInputStream
     *  and not a DataInput.
     *  AFAICS - only ArmouredPayment uses this call.
     *  
     * @returns how many bytes are read into buf.
     * @throws WireException, including if stream is not a real DataInputStream
     * 
     */
    public int readUntypedBytes(byte[] buf)
        throws WireException
    {
        if (buf == null)
            throw new IllegalArgumentException("wuf154");
        DataInputStream realDis;
        if (_dis instanceof DataInputStream)
        	realDis = (DataInputStream)_dis;
        else
        	throw new WireException("sorry - can't call this method with that stream");
        try {
            return realDis.read(buf);
        } catch(IOException ex) {
            throw new WireException(ex);
        }
    }
    
    /*
     *  readFully that many bytes.
     *  If there are not that many bytes available then Ex is thrown.
     */
    public byte[] readUntypedBytes(int len)
        throws WireException
    {
        if (len < 0)
            throw new IllegalArgumentException("weg719 " + len);
        try {
            byte[] x = new byte[len];
            _dis.readFully(x);
            return x;
        } catch(IOException ex) {
            throw new WireException(ex);
        }
    }





//..............................................................................
//
// PRIMITIVE TYPES
//

// (0) Null ....................................................................

    public void readTypedNull() throws WireException {
        readAndEnsureWireType(WireTypes.NULL_REF);
    }
    

//// (1) Boolean .................................................................
//
//    /** @deprecated */
//    public boolean readTypedBoolean() throws WireException {
//        readAndEnsureWireType(WireTypes.T_BOOLEAN);
//        return readUntypedBoolean();
//    }
//    /** @deprecated */
//    public Boolean readTypedBooleanOrNull() throws WireException {
//        return readAndEnsureWireTypeOrNull(WireTypes.T_BOOLEAN) ?
//                   new Boolean(readUntypedBoolean()) : null;
//    }
//    /** @deprecated */
//    public boolean readUntypedBoolean() throws WireException {
//        try {
//            return _dis.readBoolean();
//        } catch (IOException ex) { throw new WireException(ex); }
//    }
    

//// (2) Byte ....................................................................
//
//    /** @deprecated */
//    public byte readTypedByte() throws WireException {
//        readAndEnsureWireType(WireTypes.T_BYTE);
//        return readUntypedByte();
//    }
//    /** @deprecated */
//    public Byte readTypedByteOrNull() throws WireException {
//        return readAndEnsureWireTypeOrNull(WireTypes.T_BYTE) ?
//                   new Byte(readUntypedByte()) : null;
//    }
//    public byte readUntypedByte() throws WireException {
//        try {
//            return _dis.readByte();
//        } catch (IOException ex) { throw new WireException(ex); }
//    }
    

//// (3) Unsigned Byte ...........................................................
//
//    /** @deprecated */
//    public int readTypedUnsignedByte() throws WireException {
//        readAndEnsureWireType(WireTypes.UNSIGNED_BYTE);
//        return readUntypedUnsignedByte();
//    }
//    /** @deprecated */
//    public Integer readTypedUnsignedByteOrNull() throws WireException {
//        return readAndEnsureWireTypeOrNull(WireTypes.UNSIGNED_BYTE) ?
//                   new Integer(readUntypedUnsignedByte()) : null;
//    }
//    public int readUntypedUnsignedByte() throws WireException {
//        try {
//            return _dis.readUnsignedByte();
//        } catch (IOException ex) { throw new WireException(ex); }
//    }
    

//// (4) Int .....................................................................
//
//    /** @deprecated */
//    public int readTypedInt() throws WireException {
//        readAndEnsureWireType(WireTypes.T_INT);
//        return readUntypedInt();
//    }
//    /** @deprecated */
//    public Integer readTypedIntOrNull() throws WireException {
//        return readAndEnsureWireTypeOrNull(WireTypes.T_INT) ?
//                   new Integer(readUntypedInt()) : null;
//    }
//    /** @deprecated */
//    public int readUntypedInt() throws WireException {
//        try {
//            return _dis.readInt();
//        } catch (IOException ex) { throw new WireException(ex); }
//    }
//    
//
//// (5) Long ....................................................................
//
//    /** @deprecated */
//    public long readTypedLong() throws WireException {
//        readAndEnsureWireType(WireTypes.T_LONG);
//        return readUntypedLong();
//    }
//    /** @deprecated */
//    public Long readTypedLongOrNull() throws WireException {
//        return readAndEnsureWireTypeOrNull(WireTypes.T_LONG) ?
//                   new Long(readUntypedLong()) : null;
//    }
//    /** @deprecated */
//    public long readUntypedLong() throws WireException {
//        try {
//            return _dis.readLong();
//        } catch (IOException ex) { throw new WireException(ex); }
//    }


// (6) CompactInt ..............................................................

    public int readTypedCompactInt() throws WireException {
        readAndEnsureWireType(WireTypes.COMPACT_INT);
        return readUntypedCompactInt();
    }
    
    public Integer readTypedCompactIntOrNull() throws WireException {
        return readAndEnsureWireTypeOrNull(WireTypes.COMPACT_INT) ?
                   new Integer(readUntypedCompactInt()) : null;
    }
   
    /**
     *  The CompactInt writes out its integer in a minimum
     *  series of bytes needed.
     *  
     *  Each byte signals further bytes are needed by
     *  setting its high bit.  This leaves the
     *  7 low order bits in each byte to carry the number.
     *  The last in the series will have the high bit zero.
     *  As this is an integer, it can only go for 5 bytes,
     *  and only some of the bits in the fifth byte are used.
     *  
     *  If the caller needs to expand this to longs then
     *  readUntypedCompactLong() can be substituted without
     *  any other change, as it uses the same
     *  format.  Existing data will read in happily with
     *  either, assuming it is length-compatible.
     *  (Note that typed Compacts are not interchangeable.)
     *  
     *  FIXME: need a reference for wherever this originally
     *  came from.
     *  Sometimes know as LEB128 ("Little-Endian Base 128")
     *  in the unsigned variant.
     */
    public int readUntypedCompactInt() throws WireException {

        try {
            int count = 0;
            int res = 0;
            int dig = 0;
            while(true) {
                dig = 0xFF & _dis.readByte();
                count += 1; 

                if ((count == 1) && (dig == 0x80))
                    throw new WireException("Illegal CompactInt (0-0)");
                if (count == 6)
                    throw new WireException("Illegal CompactInt (long)");
                if (res >= 0x1000000)
                    throw new WireException("Illegal CompactInt (big)");

                res = (res << 7) | (dig & 0x7F);

                if ((dig & 0x80)==0x00)  // hi bit zero means last byte
                    break;
            }
            return res;
        } catch (IOException ex) { throw new WireException(ex); }
    }


// (7) Byte Array ..............................................................

    public byte[] readTypedByteArray() throws WireException {
        readAndEnsureWireType(WireTypes.BYTE_ARRAY);
        return readUntypedByteArray();
    }
    
    public byte[] readTypedByteArrayOrNull() throws WireException {
        return readAndEnsureWireTypeOrNull(WireTypes.BYTE_ARRAY) ?
                   readUntypedByteArray() : null;
    }
    
    public byte[] readUntypedByteArray() throws WireException {
        try {
            int len = readUntypedCompactInt();
            byte[] result = new byte[len];
            _dis.readFully(result);
            return result;
        } catch (IOException ex) { throw new WireException(ex); }
    }


// (8) String - UTF-8 Encoding .................................................

    public String readTypedString() throws WireException {
        readAndEnsureWireType(WireTypes.T_STRING);
        return readUntypedString();
    }
    
    public String readTypedStringOrNull() throws WireException {
        return readAndEnsureWireTypeOrNull(WireTypes.T_STRING) ?
                   readUntypedString() : null;
    }
    
    public String readUntypedString() throws WireException {
        try {
            byte[] bs = readUntypedByteArray();
            return new String(bs, "UTF-8");
        } catch (IOException ex) { throw new WireException(ex); }
    }


// (9) BigInteger ..............................................................

    public BigInteger readTypedBigInteger() throws WireException {
        readAndEnsureWireType(WireTypes.BIG_INT);
        return readUntypedBigInteger();
    }
    
    public BigInteger readTypedBigIntegerOrNull() throws WireException {
        return readAndEnsureWireTypeOrNull(WireTypes.BIG_INT) ?
                   readUntypedBigInteger() : null;
    }
    
    public BigInteger readUntypedBigInteger() throws WireException {
        byte[] bs = readUntypedByteArray();
        if (bs.length == 0)
            throw new WireException("Zero length BigInt is impossible");
        return new BigInteger(bs);
    }


// (10) CompactLong ............................................................
     
    public long readTypedCompactLong() throws WireException {
        readAndEnsureWireType(WireTypes.COMPACT_LONG);
        return readUntypedCompactLong();
    }   
    
    public Long readTypedCompactLongOrNull() throws WireException {
        return readAndEnsureWireTypeOrNull(WireTypes.COMPACT_LONG) ?
                   new Long(readUntypedCompactLong()) : null;
    }
    
    public long readUntypedCompactLong() throws WireException {
        /*
         *  See CompactInt doco above.
         *  Fits an 8 byte Unsigned Long into 9 bytes.
         *  63 bits = 7 bits * 9 bytes.
         */  
        try {
            int count = 0;
            long res = 0;
            int dig = 0;
            while(true) {
                dig = 0xFF & _dis.readByte();
                count += 1;

                if ((count == 1) && (dig == 0x80))
                    throw new WireException("Illegal CompactLong (0-0)");
                if (count == 10)
                    throw new WireException("Illegal CompactLong (long)");
// fits entire length
//              if (res >= 0x1000000)
//                  throw new WireException("Illegal CompactLong (big)");

                res = (res << 7) | (dig & 0x7F);

                if ((dig & 0x80)==0x00)  // hi bit zero means last byte
                    break;
            }
            return res;
        } catch (IOException ex) { throw new WireException(ex); }
    }



//..............................................................................
//
// HELPER METHODS
//

    /**
     * Reads a Wire Type from the stream and ensures that it equals the
     * <code>expected</code> type.
     *
     * @throws WireException
     *         When the read type differs from the <code>expected</code> type.
     */
    protected void readAndEnsureWireType(int expected)
        throws WireException
    {
        int found = readUntypedCompactInt();
        if (found != expected) throw new WireException(
            "Invalid type found in WireInputStream! "+
            "Expected: "+expected+", found: "+found);
    }

    /**
     * Reads a Wire Type from the stream and ensures that it equals either
     * <code>null</code> or the <code>expected</code> type.
     *
     * @returns false if the read type is null,
     *          true if it equals the <code>expected</code> type.
     *
     * @throws WireException
     *         When the read type is not null AND it differs from the
     *         <code>expected</code> type.
     */
    protected boolean readAndEnsureWireTypeOrNull(int expected) 
        throws WireException
    {
        int found = readUntypedCompactInt();
        if (found == WireTypes.NULL_REF) return false;
        if (found != expected) throw new WireException(
            "Invalid type found in WireInputStream! "+
            "Expected: "+expected+" or null("+WireTypes.NULL_REF+"), found: "+found);
        return true;
    }
}
