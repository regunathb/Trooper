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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.batch.spi.spring.admin.FileService;

/**
 * Controller for handling requests related to job configuration (Uploading job files, dependencies, editing job Files)
 * @author devashishshankar
 * @version 1.0 22 Jan, 2012
 * TODO: Make redirect work (Code will be cleaner)
 */

@Controller
public class JobConfigController {
	
	private JobService jobService;
	private JobConfigurationService jobConfigService;
	private FileService fileService;
	
	@Autowired
	public JobConfigController(JobService jobService,JobConfigurationService jobConfigService, FileService fileService) {
		this.jobService = jobService;
		this.jobConfigService = jobConfigService;
		this.fileService = fileService;		
	}
	
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
	 * Controller for job edit page. Adds XML File contents and dependencies to the view
	 */
	@RequestMapping(value = "/configuration/modify/jobs/{jobName}", method = RequestMethod.GET)
	public String modifyJob(ModelMap model, @ModelAttribute("jobName") String jobName) {
		//Load & Add JobName, XMLFileContents, Dependencies to the view
		jobName= jobName.substring(jobName.lastIndexOf('/')+1);
		String XMLFileContents = this.fileService.getFileContents(jobConfigService.getXMLFile(jobName));
		model.addAttribute("jobName", jobName);
		model.addAttribute("XMLFileContents", XMLFileContents);
		if(jobConfigService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
		}
		return "configuration/modify/jobs/job";	
	}
	
	/**
	 * Controller for new job. Just adds an attribute jobName to the model. And redirects to the job edit page.
	 */
	@RequestMapping(value = "/configuration/modify_job", method = RequestMethod.POST)
	public String addNewJob(ModelMap model, @RequestParam MultipartFile jobFile) {
		String jobFileName = jobFile.getOriginalFilename();
		//Check if file is empty or doesn't have an extension
		if (jobFile.isEmpty()||(jobFileName.lastIndexOf('.')<0)) {
			model.remove("jobFile");
			model.addAttribute("Error", "File is Empty or invalid. Only .xml files can be uploaded");
			return "redirect:/configuration";
		}
		//Check if file is .xml
		else if(!jobFileName.substring(jobFileName.lastIndexOf('.')).equals(".xml")) {

			model.remove("jobFile");
			model.addAttribute("Error", "Only .xml files can be uploaded");
			return "redirect:/configuration";
		}
		//Read file to view
		else {
			boolean invalidJobFile=false;
			try {
					byte[] buffer = jobFile.getBytes();
					String XMLFileContents = new String(buffer, "UTF-8");
					model.addAttribute("XMLFileContents", XMLFileContents);
			} 
			catch (UnsupportedEncodingException e) {
				invalidJobFile=true;
			} 
			catch (IOException e) {
				invalidJobFile=true;
			}
			String jobName= jobConfigService.getJobNameFromXML(jobFile);
			if(jobName==null || invalidJobFile) {
				model.clear();
				model.addAttribute("Error", "invalid jobFile. Couldn't find job's name");	
				return "redirect:/configuration";
			}
			if(jobService.contains(jobName)) {
				model.clear();
				model.addAttribute("Error", "The JobName '"+jobName+"' already exists. Please choose another name");	
				return "redirect:/configuration";
			}
			model.addAttribute("jobName", jobName);		
			return "configuration/modify/jobs/job";
		}
	}
	
	/**
	 * This method handles all the configuration changes:
	 * 	Uploading of XML File
	 * 	Uploading of dependency
	 * 	Saving the changes in XML File
	 */
	@RequestMapping(value = "configuration/modify/jobs/{jobName}", method = RequestMethod.POST)
	public String editJob(ModelMap model, @RequestParam String jobName, 
		@RequestParam(defaultValue = "") String XMLFileContents, @RequestParam(defaultValue = "0") 
		MultipartFile jobFile, @RequestParam(defaultValue = "0") MultipartFile depFile, 
		@RequestParam(defaultValue = "0") String identifier) throws Exception {
		
		//Button 1: Upload XML
		if(identifier.equals("Upload file")) {
			String jobFileName = jobFile.getOriginalFilename();
			//Check if file is empty or doesn't have an extension
			if (jobFile.isEmpty()||(jobFileName.lastIndexOf('.')<0)) {
				model.addAttribute("XMLFileError", "File is Empty or invalid. Only .xml files can be uploaded");
			}
			//Check if file is .xml
			else if(!jobFileName.substring(jobFileName.lastIndexOf('.')).equals(".xml")) {
				model.addAttribute("XMLFileError", "Only .xml files can be uploaded");
			}
			//Read file to view
			else {
				byte[] buffer = jobFile.getBytes();
				XMLFileContents = new String(buffer, "UTF-8");
				model.addAttribute("XMLFileContents", XMLFileContents);
			}
		}
		//Button 2: Upload dependencies
		else if(identifier.equals("Upload dependency")) {
			String depFileName = depFile.getOriginalFilename();	
			if (depFile.isEmpty()||(depFileName.lastIndexOf('.')<0)) {
				model.addAttribute("DepFileError", "File is Empty or invalid. Only .jar files can be uploaded");
			}
			//Check if file is valid
			else if(!depFileName.substring(depFileName.lastIndexOf('.')).equals(".jar")) {
				model.addAttribute("DepFileError", "Only .jar files can be uploaded");
			}
			//Check if file hasn't been added already
			else if(jobConfigService.getJobDependencyList(jobName)!=null && jobConfigService.getJobDependencyList(jobName).contains(depFileName)){
				model.addAttribute("DepFileError", "The filename is already added. Duplicates not allowed");
			}
			//Move uploaded file
			else {
				jobConfigService.addJobDependency(jobName,depFile);
			}
		}
		//Button 3: Save. Overwrite the modified XML File
		else {
			//Is XML File modified?
			boolean fileModifedFlag=false;
			try {
				//Set XML File
				if(!this.jobConfigService.setXMLFile(jobName, XMLFileContents)) {
					throw new Exception("Job name cannot be changed");
				}
				//File has been modified if previous exception was not thrown
				fileModifedFlag = true;
				//Try a trooper reload (loadResource)
				this.jobService.getComponentContainer().loadComponent(new FileSystemResource(jobConfigService.getJobDirectory(jobName)+"/spring-batch-config.xml"));
			}
			//Loading didn't work
			catch (Exception e) {
				//If file has been modified, delete the modified file and try to restore previous version
				if(fileModifedFlag) {
					jobConfigService.removeXMLFile(jobName);
					//Previous version restoring
					if(jobConfigService.getXMLFile(jobName)!=null) {
						this.jobService.getComponentContainer().loadComponent(new FileSystemResource(jobConfigService.getXMLFile(jobName)));
					}
				}
				//View: Add Error and rest of the attributes
				//Get stacktrace as string
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				model.addAttribute("LoadingError", errors.toString());
				if(errors.toString()==null) {
					model.addAttribute("LoadingError", "Unexpected error");
				}
				model.addAttribute("XMLFileContents", XMLFileContents.trim());
				model.addAttribute("jobName", jobName);
				if(jobConfigService.getJobDependencyList(jobName)!=null) {
					model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
				}
				model.addAttribute("XMLFileContents", XMLFileContents.trim());
				//Redirect
				return "configuration/modify/jobs/job";
			}
			//Loading worked. Redirect to job configuration page. Load the view details
			model.addAttribute("SuccessMessage", "The job was successfully deployed!");
			model.addAttribute("jobName", jobName);
			if(jobConfigService.getJobDependencyList(jobName)!=null) {
				model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
			}
			model.addAttribute("XMLFileContents", XMLFileContents.trim());
			String jobDirectory = this.jobConfigService.getJobDirectory(jobName);
			model.addAttribute("JobDirectoryName",jobDirectory.substring(jobDirectory.lastIndexOf('/')+1)+"/lib");
			return "configuration/jobs/job";
		}
		//Update the view
		model.addAttribute("jobName", jobName);
		if(jobConfigService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
		}
		model.addAttribute("XMLFileContents", XMLFileContents);
		//Redirect to modify page
		return "configuration/modify/jobs/job";
	}
	
	/**
	 * Displays a non-editable version of job configuration
	 * This method gets the XMLFile, Dependencies from jobService and displays it.
	 */
	@RequestMapping(value = "/configuration/jobs/{jobName}", method = RequestMethod.GET)
	public String viewConfigDetails(ModelMap model, @ModelAttribute("jobName") String jobName, Errors errors,
			@RequestParam(defaultValue = "0") int startJobInstance, @RequestParam(defaultValue = "20") int pageSize) throws IOException {	
		//Adding jobName to view
		jobName= jobName.substring(jobName.lastIndexOf('/')+1);		
		model.addAttribute("jobName", jobName);
		//Adding XMLFileContents & dependencies to view
		String XMLFileContents = fileService.getFileContents(jobConfigService.getXMLFile(jobName));
		model.addAttribute("XMLFileName", jobConfigService.getXMLFile(jobName));	
		model.addAttribute("XMLFileContents", XMLFileContents);
		String jobDirectory = this.jobConfigService.getJobDirectory(jobName);
		model.addAttribute("JobDirectoryName",jobDirectory.substring(jobDirectory.lastIndexOf('/')+1)+"/lib");
		//if job has dependencies
		if(this.jobConfigService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", this.jobConfigService.getJobDependencyList(jobName));
		}
		return "configuration/jobs/job";
	}
	

}
