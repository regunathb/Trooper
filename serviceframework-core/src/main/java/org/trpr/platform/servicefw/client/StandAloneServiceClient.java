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
 * The <code> StandAloneServiceClient </code> may be used for testing services It expects three arguments:
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
	
	/**
	 * Main method test the service in standalone nature. It expects three program arguments
     * 
	 * @param args args[0] - Bootstrap configuration path 
	 *             args[1] - Service Name
	 *             args[2] - Service Request file path             
	 * @throws PlatformException 
	 */
	public static void main(String[] args) throws PlatformException {

		//validate the service information
		if(args.length < 2) {
			LOGGER.error("Service information is not sufficient. bootstrap config path, service name and service" +
					"request file path are required parameters");
			throw new PlatformException("Service information is not sufficient");
		}
	
		//	get Service information
		String bootstrapConfigPath = args[0];
		String serviceName = args[1];
		String serviceRequestFileName = args[2];
		
		ServiceResponse<? extends PlatformServiceResponse> serviceResponse = null;
		
		// boot the uid server application
		Bootstrap bootstrap = new Bootstrap();
		// we use System.out as the logging would not have been configured until the end of bootstrap
		System.out.println(("Bootstrap Config Path: " + bootstrapConfigPath));
		bootstrap.init(bootstrapConfigPath);
		
		try { 
			// Get the XML request in 
			String requestXML = new FileUtils().readFromFile(serviceRequestFileName);
			
			// unmarshall XML String
			PlatformServiceRequest platformServiceRequest = (PlatformServiceRequest)new XMLTranscoderImpl().unmarshal(requestXML,PlatformServiceRequest.class);
	
			// log service request information
			LOGGER.debug("Service Name : " + serviceName);
			LOGGER.debug("Service Version: " + platformServiceRequest.getVersion());
			LOGGER.debug("Service Request: \n" + requestXML);
			
			// invoke Service
			ServiceRequest<? extends PlatformServiceRequest> serviceRequest = new ServiceRequestImpl<PlatformServiceRequest>(platformServiceRequest, serviceName,platformServiceRequest.getVersion());
			serviceResponse = new BrokerFactory().getBroker(new ServiceKeyImpl(serviceName, platformServiceRequest.getVersion())).invokeService(serviceRequest);
		    PlatformServiceResponse platformServiceResponse = serviceResponse.getResponseData();
			
		    // Marshall java object
			String responseXML = new XMLTranscoderImpl().marshal(platformServiceResponse);
			LOGGER.debug(serviceName + " Response: \n" + responseXML);
			
			// write response in web browser
			writeResponseinBrowser(serviceName, responseXML);
			
		} catch(IOException e) {
			LOGGER.error("IOException thrown while reading the file " + serviceRequestFileName, e);
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
	private static void writeResponseinBrowser(String serviceName, String responseXML) throws PlatformException {
		String serviceResponseFilePath = System.getProperty("java.io.tmpdir") + serviceName 
		                                 + SERVICE_RESPONSE_XML_FILE_SUFFIX;
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
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
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
