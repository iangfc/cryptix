package cryptix.alg.chacha;


/**
 *  XSalsa20 is a variant of Salsa20 with a longer nonce.
 *
 *
 * @see Salsa20
 * @see (PDF) D. J. Bernstein. "Extending the Salsa20 nonce." URL: http://cr.yp.to/papers.html#xsalsa.
 * @author Neil Alexander T.
 *
 */
public class Xsalsa20 {

	final int crypto_stream_xsalsa20_ref_KEYBYTES = 32;
	final int crypto_stream_xsalsa20_ref_NONCEBYTES = 24;
	
	public final static byte[] sigma = {(byte) 'e', (byte) 'x', (byte) 'p', (byte) 'a',
						  (byte) 'n', (byte) 'd', (byte) ' ', (byte) '3',
						  (byte) '2', (byte) '-', (byte) 'b', (byte) 'y',
						  (byte) 't', (byte) 'e', (byte) ' ', (byte) 'k'}; 
	
	public static int crypto_stream(byte[] c, int clen, byte[] n, byte[] k)
	{
		byte[] subkey = new byte[32];
		
		Hsalsa20.crypto_core(subkey, n, k, sigma);
		return Salsa20.crypto_stream(c, clen, n, 16, subkey);
	}
	
	public static int crypto_stream_xor(byte[] c, byte[] m, long mlen, byte[] n, byte[] k)
	{
		byte[] subkey = new byte[32];
		
		Hsalsa20.crypto_core(subkey, n, k, sigma);
		return Salsa20.crypto_stream_xor(c, m, (int) mlen, n, 16, subkey);
	}
}