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
package org.trpr.platform.core.spi.token;

import java.util.Map;

/**
 * The <code>TokenGenerator</code> interface defines methods for generating tokens using the passed-in specifications. Tokens may be     
 * Unique numbers, Passwords, Transaction Ids etc.  
 * 
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */

public interface TokenGenerator {

	/**
	 * Generates a token using the specification defined in the specified Map
	 * @param spec Map containing input specs, if any
	 * @return token generated as per the defined spec
	 */
	public String generate(Map<String, Object> spec);
	
}
