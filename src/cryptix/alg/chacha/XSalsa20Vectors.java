package cryptix.alg.chacha;



/**
 * Vectors for: XSalsa20
 * AlgorithmType: SymmetricCipher
 * Source: created by Wei Dai using naclcrypto-20090308
 * These numbers are taken from Crypto++.
 * 
 * 
 * We really need a way to read them in properly, but the format
 * looks clunky and annoying to parse.
 * 
 * @see http://www.cryptopp.com/
 * @author ada
 */

/**
 * AlgorithmType: SymmetricCipher
 * Name: XSalsa20
 * Source: created by Wei Dai using naclcrypto-20090308
 */
public class XSalsa20Vectors {

	    static int verbose = 2;
	    
	    /**
	     * 256 bit key set of vectors.
	     * Plaintext is always zeros.
	     */
		public static final String[][] vectors256 =
		{
			{
			                  "Set 1, vector#  0",
	              /* key = */ "1b27556473e985d462cd51197a9a46c7"+
			                  "6009549eac6474f206c4ee0844f68389",
			      /* iv */    "69696ee955b62b73cd62bda875fc73d6"+
			                  "8219e0036b7a0b37",
			  /* plaintext */ "139",
			 /* ciphertext */ "eea6a7251c1e72916d11c2cb214d3c252539121d8e234e652d651fa4c8cff880"+
			                  "309e645a74e9e0a60d8243acd9177ab51a1beb8d5a2f5d700c093c5e55855796"+
			                  "25337bd3ab619d615760d8c5b224a85b1d0efe0eb8a7ee163abb0376529fcc09"+
			                  "bab506c618e13ce777d82c3ae9d1a6f972d4160287cbfe60bf2130fc0a6ff604"+
			                  "9d0a5c8a82f429231f0080",
			  
	        },
	        {
	            			"Set 2, vector#  0:",
	            /* key = */ "a6a7251c1e72916d11c2cb214d3c252539121d8e234e652d651fa4c8cff88030",
	            /* iv */ 	"9e645a74e9e0a60d8243acd9177ab51a1beb8d5a2f5d700c",
	        /* plaintext */ "093c5e5585579625337bd3ab619d615760d8c5b224a85b1d0efe0eb8a7ee163abb0"+
	        				"376529fcc09bab506c618e13ce777d82c3ae9d1a6f972d4160287cbfe60bf2130fc"+
	        				"0a6ff6049d0a5c8a82f429231f008082e845d7e189d37f9ed2b464e6b919e6523a8"+
	        				"c1210bd52a02a4c3fe406d3085f5068d1909eeeca6369abc981a42e87fe665583f0"+
	        				"ab85ae71f6f84f528e6b397af86f6917d9754b7320dbdc2fea81496f2732f532ac7"+
	        				"8c4e9c6cfb18f8e9bdf74622eb126141416776971a84f94d156beaf67aecbf2ad41"+
	        				"2e76e66e8fad7633f5b6d7f3d64b5c6c69ce29003c6024465ae3b89be78e915d88b"+
	        				"4b5621d",
	       /* ciphertext */ "b2af688e7d8fc4b508c05cc39dd583d6714322c64d7f3e63147aede2d9534934b0"+
	        				"4ff6f337b031815cd094bdbc6d7a92077dce709412286822ef0737ee47f6b7ffa2"+
	        				"2f9d53f11dd2b0a3bb9fc01d9a88f9d53c26e9365c2c3c063bc4840bfc812e4b80"+
	        				"463e69d179530b25c158f543191cff993106511aa036043bbc75866ab7e34afc57"+
	        				"e2cce4934a5faae6eabe4f221770183dd060467827c27a354159a081275a291f69"+
	        				"d946d6fe28ed0b9ce08206cf484925a51b9498dbde178ddd3ae91a8581b91682d8"+
	        				"60f840782f6eea49dbb9bd721501d2c67122dea3b7283848c5f13e0c0de876bd22"+
	        				"7a856e4de593a3"

	        },
	        {
	        				"Set 3, vector#  0  (Test: Encrypt)",
	         /* key = */    "a6a7251c1e72916d11c2cb214d3c252539121d8e234e652d651fa4c8cff88030",//copied from above
	         /* iv */		"b2af688e7d8fc4b508c05cc39dd583d671"+
	        				"4322c64d7f3e63",
	       	/* plaintext */ "093c5e5585579625337bd3ab619d615760d8c5b224a85b1d0efe0eb8a7ee163abb0"+//copied from above
	        				"376529fcc09bab506c618e13ce777d82c3ae9d1a6f972d4160287cbfe60bf2130fc"+
	        				"0a6ff6049d0a5c8a82f429231f008082e845d7e189d37f9ed2b464e6b919e6523a8"+
	        				"c1210bd52a02a4c3fe406d3085f5068d1909eeeca6369abc981a42e87fe665583f0"+
	     					"ab85ae71f6f84f528e6b397af86f6917d9754b7320dbdc2fea81496f2732f532ac7"+
	     					"8c4e9c6cfb18f8e9bdf74622eb126141416776971a84f94d156beaf67aecbf2ad41"+
	        				"2e76e66e8fad7633f5b6d7f3d64b5c6c69ce29003c6024465ae3b89be78e915d88b"+
	        			    "4b5621d",
	      /* ciphertext */  "418078fe843f5984dd3c7975d1ff51af4dceda640999aaa3c28618ae286ca15051cb"+
	    		            "4d55f9da22a213ef14a2b905b52c99a557854c7f2a6d6ed6f69c1c6649f3fb67b862"+
	    		            "8468029b3367920c2e1148aa1f3b9c695cb1426f09ce84045842946e0454e41ab1ed"+
	    		            "b32cae4b95669de4e2ccaf00ba86ffeae6a9c5fce4153baddb0d8998a600537a9649"+
	    		            "939cb7d7a9c4e8cbca0fab77963abd516699879de0b1971dc7328668111ff5b77c25"+
	    		            "3b9e6346d1a2ce6e390cd736156ad7f44b339cfb141f00e7a766c06e130b0c31d889"+
	    		            "80d2ad8814a2d641599162ab8af25d93067f06a49637eaf6523806b8fa07d56628bb",

	        },
	        {
	        				"Set 4, vector#  0  (Test: Resync)",
	         /* key = */    "9e1da239d155f52ad37f75c7368a536668b051952923ad44f57e75ab588e475a",//copied from above
	         /* iv */		"af06f17859dffa799891c4288f6635b5c5a45eee9017fd72",
	         /* plaintext */ "feac9d54fc8c115ae247d9a7e919dd76cfcbc72d32cae4944860817cbdfb8c0"+
	        		 		 "4e6b1df76a16517cd33ccf1acda9206389e9e318f5966c093cfb3ec2d9ee2de"+
	        		 		 "856437ed581f552f26ac2907609df8c613b9e33d44bfc21ff79153e9ef81a9d"+
	        		 		 "66cc317857f752cc175fd8891fefebb7d041e6517c3162d197e2112837d3bc4"+
	        		 		 "104312ad35b75ea686e7c70d4ec04746b52ff09c421451459fb59f",
	       /* ciphertext */  "2c261a2f4e61a62e1b27689916bf03453fcbc97bb2af6f329391ef063b5a219"+
	        		 		 "bf984d07d70f602d85f6db61474e9d9f5a2deecb4fcd90184d16f3b5b5e168e"+
	        		 		 "e03ea8c93f3933a22bc3d1a5ae8c2d8b02757c87c073409052a2a8a41e7f487"+
	        		 		 "e041f9a49a0997b540e18621cad3a24f0a56d9b19227929057ab3ba950f6274"+
	        		 		 "b121f193e32e06e5388781a1cb57317c0ba6305e910961d01002f0",

	        },
	        {
							   "Set 5, vector#  0  (Test: Encrypt)",
				/* key = */    "9e1da239d155f52ad37f75c7368a536668b051952923ad44f57e75ab588e475a",//copied from above
				/* iv */	   "2c261a2f4e61a62e1b27689916bf03453fcbc97bb2af6f32",
			   /* plaintext */ "feac9d54fc8c115ae247d9a7e919dd76cfcbc72d32cae4944860817cbdfb8c0"+//copied from above
      		 		 		   "4e6b1df76a16517cd33ccf1acda9206389e9e318f5966c093cfb3ec2d9ee2de"+
      		 		 		   "856437ed581f552f26ac2907609df8c613b9e33d44bfc21ff79153e9ef81a9d"+
      		 		 		   "66cc317857f752cc175fd8891fefebb7d041e6517c3162d197e2112837d3bc4"+
      		 		 		   "104312ad35b75ea686e7c70d4ec04746b52ff09c421451459fb59f",
		     /* ciphertext */  "7030af4a9db8a6b95f55f962efefcc60d8ceb0d5d920e808cebd8cf6f31542d2"+
      		 		 		   "27a67c9db8888cfcb9410ae357f8a3da06a608a93b7fd5513978c6b8b837f6ec"+
      		 		 		   "aafd3366495cdd3ab719d9d4c2ac74d6ea2eb117f30369ea62727fa6dc7982f6"+
      		 		 		   "68fa3bf44c9da8e70ff8c18b07d63aa01afe1311bdafc457d06c69aaea0dfbb1"+
      		 		 		   "fc89d1574ad1e7be8b459d4cf36bdd88db0363219652089c50",
	        },
	        {
	        				   "Set 6, vector#  0  (Test: Resync)",
	        	
	        	/* key = */    "d5c7f6797b7e7e9c1d7fd2610b2abf2bc5a7885fb3ff78092fb3abe8986d35e2",//copied from above
				/* iv */	   "744e17312b27969d826444640e9c4a378ae334f185369c95",
			   /* plaintext */ "7758298c628eb3a4b6963c5445ef66971222be5d1a4ad839715d1188071739b7"+
					   		   "7cc6e05d5410f963a64167629757",
		     /* ciphertext */  "27b8cfe81416a76301fd1eec6a4d99675069b2da2776c360db1bdfea7c0aa613"+
					   		   "913e10f7a60fec04d11e65f2d64e",
	        },
	        {
				   			   "Set 7, vector#  0  (Test: Encrypt)",
	
			    /* key = */    "d5c7f6797b7e7e9c1d7fd2610b2abf2bc5a7885fb3ff78092fb3abe8986d35e2",//copied from above
	            /* iv */	   "27b8cfe81416a76301fd1eec6a4d99675069b2da2776c360",
               /* plaintext */ "7758298c628eb3a4b6963c5445ef66971222be5d1a4ad839715d1188071739b7"+
            		   		   "7cc6e05d5410f963a64167629757",
             /* ciphertext */  "ed158a1dd07f4316d403af3e6977afaac8205d678b38fa5928c61e366ff27003"+
            		   		   "143d5d20482a2ea76a50756225a4",
	        },
	        {		   		   		   		   
	        				   "Set 8, vector#  0  (Test: Resync)",
	        		        	
	        	/* key = */    "737d7811ce96472efed12258b78122f11deaec8759ccbd71eac6bbefa627785c",//copied from above
				/* iv */	   "6fb2ee3dda6dbd12f1274f126701ec75c35c86607adb3edd",
			   /* plaintext */ "501325fb2645264864df11faa17bbd58312b77cad3d94ac8fb8542f0eb653ad7"+
					   		   "3d7fce932bb874cb89ac39fc47f8267cf0f0c209f204b2d8578a3bdf461cb6a2"+
					   		   "71a468bebaccd9685014ccbc9a73618c6a5e778a21cc8416c60ad24ddc417a13"+
					   		   "0d53eda6dfbfe47d09170a7be1a708b7b5f3ad464310be36d9a2a95dc39e83d3"+
					   		   "8667e842eb6411e8a23712297b165f690c2d7ca1b1346e3c1fccf5cafd4f8be0",
		     /* ciphertext */  "6724c372d2e9074da5e27a6c54b2d703dc1d4c9b1f8d90f00c122e692ace7700"+
					   		   "eadca942544507f1375b6581d5a8fb39981c1c0e6e1ff2140b082e9ec016fce1"+
					   		   "41d5199647d43b0b68bfd0fea5e00f468962c7384dd6129aea6a3fdfe75abb21"+
					   		   "0ed5607cef8fa0e152833d5ac37d52e557b91098a322e76a45bbbcf4899e7906"+
					   		   "18aa3f4c2e5e0fc3de93269a577d77a5502e8ea02f717b1dd2df1ec69d8b61ca",
	        },
	        {
	        				   "Set 9, vector#  0  (Test: Encrypt)",
	        		        	
	        	/* key = */    "737d7811ce96472efed12258b78122f11deaec8759ccbd71eac6bbefa627785c",//copied from above
				/* iv */	   "6724c372d2e9074da5e27a6c54b2d703dc1d4c9b1f8d90f0",
			   /* plaintext */ "501325fb2645264864df11faa17bbd58312b77cad3d94ac8fb8542f0eb653ad7"+
					   		   "3d7fce932bb874cb89ac39fc47f8267cf0f0c209f204b2d8578a3bdf461cb6a2"+
					   		   "71a468bebaccd9685014ccbc9a73618c6a5e778a21cc8416c60ad24ddc417a13"+
					   		   "0d53eda6dfbfe47d09170a7be1a708b7b5f3ad464310be36d9a2a95dc39e83d3"+
					   		   "8667e842eb6411e8a23712297b165f690c2d7ca1b1346e3c1fccf5cafd4f8be0",
		     /* ciphertext */  "cfb653dd50a04a8580847d5bb98dc15e27c60f5a70da635718ba6d589f47935e"+
					   		   "d476fc960ffaf3b8750a59171b1434429a977ba878aea7ace8dd083a92385851"+
					   		   "12591165d0948a86e89e6118d572aa85667cceffd78a60baa5a152dc5e29bdd9"+
					   		   "3f7389edde1541eec2f3aac38ea2bfc812f73de7e2e7b1322468f823a2c7c16e"+
					   		   "30fe9283894ac057da5c45a67f4988b4edafeb51c1b4a51a849d188b15838552",
	        },
	        
		};


//		/**
//		 * This vector is not handled by the current code in use.
//		 */
//	    public static final String[][] vectors128 =
//	    {
//	        {
//	                         "Set 1, vector#  0",
//	             /* key = */ "80000000000000000000000000000000",  // 128 bits
//	             /* IV = */  "0000000000000000",
//	   /* stream[0..63] = */ "4DFA5E481DA23EA09A31022050859936"+
//	                         "DA52FCEE218005164F267CB65F5CFD7F"+
//	                         "2B4F97E0FF16924A52DF269515110A07"+
//	                         "F9E460BC65EF95DA58F740B7D1DBB0AA",
//	/* stream[192..255] = */ "DA9C1581F429E0A00F7D67E23B730676"+
//	                         "783B262E8EB43A25F55FB90B3E753AEF"+
//	                         "8C6713EC66C51881111593CCB3E8CB8F"+
//	                         "8DE124080501EEEB389C4BCB6977CF95",
//	/* stream[256..319] = */ "7D5789631EB4554400E1E025935DFA7B"+
//	                         "3E9039D61BDC58A8697D36815BF1985C"+
//	                         "EFDF7AE112E5BB81E37ECF0616CE7147"+
//	                         "FC08A93A367E08631F23C03B00A8DA2F",
//	/* stream[448..511] = */ "B375703739DACED4DD4059FD71C3C47F"+
//	                         "C2F9939670FAD4A46066ADCC6A564578"+
//	                         "3308B90FFB72BE04A6B147CBE38CC0C3"+
//	                         "B9267C296A92A7C69873F9F263BE9703",
//	     /*  xor-digest = */ "F7A274D268316790A67EC058F45C0F2A"+
//	                         "067A99FCDE6236C0CEF8E056349FE54C"+
//	                         "5F13AC74D2539570FD34FEAB06C57205"+
//	                         "3949B59585742181A5A760223AFA22D4",
//	        },
//	    };
	    
}

