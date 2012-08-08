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
package org.trpr.platform.core.impl.token;

import java.util.List;
import java.util.Map;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.token.TokenGenerator;
import org.trpr.platform.core.spi.token.filter.TokenFilter;
import org.trpr.platform.core.util.PlatformUtils;

/**
 * The <code>AbstractDecoratedTokenGenerator</code> is an implementation of the {@link TokenGenerator} that uses a regular TokenGenerator implementation
 * to generate a token and then subjects it to a set of decorations (or) filters that are configured on instances of this class.
 * The filters, when applied, may lead to rejection of a certain token. In this case, this TokenGenerator gets a new token
 * to process from the configured TokenGenerator until all filters pass the token. This behavior is in conformance with the TokenGenerator
 * interface behavior that this class supports. 
 * 
 * This generator takes a "MaxAttempts" parameter as part of the spec that determines the number of times this generator will fetch new tokens 
 * from the configured non-decorated TokenGenerator when filters reject a token. This TokenGenerator logs an error
 * message when the attempts are exhausted and return null eventually.  
 *   
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */
public abstract class AbstractDecoratedTokenGenerator implements TokenGenerator {

	/** The max value key in the spec*/
	public static String MAX_ATTEMPTS = "MaxAttempts";

	/** The default number of attempts to get a token that passes all filters*/
	public static final int DEFAULT_MAX_ATTEMPTS = 3;
		
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(AbstractDecoratedTokenGenerator.class);
	
	/**
	 * Interface method implementation. Returns a token that passes all decorations/filters configured.
	 * @see TokenGenerator#
	 */
	public String generate(Map<String, Object> spec) {
		int maxAttempts = DEFAULT_MAX_ATTEMPTS;
		if (spec != null) {
			maxAttempts = PlatformUtils.getInt(spec.get(MAX_ATTEMPTS), maxAttempts);
		}		
		int attempt = 0;
		String token = null;
		while (attempt < maxAttempts) {
			token = getFilteredToken(spec);
			if (token != null) {
				return token;
			}
			attempt++;
		}
		LOGGER.error("Max attempts exhausted getting token that matches used filters. Returning null. Max attempts is : " + maxAttempts);
		return token;
	}
	
	/**
	 * Helper method to get a token from the getNonDecoratedTokenGenerator and apply all filters on it
	 * @param spec the spec to use in token generation
	 * @return null or the filtered token string
	 */
	private String getFilteredToken(Map<String, Object> spec) {
		String token = getNonDecoratedTokenGenerator().generate(spec);
		for (TokenFilter filter : getTokenFilters()) {
			if (filter.accept(token)) {
				token = filter.applyFilter(token);
				if (token == null) {
					return null; // no point in processing other filters if one of them has rejected the token anyway
				}
			} else {
				return null;
			}
		}
		return token;
	}

	/** ==== Abstract methods to be implemented by sub-types===*/
	public abstract TokenGenerator getNonDecoratedTokenGenerator();
	public abstract List<TokenFilter> getTokenFilters();
	
}
