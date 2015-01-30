/**
 * This is Ada's Poly implementation taken direct from the paper.
 * 
 * Note that it is only the Poly1305 implementation and using
 * Poly requires an encryption algorithm to ensure the full
 * security claims.  The paper couples/assumes AES but that
 * isn't replicated here, it's a task to be completed one day. 
 * 
 * @see Dan Bernstein, "The Poly1305-AES message-authentication code"
 * @author adalovelace
 */
package cryptix.alg.poly1305;