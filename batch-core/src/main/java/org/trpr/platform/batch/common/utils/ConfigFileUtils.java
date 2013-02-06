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
package org.trpr.platform.batch.common.utils;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code> {@link ConfigFileUtils} </code> provides methods for performing useful 
 * operations on Spring Batch configuration file contents.
 * 
 * @author devashishshankar
 * @version 1.0 4 Feb, 2013
 */
public class ConfigFileUtils {

	/** Tag names used in spring batch files */
	private static final String BATCH_JOB_TAG = "batch:job";
	private static final String ID_PROP = "id";

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(ConfigFileUtils.class);

	/**
	 * Gets the job name from Config file
	 * @param configFile job config file or its contents as a <code> Resource </code>
	 * @return job name, null if unable to find a job name.
	 */
	public static String getJobName(Resource configFile) {
		return ConfigFileUtils.getJobName(new ByteArrayResource(ConfigFileUtils.getContents(configFile).getBytes()));
	}

	/**
	 * Gets the job name from Config file
	 * @param configFile job config file contents as a <code> ByteArrayResource </code>
	 * @return job name, null if unable to find a job name.
	 */	
	public static String getJobName(ByteArrayResource configFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(configFile.getInputStream());
			Element docEle = dom.getDocumentElement();
			//get a nodelist of nodes with the name "ConfigFileUtils.BATCH_JOB_TAG" 
			NodeList nl = docEle.getElementsByTagName(ConfigFileUtils.BATCH_JOB_TAG);
			//Loop over all found nodes
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
					//get the element
					Element el = (Element)nl.item(i);
					if(el.hasAttribute(ConfigFileUtils.ID_PROP)) {
						return el.getAttribute(ConfigFileUtils.ID_PROP);
					}
				}
			}	
		}
		catch(Exception e) {
			LOGGER.error("Unable to get the job name from the given Spring Batch configuration file", e);
			throw new PlatformException(e);
		}
		return null;
	}
	
	/**
	 * Gets the contents of a <code>Resource</code> in a single String
	 * @param resource Resource to be read
	 * @return Contents as a <code>String<code/>
	 */
	public static String getContents(Resource resource) {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(resource.getInputStream(), writer, "UTF-8");
		} catch (IOException e) {
			LOGGER.error("Exception while reading file "+resource.getFilename(),e);
		}
		return writer.toString();
	}
}
