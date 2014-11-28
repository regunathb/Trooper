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
package org.trpr.platform.integration.impl.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;
import org.trpr.platform.integration.spi.json.JSONTranscoder;
import org.trpr.platform.integration.spi.marshalling.MarshallingException;

/**
 * The <code> JSONTranscoderImpl </code> is an implementation of the {@link JSONTranscoder} interface. Uses the json-lib library (http://json-lib.sourceforge.net/)
 * to provide the concrete implementation.
 * 
 * @author Regunath B
 * @version 1.0, 18/09/2012
 */
public class JSONTranscoderImpl implements JSONTranscoder {
		
	/** The ObjectMapper instance for JSON marshalling/unmarshalling*/
	private ObjectMapper mapper = new ObjectMapper();
	
	/** The DateFormat for Date serialization */
	private DateFormat dateFormat = new SimpleDateFormat();
	
	/** Constructor for this class*/
	public JSONTranscoderImpl() {
		this.mapper.setDateFormat(this.getDateFormat());
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.integration.spi.json.JSONTranscoder#marshal(java.lang.Object)
	 */
	public String marshal(Object object) throws MarshallingException {
		try {
			return this.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(object);
		} catch (Exception e) {
			throw new MarshallingException("Error marshalling object : " + e.getMessage(), e);
		}
	}

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.integration.spi.json.JSONTranscoder#unmarshal(java.lang.String, java.lang.Class)
	 */
	public <T> T unmarshal(String json, Class<T> clazz) throws MarshallingException {
		try {
			return this.mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new MarshallingException("Error unmarshalling object : " + e.getMessage(), e);
		}
	}
	
	/** Start setter/getter methods */
	public DateFormat getDateFormat() {
		return this.dateFormat;
	}
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}	
	/** End setter/getter methods */
}
