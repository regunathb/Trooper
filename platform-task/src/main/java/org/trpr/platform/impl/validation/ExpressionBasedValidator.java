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
package org.trpr.platform.impl.validation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.springframework.beans.PropertyAccessException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.spi.execution.ResultCode;
import org.trpr.platform.spi.execution.Severity;
import org.trpr.platform.spi.validation.ValidationResult;
import org.trpr.platform.spi.validation.Validator;

/**
 * The <code>ExpressionBasedValidator</code> in an implementation of the {@link Validator} interface. 
 * Provides a default implementation using the mvel {@link http://mvel.codehaus.org/} expression language interpreter/compiler. 
 * This class expects all expressions to be mvel compatible. Sub types may customize the default behavior. This class is intentionally not 
 * made abstract as it can be used as is for many scenarios due to the dynamic nature of the mvel library in evaluating expressions. 
 * Example validation expressions using the sample Person entity are :
 * 
 * <pre>
 * 		Length validation  						"firstName.length() < 10"		
 * 
 * 		Validates that the firstName is not null and less than 10 in length. The firstName field is accessed via getFirstName(). Note the length()
 * 		method includes parenthesis as it is not a property
 * 		Combination field validation   			"firstName.length() < 10 && lastName !=null"
 * 
 *      Nested property access					May be done using the array notation of [index].
 * <pre>
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */

public class ExpressionBasedValidator implements Validator {

	/**
	 * Default error message message for all instances of NPE during property
	 * access
	 */
	private static final String NPE_MESSAGE = "Unexpected null error encountered during validation";

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(ExpressionBasedValidator.class);
	
	/** Cache for compiled expressions */
	private static Map<String, Serializable> compiledExpressionsCache = new HashMap<String, Serializable>();

	/** String that maybe be used to identify the input field in UI, service request etc.*/
	private String label;
	
	/** Variables used to return validation messages*/
	private String messageId;
	private String defaultMessage;	

	/** The ParserContext object shared across all instances of this class*/
	private static final ParserContext CTX = new ParserContext();
	
	/** Static block to import all packages that are relevant and possibly used in expressions*/
	static {
		ExpressionBasedValidator.CTX.addPackageImport("java.util");
		ExpressionBasedValidator.CTX.addPackageImport("org.trpr");
	}
	
	/**
	 * No args constructor for vanilla use with no interface specific label to identify the input field or service
	 * request field.
	 */
	public ExpressionBasedValidator() {		
	}

	/**
	 * Constructor with default message for use when validation by this validator fails
	 * 
	 * @param defaultMessage the default message to use when validation fails
	 */
	public ExpressionBasedValidator(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	/**
	 * Constructor with messageId and default message. The specified message Id is used to retrieve the logged in user's locale specific message when
	 * this validator returns a failure during validation.
	 * 
	 * @param messageId message identifier for reporting validation error
	 * @param defaultMessage null or the default message to be used in case a locale specific message does not exist
	 */
	public ExpressionBasedValidator(String messageId, String defaultMessage) {
		this.messageId = messageId;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * Constructor with messageId, default message and a label that the caller may use to identify the input field in the UI or service request. 
	 * The specified message Id is used to retrieve the logged in user's locale specific message when this validator returns a failure 
	 * during validation. The specified label is returned in the validation result if any.
	 * 
	 * @param messageId message identifier for reporting validation error
	 * @param defaultMessage null or the default message to be used in case a locale specific message does not exist
	 */
	public ExpressionBasedValidator(String messageId, String defaultMessage,String label) {
		this.messageId = messageId;
		this.defaultMessage = defaultMessage;
		this.label = label;
	}
	
	/**
	 * Interface method implementation. Note that this validator handles  NullPointerException that may be thrown during expression evaluation i.e.
	 * when accessing property values defined in the expression and treats it as a validation failure with FATAL severity.
	 * 
	 * @see Validator#validate(String, ResultCode, Object)
	 */
	public ValidationResult[] validate(String mvelExpression, ResultCode resultCode, Object inputObject) {
		boolean mvelResult = false;
		try {
			Serializable compiled = ExpressionBasedValidator.compiledExpressionsCache.get(mvelExpression);
			if (compiled == null) { // no synchronization etc as race condition is not destructive and will only result in additional expression compiling cost
				compiled = MVEL.compileExpression(mvelExpression, CTX);
				ExpressionBasedValidator.compiledExpressionsCache.put(mvelExpression, compiled);
			}
			mvelResult = ((Boolean) MVEL.executeExpression(compiled,inputObject)).booleanValue();
		} catch (PropertyAccessException pae) {
			if (pae.getCause() instanceof NullPointerException) {
				LOGGER.warn("Null pointer exception occurred in property access during expression evaluation in validator",	pae.getCause());
				return new ValidationResult[] { new ValidationResult(Severity.FATAL, resultCode, NPE_MESSAGE, this.label) };
			} else {
				// throw it back as it could be a development time error in expression
				throw pae;
			}
		}		
		if (!mvelResult) {
			String message = this.defaultMessage;
			// TODO : get the local specific message using the messageID. Default message is currently used.
			return new ValidationResult[] { new ValidationResult(resultCode,message) };
		}
		return null;
	}
	
	/* == Start Java Bean setter methods == */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public void setDefaultMessage(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}	
	public void setLabel(String label) {
		this.label = label;
	}
	/* == End Java Bean setter methods == */

}
