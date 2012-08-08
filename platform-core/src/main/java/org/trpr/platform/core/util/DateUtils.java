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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * Utility methods for date conversions.
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */
public class DateUtils {
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(DateUtils.class);
	
	/**
	 * Date Formats to be used for formatting.
	 */
	public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy hh:mm:ss";	
	public static final String DATE_FORMAT_SHORT = "dd/MM/yyyy";
	public static final String DATE_FORMAT_AM_PM = "dd/MM/yyyy hh:mm:ss a";
	public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATE_FORMAT_24_HOUR = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYYMMDD_HHmmss_DATE_FORMAT = "yyyy:MM:dd HH:mm:ss";
	public static final String YYYYMMDD_DATE_FORMAT = "yyyyMMdd";

	/**
	 * Converts string representation of a date to {@link Date}
	 * @param dtStr date string to be parsed.
	 * @return parsed date
	 * @throws ParseException if parsing using {@value DateUtils#DATE_FORMAT_DEFAULT} fails
	 */
	public static Date convertStringToDate(String dtStr) {
		return parseDate(dtStr, DateUtils.DATE_FORMAT_DEFAULT);
	}
	
	/**
	 * Converts string representation of a date to {@link Date} using the format specified.
	 * @param dtStr date string to be parsed.
	 * @param dateFormat one of the supported Java date formats
	 * @return parsed date
	 * @throws ParseException if parsing using the specified format fails
	 */
	public static Date convertStringToDate(String dtStr, String dateFormat) {
		return parseDate(dtStr,dateFormat);
	}

	/**
	 * Converts the specified Date to its string representation.
	 * @param dateToConvert Date to be converted to string.  
	 * @return String representation of the Date
	 */
	public static String convertDateToString(Date dateToConvert) {
		return formatDate(dateToConvert, DateUtils.DATE_FORMAT_DEFAULT);
	}
	
	/**
	 * Converts the specified Date to its string representation using the formats specified
	 * @param dateToConvert Date to be converted to string
	 * @param dateFormat one of the supported Java date formats
	 * @return String representation of the Date
	 */
	public static String convertDateToString(Date dateToConvert, String dateFormat) {
		return formatDate(dateToConvert, dateFormat);
	}
	
	/**
	 * Returns current date and time.
	 * @return current date and time
	 */
	public static Date getCurrentTime() {
		return Calendar.getInstance().getTime();
	}
	
	/**
	 * Returns a default Date, initialized to 1st Jan 1900
	 * @return Date representing 1st Jan 1900
	 */
	public static Date getDefaultDate(){
		Calendar c1 = Calendar.getInstance(); 
		c1.set(1900, 00 , 01, 0, 0, 0);
		return c1.getTime();
	}
	
	/**
	 * Helper method to parse the specified date string using the specified date format string
	 */
	private static Date parseDate(String dtStr, String dateFormat) {
		Date convertedDate = null;
		// Create an instance of SimpleDateFormat using the default format
		DateFormat df = new SimpleDateFormat(dateFormat);
		// try parsing the date string
		try {
			if(dtStr!=null && !dtStr.trim().isEmpty()){
				convertedDate =  df.parse(dtStr);
			}
		} catch (ParseException e) {
			LOGGER.debug("Date conversion failed",e);
		}
		return convertedDate;		
	}
	
	/**
	 * Helper method to format the specified string into a Date using the specified date format string 
	 */
	private static String formatDate(Date dateToConvert, String dateFormat) {
		// format the date using the SimpleDateFormat
		if(dateToConvert!=null){
			return new SimpleDateFormat(dateFormat).format(dateToConvert);
		}
		return null;		
	}
	
}
