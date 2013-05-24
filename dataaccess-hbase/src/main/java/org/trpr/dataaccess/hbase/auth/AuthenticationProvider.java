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
package org.trpr.dataaccess.hbase.auth;

import org.apache.hadoop.conf.Configuration;

/**
 * An interface for enabling authentication in HBase data access. The authentication method signatures are generic leaving the details to specific
 * implementations of this interface.
 * 
 * @author Regunath B
 */

public interface AuthenticationProvider {
	
	/** The Hadoop configuration property that indicates the authentication mechanism to use*/
	public static final String HADOOP_AUTHENTICATION_PROPERTY = "hadoop.security.authentication";
	
	/**
	 * Authenticates the currently active Principal to permit access to HBase data store
	 * @param configuration the Hadoop configuration for the target cluster
	 * @throws SecurityException in case of authentication failures
	 */
	public void authenticatePrincipal(Configuration configuration) throws SecurityException;
	
}
