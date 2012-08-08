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
package org.trpr.platform.core.impl.token.uid;

import java.util.Map;
import java.util.UUID;

import org.trpr.platform.core.spi.token.TokenGenerator;

/**
 * The <code>UUIDGenerator</code> is a {@link TokenGenerator} that generates a unique identifier string using the 
 * Java 5 {@link java.util.UUID} class  
 * 
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */
public class UUIDGenerator implements TokenGenerator {
	
	/**
	 * Interface method implementation. Returns a UID generated using the Java 5 UUID implementation
	 * @see TokenGenerator#generate(Map)
	 */
	public String generate(Map<String, Object> spec) {
		return UUID.randomUUID().toString();
	}
	
}
