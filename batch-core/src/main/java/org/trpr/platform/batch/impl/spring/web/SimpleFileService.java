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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.batch.spi.spring.admin.FileService;
/**
 * Implementation of @link {FileService}
 * @author devashishshankar
 * @version 1.0 Jan 21, 2012
 *
 */
public class SimpleFileService implements FileService {
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.FileService#getFileContents(String)
	 */
	@Override
	public String getFileContents(String filename) {
		try {
			File f = new File(filename);
			FileInputStream fin = new FileInputStream (filename);
			byte[] buffer = new byte[(int) f.length()];
			new DataInputStream(fin).readFully(buffer);
			fin.close();
			return new String(buffer,"UTF-8").trim();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.FileService#upload(MultipartFile, String)
	 */
	@Override
	public boolean upload(MultipartFile file, String dest_path) {
			try {
				File dest_file = new File(dest_path);
				//Creating directory structure
				dest_file.getParentFile().mkdirs();
				file.transferTo(dest_file);
				return true;				
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;	
	}
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.FileService#delete(String)
	 */
	@Override
	public boolean delete(String destPath) {
		File dest_file = new File(destPath);
		if(dest_file.exists()) {
			return dest_file.delete();
		}
		return false;
	}
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.FileService#deleteFolder(File)
	 */	
	@Override
	public void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
}
