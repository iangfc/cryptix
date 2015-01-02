package cryptix.alg.chacha;;
 

import webfunds.sox.crypto.CryptorAbstract;
import cryptix.X;
import webfunds.util.Example;
import webfunds.util.Hex;
import webfunds.util.Panic;

/**
 * <p>
 * This is an implementation of ChaCha
 * {@link https://en.wikipedia.org/wiki/Salsa20#ChaCha_variant}
 * taken directly from the DJB paper:
 * Bernstein, D., "ChaCha, a variant of Salsa20.", Jan 2008,
 *            {@link http://cr.yp.to/chacha/chacha-20080128.pdf};
 * and an earlier implementation of Salsa20
 * {@link webfunds.sox.crypto.alg.chacha.MySalsa20}
 * {@link https://en.wikipedia.org/wiki/Salsa20}
 * </p><p>
 * 
 * <h2>Usage</h2>
 * <p><b>Keys.</b>
 * It is recommended that rounds be CHACHA20 and key be 32b/256bits.</p>
 * 
 * <p>If a smaller key size is wanted, it is better to expand it to full
 * 32b in the application, and ignore the option of the smaller key.
 * An easy way to do this is to simply hash the key through e.g. SHA256,
 * as this also copes with any keysize, and whitens it.  A more complicated
 * way may be to run it through a function to slow down password crunching.
 * Notwithstanding, ChaCha will accept a half-sized key of 16b/128bits
 * (which internally is doubled to 32, and constants switched).</p>
 * 
 * <p><b>Nonce/IV.</b>
 * The 8-byte IV or nonce provides an additional discriminator for the
 * key, so as to share a session key amongst many packets/streams.  It is
 * in effect just another 8 bytes of key material, contributing to the
 * same inner key matrix as the blockcounter and Key.</p>
 * 
 * <p><b>Blockcounter.</b>
 * The API works in blocks and not in bytes, with each block being
 * of OUTPUT_BLOCK_SIZE or 64 bytes in size.
 * The blockcounter indicates which of the blocks are currently in use,
 * and the normal calls will advance the blockcounter by 1 block (by
 * 64 bytes) at the end of each call to help process the stream.
 * This is regardless of whether the blockcounter is delivered as an
 * array or as a long;  Note that a long does not mean a byte quantity.</p>
 * 
 * <p>Each block is independent of the last, so you can re-start your
 * stream with any given block counter and nonce (IV).
 * <p>Note that conversion between blockcounter longs and arrays should
 * be done with the methods long2bc() and bc2long() below and not with
 * Java tools. Array blockcounters are held in the internal ChaCha format,
 * which is small-endian layout and not the expected Java/network layout.
 * The blockcounters are not byte counters in either long or array form.</p>
 * 
 * <p><h2>Comparison with salsa20:</h2>
 * <ul>
 * <li>operations within rounds are changed,
 * <but the type and nature of the operations is much the same.</li>
 * <li>Constants are the same.</li>
 * </ul>
 * 
 * <h2>Internal setup</h2>
 * <p>
 * The setup of the ChaCha internal 'key' matrix is:
 * </p><p>
 * <table cellgap="5" border="1" cellspacing="5">
 * <tr> <td>constant </td><td> constant </td><td> constant </td><td> constant </td>   </tr>
 * <tr> <td> key0    </td><td>  key1    </td><td>  key2    </td><td>  key3    </td>   </tr>
 * <tr> <td> key4    </td><td>  key5    </td><td>  key6    </td><td>  key7    </td>   </tr>
 * <tr> <td> bc0     </td><td>  bc1     </td><td> nonce0   </td><td> nonce1   </td>   </tr>
 * </table>
 * 
 * </p><p>
 * ChaCha operates with two rounds, which I will call on round and off round.
 * The on round operates on columns and the off round operates on
 * south east diagonals (on round performed first).
 * </p><p>
 * The numbers for this algorithm are pretty much the same as Salsa20:
 * </p><p>
 * In each round, these operations are performed:
 *    16 adds, 16 XORs, 16 constant distance rotations.
 * </p><p>
 * 
 * <h2>Limitations</h2>
 * <p>The limitations on this algorithm are thus:
 *   <li>key    => 2^64 streams
 *   <li>stream => 2^64 blocks
 *   <li>block  == 2^9  bits, 64 bytes, 16 words
 * </p>
 * <p>Given all possible nonces, each key expands to 2^64 streams.
 * Each stream, given all possible block counters produces 2^64 64 byte 
 * blocks.
 * </p><p>
 * In the C code, Bernstein makes the comment that
 * it was the caller's responsibility to prevent the production
 * of more than 2^70 bytes per nonce.
 * (Each nonce generates 2^64 blocks which is
 *  2^64 * 2^9 = 2^73 bits = 2^70 bytes.)
 * @see testOverflow
 * </p><p>
 * This Java implementation follows that convention and provides no checking
 * that the limitations have been exceeded.
 * </p>
 * 
 * <h2>Todo:</h2>
 * <ul>
 * <li>Finish merge/extend with Salsa.
 * <li>Look at other implementations to see if we are missing a trick.
 * <li>We should maybe unroll the double matrix into an array for speed.
 * <li>Write a cryptor...
 * </ul>
 * 
 * <h2>Interesting Info</h2>
 * <p>Paraphrasing Zooko:</p>
 * <ul>
 * <li> ChaCha20 is a variant of Salsa20, which was one of the winners of
 *      the eSTREAM competition:
 *      {@link http://www.ecrypt.eu.org/stream/index.html}</li>
 * <li> There is work to implement it in TLS (to replace RC4):
 *      {@link https://www.imperialviolet.org/2013/10/07/chacha20.html}</li>
 * <li> It's now included in OpenSSH:
 *      {@link http://blog.djm.net.au/2013/11/chacha20-and-poly1305-in-openssh.html}</li>
 * <li> It is the core of my favorite secure hash function, BLAKE2!
 *      {@link https://blake2.net/}</li>
 * <li> Oh yes, and ChaCha is much more efficient than RC4.
 *      {@link http://www.cryptopp.com/benchmarks.html}
 *      says that modified alleged RC4
 *      ("MARC4") takes about 14 cycles per byte and
 *      that Salsa20 takes about 4 cycles per byte.
 *      {@link http://bench.cr.yp.to/results-stream.html}
 *      says that ChaCha20 is usually around 15% more
 *      efficient than Salsa20 on modern Intel CPUs.</li>
 * </ul>
 * @author ada
 * @see ChaChaVectors.java, MySalsa.java
 */
public class ChaCha
    extends CryptorAbstract
    // MySalsa extends from this class (as ChaCha is the recommended one)
{

    /**
     * The argument to pass for the rounds.
     * Note that the actual constant is halved from the
     * number of rounds, because of the on/off construction
     * internally.
     */
    public static final int
        CHACHA20            = 10,
        CHACHA12            = 6,
        CHACHA8             = 4;
    
    /**
     * ChaCha and Salsa use the following sizes:
     * <ul>
     * <li>block size of 64 bytes.</li>
     * <li>(large) Key length of 32bytes/256bits</li>
     * <li>small key length of 16bytes/128bits</li>
     * <li>IV/nonce length of 8 bytes</li>
     * <li>Blockcounter length of 8 bytes (layed out in small-endian, not Java/network)</li>
     * </ul>
     */
    public static final int
        OUTPUT_BLOCK_SIZE   = 64,
        KEY_LENGTH          = 32,
        SMALL_KEY_LENGTH    = 16,
        IV_LENGTH           = 8,
        BLOCKCOUNTER_LENGTH = 8;
	
	/**
	 * <p>SIGMA_32 and TAU_16 are internal fixed constants used in
	 * initializing the first row (4 words) of the matrix.
	 * If a half-sized key of 16 bytes is used, then the
	 * key is doubled and TAU_16 is used instead of SIGMA_32.
	 * (This was defined formally for Salsa and copied for ChaCha.)</p>
	 * 
	 * <p>The constants derive from the following ASCII strings (from reference C):</p>
	 * <pre>
	 *    static const char sigma[16] = "expand 32-byte k";
	 *    static const char tau[16]   = "expand 16-byte k";
	 * </pre>
	 * 
	 * <p>Here, they are calculated and set in static variables,
	 * but the phrases are also here so they can be tested.</p>
	 * @see testGreekCalculation()
	 */
    static final String          // only used in testGreekCalculation()
            SIGMA_S  = "expand 32-byte k",
            TAU_S    = "expand 16-byte k";
	static final int []
            SIGMA_32 = { 0x61707865, 0x3320646e, 0x79622d32, 0x6b206574 },
			TAU_16   = { 0x61707865, 0x3120646e, 0x79622d36, 0x6b206574 };
	
	

	static final int
        WORD                = 4,    // internally, the word is 32 bits or 4 bytes
        COL                 = 4,
        ROW                 = 4,
        INT_M               = 0x00FFFFFFFF,  // used in Salsa
        BYTE_M              = 0x00FF;



//	public static void crypto_stream_xor(byte[] returnme, byte[] xorme, int xorlen, byte[] iv, long blockcounter , byte[] key){
//		byte[] bc = new byte[8];
//		for(int i = 0; i < 8; i++){
//			bc[i] = (byte) (blockcounter & 0x00ff);	
//			blockcounter >>>= 8;
//		}
//		crypto_stream_xor(returnme, xorme, xorlen, iv, bc, key);
//	}

	/**
	 * This class holds no state, so close() does nothing.
	 * Extending classes should hold the state.
	 */
	public void close() { }

	/**
	 * Convert a blockounter in long to a ChaCha/small-endian byte array blockcounter.
	 * The result is incompatible with big-endian/network/Java layout.
	 * @param blockcounter in long form
	 * @return blockcounter in array form
	 */
	public static byte[] long2bc(long blockcounter){
	    byte[] bc = new byte[BLOCKCOUNTER_LENGTH];
	    for(int i = 0; i < BLOCKCOUNTER_LENGTH; i++){
	        bc[i] = (byte) (blockcounter & 0x00ff); 
	        blockcounter >>>= 8;
	    }
	    return bc;
	}

    /**
     * Convert a ChaCha/small-endian blockcounter to a long blockcounter.
     * The argument returned is incompatible with bigendian/network/Java layout.
     * (Internally, ChaCha works with small-endian).
     * @param blockcounter
     * @return a long holding the current blockcounter
     */
    public static long bc2long(byte[] blockcounter){
        long bc = 0;
        for(int i = BLOCKCOUNTER_LENGTH-1; i >= 0; i--){
            bc |= blockcounter[i];
            bc <<= 8;
        }
        return bc;
    }

	/**
	 * Easy call.
	 * Sets up a blockcounter == 0 and calls the following method.
	 * Rounds is set to CHACHA20 (as recommended).
	 * 
	 * @param returnme is the output, has the input xor'd with the cipherstream
	 * @param xorme is the input, unchanged
	 * @param xorlen
	 * @param iv
	 * @param key is 16b/128bits or 32b/256bits (later is recommended)
	 */
	public static void crypto_stream_xor(byte[] returnme, byte[] xorme, int xorlen, byte[] iv, byte[] key){
		byte[] blockcounter = new byte[BLOCKCOUNTER_LENGTH];
		crypto_stream_xor(returnme, xorme, xorlen, iv, blockcounter, key, CHACHA20);
	}

    /**
     * Full call, adds the Rounds parameter.
     * 
     * @param returnme is the output, has the input xor'd with the cipherstream
     * @param xorme is the input, unchanged
     * @param xorlen
     * @param iv
     * @param blockcounter (in ChaCha small-endian layout)
     * @param key is 16b/128bits or 32b/256bits (recommended)
     * @param rounds is one of the CHACHA8, CHACHA12, CHACHA20 constants
     */
	public static void crypto_stream_xor(byte[] returnme, byte[] xorme, int xorlen, byte[] iv, byte[] blockcounter, byte[] key, final int rounds){

	    int[][] matrix;
		matrix = get_context(iv, blockcounter, key);

		crypt(matrix, returnme, xorme, xorlen, rounds);
	}
	
	/**
	 * WIP - a start at initialization context, an object is needed
	 * later for Cryptor.
	 * 
	 * <p>Warning.  The blockcounter is treated as if it is in strict
	 * ChaCha little-endian layout:  b0-b7 where b7 is the high byte,
	 * so 1 looks like this:</p>
	 * <pre>
	 *       01 00 00 00   00 00 00 00
	 * </pre>
	 * <p>which is little-endian and is not compatible with Java's
	 * byte-ordering for a long (or an int).
	 * This is only important if</p>
	 * <ol type="a">
	 *     <li>the blockcounter is restarted at some other position than zero,</li>
	 *     <li>the blockcounter is calculated using longs, and</li>
	 *     <li>the number needs to be compatible with another implementation.</li>
	 * </ol>
	 * 
	 * <p>Of course (!?) this potential confusion should not happen with the IV or
	 * key which are assumed to be byte arrays and are read in, as is.  And, arguably
	 * the above problem doesn't exist because the blockcounter is only by custom
	 * assumed to be numbers ... except it is decidedly a number inside the code.
	 * Test vectors will not pick up this problem because they all start from
	 * 0 (or -1) and they are expressed in byte[] form.</p>
	 * 
	 * @param iv/nonce is IV_LENGTH (8 bytes)
	 * @param blockcounter is BLOCKCOUNTER_LENGTH (8 bytes) in ChaCha little-endian order
	 * @param key is KEY_LENGTH (32bytes/256bits recommended) or SMALL_KEY_LENGTH (16bytes/128bits)
	 * @return the internal matrix (WIP)
	 */
    public static int[][] get_context(byte[] iv, byte[] blockcounter, byte[] key)
    {
        if (key.length != SMALL_KEY_LENGTH && key.length != KEY_LENGTH)
            throw new Panic("key length must be 16/128bits or 32/256bits");
        if (iv.length != IV_LENGTH)
            throw new Panic("iv/nonce length must be 16bytes");
        if (blockcounter.length != BLOCKCOUNTER_LENGTH)
            throw new Panic("iv/nonce length must be 16bytes");
        
        final int keyWordLen = key.length / 4;
        
        int[] [] matrix = new int[4][4];
        int[] keys      = new int[keyWordLen];
        int[] bc        = new int[2];
        int[] noncez    = new int[2];

        /*
         * Notes:
         * For all the silly people like me who get matrices confused,
         * the matrix is laid out this way:
         *         int [rows] [columns]
         */
        for (int i = 0; i < 32; i += WORD) {         // 8 times, 8 key words
            final int j = i % key.length;
            final int kw = (i/WORD) % keys.length;
            keys[kw] = bytes2littleendian(key[j], key[j+1], key[j+2], key[j+3]);    
        }

        for (int i = 0; i < 8; i += WORD) {          // twice, 2 nonce words
            noncez[i/WORD] = bytes2littleendian(iv[i], iv[i+1], iv[i+2], iv[i+3]);
        }

        for (int i = 0; i < 8; i += WORD) {          // twice, 2 blockcounters words
            bc[i/WORD] = bytes2littleendian(blockcounter[i], blockcounter[i+1], blockcounter[i+2], blockcounter[i+3]);
        }

        initialize_matrix(matrix, keys, noncez, bc);
        
        return matrix;
    }

    /**
     * Sets all elements to zero, and returns zero to assign to m.
     * @param m is a matrix returned by init_context()
     * @return always null
     */
    public static int[][] close_context(int[][] m)
    {
        for(int i = 0; i < 4; i++){
            m[0][i] =  m[1][i] = m[2][i] = m[3][i] = 0;
        }
        return null;
    }

    /**
     * Crypt the block pointed to by the blockcounter in matrix,
     * return the results into b (assigned not xor'd!)
     * and increment the blockcounter.
     * 
     * The API style is hard to get right.  This call allows repeated
     * moves through the stream.  It helps the caller to use
     * the same array b for each call,
     * so the previous array is overwritten (hygiene!) after each use
     * (caller will have to destroy last array).
     *  
     * @param matrix is the context of this encryption stream
     * @param b is the block that is set to the stream at blockcounter in matrix
     * @param rounds
     * @return
     */
    public static void block(int[][] matrix0, byte[] b, final int rounds)
    {
        int[][]matrix = chachaRounds(matrix0, rounds);
        int x = 0;
        for (int i = 0; i < OUTPUT_BLOCK_SIZE/WORD; i++) {
            byte[] temp = littleendian2bytes(matrix[(i/4)%4][i%4]);
            b[x++] = temp[0];
            b[x++] = temp[1];
            b[x++] = temp[2];
            b[x++] = temp[3];
        }

        increment_bc(matrix0);
    }
    
    
    
/////////////////////////////////////////////////////////////////
////
////   Internals

	/*
	 * Crypts the ciphertext/message
	 *   <li> 0 to xorlen in xorme, results in returnme again from 0 to xorme.
	 *   <li> only starts at an exact blockcount boundary, every 64 bytes.
	 * 
	 * @param matrix0
	 * @param returnme
	 * @param xorme
	 * @param xorlen
	 */
	private static void crypt(int[][] matrix0, byte[] returnme, byte[] xorme, final int xorlen, final int rounds)
	{
		int num = (xorlen+63)/OUTPUT_BLOCK_SIZE;
		for (int j = 0; j < num; j++) {
			//System.err.println("bc 12: "+ matrix0[3][0] + " 13: "+matrix0[3][1]);
			int[][]matrix = chachaRounds(matrix0, rounds);
			int start = j << 6;     // multiply by OUTPUT_BLOCK_SIZE
			byte[] b = new byte[OUTPUT_BLOCK_SIZE];
			int x = 0;
			for (int i = start; i < (OUTPUT_BLOCK_SIZE/WORD) + start; i++) {
				byte[] temp = littleendian2bytes(matrix[(i/4)%4][i%4]);
				b[x++] = temp[0];
				b[x++] = temp[1];
				b[x++] = temp[2];
				b[x++] = temp[3];
			}

			for (int i = start; (i < OUTPUT_BLOCK_SIZE + start) && (i < xorlen); i += 1){
				returnme[i] =  (byte) (xorme[i] ^ b[i%OUTPUT_BLOCK_SIZE]);
			}
			
			increment_bc(matrix0);
			
			/* stopping at 2^70 bytes per nonce is user's responsibility-- C code*/
		}
	}

	public static String toString4x4(int[][] matrix)
	{
		String s = "";
		for (int i =0; i < 16; i++) {
			s += Integer.toHexString(matrix[i/4][i%4]);
			if((i+1) % 4 == 0)
				s += "\n";
			else
				s += "\t";
		}
		return s;
	}

    public static String toString4(int[] row)
    {
        String s = "";
        for(int i = 0; i < 4; i++){
            s += Integer.toHexString(row[i%4]);
            s += "\t";
        }
        return s;
    }
    
	/*
	 * Does all rounds of ChaCha, returning the matrix to xor.
	 * @param rounds is actually half, as each loop is a double
	 */
	private static int[][] chachaRounds(int[][] matrix, final int rounds){
		int[][] matrix0 = copy(matrix);
		for(int i = 0; i < rounds; i++){
			onround(matrix0);
			offround(matrix0);
		}
		matrix0 = add4x4(matrix0, matrix);
		return matrix0;
	}

	private static void initialize_matrix(int[][] matrix, int keys[], int[] nonce, int[] block_counter)
	{
	    int[] greek = null;
	    int key_doubler = -256;
	    if (keys.length == 8) {          // 256 bits of key, full length
	        greek = SIGMA_32;
	        key_doubler = 4;             // so don't double
	    } else {
            greek = TAU_16;
            key_doubler = 0;             // double the key, only 128 bits
	    }
		for(int i = 0; i < 4; i++){
			matrix[0][i] = greek[i];     // TAU_16 or SIGMA_32
			matrix[1][i] = keys[i];
			matrix[2][i] = keys[i + key_doubler];
		}
		matrix[3] [0] = block_counter[0];
		matrix[3] [1] = block_counter[1];
		matrix[3] [2] = nonce[0];
		matrix[3] [3] = nonce[1];
	}
	

    /*
     * Note in DJB's code at this point:
     *     stopping at 2^70 bytes per nonce is user's responsibility
     * This is preserved here, seems reasonable.
     * (JIT will optimize out the check if set to false.)
     */
	private static final boolean CHECK_FOR_TOO_MUCH_DATA = false; 

	//Now we're 1/2 of the possibilities...
	private static void increment_bc(int[][] matrix){
		final int OVERFLOW = 0xffFFffFF;
		if(matrix[3][0] == OVERFLOW) {
		    if (CHECK_FOR_TOO_MUCH_DATA) {
		        if(matrix[3][1] == OVERFLOW){
		            throw new RuntimeException(DETECT_OVERFLOW);
		        }
		    }
			matrix[3][0]  = 0;
			matrix[3][1] += 1;   // relies on Java overflow addition performing nicely
		} else{
			matrix[3][0] += 1;
		}
		return;
	}

	
	/**
	 * ChaCha has on (working on columns) and off 
	 * (working on south east diagonals) rounds, on round
	 * is performed first.  It has 1 uniform 
	 * quarter round used by both on and off
	 * rounds, this quarter round differs from
	 * salsa20.  ChaCha does not transpose the
	 * matrix.
	 */

	private static void onround(int[][] matrix){
		quarterround(matrix, 0, 4,  8, 12);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 1, 5,  9, 13);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 2, 6, 10, 14);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 3, 7, 11, 15);		//System.err.println(toString4x4(matrix));
	}

	private static void offround(int[][] matrix){
		quarterround(matrix, 0, 5, 10, 15);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 1, 6, 11, 12);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 2, 7,  8, 13);		//System.err.println(toString4x4(matrix));
		quarterround(matrix, 3, 4,  9, 14);		//System.err.println(toString4x4(matrix));
	}

	private static void quarterround(int[][]matrix, int a, int b, int c, int d){
		matrix[a/4][a%4] += matrix[b/4][b%4]; matrix[d/4][d%4] ^= matrix[a/4][a%4]; matrix[d/4][d%4] = rotateleft(matrix[d/4][d%4], 16);
		matrix[c/4][c%4] += matrix[d/4][d%4]; matrix[b/4][b%4] ^= matrix[c/4][c%4]; matrix[b/4][b%4] = rotateleft(matrix[b/4][b%4], 12);
		matrix[a/4][a%4] += matrix[b/4][b%4]; matrix[d/4][d%4] ^= matrix[a/4][a%4]; matrix[d/4][d%4] = rotateleft(matrix[d/4][d%4],  8);
		matrix[c/4][c%4] += matrix[d/4][d%4]; matrix[b/4][b%4] ^= matrix[c/4][c%4]; matrix[b/4][b%4] = rotateleft(matrix[b/4][b%4],  7);
	}
	

	static int rotateleft(int rotate, int how_much){
		how_much    = how_much % 32;
		int rotateL = rotate << how_much;
		rotate   >>>= (32 - how_much);
		return rotate | rotateL;
	}

	static int[][] add4x4(int[][]matrix0, int[][]matrix1){
	    // should move this size checking outside inner loops.
		if((matrix0.length != 4) ||( matrix1.length != 4))
			throw new Panic("lengths are not equal to 4");

		int[] [] matrix = new int[4][4];

		for(int i = 0; i < 4; i++){
			if((matrix0[i].length != 4) ||( matrix1[i].length != 4))
				throw new Panic("lengths are not equal to 4");

			for(int j = 0; j < 4; j++){
				matrix[i][j] = matrix0[i][j] + matrix1[i][j];
			}

		}
		return matrix;
	}


	/**
	 * The original algorithm was optimized for Intel's small-endian
	 * architecture.  As Java is big-endian (natural or network order)
	 * we need to convert it all in that fashion.
	 */
	static int bytes2littleendian(byte b0, byte b1, byte b2, byte b3){
		int i = ((BYTE_M & b3) << 24) | ((BYTE_M & b2) << 16) | ((BYTE_M & b1) << 8) | (BYTE_M & b0);
		return i;
	}

	static byte[] littleendian2bytes(int littleEndian){
		byte[] b  = new byte[4];
		for(int i = 0; i < 4; i++){
			b[i]  = (byte) (BYTE_M & littleEndian);
			littleEndian >>>= 8;
		}
		return b;
	}

	private static int[][] copy(int[][]matrix){
		int[][] matrix0 = new int[4][4];
		for(int i = 0; i < 16; i++){
			matrix0[i/4][i%4] = matrix[i/4][i%4];
		}
		return matrix0;
	}
	
	
	
////////////////////////////////////////////////////////////////////
////
////    Test Code
////

	static String testVector(String[][] vectors)
    {
        int q = vectors.length;
        String s = "";
        for(int i = 0; i < q; i++){
            String[] vector = vectors[i];
            s += "   " + vector[0] + "\n";
            testVector(vector);
        }
        return s;
    }
	
	static int verbose = 2; 
	
    static void testVector(String[] v)
    {
        String name      = v[0];
        int rounds = CHACHA20;
        if (name.indexOf("(8/") >= 0)
            rounds = CHACHA8;
        if (name.indexOf("(12/") >= 0)
            rounds = CHACHA12;

        byte[] key       = Hex.hex2data(v[1]);
        byte[] nonce     = Hex.hex2data(v[2]);
        byte[] keystream = Hex.hex2data(v[3]);
        byte[] bc = (v.length > 4) ? Hex.hex2data(v[4]) : new byte[8];

        int    xorlen    = keystream.length;
        byte[] returnme  = new byte[xorlen];
        byte[] xorme     = new byte[xorlen];

        if (verbose > 3)
            System.err.println(diag(v));
        else if (verbose > 2)
            System.err.print("    " + name + ": ");

        crypto_stream_xor( returnme, xorme, xorlen, nonce, bc, key, rounds);

        String s = diag(name, xorlen, key, nonce, bc, keystream, returnme);
        if(!X.ctEquals(keystream, returnme)) {
            throw new Panic("\n\n@@@@@@@@@@@@@@@@ not matched!\n" + s + "\n\n");
            //System.err.println("\n\n@@@@@@@@@@@@@@@@ not matched!\n" + s + "\n\n");
        } else if (verbose > 2) {
            System.err.println("  Good.");
        }

        byte[] returnme2 = new byte[xorlen];
        crypto_stream_xor( returnme2, returnme, xorlen, nonce, bc, key, rounds);

        if(!X.ctEquals(returnme2, xorme))
            throw new Panic("returnme2 not equal to org!!!\n\t"+Hex.data2hex(returnme2)+"\n\t"+Hex.data2hex(xorme) +"\n\n"+s);
    }
    
    static String diag(String[] vector)
    {
        String s = "test vector " + vector[0] + " len " + (vector[3].length()/2) +
                "\n\tkey:    "+ vector[1]+
                "\n\tnonce:  "+ vector[2];
        s +=    "\n\tblockc: "+ ((vector.length > 4) ? vector[4] : "(zero)");
        s +=    "\n\tstream: "+ vector[3];
        return s;
    }
    
    static String diag(String name, int len, byte[] key, byte[] nonce, byte[] bc, byte[] stream, byte[] got)
    {
        String s = "test result " + name + " len " + len +
                "\n\tkey:    "+Hex.data2hex(key)+
                "\n\tnonce:  "+Hex.data2hex(nonce)+
                "\n\tbc:     "+Hex.data2hex(bc)+
                "\n\tstream: "+Hex.data2hex(stream)+
                "\n\tGOT:    "+Hex.data2hex(got);
        return s;
    }
    

    /** Test code used by Salsa. */
    static boolean eq4x4(int[][]matrix0, int[][]matrix1){
        if(matrix0.length != matrix1.length)
            return false;
        for(int i = 0; i < matrix0.length; i++){
            if(matrix0[i].length != matrix1[i].length)
                return false;

            for(int j = 0; j < matrix0[i].length; j++){
                if(matrix0[i][j] != matrix1[i][j])
                    return false;
            }
        }
        return true;
    }

    /** Test code used by Salsa. */
    static String matrix2string(int[][] matrix){
        String s = "";
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
                s += " " + Integer.toHexString(matrix[i][j]);
            }
            s += "\n";
        }
        return s;
    }

//	/**
//	 * A test vector created from the C reference implementation.
//	 * Now in the ChaChaVectors list.
//	 */
//	final static byte FF = (byte) 0xff;
//	final static byte[] C_TEST_BLOCKCOUNTER_BYTES = { FF, FF, FF, FF, FF, FF, FF, FF, };
//	
//	final static String
//	    C_TEST_KEY    = "0000000000000000000000000000000000000000000000000000000000000000",
//	    C_TEST_NONCE  = "0000000000000000",
//	    C_TEST_BLOCKCOUNTER  = "ffffffffffffffff",
//	    C_TEST_STREAM = "df8a0bceca2fbb111f29978fa9a64407c68ced975ff55a24707265d0687e6aefb3597eac46e4b0a79b3a49c8b0f1775ac3edd7eb6a6d9bcb38f276517a79e0143e00ef2f895f40d67f5bb8e81f09a5a12c840ec3ce9a7f3b181be188ef711a1e984ce172b9216f419f445367456d5619314a42a3da86b001387bfdb80e0cfe42d2aefa0deaa5c151bf0adb6c01f2a5adc0fd581259f9a2aadcf20f8fd566a26b5032ec38bbc5da98ee0c6f568b872a65a08abf251deb21bb4b56e5d8821e68aa7fe7b1ff12cffd9d7e21f517501ecaff43cea3e8e3eb28cbd8d1001f68b5c68755b970d3b7dafc64d3e59bdeaadc8f82a975a481df31b52870aa5fa2ba340af92ba037cdb63cb5a7277dc5d6dc549e4e28a15c70670f0e97787c170485829264ecbf14bddeb68410f423e8849e0ce35c10d20a802bbc3d9a6ca01c386279bf01e8f75f478cb0d159db767341602fa02d3e01c3d9aacf9b686eccf1bb5ff4c8fd21473e89d50f51f9a1ced2390c72ee7e37f15728e61d1fb2c8c839495e4890528c146d00fe2e1caec31b159fc42dcd7e06865c6fa5267c6ca9c5284e651e175a362f469b6e722347de959f76533315542ffa440d37cde8862da3b3331e53b60d73baeb620e63a2e646ea148974350aa337491e5f5fc087cb429173d1eeb74f5a73acc6c3d72b59b8bf5ab58cdcf76aa001689aac938a75b1bb25d77b5382898c4e73ba04bae3a083c8a2109f15b8c4680ae4ba1c70df5b513425349a77e95d3b565825a0227d45068e61eb90aa1a4dc414c0976911a52d46b39f40c5849e5abec705f33056187a8549ab397d5aeba1bd59f50c2b8e6ace6be29f79c708f3fe1afb144320dd2ec5602fa4883d78a7fccc6c3cff282ac9312f44374e8b7d294126e345b7f2c017b16bb335c696bc0cc302f3db897fa25365a2ead1f149d87a97e8b847203f3046950cfaeedcef2a97681f3fd178e8df82318134b4eb1bd3a9fccca3fb84fbdb6a443f7bc758e57d4721e660eeddc75332d53712f238a5c2cd89d80929d71364ec0f99f70e90b8bdc400b61ff7619a9bc20ccb5c17c399fe568512c4a75bc98c6cd5ab1f3f90f1fb8c2b1c08e2d75fb95eef806d597369a4799eef36fdf5b068bbaa21f30038579b983b9eb5157a1fc82cea868763199da242b3567df467d0534b07072526bb31d92ba0b2e18f5366c3267e2bbd7d25b00d4c2506f6c3ffed79f58ecea88080c56070be4a3e5bafdd94f2e39ef521ae70e94a099d99389f948c15f0e9ae1888fa9a18c158760583a4a58ffb4b535d7c7d9aa7c5aa113df76516a008a971400276a8cb06d40c21cf4c8f91260ca89b89e8620ec1469eee5451b9586dfc3072aa11e389cac7b9f383dcc7cef9ab81edd9af7c15b8eae6283dfd2b72c68e";
//    
//	final static String[] vectorCCode = {
//	        "C reference implementation vector (only 1)",
//	        C_TEST_KEY,
//	        C_TEST_NONCE,
//	        C_TEST_STREAM,
//	        C_TEST_BLOCKCOUNTER,     // optional
//	};
	

	/** @see comment at top for these numbers */
	final static String DETECT_OVERFLOW =
			"gwjf2574 You are trying to produce more than 2^70 bytes/nonce, please break up the encryption with a different key/nonce";

	static String testOverflow(){
		int big = 0, little = 0;
		final int OVERFLOW = 0xffFFffFF;
		long len = (1 << 62);
		for(long i = 0; i < len; i++ ){
			if(little == OVERFLOW) {
				if(big == OVERFLOW) {
					throw new RuntimeException(DETECT_OVERFLOW);
				}
				little  = 0;
				big    += 1;   // relies on Java overflow addition performing nicely
			} else{
				little += 1;
			}
		}
		return "Overflow";
	}
	
    static String testEndian(){
        for(int i = 0; i <= 1000; i++){
            int me0 = Example.exampleInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            byte[] b =  littleendian2bytes(me0);
            int me = bytes2littleendian(b[0], b[1], b[2], b[3]);
            if(me != me0)
                throw new Panic("from endian: " + Integer.toHexString(me)+ " bits: "+ Hex.data2hex(b));
//          else
//              System.err.println("from endian: " + Integer.toHexString(me)+ " bits: "+ Hex.data2hex(b));
        }
        for(int i = 0; i <= 1000; i++){
            byte[] b  =  Example.data(4);
            int me    = bytes2littleendian(b[0], b[1], b[2], b[3]);
            byte[] b2 = littleendian2bytes(me);
            if(! X.ctEquals(b, b2))
                throw new Panic("to endian: " + Integer.toHexString(me)+ " bits: "+ Hex.data2hex(b));
//          else
//              System.err.println("to endian: " + Integer.toHexString(me)+ " bits: "+ Hex.data2hex(b));
        }
        return "Endian";
    }

    static String testGreekCalculation(String phrase, int[] greekStatic)
    {
        byte[] b = phrase.getBytes();

        int[] greek = new int[4];
        for(int i = 0; i < 4*4; i += WORD) {
            greek[i/WORD] = bytes2littleendian(b[i], b[i+1], b[i+2], b[i+3]);
        }
        boolean good = true;
        for(int i = 0; i < 4; i += 1){          // twice, 2 nonce words
            if (greek[i] != greekStatic[i])
                good = false;
        }
        if (!good)
            throw new Panic("greeks not equal..." +
                "\n    String:     " + phrase +
                "\n    calculated: " + toString4(greek) +
                "\n    static set: " + toString4(greekStatic));
        return "Greek";
    }

    public static String baseTest(){
        String s = "";
        
        s += testEndian() + ". ";
        s += testGreekCalculation(SIGMA_S, SIGMA_32) + " Sigma. ";
        s += testGreekCalculation(TAU_S,   TAU_16) + " Tau. ";
        
        s += testOverflow() + ". ";
        return s;
    }

	public static String selfTest(){
		String s = "ChaCha: ";
		
		s += baseTest();
        s += "   Test vectors:\n";
		
        s += testVector(ChaChaVectors.CReferenceVector8round);
        s += testVector(ChaChaVectors.Strombergson256key);
        s += testVector(ChaChaVectors.Langley20rounds256key);
        
        s += "ChaCha good.";
//		s += testOverflow() + "\n";
		return s;
	}
	public static void main(String[] args){
		System.out.println( selfTest() );
	}
}

//private static void quarterround(int[][]matrix, int a, int b, int c, int d){
//  long A = ((long)matrix[a/4][a%4]) & 0xffFFffFF;
//  A     += matrix[b/4][b%4];
//  matrix[a/4][a%4]  = (int) A;
//  matrix[d/4][d%4] ^= matrix[a/4][a%4];
//  matrix[d/4][d%4]  = rotateleft(matrix[d/4][d%4], 16);
//  
//  long C = ((long)matrix[c/4][c%4]) & 0xffFFffFF;
//  C     += matrix[d/4][d%4]; 
//  matrix[c/4][c%4]  = (int) C;
//  matrix[b/4][b%4] ^= matrix[c/4][c%4];
//  matrix[b/4][b%4]  = rotateleft(matrix[b/4][b%4], 12);
//  
//  A  = ((long)matrix[a/4][a%4]) & 0xffFFffFF;
//  A += matrix[b/4][b%4];
//  matrix[a/4][a%4]  = (int) A;
//  matrix[d/4][d%4] ^= matrix[a/4][a%4]; 
//  matrix[d/4][d%4]  = rotateleft(matrix[d/4][d%4],  8);
//  
//  C  = ((long)matrix[c/4][c%4]) & 0xffFFffFF;
//  C += matrix[d/4][d%4]; 
//  matrix[c/4][c%4]  = (int) C;
//  matrix[b/4][b%4] ^= matrix[c/4][c%4]; 
//  matrix[b/4][b%4] = rotateleft(matrix[b/4][b%4],  7);
//}