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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.servicefw.security.CallContext;
import org.trpr.platform.servicefw.security.SecurityContext;
import org.trpr.platform.servicefw.spi.Header;
import org.trpr.platform.servicefw.spi.ServiceKey;
import org.trpr.platform.servicefw.spi.ServiceRequest;


/**
 * <code>ServiceRequestImpl<code> is an implementation of the {@link ServiceRequest}. This implementation is essentially an envelope containing
 * meta data for the service request such as the service key and a number of optional headers. Service request data is embedded into the request using
 * one of the derived types of {@link PlatformServiceRequest}
 * 
 * @author Regunath B
 * @version 1.0, 14/08/2012
 */
public class ServiceRequestImpl<T> implements ServiceRequest<T> {

	  /**
	   * The serial version UID
	   */
	  private static final long serialVersionUID = 769739121999890779L;
	
	  /**
	   * List of class Header to store all the headers.
	   */
	  private List<Header> headerList = new ArrayList<Header>();

	  /**
	   * The name of the platform service of this request.
	   * This is a required value to initialize this request 
	   */
	  private String serviceName;

	  /**
	   * The version of the platform service of this request.
	   * This is a required value to initialize this request 
	   */
	  private String serviceVersion;

	  /**
	   * Contains the data of the request to a service.
	   * This is a required value to initialize this request
	   */
	  private T requestData;
	  
	  /**
	   * Contains information about Security Context.
	   */
	  private SecurityContext securityContext;

	  /**
	   * The constructor to create the object with the required values. 
	   * @param data The data for this request.
	   * @param name The name of the service for this request
	   * @param version The version of the service for this request
	   */
	  public ServiceRequestImpl(T data, String name, String version) {
		  requestData = data;
		  serviceName = name;
		  serviceVersion = version;
		  CallContext callContext=CallContext.getCurrentCallContext();
		  this.securityContext = callContext;
	  }
	  
	  /**
	   * The constructor to create the object with the required values. 
	   * @param data The data for this request.
	   * @param name The service key
	   */
	  public ServiceRequestImpl(T data, ServiceKey serviceKey) {
		  requestData = data;
		  serviceName = serviceKey.getName();
		  serviceVersion = serviceKey.getVersion();
		  CallContext callContext=CallContext.getCurrentCallContext();
		  this.securityContext = callContext;
	  }
	  
	  /**
	   * Returns the request data of this request as an Object.
	   * @return the request data contained by this request.   
	   */
	  public T getRequestData() {
	    return requestData;
	  }

	  /**
	   * Sets the data for this request.
	   * @param requestData
	   */
	  public void setRequestData(T requestData) {
	    this.requestData = requestData;
	  }

	  /**
	   * Returns the service name of this request. This will be the name of the
	   * service which is using an instance of this request.
	   * @return the service name 
	   */
	  public String getServiceName() {
	    return serviceName;
	  }

	  /**
	   * Set the name of the service for this request.
	   * @param serviceName
	   */
	  public void setServiceName(String serviceName) {
	    this.serviceName = serviceName;
	  }

	  /**
	   * Returns the service version  of this request. This will be the version of the
	   * service which is using an instance of this request.
	   * @return the service name 
	   */
	  public String getServiceVersion() {
	    return serviceVersion;
	  }

	  /**
	   * Set the version of the service for this request.
	   * @param serviceVersion
	   */
	  public void setServiceVersion(String serviceVersion) {
	    this.serviceVersion = serviceVersion;
	  }

	  /**
	   * Returns the <code>SecurityContext</code> for this request.
	   * This will define the level of security under which this request can 
	   * execute.
	   * @see SecurityContext
	   */
	  public SecurityContext getSecurityContext() {
		return securityContext;
	  }
	  
	  /**
	   * Returns all headers in the list of headers.
	   * @return the aarray of headers
	   */
	  public Header[] getHeaders(){
		  return (Header[])headerList.toArray(new Header[headerList.size()]);
	  }
	  
	  /**
	   * Returns the header object if the header with a specified key is present. 
	   * @return the header object
	   */
	  public Header getHeaderByKey(String key){
		  Iterator<Header> iter = headerList.iterator();
		  while(iter.hasNext()){
			  Header header = iter.next();
			  if((header.getKey()).equals(key)){
				  return header;
			  }
		  }
		  return null;
	  }
	  
	  /**
	   * Adds an array of header objects to a header list.
	   * @param headers The array of header objects to be added to the list
	   */
	  public void addHeaders(Header[] headers){
		  headerList.addAll( Arrays.asList(headers));
	  }
	  
}
