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

package org.trpr.platform.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.trpr.platform.core.PlatformConstants;

/**
 * The <code> FileUtils </code> class provides methods to read, write, copy and delete files.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012 
 *
 */
public class FileUtils {
	
	/**
	 * Deletes the file or directory denoted by this absolute file path.
	 * If path name denotes a directory, also deletes child directories and files. 
	 *
	 * @param filePathName absolute path of file or directory. e.g. E:/temp in windows, /root/temp/ in Linux
	 */
	public void deleteFile(String filePathName) {
		deleteDirectory(new File(filePathName));
	}
	
	/**
	 * Deletes the array of files. If any of files denotes a directory, also deletes its child directories
	 * and files also.
	 * 
	 * @param files file array
	 */
	public void deleteFile(File[] files) {
		for (int i=0; i< files.length; i++) {
			deleteDirectory(files[i]);
		}
	}
	
	/**
	 * Deletes the file or direcotory specified in parameter.
	 * If denoted file is a directory, also deletes the child directories and files.
	 * 
	 * @param directory File deleted directory
	 * @return boolean if true, deleting file is success
	 *                 if false, deleting file is failure  
	 */
	public boolean deleteDirectory(File directory) {
		return deleteDirectory(directory, null);
	}
	
	/**
	 * Deletes the file or direcotory specified in parameter
	 * except file which name is matched with excludeFileName.
	 * If denoted file is a directory, also deletes the child directories and files
	 * except some files which names are matched with excludeFileName.
	 * 
	 * @param directory root directory
	 * @param excludeFileName name of exclude file e.g. sample
	 * @return boolean if true, deleting file is success
	 *                 if false, deleting file is failure  
	 */
	public boolean deleteDirectory(File directory, String excludeFileName) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDirectory(new File(directory, children[i]), excludeFileName);
                if (!success) {
                    return false;
                }
            }
        }
        if(excludeFileName != null && directory.getName().equals(excludeFileName)) {
        	return true;
        } else {
//        	 The directory is now empty so delete it
            return directory.delete();
        }
    }

	/**
	 * Writes the data to file with file name and file path.
	 * If file is not there, create new file and then writes the data.
	 * 
	 * @param dataToWrite String data
	 * @param fileName String file name with extension e.g. sample.txt
	 * @param filePath String file path e.g. /root/temp in Linux user, E:/temp in Window user
	 */
	public void writeToFile(String dataToWrite,
			String fileName, String filePath) throws IOException{
		String absolutePathName = filePath + System.getProperty(PlatformConstants.FILE_SEPARATOR_VALUE) + fileName;
		File newFile  = new File(absolutePathName);
		writeToFile(dataToWrite, newFile);
	}

	/**
	 * Writes the data to file. If file is not there, create new file and then write the data.
	 * 
	 * @param dataToWrite String data
	 * @param file File distination file
	 * @throws IOException if file writting is failed  
	 */
	public void writeToFile(String dataToWrite, File file) throws IOException {
		Writer writer = null;
		file.createNewFile();
		if(!file.isFile()) {
			return;
		}
		try {
			writer = new BufferedWriter(new FileWriter(file));
		    writer.write(dataToWrite);
		}  finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Writes the data to file. If file is not there, create new file and then write the data.
	 * 
	 * @param dataToWrite String data
	 * @param file File distination file
	 * @param charSet encode value like UTF-8
	 * @throws IOException if file writting is failed  
	 */
	public void writeToFile(String dataToWrite, File file, String charset) throws IOException {
		Writer writer = null; 
		try {
		        writer = new BufferedWriter(new OutputStreamWriter(
		            new FileOutputStream(file), charset));
		        writer.write(dataToWrite);
		        writer.flush();
		 } finally {
			 if(writer != null) {
				 writer.close();
			 }
		 }
	}
	
	/**
	 * This method reads data from File Object
	 *  
	 * @param file source file
	 * @return String file data
	 * @throws IOException if file reading is failed
	 */
	public String readFromFile(File file) throws IOException {
		  StringBuffer fileContents = new StringBuffer();
	        BufferedReader input = null;
	        try {
	            input = new BufferedReader(new FileReader(file));
	            String line = null;
	            while((line = input.readLine()) != null) {
	                fileContents.append(line);
	                fileContents.append(System.getProperty(PlatformConstants.LINE_SEPARATOR_VALUE));
	            }
	        } finally {
	                if(null != input) {
	                    input.close();
	                }
	       }
	        return fileContents.toString();
	}

	/**
	 * This method reades the data from given file location.
	 * 
	 * @param filePathName String file path name
	 *                     e.g. /root/temp/sample.xml in Linux user, E:/temp/sample.xml in Window user
	 * @return String file data
	 * @throws IOException if file reading is failed
	 */
	public String readFromFile(String filePathName) throws IOException{
		return readFromFile(new File(filePathName));
	}
    
	/**
	 * This method reads the data with given file path and charset
	 * 
	 * @param filePath file path
	 * @param charset charecter set like UTF-8
	 * @return String
	 * @throws IOException
	 */
	public String readFromFile(String filePath, String charset) throws IOException {
		return readFromFile(new FileInputStream(filePath), charset);
	}
	
	/**
	 * This method reads the data with given input stream and charset
	 * 
	 * @param inputStream any input stream
	 * @param charset charecter set like UTF-8
	 * @return String
	 * @throws IOException
	 */
	public String readFromFile(InputStream inputStream, String charset) throws IOException {
		StringBuffer stringBuffer = new StringBuffer("");
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			inputStreamReader = new InputStreamReader(inputStream, charset);
			String line;
			bufferedReader = new BufferedReader(inputStreamReader);
			while((line= bufferedReader.readLine()) != null) {
			  stringBuffer.append(line);
			  stringBuffer.append("\n");
			}
		} finally {
			if(inputStreamReader != null) {
				inputStreamReader.close();
			}
			if(bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuffer.toString();
	}
	
	/**
	 * This method copy content from source file to distination file.
	 * 
	 * @param source source file
	 * @param distination distination file
	 * @throws IOException if copy file operation is failed.
	 */
	public void copyFileContent(File source, File distination) throws IOException {
		copyFileContent(source, distination, null);
	}
	
	/**
	 * copy content from souce file to distination file with replace using replaceMap.
	 * Source file is scanned for ${key} and if one is found, 
	 * it is replaced with corresponding value from the replaceMap hashtable.
	 * 
	 * @param source source file
	 * @param distination distination file
	 * @param replaceMap map of replacement values
	 * @throws IOException if either reading or writting operation is failed. 
	 */
	public void copyFileContent(File source, File distination, Map<String,Object> replaceMap) throws IOException {
		if(source.isFile()) {
			String fileContent = readFromFile(source);
			copyFileContent(fileContent, distination, replaceMap);
		}
	}
	

	/**
	 * copy source content to distination file with replace using replaceMap.
	 * Source file is scanned for ${key} and if one is found, 
	 * it is replaced with corresponding value from the replaceMap hashtable.
	 * 
	 * @param source source file
	 * @param distination distination file
	 * @param replaceMap map of replacement values
	 * @throws IOException if either reading or writting operation is failed. 
	 */
	public void copyFileContent(String source, File distination, Map<String,Object> replaceMap) throws IOException {
		if(replaceMap != null) {
			source = replaceVars(source, replaceMap);
		}
		writeToFile(source, distination);
	}
	
	/**
	 * Copy file content from source file to distination file with replace using regex, replaceMap.
	 * regex is a regular expression of source file data. Key of replace map is group number of regex
	 * in Integer object. Value of replace map is replacing content. Replacing file content with
	 * replacing content in the map if find group number of regex in the map.
	 * 
	 * @param source source file
	 * @param distination distination file
	 * @param regex regular expression of source file.
	 * @param replaceMap Map key is Integer Object which have group number of regex.
	 *                       value is replacing content
	 *  
	 * @throws IllegalStateException if invalid regular expression for source file 
	 * @throws IOException if either reading or writting operation is failed. 
	 */
	public void copyFileContent(File source, File distination, String regex, Map<String,Object> replaceMap) throws IOException {
		if(source.isFile()) {
			String fileContent = readFromFile(source);
			copyFileContent(fileContent, distination, regex, replaceMap);
		}
	}
	
	/**
	 * Copy source content distination file with replace using regex, replaceMap.
	 * regex is a regular expression of source file data. Key of replace map is group number of regex
	 * in Integer object. Value of replace map is replacing content. Replacing file content with
	 * replacing content in the map if find group number of regex in the map.
	 * 
	 * @param source source file
	 * @param distination distination file
	 * @param regex regular expression of source file.
	 * @param replaceMap Map key is Integer Object which have group number of regex.
	 *                       value is replacing content
	 *  
	 * @throws IllegalStateException if invalid regular expression for source file 
	 * @throws IOException if either reading or writting operation is failed. 
	 */
	public void copyFileContent(String source, File distination, String regex, Map<String,Object> replaceMap) throws IOException {
		if(replaceMap != null) {
			source = replaceVars(source, regex, replaceMap);
		}
		writeToFile(source, distination);
	}
	
	/** 
	 * Performs variable substitution for a string. String is scanned for ${variable_name} and if one is found, 
	 * it is replaced with corresponding value from the vars hashtable. 
	 *  
	 * @param origString unmodified string 
	 * @param vars Map of replacement values 
	 * @return modified string 
	 * @exception Exception 
	 */
	private String replaceVars(String origString, Map<String,Object> vars) {
		StringBuffer finalString = new StringBuffer();
		int index = 0;
		int i = 0;
		String key = null;
		String value = null;
		while ((index = origString.indexOf("${", i)) > -1) {
			key = origString.substring(index + 2, origString.indexOf("}",
					index + 3));
			value = (String) vars.get(key);
			finalString.append(origString.substring(i, index));
			if (value != null) {
				finalString.append(value);
			} else {
				finalString.append("${" + key + "}");
			}
			i = index + 3 + key.length();
		}
		finalString.append(origString.substring(i));
		return finalString.toString();
	}
	
	/**
	 * regex is a regular expression of original string. Key of replace map is group number of regex
	 * in Integer object. Value of replace map is replacing content. Replacing oringinal content with
	 * replacing content in the map if find group number of regex in the map.
	 * 
	 * @param origString orginal string
	 * @param regex regular expression of orginal string
	 * @param vars Map key is Integer Object which have group number of regex.
	 *                       value is replacing content
	 *                       
	 * @return String replacing String with regex, vars
	 * @throws IllegalStateException if invalid regular expression for orginal string 
	 */
	private String replaceVars(String origString, String regex, Map<String,Object> vars) {
		StringBuffer finalString = new StringBuffer();
		
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(origString);
		matcher.find();
		
	    int totalGroupCount = matcher.groupCount();
	    for(int i = 1; i <= totalGroupCount; i++) {
	    	Integer indexObject = new Integer(i);
	    	String value = (String) vars.get(indexObject);
	    	if(value == null) {
	    		value = matcher.group(i);
	    	} 
	    	finalString.append(value);
	    }
		return finalString.toString();
	}

}
