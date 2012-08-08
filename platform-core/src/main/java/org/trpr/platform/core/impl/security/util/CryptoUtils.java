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
package org.trpr.platform.core.impl.security.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * The <code>CryptoUtils</code> provides cyptography related utility methods 
 * 
 * @author Regunath B
 * @version 1.0, 31/05/2012
 */
public class CryptoUtils {

	/**
	 * Algorithm used for random number generation
	 */
	private static final String RANDOM_ALGORITHM_NAME = "SHA1PRNG";
	
	/**
	 * Converts the specified byte array to a Hex string
	 * @param bytes the byte array to be converted to Hex
	 * @return Hex string of the specified byte array
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			result.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return result.toString();
	}
	
	/**
	 * Generates a secure random object
	 * @return SecureRandom instance
	 * @throws SecurityException in case the random generation algorithm is not found
	 */
	public static SecureRandom generateRandom() throws SecurityException {
		SecureRandom sr = null;
		try {
			// Create a secure random number generator instance
			sr = SecureRandom.getInstance( CryptoUtils.RANDOM_ALGORITHM_NAME);
			// Get 1024 random bits
			byte[] bytes = new byte[1024 / 8];
			sr.nextBytes(bytes);
			int seedByteCount = 10;
			byte[] seed = sr.generateSeed(seedByteCount);
			sr = SecureRandom.getInstance( CryptoUtils.RANDOM_ALGORITHM_NAME);
			// Setting the seed value
			sr.setSeed(seed);
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("Random Number Generation Error. No such algorithm : " + CryptoUtils.RANDOM_ALGORITHM_NAME);
		}
		return sr;
	}
	
	/**
	 * Combines two byte arrays
	 * @param byteArr1 the first byte array
	 * @param byteArr2 the second byte array. Appended after the byteArr1 in the output
	 * @return byte[] combined byte array
	 */
	public static byte[] mergeArrays(byte[] byteArr1, byte[] byteArr2) {
		byte[] message = new byte[byteArr1.length + byteArr2.length];
		System.arraycopy(byteArr1, 0, message, 0, byteArr1.length);
		System.arraycopy(byteArr2, 0, message, byteArr1.length, byteArr2.length);
		return message;
	}
	
	/**
	 * Splits the specified byte array in two where:
	 * <pre>
	 * result[0] - bytes of the specified length
	 * result[1] - remaining bytes
	 * <pre>
	 * @param src byte array to be split
	 * @param length element index at which to split the byte array
	 * @return byte[][] two byte arrays that have been split
	 */
	public static byte[][] split(byte[] src, int length) {
		byte[] result, remaining;
		if (src == null || src.length <= length) {
			result = src;
			remaining = new byte[0];
		} else {
			result = new byte[length];
			remaining = new byte[src.length - length];
			System.arraycopy(src, 0, result, 0, length);
			System.arraycopy(src, length, remaining, 0, remaining.length);
		}
		return new byte[][] { result, remaining };
	}
	
}
