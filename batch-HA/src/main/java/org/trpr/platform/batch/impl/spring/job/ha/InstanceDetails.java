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
package org.trpr.platform.batch.impl.spring.job.ha;

import org.codehaus.jackson.map.annotate.JsonRootName;
/**
 * <code> {@link InstanceDetails} </code> is a container holding meta data about job Instances.
 * Currently, it only holds Host Name.
 * @author devashishshankar
 *
 */
@JsonRootName("details")
public class InstanceDetails {
	/** Description about the instance.**/
	private String description;

	/**Default constructor **/
	public InstanceDetails() {
	}
	/**Parameterized constructor **/
	public InstanceDetails(String description) {
		this.description = description;
	}

	/**
	 * Getter/Setter Methods
	 */

	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}