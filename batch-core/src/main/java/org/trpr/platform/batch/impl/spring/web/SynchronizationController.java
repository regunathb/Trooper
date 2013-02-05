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
package org.trpr.platform.batch.impl.spring.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * <code> {@link SynchronizationController} </code> is a controller handling sync requests, i.e
 * requests handling dynamic deployment of new jobs using REST API
 * @author devashishshankar
 * @version 1.0, 28 Jan, 2013
 */
@Controller
public class SynchronizationController {

	/** Trooper services being used by the class */
	private JobConfigurationService jobConfigService;
	private JobService jobService;

	/** The URL for various actions */
	public static final String PUSH_JOB_URL = "/sync/pushJob";
	public static final String PUSH_DEP_URL = "/sync/pushDep";
	public static final String PUSH_URL = "/sync/push/deploy";

	/** The success message to be returned by the server */
	public static final String SUCCESS_STRING = "success";

	/** Logger instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(SynchronizationController.class);

	/**
	 * Default Constructor.
	 */
	public SynchronizationController(JobService jobService, JobConfigurationService jobConfigService) {
		this.jobConfigService = jobConfigService;		
		this.jobService = jobService;
	}

	/**
	 * Finds the jobname from the request URL
	 */
	@ModelAttribute("jobName")
	public String getJobName(HttpServletRequest request) {
		String path = request.getPathInfo();
		int index = path.lastIndexOf("jobs/") + 5;
		if (index >= 0) {
			path = path.substring(index);
		}
		return path;
	}
	
	/**
	 * Receiver methods start
	 * These methods receive the job configuration files, dependency files and job loading requests.
	 */
	/**
	 * Receives the Job file
	 * @return "success" on success, else the error message. (This is returned using REST API)
	 */
	@RequestMapping(value="{jobName}"+SynchronizationController.PUSH_JOB_URL,method=RequestMethod.POST)
	public String addJob(ModelMap model, @RequestParam String jobName, @RequestParam String configFile) {
		//Job Names can have appended whitespace characters when sent through request
		jobName=jobName.trim();
		LOGGER.info("Adding jobName: "+jobName);
		if(this.jobService.contains(jobName)) {
			LOGGER.info("Warning: "+jobName+" already exists. Modifying old file");
		}
		try {
			//Set XML File
			this.jobConfigService.setJobConfig(jobName,new ByteArrayResource(configFile.getBytes()));
			LOGGER.info("Success in deploying configuration file for: "+jobName);
			model.addAttribute("Message","success");
		} catch (Exception e) {
			model.addAttribute("Message","Unexpected error");
		}
		return "sync/Message";
	}

	/**
	 * Receives a single dependency file
	 * @return "success" on success, else the error message. (This is returned using REST API)
	 */
	@RequestMapping(value="{jobName}"+SynchronizationController.PUSH_DEP_URL,method=RequestMethod.POST)
	public String addDep(ModelMap model, @RequestParam String fileDescription, @RequestParam String fileName, 
			@RequestParam MultipartFile attachment) {
		//File description holds jobName
		String jobName=fileDescription.trim();
		try {
			//Set dependencies
			LOGGER.info("Request to deploy file: "+jobName+" "+fileName+" "+attachment.getSize());
			this.jobConfigService.addJobDependency(jobName, fileName, attachment.getBytes());
			LOGGER.info("Success in deploying dependency file for: "+jobName);
			model.addAttribute("Message","success");
		} catch (Exception e) {
			LOGGER.error("Exception while deploying Dependency file: ",e);
			model.addAttribute("Message","Unexpected error while deploying dependencyFile: "+fileName);
		}
		return "sync/Message";
	}

	/**
	 * Receives a loading request
	 * @return "success" on success, else the error message. (This is returned using REST API)
	 */
	@RequestMapping(value="{jobName}"+SynchronizationController.PUSH_URL,method=RequestMethod.POST)
	public String push(ModelMap model, @RequestParam String jobName) {
		//jobName coming through REST API might have whitespace characters appended
		jobName=jobName.trim();
		LOGGER.info("Loading request for: "+jobName);
		try {
			this.jobConfigService.deployJob(jobName);
			LOGGER.info("Success in deploying: "+jobName);
			model.addAttribute("Message","success");
		} catch (Exception e) {
			LOGGER.error("Error while deploying job: "+jobName, e);
			model.addAttribute("Message","Unexpected error while loading: "+jobName);
		}
		return "sync/Message";
	}
	/**
	 * Receiver methods end
	 */
	
	/**
	 * Testing method
	 */
	@RequestMapping(value="/sync/test/jobs/{jobName}", method=RequestMethod.GET)
	public String caller(@ModelAttribute("jobName") String jobName) {
		String servername = "http://localhost:8082/";
		LOGGER.info("Pushing "+jobName+" to "+servername);
		return "redirect:/configuration";
	}
}
