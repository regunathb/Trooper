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
package org.trpr.platform.core.spi.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Class <code>Criteria</code> stores information for a persistence call such as a query, parameters and parameter values.  
 * The query may be ANSI SQL or of a similar type of data query language.   
 *  
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 21/05/2012
 */

public class Criteria {

	/** Type classifiers for the type of query strings*/
	public static final int DEFAULT_QUERY=0;
	public static final int NATIVE_QUERY=1;
	public static final int NAMED_QUERY=2;
	
	/** Constant to indicate no max results size*/
	public static int NO_LIMIT = -1;

	/** The query type classifier*/
	protected int queryType = Criteria.DEFAULT_QUERY;
	
	/** The query placeholder*/
	protected String query;
	
	/** The PersistentEntity that this Criteria works on*/
	protected Class<? extends PersistentEntity> managedClass;

	/** Names and values Map for parameterized queries*/
	protected Map<String, Object> paramsMap = new HashMap<String, Object>();
	
	/** The first result set index. Default is 0. Useful during pagination*/
	protected int firstResult = 0;
	
	/** Max result set size*/
	protected int maxResults = Criteria.NO_LIMIT;
	
	/**
	 * Default const for any implementations that cannot define simple queries
	 * or object types.
	 */
	public Criteria(){}
	
	/** Constructors for this class*/
	public Criteria(Class<? extends PersistentEntity> managedClass, String query){
		this(managedClass, query, Criteria.DEFAULT_QUERY);
	}	
	public Criteria(Class<? extends PersistentEntity> managedClass, String query, int type) {
		this.managedClass = managedClass;
		this.query = query;
		this.queryType = type;
	}
	
	/**
	 * Adds the specified param name and values for use in query execution. Note that for duplicate names, the last added value is retained.
	 * @param param the unique parameter name identifier
	 * @param value the parameter value
	 */
	public void addParameter(String param, Object value) {
		this.paramsMap.put(param, value);
	}
	
	/**
	 * Returns a single line toString() version of this instance
	 * @return
	 */
	public String toConciseString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Entity Class : ");
		sb.append(this.getManagedClass());
		sb.append(",Query : ");
		sb.append(this.getQuery());
		return sb.toString();
	}
	
	/**
	 * Returns the parameter value identified by the specified name
	 * @param param the parameter name
	 * @return null or the value that was set using {@link #addParameter(String, Object)}
	 */
	public Object getParameter(String param) {
		return  this.paramsMap.get(param);
	}
	
	/**
	 * Overriden super class method. Does a verbose print of all information contained in this Criteria
	 * @see java.lang.Object#toString()
	 */
	@SuppressWarnings("unchecked")
	public String toString() {
		//Print all the information of the criteria.
		StringBuffer sb = new StringBuffer();
		sb.append("Entity Class : ");
		sb.append(this.getManagedClass());
		sb.append("\nQuery : ");
		sb.append(this.getQuery());
		sb.append("\nParameters : \n");
		for(String paramKey : this.paramsMap.keySet()) {
			sb.append("Param Name : ");
			sb.append(paramKey);
			sb.append("\nParam Value : ");
			if (this.paramsMap.get(paramKey) != null && List.class.isAssignableFrom(this.paramsMap.get(paramKey).getClass())) {
				StringUtils.join((List)this.paramsMap.get(paramKey), ";");
			} else {
				sb.append(this.paramsMap.get(paramKey));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convenience method to return the parameter names as an array of type String.
	 * Note that the order of elements matches values returned in {@link #getParamValuesArray()}
	 * @return array of String parameter names
	 */
	public String[] getParamNamesArray() {
		return this.getParamsMap().keySet().toArray(new String[0]);
	}
	
	/**
	 * Convenience method to return the parameter values as an array of type Object
	 * Note that the order of elements matches values returned in {@link #getParamNamesArray()}
	 * @return array of Object parameter values
	 */
	public Object[] getParamValuesArray() {
		Object[] values = new Object[this.getParamsMap().size()];
		// populate the values by going over the key set so that order is preserved
		int index = 0;
		for (String key : this.getParamsMap().keySet()) {
			values[index] =  this.getParamsMap().get(key);
			index++;
		}
		return values;
	}
	
	/** === Start getter/setter methods == */
	public int getQueryType() {
		return this.queryType;
	}
	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}
	public String getQuery() {
		return this.query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public Class<? extends PersistentEntity> getManagedClass() {
		return this.managedClass;
	}
	public void setManagedClass(Class<? extends PersistentEntity> managedClass) {
		this.managedClass = managedClass;
	}
	public int getFirstResult() {
		return this.firstResult;
	}
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}
	public int getMaxResults() {
		return this.maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}	
	public Map<String, Object> getParamsMap() {
		return this.paramsMap;
	}
	/** === End getter/setter methods == */
}
