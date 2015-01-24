package cryptix.wire;


import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import cryptix.util.log.TrivialLogger;


/**
 * @throws Panic.assert2() if illegal values provided (x<0, x==null)
 */
public class WireOutputStream
    implements Closeable
{
    private final DataOutputStream _dos;
    private final ByteArrayOutputStream _baos;
    /** @throws NPE unless the empty constructor was used. */
    public byte[] toByteArray() { return _baos.toByteArray(); }
    
    // and what?
    private static TrivialLogger _l = new TrivialLogger();
    
    /**
     * Creates an internal ByteArrayOutputStream,
     * and makes toByteArray() available.
     */
    public WireOutputStream()
    {
        _baos =  new ByteArrayOutputStream();
        _dos = new DataOutputStream(_baos);
    }

    public WireOutputStream(DataOutputStream dos)
    {
        _dos = dos;
        _baos = null;
    }

    public WireOutputStream(OutputStream os)
    {
        _dos = new DataOutputStream(os);
        _baos = null;
    }

    @Override public void close()
    {
        try {
            if (null != _baos)
                _baos.close();
            _dos.close();
        } catch (IOException ex) {
            _l.debug("wosi4183", ex);
        }
    }


    /**
     * Equivalent to os.write().  Could be renamed.
     * @param buf of bytes to write entirely to the stream, no changes
     * @throws IOException
     */
    public void writeBytes(byte[] buf) throws IOException {
        _dos.write(buf);
    }



// (0) Null ....................................................................

    public void writeTypedNull() throws IOException {
        writeWireType(WireTypes.NULL_REF);
    }
    

//// (1) Boolean .................................................................
//
//    public void writeTypedBoolean(boolean x) throws IOException {
//        writeWireType(WireTypes.T_BOOLEAN);
//        writeUntypedBoolean(x);
//    }
//    
//    public void writeTypedBoolean(Boolean x) throws IOException {
//        if (x == null) { writeTypedNull(); return; }
//        writeWireType(WireTypes.T_BOOLEAN);
//        writeUntypedBoolean(x.booleanValue());
//    }
//    
//    public void writeUntypedBoolean(boolean x) throws IOException {
//        _dos.writeBoolean(x);
//    }
//    
//
//// (2) Byte ....................................................................
//
//    public void writeTypedByte(byte x) throws IOException {
//        writeWireType(WireTypes.T_BYTE);
//        writeUntypedByte(x);
//    }
//    
//    public void writeTypedByte(Byte x) throws IOException {
//        if (x == null) { writeTypedNull(); return; }
//        writeWireType(WireTypes.T_BYTE);
//        writeUntypedByte(x.byteValue());
//    }
//    
//    public void writeUntypedByte(byte x) throws IOException {
//        _dos.writeByte(x);
//    }
//    
//
//// (3) Unsigned Byte ...........................................................
//
//    public void writeTypedUnsignedByte(int x) throws IOException {
//        writeWireType(WireTypes.UNSIGNED_BYTE);
//        writeUntypedUnsignedByte(x);
//    }
//    
//    public void writeTypedUnsignedByte(Integer x) throws IOException {
//        if (x == null) { writeTypedNull(); return; }
//        writeWireType(WireTypes.UNSIGNED_BYTE);
//        writeUntypedUnsignedByte(x.intValue());
//    }
//    
//    public void writeUntypedUnsignedByte(int x) throws IOException {
//        Panic.assert2(x >= 0);
//        Panic.assert2(x <= 255);
//        _dos.writeByte(x);
//    }
//    
//
//// (4) Int .....................................................................
//
//    public void writeTypedInt(int x) throws IOException {
//        writeWireType(WireTypes.T_INT);
//        writeUntypedInt(x);
//    }
//    
//    public void writeTypedInt(Integer x) throws IOException {
//        if (x == null) { writeTypedNull(); return; }
//        writeWireType(WireTypes.T_INT);
//        writeUntypedInt(x.intValue());
//    }
//    
//    public void writeUntypedInt(int x) throws IOException {
//        _dos.writeInt(x);
//    }
//    
//
//// (5) Long ....................................................................
//
//    public void writeTypedLong(long x) throws IOException {
//        writeWireType(WireTypes.T_LONG);
//        writeUntypedLong(x);
//    }
//    
//    public void writeTypedLong(Long x) throws IOException {
//        if (x == null) { writeTypedNull(); return; }
//        writeWireType(WireTypes.T_LONG);
//        writeUntypedLong(x.longValue());
//    }
//    
//    public void writeUntypedLong(long x) throws IOException {
//        _dos.writeLong(x);
//    }
    

// (6) CompactInt ..............................................................

    public void writeTypedCompactInt(int x) throws IOException {
        writeWireType(WireTypes.COMPACT_INT);
        writeUntypedCompactInt(x);
    }
    
    public void writeTypedCompactInt(Integer x) throws IOException {
        if (x == null) { writeTypedNull(); return; }
        writeWireType(WireTypes.COMPACT_INT);
        writeUntypedCompactInt(x.intValue());
    }
    
    /**
     *  Natural numbers (0,1,2... not negative).
     *  Used for numbers that have no real size applicable to them,
     *  such as array counts.
     *  Can handle integers up to 31 bits (4 byte positive) long.
     *  @see writeUntypedCompactLong()
     *  
     *  @throws assert / panic if x < 0
     */
    public void writeUntypedCompactInt(int x) throws IOException {
        if (x < 0)
            throw new IllegalArgumentException(x + "<0");
        int len = x;
        int count = 0;
        while (len >= 0x80) {
            len = len >>> 7;
            count++;
        }
        len = x;
        while (count > 0)
        {
            _dos.writeByte((byte)(0x80 | ((len >>> (7*count)) & 0x7F)));
            count--;
        }
        _dos.writeByte((byte)(len & 0x7F));
    }
    

// (7) Byte Array ..............................................................

    public void writeTypedByteArray(byte[] x) throws IOException {
        if (x == null) { writeTypedNull(); return; }
        writeWireType(WireTypes.BYTE_ARRAY);
        writeUntypedByteArray(x);
    }
    
    public void writeUntypedByteArray(byte[] x) throws IOException {
        if (x == null)
            throw new IllegalArgumentException("null x");
        writeUntypedCompactInt(x.length);
        _dos.write(x);
    }
    
    
// (8) String - UTF-8 Encoding .................................................

    public void writeTypedString(String x) throws IOException {
        if (x == null) { writeTypedNull(); return; }
        writeWireType(WireTypes.T_STRING);
        writeUntypedString(x);
    }
    
    public void writeUntypedString(String x) throws IOException {
        if (x == null)
            throw new IllegalArgumentException("x null");
        writeUntypedByteArray(x.getBytes("UTF-8"));
    }
    
    
// (9) BigInteger ..............................................................

    public void writeTypedBigInteger(BigInteger x) throws IOException {
        if (x == null) { writeTypedNull(); return; }
        writeWireType(WireTypes.BIG_INT);
        writeUntypedBigInteger(x);
    }
    
    /**
     * Write out the BigInteger as the byte export of the Java
     * object, wrapped in an untyped byte array.
     * Note that this output preserves sign, which might include
     * leading zero bytes (in contrast to PGP's treatment).
     * 
     * @param x
     * @throws IOException
     */
    public void writeUntypedBigInteger(BigInteger x) throws IOException {
        if (x == null)
            throw new IllegalArgumentException("null Bix x");
        writeUntypedByteArray(x.toByteArray());
    }


// (10) CompactLong ............................................................

    public void writeTypedCompactLong(long x) throws IOException {
        writeWireType(WireTypes.COMPACT_LONG);
        writeUntypedCompactLong(x);
    }
    
    public void writeTypedCompactLong(Long x) throws IOException {
        if (x == null) { writeTypedNull(); return; }
        writeWireType(WireTypes.COMPACT_LONG);
        writeUntypedCompactLong(x.longValue());
    }
    
    /**
     *  Natural numbers (0,1,2... not negative).
     *  Used for numbers that have no real size applicable to them,
     *  such as array counts.  Can handle longs up to 63 bits
     *  (8 byte positive) long.
     */
    public void writeUntypedCompactLong(long x) throws IOException {
        if (x < 0)
            throw new IllegalArgumentException(x+"<0");
        long len = x;
        int count = 0;
        while (len >= 0x80) {
            len = len >>> 7;
            count++;
        }
        len = x;
        while (count > 0)
        {
            _dos.writeByte((byte)(0x80 | ((len >>> (7*count)) & 0x7F)));
            count--;
        }
        _dos.writeByte((byte)(len & 0x7F));
    }
    
    
// Helper methods

    private void writeWireType(int type) throws IOException {
        writeUntypedCompactInt(type);
    }
}
