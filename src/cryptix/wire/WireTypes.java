package cryptix.wire;

/**
 *  <p>
 *  Listing of each packet type number in the Wire regime.
 *  </p><p>
 *  Checklist for adding a new Wire type.
 *  </p>
 *  <ol><li>
 *      Select a number from the blocks below.
 *    </li><li>
 *      Write the handlers in WireInputStream/WireOutputStream.
 *    </li><li>
 *      Add the selftest in to test.WireTypesTest
 *    </li><li>
 *      Ensure the self test runs.
 *  </li></ul>
 *  <p>
 *  Blocks are allocated thusly:
 *  </p>
 *  <ul><li>
 *      <b>0 - 19</b>:  basic data types, allocated here.
 *    </li><li>
 *      <b>20 - 127</b>:  remaining 1 byte types, see Types.
 *    </li><li>
 *      <b>128 - 4096</b>:  Application, e.g., SOX, see Types
 *    </li><li>
 *  </li></ul>
 *  <p>
 *    Each block in the expanded space beyond the basic types
 *    should be allocated in some file such as Types.java.
 *  </p><p>
 *    Each Wire Type number is stored using a CompactInt, which uses
 *    7bit septet numbering, and a variable number of bytes to fit the number.
 *    If each high bit is set, then another byte is added (recursively).
 *  </p><p>
 *    Highly internal, highly managed, highly hard-coded, subject to change!
 *  </p><p>
 *    Note that the 'machine' types are being deprecated for lack of use and
 *    lack of portability (assumptions about machines): boolean, byte, int,
 *    long.
 *    In practice, 99% is done with compact ints and arrays.
 *  </p>
 *
 *  @see Types
 *  @see WireInputStream
 *  @see WireOutputStream
 */
public class WireTypes
{
    /**
     *  0-19  Primitive types.
     *  
     *  Some of these are no longer used (**)
     *  
     */
    public static final int
        NULL_REF               =  0,  // unallocated ref
        T_BOOLEAN              =  1,  // boolean T/F             (**)
        T_BYTE                 =  2,  // signed byte integer     (**)
        UNSIGNED_BYTE          =  3,  // unsigned ...            (**)
        T_INT                  =  4,  // signed 4 bytes          (**)
        T_LONG                 =  5,  // signed 8 byte long      (**)
        COMPACT_INT            =  6,  // 31 bits unsigned, from 1 to 5 bytes
        BYTE_ARRAY             =  7,  // CompInt then bytes
        T_STRING               =  8,  // CompInt then UTF-8 bytes
        BIG_INT                =  9,  // Signed, for crypto
        COMPACT_LONG           = 10,  // 63 bits unsigned, from 1 to 9 bytes

        LAST_PRIMITIVE         = 10,  // LOCAL CONSTANT, same as last above
        END_PRIMITIVES         = 19;  // MANIFEST CONSTANT, do not change
                                      // after this, SOX






    /*  **************************************************************
     *  Indexed by int constants, above, to return class name.
     */
    private static final String[] primitive_names = {
        /*   0 -> */ "null",        // reference/pointer is null
        /*   1 -> */ "bool (deprecated)",
        /*   2 -> */ "signed byte int (deprecated)",
        /*   3 -> */ "byte (deprecated)",
        /*   4 -> */ "signed 4byte int (deprecated)",
        /*   5 -> */ "signed 8byte long (deprecated)",
        /*   6 -> */ "compact int",
        /*   7 -> */ "byte array",
        /*   8 -> */ "UTF-8",
        /*   9 -> */ "BigInt",

        /*  10 -> */ "compact long",
        /*  11 -> */ "",
        /*  12 -> */ "",
        /*  13 -> */ "",
        /*  14 -> */ "",
        /*  15 -> */ "",
        /*  16 -> */ "",
        /*  17 -> */ "",
        /*  18 -> */ "",
        /*  19 -> */ "",
    };                           // length must be LAST_PRIMITIVE


    /**
     *  <p>
     *    A primitive is one that has no class.
     *  </p>
     *
     *  @param num is the type number of the wire type
     *  @return true if this is a primitive, not a class
     */
    public static final boolean isPrimitive(int num)
    {
        if (!((0 <= num) && (num <= LAST_PRIMITIVE)))
            return false;
        String name = primitive_names[num];
        if ((name == null) || (name.length() == 0))
            return false;
        return true;
    }

    /**
     *  <p>
     *    Something that can be printed out as a diag
     *  </p>
     *
     *  @param num is the type number of the class (0..)
     *  @return a string that is printable
     */
    public static String toDiag(int ord, int start, String[] names)
    {
        String name;
        int num = ord - start;

        if (num < 0)
            name = "<<NEGATIVE!>>";
        else if (num >= names.length)
            name = "<<OUT OF RANGE! " + names.length + ">>";
        else
        {
            name = names[num];
            if (name == null)
                name = "<null>";
            else if (name.length() == 0)
                name = "<empty>";
            else
                name = "'" + name + "'";
        }

        return "(" + ord + ") " + name;
    }

    /**
     *  <p>
     *    Something that can be printed out as a diag
     *    Intended to be overridden.
     *  </p>
     *
     *  @param num is the type number of the class (0..)
     *  @return a string that is printable
     */
    public static String toDiag(int num)
    {
        if (isPrimitive(num))
            return toDiag(num, 0, primitive_names);

        String name;
        if (num < 0)
            name = "<<NEGATIVE!>>";
        else
            return "<<OUT OF RANGE! " + primitive_names.length + ">>";

        return "(" + num + ") " + name;
    }

    static final String TAG = "WireTypes";

    /**
     *  @return a string summary of the tests run, if successful
     */
    public static String selfTest()
    {
        if (1+END_PRIMITIVES != primitive_names.length)
            throw new IllegalStateException("primitive_names[] must be set to END_PRIMITIVES");
        if (isPrimitive(-1))
            throw new IllegalStateException("-1 is primitive?");
        String s = TAG + " test";
        int i;
        for (i = 0; i <= LAST_PRIMITIVE; i++)
        {
            if (! isPrimitive(i))
                throw new IllegalStateException(s + "\nNot a Primitive: " + i);
            //Log.d(TAG, i + ": " + toDiag(i));
            s += " " + i;
        }
        for (; i <= END_PRIMITIVES; i++)
        {
            if (isPrimitive(i))
                throw new IllegalStateException(s + "\nIS A Primitive???: " + i);
        }
        if (isPrimitive(LAST_PRIMITIVE + 1))
            throw new IllegalStateException("-1 is primitive?");
        
        s += " All Good.";
        return s;
    }
}
