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

package org.trpr.platform.integration.spi.xml;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.trpr.platform.spi.validation.ValidationSummary;
import org.xml.sax.InputSource;

/**
 * The <code>XMLValidator<code> interface contains behavior for common XML validation operations the platform. Implementations of this interface
 * may be in addition to the XML transcoding functionalities provided by the platform. The {@link XMLTranscoder} and this interface may have some
 * funcitonality overlap.
 * 
 * @author Regunath B
 * @version 1.0, 25/05/2012
 */

public interface XMLValidator {

	/**
	 * Validates the XML read from the specified input source using the schema provided
	 * @param input XML input source
	 * @param schema the XML Schema object
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(InputSource input, Schema schema) throws XMLDataException;
	
	/**
	 * Validates the XML read from the specified input source using the schema read from the File provided
	 * @param input XML input source
	 * @param schema the File to read the XML Schema object from
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(InputSource input, File schema) throws XMLDataException ;
	
	/**
	 * Validates the XML read from the specified input string using the schema provided
	 * @param input XML string
	 * @param schema the XML Schema object
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(String input, Schema schema) throws XMLDataException ;
	
	/**
	 * Validates the XML read from the specified input string using the schema read from the File provided
	 * @param input XML input string
	 * @param schema the File to read the XML Schema object from
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(String input, File schema) throws XMLDataException ;
	
	/**
	 * Validates the XML read from the specified input string using the schema source provided
	 * @param input XML string
	 * @param schema the XML schema Source object
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(String input, Source schema) throws XMLDataException ;

	/**
	 * Validates the XML read from the specified input string using the schema source(s) provided
	 * @param input XML string
	 * @param schema the XML schema Source object(s)
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(String input, Source[] schema) throws XMLDataException ;

	/**
	 * Validates the XML read from the specified input source using the schema source(s) provided
	 * @param input XML string
	 * @param schema the XML schema Source object(s)
	 * @return ValidationSummary containing the validation results
	 * @throws XMLDataException in case of validation errors
	 */
	public ValidationSummary validate(InputSource input, Source[] schema) throws XMLDataException ;
	
	/**
	 * Constructs and returns the Schema object for a set of Source xsd's passed as parameter.
	 * This method is particularly useful for clients that repeatedly call validate methods on this interface where Schema instances can be
	 * compiled once, cached and returned for successive calls.
	 * @param schema the XSD Source(s)
	 * @return XML Schema object compiled from the specified source(s)
	 * @throws XMLDataException in case of Schema compilation
	 */
	public Schema getSchemaFromSource(Source[] schema) throws XMLDataException ;
	
}
