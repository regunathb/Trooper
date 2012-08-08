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
package org.trpr.platform.core.spi.security;

/**
 * The <code>CryptoProvider</code> interface defines methods for encryption and decryption. Specific implementations may support variations in type of keys
 * use, strength of encryption, padding mechanisms, key stores and approaches to creation of the final Ciphertext. 
 * 
 * @author Regunath B
 * @version 1.0, 01/06/2012
 */
public interface CryptoProvider {

	/**
	 * Encrypts the specified byte array plaintext
	 * @param plainText the content to be encrypted
	 * @return the ciphertext post encryption 
	 * @throws SecurityException in case of errors during the encryption process
	 */
	public byte[] encrypt(byte[] plainText) throws SecurityException;
	
	/**
	 * Decrypts the specified byte array ciphertext
	 * @param cipherText the content to be decrypted
	 * @return the plaintext post decryption
	 * @throws SecurityException in case of errors during the decryption process
	 */
	public byte[] decrypt(byte[] cipherText) throws SecurityException;
	
}
