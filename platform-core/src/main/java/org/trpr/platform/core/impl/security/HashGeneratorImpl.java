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
import java.security.MessageDigest;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.trpr.platform.core.impl.security.util.CryptoUtils;
import org.trpr.platform.core.spi.security.HashGenerator;
import org.trpr.platform.core.spi.security.SecurityException;

/**
 * The <code>HashGeneratorImpl</code> is an implementation of the {@link HashGenerator} using the BouncyCastle API {@linkplain http://www.bouncycastle.org/}
 * 
 * @author Regunath B
 * @version 1.0, 31/05/2012
 */
public class HashGeneratorImpl implements HashGenerator {
	
	/**
	 * Hashing Algorithm Used for encryption and decryption
	 */
	private static final String ALGORITHM = "SHA-256";

	/**
	 * SHA-256 Implementation provider
	 */
	private static final String SECURITY_PROVIDER = "BC";

	static {
		// Registering the Bouncy Castle as the JCE provider.
		Security.addProvider(new BouncyCastleProvider());
	}
	
	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHash(byte[])
	 */
	public byte[] generateHash(byte[] data) throws SecurityException {
		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM,	SECURITY_PROVIDER);
			digest.reset();
			return digest.digest(data);
		} catch (GeneralSecurityException e) {
			throw new SecurityException( ALGORITHM + " Hashing algorithm not available : " + e.getMessage(), e);
		}
	}

	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHash(byte[], byte[])
	 */
	public byte[] generateHash(byte[] data, byte[] salt) throws SecurityException {
		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM,	SECURITY_PROVIDER);
			digest.reset();
			digest.update(salt);
			return digest.digest(data);
		} catch (GeneralSecurityException e) {
			throw new SecurityException( ALGORITHM + " Hashing algorithm not available : " + e.getMessage(), e);
		}
	}

	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHashAsHexString(byte[])
	 */	
	public String generateHashAsHexString(byte[] data) throws SecurityException {
		return CryptoUtils.byteArrayToHexString(this.generateHash(data));
	}

	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHashAsHexString(byte[], byte[])
	 */	
	public String generateHashAsHexString(byte[] data, byte[] salt) throws SecurityException {
		return CryptoUtils.byteArrayToHexString(this.generateHash(data, salt));
	}

	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHashAsBase64String(byte[])
	 */	
	public String generateHashAsBase64String(byte[] data) throws SecurityException {
		return new String(Base64.encode(generateHash(data)));
	}

	/**
	 * Interface method implementation.
	 * @see HashGenerator#generateHashAsBase64String(byte[], byte[])
	 */	
	public String generateHashAsBase64String(byte[] data, byte[] salt) throws SecurityException {
		return new String(Base64.encode(generateHash(data, salt)));
	}

}
