/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trpr.platform.core.impl.security;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.trpr.platform.core.impl.security.util.CryptoUtils;
import org.trpr.platform.core.spi.security.CryptoProvider;
import org.trpr.platform.core.spi.security.HashGenerator;
import org.trpr.platform.core.spi.security.SecurityException;

/**
 * The <code>DefaultCryptoProvider</code> is an implementation of the {@link CryptoProvider} based on the AES algorithm. 
 * The final ciphertext created by this implementation is of the concatenated form:
 * <pre> 
 * 		InitializationVector + AES secret key + ciphertext from AES encryption
 * <pre>
 * 
 * @author Regunath B
 * @version 1.0, 01/06/2012
 */
public class DefaultCryptoProvider implements CryptoProvider {

	/**
	 * The AES algorithm implementation provider
	 */
	private static final String SECURITY_PROVIDER = "BC";
	
	/**
	 * Secret Key Algorithm
	 */
	private static final String SECRET_KEY_ALGORITHM = "AES";	

	/**
	 * Base Algorithm used for security of data
	 */
	private static final String ENCRYPT_ALGORITHM = "AES/CFB/NoPadding";
	private static final String DECRYPT_ALGORITHM = ENCRYPT_ALGORITHM;

	/**
	 * Size of the AES secret key in bits and bytes
	 */
	private static final int SECRET_KEY_SIZE = 128;
	private static final int SECRET_KEY_SIZE_BYTES = 128/8;
	
	/**
	 * Size of the Initialization Vector in bytes
	 */
	private static final int VECTOR_SIZE = 16;
	
	static {
		// Registering the Bouncy Castle as the JCE provider.
		Security.addProvider(new BouncyCastleProvider());
	}
	
	/** The HashGenerator for this CryptoProvider*/
	private HashGenerator hashGenerator;
	
	/**
	 * Constructor for this crypto provider
	 * @param hashGenerator the HashGenerator to use
	 */
	public DefaultCryptoProvider(HashGenerator hashGenerator) {
		this.hashGenerator = hashGenerator;
	}

	/**
	 * Interface method implementation. Produces a final ciphertext of the form:
	 * <pre> 
	 * 		InitializationVector + AES secret key + ciphertext from AES encryption
	 * <pre>
	 * @see CryptoProvider#encrypt(byte[])
	 */
	public byte[] encrypt(byte[] plainText) throws SecurityException {
		
		// compute the initialization vector
		SecureRandom random = CryptoUtils.generateRandom();
		byte[] iv32 = this.hashGenerator.generateHash(random.toString().getBytes());
		// the Hash generator creates a 32 byte hash, the IV is only 16 bytes
		byte[] iv = CryptoUtils.split(iv32, VECTOR_SIZE)[0]; 

		byte[] aesCipherText = null; // the AES ciphertext post encryption
		
		// get the AES cipher
		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
		byte[] rawSecretKey = generateSecretKey();
		SecretKeySpec skeySpec = new SecretKeySpec(rawSecretKey, ENCRYPT_ALGORITHM);
		Cipher aesCipher;
		try {
			aesCipher = Cipher.getInstance(ENCRYPT_ALGORITHM, SECURITY_PROVIDER);
			// Initialize the Cipher for Encryption
			aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
			aesCipherText = aesCipher.doFinal(plainText);
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Encrypting data using AES algorithm failed : " + e.getMessage(), e);
		}
		
		// now merge into the format: iv + rawSecretKey 
		byte[] ivPlusRawSecretKey = CryptoUtils.mergeArrays(iv, rawSecretKey);
		
		// return the final ciphertext of the form : iv + rawSecretKey + aesCipherText
		return CryptoUtils.mergeArrays(ivPlusRawSecretKey, aesCipherText);
	}
	
	/**
	 * Interface method implementation. Returns a decrypted text for the ciphertext produced using {@link #encrypt(byte[])}
	 * @see CryptoProvider#decrypt(byte[])
	 */
	public byte[] decrypt(byte[] cipherText) throws SecurityException {
		
		//ciphertext is assumed to be of the form : iv + rawSecretKey + aesCipherText
		
		// get the iv
		byte[] iv = new byte[VECTOR_SIZE];
		this.copyByteArray(cipherText, 0, iv.length, iv);
		
		// get the AES key
		byte[] rawSecretKey = new byte[SECRET_KEY_SIZE_BYTES];
		this.copyByteArray(cipherText, 0 + VECTOR_SIZE, SECRET_KEY_SIZE_BYTES, rawSecretKey);
		
		// get the ciphertext to be decrypted
		byte[] aesCipherText = new byte[cipherText.length - VECTOR_SIZE - SECRET_KEY_SIZE_BYTES];
		this.copyByteArray(cipherText, 0 + VECTOR_SIZE + SECRET_KEY_SIZE_BYTES, aesCipherText.length, aesCipherText);
		
		try {
			// perform decryption
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
			Cipher aesCipher = Cipher.getInstance(DECRYPT_ALGORITHM, SECURITY_PROVIDER);
	
			SecretKeySpec decryptedKeySpec = new SecretKeySpec(rawSecretKey, DECRYPT_ALGORITHM);
			aesCipher.init(Cipher.DECRYPT_MODE, decryptedKeySpec, paramSpec);
			return aesCipher.doFinal(aesCipherText);
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Error decrypting AES ciphertext : " + e.getMessage(), e);
		}
	}

	/**
	 * Generates an AES key using KeyGenerator
	 * @throws SecurityException if the AES algorithm not supported
	 */
	private byte[] generateSecretKey() throws SecurityException {
		byte[] rawSecretKey = null;
		try {
			KeyGenerator kgen = null;
			try {
				kgen = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM, SECURITY_PROVIDER);
			} catch (NoSuchProviderException e) {
				throw new SecurityException("Bouncy Castle Provider not available : " + e.getMessage(), e);
			}
			kgen.init(SECRET_KEY_SIZE); // We are using 128 bits AES key
			SecretKey aeskey = kgen.generateKey();
			rawSecretKey = aeskey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("AES algorithm not supported : " + e.getMessage(), e);
		}
		return rawSecretKey;
	}
	
	/**
	 * Helper method to wrap the {@link System#arraycopy(Object, int, Object, int, int)} method call and handle exceptions.
	 * Always copies to index 0 in the destination array
	 * @see System#arraycopy(Object, int, Object, int, int)
	 */
	private void copyByteArray(byte[] src, int offset, int length, byte[] dest) throws SecurityException {
		try {
			System.arraycopy(src, offset, dest, 0,length);
		}
		catch(Exception e) {
			throw new SecurityException("Index/Array access error in ciphertext during decryption : " + e.getMessage(), e);
		}
	}
	
	
}
