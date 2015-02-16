package cryptix.AdazPRNG;

import cryptix.Support;
import cryptix.X;
import cryptix.alg.chacha.ChaCha;

/**
 * TODO: This should be fixed up to export from ChaCha.
 * 
 * @author Ada
 */
public class StatefulChacha
    implements ExpansionFunctionInterface
{
	public final static int  SEEDLEN         = 32,
	                         BIG_SEEDLEN     = 64;
	public 	int              getSeedLen()    { return BIG_SEEDLEN; };
	private final int        rounds;         // slow is 10/chacha20, fast is 4/Chacha8
	private final int[][]    matrixxx        = new int[4][4];
	public void              close()         { reseed(matrixxx); }

	/** a standard fast PRNG (false) for most purposes  */
	public StatefulChacha()             { this(false); }
	/**
	 *  The PRNG.  If a marginally better result is wanted, call with slow=true
	 *  to get the increased rounds ChaCha20.  This effect will probably be
	 *  impossible to measure, but would perhaps be indicated for long-lived
	 *  public keys.
	 *  
	 *  @param slow will set the slower Chacha20 rounds cipher if true, else fast Chacha8
	 */
    public StatefulChacha(boolean slow) { rounds = slow ? 10 : 4; }
    
    /**
     *  Seed can be 32 or 64 bytes.  Latter uses only 48 bytes.
     *  Note that it is destructive over the seed, but caller still
     *  should destroy the contents somehow.
     */
	public void init(final byte[] seed) {
		if( seed.length != SEEDLEN && seed.length != BIG_SEEDLEN)
			throw new RuntimeException("the seed must be 32 or 64, not " + seed.length);
		initialize_matrix(matrixxx, seed);
		
//		if (seed.length == BIG_SEEDLEN) {
//		    // let's roll it through again, this time with the other 16 bytes
//
//	        increment(matrixxx);
//	        byte[] seed2      = new byte[BIG_SEEDLEN];   // XXX allocates an array
//	        crypt(matrixxx, seed2, seed2.length, rounds);
//	        for (int i = 0; i < BIG_SEEDLEN; i++) {
//	            seed2[i] ^= seed[ BIG_SEEDLEN - i ];     // roll in the other end
//	        }
//	        initialize_matrix(matrixxx, seed2);
//		}
	}

	/**
	 * This method will reset the key to an output from the 
	 * cipher to provide forward security.
	 */
	public void nextBytes(byte[] bytes){
		int len = bytes.length;
		crypt(matrixxx, bytes, len, rounds);
		reseed(matrixxx);
	}
	
	/**
	 * Gets a random block from the current matrix state, then
	 * puts that in as the seed, resetting the nonce
	 * and blockcounter.
	 * @param matrix
	 */
	private void reseed(int[][] matrix)
	{
		increment(matrix);   // redundant, crypt already did this?
		
		/*
		 * Do we even need to do this?  Why not just push the
		 * matrix through chachaRounds?  Also, we should be able
		 * to simplify chachaRounds to not do a copy / work in place,
		 * but at the sake of breaking compatibility.  What's the
		 * final add4x4 needed for anyway?
		 */
		byte[] seed      = new byte[BIG_SEEDLEN];     // TODO need to shrink this to not use a byte array
		crypt(matrix, seed, seed.length, rounds);
		initialize_matrix(matrix, seed);
	}
	

	static final int [] SIGMA_32   = { 0x61707865, 0x3320646e, 0x79622d32, 0x6b206574 };

	static private final int
	OUTPUT_BLOCK_SIZE = 64,   // the output block is 64 bytes long
	WORD              = 4,    // ChaCha defines the word to be 32 bits or 4 bytes
	BYTE_M            = 0x00FF;

	private static void crypt(int[][] matrix0, byte[] returnme, int xorlen, final int rounds)
	{
		int num = (xorlen+63)/OUTPUT_BLOCK_SIZE;
		for (int j = 0; j < num; j++ ) {
			int[][]matrix = chachaRounds(matrix0, rounds);
			
			
			// TODO in below, replace b[] by direct operations on returnme
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

			for (int i = start; (i < OUTPUT_BLOCK_SIZE + start) && (i < xorlen); i += 1) {
				returnme[i] = (byte)  b[i%OUTPUT_BLOCK_SIZE];
			}
			increment(matrix0);
		}
	}


	private static int[][] chachaRounds(int[][] matrix, int rounds)
	{
		int[][] matrix0 = copy(matrix);
		for(int i = 0; i < rounds; i++){
			onround(matrix0);
			offround(matrix0);
		}
		matrix0 = add4x4(matrix0, matrix);
		return matrix0;
	}

	private static void initialize_matrix(int[][] matrix, byte[] key)
	{
//		int[] keys      = new int[8];
//		for(int i = 0; i < SEEDLEN; i += WORD){        // 8 times, 8 key words
//			keys[i/WORD] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);	
//		}
		
		int i = 0;
        matrix[1][0] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[1][1] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[1][2] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[1][3] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[2][0] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[2][1] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[2][2] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
        matrix[2][3] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
        i += WORD;
		

        if (key.length == BIG_SEEDLEN)            // long key, stuff it in nonce & bc
        {
            i = SEEDLEN;
            matrix[3][0] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[3][1] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[3][2] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[3][3] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            /*
             *  This leaves another 4 words or 16 bytes un-used...
             *  Let's allocate over the SIGMA constants, which is a bit rude,
             *  but analysis of the algorithm says this isn't really a problem.
             */
            matrix[0][0] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[0][1] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[0][2] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
            matrix[0][3] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);
            i += WORD;
        }
        else
        {
            for(int j = 0; j < 4; j++){
                matrix[0][j] = SIGMA_32[j];
//                if (matrix[1][j] != keys[j]) throw new RuntimeException("1j " + j);
//                if (matrix[2][j] != keys[j + 4]) throw new RuntimeException("2j " + j);
                matrix[3][j] = 0;              // must zero last row for small SEED
            }
        }
	}

	//increments both nonce and blockcounter
	private static void increment(int[][] matrix){
		final int OVERFLOW = 0xffFFffFF;
		if(matrix[3][0] != OVERFLOW) {
			matrix[3][0] += 1;
		} else if (matrix[3][1] != OVERFLOW){
			matrix[3][0]  = 0;
			matrix[3][1] += 1;   // relies on Java overflow addition performing nicely
		}else if(matrix[3][2] != OVERFLOW) {
			matrix[3][0]  = 0;
			matrix[3][1]  = 0;   // relies on Java overflow addition performing nicely
			matrix[3][2] += 1;
		} else if(matrix[3][3] != OVERFLOW){
			matrix[3][0]  = 0;
			matrix[3][1]  = 0;   // relies on Java overflow addition performing nicely
			matrix[3][2]  = 0;
			matrix[3][3] += 1;
		} else{
			throw new RuntimeException("need a new key");
		}

		return;
	}


	private static void onround(int[][] matrix){
		quarterround(matrix, 0, 4,  8, 12);
		quarterround(matrix, 1, 5,  9, 13);
		quarterround(matrix, 2, 6, 10, 14);
		quarterround(matrix, 3, 7, 11, 15);
	}

	private static void offround(int[][] matrix){
		quarterround(matrix, 0, 5, 10, 15);
		quarterround(matrix, 1, 6, 11, 12);
		quarterround(matrix, 2, 7,  8, 13);
		quarterround(matrix, 3, 4,  9, 14);
	}

	private static void quarterround(int[][]matrix, int a, int b, int c, int d){
		matrix[a/4][a%4] += matrix[b/4][b%4]; matrix[d/4][d%4] ^= matrix[a/4][a%4]; matrix[d/4][d%4] = rotateleft(matrix[d/4][d%4], 16);
		matrix[c/4][c%4] += matrix[d/4][d%4]; matrix[b/4][b%4] ^= matrix[c/4][c%4]; matrix[b/4][b%4] = rotateleft(matrix[b/4][b%4], 12);
		matrix[a/4][a%4] += matrix[b/4][b%4]; matrix[d/4][d%4] ^= matrix[a/4][a%4]; matrix[d/4][d%4] = rotateleft(matrix[d/4][d%4],  8);
		matrix[c/4][c%4] += matrix[d/4][d%4]; matrix[b/4][b%4] ^= matrix[c/4][c%4]; matrix[b/4][b%4] = rotateleft(matrix[b/4][b%4],  7);
	}


	private static int rotateleft(int rotate, int how_much){
		how_much    = how_much % 32;
		int rotateL = rotate << how_much;
		rotate   >>>= (32 - how_much);
		return rotate | rotateL;
	}



	private static int[][] add4x4(int[][]matrix0, int[][]matrix1){
		if((matrix0.length != 4) ||( matrix1.length != 4))
			throw new RuntimeException("lengths are not equal to 4");

		int[] [] matrix2 = new int[4][4];

		for(int i = 0; i < 4; i++){
			if((matrix0[i].length != 4) ||( matrix1[i].length != 4))
				throw new RuntimeException("lengths are not equal to 4");

			for(int j = 0; j < 4; j++){
				matrix2[i][j] = matrix0[i][j] + matrix1[i][j];
			}

		}
		return matrix2;
	}


	/**
	 * The original algorithm was optimised for Intel's small-endian
	 * architecture.  As Java is big-endian (natural or network order)
	 * we need to convert it all in that way.
	 */
	public static int bytes2littleendian(byte b0, byte b1, byte b2, byte b3){
		int i = ((BYTE_M & b3) << 24) | ((BYTE_M & b2) << 16) | ((BYTE_M & b1) << 8) | (BYTE_M & b0);
		return i;
	}

	private static byte[] littleendian2bytes(int littleEndian){
		byte[] b  = new byte[4];
		for(int i = 0; i < 4; i++){
			b[i]  = (byte) (BYTE_M & littleEndian);
			littleEndian >>>= 8;
		}
		return b;
	}

	private static int[][] copy(int[][]matrix0){
		int[][] matrix1 = new int[4][4];
		for(int i = 0; i < 16; i++){
			matrix1[i/4][i%4] = matrix0[i/4][i%4];
		}
		return matrix1;
	}

	
	
	public static void main(String[]args){
		System.out.println(selfTest());
	}
	
	private static String selfTest(StatefulChacha f)
	{
	    final int rounds = f.rounds;
        for(int i = 0; i < 100; i++)
        {
            int    len      = Support.exampleInt(0, 1000);
            byte[] nostate  = new byte[len];    // must be zero, not encrypting
            byte[] stateful = new byte[len];
            
            byte[] seed     = Support.exampleData(SEEDLEN);
            byte[] bc       = new byte[8];
            byte[] nonce    = new byte[8];

            ChaCha.crypto_stream_xor(nostate, nostate, len, nonce, bc, seed, rounds);

            f.init(seed);
            f.nextBytes(stateful);

            if(!X.ctEquals(stateful, nostate))
                throw new RuntimeException("Round " + i + " not equal!!!!\n\t"
                          + X.data2hex(stateful) + "\n\t"
                          + X.data2hex(nostate)
                );
        }
        
        return "StatefulChacha " + (rounds*2) + " worked!!!";
    }
	
	public static String selfTest(){
		StatefulChacha sc = new StatefulChacha();
		String s = "";
		try {
		    s += selfTest(sc) + "   ";
		    sc.close();
		    sc = new StatefulChacha(true);
		    s += selfTest(sc);
		} finally {
		    sc.close();
		}
		return s;
	}
	
	
	

//	public BigInteger probablePrime(int bitLength) {
//	}
	public int randomInt(int i, int j) {
        throw new RuntimeException("randomInt(i,j)");       
	}
	public int randomInt() {
        throw new RuntimeException("randomInt()");
	}
	public long randomLong() {
        throw new RuntimeException("randomLong()");
	}
}
