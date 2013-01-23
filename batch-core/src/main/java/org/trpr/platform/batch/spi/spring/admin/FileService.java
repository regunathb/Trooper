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
package org.trpr.platform.batch.spi.spring.admin;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

/**
 * A service which provides File related functions such as upload, delete
 * @author devashishshankar
 * @version 1.0 22 Jan, 2012
 *
 */
public interface FileService {

	/**
	 * Uploads a multipart file to the given path. Creates the path if it doesn't exist
	 */
	boolean upload(MultipartFile file, String dest_path);
	
	/**
	 * Gets the contents of a file (on server) in a single String
	 */
	public String getFileContents(String filename);
	
	/**
	 * Deletes a file on server. Returns false if fails to delete.
	 */
	boolean delete(String destPath);
	
	/**
	 * Recursively deletes the folder and it's contents
	 */
	void deleteFolder(File folder);

}
