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
 * The <code>HashGenerator</code> interface defines methods for generating one-way hashes of data
 * 
 * @author Regunath B
 * @version 1.0, 31/05/2012
 */
public interface HashGenerator {

	/**
	 * Generates a hash value for the specified data
	 * @param data byte array to be hashed
	 * @return hashed value as a byte array
	 * @throws SecurityException in case of errors during hashing
	 */
	public byte[] generateHash(byte[] data) throws SecurityException;
	
	/**
	 * Generates a hash value for the specified data
	 * @param data byte array to be hashed
	 * @param salt byte array
	 * @return hashed value as a byte array
	 * @throws SecurityException in case of errors during hashing
	 */
	public byte[] generateHash(byte[] data,  byte[] salt) throws SecurityException;
	
	/**
	 * Variant of {@link HashGenerator#generateHash(byte[])} that returns the hash as a Hex string
	 * @see HashGenerator#generateHash(byte[])
	 * @return hash value as a Hex string
	 */
	public String generateHashAsHexString(byte[] data) throws SecurityException;

	/**
	 * Variant of {@link HashGenerator#generateHash(byte[], byte[])} that returns the hash as a Hex string
	 * @see HashGenerator#generateHash(byte[], byte[])
	 * @return hash value as a Hex string
	 */
	public String generateHashAsHexString(byte[] data, byte[] salt) throws SecurityException;
	
	/**
	 * Variant of {@link HashGenerator#generateHash(byte[])} that returns the hash as a Base64 encoded string
	 * @see HashGenerator#generateHash(byte[])
	 * @return hash value as a Base64 encoded string
	 */
	public String generateHashAsBase64String(byte[] data) throws SecurityException;

	/**
	 * Variant of {@link HashGenerator#generateHash(byte[], byte[])} that returns the hash as a Base64 encoded string
	 * @see HashGenerator#generateHash(byte[], byte[])
	 * @return hash value as a Base64 encoded string
	 */
	public String generateHashAsBase64String(byte[] data, byte[] salt) throws SecurityException;
		
}
