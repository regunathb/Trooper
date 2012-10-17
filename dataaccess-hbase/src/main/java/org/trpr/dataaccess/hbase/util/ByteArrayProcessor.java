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
package org.trpr.dataaccess.hbase.util;

/**
 * An class that allows encoding/decoding of byte arrays. This is useful for
 * HBase which stores data in byte array form, and can be used for adding
 * additional layers of processing to data before it gets persisted, or after it
 * is read from database.
 * 
 * A typical example will be to add encryption of data.
 * 
 * @author Srikanth P Shreenivas
 * 
 */
public abstract class ByteArrayProcessor {

	/**
	 * Flag indicates whether this processor is enabled
	 */
	private boolean enabled;

	/**
	 * Encodes the data. This will be called before saving data into HBase.
	 * Encode method will not do anything if the processor is not enabled.
	 * 
	 * @param input
	 *            Data to encode
	 * @return Encode data
	 * @throws PlatformException
	 */
	public byte[] encode(byte[] input) throws RuntimeException {
		if (enabled) {
			return _encode(input);
		} else {
			return input;
		}
	}

	protected abstract byte[] _encode(byte[] input) throws RuntimeException;

	/**
	 * Decodes the data. This should be called after reading data from HBase.
	 * Decode method will try to do decoding even if processor is disabled.
	 * Internally, it applies decoding only if data was ever encoded using this
	 * processor. An advantage of this is that data that was encoded using a
	 * processor which is currently disabled will continue to work.
	 * 
	 * @param input
	 *            Data to decode
	 * @return Decoded data
	 * @throws PlatformException
	 */
	public byte[] decode(byte[] input) throws RuntimeException {
		return _decode(input);
	}

	protected abstract byte[] _decode(byte[] input) throws RuntimeException;

	/**
	 * Indicates whether this processor is enabled or not
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Should be used for indicating whether it is enabled or not. This flag
	 * helps to disable/enable processors based on environment's needs. Example:
	 * In QA, you would probably like to turn off encryption processor, while
	 * enable it in production.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
