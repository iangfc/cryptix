package cryptix.alg.poly1305;

import cryptix.X;

/*
 * <p>This is a test for Poly1305 (only, sans encryption).</p>
 * 
 * <p>Working, may need more refinement.  The test vectors
 * were taken from a variety of sources, as it was the
 * intention of the author to deliver it only integrated
 * with AES.  If you have any more raw Poly1305 test
 * vectors, please send them to me!  name below @ name dot org.</p>
 * 
 * @author adalovelace
 * @author iang
*/
public class Poly1305Test
{

    static class PolyTestVector
    {
        final String       title;
        final byte[]       ms;
        final byte[]       nonce;
        final byte[]       message;
        final byte[]       expectedMac;

        /**
         * 
         * @param title
         * @param macsecret r[0-15]
         * @param rawnonce is already encrypted
         * @param message
         * @param expectedMac
         */
        public PolyTestVector(String title, String macsecret, String rawnonce, String message, String expectedMac)
        {
            this.title = title;
            this.ms = X.hex2data(macsecret);

            if (Poly1305.isClamped(ms)) {
                //System.err.println("was Clamped!!!!!!!!!!!!!!!!");
            } else {
                //System.err.println("was NOT Clamped: " + title);
                Poly1305.clampMacSecret(ms);
                if (! Poly1305.isClamped(ms))
                    throw new RuntimeException(" Clamped failed !!!!!!!!!!!!!!!!");
            }
            this.nonce = X.hex2data(rawnonce);
            this.message = X.hex2data(message);
            this.expectedMac = X.hex2data(expectedMac);
        }
        
        public String toString()
        {
        	String s = "PolyTestVector:";
        	s += "\n  Title:      " + title;
        	s += "\n  Key:        " + X.data2hex(ms);
        	s += "\n  Message:    " + X.quick(message);
        	if (null != nonce)
            	s += "\n  Nonce:      " + X.data2hex(nonce);
        	s += "\n  ExpectedMAC:" + X.data2hex(expectedMac);
        	return s;
        }
    }

    static PolyTestVector[] TEST_VECTORS_RAW = {
            new PolyTestVector(
                    "Raw Poly1305 -- onetimeauth.c from nacl-20110221/test/onetimeauth.c",
        /*ms*/      "eea6a7251c1e72916d11c2cb214d3c25",     // mac secret
        /*ek(n)*/   "2539121d8e234e652d651fa4c8cff880",     // aesK(n) (encrypted nonce)
        /*message*/ "8e993b9f48681273c29650ba32fc76ce48332ea7164d96a4476fb8c531a1186a"
                        + "c0dfc17c98dce87b4da7f011ec48c97271d2c20f9b928fe2270d6fb863d51738"
                        + "b48eeee314a7cc8ab932164548e526ae90224368517acfeabd6bb3732bc0e9da"
                        + "99832b61ca01b6de56244a9e88d5f9b37973f622a43d14a6599b1f654cb45a74e355a5",
        /*mac*/     "f3ffc7703f9400e52a7dfb4b3d3305d9"
            ),

            new PolyTestVector(
                    "Raw Poly1305 -- poly1305-20050329-paper-B #1",
                    "851fc40c3467ac0be05cc20404f3f700",
                    "580b3b0f9447bb1e69d095b5928b6dbc",
                    "f3f6",
                    "f4c633c3044fc145f84f335cb81953de"
            ),

            new PolyTestVector(
                    "Raw Poly1305 -- poly1305-20050329-paper-B #2 empty message",
                    "a0f3080000f46400d0c7e9076c834403",
                    "dd3fab2251f11ac759f0887129cc2ee7",
                    "",
                    "dd3fab2251f11ac759f0887129cc2ee7"
            ),
            new PolyTestVector(
                    "Raw Poly1305 -- poly1305-20050329-paper-B #3",
                    "48443d0bb0d21109c89a100b5ce2c208",
                    "83149c69b561dd88298a1798b10716ef",
                    "663cea190ffb83d89593f3f476b6bc24d7e679107ea26adb8caf6652d0656136",
                    "0ee1c16bb73f0f4fd19881753c01cdbe"
            ),
            new PolyTestVector(
                    "Raw Poly1305 -- poly1305-20050329-paper-B #4",
                    "12976a08c4426d0ce8a82407c4f48207",
                    "80f8c20aa71202d1e29179cbcb555a57",
                    "ab0812724a7f1e342742cbed374d94d136c6b8795d45b3819830f2c04491faf0990c62e48b8018b2c3e4a0fa3134cb67fa83e158c994d961c4cb21095c1bf9",
                    "5154ad0d2cb26e01274fc51148491f1b"
            ),
    };
    
    public static void performTest(PolyTestVector[] tv)
        throws Exception
    {
        for (int i = 0; i < tv.length; i++)
        {
        	if (vv > 3) System.err.println("Test " + i + " vector " + tv[i]);
            runTestCase(tv[i]);
            if (vv > 3) System.err.println("");
        }
    }

    private static void runTestCase(PolyTestVector tc)
    {
        byte[] out = new byte[Poly1305.MAC_LENGTH];
        Poly1305.auth(out, 0, tc.message, 0, tc.message.length, tc.nonce, tc.ms);
        
        if (!X.ctEquals(out, tc.expectedMac)) {
            new RuntimeException("Mismatched output " + tc + "\nMAC calc:   " + X.data2hex(out) + "\n");
        }
    }
    
    public static String selfTest()
    {
        String s = "Poly1305 (raw)";
        PolyTestVector[] tv = TEST_VECTORS_RAW;

        for (int i = 0; i < tv.length; i++)
        {
            s += "\n      " + tv[i].title;
            runTestCase(tv[i]);
        }
        s += "\n Tests complete.";
        return s;
    }
    
    public static int vv = 2;
    
    public static void main(String[] args)
        throws Exception
    {
        performTest(TEST_VECTORS_RAW);
        
        String s = selfTest();
        System.out.println(s);
    }
}
