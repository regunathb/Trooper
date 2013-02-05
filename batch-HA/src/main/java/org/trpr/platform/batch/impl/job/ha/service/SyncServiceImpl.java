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
package org.trpr.platform.batch.impl.job.ha.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.batch.common.utils.ConfigFileUtils;
import org.trpr.platform.batch.impl.spring.web.SynchronizationController;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * <code> SyncServiceImpl </code> is an implementation of @link {SyncService}. Provides 
 * methods for deploying jobs into different Trooper instances
 * 
 * @author devashishshankar
 * @version 1.0, Jan 31, 2013
 */
public class SyncServiceImpl implements SyncService {

	/** Logger instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(SyncServiceImpl.class);

	/** Success message returned by server */
	private static final String SUCCESS_STRING = SynchronizationController.SUCCESS_STRING;

	/** Protocol used to send request */
	private static final String PROTOCOL = "http://";	

	/** Encoding scheme to be used for the requests */
	private static final String ENCODING = "UTF-8";

	/** Trooper services being used by this class */
	private JobConfigurationService jobConfigService;

	/** Autowired Default Constructor */
	@Autowired
	public SyncServiceImpl(JobConfigurationService jobConfigurationService) {
		this.jobConfigService = jobConfigurationService;
	}

	/**
	 * Checks whether the jobs running in the current server exist in all the other servers,
	 * if not, pushes the missing jobs
	 */
	@Override
	public void syncAllHosts() {
		for(String jobName: this.jobConfigService.getCurrentHostJobs()) {
			for(JobHost serverName: this.jobConfigService.getAllHostNames()) {
				if(!this.jobConfigService.getHostNames(jobName).contains(serverName)) {
					this.pushJobToHost(jobName, serverName.getAddress());
				}
			}
		}
	}
	
	/**
	 * Pusher methods start.
	 * These methods send a request to the Trooper server to which jobs have to be deployed
	 */
	/**
	 * Interface Method Implementation
	 * @see SyncService#pushJobToHost(String, String)
	 */
	@Override
	public boolean pushJobToHost(String jobName, String serverName) {
		String configReturnString = this.pushConfig(jobName, serverName);
		if(configReturnString.equals(SyncServiceImpl.SUCCESS_STRING)) {
			String depReturnString = this.pushDependencies(jobName, serverName);
			if(!depReturnString.trim().equals(SyncServiceImpl.SUCCESS_STRING)) {
				LOGGER.error("Error while pushing dependency file. The server returns: "+depReturnString);
			}
		} else {
			LOGGER.error("Error while deploying job. The server returns: "+configReturnString);
		}
		//Even if one of the above two steps fail, try to deploy
		String deployReturnString = this.deploy(jobName, serverName);
		if(deployReturnString.equals(SyncServiceImpl.SUCCESS_STRING)) {
			return true;
		} else {
			LOGGER.error("Error while pushing configuration file. The server returns: "+deployReturnString);
		}
		return false;
	}

	/**
	 * Pushes a configuration file
	 * @param jobName name of the job
	 * @param serverName server name in format IP:port
	 * @return Host response, empty string if no response received from server, "exception" in case of any other error
	 */
	private String pushConfig(String jobName,String servername) {
		String urlToConnect = SyncServiceImpl.PROTOCOL+servername+"/"+jobName+SynchronizationController.PUSH_JOB_URL;
		LOGGER.info("Pushing "+jobName+" configuration file to "+servername);
		try {
			String contents = ConfigFileUtils.getContents(this.jobConfigService.getJobConfig(jobName));
			//Percent(URI) Encoding should be necessary as the data is being passed as an URI. 
			String data = URLEncoder.encode("jobName", SyncServiceImpl.ENCODING) + "=" + URLEncoder.encode(jobName, SyncServiceImpl.ENCODING);
			data += "&" + URLEncoder.encode("configFile", SyncServiceImpl.ENCODING) + "=" + URLEncoder.encode(contents, SyncServiceImpl.ENCODING);
			return this.pushRequest(data, urlToConnect);
		} catch (IOException e) {
			LOGGER.error("IOException while pushing spring batch config file of "+jobName+" to "+servername, e);
		} 
		return "Unexpected exception. See log for details";
	}

	/**
	 * Push all the dependency files for a given job
	 * @param jobName name of the job
	 * @param serverName server name in format IP:port
	 * @return Host response, empty string if no response received from server
	 */
	private String pushDependencies(String jobName,String servername) {
		String urlToConnect = SyncServiceImpl.PROTOCOL+servername+"/"+jobName+SynchronizationController.PUSH_DEP_URL;
		String returnStr=SyncServiceImpl.SUCCESS_STRING;
		FileUpload fileUpload = new FileUpload() ;
		if(this.jobConfigService.getJobDependencyList(jobName)!=null) {
			for(String dep : this.jobConfigService.getJobDependencyList(jobName)) {
				File depFile = new File(this.jobConfigService.getJobStoreURI(jobName).getPath()+"/lib/"+dep);
				String response = fileUpload.executeMultiPartRequest(urlToConnect, depFile, depFile.getName(), jobName) ;
				if(response.equals(SyncServiceImpl.SUCCESS_STRING)) {
					returnStr = SyncServiceImpl.SUCCESS_STRING;
				} else {
					returnStr = response;
				}
			}
		}
		return returnStr;
	}

	/**
	 * Send a loading request	 
	 * @param jobName name of the job
	 * @param serverName server name in format IP:port
	 * @return Host response, empty string if no response received from server, "exception" in case of any other error
	 */
	private String deploy(String jobName,String servername) {
		try {
			String urlToConnect = SyncServiceImpl.PROTOCOL+servername+"/"+jobName+SynchronizationController.PUSH_URL;
			String data = URLEncoder.encode("jobName",SyncServiceImpl.ENCODING) + "=" + URLEncoder.encode(jobName,SyncServiceImpl.ENCODING);
			return this.pushRequest(data, urlToConnect); 
		}
		catch(IOException e) {
			LOGGER.error("IOException while sending a job load request for job: "+jobName+" to "+servername, e);
		}
		return "Unexpected exception. See log for details";
	}

	/**
	 * Generic function which is finally used to send a request. 
	 * @param data The POST request string
	 * @param urlToConnect The URL of server
	 * @return Response from server, empty string if no response received
	 */
	private String pushRequest(String data,String urlToConnect) {
		PrintWriter writer = null;
		OutputStream output = null;
		try {
			//Connecting..
			URL url = new URL(urlToConnect);
			URLConnection conn = url.openConnection();
			//POST method
			conn.setDoOutput(true);
			output = conn.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output,SyncServiceImpl.ENCODING), true); // true = Autoflush
			//Writing the response
			writer.println(data);
			//Reading the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String retline;
			String finalReturnValue = "";
			while ((retline = reader.readLine()) != null) {
				finalReturnValue+=retline;				    
			}
			return finalReturnValue;
		} catch (IOException e) {
			LOGGER.error("Exception while pushing request",e);
		} 
		return "Unexpected exception. See log for details";
	}
}
