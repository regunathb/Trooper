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
package org.trpr.dataaccess.hbase.mappings.config;

import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.trpr.dataaccess.hbase.model.config.ColumnDefinition;
import org.trpr.dataaccess.hbase.model.config.CompositeRowKey;
import org.trpr.dataaccess.hbase.model.config.HbaseMapping;
import org.trpr.dataaccess.hbase.model.config.RowKeyDefinition;
import org.trpr.dataaccess.hbase.model.config.RowKeyMember;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.integration.impl.xml.XMLTranscoderImpl;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.config.ConfigurationException;

/**
 * This class contains all the mappings configurations for objects that can be
 * persisted in HBase. All these mappings objects are stored in Map using the 
 * persistent entity class name as the key. These mapping objects are used in
 * all the further HBase operations.
 * 
 * @author Shashikant Soni
 * 
 */
public class HBaseMappingContainer {

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(HBaseMappingContainer.class);

	/**
	 * Map holds the hbase mappings information
	 */
	private Map<String, HbaseMapping> configs;
	
	/**
	 * No argument constructor
	 */
	public HBaseMappingContainer() {
		configs = new HashMap<String, HbaseMapping>();
	}

	/**
	 * Initialize the HBase Container using the hbase mapping files
	 * 
	 * @param mappingList
	 *            list of hbase mapping file names
	 */
	public void init(List<String> mappingFiles) throws ConfigurationException {
		for (String mappingFile : mappingFiles) {
			String xml;
			try {
				xml = readFileToString(mappingFile);
				HbaseMapping mapping = new XMLTranscoderImpl().unmarshal(xml, HbaseMapping.class);
				validate(mappingFile, mapping);
				this.configs.put(mapping.getHbaseClass().getName(), mapping);
			} catch (Exception e) {
				LOGGER.error("Error initializing HBase configurations", e);
				throw new ConfigurationException("Error initializing HBase configurations", e);
			}
		}
	}

	/*
	 * loads a single configuration file as a single XML string
	 */
	private String readFileToString(String mappingFile) throws ConfigurationException {	
		try {
			InputStream stream = new FileInputStream(FileLocator.findUniqueFile(mappingFile));
			List<String> lines=IOUtils.readLines(stream);				
			StringBuffer strBuf = new StringBuffer();				
			for (String s : lines) {
				strBuf.append(s);
				strBuf.append("\n");
			}		
			return strBuf.toString();
		} catch (Exception e) {
			throw new ConfigurationException("Error while reading Hbase mapping file : " + mappingFile, e);
		}
	}
	
	/*
	 * validates the structure of a given mapping file.
	 */
	private void validate(String mappingFileName, HbaseMapping mapping) throws ConfigurationException {
		try {
			
			Object o = Class.forName(mapping.getHbaseClass().getName()).newInstance();
			
			RowKeyDefinition rowKeyDefinition = mapping.getHbaseClass().getRowkeyDefinition();
			if (rowKeyDefinition == null) {
				throw new ConfigurationException("No row key definition found in " + mappingFileName);
			}

			try {
				if (rowKeyDefinition.getCompositeRowKey() != null) {
					CompositeRowKey compositeRowKey = rowKeyDefinition.getCompositeRowKey();
					if (compositeRowKey.getRowKeyMember().size() == 0) {
						throw new ConfigurationException("At least one row key member must be specified when using Composite Row key in " + mappingFileName);
					}
					for (RowKeyMember member : compositeRowKey.getRowKeyMember()) {
						PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(o, member.getValueAttribute());
						
						if (propertyDescriptor == null) {
							throw new ConfigurationException("Attribute " + member.getValueAttribute() + " not found in class: " + mapping.getHbaseClass().getName() + " in file " + mappingFileName);
						}

						if (!propertyDescriptor.getPropertyType().getName().equals(member.getValueType())
								&& !"byte[]".equals(rowKeyDefinition.getValueType())) {
							throw new ConfigurationException("Wrong value specified for " + member.getValueAttribute() + " in " + mappingFileName);
						}
					}
				} else {
					PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(o, rowKeyDefinition.getValueAttribute());
					
					if (propertyDescriptor == null) {
						throw new ConfigurationException("Attribute " + rowKeyDefinition.getValueAttribute() + " not found in class: " + mapping.getHbaseClass().getName() + " in file " + mappingFileName);
					}

					if (!propertyDescriptor.getPropertyType().getName().equals(rowKeyDefinition.getValueType())
							&& !"byte[]".equals(rowKeyDefinition.getValueType())) {
						throw new ConfigurationException("Wrong value specified for " + rowKeyDefinition.getValueAttribute() + " in " + mappingFileName);
					}
				}
				
				
				
				for (ColumnDefinition columnDefinition : mapping.getHbaseClass().getColumnDefinition()) {
					PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(o, columnDefinition.getValueAttribute());

					if (propertyDescriptor == null) {
						throw new ConfigurationException("Attribute " + columnDefinition.getValueAttribute() + " not found in class: " + mapping.getHbaseClass().getName() + " in file " + mappingFileName);
					}
					
					if (!propertyDescriptor.getPropertyType().getName().equals(columnDefinition.getValueType())
							&& !"byte[]".equals(columnDefinition.getValueType())) {
						throw new ConfigurationException("Wrong value type specified for " + columnDefinition.getValueAttribute() + " in " + mappingFileName);
					}	
					
					if (StringUtils.isBlank(columnDefinition.getColumnQualifier()) && StringUtils.isBlank(columnDefinition.getColumnQualifierAttribute())) {
						throw new ConfigurationException("Either \"columnFamily\" or \"columnQualifierAttribute\" must be specified for " + columnDefinition.getValueAttribute() + " in " + mappingFileName);
					}
				}
			} catch (InvocationTargetException e) {
				throw new ConfigurationException("Attribute not accessible " + rowKeyDefinition.getValueAttribute() + " in " + mappingFileName, e);
			} catch (NoSuchMethodException e) {
				throw new ConfigurationException("Invalid attribute " + rowKeyDefinition.getValueAttribute() + " in " + mappingFileName, e);
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("Class not found : " + mapping.getHbaseClass().getName(), e);
		} catch (InstantiationException e) {
			throw new ConfigurationException("Class not instantiable : " + mapping.getHbaseClass().getName(), e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("Class constructor not accessible : " + mapping.getHbaseClass().getName(), e);
		}
	}

	/**
	 * Getting the configuration of a HBase table
	 * 
	 * @param className
	 *            used as the key to fetch the configuration information
	 * @return HbaseMapping contains the configuration info of a HBase table
	 */
	public HbaseMapping getMappingForClass(String className) {
		return this.configs.get(className);
	}
	
	/**
	 * Returns an array of all configured HbaseMapping instances that were loaded
	 * @return array of HbaseMapping instances
	 */
	public HbaseMapping[] getMappingForAllClasses() {
		return this.configs.values().toArray(new HbaseMapping[0]);
	}
	
}
