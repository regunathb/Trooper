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
package org.trpr.platform.batch.impl.spring.web;

/**
 * <code>Host</code> is a data container holding the Trooper Host details, i.e. 
 * host name, host's IP address and port.
 * @author devashishshankar
 * @version 1.0 31 Jan, 2013
 */
public class Host {

	private String hostName;
	private String IP;
	private int port;

	/**
	 * Default constructor
	 */
	public Host(String hostName, String IP, int port) {
		this.setHostName(hostName);
		this.setIP(IP);
		this.setPort(port);
	}
	
	/**
	 * If IP address is not passed, default IP address is loopback address
	 */
	public Host(String hostName, int port) {
		this(hostName, "127.0.0.1", port);
	}

	/** Getter/Setter methods */
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getIP() {
		return IP;
	}
	public void setIP(String iP) {
		IP = iP;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	/** End getter/Setter methods */
	
	/**
	 * Returns the Address in the form "IP:port",
	 * which can be used for HTTP request
	 * @return @link{String} address
	 */
	public String getAddress() {
		return this.IP+":"+this.port;
	}

	/**
	 * Overriden method from object class
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Host) {
			Host a = (Host) obj;
			if(a.hostName.equals(this.hostName) && a.port == this.port) {
				return true;
			}
		}
		return false;
	}
}
