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

package org.trpr.platform.core.impl.token.pwd;

import java.util.Map;
import java.util.Random;

import org.trpr.platform.core.spi.token.TokenGenerator;
import org.trpr.platform.core.util.PlatformUtils;

/**
 * The <code>PasswordGenerator</code> class is a {@link TokenGenerator} that generates a fixed length String from the ASCII subset using 
 * a random selection of characters. Note that strings generated using this generator may be used for requirements like creating first-time passwords. 
 * Duplicates are possible with this generator as no uniqueness check is performed across prior strings generated by this class
 * 
 * @author Regunath B
 * @version 1.0, 31/05/2012
 */
public class PasswordGenerator implements TokenGenerator {

	/** The max value key in the spec*/
	public static String MAX_VALUE = "MaxValue";
	
	/** The default password length*/
	private static final int DEFAULT_LENGTH = 8;

	/** Random object*/
	private static Random rand = new Random();
	
	/** Fixed set of characters used in generating the password*/
	private static char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 
		'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 
		'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
		'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '#', '$', '-', '+'};

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.core.spi.token.TokenGenerator#generate(java.util.Map)
	 */
	public String generate(Map<String, Object> spec) {
		int len = DEFAULT_LENGTH;
		if (spec != null) {
			len = PlatformUtils.getInt(spec.get(MAX_VALUE), len);
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < len; i++) {
			int n = rand.nextInt(chars.length);
			buf.append(chars[n]);
		}
		return buf.toString();
	}
	
}
