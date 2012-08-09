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
package org.trpr.dataaccess;

import org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean;
import org.trpr.platform.core.spi.persistence.PersistenceHandler;

/**
 * <code>RDBMSHandler<code> is an implementation of {@link PersistenceHandler} that caters to persisting data into an RDBMS.
 * This class is abstract and may be extended to create specific framework handlers - for example to persist using Hibernate or JPA or iBatis. 

 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public abstract class RDBMSHandler implements PersistenceHandler, InstanceAwareMBean {

	/**
	 * No arg constructor.
	 */
	public RDBMSHandler(){
	}

	/**
	 * Interface method implementation. Returns the passed in bean identifier i.e. beanKey param appended with the Java hashCode of a newly
	 * created Java Object.
	 * @see InstanceAwareMBean#getMBeanNameSuffix(Object, String)
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey) {
		// we could have used the hashCode of the managedBean object which apparently does not work when the object is instantiated using a Spring
		// ApplicationContext. The reason is not known and hence choosing the safer option of creating a new Object and using its hashcode instead.
		return "-" + beanKey + "[" + new Object().hashCode()+ "]";
	}
	
}