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
package org.trpr.platform.servicefw.impl.spring.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;


import org.apache.commons.io.IOUtils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.integration.impl.xml.XMLTranscoderImpl;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.impl.BrokerFactory;
import org.trpr.platform.servicefw.impl.ServiceKeyImpl;
import org.trpr.platform.servicefw.impl.ServiceRequestImpl;
import org.trpr.platform.servicefw.impl.ServiceStatisticsGatherer;
import org.trpr.platform.servicefw.impl.spring.admin.ConfigurationService;
import org.trpr.platform.servicefw.spi.ServiceKey;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>ServiceController</code> class is a Spring MVC Controller that displays Service Metrics
 * gathered from {@link ServiceStatisticsGatherer} and displays them on the view. Also provides
 * functionality to test the deployed services
 * 
 * @author devashishshankar
 * @version 1.0, 03 March 2013 
 */
@Controller
public class ServiceController {

	/** The {@link ServiceStatisticsGatherer} object which is used to collect metrics */
	private ServiceStatisticsGatherer serviceStatisticsGatherer;

    /** ConfigurationService instance for loading and modifying configuration files */
    private ConfigurationService configurationService;

	/**
	 * Finds the serviceName from the request URL
	 */
	@ModelAttribute("services")
	public String getServiceName(HttpServletRequest request) {
		String path = request.getServletPath();
		int index = path.lastIndexOf("services/") + 9;
		if (index >= 0 && index<path.length()) {
			path = path.substring(index);
		}
		return path;
	}

    /**
     * Controller for index(homepage)
     */
    @RequestMapping(value = {"/services","/"}, method = RequestMethod.GET)
    public String services(ModelMap model, HttpServletRequest request) {
        ServiceStatistics[] serviceStatisticsAsArray = this.serviceStatisticsGatherer.getStats();
        model.addAttribute("serviceInfo",serviceStatisticsAsArray);
        if(request.getServletPath().endsWith(".json")) {
            return "services-json";
        }
        return "services";
    }

    /**
     * Controller for index(homepage)
     */
    @RequestMapping(value = {"/configuration"}, method = RequestMethod.GET)
    public String configuration(ModelMap model, HttpServletRequest request) {
        ServiceStatistics[] serviceStatisticsAsArray = this.serviceStatisticsGatherer.getStats();
        model.addAttribute("serviceInfo",serviceStatisticsAsArray);
        return "configuration";
    }

    /** Controller for Test page */
    @RequestMapping(value = {"/test/services/{serviceName}"}, method = RequestMethod.GET)
    public String test(ModelMap model, @ModelAttribute("services") String serviceName) {
        ServiceStatistics[] serviceStatisticsAsArray = this.serviceStatisticsGatherer.getStats();
        for(ServiceStatistics statistics:serviceStatisticsAsArray) {
            if(statistics.getServiceName().equalsIgnoreCase(serviceName)) {
                model.addAttribute("serviceInfo",statistics);
            }
        }
        return "test";
    }

    /** Controller for Modify page */
    @RequestMapping(value = {"/modify/services/{serviceName}"}, method = RequestMethod.GET)
    public String modify(ModelMap model, @ModelAttribute("services") String serviceName) {
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("XMLFileContents",getContents(this.configurationService.getConfig(this.constructServiceKey(serviceName)))) ;
        return "modifyConfig";
    }

    /** Controller for View Config page */
    @RequestMapping(value = {"/reInit/services/{serviceName}"}, method = RequestMethod.GET)
    public String reInit(ModelMap model, @ModelAttribute("services") String serviceName) {
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("XMLFileContents",getContents(this.configurationService.getConfig(this.constructServiceKey(serviceName)))) ;
        try {
            this.configurationService.reloadConfig(this.constructServiceKey(serviceName));
        } catch(Exception e) {
            model.addAttribute("XMLFileError", "Exception while Reiniting. This could be due to external changes to config files");
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            model.addAttribute("LoadingError", errors.toString());
            if(errors.toString()==null) {
                model.addAttribute("LoadingError", "Unexpected error");
            }
            model.addAttribute("XMLFileContents",getContents(this.configurationService.getConfig(this.constructServiceKey(serviceName))));

        }
        model.addAttribute("SuccessMessage","Succesfully reInited service configuration");
        return "viewConfig";
    }


    /** Controller for deploy page (called from modify) */
    @RequestMapping(value = {"/deploy/services/{serviceName}"}, method = RequestMethod.POST)
    public String deploy(ModelMap model, @ModelAttribute("services") String serviceName,
                         @RequestParam(defaultValue = "") String XMLFileContents) {
        model.addAttribute("serviceName", serviceName);

        try {
            this.configurationService.modifyConfig(this.constructServiceKey(serviceName),new ByteArrayResource(XMLFileContents.getBytes()));
        } catch (Exception e) {
            model.addAttribute("XMLFileError", "Unable to deploy file");
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            model.addAttribute("LoadingError", errors.toString());
            if(errors.toString()==null) {
                model.addAttribute("LoadingError", "Unexpected error");
            }
            model.addAttribute("XMLFileContents",getContents(this.configurationService.getConfig(this.constructServiceKey(serviceName))));
            return "modifyConfig";
        }
        //Loading success
        model.addAttribute("SuccessMessage", "Successfully Deployed the new Service Configuration");
        Resource serviceConfigFile = this.configurationService.getConfig(this.constructServiceKey(serviceName));
        model.addAttribute("XMLFileContents",getContents(serviceConfigFile));
        try {
            model.addAttribute("XMLFileName",serviceConfigFile.getURI());
        } catch (IOException e) {
            model.addAttribute("XMLFileName","File not found");
        }
        return "viewConfig";
    }

    /** Controller for View Config page */
    @RequestMapping(value = {"/viewConfig/services/{serviceName}"}, method = RequestMethod.GET)
    public String viewConfig(ModelMap model, @ModelAttribute("services") String serviceName) {
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("XMLFileContents",getContents(this.configurationService.getConfig(this.constructServiceKey(serviceName)))) ;
        return "viewConfig";
    }

	/** Controller which gets the Service details, runs the service and displays the output */
	@RequestMapping(value = {"/execute/services/{serviceName}"}, method = RequestMethod.POST)
	public String execute_service(ModelMap model, @ModelAttribute("services") String serviceName,
			@RequestParam(required=true) String serviceRequestClass, 
			@RequestParam(required=true) String serviceResponseClass, 
			@RequestParam(required=true) String XMLFileContents) {

		ServiceResponse<? extends PlatformServiceResponse> serviceResponse = null;
		//Code for executing the service
		try { 
			// Get the XML/JSON request in 
			String requestContents = XMLFileContents.trim();
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
			Object requestContentsObject =new XMLTranscoderImpl().unmarshal(requestContents,requestClazz);
			PlatformServiceRequest platformServiceRequest = (PlatformServiceRequest)requestGetterMethod.invoke(requestContentsObject, new Object[0]);
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
			String responseContents = new XMLTranscoderImpl().marshal(responseContentsObject);
			// write response in web browser
			model.addAttribute("response", responseContents);
		} catch(Throwable e) {
			throw new PlatformException(e);
		}
		return "response";
	}


    /**
     * Helper method that gets the contents of a <code>Resource</code> in a single String
     * @param resource Resource to be read
     * @return Contents as a <code>String<code/>
     */
    private static String getContents(Resource resource) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(resource.getInputStream(), writer, "UTF-8");
        } catch (IOException e) {
            //TODO
        }
        return writer.toString();
    }

    /**
     * Helper method to construct a ServiceKey
     * @param serviceNameAndVersion The name and version of Service seperated by ServiceKeyImpl.SERVICE_VERSION_SEPARATOR
     * @return ServiceKey
     */
    private ServiceKey constructServiceKey(String serviceNameAndVersion) {
        String[] nameAndVersion = serviceNameAndVersion.split(ServiceKeyImpl.SERVICE_VERSION_SEPARATOR);
        if(nameAndVersion.length!=2) {
            throw new RuntimeException("Invalid serviceNameAndVersion");
        }
        return new ServiceKeyImpl(nameAndVersion[0],nameAndVersion[1]);
    }

    /** Getter Setter methods */
    public ServiceStatisticsGatherer getServiceStatisticsGatherer() {
        return serviceStatisticsGatherer;
    }
    public void setServiceStatisticsGatherer(ServiceStatisticsGatherer serviceStatisticsGatherer) {
        this.serviceStatisticsGatherer = serviceStatisticsGatherer;
    }
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}