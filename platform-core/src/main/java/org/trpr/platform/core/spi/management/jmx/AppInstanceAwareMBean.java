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

import org.trpr.platform.core.PlatformConstants;

/**
 * Class <code>AppInstanceAwareMBean</code> class is an implementation of {@link InstanceAwareMBean} interface that derives the MBean name from  
 * the JVM system property identified by PlatformConstants.CCELL_APP_NAME. 
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
public class AppInstanceAwareMBean implements InstanceAwareMBean {

	/**
	 * Interface method implementation. Returns null or the JVM system property identified by PlatformConstants.CCELL_APP_NAME.
	 * Ignores the passed in managed bean instance and its identifier
	 * @see InstanceAwareMBean#getMBeanNameSuffix(Object, String)
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey) {
		return System.getProperty(PlatformConstants.CCELL_APP_NAME);
	}

}
