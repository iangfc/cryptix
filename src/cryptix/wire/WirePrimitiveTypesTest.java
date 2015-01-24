package cryptix.wire;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import cryptix.X;
import cryptix.util.log.TrivialLogger;

/**
 * Tests for *Primitive* types available in the Wire streams,
 * being those from 0 to 20.
 */
public class WirePrimitiveTypesTest
{
    public WirePrimitiveTypesTest() { }


    public static void main(String[] args)
        throws Exception
    {
        String s = "";

        s = WireTypes.selfTest();
        System.out.println(s);
        System.out.println();

        s = selfTest();
        System.out.println(_l.getLog());
        System.out.println(s);
        
        System.exit(0);
    }
    
    public static String selfTest() {
        WirePrimitiveTypesTest t = new WirePrimitiveTypesTest();
        //boolean before = Panic.setHandling(false);
        
        try {
            _l.info("Running WireOutputStream tests");
            t.runOutputStreamTests();
            _l.info("Running WireInputStream tests");
            t.runInputStreamTests();
        } catch (Throwable ex) {
            // don't use Panic because we've turned handling off for tests
            _l.fatal("Tests Failed", ex);
            System.exit(1);
        }

        //Panic.setHandling(before);
        return "wire primitive types succeeded";
    }

    private static TrivialLogger _l = new TrivialLogger();
    private boolean _fail = false;


    /*
     *  All this taken from TestWireStreams.java
     */

    public void run() {

        //boolean before = Panic.setHandling(false);
        try {
            _l.info("Running WireOutputStream tests");
            runOutputStreamTests();
            _l.info("Running WireInputStream tests");
            runInputStreamTests();
        } catch (Throwable ex) {
            _l.warn("",ex);
            _fail = true;
        }
        if (_fail) {
            _l.error("Tests failed");
            System.exit(1);
        } else {
            _l.info("Tests succeeded");
        }

        //Panic.setHandling(before);
    }

    @SuppressWarnings("resource")
    private void runInputStreamTests()
        throws Exception
    {
        PS ps;

        // Null
        _l.info("Running tests for reading nulls");

        ps = new PS(new int[]{0x00});
        try{ ps.readTypedNull(); }catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01});
        try{ ps.readTypedNull(); throw new ENTE();}catch(Exception ex){yes(ex);};


//        // Boolean
//        _l.info("Running tests for reading booleans");
//
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readUntypedBoolean(),false);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01});
//        try{ass(ps.readUntypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0xff});
//        try{ass(ps.readUntypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02});
//        try{ass(ps.readUntypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{});
//        try{    ps.readUntypedBoolean(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x01,0x00});
//        try{ass(ps.readTypedBoolean(),false);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0x01});
//        try{ass(ps.readTypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0xff});
//        try{ass(ps.readTypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0x02});
//        try{ass(ps.readTypedBoolean(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01});
//        try{    ps.readTypedBoolean(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x02});
//        try{    ps.readTypedBoolean(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{    ps.readTypedBoolean(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x01,0x00});
//        try{ass(ps.readTypedBooleanOrNull(),false);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0x01});
//        try{ass(ps.readTypedBooleanOrNull(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0xff});
//        try{ass(ps.readTypedBooleanOrNull(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01,0x02});
//        try{ass(ps.readTypedBooleanOrNull(),true);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x01});
//        try{    ps.readTypedBooleanOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x02});
//        try{    ps.readTypedBooleanOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readTypedBooleanOrNull(),null);}catch(Exception ex){no(ex);};
//
//
//        // Byte
//        _l.info("Running tests for reading bytes");
//
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readUntypedByte(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x7f});
//        try{ass(ps.readUntypedByte(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x80});
//        try{ass(ps.readUntypedByte(),-128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0xff});
//        try{ass(ps.readUntypedByte(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{});
//        try{    ps.readUntypedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x02,0x00});
//        try{ass(ps.readTypedByte(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0x7f});
//        try{ass(ps.readTypedByte(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0x80});
//        try{ass(ps.readTypedByte(),-128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0xff});
//        try{ass(ps.readTypedByte(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02});
//        try{    ps.readTypedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x01,0xff});
//        try{    ps.readTypedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{    ps.readTypedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x02,0x00});
//        try{ass(ps.readTypedByteOrNull(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0x7f});
//        try{ass(ps.readTypedByteOrNull(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0x80});
//        try{ass(ps.readTypedByteOrNull(),-128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02,0xff});
//        try{ass(ps.readTypedByteOrNull(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x02});
//        try{    ps.readTypedByteOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x01,0xff});
//        try{    ps.readTypedByteOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readTypedByteOrNull(),null);}catch(Exception ex){no(ex);};
//
//
//        // Unsigned Byte
//        _l.info("Running tests for reading unsigned bytes");
//
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readUntypedUnsignedByte(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x7f});
//        try{ass(ps.readUntypedUnsignedByte(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x80});
//        try{ass(ps.readUntypedUnsignedByte(),128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0xff});
//        try{ass(ps.readUntypedUnsignedByte(),255);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{});
//        try{    ps.readUntypedUnsignedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x03,0x00});
//        try{ass(ps.readTypedUnsignedByte(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0x7f});
//        try{ass(ps.readTypedUnsignedByte(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0x80});
//        try{ass(ps.readTypedUnsignedByte(),128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0xff});
//        try{ass(ps.readTypedUnsignedByte(),255);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03});
//        try{    ps.readTypedUnsignedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x02,0xff});
//        try{    ps.readTypedUnsignedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{    ps.readTypedUnsignedByte(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x03,0x00});
//        try{ass(ps.readTypedUnsignedByteOrNull(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0x7f});
//        try{ass(ps.readTypedUnsignedByteOrNull(),127);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0x80});
//        try{ass(ps.readTypedUnsignedByteOrNull(),128);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03,0xff});
//        try{ass(ps.readTypedUnsignedByteOrNull(),255);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x03});
//        try{    ps.readTypedUnsignedByteOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x02,0xff});
//        try{    ps.readTypedUnsignedByteOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readTypedUnsignedByteOrNull(),null);}catch(Exception ex){no(ex);};
//
//
//        // Int
//        _l.info("Running tests for reading ints");
//
//        ps = new PS(new int[]{0x00,0x00,0x00,0x00});
//        try{ass(ps.readUntypedInt(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x12,0x34,0x56,0x78});
//        try{ass(ps.readUntypedInt(),0x12345678);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0xff,0xff,0xff,0xff});
//        try{ass(ps.readUntypedInt(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x34,0x56,0x78});
//        try{    ps.readUntypedInt(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x04,0x00,0x00,0x00,0x00});
//        try{ass(ps.readTypedInt(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0x12,0x34,0x56,0x78});
//        try{ass(ps.readTypedInt(),0x12345678);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0xff,0xff,0xff,0xff});
//        try{ass(ps.readTypedInt(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0x34,0x56,0x78});
//        try{    ps.readTypedInt(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x03,0x12,0x34,0x56,0x78});
//        try{    ps.readTypedInt(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{    ps.readTypedInt(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x04,0x00,0x00,0x00,0x00});
//        try{ass(ps.readTypedIntOrNull(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0x12,0x34,0x56,0x78});
//        try{ass(ps.readTypedIntOrNull(),0x12345678);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0xff,0xff,0xff,0xff});
//        try{ass(ps.readTypedIntOrNull(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x04,0x34,0x56,0x78});
//        try{    ps.readTypedIntOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x03,0x12,0x34,0x56,0x78});
//        try{    ps.readTypedIntOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readTypedIntOrNull(),null);}catch(Exception ex){no(ex);};
//
//
//        // Long
//        _l.info("Running tests for reading longs");
//
//        ps = new PS(new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        try{ass(ps.readUntypedLong(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{ass(ps.readUntypedLong(),0x123456789abcdef0L);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//        try{ass(ps.readUntypedLong(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{    ps.readUntypedLong(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        try{ass(ps.readTypedLong(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{ass(ps.readTypedLong(),0x123456789abcdef0L);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//        try{ass(ps.readTypedLong(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{    ps.readTypedLong(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x04,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{    ps.readTypedLong(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{    ps.readTypedLong(); throw new ENTE();}catch(Exception ex){yes(ex);};
//
//        ps = new PS(new int[]{0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        try{ass(ps.readTypedLongOrNull(),0);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{ass(ps.readTypedLongOrNull(),0x123456789abcdef0L);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//        try{ass(ps.readTypedLongOrNull(),-1);}catch(Exception ex){no(ex);};
//        ps = new PS(new int[]{0x05,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{    ps.readTypedLongOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x04,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        try{    ps.readTypedLongOrNull(); throw new ENTE();}catch(Exception ex){yes(ex);};
//        ps = new PS(new int[]{0x00});
//        try{ass(ps.readTypedLongOrNull(),null);}catch(Exception ex){no(ex);};
//

        // CompactInt
        _l.info("Running tests for reading compact ints");

        ps = new PS(new int[]{0x00});
        try{ass(ps.readUntypedCompactInt(),0x00);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x7f});
        try{ass(ps.readUntypedCompactInt(),0x7f);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x80);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x7F});
        try{ass(ps.readUntypedCompactInt(),0xff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x82, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x100);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0xff, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0x3fff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x80, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x4000);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x80, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0x407f);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x81, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x4080);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0xff, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0x7fff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x82, 0x80, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x8000);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0xff, 0xff, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0x1fffff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x80, 0x80, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x200000);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0xff, 0xff, 0xff, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0xfffffff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x81, 0x80, 0x80, 0x80, 0x00});
        try{ass(ps.readUntypedCompactInt(),0x10000000);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x87, 0xff, 0xff, 0xff, 0x7f});
        try{ass(ps.readUntypedCompactInt(),0x7fffffff);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x80});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x81});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x80, 0x00});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x88, 0x80, 0x80, 0x80, 0x00});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0xff, 0xff, 0xff, 0xff, 0x7f});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x80, 0x80, 0x80, 0x80, 0x80, 0x00});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x81, 0x80, 0x80, 0x80, 0x80, 0x00});
        try{    ps.readUntypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};

        ps = new PS(new int[]{0x06,0x45});
        try{ass(ps.readTypedCompactInt(),0x45);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x06,0x87, 0xff, 0xff, 0xa2, 0x73});
        try{ass(ps.readTypedCompactInt(),0x7fffd173);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x06,0x9a});
        try{    ps.readTypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x06,0x80, 0x67});
        try{    ps.readTypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x06,0xbc, 0x80, 0x80, 0x80, 0x00});
        try{    ps.readTypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x04,0x45,0x45,0x45,0x45});
        try{    ps.readTypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x00,0x45});
        try{    ps.readTypedCompactInt();throw new ENTE();}catch(Exception ex){yes(ex);};

        ps = new PS(new int[]{0x06,0x67});
        try{ass(ps.readTypedCompactIntOrNull(),0x67);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x06,0x87, 0xff, 0xff, 0xc4, 0x75});
        try{ass(ps.readTypedCompactIntOrNull(),0x7fffe275);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x06,0xbc});
        try{    ps.readTypedCompactIntOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x06,0x80, 0x7f});
        try{    ps.readTypedCompactIntOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x06,0xde, 0x80, 0x80, 0x80, 0x00});
        try{    ps.readTypedCompactIntOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x04,0x67,0x67,0x67,0x67});
        try{    ps.readTypedCompactIntOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x00});
        try{ass(ps.readTypedCompactIntOrNull(),null);}catch(Exception ex){no(ex);};


        // Byte Array
        _l.info("Running tests for reading byte arrays");

        byte[] bs; int[] is;
        
        bs = new byte[0]; is = new int[bs.length+1];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+1] = i; }
        is[0] = 0x00; ps = new PS(is); 
        try{ass(ps.readUntypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[1]; is = new int[bs.length+1];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+1] = i; }
        is[0] = 0x01; ps = new PS(is); 
        try{ass(ps.readUntypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[127]; is = new int[bs.length+1];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+1] = i; }
        is[0] = 0x7f; ps = new PS(is); 
        try{ass(ps.readUntypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[128]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x81; is[1] = 0x00; ps = new PS(is); 
        try{ass(ps.readUntypedByteArray(),bs);}catch(Exception ex){no(ex);};

        bs = new byte[0]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x00; ps = new PS(is); 
        try{ass(ps.readTypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[1]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x01; ps = new PS(is); 
        try{ass(ps.readTypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[127]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x7f; ps = new PS(is); 
        try{ass(ps.readTypedByteArray(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[128]; is = new int[bs.length+3];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+3] = i; }
        is[0] = 0x07; is[1] = 0x81; is[2] = 0x00; ps = new PS(is); 
        try{ass(ps.readTypedByteArray(),bs);}catch(Exception ex){no(ex);};

        bs = new byte[0]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x00; ps = new PS(is); 
        try{ass(ps.readTypedByteArrayOrNull(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[1]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x01; ps = new PS(is); 
        try{ass(ps.readTypedByteArrayOrNull(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[127]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x7f; ps = new PS(is); 
        try{ass(ps.readTypedByteArrayOrNull(),bs);}catch(Exception ex){no(ex);};
        bs = new byte[128]; is = new int[bs.length+3];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+3] = i; }
        is[0] = 0x07; is[1] = 0x81; is[2] = 0x00; ps = new PS(is); 
        try{ass(ps.readTypedByteArrayOrNull(),bs);}catch(Exception ex){no(ex);};
        
        is = new int[1]; is[0] = 0x00; ps = new PS(is);
        try{ass(ps.readTypedByteArrayOrNull(),null);}catch(Exception ex){no(ex);};

        is = new int[16];

        is[0] = 0x10; ps = new PS(is); 
        try{ps.readUntypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x80; is[1] = 0x00; ps = new PS(is); 
        try{ps.readUntypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0xff; is[1] = 0xff; is[2] = 0x7f; ps = new PS(is); 
        try{ps.readUntypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x88; is[1] = 0x80; is[2] = 0x80; is[3] = 0x80; is[4] = 0x00; ps = new PS(is); 
        try{ps.readUntypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};

        is[0] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x06; is[1] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0x0f; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0x80; is[2] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0xff; is[2] = 0xff; is[3] = 0x7f; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0x88; is[2] = 0x80; is[3] = 0x80; is[4] = 0x80; is[5] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArray();throw new ENTE();}catch(Exception ex){yes(ex);};

        is[0] = 0x06; is[1] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArrayOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[0] = 0x0f; ps = new PS(is); 
        try{ps.readTypedByteArrayOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0x80; is[2] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArrayOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0xff; is[2] = 0xff; is[3] = 0x7f; ps = new PS(is); 
        try{ps.readTypedByteArrayOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};
        is[0] = 0x07; is[1] = 0x88; is[2] = 0x80; is[3] = 0x80; is[4] = 0x80; is[5] = 0x00; ps = new PS(is); 
        try{ps.readTypedByteArrayOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};

        
        // String
        _l.info("Running tests for reading strings");

        String ls;

        ps = new PS(new int[]{0x00});
        try{ass(ps.readUntypedString(),"");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01,0x20});
        try{ass(ps.readUntypedString()," ");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x02,0xc2,0x80});
        try{ass(ps.readUntypedString(),"\u0080");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x02,0x61,0x62});
        try{ass(ps.readUntypedString(),"ab");}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                               ";
        is = new int[128]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x7f; ps = new PS(is);
        try{ass(ps.readUntypedString(),ls);}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                                ";
        is = new int[130]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x81; is[1] = 0x00; ps = new PS(is);
        try{ass(ps.readUntypedString(),ls);}catch(Exception ex){no(ex);};
        
        
        ps = new PS(new int[]{0x08,0x00});
        try{ass(ps.readTypedString(),"");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x01,0x20});
        try{ass(ps.readTypedString()," ");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x02,0xc2,0x80});
        try{ass(ps.readTypedString(),"\u0080");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x02,0x61,0x62});
        try{ass(ps.readTypedString(),"ab");}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                               ";
        is = new int[129]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x08; is[1] = 0x7f; ps = new PS(is);
        try{ass(ps.readTypedString(),ls);}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                                ";
        is = new int[131]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x08; is[1] = 0x81; is[2] = 0x00; ps = new PS(is);
        try{ass(ps.readTypedString(),ls);}catch(Exception ex){no(ex);};

        ps = new PS(new int[]{0x08,0x00});
        try{ass(ps.readTypedStringOrNull(),"");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x01,0x20});
        try{ass(ps.readTypedStringOrNull()," ");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x02,0xc2,0x80});
        try{ass(ps.readTypedStringOrNull(),"\u0080");}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x08,0x02,0x61,0x62});
        try{ass(ps.readTypedStringOrNull(),"ab");}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                               ";
        is = new int[129]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x08; is[1] = 0x7f; ps = new PS(is);
        try{ass(ps.readTypedStringOrNull(),ls);}catch(Exception ex){no(ex);};
        ls = "                                "+
             "                                "+
             "                                "+
             "                                ";
        is = new int[131]; for (int i=0; i<is.length; i++) is[i] = 0x20;
        is[0] = 0x08; is[1] = 0x81; is[2] = 0x00; ps = new PS(is);
        try{ass(ps.readTypedStringOrNull(),ls);}catch(Exception ex){no(ex);};


        ps = new PS(new int[]{0x00});
        try{ps.readTypedString();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x00});
        try{ass(ps.readTypedStringOrNull(),null);}catch(Exception ex){no(ex);};

        ps = new PS(new int[]{0x07,0x01,0x61});
        try{ps.readTypedString();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x07,0x01,0x61});
        try{ps.readTypedStringOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};

        // BigInt
        _l.info("Running tests for reading big ints");
        
        ps = new PS(new int[]{0x01,0x00});
        try{ass(ps.readUntypedBigInteger(),0);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01,0x01});
        try{ass(ps.readUntypedBigInteger(),1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01,0xff});
        try{ass(ps.readUntypedBigInteger(),-1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01,0x7f});
        try{ass(ps.readUntypedBigInteger(),127);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x02,0x00,0x80});
        try{ass(ps.readUntypedBigInteger(),128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x01,0x80});
        try{ass(ps.readUntypedBigInteger(),-128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x02,0xff,0x7f});
        try{ass(ps.readUntypedBigInteger(),-129);}catch(Exception ex){no(ex);};
        
        bs = new byte[128];
        for (int i=0; i<bs.length; i++) bs[i] = (byte)0;
        bs[0] = (byte)1;

        is = new int[130];
        for (int i=3; i<is.length; i++) is[i] = 0;
        is[0] = 0x81;
        is[1] = 0x00;
        is[2] = 0x01;
        ps = new PS(is);
        try{ass(ps.readUntypedBigInteger(),new BigInteger(bs));}catch(Exception ex){no(ex);};
        
        ps = new PS(new int[]{0x09,0x01,0x00});
        try{ass(ps.readTypedBigInteger(),0);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x01});
        try{ass(ps.readTypedBigInteger(),1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0xff});
        try{ass(ps.readTypedBigInteger(),-1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x7f});
        try{ass(ps.readTypedBigInteger(),127);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x02,0x00,0x80});
        try{ass(ps.readTypedBigInteger(),128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x80});
        try{ass(ps.readTypedBigInteger(),-128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x02,0xff,0x7f});
        try{ass(ps.readTypedBigInteger(),-129);}catch(Exception ex){no(ex);};
        
        bs = new byte[128];
        for (int i=0; i<bs.length; i++) bs[i] = (byte)0;
        bs[0] = (byte)1;

        is = new int[131];
        for (int i=4; i<is.length; i++) is[i] = 0;
        is[0] = 0x09;
        is[1] = 0x81;
        is[2] = 0x00;
        is[3] = 0x01;
        ps = new PS(is);
        try{ass(ps.readTypedBigInteger(),new BigInteger(bs));}catch(Exception ex){no(ex);};
        
        ps = new PS(new int[]{0x09,0x01,0x00});
        try{ass(ps.readTypedBigIntegerOrNull(),0);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x01});
        try{ass(ps.readTypedBigIntegerOrNull(),1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0xff});
        try{ass(ps.readTypedBigIntegerOrNull(),-1);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x7f});
        try{ass(ps.readTypedBigIntegerOrNull(),127);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x02,0x00,0x80});
        try{ass(ps.readTypedBigIntegerOrNull(),128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x01,0x80});
        try{ass(ps.readTypedBigIntegerOrNull(),-128);}catch(Exception ex){no(ex);};
        ps = new PS(new int[]{0x09,0x02,0xff,0x7f});
        try{ass(ps.readTypedBigIntegerOrNull(),-129);}catch(Exception ex){no(ex);};
        
        bs = new byte[128];
        for (int i=0; i<bs.length; i++) bs[i] = (byte)0;
        bs[0] = (byte)1;

        is = new int[131];
        for (int i=4; i<is.length; i++) is[i] = 0;
        is[0] = 0x09;
        is[1] = 0x81;
        is[2] = 0x00;
        is[3] = 0x01;
        ps = new PS(is);
        try{ass(ps.readTypedBigIntegerOrNull(),new BigInteger(bs));}catch(Exception ex){no(ex);};
        
        is = new int[1]; is[0] = 0x00; ps = new PS(is);
        try{ps.readUntypedBigInteger();throw new ENTE();}catch(Exception ex){yes(ex);};
        is = new int[1]; is[0] = 0x00; ps = new PS(is);
        try{ps.readTypedBigInteger();throw new ENTE();}catch(Exception ex){yes(ex);};
        is = new int[1]; is[0] = 0x00; ps = new PS(is);
        try{ass(ps.readTypedBigIntegerOrNull(),null);}catch(Exception ex){no(ex);};
        is = new int[2]; is[0] = 0x09; is[1] = 0x00; ps = new PS(is);
        try{ps.readTypedBigInteger();throw new ENTE();}catch(Exception ex){yes(ex);};
        is = new int[2]; is[0] = 0x09; is[1] = 0x00; ps = new PS(is);
        try{ps.readTypedBigIntegerOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};

        ps = new PS(new int[]{0x07,0x01,0x61});
        try{ps.readTypedBigInteger();throw new ENTE();}catch(Exception ex){yes(ex);};
        ps = new PS(new int[]{0x07,0x01,0x61});
        try{ps.readTypedBigIntegerOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};


//        // out of range number
//        _l.info("Running tests for out of range wiretypes");
//        ps = new PS(new int[]{0xff,0xff,0xff,0x00});
//        try{ps.readTypedObjectOrNull();throw new ENTE();}catch(Exception ex){yes(ex);};

    }

//    private void ass(boolean found, boolean expect)
//    { ass2 (new Boolean(found), new Boolean(expect)); }
//
//    private void ass(Boolean found, boolean expect)
//    { ass2 (found, new Boolean(expect)); }

    private void ass(long found, long expect)
    { ass2 (new Long(found), new Long(expect)); }

    private void ass(Number found, long expect)
    { ass2 (found, new Long(expect)); }

    private void ass(byte[] found, byte[] expect)
    { ass2 (found == null ? null : X.data2hex(found), 
           expect == null ? null : X.data2hex(expect)); }

    private void ass(Object found, Object expect)
    { ass2 (found, expect); }

    private void ass2(Object found, Object expect)
    {
        _l.debug("Expect: |"+expect+"|");
        _l.debug("Found:  |"+found+"|");
        if (! equals(found,expect)) {
            try {
                throw new FAIL();
            } catch (FAIL ex) {
                StackTraceElement el = ex.getStackTrace()[2];
                _l.warn("Fail: expected != found at line: "+el.getLineNumber());
                _fail = true;
            }
        }
    }

    private boolean equals(Object a, Object b)
    {
        if (a == null) {
            if (b == null) return true;
            return false;
        }
        if (b == null) return false;
        if ((a instanceof Number) && (b instanceof Number))
            return ((Number)a).toString().equals(((Number)b).toString());
        return a.equals(b);
    }

    private void no(Throwable ex)
    {
        Throwable t = new Throwable(); t.fillInStackTrace();
        if (ex instanceof WireException) {
            _l.warn("Fail: WireException not expected at line: "+
                t.getStackTrace()[1].getLineNumber());
            ex.printStackTrace();
            _fail = true;
        } else {
            _l.warn("Fail: ", ex);
            _fail = true;
        }
    }

    private void yes(Throwable ex)
    {
        Throwable t = new Throwable(); t.fillInStackTrace();
        if (ex instanceof WireException) {
            _l.debug("Good: WireException");
        } else if (ex instanceof ENTE) {
            StackTraceElement el = ex.getStackTrace()[0];
            _l.warn("Fail: no exception thrown at line: "+el.getLineNumber());
            _fail = true;
        } else {
            _l.warn("Fail: ", ex);
            _fail = true;
        }
    }

//    @SuppressWarnings("resource")
    private void runOutputStreamTests()
        throws Exception
    {
        OS os;

//        // Non-encodeable object
//        _l.info("Running tests for writing non-encodable objects");
//        try {
//            os = new OS(); os.writeTypedObject(new Object());
//            /* oops, it didn't throw anything! */
//            throw new ENTE();
//        } catch(IllegalArgumentException ex) {
//            // this we expect
//        }

        // Null
        _l.info("Running tests for writing nulls");

        os = new OS(); os.writeTypedNull();
        cmp (os, new int[]{0x00});


//        // Boolean
//        _l.info("Running tests for writing booleans");
//
//        os = new OS(); os.writeUntypedBoolean(true);
//        cmp (os, new int[]{0x01});
//        os = new OS(); os.writeUntypedBoolean(false);
//        cmp (os, new int[]{0x00});
//
//        os = new OS(); os.writeTypedBoolean(true);
//        cmp (os, new int[]{0x01,0x01});
//        os = new OS(); os.writeTypedBoolean(false);
//        cmp (os, new int[]{0x01,0x00});
//
//        os = new OS(); os.writeTypedBoolean(new Boolean(true));
//        cmp (os, new int[]{0x01,0x01});
//        os = new OS(); os.writeTypedBoolean(new Boolean(false));
//        cmp (os, new int[]{0x01,0x00});
//        os = new OS(); os.writeTypedBoolean(null);
//        cmp (os, new int[]{0x00});
//
//
//        // Byte
//        _l.info("Running tests for writing bytes");
//
//        os = new OS(); os.writeUntypedByte((byte)-128);
//        cmp (os, new int[]{0x80});
//        os = new OS(); os.writeUntypedByte((byte)-1);
//        cmp (os, new int[]{0xff});
//        os = new OS(); os.writeUntypedByte((byte)0);
//        cmp (os, new int[]{0x00});
//        os = new OS(); os.writeUntypedByte((byte)127);
//        cmp (os, new int[]{0x7f});
//        os = new OS(); os.writeUntypedByte((byte)128);
//        cmp (os, new int[]{0x80});
//        os = new OS(); os.writeUntypedByte((byte)255);
//        cmp (os, new int[]{0xff});
//
//        os = new OS(); os.writeTypedByte((byte)-128);
//        cmp (os, new int[]{0x02,0x80});
//        os = new OS(); os.writeTypedByte((byte)-1);
//        cmp (os, new int[]{0x02,0xff});
//        os = new OS(); os.writeTypedByte((byte)0);
//        cmp (os, new int[]{0x02,0x00});
//        os = new OS(); os.writeTypedByte((byte)127);
//        cmp (os, new int[]{0x02,0x7f});
//        os = new OS(); os.writeTypedByte((byte)128);
//        cmp (os, new int[]{0x02,0x80});
//        os = new OS(); os.writeTypedByte((byte)255);
//        cmp (os, new int[]{0x02,0xff});
//
//        os = new OS(); os.writeTypedByte(new Byte((byte)-128));
//        cmp (os, new int[]{0x02,0x80});
//        os = new OS(); os.writeTypedByte(new Byte((byte)-1));
//        cmp (os, new int[]{0x02,0xff});
//        os = new OS(); os.writeTypedByte(new Byte((byte)0));
//        cmp (os, new int[]{0x02,0x00});
//        os = new OS(); os.writeTypedByte(new Byte((byte)127));
//        cmp (os, new int[]{0x02,0x7f});
//        os = new OS(); os.writeTypedByte(new Byte((byte)128));
//        cmp (os, new int[]{0x02,0x80});
//        os = new OS(); os.writeTypedByte(new Byte((byte)255));
//        cmp (os, new int[]{0x02,0xff});
//        os = new OS(); os.writeTypedByte(null);
//        cmp (os, new int[]{0x00});
//
//
//        // Unsigned Byte
//        _l.info("Running tests for writing unsigned bytes");
//
//        os = new OS(); os.writeUntypedUnsignedByte(0);
//        cmp (os, new int[]{0x00});
//        os = new OS(); os.writeUntypedUnsignedByte(127);
//        cmp (os, new int[]{0x7f});
//        os = new OS(); os.writeUntypedUnsignedByte(128);
//        cmp (os, new int[]{0x80});
//        os = new OS(); os.writeUntypedUnsignedByte(255);
//        cmp (os, new int[]{0xff});
//        try{(new OS()).writeUntypedUnsignedByte(-1);
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//        try{(new OS()).writeUntypedUnsignedByte(256);
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//
//        os = new OS(); os.writeTypedUnsignedByte(0);
//        cmp (os, new int[]{0x03,0x00});
//        os = new OS(); os.writeTypedUnsignedByte(127);
//        cmp (os, new int[]{0x03,0x7f});
//        os = new OS(); os.writeTypedUnsignedByte(128);
//        cmp (os, new int[]{0x03,0x80});
//        os = new OS(); os.writeTypedUnsignedByte(255);
//        cmp (os, new int[]{0x03,0xff});
//        try{(new OS()).writeTypedUnsignedByte(-1);
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//        try{(new OS()).writeTypedUnsignedByte(256);
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//
//        os = new OS(); os.writeTypedUnsignedByte(new Integer(0));
//        cmp (os, new int[]{0x03,0x00});
//        os = new OS(); os.writeTypedUnsignedByte(new Integer(127));
//        cmp (os, new int[]{0x03,0x7f});
//        os = new OS(); os.writeTypedUnsignedByte(new Integer(128));
//        cmp (os, new int[]{0x03,0x80});
//        os = new OS(); os.writeTypedUnsignedByte(new Integer(255));
//        cmp (os, new int[]{0x03,0xff});
//        os = new OS(); os.writeTypedUnsignedByte(null);
//        cmp (os, new int[]{0x00});
//        try{(new OS()).writeTypedUnsignedByte(new Integer(-1));
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//        try{(new OS()).writeTypedUnsignedByte(new Integer(256));
//        throw new ENTE(); } catch (Throwable e) { exc(e); }
//
//
//        // Int
//        _l.info("Running tests for writing ints");
//
//        os = new OS(); os.writeUntypedInt(0x12345678);
//        cmp (os, new int[]{0x12,0x34,0x56,0x78});
//        os = new OS(); os.writeUntypedInt(0);
//        cmp (os, new int[]{0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeUntypedInt(-1);
//        cmp (os, new int[]{0xff,0xff,0xff,0xff});
//
//        os = new OS(); os.writeTypedInt(0x12345678);
//        cmp (os, new int[]{0x04,0x12,0x34,0x56,0x78});
//        os = new OS(); os.writeTypedInt(0);
//        cmp (os, new int[]{0x04,0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeTypedInt(-1);
//        cmp (os, new int[]{0x04,0xff,0xff,0xff,0xff});
//
//        os = new OS(); os.writeTypedInt(new Integer(0x12345678));
//        cmp (os, new int[]{0x04,0x12,0x34,0x56,0x78});
//        os = new OS(); os.writeTypedInt(new Integer(0));
//        cmp (os, new int[]{0x04,0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeTypedInt(new Integer(-1));
//        cmp (os, new int[]{0x04,0xff,0xff,0xff,0xff});
//        os = new OS(); os.writeTypedInt(null);
//        cmp (os, new int[]{0x00});
//
//
//        // Long
//        _l.info("Running tests for writing longs");
//
//        os = new OS(); os.writeUntypedLong(0x123456789abcdef0L);
//        cmp (os, new int[]{0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        os = new OS(); os.writeUntypedLong(0);
//        cmp (os, new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeUntypedLong(-1);
//        cmp (os, new int[]{0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//
//        os = new OS(); os.writeTypedLong(0x123456789abcdef0L);
//        cmp (os, new int[]{0x05,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        os = new OS(); os.writeTypedLong(0);
//        cmp (os, new int[]{0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeTypedLong(-1);
//        cmp (os, new int[]{0x05,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//
//        os = new OS(); os.writeTypedLong(new Long(0x123456789abcdef0L));
//        cmp (os, new int[]{0x05,0x12,0x34,0x56,0x78,0x9a,0xbc,0xde,0xf0});
//        os = new OS(); os.writeTypedLong(new Long(0));
//        cmp (os, new int[]{0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
//        os = new OS(); os.writeTypedLong(new Long(-1));
//        cmp (os, new int[]{0x05,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff});
//        os = new OS(); os.writeTypedLong(null);
//        cmp (os, new int[]{0x00});


        // CompactInt
        _l.info("Running tests for writing compact ints");

        os = new OS(); os.writeUntypedCompactInt(0x00);
        cmp (os, new int[]{0x00});
        os = new OS(); os.writeUntypedCompactInt(0x7f);
        cmp (os, new int[]{0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x80);
        cmp (os, new int[]{0x81, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0xff);
        cmp (os, new int[]{0x81, 0x7F});
        os = new OS(); os.writeUntypedCompactInt(0x100);
        cmp (os, new int[]{0x82, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0x3fff);
        cmp (os, new int[]{0xff, 0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x4000);
        cmp (os, new int[]{0x81, 0x80, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0x407f);
        cmp (os, new int[]{0x81, 0x80, 0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x4080);
        cmp (os, new int[]{0x81, 0x81, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0x7fff);
        cmp (os, new int[]{0x81, 0xff, 0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x8000);
        cmp (os, new int[]{0x82, 0x80, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0x1fffff);
        cmp (os, new int[]{0xff, 0xff, 0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x200000);
        cmp (os, new int[]{0x81, 0x80, 0x80, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0xfffffff);
        cmp (os, new int[]{0xff, 0xff, 0xff, 0x7f});
        os = new OS(); os.writeUntypedCompactInt(0x10000000);
        cmp (os, new int[]{0x81, 0x80, 0x80, 0x80, 0x00});
        os = new OS(); os.writeUntypedCompactInt(0x7fffffff);
        cmp (os, new int[]{0x87, 0xff, 0xff, 0xff, 0x7f});

        os = new OS(); os.writeTypedCompactInt(0x12);
        cmp (os, new int[]{0x06,0x12});
        os = new OS(); os.writeTypedCompactInt(0x7fffff34);
        cmp (os, new int[]{0x06,0x87, 0xff, 0xff, 0xfe, 0x34});

        os = new OS(); os.writeTypedCompactInt(new Integer(0x56));
        cmp (os, new int[]{0x06,0x56});
        os = new OS(); os.writeTypedCompactInt(new Integer(0x7fffff78));
        cmp (os, new int[]{0x06,0x87, 0xff, 0xff, 0xfe, 0x78});
        os = new OS(); os.writeTypedCompactInt(null);
        cmp (os, new int[]{0x00});

        try{(new OS()).writeUntypedCompactInt(-1);
        throw new ENTE(); } catch (Throwable e) { exc(e); }
        try{(new OS()).writeTypedCompactInt(-1);
        throw new ENTE(); } catch (Throwable e) { exc(e); }
        try{(new OS()).writeTypedCompactInt(new Integer(-1));
        throw new ENTE(); } catch (Throwable e) { exc(e); }


        // Byte array
        _l.info("Running tests for writing byte arrays");

        byte[] bs;
        int[] is;

        bs = new byte[0]; is = new int[bs.length+2];
        is[0] = 0x07; is[1] = 0x00;
        os = new OS(); os.writeTypedByteArray(bs); cmp(os,is);

        bs = new byte[1]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)12; is[i+2] = 12; }
        is[0] = 0x07; is[1] = 0x01;
        os = new OS(); os.writeTypedByteArray(bs); cmp(os,is);

        bs = new byte[127]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x07; is[1] = 0x7f;
        os = new OS(); os.writeTypedByteArray(bs); cmp(os,is);

        bs = new byte[128]; is = new int[bs.length+3];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+3] = i; }
        is[0] = 0x07; is[1] = 0x81; is[2] = 0x00;
        os = new OS(); os.writeTypedByteArray(bs); cmp(os,is);

        os = new OS(); os.writeTypedByteArray(null);
        cmp (os, new int[]{0x00});


        bs = new byte[0]; is = new int[bs.length+1];
        is[0] = 0x00;
        os = new OS(); os.writeUntypedByteArray(bs); cmp(os,is);

        bs = new byte[1]; is = new int[bs.length+1];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)34; is[i+1] = 34; }
        is[0] = 0x01;
        os = new OS(); os.writeUntypedByteArray(bs); cmp(os,is);

        bs = new byte[127]; is = new int[bs.length+1];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+1] = i; }
        is[0] = 0x7f;
        os = new OS(); os.writeUntypedByteArray(bs); cmp(os,is);

        bs = new byte[128]; is = new int[bs.length+2];
        for (int i=0; i<bs.length; i++) { bs[i] = (byte)i; is[i+2] = i; }
        is[0] = 0x81; is[1] = 0x00;
        os = new OS(); os.writeUntypedByteArray(bs); cmp(os,is);

        try{(new OS()).writeUntypedByteArray(null);
        throw new ENTE(); } catch (Throwable e) { exc(e); }


        // String
        _l.info("Running tests for writing strings");

        os = new OS(); os.writeTypedString("");
        cmp (os, new int[]{0x08,0x00});
        os = new OS(); os.writeTypedString(" ");
        cmp (os, new int[]{0x08,0x01,0x20});
        os = new OS(); os.writeTypedString("\u0080");
        cmp (os, new int[]{0x08,0x02,0xc2,0x80});
        os = new OS(); os.writeTypedString("ab");
        cmp (os, new int[]{0x08,0x02,0x61,0x62});
        os = new OS(); os.writeTypedString(
                       "                                "+
                       "                                "+
                       "                                "+
                       "                               ");
        cmp (os, new int[]{0x08,0x7f,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20
                       });
        os = new OS(); os.writeTypedString(
                       "                                "+
                       "                                "+
                       "                                "+
                       "                                ");
        cmp (os, new int[]{0x08,0x81,0x00,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20
                       });
        os = new OS(); os.writeTypedString(null);
        cmp (os, new int[]{0x00});

        os = new OS(); os.writeUntypedString("");
        cmp (os, new int[]{0x00});
        os = new OS(); os.writeUntypedString(" ");
        cmp (os, new int[]{0x01,0x20});
        os = new OS(); os.writeUntypedString("\u0080");
        cmp (os, new int[]{0x02,0xc2,0x80});
        os = new OS(); os.writeUntypedString("ab");
        cmp (os, new int[]{0x02,0x61,0x62});
        os = new OS(); os.writeUntypedString("                                "+
                                             "                                "+
                                             "                                "+
                                             "                               ");
        cmp (os, new int[]{0x7f,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20
                       });
        os = new OS(); os.writeUntypedString(
                       "                                "+
                       "                                "+
                       "                                "+
                       "                                "
                       );
        cmp (os, new int[]{0x81,0x00,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,
                       0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20
                       });
        try{(new OS()).writeUntypedString(null);
        throw new ENTE(); } catch (Throwable e) { exc(e); }


        // BigInteger
        _l.info("Running tests for writing BigInteger");


        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf(   0));
        cmp (os, new int[]{0x09,0x01,0x00});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf(   1));
        cmp (os, new int[]{0x09,0x01,0x01});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf(  -1));
        cmp (os, new int[]{0x09,0x01,0xff});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf( 127));
        cmp (os, new int[]{0x09,0x01,0x7f});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf( 128));
        cmp (os, new int[]{0x09,0x02,0x00,0x80});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf(-128));
        cmp (os, new int[]{0x09,0x01,0x80});
        os = new OS(); os.writeTypedBigInteger(BigInteger.valueOf(-129));
        cmp (os, new int[]{0x09,0x02,0xff,0x7f});

        bs = new byte[128];
        for (int i=0; i<bs.length; i++) bs[i] = (byte)0;
        bs[0] = (byte)1;

        is = new int[131];
        for (int i=4; i<is.length; i++) is[i] = 0;
        is[0] = 0x09;
        is[1] = 0x81;
        is[2] = 0x00;
        is[3] = 0x01;

        os = new OS(); os.writeTypedBigInteger(new BigInteger(bs));
        cmp (os,is);

        os = new OS(); os.writeTypedBigInteger(null);
        cmp (os, new int[]{0x00});


        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf(   0));
        cmp (os, new int[]{0x01,0x00});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf(   1));
        cmp (os, new int[]{0x01,0x01});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf(  -1));
        cmp (os, new int[]{0x01,0xff});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf( 127));
        cmp (os, new int[]{0x01,0x7f});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf( 128));
        cmp (os, new int[]{0x02,0x00,0x80});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf(-128));
        cmp (os, new int[]{0x01,0x80});
        os = new OS(); os.writeUntypedBigInteger(BigInteger.valueOf(-129));
        cmp (os, new int[]{0x02,0xff,0x7f});

        is = new int[130];
        for (int i=3; i<is.length; i++) is[i] = 0;
        is[0] = 0x81;
        is[1] = 0x00;
        is[2] = 0x01;

        os = new OS(); os.writeUntypedBigInteger(new BigInteger(bs));
        cmp (os,is);

        try{(new OS()).writeUntypedBigInteger(null);
        throw new ENTE(); } catch (Throwable e) { exc(e); }

    }

    private void exc(Throwable ex)
    {
        if (ex instanceof ENTE) {
            StackTraceElement el = ex.getStackTrace()[0];
            _l.warn("Fail: no exception thrown at line: "+el.getLineNumber());
            _fail = true;
        } else {
            _l.debug("Good: "+ex);
        }
    }

    private void cmp(OS os, int[] e)
    {
        byte[] expected = new byte[e.length];
        for (int i=0; i<e.length; i++) expected[i] = (byte)e[i];

        byte[] real = os.toByteArray();

        String es = X.data2hex(expected);
        String rs = X.data2hex(real);
        _l.debug("Expect: "+es);
        _l.debug("Found:  "+rs);

        if (!es.equals(rs)) {
            try {
                throw new FAIL();
            } catch (FAIL ex) {
                StackTraceElement el = ex.getStackTrace()[1];
                _l.warn("Fail: expected != found at line: "+el.getLineNumber());
                _fail = true;
            }
        }
    }

    // ExceptionNotThrownException
    private class ENTE extends RuntimeException {
        private ENTE() {
            super("Exception not thrown");
        }
    }

    // ExceptionNotThrownException
    private class FAIL extends RuntimeException {
        private FAIL() {
            super("expected != found");
        }
    }

    // OutputStream
    private class OS extends WireOutputStream {
        private OS() {
        }
    }

    // InputStream
    private class PS extends WireInputStream {
        private final ByteArrayInputStream _bais;
        private PS(int[] is) {
            byte[] bs = new byte[is.length];
            for (int i=0; i<bs.length; i++) bs[i] = (byte)is[i];
            _bais = new ByteArrayInputStream(bs);
            _l.debug("Input:  "+X.data2hex(bs));
            setInputStream(_bais);
        }
    }

}
