package cryptix.AdazPRNG;

import java.io.Closeable;

import cryptix.PsuedoRandomNumberGenerator;

public interface ExpansionFunctionInterface
    extends PsuedoRandomNumberGenerator, Closeable
{
	public int  getSeedLen();
	public void init(byte[] seed);
	public void nextBytes(byte[] bytes);
	
	public void close();
}
