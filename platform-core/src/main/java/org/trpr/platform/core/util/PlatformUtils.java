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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * The <code>PlatformUtils</code> class defines common utility methods used across modules.
 * @author Kishor Padhan
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */
public class PlatformUtils {
	
	/** 
	 * Converts an object to an array of bytes . 
	 * @param object the object to convert.
	 * @return the associated byte array.
	 * @throws IOException 
	 */
	public static byte[] toBytes(Object object) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		return baos.toByteArray();
	}

	/**
	 * Converts an array of bytes back to its constituent object. The input array is assumed to
	 * have been created from the original object. 
	 * @param bytes the byte array to convert.
	 * @return the associated object.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException{
		Object object = null;
		object = new java.io.ObjectInputStream(new
				java.io.ByteArrayInputStream(bytes)).readObject();
		return object;
	}
	
	/**
	 * Returns a Java primitive int from the specified Object or the default value in case of error
	 * @param obj Object to get the int from
	 * @param defaultValue default int value
	 * @return Java primitive int value 
	 */
	public static int getInt(Object obj, int defaultValue) {
		int v = defaultValue;
		try {
			if (obj instanceof Integer) {
				v = ((Integer)obj).intValue();
			}
			else {
				v = Integer.parseInt(obj.toString());
			}
		}
		catch (Exception ex) {
			v = defaultValue;
		}
		return v;
	}
	
	/**
	 * Returns a Java primitive boolean from the specified Object or the default value in case of error
	 * @param obj Object to get the boolean from
	 * @param defaultValue default boolean value
	 * @return Java primitive boolean value 
	 */
	public static boolean getBoolean(Object obj, boolean defaultValue) {
		boolean v = defaultValue;
		try {
			if (obj instanceof Boolean) {
				v = ((Boolean)obj).booleanValue();
			}
			else if (obj instanceof String) {
				v = ((String)obj).trim().equalsIgnoreCase("yes") || 
					((String)obj).trim().equalsIgnoreCase("y") ||
					((String)obj).trim().equalsIgnoreCase("true") ? true : false;
			}
			else {
				v = Boolean.parseBoolean(obj.toString().trim());
			}
		}
		catch (Exception ex) {
			v = defaultValue;
		}
		return v;
	}
	
	/**
	 * Checks if the specified object is "empty" i.e. null or of length zero in case of String
	 * @param obj the Object to check for empty
	 * @return boolean indicating empty or not
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		else if (obj instanceof String) {
			return (obj.toString().trim().length() == 0) ? true : false;
		}
		else {
			return false;
		}
	}
	
}
