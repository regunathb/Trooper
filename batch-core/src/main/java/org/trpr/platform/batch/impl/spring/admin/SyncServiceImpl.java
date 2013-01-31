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
import org.trpr.platform.batch.impl.FileUpload;
import org.trpr.platform.batch.impl.spring.web.Host;
import org.trpr.platform.batch.impl.spring.web.SynchronizationController;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
/**
 * <code> SyncServiceImpl </code> is an implementation of @link {SyncService}. Provides 
 * methods for deploying jobs into different Trooper instances
 * @author devashishshankar
 *
 */
public class SyncServiceImpl implements SyncService {

	/**Logger instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(SyncServiceImpl.class);

	private JobConfigurationService jobConfigService;

	@Autowired
	public SyncServiceImpl(JobConfigurationService jobConfigurationService) {
		this.jobConfigService = jobConfigurationService;
	}
	/**
	 * Pusher methods start.
	 * These methods send a request to the Trooper server to which jobs have to be deployed
	 */
	/**
	 * Pushes a job to a given serverName.
	 * @return true on success, false on failure
	 */
	@Override
	public boolean pushJobToServer(String jobName, String serverName) {
		String ret1 = this.pushConfig(jobName, serverName);
		if(ret1.equals("success")) {
			String ret2 = this.pushDependencies(jobName, serverName);
			if(ret2.equals("success")) {
				String ret3 = this.deploy(jobName, serverName);
				if(ret3.equals("success")) {
					return true;
				}
				else {
					LOGGER.error("Error while pushing configuration file. The server returns: "+ret3);
				}
			}
			else {
				LOGGER.error("Error while pushing dependency file. The server returns: "+ret2);
			}
		}
		else {
			LOGGER.error("Error while deploying job. The server returns: "+ret1);
		}

		return false;
	}
	/**
	 * Pushes a configuration file
	 * @return Server response, empty string if no response received from server, "exception" in case of any other error
	 */
	@Override
	public String pushConfig(String jobName,String servername) {
		String urlToConnect = "http://"+servername+"/"+jobName+SynchronizationController.PUSH_JOB_URL;
		LOGGER.info("Pushing "+jobName+" configuration file to "+servername);
		try {
			String contents = this.jobConfigService.getFileContents(this.jobConfigService.getXMLFilePath(jobName));
			//Percent(URI) Encoding should be necessary as the data is being passed as an URI.
			String data = URLEncoder.encode("jobName", "UTF-8") + "=" + URLEncoder.encode(jobName, "UTF-8");
			data += "&" + URLEncoder.encode("configFile", "UTF-8") + "=" + URLEncoder.encode(contents, "UTF-8");
			return this.pushRequest(data, urlToConnect);
		} catch (IOException e) {
			LOGGER.error("IOException while pushing spring batch config file of "+jobName+" to "+servername, e);
		} 
		return "exception";
	}

	/**
	 * Push all the dependency files for a given job
	 * @return Server response, empty string if no response received from server
	 */
	@Override
	public String pushDependencies(String jobName,String servername) {
		String urlToConnect = "http://"+servername+"/"+jobName+SynchronizationController.PUSH_DEP_URL;
		String returnStr="success";
		FileUpload fileUpload = new FileUpload() ;
		System.out.println(this.jobConfigService);
		for(String dep : this.jobConfigService.getJobDependencyList(jobName)) {
			File depFile = new File(this.jobConfigService.getJobDirectory(jobName)+"/lib/"+dep);
			String response = fileUpload.executeMultiPartRequest(urlToConnect, depFile, depFile.getName(), jobName) ;
			//TODO: Improve this
			if(response.equals("success"))
				returnStr = "success";
			else
				returnStr = response;
		}
		return returnStr;
	}
	/**
	 * Send a loading request	 
	 * @return Server response, empty string if no response received from server, "exception" in case of any other error
	 */
	@Override
	public String deploy(String jobName,String servername) {
		try {
			String urlToConnect = "http://"+servername+"/"+jobName+SynchronizationController.PUSH_URL;
			String data = URLEncoder.encode("jobName", "UTF-8") + "=" + URLEncoder.encode(jobName, "UTF-8");
			return this.pushRequest(data, urlToConnect); 
		}
		catch(IOException e) {
			LOGGER.error("IOException while sending a job load request for job: "+jobName+" to "+servername, e);
		}
		return "Unexpected error";
	}

	/**
	 * Generic function which is finally used to send a request. 
	 * @param data The POST request string
	 * @param urlToConnect The URL of server
	 * @return Response from server, empty string if no response received
	 */
	public String pushRequest(String data,String urlToConnect) {

		PrintWriter writer = null;
		OutputStream output = null;
		try {
			//Connecting..
			URL url = new URL(urlToConnect);
			URLConnection conn = url.openConnection();
			//POST method
			conn.setDoOutput(true);
			output = conn.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true); // true = Autoflush
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
		return "";
	}

	/**
	 * Checks whether the jobs running in the current server exist in all the other servers,
	 * if not, pushes the missing jobs
	 */
	@Override
	public void syncAllServers() {
		for(String jobName: this.jobConfigService.getCurrentServerJobs()) {
			for(Host serverName: this.jobConfigService.getAllServerNames()) {
				if(!this.jobConfigService.getServerNames(jobName).contains(serverName)) {
					this.pushJobToServer(jobName, serverName.getAddress());
				}
			}
		}

	}

}
