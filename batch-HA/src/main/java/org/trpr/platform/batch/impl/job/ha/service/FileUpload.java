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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * <code>FileUpload </code> is a class providing methods to easily upload files via HTTP POST
 * 
 * @author devashishshankar
 * @version 1.0, 31 Jan, 2013
 */
public class FileUpload {

	/** Logger instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(FileUpload.class);

	/**
	 * A generic method to execute any type of Http Request and constructs a response object
	 * @param requestBase the request that needs to be exeuted
	 * @return server response as <code>String</code>
	 */
	private static String executeRequest(HttpRequestBase requestBase){
		//The string holding the server response
		String responseString = "" ;
		InputStream responseStream = null ;
		HttpClient client = new DefaultHttpClient () ;
		try{
			HttpResponse response = client.execute(requestBase) ;
			if (response != null){
				HttpEntity responseEntity = response.getEntity() ;
				if (responseEntity != null){
					responseStream = responseEntity.getContent() ;
					if (responseStream != null){
						BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
						String responseLine = br.readLine() ;
						String tempResponseString = "" ;
						while (responseLine != null){
							tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator") ;
							responseLine = br.readLine() ;
						}
						br.close() ;
						if (tempResponseString.length() > 0){
							responseString = tempResponseString ;
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception while uploading file to server",e);
		}
		client.getConnectionManager().shutdown() ;
		return responseString ;
	}

	/**
	 * Method that builds the multi-part form data request. This method has been configured to upload
	 * job Dependency files for Trooper
	 * @param urlString the urlString to which the file needs to be uploaded
	 * @param file the actual file instance that needs to be uploaded
	 * @param fileName name of the file, just to show how to add the usual form parameters
	 * @param fileDescription some description for the file, just to show how to add the usual form parameters
	 * @return server response as <code>String</code>
	 */
	public String executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription) {
		HttpPost postRequest = new HttpPost (urlString) ;
		try{
			MultipartEntity multiPartEntity = new MultipartEntity () ;
			multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : "")) ;
			multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName())) ;
			FileBody fileBody = new FileBody(file, "application/octect-stream") ;
			multiPartEntity.addPart("attachment", fileBody) ;
			postRequest.setEntity(multiPartEntity) ;
		}catch (UnsupportedEncodingException ex){
			LOGGER.error("Error while forming multiPart request",ex);
		}	
		return executeRequest (postRequest) ;
	} 
}