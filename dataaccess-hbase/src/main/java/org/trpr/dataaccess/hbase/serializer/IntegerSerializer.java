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
package org.trpr.dataaccess.hbase.serializer;

import org.apache.hadoop.hbase.util.Bytes;
import org.trpr.platform.core.spi.persistence.Serializer;

/**
 * Implementation of {@link}org.trpr.platform.core.spi.persistence.Serializer 
 * that specializes in serializing and de-serializing values of type {@link}java.lang.Integer. 
 * @author Srikanth P Shreenivas
 *
 */
public class IntegerSerializer implements Serializer {
	
	@Override
	public byte[] toBytes(Object data) {
		if (data instanceof Integer) {
			return Bytes.toBytes(((Integer) data));
		} else {
			throw new IllegalArgumentException("java.lang.Integer expected");
		}
	}

	@Override
	public Integer toObject(byte[] dataBytes) {
		return Integer.valueOf(Bytes.toInt(dataBytes));
	}
}