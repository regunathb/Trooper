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
package org.trpr.platform.servicefw.impl.validation;

import org.trpr.platform.impl.validation.AbstractBusinessEntityValidationStrategy;
import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.model.common.EntityIdentifierType;
import org.trpr.platform.service.model.common.error.BusinessEntityErrorDetail;
import org.trpr.platform.service.model.common.error.ErrorDetail.ErrorBlock;
import org.trpr.platform.service.model.common.error.ErrorSummary;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.service.model.common.status.Status;
import org.trpr.platform.service.model.common.warning.BusinessEntityWarningDetail;
import org.trpr.platform.service.model.common.warning.WarningDetail.WarningBlock;
import org.trpr.platform.service.model.common.warning.WarningSummary;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.spi.execution.Severity;
import org.trpr.platform.spi.validation.ValidationResult;
import org.trpr.platform.spi.validation.ValidationSummary;

/**
 * The <code>ValidationServiceResponseGenerator</code> is a AbstractBusinessEntityValidationStrategy sub-type that provides convenience
 * methods to validate BusinessEntity data contained in Service requests and generate a service response
 * containing validation errors
 * 
 * @see AbstractBusinessEntityValidationStrategy
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

public class ValidationServiceResponseGenerator<T extends PlatformServiceRequest, S extends PlatformServiceResponse> {
	
	/** Index of the first BusinessEntityErrorDetail*/
	private static final int FIRST_INDEX = 0;

	/** ErrorSummary that contains validation errors if any*/
	private ErrorSummary errorSummary;
	/** WarningSummary that contains execution warnings if any*/
	private WarningSummary warningSummary;

	/** The BusinessEntity that is to be validated*/
	private BusinessEntity entity;
	
	/**
	 * Constructor for this class
	 * @param entity the BusinessEntity instance from the service request
	 */
	public ValidationServiceResponseGenerator(BusinessEntity entity) {
		this.entity = entity;
	}
	
	/**
	 * Creates and populates a ServiceResponse with error status and messages based on the validation errors contained in the specified
	 * ValidationSummary. Sets the appropriate objects in the specified PlatformServiceResponse as well.
	 * @param validationSummary the ValidationSummary containing error results
	 * @param platformResponse the PlatformServiceResponse to populate with error details
	 * @param platformRequest the PlatformServiceRequest to send back in the response
	 * @return
	 */
	public ServiceResponse<S> populateResponseFromValidationResults(ValidationSummary validationSummary, 
			S platformResponse, T platformRequest) {
			return populateResponseFromValidationResults(validationSummary, platformResponse, platformRequest,  false);		
	}
	
	
	/**
	 * Creates and populates a ServiceResponse with error status and messages based on the validation errors contained in the specified
	 * ValidationSummary. Sets the appropriate objects in the specified PlatformServiceResponse as well.
	 * @param validationSummary the ValidationSummary containing error results
	 * @param platformResponse the PlatformServiceResponse to populate with error details
	 * @param platformRequest the PlatformServiceRequest to send back in the response
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServiceResponse<S> populateResponseFromValidationResults(ValidationSummary validationSummary, 
			S platformResponse, T platformRequest,  boolean setSupplementaryCodes) {
		
		Status status = new Status();
		status.setCode(ServiceFrameworkConstants.FAILURE_STATUS_CODE);
		status.setMessage(ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE);
		platformResponse.setStatus(status);
		
		ServiceResponseImpl serviceResponse = new ServiceResponseImpl(String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE));
		serviceResponse.setResponseData(platformResponse);
		ValidationResult[] validationResults = validationSummary.getAllResults();
		for (ValidationResult result : validationResults) {
			if (result.getSeverity() == Severity.ERROR) {
				addErrorBlock(getErrorBlock(result.getResultCode(), result.getCodeName(), result.getLabel(), result.getMessage()));
				if(setSupplementaryCodes){
					serviceResponse.setSupplementaryCode(String.valueOf(result.getResultCode()));
				}
			} else if (result.getSeverity() == Severity.WARNING) {
				addWarningBlock(getWarningBlock(result.getResultCode(), result.getCodeName(), result.getLabel(), result.getMessage()));
			}
		}
		
		platformResponse.setErrorSummary(this.errorSummary);
		platformResponse.setWarningSummary(this.warningSummary);
		platformResponse.setPlatformServiceRequest(platformRequest);
		
		return serviceResponse;		
	}

	/**
	 * Creates and populates a ServiceResponse with error status and messages based on the validation errors contained in the specified
	 * ValidationSummary. Sets the appropriate objects in the specified PlatformServiceResponse as well.
	 * @param validationSummary the ValidationSummary containing error results
	 * @param platformResponse the PlatformServiceResponse to populate with error details
	 * @param platformRequest the PlatformServiceRequest to send back in the response
	 * @return
	 */
	public ServiceResponse<S> populateResponseFromValidationResults(ValidationSummary[] validationSummaries, 
			S platformResponse, T platformRequest) {
		return populateResponseFromValidationResults(validationSummaries, 
				platformResponse, platformRequest, false);
	}
	
	/**
	 * Creates and populates a ServiceResponse with error status and messages based on the validation errors contained in the specified
	 * ValidationSummary. Sets the appropriate objects in the specified PlatformServiceResponse as well.
	 * @param validationSummary the ValidationSummary containing error results
	 * @param platformResponse the PlatformServiceResponse to populate with error details
	 * @param platformRequest the PlatformServiceRequest to send back in the response
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServiceResponse<S> populateResponseFromValidationResults(ValidationSummary[] validationSummaries, 
			S platformResponse, T platformRequest, boolean setSupplementaryCodes) {
		
		Status status = new Status();
		status.setCode(ServiceFrameworkConstants.FAILURE_STATUS_CODE);
		status.setMessage(ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE);
		platformResponse.setStatus(status);
		ServiceResponseImpl serviceResponse = new ServiceResponseImpl(String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE));
		serviceResponse.setResponseData(platformResponse);
		for (ValidationSummary validationSummary : validationSummaries) {
			ValidationResult[] validationResults = validationSummary.getAllResults();
			for (ValidationResult result : validationResults) {
				if (result.getSeverity() == Severity.ERROR) {
					addErrorBlock(getErrorBlock(result.getResultCode(), result.getCodeName(), result.getLabel(), result.getMessage()));
	                if(setSupplementaryCodes){
	                    serviceResponse.setSupplementaryCode(String.valueOf(result.getResultCode()));
	                }
				} else if (result.getSeverity() == Severity.WARNING) {
					addWarningBlock(getWarningBlock(result.getResultCode(), result.getCodeName(), result.getLabel(), result.getMessage()));
				}
			}
		}
		
		platformResponse.setErrorSummary(this.errorSummary);
		platformResponse.setWarningSummary(this.warningSummary);
		platformResponse.setPlatformServiceRequest(platformRequest);
		
		return serviceResponse;	
	}
	
	/**
	 * Adds the specified ErrorBlock to the ErrorSummary maintained by this class
	 * @param errorBlock the ErrorBlock to be added
	 */
	private void addErrorBlock(ErrorBlock errorBlock) {
		BusinessEntityErrorDetail businessEntityErrorDetail = null;
		if (this.errorSummary == null) {
			this.errorSummary = new ErrorSummary();
			businessEntityErrorDetail = new BusinessEntityErrorDetail();
			EntityIdentifierType entityIdentifier = new EntityIdentifierType();
			entityIdentifier.setUniqueId((long)entityIdentifier.hashCode());
			this.entity.setEntityIdentifier(entityIdentifier);
			businessEntityErrorDetail.setBusinessEntity(this.entity);
		    this.errorSummary.getBusinessEntityErrorDetail().add(businessEntityErrorDetail);
		}
		// get the first and only BusinessEntityErrorDetail
		businessEntityErrorDetail = this.errorSummary.getBusinessEntityErrorDetail().get(FIRST_INDEX);
	    businessEntityErrorDetail.getErrorBlock().add(errorBlock);
	}
	/**
	 * Adds the specified ErrorBlock to the ErrorSummary maintained by this class
	 * @param warningBlock the ErrorBlock to be added
	 */
	private void addWarningBlock(WarningBlock warningBlock) {
		BusinessEntityWarningDetail businessEntityWarnDetail = null;
		if (this.warningSummary == null) {
			this.warningSummary = new WarningSummary();
			businessEntityWarnDetail = new BusinessEntityWarningDetail();
			EntityIdentifierType entityIdentifier = new EntityIdentifierType();
			entityIdentifier.setUniqueId((long)entityIdentifier.hashCode());
			this.entity.setEntityIdentifier(entityIdentifier);
			businessEntityWarnDetail.setBusinessEntity(this.entity);
		    this.warningSummary.getBusinessEntityWarningDetail().add(businessEntityWarnDetail);
		}
		// get the first and only BusinessEntityErrorDetail
		businessEntityWarnDetail = this.warningSummary.getBusinessEntityWarningDetail().get(FIRST_INDEX);
	    businessEntityWarnDetail.getWarningBlock().add(warningBlock);
	}
	/** Helper method to create an ErrorBlock using specified parameters */
	private ErrorBlock getErrorBlock(int errorCode, String errorName, String fieldName,
			String errorMessage) {
		ErrorBlock errorBlock = new ErrorBlock();
		errorBlock.setErrorCode(errorCode);
		errorBlock.setErrorName(errorName);
		errorBlock.setFieldName(fieldName);
		errorBlock.setContent(errorMessage);
		return errorBlock;
	}	
	
	/** Helper method to create an WarningBlock using specified parameters */
	private WarningBlock getWarningBlock(int warningCode, String warningName, String fieldName,
			String warningMessage) {
		WarningBlock warnBlock = new WarningBlock();
		warnBlock.setWarningCode(warningCode);
		warnBlock.setWarningName(warningName);
		warnBlock.setFieldName(fieldName);
		warnBlock.setContent(warningMessage);
		return warnBlock;
	}	
}
