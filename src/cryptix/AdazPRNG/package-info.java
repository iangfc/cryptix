/**
 * This is the PRNG written by AdaLovelace in summer internship
 * of 2013.  It was absorbed into the webfunds code base some time
 * after that and has been working reasonably well.
 * 
 * The design follows that layed out in
 * http://iang.org/ssl/hard_truths_hard_random_numbers.html
 * which basically includes many collectors, and one mixer
 * feeding into an expansion function based on the Chacha
 * stream cipher (also written by Ada).
 * 
 * In this case the design is a "pull" design that does not
 * cache in the middle mixer.  When a PRNG is request from
 * the system, the expansion function (Chacha) is seeded from
 * the mixer, which in turn demands a fast contribution from
 * each of the collectors.  Each collector responds quickly,
 * so if caching is required, each has to do it by themselves.
 * 
 * User code should request a PRNG and then dispose of it in
 * the short term.  See ../PRNG
 * 
 * 
 * @author ada
 * @category free speach use-till-you-drop public domain
 */
package cryptix.AdazPRNG;