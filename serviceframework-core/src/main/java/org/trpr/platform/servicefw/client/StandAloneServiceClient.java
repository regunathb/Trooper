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
package org.trpr.platform.servicefw.client;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.util.FileUtils;
import org.trpr.platform.integration.impl.json.JSONTranscoderImpl;
import org.trpr.platform.integration.impl.xml.XMLTranscoderImpl;
import org.trpr.platform.runtime.impl.bootstrap.spring.Bootstrap;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.impl.BrokerFactory;
import org.trpr.platform.servicefw.impl.ServiceKeyImpl;
import org.trpr.platform.servicefw.impl.ServiceRequestImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code> StandAloneServiceClient </code> may be used for testing services. It expects the following arguments:
 * <pre>
 * 1. Bootstrap config location  
 * 2. Service Name e.g. GreetingsService
 * 3. Service Request File Path e.g. /Users/regunathb/tropper/requests/GreetingsServiceRequest.xml
 * <pre>
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 * 
 */
public class StandAloneServiceClient {

	/** The Log instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(StandAloneServiceClient.class);
	
	/** service response file suffix */
	private static final String SERVICE_RESPONSE_XML_FILE_SUFFIX = "Response.xml";
	private static final String SERVICE_RESPONSE_JSON_FILE_SUFFIX = "Response.json";
	
	/**
	 * Main method to test the service in standalone nature. It expects the following arguments
     * 
	 * @param args args[0] - Bootstrap configuration path 
	 *             args[1] - Service Name
	 *             args[2] - Service Request class name 
	 *             args[3] - Service Response class name            
	 *             args[4] - Service Request file path            
	 * @throws PlatformException 
	 */
	public static void main(String[] args) throws PlatformException {

		//validate the service information
		if(args.length < 4) {
			LOGGER.error("Service information is not sufficient. bootstrap config path, service name, service request class name, service response class name and service " +
					"request file path are required parameters");
			throw new PlatformException("Service information is not sufficient");
		}
	
		//	get Service information
		String bootstrapConfigPath = args[0];
		String serviceName = args[1];
		String serviceRequestClass = args[2];
		String serviceResponseClass = args[3];
		String serviceRequestFileName = args[4];
		
		ServiceResponse<? extends PlatformServiceResponse> serviceResponse = null;
		
		// boot the Trooper runtime
		Bootstrap bootstrap = new Bootstrap();
		// we use System.out as the logging would not have been configured until the end of bootstrap
		System.out.println(("Bootstrap Config Path: " + bootstrapConfigPath));
		bootstrap.init(bootstrapConfigPath);
		
		try { 
			// Get the XML/JSON request in 
			String requestContents = new FileUtils().readFromFile(serviceRequestFileName);
			
			// unmarshall XML/JSON String
			Class requestClazz = Class.forName(serviceRequestClass);
			
			// get the request getter method
			Method[] requestMethods = requestClazz.getDeclaredMethods();
			Method requestGetterMethod = null;
			for (Method m : requestMethods) {
				if (m.getName().startsWith("get") && m.getName().indexOf("ServiceRequest") > 0) {
					requestGetterMethod = m;
					break;
				}
			}
			
			Object requestContentsObject = serviceRequestFileName.endsWith(".xml") ? new XMLTranscoderImpl().unmarshal(requestContents,requestClazz) : 
				new JSONTranscoderImpl().unmarshal(requestContents,requestClazz);
			PlatformServiceRequest platformServiceRequest = (PlatformServiceRequest)requestGetterMethod.invoke(requestContentsObject, new Object[0]);
	
			// log service request information
			LOGGER.debug("Service Name : " + serviceName);
			LOGGER.debug("Service Version: " + platformServiceRequest.getVersion());
			LOGGER.debug("Service Request Class: " + serviceRequestClass);
			LOGGER.debug("Service Request: \n" + requestContents);
			
			// invoke Service
			ServiceRequest<? extends PlatformServiceRequest> serviceRequest = new ServiceRequestImpl<PlatformServiceRequest>(platformServiceRequest, serviceName,platformServiceRequest.getVersion());
			serviceResponse = new BrokerFactory().getBroker(new ServiceKeyImpl(serviceName, platformServiceRequest.getVersion())).invokeService(serviceRequest);
			
		
			Object responseContentsObject = null;
			responseContentsObject = Class.forName(serviceResponseClass).newInstance(); 
			
			// get the request getter method
			Class responseClazz = Class.forName(serviceResponseClass);
			Method[] responseMethods = responseClazz.getDeclaredMethods();
			Method responseSetterMethod = null;
			for (Method m : responseMethods) {
				if (m.getName().startsWith("set") && m.getName().indexOf("ServiceResponse") > 0) {
					responseSetterMethod = m;
					break;
				}
			}
			// set the PlatformServiceResponse on the response XML object
			responseSetterMethod.invoke(responseContentsObject, serviceResponse.getResponseData());

		    // Marshall java object
			String responseContents = serviceRequestFileName.endsWith(".xml") ? new XMLTranscoderImpl().marshal(responseContentsObject) : new JSONTranscoderImpl().marshal(responseContentsObject);
			LOGGER.debug(serviceName + " Response: \n" + responseContents);
			
			// write response in web browser
			writeResponseinBrowser(serviceName, responseContents,serviceRequestFileName.endsWith(".xml"));
			
		} catch(Throwable e) {
			LOGGER.error("Exception running the service", e);
			throw new PlatformException(e);
		} finally {
			try {
				bootstrap.destroy();
			} catch (Exception e) {
				LOGGER.error("Exception thrown while destroying Platform ", e);
				throw new PlatformException(e);
			}
		}
	}
	
	/**
	 * Write Service response XML in web browser. 
	 * 
	 * @param responseXML service reponse in xml format
	 * @throws PlatformException throw while writing service response in temp directory
	 */
	private static void writeResponseinBrowser(String serviceName, String responseXML, boolean isXML) throws PlatformException {
		String serviceResponseFilePath = System.getProperty("java.io.tmpdir") + serviceName 
		                                 + (isXML ? SERVICE_RESPONSE_XML_FILE_SUFFIX : SERVICE_RESPONSE_JSON_FILE_SUFFIX);
		try {
			// write response XML into temp directory
			FileWriter writer = new FileWriter(serviceResponseFilePath);
			writer.write(responseXML);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.error("IOException thrown while writing service response in temp directory ", e);
			throw new PlatformException(e);
		}
		// open service response XML file in web browser
		openURL(serviceResponseFilePath);
	}
	
	/**
	 * Helper method to launch a browser for given URL
	 * 
	 * @param url file path
	 * @throws PlatformException thrown while open web browser for given URL 
	 */
	private static void openURL(String url) throws PlatformException {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Runtime.getRuntime().exec("open " + url);
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(
							new String[] { "which", browsers[count] })
							.waitFor() == 0)
						browser = browsers[count];
				if (browser == null)
					throw new Exception("Could not find web browser");
				else
					Runtime.getRuntime().exec(new String[] { browser, url });
			}
		} catch (Exception e) {
			LOGGER.error("Exception thrown while launch a browser. ", e);
			throw new PlatformException(e);
		}
	}	
}
