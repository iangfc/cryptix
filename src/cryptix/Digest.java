package cryptix;

/**
 * Interface that a message digest conforms to.
 *
 * @see org.bouncycastle.crypto.Digest;
 */
public interface Digest
{
    /**
     * return the algorithm name
     *
     * @return the algorithm name
     */
    public String getAlgorithmName();

    /**
     * @return the size, in bytes, of the digest produced by this message digest.
     */
    public int getDigestSize();

    /**
     * update the message digest with a single byte.
     *
     * @param in - is the input byte to be entered.
     */
    public void update(byte in);

    /**
     * update the message digest with a block of bytes.
     *
     * @param in - is the byte array containing the data.
     * @param inOff the offset into the byte array where the data starts.
     * @param len the length of the data.
     */
    public void update(byte[] in, int inOff, int len);

    /**
     * close the digest, producing the final digest value. The doFinal
     * call leaves the digest reset.
     *
     * @param out - the array the digest is to be copied into.
     * @param outOff the offset into the out array the digest is to start at.
     */
    public int doFinal(byte[] out, int outOff);

    /**
     * reset the digest back to it's initial state.
     */
    public void reset();
    
    
    
    /*
     * BouncyCastle had this in ExtendedDigest, but it was not clear
     * what it was for.
     * Return the size in bytes of the internal buffer the digest applies it's compression
     * function to.
     * 
     * @return byte length of the digests internal buffer.
     * 
     * public int getByteLength();
     */
}
