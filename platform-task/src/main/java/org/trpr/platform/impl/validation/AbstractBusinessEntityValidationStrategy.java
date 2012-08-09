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

import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.spi.execution.ResultCode;
import org.trpr.platform.spi.execution.Severity;
import org.trpr.platform.spi.validation.BusinessEntityValidationStrategy;
import org.trpr.platform.spi.validation.ValidationResult;
import org.trpr.platform.spi.validation.ValidationSummary;

/**
 * The <code>AbstractBusinessEntityValidationStrategy</code> is an implementation of the {@link BusinessEntityValidationStrategy}
 * Provides methods that may be common to all business entity validations such as vetoing validation
 * when prior validation results of severity FATAL have been encountered.
 * 
 * @see BusinessEntityValidationStrategy
 * 
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */

public abstract class AbstractBusinessEntityValidationStrategy implements BusinessEntityValidationStrategy {
	
	/** Property boundary start identifiers in Mvel expressions*/
	private static final String[] MVEL_PROPERTY_BOUNDARY_START = new String[] {".", "[" };

	/** The validator */
	private ExpressionBasedValidator validator = new ExpressionBasedValidator();

	/** The ValidationSummary instance to be returned at the end of validation*/
	protected ValidationSummary validationSummary = new ValidationSummary();
	
	/** The BusinessEntity instance that this validator validates*/
	protected BusinessEntity entity;
	
	/**
	 * Default Constructor.
	 */
	public AbstractBusinessEntityValidationStrategy() {
		
	}
	/**
	 * Constructor for this class
	 * @param entity the BusinessEntity validated by this validator
	 */
	public AbstractBusinessEntityValidationStrategy(BusinessEntity entity) {
		this.entity = entity;
	}
	
	/**
	 * Set the ExpressionBasedValidator to use
	 * @param validator ExpressionBasedValidator
	 */
	public void setValidator(ExpressionBasedValidator validator) {
		this.validator = validator;
	}
	
	/**
	 * Interface method implementation. Calls the abstract {@link AbstractBusinessEntityValidationStrategy#validate()} method.
	 * @see BusinessEntityValidationStrategy#validate(BusinessEntity)
	 */
	public ValidationSummary validate(BusinessEntity entity) {
		this.entity = entity;
		return validate();
	}
	
	/**
	 * Validates the associated BusinessEntity
	 * @return ValidationSummary containing validation results
	 */
	protected abstract ValidationSummary validate();

	/**
	 * Returns the results of validation on the associated BusinessEntity
	 * @return ValidationSummary containing all the validation results
	 */
	protected ValidationSummary getValidationResults() {
		return this.validationSummary;
	}

	/* == Start Convenience methods to call validation and continue even if there are errors==*/
	/**
	 * Validates the associated BusinessEntity using the specified expression. Continues with validation even if validation errors are encountered.
	 * @param expression the mvel expression to be evaluated on the associated BusinessEntity
	 * @param resultCode null or the code to be used uniquely identify the validation result 
	 */
	protected void validate(String mvelExpression, ResultCode result) {
		checkAndValidate(mvelExpression, result, BusinessEntityValidationStrategy.CONTINUE_VALIDATION, null, null, null);
	}
	
	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, String)}. Additionally the specified default message is used in case
	 * of validation errors
	 * @param expression
	 * @param validationInfluence
	 * @param defaultMessage the default message to use in case of validation failures. Note that this message is not localized
	 */
	protected void validate(String mvelExpression, ResultCode resultCode, String defaultMessage) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.CONTINUE_VALIDATION, defaultMessage);		
	}

	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, String, String)}. Additionally retrieves the localized message from
	 * the a suitable i18n service in case of validation errors  
	 * @param expression
	 * @param validationInfluence
	 * @param messageId the message Id to use to retrieve the Locale specific message for the currently logged in user 
	 * @param defaultMessage
	 */
	protected void validate(String mvelExpression, ResultCode resultCode, String messageId, String defaultMessage) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.CONTINUE_VALIDATION, messageId, defaultMessage);				
	}
	
	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, String, String, String)}. Additionally returns the specified label in
	 * the ValidationResult that is returned through the ValidationSummary maintained by this class.
	 * @see ValidationResult
	 * @param expression
	 * @param validationInfluence
	 * @param messageId
	 * @param defaultMessage
	 * @param label the label that may be optionally used by calling classes to locate the field in the client interface - e.g. UI or service request
	 */
	protected void validate(String mvelExpression, ResultCode resultCode, String messageId, String defaultMessage, String label) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.CONTINUE_VALIDATION, messageId, defaultMessage, label);						
	}
	/* == End Convenience methods to call validation and continue even if there are errors==*/
	
	/* == Start Convenience methods to call validation and veto subsequent execution if there are errors==*/
	/**
	 * Validates the associated BusinessEntity using the specified expression. Vetoes the validation if validation errors are encountered
	 * @see AbstractBusinessEntityValidationStrategy#validate(String, String)
	 */
	protected void vetoValidate(String mvelExpression, ResultCode resultCode) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.VETO_VALIDATION);		
	}

	/**
	 * Vetoes the validation in case of validation errors.
	 * @see  AbstractBusinessEntityValidationStrategy#validate(String, String, String)
	 */
	protected void vetoValidate(String mvelExpression, ResultCode resultCode, String defaultMessage) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.VETO_VALIDATION, defaultMessage);				
	}	
	/**
	 * Vetoes the validation in case of validation errors.
	 * @see  AbstractBusinessEntityValidationStrategy#validate(String, String, String, String)
	 */
	protected void vetoValidate(String mvelExpression, ResultCode resultCode, String messageId, String defaultMessage) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.VETO_VALIDATION, messageId, defaultMessage);						
	}
	/**
	 * Vetoes the validation in case of validation errors.
	 * @see  AbstractBusinessEntityValidationStrategy#validate(String, String, String, String, String)
	 */
	protected void vetoValidate(String mvelExpression, ResultCode resultCode, String messageId, String defaultMessage, String label) {
		validate(mvelExpression, resultCode, BusinessEntityValidationStrategy.VETO_VALIDATION, messageId, defaultMessage, label);								
	}
	/* == End Convenience methods to call validation and veto subsequent execution if there are errors==*/
	
	/**
	 * Validates the associated BusinessEntity using the specified expression. The specified validationInfluence parameter determines execution of 
	 * further validation calls to this validator when this validation call returns a validation failure. In case of VETO_VALIDATION, subsequent
	 * calls are not executed and the validation summary contains results only until the validation call that vetoed further validation.
	 * @param expression the mvel expression to be evaluated on the associated BusinessEntity
	 * @param resultCode null or the code to be used uniquely identify the validation result 
	 * @param validationInfluence one of the BusinessEntityValidator values of CONTINUE_VALIDATION or VETO_VALIDATION
	 */
	private void validate(String mvelExpression, ResultCode resultCode, int validationInfluence) {		
		checkAndValidate(mvelExpression, resultCode, validationInfluence, null, null, null);
	}

	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, int)}. Additionally the specified default message is used in case
	 * of validation errors
	 * @param expression
	 * @param validationInfluence
	 * @param defaultMessage the default message to use in case of validation failures. Note that this message is not localized
	 */
	private void validate(String mvelExpression, ResultCode resultCode, int validationInfluence, String defaultMessage) {	
		checkAndValidate(mvelExpression, resultCode, validationInfluence,  null, defaultMessage, null);
	}
	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, int, String)}. Additionally retrieves the localized message from
	 * a suitable i18n service in case of validation errors  
	 * @param expression
	 * @param validationInfluence
	 * @param messageId the message Id to use to retrieve the Locale specific message for the currently logged in user 
	 * @param defaultMessage
	 */
	private void validate(String mvelExpression, ResultCode resultCode, int validationInfluence, String messageId, String defaultMessage) {		
		checkAndValidate(mvelExpression, resultCode, validationInfluence, messageId, defaultMessage, null);
	}
	/**
	 * Same behavior as {@link AbstractBusinessEntityValidationStrategy#validate(String, int, String)}. Additionally returns the specified label in
	 * the ValidationResult that is returned through the ValidationSummary maintained by this class.
	 * @see ValidationResult
	 * @param expression
	 * @param validationInfluence
	 * @param messageId
	 * @param defaultMessage
	 * @param label the label that may be optionally used by calling classes to locate the field in the client interface - e.g. UI or service request
	 */
	private void validate(String mvelExpression, ResultCode resultCode, int validationInfluence, String messageId, String defaultMessage, String label) {	
		checkAndValidate(mvelExpression, resultCode, validationInfluence, messageId, defaultMessage, label);
	}

	/**
	 * Helper method that first checks if there are errors (failures or veto business errors) in prior validations. Continues with execution
	 * only if there are no errors.
	 */
	private void checkAndValidate(String mvelExpression, ResultCode resultCode, int validationInfluence, String messageId, String defaultMessage, String label) {
		
		if (!this.validationSummary.hasFatalValidationErrors()) {
			
			validator.setMessageId(messageId);
			validator.setDefaultMessage(defaultMessage);
			
			// try to determine a label using the passed in Mvel expression. 
			// Uses the first property identified as the label. For e.g. "firstName" in the Mvel expression : "firstName.length() < 10"
			if (label == null) {
				int propertyBoundaryStartIndex = -1;
				for (String boundary : MVEL_PROPERTY_BOUNDARY_START) {
					propertyBoundaryStartIndex = mvelExpression.indexOf(boundary);
					if (propertyBoundaryStartIndex > -1) {
						label = mvelExpression.substring(0, propertyBoundaryStartIndex);
						break;
					}
				}
				if (propertyBoundaryStartIndex == -1) { // no navigation, the property is the expression itself
					label = mvelExpression;
				}
			}
			validator.setLabel(label);
			
			ValidationResult[] validationResults = validator.validate(mvelExpression, resultCode, this.entity);
			if (validationInfluence == BusinessEntityValidationStrategy.VETO_VALIDATION) {
				// This validation run should stop further validation in case of failures or business validation errors as this call
				// has signaled a veto
			    for (ValidationResult result : validationResults) {
			    	if (result.getSeverity().getCode() >= Severity.ERROR.getCode()) {
			    		// signal a veto by increasing the severity to FATAL
			    		result.setSeverity(Severity.FATAL);
			    	}
			    }				
			}
			if (validationResults != null) {
				this.validationSummary.addResults(validationResults);
			}
		}
	}	
}
