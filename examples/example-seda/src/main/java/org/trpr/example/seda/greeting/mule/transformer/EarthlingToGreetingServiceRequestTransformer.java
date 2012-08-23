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
package org.trpr.example.seda.greeting.mule.transformer;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.example.model.service.greetingservice.GreetingServiceRequest;
import org.trpr.platform.servicefw.impl.ServiceRequestImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;

/**
 * The class <code>EarthlingToGreetingServiceRequestTransformer</code> is a Mule message transformer implementation used in the sample
 * Greeting Service implementation to transform the Earthling object into a Trooper service framework ServiceRequest object  
 * 
 * @author  Regunath B
 * @version 1.0, 23/08/2012
 */
public class EarthlingToGreetingServiceRequestTransformer<T extends GreetingServiceRequest> extends AbstractTransformer{
	
	/**
	 * Constants for service related attributes for this transformer
	 */
	private static final String SERVICE_NAME = "greetingService";
	private static final String SERVICE_VERSION = "1.0";
	
	/**
	 * Constructor for this class
	 */
	public EarthlingToGreetingServiceRequestTransformer() {
        super();
        this.registerSourceType(byte[].class);
        this.setReturnClass(ServiceRequest.class);
    }

    /**
     * Overriden superclass method. Converts the Earthling object to a ServiceRequestImpl object that contains a GreetingServiceRequest object
     * 
     * @see Earthling
     * @see ServiceRequestImpl
     * @see org.mule.transformer.AbstractTransformer#doTransform(java.lang.Object)
     */
    public Object doTransform(Object src, String encoding) throws TransformerException {
    	GreetingServiceRequest greetingServiceRequest = new GreetingServiceRequest();
    	try {
    		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream((byte[])src));
    		Earthling earthling = (Earthling)ois.readObject();
    		ois.close();
    		greetingServiceRequest.setEarthling(earthling);
    		greetingServiceRequest.setVersion(SERVICE_VERSION);
    	} catch (Exception e) {
    		throw new TransformerException(this, e);
    	}
    	return new ServiceRequestImpl<GreetingServiceRequest>(greetingServiceRequest, SERVICE_NAME,greetingServiceRequest.getVersion());
    }	
}
