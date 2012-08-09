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
package org.trpr.platform.integration.impl.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.trpr.platform.integration.spi.xml.XMLDataException;
import org.trpr.platform.integration.spi.xml.XMLTranscoder;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * The <code> XMLTranscoderImpl </code> is as implementation of {@link XMLTranscoder} using the Spring OXM framework.
 * Note that marshaling/unmarshaling works depending on the {@link Marshaller} and {@link Unmarshaller} implementations injected into an instance of this class.
 * The default implementation uses the JAXB provider and thereby implies support only for those Java types that have been XML schema derived, 
 * usually defined and compiled in any of the "model" projects defined - for e.g. platform-model.
 * 
 * Users of this transcoder implementation may inject suitable binding providers such as Castor, Jibx or any of the types supported by Spring OXM.
 * 
 * @author Regunath B
 * @version 1.0, 25/05/2012
 */
public class XMLTranscoderImpl implements XMLTranscoder {
	
	/** Default properties for the JAXB Marshaller*/
	private static final Map<String, Object> DEFAULT_MARSHALLER_PROPS = new HashMap<String,Object>();
	static {
		DEFAULT_MARSHALLER_PROPS.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}
	
	/** The default Marshaller, Unmarshaller instances based on JAXB*/
	private Jaxb2Marshaller defaultMarshaller = new Jaxb2Marshaller();
	private Jaxb2Marshaller defaultUnmarshaller = defaultMarshaller;
	
	/** The Spring OXM Marshaller, Unmarshaller instances to use*/
	private Marshaller marshaller = defaultMarshaller;
	private Unmarshaller unmarshaller = defaultUnmarshaller;

	/**
	 * Interface method implementation. Note that the specified Object should be an XML schema derived type. 
	 * @see XMLTranscoder#marshal(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public String marshal(Object object) throws XMLDataException {
		StringWriter stringWriter = new StringWriter();
		this.marshalToResult(object, new StreamResult(stringWriter));
		try {
			stringWriter.close();
		} catch (IOException e) {
			// ignore
		}
		return stringWriter.toString();
	}
	
	/**
	 * Interface method implementation
	 * @see XMLTranscoder#marshal(Object, Result)
	 */
	public void marshal(Object object, Result result) throws XMLDataException {
		this.marshalToResult(object, result);
	}

	/**
	 * Interface method implementation. Note that the package name specified should contain JAXB compatible generated classes/artifacts.
	 * @see XMLTranscoder#unmarshal(String, Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(String xml, Class<T> clazz) throws XMLDataException {
		StringReader stringReader = new StringReader(xml);
		try {
			if (this.getUnmarshaller() == this.defaultUnmarshaller) {
				// the default unmarshaller is not initialized with the context path. Initialize it by calling suitable methods
				this.defaultUnmarshaller.setContextPath(clazz.getPackage().getName());
				this.defaultUnmarshaller.afterPropertiesSet();
			}
			return (T)this.getUnmarshaller().unmarshal(new StreamSource(stringReader));
		} catch (Exception e) {
			throw new XMLDataException("Error unmarshalling XML. XML:packageName is " + xml + ":" + clazz.getPackage().getName(),e);
		} finally {
			stringReader.close();
		}
	}
	
	/** Start Spring DI style setters/getters*/
	public Marshaller getMarshaller() {
		return this.marshaller;
	}
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	public Unmarshaller getUnmarshaller() {
		return this.unmarshaller;
	}
	public void setUnmarhaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}
	/** End Spring DI style setters/getters*/

	/**
	 * Helper method to marshal the Java Object to the specified Result
	 */
	private void marshalToResult(Object object, Result result) throws XMLDataException {
		try {
			if (this.getMarshaller() == this.defaultMarshaller) {
				// the default marshaller is not initialized with the context path. Initialize it by calling suitable methods
				this.defaultMarshaller.setMarshallerProperties(XMLTranscoderImpl.DEFAULT_MARSHALLER_PROPS);
				this.defaultMarshaller.setContextPath(object.getClass().getPackage().getName());
				this.defaultMarshaller.afterPropertiesSet();
			}
			this.getMarshaller().marshal(object, result);		
		} catch (Exception e) {
			throw new XMLDataException("Error marshalling Object of type : " + object.getClass().getName(),e);
		}
	}
	
	/**
	 * Replacement method that uses JAXB directly instead of via Spring OXM. Provided just as an option
	 */
	private String marshalUsingJAXB(Object object) throws XMLDataException {		
		try {
			StringWriter stringWriter = new StringWriter();
			javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance(object.getClass().getPackage().getName());
			javax.xml.bind.Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);			
			marshaller.marshal(object, stringWriter);
			return stringWriter.toString();
		} catch (javax.xml.bind.JAXBException e) {
			throw new XMLDataException("Error marshalling Object of type : " + object.getClass().getName(),e);
		}
	}

	/**
	 * Replacement method that uses JAXB directly instead of via Spring OXM. Provided just as an option
	 */
	@SuppressWarnings("unchecked")
	private <T> T unmarshalUsingJAXB(String xml, Class<T> clazz) throws XMLDataException {
		try {
			javax.xml.bind.JAXBContext context = javax.xml.bind.JAXBContext.newInstance(clazz.getPackage().getName());
			javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
			return (T)unmarshaller.unmarshal(new StringReader(xml));
		} catch (javax.xml.bind.JAXBException e) {
			throw new XMLDataException("Error unmarshalling XML. XML:packageName is " + xml + ":" + clazz.getPackage().getName(),e);
		}
	}
}
