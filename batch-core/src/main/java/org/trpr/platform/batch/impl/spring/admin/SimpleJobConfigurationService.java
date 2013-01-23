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

package org.trpr.platform.batch.impl.spring.admin;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.BatchFrameworkConstants;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.TrooperFileService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An implementation of @link {JobConfigurationService}
 * Provides functions for configuring jobs, their configuration files, dependencies, dynamic loading, etc.
 * @author devashishshankar
 * @version 1.0 22 Jan, 2013
 */
public class SimpleJobConfigurationService implements JobConfigurationService {

	/**Holds the list of job Dependencies */
	private Map<String,List<String>> jobDependencies;
	
	/**Holds the list of XML Files */
	private Map<String,String> jobXMLFile;
	
	/**JobRegistry. Has the name of jobs */
	private JobRegistry jobRegistry;
	
	/**TrooperFIleSErvice provides filesystem functions such as upload, delete **/
	private TrooperFileService fileService;
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(SimpleJobConfigurationService.class);
	
	private final String SPRING_BATCH_FILE = "/spring-batch-config.xml";
	private final String JOB_FOLDER = "/src/main/resources/external/";
	private final String LIBRARY_FOLDER = "/lib/";
	private final String SPRING_BATCH_PREV = "/spring-batch-config-prev.xml";
	
	public SimpleJobConfigurationService(JobRegistry jobRegistry, TrooperFileService fileService) {
		this.jobRegistry = jobRegistry;		
		this.fileService = fileService;
		this.jobDependencies = new HashMap<String, List<String>>();
		this.jobXMLFile = new HashMap<String, String>();
		this.deleteEmptyFolders();
	}
	
	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getJobDirectory(String)
	 */
	public String getJobDirectory(String jobName) {	
		String XMLFilePath = this.getXMLFile(jobName);
		if(XMLFilePath==null) {
			return RuntimeVariables.getProjectsRoot()+this.JOB_FOLDER+jobName+"/";	
		}
		else {
			return XMLFilePath.substring(0, XMLFilePath.lastIndexOf('/'));
		}
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobDependency(String, MultipartFile)
	 */
	@Override
	public void addJobDependency(String jobName, MultipartFile depFile) {
		//Scan for dependencies
		if(this.jobDependencies.isEmpty())
		this.scanJobDependencies();
		
		//Upload file
		String destFileName = depFile.getOriginalFilename();
		String dest_path = this.getJobDirectory(jobName);
		this.fileService.upload(depFile,dest_path+this.LIBRARY_FOLDER+destFileName);
		List<String> dependencyList = null;
		if(this.jobDependencies.containsKey(jobName)) {
			dependencyList = this.jobDependencies.get(jobName);
		}
		else {
			dependencyList = new LinkedList<String>();
			this.jobDependencies.put("jobName", dependencyList);
		}
		dependencyList.add(destFileName);
		this.jobDependencies.put(jobName, dependencyList);
	}
	
	/**
	 * Interface Method Implementation. Gets the list of dependencies of given job. Returns null if 
	 * jobName doesn't exist or doesn't have any dependency
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobDependencyList
	 */
	@Override
	public List<String> getJobDependencyList(String jobName) {
		//Scan for jobDependencies
		if(this.jobDependencies.isEmpty())
		this.scanJobDependencies();
		return this.jobDependencies.get(jobName);
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getXMLFile(String)
	 */
	@Override
	public String getXMLFile(String jobName) {
		if(this.jobXMLFile.isEmpty())
			this.scanXMLFiles();

		if(this.jobXMLFile.containsKey(jobName)) {
			return this.jobXMLFile.get(jobName);
		}
		return null;
	}
	
	/**
	 * Interface method implementation. After setting an XML File, also saves the previous file.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#setXMLFile(String, String)
	 */
	@Override
	public boolean setXMLFile(String jobName, String XMLFileContents) throws IOException {
		if(this.jobXMLFile.isEmpty())
			this.scanXMLFiles();
		
		//Check if jobName has been changed
		if(!this.getJobNameFromXML(XMLFileContents).equals(jobName)) {
			return false;
		}
		//Code for overwriting file to location
		//TODO: Do this using FileService
		String dest_path = null;
		File xmlFile = null;
		//NEW JOB
		if(this.getXMLFile(jobName)==null) {
			dest_path=this.getJobDirectory(jobName)+this.SPRING_BATCH_FILE;
			xmlFile = new File(dest_path);
			xmlFile.getParentFile().mkdirs();
			xmlFile.createNewFile();
		}
		else {
			dest_path = this.getXMLFile(jobName);
			File prevXMLFile = new File(dest_path.substring(0, dest_path.lastIndexOf('/'))+this.SPRING_BATCH_PREV);
			xmlFile = new File(dest_path);
			if(prevXMLFile.exists()) {
				prevXMLFile.delete();
			}
			xmlFile.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile));
		writer.write(XMLFileContents.trim());
		writer.close();
		this.jobXMLFile.put(jobName,dest_path);
		return true;
	}

	/**
	 * Interface method implementation. After removing XML File, checks for previous spring batch file,
	 * if it finds it, it restores it.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#removeXMLFile(String)
	 */
	@Override
	public void removeXMLFile(String jobName) {
		String filePath= this.getXMLFile(jobName);
		this.fileService.delete(filePath);
		//Restore previous file
		File parentFile = new File(filePath).getParentFile();
		for (File content:parentFile.listFiles()) {
			if(content.getName().equals(this.SPRING_BATCH_PREV.substring(1))) {
				content.renameTo(new File(parentFile.getAbsolutePath()+this.SPRING_BATCH_FILE));
				return;
			}
		}
		this.jobXMLFile.remove(jobName);
	}
	
	/**
	 * Interface Method Implementation
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobNameFromXML(MultipartFile)
	 * Get the jobname from a spring batch config file
	 */
	@Override
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
	 * Interface Method Implementation
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobNameFromXML(String)
	 * Get the jobname from a spring batch config file
	 */
	@Override
	public String getJobNameFromXML(String fileContents) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(new ByteArrayInputStream(fileContents.getBytes()));
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
	 * Scan the jobDirectory for any new dependency files and update JobDependencies.
	 */
	private void scanJobDependencies() {		
		for(String jobName:this.jobRegistry.getJobNames()) {			
			  String jobDirectory = this.getJobDirectory(jobName)+this.LIBRARY_FOLDER;
			  File folder = new File(jobDirectory);
			  File[] listOfFiles = folder.listFiles(); 
			  List<String> dependencyList = new LinkedList<String>();
			  //if directory does exist
			  if(listOfFiles!=null) {
				  for(File dependency: listOfFiles) {
					  dependencyList.add(dependency.getName());
				  }
			  }
			  this.jobDependencies.put(jobName, dependencyList);
		}
	}
	
	private void deleteEmptyFolders() {
		File jobDirectory = new File(RuntimeVariables.getProjectsRoot()+this.JOB_FOLDER);
		File[] listOfFiles=jobDirectory.listFiles();
		
		  if(listOfFiles!=null) {
			  for(File jobFolder: listOfFiles) {
				  if(jobFolder.isDirectory()) {
					  if(jobFolder.delete()) {
						  LOGGER.info("Deleted empty folder: "+jobFolder.getName());
					  }
				  }
			  }
		  }
	}
	
	/**
	 * Scans XML Spring Batch Config files and adds them to jobXMLFile map
	 */
	private void scanXMLFiles() {
		File[] jobBeansFiles = FileLocator.findFiles(BatchFrameworkConstants.SPRING_BATCH_CONFIG);					
		for (File jobBeansFile : jobBeansFiles) {
			this.jobXMLFile.put(this.getJobNameFromXML(fileService.getFileContents(jobBeansFile.getAbsolutePath())), jobBeansFile.getAbsolutePath());
		}
	}
}
