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
package org.trpr.platform.core.spi.token.filter;

/**
 * The <code>TokenFilter</code> interface defines methods for applying various filtering 
 * behavior on generated token strings such as - ignoring duplicates, ignoring specific patterns, formatting etc.  
 * 
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */
public interface TokenFilter {

	/**
	 * Checks to see if this filter accepts the specified token for further processing. 
	 * @param token the token String to check acceptance for subsequent filtering 
	 * @return boolean indicating if this filter accepts the specified token for subsequent filtering calls
	 */
	public boolean accept(String token);
	
	/**
	 * Applies the behavior of this filter on the specified token. This method is expected to be called only after a call to  
	 * {@link TokenFilter#accept(String)} returns boolean "true".
	 * @param token the token string to filter
	 * @return the specified token or a modified one if it passes through this filter or null in case this filter rejects the token
	 */
	public String applyFilter(String token);
	
	/**
	 * Returns the number of tokens rejected by this filter
	 * @return the number of tokens rejected by this filter
	 */
	public long getRejectedNumberCount();
	
}
