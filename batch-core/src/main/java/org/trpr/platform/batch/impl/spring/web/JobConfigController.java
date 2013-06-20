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
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.sun.org.apache.xerces.internal.xs.StringList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.common.utils.ConfigFileUtils;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * <code> JobConfigController </code> is a controller for handling requests related to 
 * job configuration (Uploading job files, dependencies, editing job Files)
 * 
 * @author devashishshankar
 * @version 1.1, 5 Feb, 2013
 */
@Controller
public class JobConfigController {

	/**Trooper services used by this class **/
	private JobService jobService;
	private JobConfigurationService jobConfigService;

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(JobConfigController.class);

	/**
	 * Autowired default constructor
	 */
	@Autowired
	public JobConfigController(JobService jobService,JobConfigurationService jobConfigService) {
		this.jobService = jobService;
		this.jobConfigService = jobConfigService;
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
	 * Controller for job edit page. Adds XML File contents and dependencies to the view
	 */
	@RequestMapping(value = "/configuration/modify/jobs/{jobName}", method = RequestMethod.GET)
	public String modifyJob(ModelMap model, @ModelAttribute("jobName") String jobName) {
		//Load & Add JobName, XMLFileContents, Dependencies to the view
		jobName= jobName.substring(jobName.lastIndexOf('/')+1);
		model.addAttribute("XMLFileContents", ConfigFileUtils.getContents(this.jobConfigService.getJobConfig(jobName)).trim());
		model.addAttribute("jobName", ConfigFileUtils.getJobName(this.jobConfigService.getJobConfig(jobName)));
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
		} else if(!jobFileName.substring(jobFileName.lastIndexOf('.')).equals(".xml")) { //Check if file is .xml
			model.remove("jobFile");
			model.addAttribute("Error", "Only .xml files can be uploaded");
			return "redirect:/configuration";
		} else { //Read file to view
			boolean invalidJobFile=false;
			List<String> jobNameList = null;
			try {
				byte[] buffer = jobFile.getBytes();
				String XMLFileContents = new String(buffer);
				model.addAttribute("XMLFileContents", XMLFileContents);
				jobNameList= ConfigFileUtils.getJobName(new ByteArrayResource(jobFile.getBytes()));
				if(jobNameList==null || jobNameList.size()==0) {
					throw new PlatformException("Empty list");
				}
			} 
			catch (UnsupportedEncodingException e) {
				invalidJobFile=true;
			} 
			catch (IOException e) {
				invalidJobFile=true;
			}
			catch (PlatformException p) {
				invalidJobFile = true;
			}
			for(String jobName : jobNameList) {
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
			}
			model.addAttribute("jobName", jobNameList);		
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
	public String editJob(ModelMap model, @RequestParam(value="jobName") String[] jobNames, 
			@RequestParam(defaultValue = "") String XMLFileContents, 
			@RequestParam(defaultValue = "0") MultipartFile jobFile, 
			@RequestParam(defaultValue = "0") MultipartFile depFile, 
			@RequestParam(defaultValue = "0") String identifier) throws Exception {
		
		List<String> jobNameList = Arrays.asList(jobNames);
		//FOr getter methods, such as getJobdependency, any of the jobNames among the list would do
		String jobName = jobNameList.get(0);
		//Button 1: Upload XML
		if(identifier.equals("Upload file")) {
			String jobFileName = jobFile.getOriginalFilename();
			//Check if file is empty or doesn't have an extension
			if (jobFile.isEmpty()||(jobFileName.lastIndexOf('.')<0)) {
				model.addAttribute("XMLFileError", "File is Empty or invalid. Only .xml files can be uploaded");
			} else if(!jobFileName.substring(jobFileName.lastIndexOf('.')).equals(".xml")) {//Check if file is .xml
				model.addAttribute("XMLFileError", "Only .xml files can be uploaded");
			} else { //Read file to view
				byte[] buffer = jobFile.getBytes();
				XMLFileContents = new String(buffer);
				model.addAttribute("XMLFileContents", XMLFileContents);
			}
		} else if(identifier.equals("Upload dependency")) { 
			//Button 2: Upload dependencies
			String depFileName = depFile.getOriginalFilename();	
			if (depFile.isEmpty()||(depFileName.lastIndexOf('.')<0)) {
				model.addAttribute("DepFileError", "File is Empty or invalid. Only .jar files can be uploaded");
			} else if(!depFileName.substring(depFileName.lastIndexOf('.')).equals(".jar")) { //Check if file is valid
				model.addAttribute("DepFileError", "Only .jar files can be uploaded");
			} else {//Move uploaded file
				//Check if file hasn't been added already
				if(jobConfigService.getJobDependencyList(jobName)!=null && jobConfigService.getJobDependencyList(jobName).contains(depFileName)){
					model.addAttribute("DepFileError", "The filename is already added. Overwriting");
				}
				jobConfigService.addJobDependency(jobNameList,depFile.getOriginalFilename(),depFile.getBytes());
			}
		} else { //Button 3: Save. Overwrite the modified XML File
			LOGGER.info("Request to deploy jobConfig file for: "+jobNameList);
			try {
				//Set XML File
				this.jobConfigService.setJobConfig(jobNameList, new ByteArrayResource(XMLFileContents.getBytes()));
				this.jobConfigService.deployJob(jobNameList);
			}
			catch (Exception e) {
				LOGGER.info("Error while deploying job",e);
				//View: Add Error and rest of the attributes
				//Get stacktrace as string
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				model.addAttribute("LoadingError", errors.toString());
				if(errors.toString()==null) {
					model.addAttribute("LoadingError", "Unexpected error");
				}
				model.addAttribute("XMLFileContents", XMLFileContents.trim());
				model.addAttribute("jobName", jobNameList);
				if(jobConfigService.getJobDependencyList(jobName)!=null) {
					model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
				}
				model.addAttribute("XMLFileContents", XMLFileContents.trim());
				//Redirect
				return "configuration/modify/jobs/job";
			}
			//Loading worked. Deploy to all hosts
			if(this.jobConfigService.getSyncService()!=null)
				this.jobConfigService.getSyncService().deployJobToAllHosts(jobName);
			//Redirect to job configuration page. Load the view details
			model.addAttribute("SuccessMessage", "The job was successfully deployed!");
			model.addAttribute("jobName", jobName);
			//Push jobs to all servers
			if(jobConfigService.getJobDependencyList(jobName)!=null) {
				model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
			}
			model.addAttribute("XMLFileContents", XMLFileContents.trim());
			String jobDirectory = this.jobConfigService.getJobStoreURI(jobName).getPath();
			model.addAttribute("JobDirectoryName",jobDirectory.substring(jobDirectory.lastIndexOf('/')+1)+"/lib");
			return "configuration/jobs/job";
		}
		//Update the view
		model.addAttribute("jobName", jobNameList);
		if(jobConfigService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobConfigService.getJobDependencyList(jobName));
		}
		model.addAttribute("XMLFileContents", XMLFileContents);
		//Redirect to modify page
		return "configuration/modify/jobs/job";
	}

    /**
     * Displays a read only version of job configuration
     * This method gets the XMLFile, Dependencies from jobService and displays it.
     */
    @RequestMapping(value = "/configuration/jobs/{jobName}", method = RequestMethod.GET)
    public String viewConfigDetails(ModelMap model, @ModelAttribute("jobName") String jobName) throws IOException {
        //Adding jobName to view
        jobName= jobName.substring(jobName.lastIndexOf('/')+1);
        model.addAttribute("jobName", jobName);
        //Adding XMLFileContents & dependencies to view
        String XMLFileContents = ConfigFileUtils.getContents(this.jobConfigService.getJobConfig(jobName));
        model.addAttribute("XMLFileName", this.jobConfigService.getJobConfig(jobName).getFilename());
        model.addAttribute("XMLFileContents", XMLFileContents);
        String jobDirectory = this.jobConfigService.getJobStoreURI(jobName).getPath();
        model.addAttribute("JobDirectoryName",jobDirectory.substring(jobDirectory.lastIndexOf('/')+1)+"/lib");
        //if job has dependencies
        if(this.jobConfigService.getJobDependencyList(jobName)!=null) {
            model.addAttribute("dependencies", this.jobConfigService.getJobDependencyList(jobName));
        }
        return "configuration/jobs/job";
    }

    /**
     * ReInits the Job. All the jobs present in the configuration file will be reInited
     */
    @RequestMapping(value = "/configuration/reInit/jobs/{jobName}", method = RequestMethod.GET)
    public String reInitJob(ModelMap model, @ModelAttribute("jobName") String jobName) throws IOException {
        //Adding jobName to view
        jobName= jobName.substring(jobName.lastIndexOf('/')+1);
        model.addAttribute("jobName", jobName);
        //Adding XMLFileContents & dependencies to view
        String XMLFileContents = ConfigFileUtils.getContents(this.jobConfigService.getJobConfig(jobName));
        model.addAttribute("XMLFileName", this.jobConfigService.getJobConfig(jobName).getFilename());
        model.addAttribute("XMLFileContents", XMLFileContents);

        try {
            //Try deploying
            this.jobConfigService.deployJob(Arrays.asList(new String[] {jobName}));
        }
        catch (Exception e) {
            LOGGER.info("Error while deploying job",e);
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
        String jobDirectory = this.jobConfigService.getJobStoreURI(jobName).getPath();
        model.addAttribute("JobDirectoryName",jobDirectory.substring(jobDirectory.lastIndexOf('/')+1)+"/lib");

        model.addAttribute("SuccessMessage", "The job was successfully reInited!");
        //if job has dependencies
        if(this.jobConfigService.getJobDependencyList(jobName)!=null) {
            model.addAttribute("dependencies", this.jobConfigService.getJobDependencyList(jobName));
        }
        return "configuration/jobs/job";
    }

}
