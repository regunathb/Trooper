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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.batch.BatchFrameworkConstants;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.batch.common.utils.ConfigFileUtils;
import org.trpr.platform.batch.impl.spring.BatchConfigInfo;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.config.FileLocator;

/**
 * <code> SimpleJobConfigurationService </code>: An implementation of @link {JobConfigurationService}
 * Provides methods for configuring jobs - their configuration files and dependencies. It also holds the 
 * list of running Trooper instances and the list of deployed jobs in each of them (for HA mode)
 * 
 * @author devashishshankar
 * @version 1.1 5 Feb, 2013
 */
public class SimpleJobConfigurationService implements JobConfigurationService {

	/**Holds the list of job Dependencies */
	private Map<String,List<String>> jobDependencies;

	/**Holds the list of XML Files */
	private Map<String,URI> jobXMLFile;

	/** Holds the list of hosts onto which a job is deployed */
	private Map<String,List<JobHost>> jobHostNames;

	/** Holds the list of hostNames*/
	private List<JobHost> hostNames;

	/** List of HA jobs in current host **/
	private List<String> currentJobNames;

	/** JobRegistry. Has the name of jobs */
	private JobRegistry jobRegistry;

	/** Holds the current host details **/
	private JobHost hostName;

	/** Trooper services being used by this class */
	private SyncService syncService;
	private JobService jobService;

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(SimpleJobConfigurationService.class);

	/**The params holding the filenames, directories and tag name conventions of Trooper configuration files **/
	private static final String SPRING_BATCH_FILE = "/" + BatchFrameworkConstants.SPRING_BATCH_CONFIG;
	private static final String JOB_FOLDER = "/src/main/resources/external/";
	private static final String LIBRARY_FOLDER = "/" + BatchConfigInfo.BINARIES_PATH + "/";
	private static final String SPRING_BATCH_PREV = "/spring-batch-config-prev.xml";

	/**
	 * Constructor method
	 * @param jobRegistry THe registry containing Job Names
	 * @param fileService Instance of FileService
	 */
	public SimpleJobConfigurationService(JobRegistry jobRegistry, JobService jobService) {
		this.jobRegistry = jobRegistry;		
		this.jobDependencies = new HashMap<String, List<String>>();
		this.jobXMLFile = new HashMap<String, URI>();
		this.jobHostNames = new HashMap<String, List<JobHost>>();
		this.hostNames = new LinkedList<JobHost>();
		this.currentJobNames =  new LinkedList<String>();
		this.syncService = null;
		this.jobService = jobService;
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getSyncService()
	 */
	@Override
	public SyncService getSyncService() {
		return this.syncService;
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#setSyncService(SyncService)
	 */
	@Override
	public void setSyncService(SyncService syncService) {
		this.syncService = syncService;		
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
			//Iterate through all network interfaces
			while (nets.hasMoreElements())
			{
				NetworkInterface netint = (NetworkInterface) nets.nextElement();
				Enumeration<InetAddress> ips = netint.getInetAddresses();
				//Iterate through all IP adddress
				while(ips.hasMoreElements()) {
					InetAddress ip = ips.nextElement();
					//Take the first address which isn't a loopback and is in the local address
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
		this.hostName = new JobHost(hostName,ipAddr,port);
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentHostName()
	 */
	@Override
	public JobHost getCurrentHostName() {	
		return this.hostName;
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentHostJobs()
	 */
	@Override
	public Collection<String> getCurrentHostJobs() {
		return this.currentJobNames;
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobInstance(String, JobHost)
	 */
	@Override
	public void addJobInstance(String jobName, JobHost hostName) {
		LOGGER.info("hostName: "+hostName.getAddress()+" has job:"+jobName);
		if(this.jobHostNames.containsKey(jobName)) { //Job is existing
			if(!this.jobHostNames.get(jobName).contains(hostName)) {
				this.jobHostNames.get(jobName).add(hostName);
				//LOGGER.info("Added new host: "+hostName.getAddress()+" to "+jobName);
			}
		} else {
			List<JobHost> hostList = new LinkedList<JobHost>();
			hostList.add(hostName);
			this.jobHostNames.put(jobName, hostList);
			//LOGGER.info("New job: "+jobName+" added to host:"+hostName);
		}
		if(!this.hostNames.contains(hostName)) {
			this.hostNames.add(hostName);
			//LOGGER.info("Added to hostNames: "+hostName.getAddress());
		}
		if(!this.currentJobNames.contains(jobName)) {
			if(this.jobHostNames.get(jobName).contains(this.getCurrentHostName()))
				this.currentJobNames.add(jobName);
		}
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#clearJobInstances()
	 */
	@Override
	public void clearJobInstances() {
		this.hostNames = new LinkedList<JobHost>();
		this.jobHostNames = new HashMap<String, List<JobHost>>();
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getHostNames(String)
	 */
	@Override
	public List<JobHost> getHostNames(String jobName) {
		return this.jobHostNames.get(jobName);		
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getAllHostNames()
	 */
	@Override
	public List<JobHost> getAllHostNames() {
		return this.hostNames;	
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getJobStoreURI(String)
	 */
	@Override
	public URI getJobStoreURI(String jobName) {	
		URI XMLFileURI = this.getJobConfigURI(jobName);
		String directoryPath;
		if(XMLFileURI==null) {
			directoryPath = RuntimeVariables.getProjectsRoot()+SimpleJobConfigurationService.JOB_FOLDER+jobName+"/";	
		} else {
			String XMLFilePath = XMLFileURI.getPath();
			directoryPath = XMLFilePath.substring(0, XMLFilePath.lastIndexOf('/'));
		}
		return new File(directoryPath).toURI();
	}

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobDependency(String, String, byte[])
	 */
	@Override
	public void addJobDependency(List<String> jobNames, String destFileName, byte[] fileContents) {
		//Scan for dependencies
		if(this.jobDependencies.isEmpty())
			this.scanJobDependencies();
		if(jobNames==null || jobNames.size()==0) {
			throw new PlatformException("No job names supplied");
		}
		for(String jobName: jobNames) {
			String destPath = this.getJobStoreURI(jobName).getPath();
			//Upload file
			try {
				if(this.jobDependencies.containsKey(jobName)) {
					if(this.jobDependencies.get(jobName).contains(destFileName)) {
						LOGGER.info("Overwriting existing dependency file");
					}
				}
				this.upload(fileContents,destPath+SimpleJobConfigurationService.LIBRARY_FOLDER+destFileName);
			} catch (IOException e) {
				LOGGER.error("Error uploading file: "+destFileName+" to "+destPath);
				throw new PlatformException("Error uploading file: "+destFileName+" to "+destPath,e);
			}
			//Update the jobDependency list
			List<String> dependencyList = null;
			if(this.jobDependencies.containsKey(jobName)) {
				dependencyList = this.jobDependencies.get(jobName);
			} else {
				dependencyList = new LinkedList<String>();
				this.jobDependencies.put(jobName, dependencyList);
			}
			if(!dependencyList.contains(destFileName)) {
				dependencyList.add(destFileName);
			}
			this.jobDependencies.put(jobName, dependencyList);
		}
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobDependencyList
	 * TODO: Return a URI
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
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobConfig(String)
	 */
	@Override
	public Resource getJobConfig(String jobName) {
		if(this.getJobConfigURI(jobName)==null) {
			return null;
		}
		return new FileSystemResource(new File(getJobConfigURI(jobName)));
	}

	/**
	 * Interface method implementation. After setting an XML File, also saves the previous file.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#setJobconfig(String, byte[])
	 */
	@Override
	public void setJobConfig(List<String> jobNames, Resource jobConfigFile) throws PlatformException {
		if(this.jobXMLFile.isEmpty())
			this.scanXMLFiles();
		String destPath = null;
		//Check if jobName has been changed
		try {
			//Using sets to comapare whether the two list of jobNames are equal
			Set<String> jobNamesUserSet = new HashSet<String>();
			Set<String> jobNamesFileSet = new HashSet<String>();
			jobNamesUserSet.addAll(jobNames);
			jobNamesFileSet.addAll(ConfigFileUtils.getJobName(jobConfigFile));
			if(!jobNamesUserSet.equals(jobNamesFileSet)) {
				throw new PlatformException("The Job Name cannot be changed. Expecting: "+jobNamesUserSet.toString()+" Got: "+jobNamesFileSet.toString());
			}
			//Take first jobName to check whether it is a new Job
			String jobName = jobNames.get(0);
			//Code for overwriting file to location
			if(this.getJobConfigURI(jobName)==null) {  //NEW JOB
				destPath=this.getJobStoreURI(jobName).getPath()+SimpleJobConfigurationService.SPRING_BATCH_FILE;
			} else { //Already deployed job. Store the previous file
				destPath = this.getJobConfigURI(jobName).getPath();
				this.createPrevConfigFile(jobName);
			}
			this.upload(ConfigFileUtils.getContents(jobConfigFile).getBytes(), destPath);
			for(String allJobName:jobNames) {
				this.jobXMLFile.put(allJobName,new File(destPath).toURI());
			}
			LOGGER.info("Uploaded job config to "+destPath);
		} catch (IOException ioe) {
			LOGGER.error("Error creating job configuration file for : " + jobNames.toString() + " in location : " + destPath);
			throw new PlatformException("Error creating job configuration file for : " + jobNames.toString() + " in location : " + destPath, ioe);
		}
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#deployJob(String)
	 */
	@Override
	public void deployJob(List<String> jobNames) {
		//Store in a List deployed config files. If a file has been deployed, don't deploy it again.
		Set<URI> deployedJobConfigs = new HashSet<URI>();
		for(String jobName:jobNames) {
			try {
				//Job is already deployed
				if(deployedJobConfigs.contains(this.getJobConfig(jobName).getURI())) {
					continue;
				}
				LOGGER.info("The config file is: "+this.getJobConfig(jobName).getURI());
				this.jobService.getComponentContainer().loadComponent(this.getJobConfig(jobName));
				deployedJobConfigs.add(this.getJobConfig(jobName).getURI());
			}
			catch(Exception e) {
				//Loading failed. Restore previous XML File.
				this.restorePrevConfigFile(jobName);
				if(this.getJobConfig(jobName)!=null) {
					this.jobService.getComponentContainer().loadComponent(this.getJobConfig(jobName));
				}
				throw new PlatformException(e);				
			}
			this.removePrevConfigFile(jobName);
		}
	}

	/**
	 * Gets the URI of a job config
	 */
	private URI getJobConfigURI(String jobName) {
		if(this.jobXMLFile.isEmpty()) {
			this.scanXMLFiles();
		}
		if(this.jobXMLFile.containsKey(jobName)) {
			return this.jobXMLFile.get(jobName);
		}
		return null;
	}

	/**
	 * Creates a temporary file, which is a duplicate of the current config file,
	 * with the name {@link SimpleJobConfigurationService#SPRING_BATCH_PREV}
	 * @param jobName
	 */
	private void createPrevConfigFile(String jobName) {
		File configFile = new File(this.getJobConfigURI(jobName));
		File prevFile = new File(this.getJobStoreURI(jobName).getPath()+SimpleJobConfigurationService.SPRING_BATCH_PREV);
		if(configFile.exists()) {
			if(prevFile.exists()) {
				prevFile.delete();
			}
			configFile.renameTo(prevFile);
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				LOGGER.error("IOException while clearing config File",e);
			}
			prevFile.deleteOnExit();
		}

	}

	/**
	 * This method removes the temporary previous XML File
	 * @param jobName Name of the job
	 */
	private void removePrevConfigFile(String jobName) {
		// DELETE previous XML File
		String prevFilePath = this.getJobStoreURI(jobName).getPath()+SimpleJobConfigurationService.SPRING_BATCH_PREV;
		File prevFile = new File(prevFilePath);
		if(prevFile.exists()){
			prevFile.delete();
		}
	}

	/**
	 * Restores the previous spring batch file, if found
	 * @param jobName Name of the job
	 */
	private void restorePrevConfigFile(String jobName) {
		File configFile = new File(this.getJobConfigURI(jobName));
		if(configFile.exists()) {
			configFile.delete();
		}
		File prevFile = new File(this.getJobStoreURI(jobName).getPath()+SimpleJobConfigurationService.SPRING_BATCH_PREV);
		if(prevFile.exists()) {
			prevFile.renameTo(configFile);
		}
	}

	/**
	 * Scan the jobDirectory for any new dependency files and update JobDependencies.
	 */
	private void scanJobDependencies() {		
		for(String jobName:this.jobRegistry.getJobNames()) {			
			String jobDirectory = this.getJobStoreURI(jobName).getPath()+SimpleJobConfigurationService.LIBRARY_FOLDER;
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
			for(String jobName: ConfigFileUtils.getJobName(new FileSystemResource(jobBeansFile))) {
				this.jobXMLFile.put(jobName, jobBeansFile.toURI());
			}
		}
	}

	/**
	 * Uploads the file to the given path. Creates the file and directory structure, if the file
	 * or parent directory doesn't exist
	 */
	private void upload(byte[] fileContents, String destPath) throws IOException {
		File destFile = new File(destPath);
		//If exists, overwrite
		if(destFile.exists()) {
			destFile.delete();
			destFile.createNewFile();
		}
		//Creating directory structure
		destFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(destFile);
		fos.write(fileContents);						
	}
}
