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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.trpr.platform.batch.common.JobHost;
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

	/** No. of times retry should be done if server response is an error */
	public int maxRetryCount = 3;

	/** Autowired Default Constructor */
	@Autowired
	public SyncServiceImpl(JobConfigurationService jobConfigurationService) {
		this.jobConfigService = jobConfigurationService;
	}

	/**
	 * Interface method implementation.
	 * @see SyncService#syncAllHosts()
	 */
	@Override
	public void syncAllHosts() {
		for(String jobName: this.jobConfigService.getCurrentHostJobs()) {
			for(JobHost serverName: this.jobConfigService.getAllHostNames()) {
				if(this.jobConfigService.getHostNames(jobName)!=null)
					if(!this.jobConfigService.getHostNames(jobName).contains(serverName)) {
						LOGGER.info("Server: "+serverName.getAddress()+" doesnot have "+jobName+". Hence pushing");
						if(!this.pushJobToHostWithRetry(jobName, serverName.getAddress(),this.maxRetryCount)) {
							LOGGER.info("Failed to push jobName="+jobName+" servername = "+serverName.getAddress());
						}
					}
			}
		}
	}

	/**
	 * Interface method implementation.
	 * @see SyncService#pullRequest(String)
	 */
	@Override
	public void pullRequest(String serverName) {
		try {
			String urlToConnect = SyncServiceImpl.PROTOCOL+serverName+"/"+SynchronizationController.PULL_URL;
			String data = URLEncoder.encode("serverName", SyncServiceImpl.ENCODING) + "=" + URLEncoder.encode(this.jobConfigService.getCurrentHostName().getAddress(), SyncServiceImpl.ENCODING);
			this.request(data, urlToConnect);
			LOGGER.info("Sent a pull request to "+serverName);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Exception while pull request",e);
		}
	}

	/**
	 * Interface Method Implementation
	 * @see SyncService#pushJobToHostWithRetry(String, String)
	 */
	@Override
	public boolean pushJobToHostWithRetry(String jobName, String serverName,int retryCount) {
		int count = 0;
		while(!this.pushJobToHost(jobName, serverName)) {
			count++;
			if(count>=retryCount)
				return false;
		}
		return true;
	}

	/**
	 * Interface Method Implementation
	 * @see SyncService#pushJobToHost(String, String)
	 */
	public boolean pushJobToHost(String jobName, String serverName) {
		serverName = SyncServiceImpl.PROTOCOL+serverName+SynchronizationController.PUSH_URL;

		HttpPost postRequest = new HttpPost (serverName) ;
		try {
			MultipartEntity multiPartEntity = new MultipartEntity () ;
			multiPartEntity.addPart("jobName", new StringBody(jobName)) ;

			FileBody fileBody = new FileBody(this.jobConfigService.getJobConfig(jobName).getFile(), "application/octect-stream") ;
			multiPartEntity.addPart("jobConfig", fileBody) ;

			if(this.jobConfigService.getJobDependencyList(jobName)!=null) {
				for(String dependency : this.jobConfigService.getJobDependencyList(jobName)) {
					File depFile = new File(this.jobConfigService.getJobStoreURI(jobName).getPath()+"/lib/"+dependency);
					FileBody depFileBody = new FileBody(depFile);
					multiPartEntity.addPart("depFiles[]", depFileBody);								
				}
			}
			postRequest.setEntity(multiPartEntity) ;
		}catch (UnsupportedEncodingException ex){
			LOGGER.error("Error while forming multiPart request",ex);
		} catch (IOException e) {
			LOGGER.error("Error while forming multiPart request",e);
		}	
		String retValue = org.trpr.platform.batch.impl.job.ha.service.FileUpload.executeRequest(postRequest);
		LOGGER.info("Server returns: "+retValue);
		if(retValue.trim().equalsIgnoreCase(SyncServiceImpl.SUCCESS_STRING)) {
			return true;
		}
		return false;
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.SyncService#deployJobToAllHosts(String)
	 */
	@Override
	public void deployJobToAllHosts(String jobName) {
		//First check if the job is an HA job
		if(this.jobConfigService.getCurrentHostJobs().contains(jobName)) {
			if(this.jobConfigService.getAllHostNames()!=null) {
				for(JobHost host : this.jobConfigService.getAllHostNames()) {
					if(!host.equals(this.jobConfigService.getCurrentHostName())) {
						if(!this.pushJobToHostWithRetry(jobName, host.getAddress(),this.maxRetryCount)) {
							LOGGER.info("Failed to push: "+jobName+" to "+host.getAddress());
						}
					}
				}
			}
		}
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.SyncService#deployAllJobsToHost(JobHost)
	 */
	@Override
	public void deployAllJobsToHost(String hostAddress) {
		if(hostAddress.equals(this.jobConfigService.getCurrentHostName().getAddress())) {//Don't push to the same server
			return;
		}
		LOGGER.info("Pushing all jobs to "+hostAddress);
		for(String jobName : this.jobConfigService.getCurrentHostJobs()) {
			if(!this.pushJobToHostWithRetry(jobName, hostAddress,this.maxRetryCount)) {
				LOGGER.info("Failed to push: "+jobName+" to "+hostAddress);
			}
			else {
				LOGGER.info("Pushed job "+jobName+" to server: "+hostAddress);
			}
		}
	}

	/**
	 * Generic method which is used to send a request from request String
	 * @param data The POST request string
	 * @param urlToConnect The URL of server
	 * @return Response from server, empty string if no response received
	 */
	private String request(String data,String urlToConnect) {
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
