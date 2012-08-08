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
package org.trpr.platform.core.impl.token.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.token.filter.TokenFilter;

/**
 * The <code>NumberFilter</code> class is a {@link TokenFilter} that checks if the specified token is numeric
 *
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */
public class NumberFilter implements TokenFilter {

	/** Number-only pattern used by this filter */
	private static final String ALL_NUMERIC = "\\d+";
	private static final Pattern NUMERIC_PATTERN = Pattern.compile(ALL_NUMERIC);

	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(NumberFilter.class);
	
	/** Count of rejects*/
	protected long rejectCount;

	/**
	 * Interface method implementation. Accepts only if the token is numeric 
	 * @see TokenFilter#accept(java.lang.String)
	 */
	public boolean accept(String token) {
		Matcher matcher = NUMERIC_PATTERN.matcher(token);
		if (!matcher.matches()) {
			this.rejectCount++;
			return false;
		}
		return true;
	}
	
	/**
	 * Interface method implementation.
	 * @see TokenFilter#applyFilter(String)
	 */
	public String applyFilter(String token) {
		Matcher matcher = NUMERIC_PATTERN.matcher(token);
		if (!matcher.matches()) {
			LOGGER.info("Filtering out non-numeric token : " + token);
			return null;
		}
		return token;		
	}

	/**
	 * Interface method implementation
	 * @see TokenFilter#getRejectedNumberCount()
	 */
	public long getRejectedNumberCount() {
		return this.rejectCount;
	}

}
