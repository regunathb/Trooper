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

package org.trpr.platform.servicefw.impl;

import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * <code>ServiceResponseImpl<code> is an implementation of the {@link ServiceResponse}. This object is an envelope over the actual service response and
 * contains summary level information on service execution outcome such as status and supplementary information.
 * The service response is quite rich in information and is one of the sub-types of {@link PlatformServiceResponse}
 * 
 * @author Regunath B
 * @version 1.0, 14/08/2012
 * 
 */
public class ServiceResponseImpl<T> implements ServiceResponse<T> {

	/**
	 * serial version UID 
	 */
	private static final long serialVersionUID = 1096245534910070429L;

	/**
	   * The status code of this Service Response. This is used to indicate the
	   * result of the operation requested by a client. Ex. FAILURE, SUCCESS etc.
	   */
	  private String statusCode;

	  /**
	   * The status message describing the statusCode of this response. This may
	   * contain some details for the status code of response code.
	   *
	   */
	  private String statusMessage;

	  /**
	   * The supplementary status code which will be used in addition to status
	   * code to define any additional status conditions.
	   */
	  private String supplementaryCode;

	  /**
	   * The supplementary message in addition to status message. This will contain
	   * additional information regarding status codes and operation status messages.
	   */
	  private String supplementaryMessage;

	  /**
	   * This will hold all the data that is to be sent back in response to the
	   * request received from any calling application/client.
	   */
	  private T responseData;

	  /**
	   * Constructor to initialize the Response data
	   * @param code The default status code to initialize the response
	   */
	  public ServiceResponseImpl(String code) {
	    super();
	    statusCode = code;
	  }

	  /**
	   * Constructor to initialize the Response data
	   */
	  public ServiceResponseImpl() {
	    super();
	  }


	  /**
	   * Get the response data for this response
	   * @return responseData
	   */
	  public T getResponseData() {
	    return responseData;
	  }

	  /**
	   * Set the response data for this response
	   * @param responseData The response data values to be sent
	   */
	  public void setResponseData(T responseData) {
	    this.responseData = responseData;
	  }

	  /**
	   * Get the status code for this response
	   * @return statusCode
	   */
	  public String getStatusCode() {
	    return statusCode;
	  }

	  /**
	   * Set the status for this response
	   * @param statusCode The status code of this response
	   */
	  public void setStatusCode(String statusCode) {
	    this.statusCode = statusCode;
	  }

	  /**
	   * Get the Supplementary Code for this response
	   * @return supplementaryCode
	   */
	  public String getSupplementaryCode() {
	    return supplementaryCode;
	  }

	  /**
	   * Set the supplementary code for this response.
	   * @param supplementaryCode
	   */
	  public void setSupplementaryCode(String supplementaryCode) {
	    this.supplementaryCode = supplementaryCode;
	  }

	  /**
	   * Get the supplementary message for this response if it is defined.
	   * @return The supplementary message
	   */
	  public String getSupplementaryMessage() {
	    return supplementaryMessage;
	  }

	  /**
	   * Set the supplementary message for this response.
	   * @param supplementaryMessage
	   */
	  public void setSupplementaryMessage(String supplementaryMessage) {
	    this.supplementaryMessage = supplementaryMessage;
	  }

	  /**
	   * Get status message for this response
	   * @return statusMessage The status message
	   */
	  public String getStatusMessage() {
		return statusMessage;
	  }

	  /**
	   * Set the status message for this response.
	   * @param statusMessage
	   */
	  public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	  }

}
