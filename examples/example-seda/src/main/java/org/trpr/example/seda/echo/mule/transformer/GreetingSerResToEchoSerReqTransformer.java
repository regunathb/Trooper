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

package org.trpr.example.seda.echo.mule.transformer;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.example.model.service.echoservice.EchoServiceRequest;
import org.trpr.example.model.service.greetingservice.GreetingServiceRequest;
import org.trpr.example.model.service.greetingservice.GreetingServiceResponse;
import org.trpr.platform.servicefw.impl.ServiceRequestImpl;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;

/**
 * The class <code>GreetingSerResToEchoSerReqTransformer</code> is a Mule message transformer implementation used in the sample
 * Echo Service mule model implementation to transform the Trooper service framework ServiceResponse object into a ServiceRequest.
 *   
 * @author  Regunath B
 * @version 1.0, 23/08/2012
 */
public class GreetingSerResToEchoSerReqTransformer<P extends GreetingServiceRequest, T extends GreetingServiceResponse, S extends EchoServiceRequest>  extends AbstractTransformer{
	
	/**
	 * Constants for service related attributes for this transformer
	 */
	private static final String SERVICE_NAME = "echoService";
	private static final String SERVICE_VERSION = "1.0";
	
	/**
	 * Constructor for this class
	 */
	public GreetingSerResToEchoSerReqTransformer() {
        super();
        this.registerSourceType(byte[].class);
        this.setReturnClass(ServiceRequest.class);
    }

    /**
     * Overriden superclass method. Converts the HelloPersonServiceResponse object to a ServiceRequestImpl object that contains a EchoServiceRequest object
     * 
     * @see Earthling
     * @see ServiceRequestImpl
     * @see org.mule.transformer.AbstractTransformer#doTransform(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	public Object doTransform(Object src, String encoding) throws TransformerException {
    	EchoServiceRequest echoServiceRequest = new EchoServiceRequest();
    	try {
    		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream((byte[])src));
    		ServiceResponseImpl<T> serviceResponse = (ServiceResponseImpl<T>)ois.readObject();
    		ois.close();
    		T greetingServiceResponse = (T)serviceResponse.getResponseData();
    		Earthling earthling = ((P)greetingServiceResponse.getPlatformServiceRequest()).getEarthling();
    		echoServiceRequest.setEarthling(earthling);
    		echoServiceRequest.setVersion(SERVICE_VERSION);
    	} catch (Exception e) {
    		throw new TransformerException(this, e);
    	}
    	return new ServiceRequestImpl<EchoServiceRequest>(echoServiceRequest, SERVICE_NAME,echoServiceRequest.getVersion());
    }	
}
