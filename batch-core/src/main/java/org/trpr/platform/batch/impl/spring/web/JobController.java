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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.batch.admin.web.TableUtils;
import org.springframework.batch.core.launch.NoSuchJobException;
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
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The <code>JobController</code> class is an extension of {@link JobController} that adds abilities 
 * to add extra information about trigger, such as cronexpression, next fire time, etc.
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 * 
 * Changelog:
 * Added controllers handling the configuration of job files (Uploading job files, dependencies, editing job Files)
 * @author devashishshankar
 * @version 1.0, 21 Jan 2013
 * 
 */

@Controller
public class JobController extends org.springframework.batch.admin.web.JobController {
	
	//An instance of JobService which holds all the information about jobs
	private JobService jobService;
	
	/**
	 * Autowired default constructor
	 */
	@Autowired
	private JobController(JobService jobService) {
		super(jobService);
		this.jobService = jobService;
	}
		
	/**
	 * Controller methods start
	 */
	/**
	 * Controller for job edit page
	 */
	@RequestMapping(value = "/configuration/modify/jobs/{jobName}", method = RequestMethod.GET)
	public String modifyJobDisplay(ModelMap model, @ModelAttribute("jobName") String jobName) {
		//Load & Add JobName, XMLFileContents, Dependencies to the view
		jobName= jobName.substring(jobName.lastIndexOf('/')+1);
		String XMLFileContents = this.getFileContents(jobService.getXMLFile(jobName));
		model.addAttribute("jobName", jobName);
		model.addAttribute("XMLFileContents", XMLFileContents);
		if(jobService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobService.getJobDependencyList(jobName));
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
			try {
				
				byte[] buffer = jobFile.getBytes();
				String XMLFileContents = new String(buffer, "UTF-8");
				model.addAttribute("XMLFileContents", XMLFileContents);
				
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			String jobName= this.getJobNameFromXML(jobFile);
			if(jobName==null) {
				model.clear();
				model.addAttribute("Error", "invalid jobFile. Couldn't find job's name");	
				return "redirect:/configuration";
			}
			if(jobService.contains(jobName)) {
				model.clear();
				model.addAttribute("Error", "jobName exists");	
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
	 * 
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
			else if(jobService.getJobDependencyList(jobName)!=null && jobService.getJobDependencyList(jobName).contains(depFileName)){
				model.addAttribute("DepFileError", "The filename is already added. Duplicates not allowed");
			}
			//Move uploaded file
			else {
				jobService.addJobDependency(jobName,depFile);
			}
		}
		
		//Button 3: Save. Overwrite the modified XML File
		else {
			//Get path of XML File
			BufferedWriter writer;
			String dest_path = jobService.getXMLFile(jobName);
			File xml_file=null;
			//XML File doesn't exist in jobService (new job)
			if(dest_path==null) {
				dest_path = RuntimeVariables.getProjectsRoot();
				dest_path+="/main/resources/external/"+jobName+"/spring-batch-config.xml";
				xml_file = new File(dest_path);
				xml_file.getParentFile().mkdirs();
				xml_file.createNewFile();
			}
			else {
				xml_file = new File(dest_path);
			}
			//Write "XMLFilecontents" to XML file
			writer = new BufferedWriter(new FileWriter(xml_file));
			writer.write(XMLFileContents.trim());
			writer.close();

			//Try a trooper reload (loadResource)
			try {
				jobService.getComponentContainer().loadComponent(new FileSystemResource(dest_path));
			}
			//Loading didn't work
			catch (Exception e) {
				//Delete uploaded file
				xml_file.delete();
				//View: Add Error and rest of the attributes
				model.addAttribute("XMLFileError", "Incorrect Spring Batch File or Required dependencies not included");
				model.addAttribute("jobName", jobName);
				if(jobService.getJobDependencyList(jobName)!=null) {
					model.addAttribute("dependencies", jobService.getJobDependencyList(jobName));
				}
				model.addAttribute("XMLFileContents", XMLFileContents.trim());
				//Redirect
				return "configuration/modify/jobs/job";
			}
			//Loading worked. Redirect to job configuration page. Load the view details
			model.addAttribute("jobName", jobName);
			if(jobService.getJobDependencyList(jobName)!=null) {
				model.addAttribute("dependencies", jobService.getJobDependencyList(jobName));
			}
			model.addAttribute("XMLFileContents", XMLFileContents.trim());
			return "configuration/jobs/job";
		}
		//Update the view
		model.addAttribute("jobName", jobName);
		if(jobService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobService.getJobDependencyList(jobName));
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
	public String config_details(ModelMap model, @ModelAttribute("jobName") String jobName, Errors errors,
			@RequestParam(defaultValue = "0") int startJobInstance, @RequestParam(defaultValue = "20") int pageSize) throws IOException {	
		//Adding jobName to view
		jobName= jobName.substring(jobName.lastIndexOf('/')+1);		
		model.addAttribute("jobName", jobName);
		//Adding XMLFileContents & dependencies to view
		String XMLFileContents = this.getFileContents(jobService.getXMLFile(jobName));
		model.addAttribute("XMLFileName", jobService.getXMLFile(jobName));	
		model.addAttribute("XMLFileContents", XMLFileContents);
		//if job has dependencies
		if(jobService.getJobDependencyList(jobName)!=null) {
			model.addAttribute("dependencies", jobService.getJobDependencyList(jobName));
		}
		return "configuration/jobs/job";
	}
	
	
	/**
	 * Overridden method from @link {org.springframework.batch.admin.web.JobController}. It now uses 
	 * @link {org.trpr.platform.batch.impl.spring.web.JobInfo} to hold additional details about job
	 * (Mainly scheduler related information). This is returned and added to the model so that it
	 * can be accessed by the ftl files
	 */
	@Override
	@RequestMapping(value = {"/jobs","/configuration"}, method = RequestMethod.GET)
	public void jobs(ModelMap model, @RequestParam(defaultValue = "0") int startJob,
			@RequestParam(defaultValue = "20") int pageSize ) {
		int total = jobService.countJobs();
		TableUtils.addPagination(model, total, startJob, pageSize, "Job");
		Collection<String> names = jobService.listJobs(startJob, pageSize);
		
		//List of JobInfo elements to hold information to be displayed on the web console
		List<JobInfo> jobs = new ArrayList<JobInfo>();
		
		for (String name : names) {
			int count = 0;
			try {
				count = jobService.countJobExecutionsForJob(name);
			}
			catch (NoSuchJobException e) {
				// shouldn't happen
			}
			
			//Getting attributes from jobService
			boolean launchable = jobService.isLaunchable(name);
			boolean incrementable = jobService.isIncrementable(name);
			String cronExp = jobService.getCronExpression(name);
			Date nextFireDate = jobService.getNextFireDate(name);

			//Adding attributes to the list
			jobs.add(new JobInfo(name, count, null, launchable, incrementable,cronExp,nextFireDate));
		}
		//Adding the list to the model "newjobs" to be accessed in the FTL files
		model.addAttribute("newjobs", jobs);
	}	
	
	/**
	 * End controller Methods
	 */
	
	/**
	 * Get the jobname from a spring batch config file
	 */
	public String getJobNameFromXML(MultipartFile jobFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(new ByteArrayInputStream(jobFile.getBytes()));
			Element docEle = dom.getDocumentElement();
			//get a nodelist of nodes with the name "bean" 
			NodeList nl = docEle.getElementsByTagName("batch:job");
			//Loop over all found nodes
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
					//get the employee element
					Element el = (Element)nl.item(i);
					if(el.hasAttribute("id")) {
							return el.getAttribute("id");
							}
					}
			}	
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch(SAXException se) {
			se.printStackTrace();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets the contents of a file (on server) in a single String
	 */
	private String getFileContents(String filename) {
		try {
			File f = new File(filename);
			FileInputStream fin = new FileInputStream (filename);
			byte[] buffer = new byte[(int) f.length()];
			new DataInputStream(fin).readFully(buffer);
			fin.close();
			return new String(buffer,"UTF-8").trim();
		}
		catch(Exception e) {
			//Will return null
		}
		return null;
	}
}