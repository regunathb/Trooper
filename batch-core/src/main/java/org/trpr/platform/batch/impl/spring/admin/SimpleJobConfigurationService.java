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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.BatchFrameworkConstants;
import org.trpr.platform.batch.impl.spring.BatchConfigInfo;
import org.trpr.platform.batch.impl.spring.web.Host;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code> SimpleJobConfigurationService </code>: An implementation of @link {JobConfigurationService}
 * Provides methods for configuring jobs - their configuration files and dependencies. It also holds the 
 * list of running Trooper instances and the list of deployed jobs in each of them (for HA mode)
 * 
 * @author devashishshankar
 * @version 1.0 22 Jan, 2013
 */
public class SimpleJobConfigurationService implements JobConfigurationService {

	/**Holds the list of job Dependencies */
	private Map<String,List<String>> jobDependencies;

	/**Holds the list of XML Files */
	private Map<String,String> jobXMLFile;

	/** Holds the list of servers onto which a job is deployed */
	private Map<String,List<Host>> jobServerNames;

	/** Holds the list of serverNames*/
	private List<Host> serverNames;

	/** List of HA jobs in current server **/
	private List<String> currentJobNames;

	/**JobRegistry. Has the name of jobs */
	private JobRegistry jobRegistry;

	/**Holds the current server details **/
	private Host serverName;

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(SimpleJobConfigurationService.class);

	/**The params holding the filenames, directories and tag name conventions of Trooper configuration files **/
	private static final String SPRING_BATCH_FILE = "/" + BatchFrameworkConstants.SPRING_BATCH_CONFIG;
	private static final String JOB_FOLDER = "/src/main/resources/external/";
	private static final String LIBRARY_FOLDER = "/" + BatchConfigInfo.BINARIES_PATH + "/";
	private static final String SPRING_BATCH_PREV = "/spring-batch-config-prev.xml";
	private static final String BATCH_JOB_TAG = "batch:job";
	private static final String ID_PROP = "id";

	/**
	 * Constructor method
	 * @param jobRegistry THe registry containing Job Names
	 * @param fileService Instance of FileService
	 */
	public SimpleJobConfigurationService(JobRegistry jobRegistry) {
		this.jobRegistry = jobRegistry;		
		this.jobDependencies = new HashMap<String, List<String>>();
		this.jobXMLFile = new HashMap<String, String>();
		this.jobServerNames = new HashMap<String, List<Host>>();
		this.serverNames = new LinkedList<Host>();
		this.currentJobNames =  new LinkedList<String>();
	}

	/**
	 * Interface method implementation.
	 * Also sets the hostName
	 * @see JobConfigurationService#setPort(int)
	 */
	@Override
	public void setPort(int port) {
		String hostName = "";
		String ipAddr = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			ipAddr = InetAddress.getLocalHost().getHostAddress();
			Enumeration<NetworkInterface> nets = 
					NetworkInterface.getNetworkInterfaces();
			while (nets.hasMoreElements())
			{
				NetworkInterface netint = (NetworkInterface) nets.nextElement();
				Enumeration<InetAddress> ips = netint.getInetAddresses();
				while(ips.hasMoreElements()) {
					InetAddress ip = ips.nextElement();
					if (!ip.isLoopbackAddress()  &&  
							ip.isSiteLocalAddress()) {
						LOGGER.info("Host IP Address: "+ip.getHostAddress());
						ipAddr = ip.getHostAddress();
						break;
					}
				}
			}
		} catch (UnknownHostException e) {
			LOGGER.error("Error while getting hostName ",e);
		} catch (SocketException e) {
			LOGGER.error("Error while getting hostName ",e);
		}
		this.serverName = new Host(hostName,ipAddr,port);
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentServerName()
	 */
	@Override
	public Host getCurrentServerName() {	
		return this.serverName;
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentServerJobs()
	 */
	@Override
	public Collection<String> getCurrentServerJobs() {
		return this.currentJobNames;
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobInstance(String, Host)
	 */
	@Override
	public void addJobInstance(String jobName, Host serverName) {
		LOGGER.info("Adding instance: "+jobName+" "+serverName.getPort());
		if(this.jobServerNames.containsKey(jobName)) {
			if(!this.jobServerNames.get(jobName).contains(serverName)) {
				this.jobServerNames.get(jobName).add(serverName);
				LOGGER.info("added new server");
			}
		}
		else {
			List<Host> serverList = new LinkedList<Host>();
			serverList.add(serverName);
			this.jobServerNames.put(jobName, serverList);
		}
		if(!this.serverNames.contains(serverName)) {
			this.serverNames.add(serverName);
			LOGGER.info("Added to serverNames: "+serverName+": "+serverName.getAddress());
		}
		if(!this.currentJobNames.contains(jobName)) {
			this.currentJobNames.add(jobName);
		}
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getServerNames(String)
	 */
	@Override
	public List<Host> getServerNames(String jobName) {
		return this.jobServerNames.get(jobName);		
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getAllServerNames()
	 */
	@Override
	public List<Host> getAllServerNames() {
		return this.serverNames;	
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getJobDirectory(String)
	 */
	public String getJobDirectory(String jobName) {	
		String XMLFilePath = this.getXMLFilePath(jobName);
		if(XMLFilePath==null) {
			return RuntimeVariables.getProjectsRoot()+SimpleJobConfigurationService.JOB_FOLDER+jobName+"/";	
		}else {
			return XMLFilePath.substring(0, XMLFilePath.lastIndexOf('/'));
		}
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobDependency(String, MultipartFile)
	 */
	@Override
	public void addJobDependency(String jobName, String destFileName, byte[] fileContents) {
		//Scan for dependencies
		if(this.jobDependencies.isEmpty())
			this.scanJobDependencies();
		String destPath = this.getJobDirectory(jobName);
		if(this.jobDependencies.containsKey(jobName)) {
			if(this.jobDependencies.get(jobName).contains(destFileName)) {
				throw new PlatformException("The dependency: "+destFileName+" already exists");
			}
		}
		//Upload file
		try {
			this.upload(fileContents,destPath+SimpleJobConfigurationService.LIBRARY_FOLDER+destFileName);
		} catch (IOException e) {
			LOGGER.error("Error uploading file: "+destFileName+" to "+destPath);
			throw new PlatformException("Error uploading file: "+destFileName+" to "+destPath,e);
		}
		//Update the jobDependency list
		List<String> dependencyList = null;
		if(this.jobDependencies.containsKey(jobName)) {
			dependencyList = this.jobDependencies.get(jobName);
		}else {
			dependencyList = new LinkedList<String>();
			this.jobDependencies.put(jobName, dependencyList);
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
		if(this.jobDependencies.isEmpty()) {
			this.scanJobDependencies();
		}
		return this.jobDependencies.get(jobName);
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getXMLFile(String)
	 */
	@Override
	public String getXMLFilePath(String jobName) {
		if(this.jobXMLFile.isEmpty()) {
			this.scanXMLFiles();
		}
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
	public void setXMLFile(String jobName, String XMLFileContents) throws PlatformException {
		if(this.jobXMLFile.isEmpty())
			this.scanXMLFiles();
		String destPath = null;
		//Check if jobName has been changed
		try {
			if(!this.getJobNameFromXML(XMLFileContents.getBytes()).equals(jobName)) {
				throw new PlatformException("The Job Name cannot be changed. Expecting: "+jobName+" Got: "+this.getJobNameFromXML(XMLFileContents.getBytes()));
			}
			//Code for overwriting file to location
			if(this.getXMLFilePath(jobName)==null) {  //NEW JOB
				destPath=this.getJobDirectory(jobName)+SimpleJobConfigurationService.SPRING_BATCH_FILE;
			}else { //Already deployed job. Store the previous file
				destPath = this.getXMLFilePath(jobName);
				String prevPath = destPath.substring(0, destPath.lastIndexOf('/'))+SimpleJobConfigurationService.SPRING_BATCH_PREV;
				File prevXMLFile = new File(prevPath);
				File xmlFile = new File(destPath);
				if(prevXMLFile.exists()) {
					prevXMLFile.delete();
				}
				//Rename the old config file
				xmlFile.renameTo(prevXMLFile);
				xmlFile.createNewFile();
				prevXMLFile.deleteOnExit();
			}
			this.upload(XMLFileContents.getBytes(), destPath);
			this.jobXMLFile.put(jobName,destPath);
		} catch (IOException ioe) {
			LOGGER.error("Error creating job configuration file for : " + jobName + " in location : " + destPath,ioe);
			throw new PlatformException("Error creating job configuration file for : " + jobName + " in location : " + destPath, ioe);
		}
	}

	/**
	 * Interface method implementation. After removing XML File, checks for previous spring batch file,
	 * if it finds it, it restores it.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#removeXMLFile(String)
	 */
	@Override
	public void removeXMLFile(String jobName) {
		String filePath= this.getXMLFilePath(jobName);
		new File(filePath).delete();
		//Restore previous file
		File parentFile = new File(filePath).getParentFile();
		for (File content:parentFile.listFiles()) {
			if(content.getName().equals(SimpleJobConfigurationService.SPRING_BATCH_PREV.substring(1))) {
				content.renameTo(new File(parentFile.getAbsolutePath()+SimpleJobConfigurationService.SPRING_BATCH_FILE));
				return;
			}
		}
		this.jobXMLFile.remove(jobName);
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#deploymentSuccess(String)
	 */
	@Override
	public void deploymentSuccess(String jobName) {
		// DELETE previous XML File
		String prevFilePath = this.getJobDirectory(jobName)+SimpleJobConfigurationService.SPRING_BATCH_PREV;
		File prevFile = new File(prevFilePath);
		if(prevFile.exists()){
			prevFile.delete();
		}
	}
	
	/**
	 * Interface Method Implementation
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobNameFromXML(String)
	 * Get the jobname from a spring batch config file
	 */
	@Override
	public String getJobNameFromXML(byte[] XMLFileContents) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(new ByteArrayInputStream(XMLFileContents));
			Element docEle = dom.getDocumentElement();
			//get a nodelist of nodes with the name "bean" 
			NodeList nl = docEle.getElementsByTagName(SimpleJobConfigurationService.BATCH_JOB_TAG);
			//Loop over all found nodes
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
					//get the element
					Element el = (Element)nl.item(i);
					if(el.hasAttribute(SimpleJobConfigurationService.ID_PROP)) {
						return el.getAttribute(SimpleJobConfigurationService.ID_PROP);
					}
				}
			}	
		}
		catch(Exception e) {
			LOGGER.error("Unable to get the job name from the given Spring Batch configuration file", e);
		}
		return null;
	}
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getFileContents(String)
	 */
	@Override
	public String getFileContents(String filename) {
		try {
			File f = new File(filename);
			FileInputStream fin = new FileInputStream (filename);
			byte[] buffer = new byte[(int) f.length()];
			new DataInputStream(fin).readFully(buffer);
			fin.close();
			return new String(buffer).trim();
		}
		catch(Exception e) {
			LOGGER.error("Error while reading contents of: "+filename,e);
		}
		return null;
	}

	/**
	 * Scan the jobDirectory for any new dependency files and update JobDependencies.
	 */
	private void scanJobDependencies() {		
		for(String jobName:this.jobRegistry.getJobNames()) {			
			String jobDirectory = this.getJobDirectory(jobName)+SimpleJobConfigurationService.LIBRARY_FOLDER;
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

	/**
	 * Scans XML Spring Batch Config files and adds them to jobXMLFile map
	 */
	private void scanXMLFiles() {
		File[] jobBeansFiles = FileLocator.findFiles(BatchFrameworkConstants.SPRING_BATCH_CONFIG);					
		for (File jobBeansFile : jobBeansFiles) {
			this.jobXMLFile.put(this.getJobNameFromXML(this.getFileContents(jobBeansFile.getAbsolutePath()).getBytes()), jobBeansFile.getAbsolutePath());
		}
	}

	/**
	 * Uploads the file to the given path. Creates the file and directory structure, if the file
	 * or parent directory doesn't exist
	 */
	private void upload(byte[] fileContents, String destPath) throws IOException {
		File destFile = new File(destPath);
		//Creating directory structure
		destFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(destFile);
		fos.write(fileContents);						
	}
}
