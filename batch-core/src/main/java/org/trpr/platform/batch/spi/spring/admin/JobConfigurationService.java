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
package org.trpr.platform.batch.spi.spring.admin;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

/**
 * Provides methods for job configuration such as adding, removing XML Config files and dependencies
 * 
 * @author devashishshankar
 * @version 1.0, 22 Jan 2013
 */
public interface JobConfigurationService {	
	
	/**
	 * Gets the jobDirectory of a job, where all the config-files and dependencies are stored.
	 * Returns a new directory based on jobName if job doesn't exist
	 */
	public String getJobDirectory(String jobName);
	
	/**
	 * Gets the XML File path for given job. If not found, returns null.
	 * @param jobName
	 * @return Path of XMLFile. null if not found
	 */
	public String getXMLFile(String jobName);
	
	/**
	 * Sets the XML File. If the job doesn't have an XML file (new job), a new directory 
	 * is created and a new XML File is created there. Otherwise, the old file is overwritten.
	 * Returns false if some exceptional condition has occurred.
	 * @param jobName
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public boolean setXMLFile(String jobName, String XMLFileContents) throws IOException, ParserConfigurationException, SAXException;
	
	/**
	 * 	Removes an XML File. Should be used if a wrong XML File has been uploaded
	 * @param jobName
	 */
	public void removeXMLFile(String jobName);
	/**
	 * Add a job dependency for a given job. Also uploads the dependency file to its directory
	 * @param jobName Name of the job
	 */
	public void addJobDependency(String jobName, MultipartFile file);
	
	/** 
	 * Returns the list of dependencies of given job. 
	 * @param jobName Name of the job
	 * @return List of dependencies. If not found, returns null
	 */
	public List<String> getJobDependencyList(String jobName);
	
	/**
	 * Get the jobname from a spring batch config file
	 */
	public String getJobNameFromXML(MultipartFile jobFile);
	
	/**
	 * Get the jobname from a spring batch config file contents.
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public String getJobNameFromXML(String fileContents) throws ParserConfigurationException, SAXException;

}
