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
package org.trpr.dataaccess.hbase.auth.kerberos;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.dataaccess.hbase.auth.AuthenticationProvider;

/**
 * A Kerberos implementation of the {@link AuthenticationProvider}.
 * 
 * @author Regunath B
 */

public class KerberosAuthenticationProvider implements AuthenticationProvider, InitializingBean {
	
	/** The Hadoop Kerberos authentication indicator property value*/
	private static final String KERBEROS = "kerberos";
	
	/** The JVM property for setting the default path to the Kerberos configuration location*/
	private static final String KERBEROS_CONFIG_SYSTEM_VARIABLE = "java.security.krb5.conf";

	/** Kerberos authentication properties. These are added to the HBase Configuration during authentication*/
	private Properties kerberosAuthProperties;
	
	/** The location of the Kerberos configuration file */
	private String kerberosConfigLocation;

	/** The location of the Kerberos keytab file */
	private String kerberosKeytabLocation;
	
	/** The Kerberos Principal to use for authentication*/
	private String kerberosPrincipal;

	/**
	 * Interface method implementation. Initializes the specified HBase configuration with Kerberos authentication properties
	 * @see org.trpr.dataaccess.hbase.auth.AuthenticationProvider#authenticatePrincipal(org.apache.hadoop.conf.Configuration)
	 */
	public void authenticatePrincipal(Configuration configuration) throws SecurityException {
		for (Object key : this.kerberosAuthProperties.keySet()) {
			configuration.set(key.toString(), this.kerberosAuthProperties.getProperty(key.toString()));
		}		
		System.setProperty(KerberosAuthenticationProvider.KERBEROS_CONFIG_SYSTEM_VARIABLE,this.kerberosConfigLocation);
		try {
			UserGroupInformation.setConfiguration(configuration);
			UserGroupInformation.loginUserFromKeytab(this.kerberosPrincipal, this.kerberosKeytabLocation);
		} catch (Exception e) {
			throw new SecurityException("Error authenticating Kerberos Principal : " + this.kerberosPrincipal + " .Error message : " + e.getMessage(), e);
		}
	}
	
	/**
	 * Interface method implementation. Checks to see if all mandatory properties have been set
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.kerberosConfigLocation, "The 'kerberosConfigLocation' may not be null");		
		Assert.notNull(this.kerberosKeytabLocation, "The 'kerberosKeytabLocation' may not be null");		
		Assert.notNull(this.kerberosPrincipal, "The 'kerberosPrincipal' may not be null");		
		Assert.notNull(this.kerberosAuthProperties, "The 'kerberosAuthProperties' may not be null");	
		if (this.kerberosAuthProperties.getProperty(AuthenticationProvider.HADOOP_AUTHENTICATION_PROPERTY) == null || 
				!this.kerberosAuthProperties.getProperty(AuthenticationProvider.HADOOP_AUTHENTICATION_PROPERTY).equalsIgnoreCase(KerberosAuthenticationProvider.KERBEROS)) {
			throw new SecurityException("Property : " + AuthenticationProvider.HADOOP_AUTHENTICATION_PROPERTY + " must be of value : " + KerberosAuthenticationProvider.KERBEROS);
		}
	}
	
	/** Start Getter/Setter methods*/
	public Properties getKerberosAuthProperties() {
		return this.kerberosAuthProperties;
	}
	public void setKerberosAuthProperties(Properties kerberosAuthProperties) {
		this.kerberosAuthProperties = kerberosAuthProperties;
	}
	public String getKerberosConfigLocation() {
		return this.kerberosConfigLocation;
	}
	public void setKerberosConfigLocation(String kerberosConfigLocation) {
		this.kerberosConfigLocation = kerberosConfigLocation;
	}
	public String getKerberosKeytabLocation() {
		return this.kerberosKeytabLocation;
	}
	public void setKerberosKeytabLocation(String kerberosKeytabLocation) {
		this.kerberosKeytabLocation = kerberosKeytabLocation;
	}
	public String getKerberosPrincipal() {
		return this.kerberosPrincipal;
	}
	public void setKerberosPrincipal(String kerberosPrincipal) {
		this.kerberosPrincipal = kerberosPrincipal;
	}			
	/** End Getter/Setter methods*/

}
