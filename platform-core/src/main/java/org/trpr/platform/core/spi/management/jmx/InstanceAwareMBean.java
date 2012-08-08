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

package org.trpr.platform.core.spi.management.jmx;

/**
 * Class <code>InstanceAwareMBean</code> interface provides behavior common to all MBeans that are instance aware i.e.
 * might be instantiated multiple times in a JVM and therefore have a need to influence JMX operations like object naming.  
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
public interface InstanceAwareMBean {
	
	/**
	 * Returns null or the instance specific MBean name suffix. Note that this suffix is added to the derived MBean object name 
	 * at the time of  binding to the MBean server
	 * @param managedBean the type instance that implements this interface
	 * @param beanKey the type instance identifier in the application container, for instance the Spring bean name in an ApplicationContext
	 * @return null or the MBean object name suffix
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey); 

}
