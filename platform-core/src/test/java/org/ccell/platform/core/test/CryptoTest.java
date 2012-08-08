package org.trpr.platform.core.test;

import java.util.HashMap;

import org.trpr.platform.core.impl.security.DefaultCryptoProvider;
import org.trpr.platform.core.impl.security.HashGeneratorImpl;
import org.trpr.platform.core.impl.token.pwd.PasswordGenerator;

/**
 * Test class for the platform cryptography implementation
 * 
 * @author Regunath B
 *
 */
public class CryptoTest {

	public static void main(String[] args) {
		
		// check hash generator
		System.out.println("Hash generated for 'test' : " + new HashGeneratorImpl().generateHashAsBase64String("test".getBytes()));
		// check password generator
		System.out.println("Password of default length : " + new PasswordGenerator().generate(null));
		// check password generator with length 4
		System.out.println("Password of length 4 : " + new PasswordGenerator().generate(new HashMap(){{
			put("MaxValue",4);
		}}));
		
		// test encryption & decryption
		DefaultCryptoProvider cryptoProvider = new DefaultCryptoProvider(new HashGeneratorImpl());
		byte[] cipherText = cryptoProvider.encrypt("test".getBytes());		
		System.out.println("Result after encrypt,decrypt : " + new String(cryptoProvider.decrypt(cipherText)));		
	}
	
}
