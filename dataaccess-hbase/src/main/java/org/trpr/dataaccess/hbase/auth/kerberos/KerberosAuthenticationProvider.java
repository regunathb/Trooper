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

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.zookeeper.Shell;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.dataaccess.hbase.auth.AuthenticationProvider;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * A Kerberos implementation of the {@link AuthenticationProvider}.
 * 
 * @author Regunath B
 */

public class KerberosAuthenticationProvider implements AuthenticationProvider, InitializingBean {
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(KerberosAuthenticationProvider.class);
	
	/** The Hadoop Kerberos authentication indicator property value*/
	private static final String KERBEROS = "kerberos";
	
	/** The JVM property for setting the default path to the Kerberos configuration location*/
	private static final String KERBEROS_CONFIG_SYSTEM_VARIABLE = "java.security.krb5.conf";
	
	/** The Kerberos kinit command and defaults */
	private static final String HADOOP_KINIT_COMMAND = "hadoop.kerberos.kinit.command";
	private static final String HADOOP_KINIT_COMMAND_DEFAULT = "kinit";
	private static final String HADOOP_KINIT_COMMAND_REFRESH_FLAG = "-R";
		
	/** The default call frequency for TGT renewal*/
	private static final int CALL_TGT_RENEWAL_CHECK_MINUTES = 60;

	/** Kerberos authentication properties. These are added to the HBase Configuration during authentication*/
	private Properties kerberosAuthProperties;
	
	/** The location of the Kerberos configuration file */
	private String kerberosConfigLocation;

	/** The location of the Kerberos keytab file */
	private String kerberosKeytabLocation;
	
	/** The Kerberos Principal to use for authentication*/
	private String kerberosPrincipal;
	
	/** The TGT renewal frequency in minutes */
	private int callTGTRenewalCheckMinutes = CALL_TGT_RENEWAL_CHECK_MINUTES;

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
			UserGroupInformation loggedInUser = UserGroupInformation.getLoginUser();
			LOGGER.info("Currently logged in Kerberos principal : " + loggedInUser);
			new TGTRenewalThread(configuration, loggedInUser);
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
	
	/**
	 * Helper class to renew Kerberos TGT. This thread only invokes the {@link UserGroupInformation#checkTGTAndReloginFromKeytab()} at the configured interval
	 */
	class TGTRenewalThread extends Thread {
		Configuration configuration;
		UserGroupInformation ugi;
		TGTRenewalThread(Configuration configuration, UserGroupInformation ugi) {
			this.configuration = configuration;
			this.ugi = ugi;
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
			setName(ugi.getUserName() + " TGT_Renewer");
			start();
		}
		/**
		 * Calls the {@link UserGroupInformation#checkTGTAndReloginFromKeytab()} repeatedly after the sleep duration. Note that the actual TGT renewal frequency
		 * is determined by the lifetime of the TGT and the logic implemented by the UserGroupInformation around it.
		 */
		public void run() {
			while(true) {
				try {
					Thread.sleep((long)(getCallTGTRenewalCheckMinutes() * 60000));
					// try to renew the ticket cache, if it exists and tickets are renewable
					LOGGER.debug("Try to refresh the Kerberos ticket cache");
					Shell.execCommand(this.configuration.get(HADOOP_KINIT_COMMAND,HADOOP_KINIT_COMMAND_DEFAULT), HADOOP_KINIT_COMMAND_REFRESH_FLAG);
				} catch (IOException ioe) {
					LOGGER.debug("Refresh of Kerberos ticket cache failed with reason : " + ioe.getMessage());
					// try to check and reload the TGT from the keytab
					try {
						LOGGER.debug("Check TGT and attempt relogin from Kerberos keytab");
						ugi.checkTGTAndReloginFromKeytab();
					} catch (IOException ie) {
						LOGGER.error("Error renewing Kerberos TGT for user : " + ugi.getUserName(), ie); 
						// just log the error and don't exit the Thread. TGT expiry will eventually cause HBase read/writes to fail. Continue to try until that happens
					}
				} catch (InterruptedException e) {
					// ignore. This is a daemon thread and will die when the JVM exits
				}
			}
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
	public int getCallTGTRenewalCheckMinutes() {
		return this.callTGTRenewalCheckMinutes;
	}
	public void setCallTGTRenewalCheckMinutes(int callTGTRenewalCheckMinutes) {
		this.callTGTRenewalCheckMinutes = callTGTRenewalCheckMinutes;
	}				
	/** End Getter/Setter methods*/

}
