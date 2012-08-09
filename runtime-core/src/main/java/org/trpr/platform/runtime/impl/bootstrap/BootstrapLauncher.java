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
package org.trpr.platform.runtime.impl.bootstrap;

import org.trpr.platform.runtime.impl.bootstrap.spring.Bootstrap;

/**
 * The <code>BootstrapLauncher</code> class is a launcher for {@link Bootstrap}
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public class BootstrapLauncher {

	/**
	 * JVM entry point
	 * @param args JVM args where first param should be path to bootstrap config file
	 */
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.err.println("Bootstrap config file not specified. Exiting!");
			System.exit(-1);
		}
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.init(args[0]);
	}
}
