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
package org.trpr.platform.integration.impl.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.NewBeanInstanceStrategy;

import org.trpr.platform.integration.spi.json.JSONDataException;
import org.trpr.platform.integration.spi.json.JSONTranscoder;

/**
 * The <code> JSONTranscoderImpl </code> is an implementation of the {@link JSONTranscoder} interface. Uses the json-lib library (http://json-lib.sourceforge.net/)
 * to provide the concrete implementation.
 * 
 * @author Regunath B
 * @version 1.0, 18/09/2012
 */
public class JSONTranscoderImpl implements JSONTranscoder {

	/** The indent for pretty print*/
	private static final int PRETTY_PRINT_INDENT = 4;
	
	/** Map containing class names and static instantiation method names*/
	private static Map<String, String> instantiationMap = new HashMap<String, String>();
	
	/** Static block to initialize with custom instantiation for certain core Java types*/
	static {
		JSONTranscoderImpl.instantiationMap.put("java.util.Calendar", "getInstance");
		JSONTranscoderImpl.instantiationMap.put("java.util.TimeZone", "getDefault");
	}
	
	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.integration.spi.json.JSONTranscoder#marshal(java.lang.Object)
	 */
	public String marshal(Object object) throws JSONDataException {
		return JSONSerializer.toJSON(object).toString(JSONTranscoderImpl.PRETTY_PRINT_INDENT);
	}

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.integration.spi.json.JSONTranscoder#unmarshal(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(String json, Class<T> clazz) throws JSONDataException {
		JsonConfig jsonConfig = new JsonConfig();  
		jsonConfig.setRootClass(clazz);
		jsonConfig.setNewBeanInstanceStrategy(new CustomNewInstanceStrategy());
		return (T)JSONSerializer.toJava(JSONObject.fromObject(json), jsonConfig);
	}
	
	/** Getter/Setter methods*/
	public void setInstantiationMap(Map<String,String> map) {
		JSONTranscoderImpl.instantiationMap = map;
	}	
	/** End Getter/Setter methods */
	
	/**
	 * Custom instantiation strategy for specific types as needed. Uses no args constructor as the default if no cutomization is specified.
	 */
	private class CustomNewInstanceStrategy extends NewBeanInstanceStrategy {
		private Object[] EMPTY_ARGS = new Object[0];
	    private Class[] EMPTY_PARAM_TYPES = new Class[0];
	    
	    public Object newInstance(Class target, JSONObject source) throws InstantiationException, IllegalAccessException, 
	    	SecurityException, NoSuchMethodException, InvocationTargetException {
	    	if( target != null ) {
	    		if (JSONTranscoderImpl.instantiationMap.containsKey(target.getName())) {
	    			return target.getDeclaredMethod(JSONTranscoderImpl.instantiationMap.get(target.getName()), null).invoke(null, null);
	    		} else {
	    			Constructor c = target.getDeclaredConstructor( EMPTY_PARAM_TYPES );
	    			c.setAccessible( true );
	    			return c.newInstance( EMPTY_ARGS );
	    	  }
	       	}
	       return null;
	    }
		
	}

}
