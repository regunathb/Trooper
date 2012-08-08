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

package org.trpr.platform.core.impl.management.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;

/**
 * Class <code>BeanInstanceMetadataNamingStrategy</code> class is a sub-type of the Spring {@link MetadataNamingStrategy} that permits
 * bean instances to influence the JMX name used to bind to the MBean server. 
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
public class BeanInstanceMetadataNamingStrategy extends MetadataNamingStrategy {

	/**
	 * Overriden superclass method. If the specified managedBean is of type {@link InstanceAwareMBean}, 
	 * calls {@link InstanceAwareMBean#getMBeanNameSuffix()} to determine if a bean instance specific name suffix is to be included.
	 * Appends the suffix if one is returned or else continues with the default behavior of {@link MetadataNamingStrategy#getObjectName(Object, String)}
	 * @see org.springframework.jmx.export.naming.MetadataNamingStrategy#getObjectName(java.lang.Object, java.lang.String)
	 */
	public ObjectName getObjectName(Object managedBean, String beanKey) throws MalformedObjectNameException {
		if (managedBean instanceof InstanceAwareMBean) {
			String nameSuffix = ((InstanceAwareMBean)managedBean).getMBeanNameSuffix(managedBean, beanKey);
			if (nameSuffix != null) {
				return ObjectName.getInstance(super.getObjectName(managedBean, beanKey).toString() + nameSuffix);
			}
		}
		return super.getObjectName(managedBean, beanKey);
	}
		
}
