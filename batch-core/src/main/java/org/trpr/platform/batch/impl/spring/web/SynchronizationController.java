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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
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

	public static final String PUSH_URL = "/sync/push/deploy";
	public static final String PULL_URL = "/sync/pull";

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
	 * Receives a pull request
	 */
	@RequestMapping(value=SynchronizationController.PULL_URL,method=RequestMethod.POST)
	public String pullRequest(ModelMap model, @RequestParam String serverName) {
		LOGGER.info("Received a pull request from: "+serverName.trim());
		if(this.jobConfigService.getSyncService()!=null) {
			this.jobConfigService.getSyncService().deployAllJobsToHost(serverName.trim());
			model.addAttribute("Message","success");
		}
		return "sync/Message";
	}


	/**
	 * Receiver methods start
	 * These methods receive the job configuration files, dependency files and job loading requests.
	 */
	@RequestMapping(value=SynchronizationController.PUSH_URL,method=RequestMethod.POST)
	public synchronized String jobReceiver(ModelMap model,@RequestParam String jobName, 
			@RequestParam(value="jobConfig") MultipartFile jobConfig 
			, @RequestParam(value="depFiles[]", required= false) MultipartFile[] depFiles) {


		jobName=jobName.trim();
		LOGGER.info("Push job request received for job: "+jobName);

		//Upload configuration file
		if(this.jobService.contains(jobName)) {
			LOGGER.info("Warning: "+jobName+" already exists. Modifying old file");
		}
		try {
			//Set XML File
			List<String> jobNames = new LinkedList<String>();
			jobNames.add(jobName);
			this.jobConfigService.setJobConfig(jobNames,new ByteArrayResource(jobConfig.getBytes()));
			LOGGER.info("Success in deploying configuration file for: "+jobName);
			model.addAttribute("Message","success");
		} catch (Exception e) {
			model.addAttribute("Message","Unexpected error");
		}

		//Upload dependency Files
		if(depFiles!=null && depFiles.length!=0) { //Dep files exist
			for(MultipartFile depFile: depFiles) {
				try {
					//Set dependencies
					LOGGER.info("Request to deploy file: "+jobName+" "+depFile.getOriginalFilename()+" "+depFile.getSize());
					List<String> jobNames = new LinkedList<String>();
					jobNames.add(jobName);
					this.jobConfigService.addJobDependency(jobNames, depFile.getOriginalFilename(), depFile.getBytes());
					LOGGER.info("Success in deploying dependency file for: "+jobName);
					model.addAttribute("Message","success");
				} catch (Exception e) {
					LOGGER.error("Exception while deploying Dependency file: ",e);
					model.addAttribute("Message","Unexpected error while deploying dependencyFile: "+depFile.getOriginalFilename());
				}
			}
		}

		LOGGER.info("Deploy request");
		//Deploy request
		try {
			List<String> jobNames = new LinkedList<String>();
			jobNames.add(jobName);
			this.jobConfigService.deployJob(jobNames);
			LOGGER.info("Success in deploying: "+jobName);
			model.addAttribute("Message","success");
		} catch (Exception e) {
			LOGGER.error("Error while deploying job: "+jobName, e);
			model.addAttribute("Message","Unexpected error while loading: "+jobName);
		}

		return "sync/Message";
	}
}
