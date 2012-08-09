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

package org.trpr.platform.runtime.impl.bootstrap.management.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.trpr.platform.runtime.spi.bootstrap.management.jmx.BootstrapManagedBean;

/**
 * The <code>BootstrapModelMBeanExporter</code> class exports the Bootstrap class's interface as a Model MBean for monitoring via 
 * JMX.
 *  
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public class BootstrapModelMBeanExporter {
	
	/** Name for the Bootstrap MBean*/
	private static final String MBEAN_NAME = "spring.application:type=Trooper,application=Runtime,name=Bootstrap-";	
	
	/** String literals for constructing operation and parameter info objects*/
	private static final String VOID = "void";
	private static final String STRING = "java.lang.String";
	
	/** String literal for Model MBean object reference*/
	private static final String OBJECT_REFERENCE = "ObjectReference";
	
	/** The Platform MBeanServer instance */
	private MBeanServer mbeanServer;
	
	/**
	 * Constructor for this class
	 */
	public BootstrapModelMBeanExporter() {
		this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
	}
	
	/**
	 * Exports the specified BootstrapMXBean interface as a Model MBean
	 * @param bootstrap the BootstrapMXBean interface to export
	 * @throws Exception in case of errors in registering the specified BootstrapMXBean with the JMX sub-system
	 */
	public void exportBootstrapMBean(BootstrapManagedBean bootstrap) throws Exception {
		ModelMBeanInfoSupport mmbInfoSupport = new ModelMBeanInfoSupport(bootstrap.getClass().getName(), "Trooper Runtime startup class",
				null, //attributes
				null, //constructors
				buildModelMBeanOperationInfo(),
				null //notifications
				);
		RequiredModelMBean rmb = new RequiredModelMBean(mmbInfoSupport);
		rmb.setManagedResource(bootstrap, OBJECT_REFERENCE);
		ObjectName objectName = new ObjectName(bootstrap.getMBeanNameSuffix(bootstrap, MBEAN_NAME) != null ? MBEAN_NAME + bootstrap.getMBeanNameSuffix(bootstrap, MBEAN_NAME) : MBEAN_NAME);
		if (this.mbeanServer.isRegistered(objectName)){
			this.mbeanServer.unregisterMBean(objectName);
		}
		this.mbeanServer.registerMBean(rmb, objectName);
	}
	
	/**
	 * Helper method to construct the MBean opeation info objects
	 * @return array of ModelMBeanOperationInfo instances for the BootstrapMXBean interface methods
	 */
	private ModelMBeanOperationInfo[] buildModelMBeanOperationInfo() {
		// the start() operation info
		ModelMBeanOperationInfo startOperationInfo =
	         new ModelMBeanOperationInfo("start","Start Runtime",
	        	null, // no params for this method
	            VOID, ModelMBeanOperationInfo.ACTION);
		// the stop operation info
		ModelMBeanOperationInfo stopOperationInfo =
	         new ModelMBeanOperationInfo("stop","Stop Runtime",
	        	null, // no params for this method
	            VOID, ModelMBeanOperationInfo.ACTION);
		// the destroy operation info
		ModelMBeanOperationInfo destroyOperationInfo =
	         new ModelMBeanOperationInfo("destroy","Shutdown Runtime",
	        	null, // no params for this method
	            VOID, ModelMBeanOperationInfo.ACTION);
		// the reloadLoggingConfigurations operation info
		ModelMBeanOperationInfo reloadLoggingConfigurationsOperationInfo =
	         new ModelMBeanOperationInfo("reloadLoggingConfigurations","Reload Logging configuration",
	        	null, // no params for this method
	            VOID, ModelMBeanOperationInfo.ACTION);
		// the getBootstrapConfigPath operation info
		ModelMBeanOperationInfo getBootstrapConfigPathOperationInfo =
	         new ModelMBeanOperationInfo("getBootstrapConfigPath","Gets the bootstrap config path",
	        	null, // no params for this method
	        	STRING, ModelMBeanOperationInfo.ACTION);
		// the setBootstrapConfigPath operation info
		ModelMBeanOperationInfo setBootstrapConfigPathOperationInfo =
	         new ModelMBeanOperationInfo("setBootstrapConfigPath","Sets the bootstrap config path",
	        		 new MBeanParameterInfo[] {new MBeanParameterInfo("bootstrapConfigPath",STRING,"The bootstrap config path")}, 
	            VOID, ModelMBeanOperationInfo.ACTION);
		return new ModelMBeanOperationInfo[] {startOperationInfo, stopOperationInfo, destroyOperationInfo, 
				reloadLoggingConfigurationsOperationInfo,getBootstrapConfigPathOperationInfo,
				setBootstrapConfigPathOperationInfo};
	}

}
