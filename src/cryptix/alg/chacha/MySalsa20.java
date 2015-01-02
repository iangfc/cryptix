package cryptix.alg.chacha;

import cryptix.X;
import webfunds.util.Example;
import webfunds.util.Panic;


/**
 * This is an implementation of Salsa20 taken directly from the DJB paper
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.64.8844&rep=rep1&type=pdf
 * 
 * </p><p>
 * Each round is: 16 add, 16 xor, 16 constant distance rotations (currently 1 transposition)
 * </p><p>
 * The limitations are thus:
 * key    => 2^64 streams
 * stream => 2^64 blocks
 * block == 2^9 bits, 64 bytes, 16 words
 * </p><p>
 * Given all possible nonces, each key expands to 2^64 streams, each
 * stream, given all possible block counters produces 2^64 64 byte 
 * blocks.
 * 
 * </p><p>
 * Each block is independent of the last, so you can randomly start your
 * stream with any given block counter and nounce.
 * 
 * </p><p>
 * In the C code it was the users responsibility to prevent the production
 * of more than 2^70 bytes/nonce (comment in Bernstien's code).  (Each
 * nonce generates 2^64 blocks which is 2^62 * 2^9 = 2^73 bits = 2^70 bytes.)
 * 
 * </p><p>
 * It is recommended to use full sizes / rounds:  key of 32b/256bits and 20 rounds.
 * Indeed, that is the only mode supported in this Salsa implementation!
 * </p><p>
 * It is also recommended to use ChaCha rather than Salsa as it has a few tiny
 * improvements.
 * </p>
 * 
 * <p>Todo</p>
 * <ul>
 * <li>XXX: Ian please check this implementation
 * <li>My code requires the user to update the nonce.
 * <li>Check: The user may begin crypting 
 * with any block number, an exception will be thrown if the block number exceeds
 * the max long length.
 * <li>Merge this more with ChaCha.
 * <li>Add support for rounds8/12 and keysize 128/16.
 * </ul>
 * 
 * @author adalovelace
 */
public class MySalsa20
    extends ChaCha // is the recommended one, therefore is the super class
{
	/*
	 * TAU, SIGMA and other constants are taken from ChaCha
	 */

	public static void crypto_stream_xor(byte[] returnme, byte[] xorme, int xorlen, byte[] iv, long blockcounter , byte[] key){
		int[] [] matrix     = new int[4][4];
		int[] keys          = new int[8];
		int[] block_counter = new int[2];
		int[] noncez        = new int[2];

		/*
		 * Notes:
		 * For all the silly people like me who get matrices confused,
		 * the matrix is laid out this way:
		 *         int [rows] [columns]
		 */
		for(int i = 0; i < 32; i += WORD){        // 8 times, 8 key words
			keys[i/WORD] = bytes2littleendian(key[i], key[i+1], key[i+2], key[i+3]);	
		}

		for(int i = 0; i < 8; i += WORD){	       // twice, 2 nonce words
			noncez[i/WORD] = bytes2littleendian(iv[i], iv[i+1], iv[i+2], iv[i+3]);
		}

		/*
		 * Little endian -- low order int comes first.
		 */
		block_counter[0] = (int) (INT_M & blockcounter);
		block_counter[1] = (int) (INT_M & (blockcounter >>> 32));

		initialize_matrix(matrix, keys, noncez, block_counter);

		crypt(matrix, returnme, xorme, xorlen );

	}

	
	
	private static void crypt(int[][] matrix0, byte[] returnme, byte[] xorme, int xorlen){
		int num = (xorlen+63)/OUTPUT_BLOCK_SIZE;
		for(int j = 0; j < num; j++ ){
			int[][]matrix = salsa20(matrix0);
			int start = j << 6;     // multiply by OUTPUT_BLOCK_SIZE
			byte[] b = new byte[OUTPUT_BLOCK_SIZE];
			int x = 0;
			for(int i = start; i< (OUTPUT_BLOCK_SIZE/WORD)+start; i++){
				byte[] temp = littleendian2bytes(matrix[(i/4)%4][i%4]);
				b[x++] = temp[0];
				b[x++] = temp[1];
				b[x++] = temp[2];
				b[x++] = temp[3];
			}

			for(int i = start; (i < OUTPUT_BLOCK_SIZE + start) && (i < xorlen); i += 1){
				returnme[i] =  (byte) (xorme[i] ^ b[i%OUTPUT_BLOCK_SIZE]);
			}
			increment_bc(matrix0);
		}
	}

	//does 20 rounds of salsa, returning the matrix to 
	//xor...
	private static int[][] salsa20(int[] [] matrix){
		int[][] matrix0 = round(matrix);
		for(int i = 0; i < 19; i++){
			matrix0 = round(matrix0);
		}
		matrix0 = add4x4(matrix0, matrix);
		return matrix0;
	}

	//XXX: ask if these things need to be switched for little/big endian stuff???
	private static void initialize_matrix(int[] [] matrix, int keys[], int[] nonce, int[] block_counter)
	{
		//		matrix[0] [0] = TAU[0];
		//		matrix[0] [1] = keys[0];
		//		matrix[0] [2] = keys[1];
		//		matrix[0] [3] = keys[2];
		//		matrix[1] [0] = keys[3];
		//		matrix[1] [1] = TAU [1];
		//		matrix[1] [2] = nounce[0];
		//		matrix[1] [3] = nounce[1];
		//		matrix[2] [0] = block_counter[0];
		//		matrix[2] [1] = block_counter[1];
		//		matrix[2] [2] = TAU[2];
		//		matrix[2] [3] = keys[4];
		//		matrix[3] [0] = keys[5];
		//		matrix[3] [1] = keys[6];
		//		matrix[3] [2] = keys[7];
		//		matrix[3] [3] = TAU[3];

		for(int i = 0; i < 4; i++){
			matrix[i][i] = SIGMA_32[i];
		}

		matrix[1] [2] = nonce[0];
		matrix[1] [3] = nonce[1];
		matrix[2] [0] = block_counter[0];
		matrix[2] [1] = block_counter[1];	

		matrix[0] [1] = keys[0];
		matrix[0] [2] = keys[1];
		matrix[0] [3] = keys[2];
		matrix[1] [0] = keys[3];

		matrix[2] [3] = keys[4];
		matrix[3] [0] = keys[5];
		matrix[3] [1] = keys[6];
		matrix[3] [2] = keys[7];
	}

	//this method increments nothing if all block positions are used.
	//XXX: ask if I should do the first one first? or the second??
	// XXX CHECK THE C CODE!!!!
	//should it return null? or go back to the beginning?
	//are we including negative numbers in this as well??
	private static void increment_bc(int[][] matrix){
		// XXX need to fix this to use UNsigned ints not SIGNed ints
		if(matrix[2][0] < Integer.MAX_VALUE)
			matrix[2][0] += 1;
		else if(matrix[2][1] < Integer.MAX_VALUE) {
			matrix[2][0]  = 0;
			matrix[2][1] += 1;
		}
		return;
	}

	/**
	 * Performs 1 round of Salsa.
	 * It is currently rolled up into methods for each
	 * step so as to assist with future ChaCha algorithm.
	 * Once we get this all working, it would be
	 * good to unroll all the little steps into one method.
	 */
	//method, but it is going to be easier to do
	//ChaCha if we leave it separated....
	private static int[][] round(int[][] matrix){
		int[][] matrix0 = step1(matrix);
		step2(matrix0);
		step3(matrix0);
		step4(matrix0);
		matrix0 = transpose(matrix0);
		return matrix0;
	}


	
	
	
	private static int[][] step1(int[] [] matrix){
		int[][] matrix0 = new int[4][4];
		for(int i = 0; i < 4; i++){
			matrix0[0][i] = matrix[0][i];
			matrix0[1][i] = matrix[1][i];
			matrix0[2][i] = matrix[2][i];
			matrix0[3][i] = matrix[3][i];
			int modify = i + 1;
			int input  = matrix[(modify + 3) % 4][i] + matrix[(modify + 2) % 4][i];
			input      = rotateleft(input, 7);
			matrix0[modify % 4][i] = matrix[modify % 4][i] ^ input;
		}
		return matrix0;
	}

	private static void step2(int[] [] matrix){
		for(int i =0; i < 4; i++){
			int modify = i + 2;
			int input  = matrix[(modify + 2) % 4][i] + matrix[(modify + 3) % 4][i];
			input      = rotateleft(input, 9);
			matrix[modify % 4][i] ^= input;
		}
	}

	private static void step3(int[] [] matrix){
		for(int i =0; i < 4; i++){
			int modify = i + 3;
			int input  = matrix[(modify + 2) % 4][i] + matrix[(modify + 3) % 4][i];
			input      = rotateleft(input, 13);
			matrix[modify % 4][i] ^= input;

		}
	}

	private static void step4(int[] [] matrix){
		for(int i =0; i < 4; i++){
			int modify = i;
			int input  = matrix[(modify + 2) % 4][i] + matrix[(modify + 3) % 4][i];
			input      = rotateleft(input, 18);
			matrix[modify % 4][i] ^= input;

		}
	}

	private static int[][] transpose(int[] [] matrix){
		int [] [] matrix1 = new int[4][4];
		for(int i =0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				matrix1[j][i] = matrix[i][j];
			}
		}
		return matrix1;
	}
	
	
	/**
	 * Test the algorithm with the test vectors found in
	 * the original paper.
	 * For a wider test, see the Salsa20Test which tries
	 * some of the ECRYPT test vectors.
	 * @return a diag string that says "all ok".
	 */

	public static String selfTest(){

        String s = "Salsa20: ";
		s += "  " +baseTest();
		
		int[][] matrix = testround1(testmatrix0);
		matrix = round(matrix);
		if(! eq4x4(matrix, testmatrixround2))
			throw new Panic(s+"\nround2 matrices are not equal\n" + matrix2string(matrix)+"\ntestmatrixround2\n"+matrix2string(testmatrixround2));
		s += "  Round2.";
		
		for(int i = 0; i <18; i++){
			matrix = round(matrix);
		}
		if(! eq4x4(matrix, testmatrixround20))
			throw new Panic(s+"\nround20 matrices are not equal\n" + matrix2string(matrix)+"\ntestmatrixround20\n"+matrix2string(testmatrixround20));
        s += "  Round20.";
        
		matrix = salsa20(testmatrix0);
		if(! eq4x4(matrix, finaltestmatrix20))
			throw new Panic(s+"\nfinalround20 matrices are not equal\n" + matrix2string(matrix)+"\ntestmatrixfinal20\n"+matrix2string(finaltestmatrix20));
        s += "  Final.";
        
		s += "  " + oroborousSalsa20();
		
		return s;
	}

	private static int[][] testround1(int[][] matrix){
		int[][] matrix0 = new int[4][4];
		matrix0 = step1(matrix);
		if(! eq4x4(matrix0, testmatrix1))
			throw new Panic("matrix != testmatrix1");

		step2(matrix0);
		if(! eq4x4(matrix0, testmatrix2))
			throw new Panic("matrix != testmatrix2");

		step3(matrix0);
		if(! eq4x4(matrix0, testmatrix3))
			throw new Panic("matrix != testmatrix3");

		step4(matrix0);
		if(! eq4x4(matrix0, testmatrix4))
			throw new Panic("matrix != testmatrix4\n" );

		matrix0 = transpose(matrix0);
		if(! eq4x4(matrix0, testmatrix5))
			throw new Panic("matrix != testmatrix5");
		return matrix0;
	}
	
	/**
	 * These are the test matrices taken from the original paper.
	 */
	private static final int[][] testmatrix0 = 
		{ 
		{ 0x61707865, 0x04030201, 0x08070605, 0x0c0b0a09 },
		{ 0x100f0e0d, 0x3320646e, 0x01040103, 0x06020905 },
		{ 0x00000007, 0x00000000, 0x79622d32, 0x14131211 },
		{ 0x18171615, 0x1c1b1a19, 0x201f1e1d, 0x6b206574 }
		};

	private static final int[][] testmatrix1 = 
		{ 
		{ 0x61707865, 0x04030201, 0x08070605, 0x95b0c8b6 },
		{ 0xd3c83331, 0x3320646e, 0x01040103, 0x06020905 },
		{ 0x00000007, 0x91b3379b, 0x79622d32, 0x14131211 },
		{ 0x18171615, 0x1c1b1a19, 0x130804a0, 0x6b206574 }
		};

	private static final int[][] testmatrix2 = 
		{ 
		{ 0x61707865, 0x04030201, 0xdc64a31d, 0x95b0c8b6 },
		{ 0xd3c83331, 0x3320646e, 0x01040103, 0xa45e5d04 },
		{ 0x71572c6d, 0x91b3379b, 0x79622d32, 0x14131211 },
		{ 0x18171615, 0xbb230990, 0x130804a0, 0x6b206574 }
		};

	private static final int[][] testmatrix3 = 
		{ 
		{ 0x61707865, 0xcc266b9b, 0xdc64a31d, 0x95b0c8b6 },
		{ 0xd3c83331, 0x3320646e, 0x95f3bcee, 0xa45e5d04 },
		{ 0x71572c6d, 0x91b3379b, 0x79622d32, 0xf0a45550 },
		{ 0xf3e4deb6, 0xbb230990, 0x130804a0, 0x6b206574 }
		};

	private static final int[][] testmatrix4 = 
		{ 
		{ 0x4dfdec95, 0xcc266b9b, 0xdc64a31d, 0x95b0c8b6 },
		{ 0xd3c83331, 0xe78e794b, 0x95f3bcee, 0xa45e5d04 },
		{ 0x71572c6d, 0x91b3379b, 0xf94fe453, 0xf0a45550 },
		{ 0xf3e4deb6, 0xbb230990, 0x130804a0, 0xa272317e }
		};

	private static final int[][] testmatrix5 = 
		{ 
		{ 0x4dfdec95, 0xd3c83331, 0x71572c6d, 0xf3e4deb6 },
		{ 0xcc266b9b, 0xe78e794b, 0x91b3379b, 0xbb230990 },
		{ 0xdc64a31d, 0x95f3bcee, 0xf94fe453, 0x130804a0 },
		{ 0x95b0c8b6, 0xa45e5d04, 0xf0a45550, 0xa272317e }
		};

	private static final int[][] testmatrixround2 = 
		{ 
		{ 0xba2409b1, 0x1b7cce6a, 0x29115dcf, 0x5037e027 },
		{ 0x37b75378, 0x348d94c8, 0x3ea582b3, 0xc3a9a148 },
		{ 0x825bfcb9, 0x226ae9eb, 0x63dd7748, 0x7129a215 },
		{ 0x4effd1ec, 0x5f25dc72, 0xa6c3d164, 0x152a26d8 }
		};

	private static final int[][] testmatrixround20 = 
		{ 
		{ 0x58318d3e, 0x0292df4f, 0xa28d8215, 0xa1aca723 },
		{ 0x697a34c7, 0xf2f00ba8, 0x63e9b0a1, 0x27250e3a },
		{ 0xb1c7f1f3, 0x62066edc, 0x66d3ccf1, 0xb0365cf3 },
		{ 0x091ad09e, 0x64f0c40f, 0xd60d95ea, 0x00be78c9 }
		};

	private static final int[][] finaltestmatrix20 = 
		{ 
		{ 0xb9a205a3, 0x0695e150, 0xaa94881a, 0xadb7b12c },
		{ 0x798942d4, 0x26107016, 0x64edb1a4, 0x2d27173f },
		{ 0xb1c7f1fa, 0x62066edc, 0xe035fa23, 0xc4496f04 },
		{ 0x2131e6b3, 0x810bde28, 0xf62cb407, 0x6bdede3d }
		};
	
	public static String oroborousSalsa20(){
		for(int i = 0; i <100; i++){
			byte[] key    = Example.data(32);
			byte[] nounce = Example.data(8);
		    int bc        = 0;
			byte[] pt     = Example.data(0, 1000);
			byte[] ct     = new byte[pt.length];
			byte[] pt2    = new byte[pt.length];
			MySalsa20.crypto_stream_xor(ct, pt, pt.length, nounce, bc, key);
			Salsa20.crypto_stream_xor(pt2, ct, ct.length, nounce, bc, key);
			//public static int crypto_stream_xor(byte[] c, byte[] m, int mlen, byte[] n, int noffset, byte[] key)
			if(!X.ctEquals(pt, pt2))
				throw new Panic("they're not equal!!!");
		}
		
		return "oroborous!";
	}
	



    public static void main(String[] args){
        System.out.println(selfTest());
    }
}



//private static int rotateleft(int rotate, int how_much){
//  how_much    = how_much % 32;
//  int rotateL = rotate << how_much;
//  rotate   >>>= (32 - how_much);
//  return rotate | rotateL;
//}
//
///*
//* Test code.
//*/
//private static boolean eq4x4(int[][]matrix0, int[][]matrix1){
//  if(matrix0.length != matrix1.length)
//      return false;
//  for(int i = 0; i < matrix0.length; i++){
//      if(matrix0[i].length != matrix1[i].length)
//          return false;
//
//      for(int j = 0; j < matrix0[i].length; j++){
//          if(matrix0[i][j] != matrix1[i][j])
//              return false;
//      }
//  }
//  return true;
//}
//
//private static int[][] add4x4(int[][]matrix0, int[][]matrix1){
//  if((matrix0.length != 4) || ( matrix1.length != 4))
//      throw new Panic("lengths are not equal to 4");
//
//  int[] [] matrix = new int[4][4];
//
//  for(int i = 0; i < 4; i++){
//      if((matrix0[i].length != 4) ||( matrix1[i].length != 4))
//          throw new Panic("lengths are not equal to 4");
//
//      for(int j = 0; j < 4; j++){
//          matrix[i][j] = matrix0[i][j] + matrix1[i][j];
//      }
//
//  }
//  return matrix;
//}
//
//private static String matrix2string(int[][] matrix){
//  String s = "";
//  for(int i = 0; i < matrix.length; i++){
//      for(int j = 0; j < matrix[i].length; j++){
//          s += " " + Integer.toHexString(matrix[i][j]);
//      }
//      s += "\n";
//  }
//  return s;
//}
//
///**
//* The original algorithm was optimised for Intel's small-endian
//* architecture.  As Java is big-endian (natural or network order)
//* we need to convert it all in that way.
//*/
//private static int bytes2littleendian(byte b0, byte b1, byte b2, byte b3){
//  int i = ((BYTE_M & b3) << 24) | ((BYTE_M & b2) << 16) | ((BYTE_M & b1) << 8) | (BYTE_M & b0);
//  return i;
//}
//
//private static byte[] littleendian2bytes(int littleEndian){
//  byte[] b  = new byte[4];
//  for(int i = 0; i < 4; i++){
//      b[i]  = (byte) (BYTE_M & littleEndian);
//      littleEndian >>>= 8;
//  }
//  return b;
//}