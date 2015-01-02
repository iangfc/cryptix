package cryptix.alg.chacha;

/**
 * <p>
 * Test vectors for Chacha.
 * </p><p>
 * The <b> blockcounter </b> may be set with an optional 5th parameter,
 * else it is set to all zeros if not present.  This is the norm for
 * almost all test vectors.
 * </p><p>
 * The first argument (v[0]) is the Name,
 * which has the info for display.
 * The name also can switch the round count over to
 * 8 rounds by including the string "(8/" in it like "(8/256)",
 * and to 12 rounds with "(12/".
 * Sorry 'bot dat - was too lazy to do more params.
 * </p><p>
 * Another reference set is here:
 * http://www.dsource.org/projects/tango/docs/current/htmlsrc/tango.util.cipher.ChaCha.html
 * </p>
 * 
 * @author ada
 */
public class ChaChaVectors {
    
    /**
     * DJB's original single reference test vector.
     * Note that this was only 8 rounds, 256bit key,
     * and with a blockcounter of -1, probably to test
     * the rollover of the blockcounter (see warning
     * note in code).
     */
    public static final String[][] CReferenceVector8round =
    {
        {
            "DJB's C Reference (only 1) zero key, zero IV, BC -1! (8/256)",
            /* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
        /* KEYSTREAM */ "df8a0bceca2fbb111f29978fa9a64407c68ced975ff55a24707265d0687e6aefb3597eac46e4b0a79b3a49c8b0f1775ac3edd7eb6a6d9bcb38f276517a79e0143e00ef2f895f40d67f5bb8e81f09a5a12c840ec3ce9a7f3b181be188ef711a1e984ce172b9216f419f445367456d5619314a42a3da86b001387bfdb80e0cfe42d2aefa0deaa5c151bf0adb6c01f2a5adc0fd581259f9a2aadcf20f8fd566a26b5032ec38bbc5da98ee0c6f568b872a65a08abf251deb21bb4b56e5d8821e68aa7fe7b1ff12cffd9d7e21f517501ecaff43cea3e8e3eb28cbd8d1001f68b5c68755b970d3b7dafc64d3e59bdeaadc8f82a975a481df31b52870aa5fa2ba340af92ba037cdb63cb5a7277dc5d6dc549e4e28a15c70670f0e97787c170485829264ecbf14bddeb68410f423e8849e0ce35c10d20a802bbc3d9a6ca01c386279bf01e8f75f478cb0d159db767341602fa02d3e01c3d9aacf9b686eccf1bb5ff4c8fd21473e89d50f51f9a1ced2390c72ee7e37f15728e61d1fb2c8c839495e4890528c146d00fe2e1caec31b159fc42dcd7e06865c6fa5267c6ca9c5284e651e175a362f469b6e722347de959f76533315542ffa440d37cde8862da3b3331e53b60d73baeb620e63a2e646ea148974350aa337491e5f5fc087cb429173d1eeb74f5a73acc6c3d72b59b8bf5ab58cdcf76aa001689aac938a75b1bb25d77b5382898c4e73ba04bae3a083c8a2109f15b8c4680ae4ba1c70df5b513425349a77e95d3b565825a0227d45068e61eb90aa1a4dc414c0976911a52d46b39f40c5849e5abec705f33056187a8549ab397d5aeba1bd59f50c2b8e6ace6be29f79c708f3fe1afb144320dd2ec5602fa4883d78a7fccc6c3cff282ac9312f44374e8b7d294126e345b7f2c017b16bb335c696bc0cc302f3db897fa25365a2ead1f149d87a97e8b847203f3046950cfaeedcef2a97681f3fd178e8df82318134b4eb1bd3a9fccca3fb84fbdb6a443f7bc758e57d4721e660eeddc75332d53712f238a5c2cd89d80929d71364ec0f99f70e90b8bdc400b61ff7619a9bc20ccb5c17c399fe568512c4a75bc98c6cd5ab1f3f90f1fb8c2b1c08e2d75fb95eef806d597369a4799eef36fdf5b068bbaa21f30038579b983b9eb5157a1fc82cea868763199da242b3567df467d0534b07072526bb31d92ba0b2e18f5366c3267e2bbd7d25b00d4c2506f6c3ffed79f58ecea88080c56070be4a3e5bafdd94f2e39ef521ae70e94a099d99389f948c15f0e9ae1888fa9a18c158760583a4a58ffb4b535d7c7d9aa7c5aa113df76516a008a971400276a8cb06d40c21cf4c8f91260ca89b89e8620ec1469eee5451b9586dfc3072aa11e389cac7b9f383dcc7cef9ab81edd9af7c15b8eae6283dfd2b72c68e",
        /* BlockCtr  */ "ffffffffffffffff",
        }
    };
            
    /**
     * Joachim Strombergson's test vector set:
     * http://tools.ietf.org/html/draft-strombergson-chacha-test-vectors-00
     * The below set only includes some 8,20 rounds, 256 bit keys,
     * there are many more in the published set.
     * Note that you want the latest version:
     * http://datatracker.ietf.org/doc/draft-strombergson-chacha-test-vectors/
     * https://github.com/secworks/chacha_testvectors/blob/master/src/chacha_testvectors.txt
     * The generator is in github, above.
     */
    public static final String[][] Strombergson256key =
    {
        
        {
            "Strombergson ID #TC1 zero key, zero iv - 4 (8/256)",
            /* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "3e00ef2f895f40d67f5bb8e81f09a5a12c840ec3ce9a7f3b181be188ef711a1e984ce172b9216f419f445367456d5619314a42a3da86b001387bfdb80e0cfe42d2aefa0deaa5c151bf0adb6c01f2a5adc0fd581259f9a2aadcf20f8fd566a26b5032ec38bbc5da98ee0c6f568b872a65a08abf251deb21bb4b56e5d8821e68aa"
        },
        {
            "Strombergson ID #TC1 zero key, zero iv - 5 (12/256)",
            /* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "9bf49a6a0755f953811fce125f2683d50429c3bb49e074147e0089a52eae155f0564f879d27ae3c02ce82834acfa8c793a629f2ca0de6919610be82f411326be0bd58841203e74fe86fc71338ce0173dc628ebb719bdcbcc151585214cc089b442258dcda14cf111c602b8971b8cc843e91e46ca905151c02744a6b017e69316"
        },
        {
            "Strombergson ID #TC1 zero key, zero iv - 6 (20/256)",
            /* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "76b8e0ada0f13d90405d6ae55386bd28bdd219b8a08ded1aa836efcc8b770dc7da41597c5157488d7724e03fb8d84a376a43b8f41518a11cc387b669b2ee65869f07e7be5551387a98ba977c732d080dcb0f29a048e3656912c6533e32ee7aed29b721769ce64e43d57133b074d839d531ed1f28510afb45ace10a1f4b794d6f"
        },
        
        {
            "Strombergson ID #TC1 zero key, zero iv - 1 (8/128)",
            /* key = */ "00000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "e28a5fa4a67f8c5defed3e6fb7303486aa8427d31419a729572d777953491120b64ab8e72b8deb85cd6aea7cb6089a101824beeb08814a428aab1fa2c816081b8a26af448a1ba906368fd8c83831c18cec8ced811a028e675b8d2be8fce081165ceae9f1d1b7a975497749480569ceb83de6a0a587d4984f19925f5d338e430d"
        },
        {
            "Strombergson ID #TC1 zero key, zero iv - 2 (12/128)",
            /* key = */ "00000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "e1047ba9476bf8ff312c01b4345a7d8ca5792b0ad467313f1dc412b5fdce32410dea8b68bd774c36a920f092a04d3f95274fbeff97bc8491fcef37f85970b4501d43b61a8f7e19fceddef368ae6bfb11101bd9fd3e4d127de30db2db1b472e76426803a45e15b962751986ef1d9d50f598a5dcdc9fa529a28357991e784ea20f"
        },
        {
            "Strombergson ID #TC1 zero key, zero iv - 3 (20/128)",
            /* key = */ "00000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "89670952608364fd00b2f90936f031c8e756e15dba04b8493d00429259b20f46cc04f111246b6c2ce066be3bfb32d9aa0fddfbc12123d4b9e44f34dca05a103f6cd135c2878c832b5896b134f6142a9d4d8d0d8f1026d20a0a81512cbce6e9758a7143d021978022a384141a80cea3062f41f67a752e66ad3411984c787e30ad"
        },
        
        
        
        {
            "Strombergson ID #TC2 1bit key, zero iv - 6 (8/256)",
            /* key = */ "0100000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "cf5ee9a0494aa9613e05d5ed725b804b12f4a465ee635acc3a311de8740489ea289d04f43c7518db56eb4433e498a1238cd8464d3763ddbb9222ee3bd8fae3c8b4355a7d93dd8867089ee643558b95754efa2bd1a8a1e2d75bcdb32015542638291941feb49965587c4fdfe219cf0ec132a6cd4dc067392e67982fe53278c0b4"
        },
        {
            "Strombergson ID #TC2 1bit key, zero iv - 6 (20/256)",
            /* key = */ "0100000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0000000000000000",
            /* stream*/ "c5d30a7ce1ec119378c84f487d775a8542f13ece238a9455e8229e888de85bbd29eb63d0a17a5b999b52da22be4023eb07620a54f6fa6ad8737b71eb0464dac010f656e6d1fd55053e50c4875c9930a33f6d0263bd14dfd6ab8c70521c19338b2308b95cf8d0bb7d202d2102780ea3528f1cb48560f76b20f382b942500fceac" 
                       // "12056e595d56b0f6eef090f0cd25a20949248c2790525d0f930218ff0b4ddd10a6002239d9a454e29e107a7d06fefdfef0210feba044f9f29b1772c960dc29c00c7366c5cbc604240e665eb02a69372a7af979b26fbb78092ac7c4b88029a7c854513bc217bbfc7d90432e308eba15afc65aeb48ef100d5601e6afba257117a9"
        },
        {
            "Strombergson ID #TC3 zero key, 1bit iv - 6 (20/256)",
            /* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
            /* IV = */  "0100000000000000",
            /* stream*/ "ef3fdfd6c61578fbf5cf35bd3dd33b8009631634d21e42ac33960bd138e50d32111e4caf237ee53ca8ad6426194a88545ddc497a0b466e7d6bbdb0041b2f586b5305e5e44aff19b235936144675efbe4409eb7e8e5f1430f5f5836aeb49bb5328b017c4b9dc11f8a03863fa803dc71d5726b2b6b31aa32708afe5af1d6b69058"
        },
        {
            "Strombergson ID #TC4 ff key, ff iv - 6 (20/256)",
            /* key = */ "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            /* IV = */  "ffffffffffffffff",
            /* stream*/ "d9bf3f6bce6ed0b54254557767fb57443dd4778911b606055c39cc25e674b8363feabc57fde54f790c52c8ae43240b79d49042b777bfd6cb80e931270b7f50eb5bac2acd86a836c5dc98c116c1217ec31d3a63a9451319f097f3b4d6dab0778719477d24d24b403a12241d7cca064f790f1d51ccaff6b1667d4bbca1958c4306"
        },
        {
            "Strombergson ID #TC5 55 key, 55 iv - 6 (20/256)",
            /* key = */ "5555555555555555555555555555555555555555555555555555555555555555",
            /* IV = */  "5555555555555555",
            /* stream*/ "bea9411aa453c5434a5ae8c92862f564396855a9ea6e22d6d3b50ae1b3663311a4a3606c671d605ce16c3aece8e61ea145c59775017bee2fa6f88afc758069f7e0b8f676e644216f4d2a3422d7fa36c6c4931aca950e9da42788e6d0b6d1cd838ef652e97b145b14871eae6c6804c7004db5ac2fce4c68c726d004b10fcaba86",
        },
        {
            "Strombergson ID #TC6 aa key, aa iv - 6 (20/256)",
            /* key = */ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            /* IV = */  "aaaaaaaaaaaaaaaa",
            /* stream*/ "9aa2a9f656efde5aa7591c5fed4b35aea2895dec7cb4543b9e9f21f5e7bcbcf3c43c748a970888f8248393a09d43e0b7e164bc4d0b0fb240a2d72115c480890672184489440545d021d97ef6b693dfe5b2c132d47e6f041c9063651f96b623e62a11999a23b6f7c461b2153026ad5e866a2e597ed07b8401dec63a0934c6b2a9"
        },
        {
            "Strombergson ID #TC7 seq key, seq iv - 6 (20/256)",
            /* key = */ "00112233445566778899aabbccddeeffffeeddccbbaa99887766554433221100",
            /* IV = */  "0f1e2d3c4b596877",
            /* stream*/ "87fa92061043ca5e631fedd88e8bfb84ad6b213bdee4bc806e2764935fb89097218a897b7aead10e1b17f6802b2abdd95594903083735613d6b3531b9e0d1b6747908c74f018f6e182138b991b9c5a957c69f23c26c8a2fbb8b0acf8e64222cc251281a61cff673608de6490b41ca1b9f4ab754474f9afc7c35dcd65de3d745f",
        },
        {
            "Strombergson ID #TC8 rand key, rand iv - 6 (20/256)",
            /* key = */ "c46ec1b18ce8a878725a37e780dfb7351f68ed2e194c79fbc6aebee1a667975d",
            /* IV = */  "1ada31d5cf688221",
            /* stream*/ "f63a89b75c2271f9368816542ba52f06ed49241792302b00b5e8f80ae9a473afc25b218f519af0fdd406362e8d69de7f54c604a6e00f353f110f771bdca8ab92e5fbc34e60a1d9a9db17345b0a402736853bf910b060bdf1f897b6290f01d138ae2c4c90225ba9ea14d518f55929dea098ca7a6ccfe61227053c84e49a4a3332",
        },
    };
            
    /**
     * Adam Langley's chacha20poly1305 construction, all vectors included below.
     * https://tools.ietf.org/html/draft-agl-tls-chacha20poly1305-02#section-7
     */
	public static final String[][] Langley20rounds256key =
	{
		{
			"Langley Set 1, vector#  0",
			/* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
			/* IV =  */ "0000000000000000",
		/* KEYSTREAM */ "76b8e0ada0f13d90405d6ae55386bd28bdd219b8a08ded1aa836efcc8b770dc7da41597c5157488d7724e03fb8d84a376a43b8f41518a11cc387b669",
		},
		{
			"Langley Set 1, vector#  1",
			/* key = */ "0000000000000000000000000000000000000000000000000000000000000001",
			/* IV =  */ "0000000000000000",
		/* KEYSTREAM */ "4540f05a9f1fb296d7736e7b208e3c96eb4fe1834688d2604f450952ed432d41bbe2a0b6ea7566d2a5d1e7e20d42af2c53d792b1c43fea817e9ad275",
		},
		{
			"Langley Set 1, vector#  2",
			/* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
			/* IV =  */ "0000000000000001",
		/* KEYSTREAM */ "de9cba7bf3d69ef5e786dc63973f653a0b49e015adbff7134fcb7df137821031e85a050278a7084527214f73efc7fa5b5277062eb7a0433e445f41e3",
		},
		{
			"Langley Set 1, vector#  3",
			/* key = */ "0000000000000000000000000000000000000000000000000000000000000000",
			/* IV =  */ "0100000000000000",
		/* KEYSTREAM */ "ef3fdfd6c61578fbf5cf35bd3dd33b8009631634d21e42ac33960bd138e50d32111e4caf237ee53ca8ad6426194a88545ddc497a0b466e7d6bbdb004",
		},
		{
			"Langley Set 1, vector#  4",
			/* key = */ "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
			/* IV =  */ "0001020304050607",
		/* KEYSTREAM */ "f798a189f195e66982105ffb640bb7757f579da31602fc93ec01ac56f85ac3c134a4547b733b46413042c9440049176905d3be59ea1c53f15916155c2be8241a38008b9a26bc35941e2444177c8ade6689de95264986d95889fb60e84629c9bd9a5acb1cc118be563eb9b3a4a472f82e09a7e778492b562ef7130e88dfe031c79db9d4f7c7a899151b9a475032b63fc385245fe054e3dd5a97a5f576fe064025d3ce042c566ab2c507b138db853e3d6959660996546cc9c4a6eafdc777c040d70eaf46f76dad3979e5c5360c3317166a1c894c94a371876a94df7628fe4eaaf2ccb27d5aaae0ad7ad0f9d4b6ad3b54098746d4524d38407a6deb",
		}
	};	
}

