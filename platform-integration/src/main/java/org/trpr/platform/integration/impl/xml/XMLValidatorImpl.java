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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.trpr.platform.integration.spi.xml.XMLDataException;
import org.trpr.platform.integration.spi.xml.XMLValidator;
import org.trpr.platform.spi.execution.Severity;
import org.trpr.platform.spi.validation.ValidationResult;
import org.trpr.platform.spi.validation.ValidationSummary;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <code>XMLValidatorImpl<code> is an implementation of the {@link XMLValidator} interface using the default XML parsing library 
 * bundled with the platform
 * 
 * @see XMLValidator
 * 
 * @author Regunath B
 * @version 1.0, 25/05/2012
 */
public class XMLValidatorImpl implements XMLValidator {

	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(org.xml.sax.InputSource, javax.xml.validation.Schema)
	 */
     public ValidationSummary validate(InputSource input, Schema schema) throws XMLDataException{		
		ValidationSummary summary = new ValidationSummary();		
		Validator validator = schema.newValidator();
		try {
			validator.validate(new SAXSource(input));
		} catch (SAXException e) {
			ValidationResult result = new ValidationResult();
			result.setSeverity(Severity.ERROR);
			result.setMessage(e.getMessage());
			summary.addResult(result);
		} catch (IOException e) {
			throw new XMLDataException("Error during validation (Unable to read XML)", e);
		}		
		return summary;
	}

    /**
     * Interface method implementation
     * @see XMLValidator#validate(org.xml.sax.InputSource, java.io.File)
     */
	public ValidationSummary validate(InputSource input, File schema) throws XMLDataException {
		try {
			return this.validate(input, SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema));
		} catch (SAXException e) {
			throw new XMLDataException("Error during validation (Unable to read schema) ", e);
		}
	}

	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(java.lang.String, javax.xml.validation.Schema)
	 */
	public ValidationSummary validate(String input, Schema schema) throws XMLDataException {
		return this.validate(new InputSource(new ByteArrayInputStream(input.getBytes())), schema);
	}

	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(java.lang.String, java.io.File)
	 */
	public ValidationSummary validate(String input, File schema) throws XMLDataException {
		try {
			return this.validate(new InputSource(new ByteArrayInputStream(input.getBytes())), SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema));
		} catch (SAXException e) {
			throw new XMLDataException("Error during validation (Unable to read schema) ", e);
		}
	}

	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(java.lang.String, javax.xml.transform.Source)
	 */
	public ValidationSummary validate(String input, Source schema)
			throws XMLDataException {
		try {
			return this.validate(input, SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema));
		} catch (SAXException e) {
			throw new XMLDataException("Error during validation (Unable to read schema) ", e);
		}
	}

	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(java.lang.String, javax.xml.transform.Source[])
	 */
	public ValidationSummary validate(String input, Source[] schema)
			throws XMLDataException {
		try {
			return this.validate(input, SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema));
		} catch (SAXException e) {
			throw new XMLDataException("Error during validation (Unable to read schema) ", e);
		}
	}
	
	/**
	 * Interface method implementation
	 * @see XMLValidator#validate(org.xml.sax.InputSource, javax.xml.transform.Source[])
	 */
	public ValidationSummary validate(InputSource input, Source[] schema)
			throws XMLDataException {
		try {
			return this.validate(input, SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema));
		} catch (SAXException e) {
			throw new XMLDataException("Error during validation (Unable to read schema) ", e);
		}
	}

	/**
	 * Interface method implementation
	 * @see XMLValidator#getSchemaFromSource(javax.xml.transform.Source[])
	 */
	public Schema getSchemaFromSource(Source[] schema) throws XMLDataException {
		try {
			return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schema);
		} catch (SAXException e) {
			throw new XMLDataException("Error while creating the Schema object from a set of Source xsd's", e); 
		}
	}
	
}
