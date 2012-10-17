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
package org.trpr.dataaccess.hbase.persistence;



import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.trpr.dataaccess.hbase.mappings.config.HBaseMappingContainer;
import org.trpr.dataaccess.hbase.model.config.ColumnDefinition;
import org.trpr.dataaccess.hbase.model.config.HbaseClass;
import org.trpr.dataaccess.hbase.model.config.HbaseMapping;
import org.trpr.dataaccess.hbase.model.config.MapDefinition;
import org.trpr.dataaccess.hbase.model.config.RowKeyDefinition;
import org.trpr.dataaccess.hbase.model.config.RowKeyMember;
import org.trpr.dataaccess.hbase.serializer.DateSerializer;
import org.trpr.dataaccess.hbase.serializer.IntegerSerializer;
import org.trpr.dataaccess.hbase.serializer.LongSerializer;
import org.trpr.dataaccess.hbase.serializer.StringSerializer;
import org.trpr.dataaccess.hbase.util.ByteArrayProcessor;
import org.trpr.platform.core.impl.logging.NullMetricsLogger;
import org.trpr.platform.core.impl.persistence.AbstractPersistenceHandler;
import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.core.spi.persistence.Serializer;
import org.trpr.platform.runtime.spi.config.ConfigurationException;

/**
 * This implementation of <code>PersistenceHandler</code> that can be used for
 * persisting and retrieving entities of type <code>PersistentEntity</code> from HBase.
 * 
 * For example on how to use HBase persistence API, refer to test sample, HBasePersistenceSample.java
 * and related files.
 * 
 * @author Srikanth Srinivas
 * 
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Performance-Metrics,name=HBaseMetrics-", description = "HBase Performance Metrics Logger")
public class HBaseHandler extends AbstractPersistenceHandler {

	private static final String PROCESSOR_BEGIN_INDICATOR = "__@@__";
	private static final String PROCESSOR_END_INDICATOR = "__@@__";
	
	private static final Log logger = LogFactory.getLog(HBaseHandler.class);
	private static final String FAMILY_QUALIFIER_SEPARATOR = ":";
	
	private Configuration hbaseConfiguration;
	private HBaseMappingContainer hbaseMappingContainer;
	private HTablePool hbaseTablePool;
	
	private Boolean useWAL = true;
	private Boolean useAutoFlush = true;
	private int HTABLE_POOL_SIZE=10;

	private Map<String, Serializer> classNameToSerializerMap = new HashMap<String, Serializer>();
	
	private Map<String, ByteArrayProcessor> processorMap = new HashMap<String, ByteArrayProcessor>();
	
	/**
	 * The PerformanceMetricsLogger instance to use for capturing metrics of code block execution
	 */
	protected PerformanceMetricsLogger performanceMetricsLogger = new NullMetricsLogger();
	
	
	public HBaseHandler() {
		//Default serializers. It can be overridden by setting new values in Spring bean definition
		classNameToSerializerMap.put("java.lang.String", new StringSerializer());
		classNameToSerializerMap.put("java.lang.Long", new LongSerializer());
		classNameToSerializerMap.put("java.lang.Integer", new IntegerSerializer());
		classNameToSerializerMap.put("java.util.Date", new DateSerializer());
	}

	public Configuration getHbaseConfiguration() {
		return hbaseConfiguration;
	}
	
	public HbaseMapping getMappingForClass(String className) {
		return hbaseMappingContainer.getMappingForClass(className);
	}
	
	public void setHbaseConfigProps(Properties props) {
		hbaseConfiguration = HBaseConfiguration.create();
		for(Object key : props.keySet()) {
			hbaseConfiguration.set(key.toString(), props.getProperty(key.toString()));
		}
	}

	public void setHbaseMappings(List<String> mappingFileNameList) throws ConfigurationException {
		hbaseMappingContainer = new HBaseMappingContainer();
		hbaseMappingContainer.init(mappingFileNameList);
	}
	
	public void setClassNameToSerializerMap(Map<String, Serializer> classNameToSerializerMap) {
		//Do a putAll to avoid loosing the default serializer mappings
		this.classNameToSerializerMap.putAll(classNameToSerializerMap);
	}
	
	public void setHbaseTablePool(HTablePool hbaseTablePool) {
		this.hbaseTablePool = hbaseTablePool;
	}
	

	/**
	 * Inserts a row in the HBase table.  In case the row already exists, it results in creation of a new version for all the columns.
	 * @param hbaseConfig HBase configuration describing connection details  
	 * @param metadata HBase mapping defining mapping from PersistentEntity to various columsn of HBase table
	 * @param entity Entity to persist.
	 * @throws PersistenceException In cases when HBase is not reachable or table does not exist or any other errors.
	 */
	private void addEntity(HBaseConfiguration hbaseConfig, HbaseMapping metadata, PersistentEntity entity) throws PersistenceException {

		HTable table = null;
		HbaseClass classDefinition = metadata.getHbaseClass();

		try {
			if (hbaseTablePool==null){				
				hbaseTablePool = new HTablePool(hbaseConfig,HTABLE_POOL_SIZE);				
			}
			table = (HTable)hbaseTablePool.getTable(classDefinition.getTable());
			table.setAutoFlush(useAutoFlush);
			//table.setWriteBufferSize(1024*1024*12);

			Put put = new Put(constructRowKey(entity, classDefinition.getRowkeyDefinition()));
			put.setWriteToWAL(useWAL);

			for (ColumnDefinition column : classDefinition.getColumnDefinition()) {
				put.add(getColumnFamilyInBytes(column), 
						getColumnQualifierInBytes(entity, column), 
						encode(classDefinition, column.getProcessors(), getColumnValueInBytes(entity, column)));
			}

			for (MapDefinition mapDef : classDefinition.getMapDefinition()) {
				Map map = getColumnValueAsMap(classDefinition, entity, mapDef);
				
				for (Object key : map.keySet()) {
					put.add(getColumnFamilyInBytes(mapDef), 
						Bytes.add(getColumnQualifierInBytes(entity, mapDef), convertToBytes(key)), 
						encode(classDefinition, mapDef.getProcessors(), convertToBytes(map.get(key))));
				}
			}

			// Insert new row into HBase table
			table.put(put);
		}
		catch (Exception e) {
			throw new PersistenceException("Exception in putData of " + classDefinition.getTable(), e);
		} finally {
				if (table != null) {
					hbaseTablePool.putTable(table);
				}
		}
	}

	@SuppressWarnings("rawtypes")
	private Map getColumnValueAsMap(HbaseClass classDefinition, PersistentEntity entity, MapDefinition mapDefinition) throws ConfigurationException {
		Object o = getAttribute(entity, mapDefinition.getMapAttribute());
		if (o instanceof java.util.Map) {
			return (Map)o;
		} else {
			throw new ConfigurationException(classDefinition.getName() + ", attribute " + mapDefinition.getMapAttribute() + " is not of type " + mapDefinition.getValueType());
		}
	}

	/**
	 * Deletes a row in HBase table
	 * @param hbaseConfig HBase configuration
	 * @param entity Entity to delete
	 * @param metadata Table mapping definition
	 * @throws PersistenceException in case anything goes wrong
	 */
	private void deleteEntity(HBaseConfiguration hbaseConfig, PersistentEntity entity, HbaseMapping metadata) throws PersistenceException {

		HTable table = null;
		try {
			if (hbaseTablePool==null){				
				hbaseTablePool = new HTablePool(hbaseConfig,HTABLE_POOL_SIZE);				
			}
			table = (HTable)hbaseTablePool.getTable(metadata.getHbaseClass().getTable());
			
			Delete delete = new Delete(constructRowKey(entity, metadata.getHbaseClass().getRowkeyDefinition()));
			table.delete(delete);
		} catch (Exception e) {
			throw new PersistenceException("Failed to delete entry for table " + metadata.getHbaseClass().getTable(), e);
		} finally {
			if (table != null) {
				hbaseTablePool.putTable(table);
			}
		}

	}

	/**
	 * Looks up rows from HBase table and returns PersistentEntity instances corresponding to them.
	 * @param hbaseConfig HBase details for connection purposes
	 * @param entity Type of entity to fetch
	 * @param metadata Table mapping details
	 * @return List of PersistentEntity instances
	 * @throws PersistenceException in case anything goes wrong
	 */
	@SuppressWarnings("unused")
	private List<PersistentEntity> findEntities(HBaseConfiguration hbaseConfig, PersistentEntity entity, HbaseMapping metadata) throws PersistenceException {
		List<PersistentEntity> searchResultList = null;
		HTable table = null;
		try {
			if (hbaseTablePool==null){				
				hbaseTablePool = new HTablePool(hbaseConfig,HTABLE_POOL_SIZE);				
			}
			table = (HTable)hbaseTablePool.getTable(metadata.getHbaseClass().getTable());
			
			byte[] rowKey = constructRowKey(entity, metadata.getHbaseClass().getRowkeyDefinition());

			searchResultList = new ArrayList<PersistentEntity>();

			if (rowKey != null && rowKey.length > 0) {
				// do a get operation
				Get g = constructGetQuery(metadata, entity, rowKey);
				Result result = table.get(g);
				if(!result.isEmpty()){
					searchResultList.add(constructEntityFromResultRow(metadata, result, entity));
				}			

			} else {
				// do a scan operation
				HBaseCriteria critiera = (HBaseCriteria) entity.getCriteriaForLoad();
				Scan s = constructScanQuery(metadata, critiera);
				ResultScanner scanner = table.getScanner(s);
				
				try {
					for (Result resultRow = scanner.next(); resultRow != null; resultRow = scanner.next()) {
						if (!resultRow.isEmpty()) {
							searchResultList.add(constructEntityFromResultRow(metadata, resultRow, entity));
							
							//Check if we need to return only specific number of records.
							if (critiera.getNumRecordsToFetch() > 0 && searchResultList.size() >= critiera.getNumRecordsToFetch()) {
								break;
							}
						}
					}
				} catch (Exception e) {
					logger.error("Error while scanning results for " + metadata.getHbaseClass().getTable(), e);
				} finally {
					scanner.close();
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred in searchData:", e);
			throw new PersistenceException("Exception occcurred while performing search for table" + metadata.getHbaseClass().getTable(), e);
		} finally {
			if (table != null) {
				hbaseTablePool.putTable(table);
			}
		}

		// return a list of PersistentEntity objects
		return searchResultList;
	}

	/**
	 * Retrieves a column family name as bytes
	 * @param column Column definition
	 * @return byte array representing column family
	 */
	private byte[] getColumnFamilyInBytes(ColumnDefinition column) {
		return Bytes.toBytes(column.getColumnFamily());
	}
	private byte[] getColumnFamilyInBytes(MapDefinition mapDefinition) {
		return Bytes.toBytes(mapDefinition.getColumnFamily());
	}

	/**
	 * Retrieves the value of the column specified by <code>column</code> as byte array
	 * @param entity PersistentEntity in the context,
	 * @param column Column definition of the column in the context
	 * @return byte array representing column value as needed by HBase APIs
	 * @throws ConfigurationException 
	 */
	private byte[] getColumnValueInBytes(PersistentEntity entity, ColumnDefinition column) throws PersistenceException {
		return convertToBytes(getAttribute(entity, column.getValueAttribute()));
	}

	/**
	 * Encodes a given byte array using set of processors.
	 * As part of encoding, it prepends the processor name to the value so that
	 * while decoding the application can determine whether processor used at the time
	 * of save is available at the time of read or not.  This is important as it will ease
	 * the gradual inclusion of new processor wherein some of the data in database may be
	 * encoded and some may not be.  The decoder logic will try to decode any byte array based
	 * on decoder name that is found in front of the value bytes.
	 * 
	 * @param hbaseClass HBase class definition, used for logging purposes only
	 * @param processorList Comma separate list of processors
	 * @param input Byte array to encode
	 * @return Encoded tye arra
	 * @throws ConfigurationException if processor is not found
	 */
	private byte[] encode(HbaseClass hbaseClass, String processorList, byte[] input) throws PersistenceException {
		byte[] encodedValue = input;
		if (StringUtils.isNotBlank(processorList)) {
			String[] processors = processorList.split(",");
			for (int i = 0; i < processors.length; i++) {
				if (StringUtils.isNotBlank(processors[i])) {
					ByteArrayProcessor processor = this.processorMap.get(processors[i].trim());
					if (processor != null) {
						if (processor.isEnabled()) {
							try {
								encodedValue = processor.encode(encodedValue);
							} catch (PersistenceException e) {
								throw new PersistenceException("Processor '" + processors[i] + "' error '" + hbaseClass.getTable() + "'", e);
							}
							encodedValue = Bytes.add(decoratedName(processors[i]).getBytes(), encodedValue);
						} else {
							logger.debug(" Processor '" + processors[i] + "'is configure, but disabled for '" + hbaseClass.getTable() + "', skipping it.");
						}
					} else {
						logger.warn("WARNING!!! Processor '" + processors[i] + "' not found for table '" + hbaseClass.getTable() + "', skipping it. Please CHECK your configuration to ensure this is not an error.");
					}
				}
			}
		}
		return encodedValue;
	}
	
	/**
	 * Decodes a given byte array using a list of processors.  Important thing to note here is that even
	 * though entity.hbase.xml may indicate that certain decoder be used, this code intelligently ignores
	 * a given processor if the data that was read from database was never really encoded using that 
	 * processor (Refer to Bytes.startsWith line in the code wherein a check is performed to check whether
	 * data is encoded using given processor or not).  
	 *    
	 * @param hbaseClass HBase class definition, used for logging purposes only
	 * @param processorList List of processors that should be used for decoding.
	 * @param input Encoded byte array
	 * @return Decoded byte array
	 * @throws ConfigurationException if processor used for encoding is no more available.
	 */
	private byte[] decode(HbaseClass hbaseClass, String processorList, byte[] input) throws ConfigurationException {
		byte[] decodedValue = input;
		if (StringUtils.isNotBlank(processorList)) {
			String[] processors = processorList.split(",");
			for (int i = 0; i < processors.length; i++) {
				if (StringUtils.isNotBlank(processors[i])) {
					byte[] processorNameInBytes = decoratedName(processors[i]).getBytes();
					if (decodedValue.length > processorNameInBytes.length) {
						if (Bytes.compareTo( Bytes.head(decodedValue, processorNameInBytes.length), processorNameInBytes) == 0) {
							ByteArrayProcessor processor = this.processorMap.get(processors[i].trim());
							if (processor != null) {
								try {
									decodedValue = processor.decode(Bytes.tail(decodedValue, decodedValue.length - decoratedName(processors[i]).getBytes().length));
								} catch (PersistenceException e) {
									throw new PersistenceException("Processor '" + processors[i] + "' error '" + hbaseClass.getTable() + "'", e);
								}
							} else {
								logger.warn("WARNING!!! Processor '" + processors[i] + "' not found for table '" + hbaseClass.getTable() + "', skipping it. Please CHECK your configuration to ensure this is not an error.");
							}
						}
					}
				}
			}
		}
		return decodedValue;
	}

	private String decoratedName(String string) {
		return PROCESSOR_BEGIN_INDICATOR + string.trim() + PROCESSOR_END_INDICATOR;
	}

	/**
	 * Returns the byte array representation of the column represented by <code>column</column>
	 * @param entity PersistentEntity instance in the context. Some columns may have variable part in column qualifier whose value may have to be retrieved from the PersistentEntity instance.
	 * @param column Column definition of the column in the context
	 * @return byte array representing column qualifier as needed by HBase APIs
	 * @throws ConfigurationException 
	 */
	private byte[] getColumnQualifierInBytes(PersistentEntity entity, ColumnDefinition column) throws ConfigurationException {

		byte[] columnQualifier = new byte[0];
		
		if (StringUtils.isNotBlank(column.getColumnQualifier())) {
			columnQualifier = Bytes.add(columnQualifier, column.getColumnQualifier().getBytes());	
		}
		
		if (StringUtils.isNotBlank(column.getColumnQualifierAttribute())) {
			try {
				columnQualifier = Bytes.add(columnQualifier, convertToBytes(getAttribute(entity, column.getColumnQualifierAttribute())));
			} catch (Exception e) {
				logger.error("Error reading column qualifier value for : " + column.getColumnQualifierAttribute(), e);
			}
		}
	
		if (columnQualifier.length == 0) {
			throw new ConfigurationException("Could not determine the column qualifier value for a column from family: " + column.getColumnFamily());
		}
		
		return columnQualifier;
	}
	
	private byte[] getColumnQualifierInBytes(PersistentEntity entity, MapDefinition mapDefinition) throws ConfigurationException {

		byte[] columnQualifier = new byte[0];
		
		if (StringUtils.isNotBlank(mapDefinition.getColumnQualifier())) {
			columnQualifier = Bytes.add(columnQualifier, mapDefinition.getColumnQualifier().getBytes());	
		}
		
		if (columnQualifier.length == 0) {
			throw new ConfigurationException("Could not determine the column qualifier value for a column from family: " + mapDefinition.getColumnFamily());
		}
		
		return columnQualifier;
	}

	
	private final byte[] constructRowKey(PersistentEntity obj, RowKeyDefinition rowIdMetaData) throws PersistenceException {
		try {
			if (rowIdMetaData.getCompositeRowKey() != null) {
				//Compose row key from multiple attributes
				
				List<RowKeyMember> rowKeyMembers = (List<RowKeyMember>) rowIdMetaData.getCompositeRowKey();
				
				byte[] idValue = new byte[0];
				for (RowKeyMember rowKeyMember : rowKeyMembers) {
					//Get the value of each attribute and concatenate bytes to form the composite row key
					idValue = Bytes.add(idValue, convertToBytes(getAttribute(obj, rowKeyMember.getValueAttribute())));
				}
				
				return idValue;
			} else {
				byte[] idValue = convertToBytes(getAttribute(obj, rowIdMetaData.getValueAttribute()));
				return idValue;
			}
			
		} catch (Exception e) {
			logger.error("Error reading ID attribute : " + rowIdMetaData.getValueAttribute(), e);
			return new byte[0];
		}
	}

	@SuppressWarnings("rawtypes")
	private Get constructGetQuery(HbaseMapping metadata, PersistentEntity entity, byte[] rowId) throws PersistenceException {

		Get getRequest = null;

		try {
			getRequest = new Get(rowId);

			HBaseCriteria queryCriteria = (HBaseCriteria) entity.getCriteriaForLoad();
			
			if (queryCriteria !=null){
			/* add the maximum versions to fetch */
			if (queryCriteria.getNumVersionsToFetch() > 1) {
				getRequest.setMaxVersions(queryCriteria.getNumVersionsToFetch());
			}

			/* add the time range for fetching records */
			if (queryCriteria.getStartTimestamp() != 0) {
				/*
				 * Verify the end timestamp value if it is empty we will use the
				 * current system time
				 */
				if (queryCriteria.getEndTimestamp() == 0) {
					queryCriteria.setEndTimestamp(System.currentTimeMillis());
				}
				getRequest.setTimeRange(queryCriteria.getStartTimestamp(), queryCriteria.getEndTimestamp());
			}
			}

			for (ColumnDefinition column : metadata.getHbaseClass().getColumnDefinition()) {
				/*
				 * search by columnfamily:qualifier column family should contain
				 * value but qualifier can be empty
				 */
				
				byte[] columnQualifier = getColumnQualifierInBytes(entity, column);

				if (columnQualifier == null || columnQualifier.length == 0) {
					getRequest.addColumn(getColumnFamilyInBytes(column), columnQualifier);
				} else {
					getRequest.addColumn(Bytes.add(getColumnFamilyInBytes(column), FAMILY_QUALIFIER_SEPARATOR.getBytes(), columnQualifier), columnQualifier);
				}
			}
			
			for (MapDefinition mapDefinition : metadata.getHbaseClass().getMapDefinition()) {
				byte[] columnQualifier = getColumnQualifierInBytes(entity, mapDefinition);
				
				Map m = (Map)getAttribute(entity, mapDefinition.getMapAttribute());
				for (Object key : m.keySet()) {
					if (key != null) {
						if (columnQualifier == null || columnQualifier.length == 0) {
							getRequest.addColumn(Bytes.add(getColumnFamilyInBytes(mapDefinition), FAMILY_QUALIFIER_SEPARATOR.getBytes(), convertToBytes(key)), columnQualifier);
						} else {
							byte[] columnFamily = Bytes.add(getColumnFamilyInBytes(mapDefinition), FAMILY_QUALIFIER_SEPARATOR.getBytes());
							getRequest.addColumn(Bytes.add(columnFamily, columnQualifier, convertToBytes(key)), columnQualifier);
						}
					}
				}
			}
	
		} catch (IOException e) {
			throw new PersistenceException("Exception occurred while constructing get query for table " + metadata.getHbaseClass().getTable(), e);
		}
		return getRequest;
	}

	private Scan constructScanQuery(HbaseMapping metadata, HBaseCriteria queryCriteria) throws PersistenceException {

		Scan scanRequest = null;
		try {
			
			if (queryCriteria != null){				
			if (queryCriteria.getScan() != null) {
				scanRequest = queryCriteria.getScan();
			} else {
				scanRequest = new Scan();

				/* add the maximum versions to fetch */
				if (queryCriteria.getNumVersionsToFetch() > 1) {
					scanRequest.setMaxVersions(queryCriteria.getNumVersionsToFetch());
				}

				/* add the time range for fetching records */
				if (queryCriteria.getStartTimestamp() != 0) {
					/**
					 * Verify the end timestamp value if it is empty we will use the
					 * current system time
					 */
					if (queryCriteria.getEndTimestamp() == 0) {
						queryCriteria.setEndTimestamp(System.currentTimeMillis());
					}
					scanRequest.setTimeRange(queryCriteria.getStartTimestamp(), queryCriteria.getEndTimestamp());
				}
			}
			} else{
				scanRequest = new Scan();
			}

		} catch (IOException e) {
			throw new PersistenceException("Error during constructing scan query, given time range is not valid for table " + metadata.getHbaseClass().getTable(), e);
		}
		return scanRequest;
	}

	/**
	 * Constructs an entity from the HBase query <code>Result</code>
	 * @param mapping HBase table mapping data
	 * @param resultRow Result of search result
	 * @param entity Type of entity to create
	 * @return Entity that is populated using Result
	 * @throws PersistenceException
	 * 
	 * NOTE:  This method cannot handle scenarios wherein a column qualifier is not a constant printable text, but instead is a 
	 *        a variable value.  In those cases, we cannot determine which attribute of the entity the value corresponds to.
	 *        Workaround is to use raw HBase APIs for reading data in such cases.
	 */
	private PersistentEntity constructEntityFromResultRow(HbaseMapping metadata, Result resultRow, PersistentEntity entity) throws ConfigurationException {
		PersistentEntity resEntity = null;
		
		try {
			resEntity = entity.getClass().newInstance();
			
			// Populate attribute from row key
			byte[] rowKey = resultRow.getRow();
			
			populateRowKeyAttributes(metadata, resEntity, rowKey);
			
			// Populate attributes from column values
			// TODO: How to handle multiple versions of columns?
			List<KeyValue> keyValuePairs = resultRow.list();
			if (keyValuePairs != null && keyValuePairs.size() > 0) {
				for (KeyValue keyValue : keyValuePairs) {
					String columnFamily = new String(keyValue.getFamily());
					String columnQualifier = new String(keyValue.getQualifier());
					
					ColumnDefinition columnDefinition = findColumnDefinition(metadata, columnFamily, columnQualifier);
					if (columnDefinition != null) {
						setAttribute(resEntity,	columnDefinition.getValueAttribute(),
								convertToObject(PropertyUtils.getPropertyDescriptor(resEntity,	
										columnDefinition.getValueAttribute()).getPropertyType(), 
										decode(metadata.getHbaseClass(), columnDefinition.getProcessors(), keyValue.getValue())));
					
						if (StringUtils.isNotBlank(columnDefinition.getColumnQualifierAttribute())) {
							byte[] columnQualifierAttributeValue = keyValue.getQualifier();
							
							// If column qualifier has fixed string literal part, then, remove those leading bytes to get the actual
							// value of the column qualifier attribute
							if (StringUtils.isNotBlank(columnDefinition.getColumnQualifier())) {
								columnQualifierAttributeValue = Bytes.tail(columnQualifierAttributeValue, columnQualifierAttributeValue.length - columnDefinition.getColumnQualifier().getBytes().length);
							}
							
							setAttribute(resEntity,	columnDefinition.getColumnQualifierAttribute(),
									convertToObject(PropertyUtils.getPropertyDescriptor(resEntity,	columnDefinition.getColumnQualifierAttribute()).getPropertyType(), columnQualifierAttributeValue));
						}
					} else {
						
						MapDefinition mapDefinition = findMapDefinition(metadata, columnFamily, columnQualifier);
						
						if (mapDefinition != null) {
							Map m = (Map)getAttribute(resEntity, mapDefinition.getMapAttribute());
							
							byte[] mapKeyValue = keyValue.getQualifier();
							if (StringUtils.isNotBlank(mapDefinition.getColumnQualifier())) {
								mapKeyValue = Bytes.tail(mapKeyValue, mapKeyValue.length - mapDefinition.getColumnQualifier().getBytes().length);
							}
							
							Object key = convertToObject(Class.forName(mapDefinition.getKeyType()), mapKeyValue);
							Object value = convertToObject(Class.forName(mapDefinition.getValueType()), 
									decode(metadata.getHbaseClass(), mapDefinition.getProcessors(), keyValue.getValue()));
							m.put(key, value);
						}
					}
				}
			}

		} catch (Exception e){
			throw new PersistenceException("Error while creating entity for table " + metadata.getHbaseClass().getTable(), e);
		}

		return resEntity;
	}

	private void populateRowKeyAttributes(HbaseMapping metadata, PersistentEntity resEntity, byte[] rowKey) throws ConfigurationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		// If composite row key, loop through all row key members and populate entity accordingly
		if (metadata.getHbaseClass().getRowkeyDefinition().getCompositeRowKey() != null) {
			List<RowKeyMember> rowKeyMembers = metadata.getHbaseClass().getRowkeyDefinition().getCompositeRowKey().getRowKeyMember();
			
			int startIndex = 0;
			for (RowKeyMember rowKeyMember : rowKeyMembers) {
				// For each constituent of a composite row key, extract the corresponding bytes and store it in entity
				int endIndex = startIndex + rowKeyMember.getValueLength();
				byte[] part = extractBytes(rowKey, startIndex, endIndex);
				setAttribute(resEntity,
						rowKeyMember.getValueAttribute(),
						convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, rowKeyMember.getValueAttribute()).getPropertyType(), part));
				startIndex = endIndex;
			}
			
		} else {
			//Single attribute based row key
			setAttribute(resEntity,
					metadata.getHbaseClass().getRowkeyDefinition().getValueAttribute(),
					convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, metadata.getHbaseClass().getRowkeyDefinition().getValueAttribute()).getPropertyType(), rowKey));
		}
	}

	private byte[] extractBytes(byte[] rowKey, int startIndex, int endIndex) {
		byte[] extractedBytes = new byte[endIndex - startIndex];
		for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
			extractedBytes[j] = rowKey[i]; 
		}
		return extractedBytes;
	}

	/**
	 * This method iterates through the HbaseMapping entries and finds the Column definition for column identified by a given 
	 * <code>columnFamily</code> and <code>columnQualifier</code>
	 * @param metadata HBaseMapping object representing a Hbase table meta data
	 * @param columnFamilyFromHbaseRow Column family name
	 * @param columnQualifierFromHbaseRow Column qualifier name
	 * @return ColumnDefinition instance for the column 
	 */
	private ColumnDefinition findColumnDefinition(HbaseMapping metadata, String columnFamilyFromHbaseRow, String columnQualifierFromHbaseRow) {
		for (ColumnDefinition columnDefinition : metadata.getHbaseClass().getColumnDefinition()) {
			
			// We are looking for a column definition whose column family matches the one from HBase, and
			// whose column qualifier is a prefix in the column qualifier read from HBase.  This is needed
			// to support those columns which are comprised of a fixed part string literal value and a variable
			// value from the entity's attribute.  Hence, below code uses "starsWith" for matching column qualifier.
			if (columnFamilyFromHbaseRow != null && columnFamilyFromHbaseRow.equals(columnDefinition.getColumnFamily()) && 
					columnQualifierFromHbaseRow != null && columnQualifierFromHbaseRow.startsWith(columnDefinition.getColumnQualifier())) {
				return columnDefinition;
			}
		}
		return null;
	}
	
	/**
	 * This method iterates through the HbaseMapping entries and finds the Map definition for map identified by a given 
	 * <code>columnFamily</code> and <code>columnQualifier</code>
	 * @param metadata HBaseMapping object representing a Hbase table meta data
	 * @param columnFamilyFromHbaseRow Column family name
	 * @param columnQualifierFromHbaseRow Column qualifier name
	 * @return MapDefinition instance for the map 
	 */
	private MapDefinition findMapDefinition(HbaseMapping metadata, String columnFamilyFromHbaseRow, String columnQualifierFromHbaseRow) {
		for (MapDefinition mapDefinition : metadata.getHbaseClass().getMapDefinition()) {
			
			// We are looking for a column definition whose column family matches the one from HBase, and
			// whose column qualifier is a prefix in the column qualifier read from HBase.  This is needed
			// to support those columns which are comprised of a fixed part string literal value and a variable
			// value from the entity's attribute.  Hence, below code uses "starsWith" for matching column qualifier.
			if (columnFamilyFromHbaseRow != null && columnFamilyFromHbaseRow.equals(mapDefinition.getColumnFamily()) && 
					columnQualifierFromHbaseRow != null && columnQualifierFromHbaseRow.startsWith(mapDefinition.getColumnQualifier())) {
				return mapDefinition;
			}
		}
		return null;
	}

	

	/**
	 * Sets value for an entity's attribute
	 * @param entity  HBase entity that is being updated
	 * @param attribute  Attribute whose value is to be updated
	 * @param value Value for the attribute
	 * @throws PersistenceException
	 */
	private void setAttribute(PersistentEntity entity, String attribute, Object value) throws PersistenceException {
		try {
			if (StringUtils.isNotBlank(attribute)) {
				PropertyUtils.setProperty(entity, attribute, value);
			}
		} catch (Exception e) {
			logger.error("Error setting attribute " + attribute, e);
		}
	}
	
	/**
	 * Returns the value of an entity's attribute
	 * @param entity Entity whose attribute's value is being read
	 * @param attribute Name of the attribute whose value is being read
	 * @return Object represent the value of the attribute
	 */
	private Object getAttribute(Object entity, String attribute) {
		Object returnValue = null;
		try {
			returnValue = PropertyUtils.getProperty(entity, attribute);
		} catch (Exception e) {
			logger.error("Error reading attribute : '" + attribute + "' in class " + entity.getClass().getName(), e);
		}
		return returnValue;
	}

	/**
	 * This method converts given value into a byte array.  It applies appropriate conversion logic based on data type of the <code>value</code>
	 * @param value Object representing input value
	 * @param valueType HbaseType representing data type of <code>value</code>
	 * @return byte array that is byte representation of <code>value</code>
	 * @throws ConfigurationException 
	 */
	private byte[] convertToBytes(Object value) throws ConfigurationException {
		if (value != null) {
			if (value instanceof byte[]) {
				return (byte[])value;
			} else {
				Serializer serializer = this.classNameToSerializerMap.get(value.getClass().getName());
				if (serializer != null) {
					return serializer.toBytes(value);
				} else {
					throw new ConfigurationException("Don't know how to serialize " + value.getClass().getName());
				}
			}
		}
		return new byte[0];
	}

	/**
	 * This method converts a byte array into a value of type as specified by HbaseType
	 * @param bytes  Byte array input as read from HBase 
	 * @param valueType HbaseType that represents the data type to which the <code>bytes</code> should be converted to
	 * @return Object whose value is constructed from <code>bytes</code> and is of type as specified by <code>valueType</code>
	 * @throws ConfigurationException 
	 */
	private Object convertToObject(Class targetClass, byte[] bytes) throws ConfigurationException {
		if (bytes != null) {
			if (targetClass == byte[].class) {
				return bytes;
			} else {
				Serializer serializer = this.classNameToSerializerMap.get(targetClass.getName());
				if (serializer != null) {
					return serializer.toObject(bytes);
				} else {
					throw new ConfigurationException("Don't know how to serialize " + targetClass.getName());
				}
			}
		}
		return null;
	}


	public void setUseWAL(Boolean useWAL) {
		this.useWAL = useWAL;
	}

	public Boolean getUseWAL() {
		return useWAL;
	}

	public void setUseAutoFlush(Boolean useAutoFlush) {
		this.useAutoFlush = useAutoFlush;
	}

	public Boolean getUseAutoFlush() {
		return useAutoFlush;
	}
	
	public void setHTABLE_POOL_SIZE(int htable_pool_size) {
		HTABLE_POOL_SIZE = htable_pool_size;
	}

	public int getHTABLE_POOL_SIZE() {
		return HTABLE_POOL_SIZE;
	}

	
	/**
	 * Interface method implementation. Returns the passed in bean identifier i.e. beanKey param appended with the Java hashCode of a newly
	 * created Java Object.
	 * @see org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean#getMBeanNameSuffix(Object, String)
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey) {
		// we could have used the hashCode of the managedBean object which apparently does not work when the object is instantiated using a Spring
		// ApplicationContext. The reason is not known and hence choosing the safer option of creating a new Object and using its hashcode instead.
		return "-" + beanKey + "[" + new Object().hashCode()+ "]";
	}

	@Override
	public PersistentEntity makePersistent(PersistentEntity entity)
			throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #togglePerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();

		PersistentEntity hEntity = (PersistentEntity) entity;

		HbaseMapping metadata = hbaseMappingContainer.getMappingForClass(hEntity.getClass().getName());
		addEntity((HBaseConfiguration) hbaseConfiguration, metadata, hEntity);

		// log performance metrics captured. actual capture will happen only if it has been enabled via #togglePerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HBaseHandler.makePersistent", entity.getClass().getName());

		return hEntity;
	}

	@Override
	public void makeTransient(PersistentEntity entity)
			throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #togglePerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		
		PersistentEntity hEntity = (PersistentEntity) entity;
		HbaseMapping metadata = hbaseMappingContainer.getMappingForClass(hEntity.getClass().getName());
		deleteEntity((HBaseConfiguration) hbaseConfiguration, hEntity, metadata);
		// log performance metrics captured. actual capture will happen only if it has been enabled via #togglePerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("FSHandler.makeTransient", entity.getClass().getName());

		
	}

	
	// ///////////////////// UNSUPPORTED OPERATIONS ////////////////////////////
	

	@Override
	public int update(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Operation not supported!!!!!");
	}
	
	public void setProcessorMap(Map<String, ByteArrayProcessor> processorMap) {
		this.processorMap = processorMap;
	}
	@Override
	public PersistentEntity findEntity(Criteria criteria)
			throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public PersistentEntity findEntity(PersistentEntity entity)
			throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public Collection<PersistentEntity> findEntities(Criteria criteria)
			throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

	
}
