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

import java.util.LinkedList;
import java.util.List;

import org.trpr.platform.core.spi.token.TokenGenerator;
import org.trpr.platform.core.spi.token.filter.TokenFilter;

/**
 * The <code>FilteringTokenGenerator</code> class is a {@link AbstractDecoratedTokenGenerator} that applies various filters as applicable for 
 * generating a token. 
 *
 * @author Regunath B
 * @version 1.0, 30/05/2012
 */
public class FilteringTokenGenerator extends AbstractDecoratedTokenGenerator {

	/** The TokenGenerator to get the tokens from*/
	private TokenGenerator nonDecoratedTokenGenerator;
	
	/** The list of filters to apply*/
	private List<TokenFilter> tokenFilters = new LinkedList<TokenFilter>();

	/**
	 * Constructor for this class
	 * @param nonDecoratedTokenGenerator the TokenGenerator used for generating candidate tokens
	 */
	public FilteringTokenGenerator (TokenGenerator nonDecoratedTokenGenerator) {
		this.nonDecoratedTokenGenerator = nonDecoratedTokenGenerator;
	}

	/** == Start Java bean style setters and getters  === */
	
	/**
	 * Abstract method implementation
	 * @see AbstractDecoratedTokenGenerator#getNonDecoratedTokenGenerator()
	 */
	public TokenGenerator getNonDecoratedTokenGenerator() {
		return nonDecoratedTokenGenerator;
	}
	/**
	 * Abstract method implementation
	 * @see AbstractDecoratedTokenGenerator#getTokenFilters()
	 */
	public List<TokenFilter> getTokenFilters() {
		return tokenFilters;
	}
	public void setTokenFilters(List<TokenFilter> tokenFilters) {
		this.tokenFilters = tokenFilters;
	}
	/** == End Java bean style setters and getters  === */
	
}
