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
package org.trpr.platform.batch.impl.job.ha;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * <code> {@link JobInstanceDetails} </code> is a container holding meta data about job Instances.
 * Currently, it only holds Host Name.
 * 
 * @author devashishshankar
 * @version 1.0, Jan 31, 2013
 */
@JsonRootName("details")
public class JobInstanceDetails {
	
	/** Description about the instance.**/
	private String hostName;

	/**Default constructor **/
	public JobInstanceDetails() {
	}
	
	/**Parameterized constructor **/
	public JobInstanceDetails(String hostName) {
		this.hostName = hostName;
	}

	/** Getter/Setter Methods  */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostName() {
		return hostName;
	}
	/** End getter/Setter Methods  */
}