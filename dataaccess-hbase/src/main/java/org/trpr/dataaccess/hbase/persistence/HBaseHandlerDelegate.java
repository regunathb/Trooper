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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.dataaccess.hbase.HTablePool;
import org.trpr.dataaccess.hbase.mappings.config.HBaseMappingContainer;
import org.trpr.dataaccess.hbase.model.config.ColumnDefinition;
import org.trpr.dataaccess.hbase.model.config.HbaseClass;
import org.trpr.dataaccess.hbase.model.config.HbaseMapping;
import org.trpr.dataaccess.hbase.model.config.RowKeyDefinition;
import org.trpr.dataaccess.hbase.model.config.RowKeyMember;
import org.trpr.dataaccess.hbase.persistence.entity.HBaseEntity;
import org.trpr.dataaccess.hbase.serializer.DateSerializer;
import org.trpr.dataaccess.hbase.serializer.IntegerSerializer;
import org.trpr.dataaccess.hbase.serializer.LongSerializer;
import org.trpr.dataaccess.hbase.serializer.StringSerializer;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.core.spi.persistence.Serializer;
import org.trpr.platform.runtime.spi.config.ConfigurationException;

/**
 * The <code>HBaseHandlerDelegate</code> is a delegate providing persistence
 * operations using HTablePool instances created and managed by the
 * {@link HBaseHandler}.
 * 
 * @author Regunath B
 */
public class HBaseHandlerDelegate implements InitializingBean {

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(HBaseHandlerDelegate.class);

	/** The serializer map */
	private Map<String, Serializer> classNameToSerializerMap = new HashMap<String, Serializer>();

	/** The HBase mapping container instance */
	private HBaseMappingContainer hbaseMappingContainer;

	/** Properties to control HBase data writes */
	private Boolean useWAL = true;
	private Boolean useAutoFlush = true;
	
	/**
	 * No args constructor. Instance created using this default constructor is useful after #setClassNameToSerializerMap() and #setHBaseMappingContainer()
	 * have been called subsequently.
	 */
	public HBaseHandlerDelegate() {
	}

	/**
	 * Constructor for this delegate Initialized for use after 
	 * 
	 * @param hbaseMappingContainer
	 *            the HBaseMappingContainer instance containing meta data used
	 *            in persistence
	 */
	public HBaseHandlerDelegate(HBaseMappingContainer hbaseMappingContainer) {
		// Default serializers. It can be overridden by setting new values in
		// Spring bean definition of the HBaseHandler
		classNameToSerializerMap.put("java.lang.String", new StringSerializer());
		classNameToSerializerMap.put("java.lang.Long", new LongSerializer());
		classNameToSerializerMap.put("java.lang.Integer", new IntegerSerializer());
		classNameToSerializerMap.put("java.util.Date", new DateSerializer());		
		this.setHBaseMappingContainer(hbaseMappingContainer);
	}

	/**
	 * Method to replace the default serializer map.
	 * 
	 * @param classNameToSerializerMap
	 *            map containing class names and their serializers
	 */
	public void setClassNameToSerializerMap(Map<String, Serializer> classNameToSerializerMap) {
		// Do a putAll to avoid loosing the default serializer mappings
		this.classNameToSerializerMap.putAll(classNameToSerializerMap);
	}
	
	/**
	 * Initializing bean method implementation. Checks to see if required properties are set
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.classNameToSerializerMap, "The 'classNameToSerializerMap' may not be null");
	}
	
	/**
	 * Sets the specified HBaseMappingContainer for use by this instance. Package level scope as we don't expect this to be set directly
	 * @param hbaseMappingContainer the HBaseMappingContainer instance
	 */
	void setHBaseMappingContainer(HBaseMappingContainer hbaseMappingContainer) {
		this.hbaseMappingContainer = hbaseMappingContainer;
	}	
	
	/**
	 * Persists the specified entity using the specified HTablePool
	 * 
	 * @param entity
	 *            the HBaseEntity to persist
	 * @param hbaseTablePool
	 *            the HTablePool to use for persistence
	 * @return the persisted PersistentEntity
	 * @throws PersistenceException
	 *             in case of persistence errors
	 */
	public PersistentEntity makePersistent(HBaseEntity entity, HTablePool hbaseTablePool) throws PersistenceException {
		HbaseMapping metadata = hbaseMappingContainer.getMappingForClass(entity.getClass().getName());
		addEntity(hbaseTablePool, metadata, entity);
		return entity;
	}

	/**
	 * Deletes the specified entity using the specified HTablePool
	 * 
	 * @param entity
	 *            the HBaseEntity to delete
	 * @param hbaseTablePool
	 *            the HTablePool to use for persistence
	 * @return the PersistentEntity to delete
	 * @throws PersistenceException
	 *             in case of persistence errors
	 */
	public void makeTransient(HBaseEntity entity, HTablePool hbaseTablePool) throws PersistenceException {
		HbaseMapping metadata = hbaseMappingContainer.getMappingForClass(entity.getClass().getName());
		deleteEntity(hbaseTablePool, entity, metadata);
	}

	/**
	 * Inserts a row in the HBase table. In case the row already exists, it
	 * results in creation of a new version for all the columns.
	 * 
	 * @param hbaseTablePool
	 *            HTablePool instance for HBase access
	 * @param metadata
	 *            HBase mapping defining mapping from PersistentEntity to
	 *            various columns of HBase table
	 * @param entity
	 *            HBaseEntity to persist.
	 * @throws PersistenceException
	 *             In cases when HBase is not reachable or table does not exist
	 *             or any other errors.
	 */
	private void addEntity(HTablePool hbaseTablePool, HbaseMapping metadata, HBaseEntity entity) throws PersistenceException {
		HTableInterface table = null;
		HbaseClass classDefinition = metadata.getHbaseClass();
		try {
			table = (HTableInterface) hbaseTablePool.getTable(classDefinition.getTable());
			table.setAutoFlush(useAutoFlush);

			Put put = new Put(constructRowKey(entity, classDefinition.getRowkeyDefinition()));
			put.setWriteToWAL(useWAL);

			for (ColumnDefinition column : classDefinition.getColumnDefinition()) {
				put.add(getColumnFamilyInBytes(column), getColumnQualifierInBytes(entity, column), getColumnValueInBytes(entity, column));
			}

			// Insert new row into HBase table
			table.put(put);
		} catch (Exception e) {
			throw new PersistenceException("Exception in putData of " + classDefinition.getTable(), e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					LOGGER.warn("Error returning table to the pool : " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Deletes a row in HBase table
	 * 
	 * @param hbaseTablePool
	 *            the HBase table to use for HBase access
	 * @param entity
	 *            HBaseEntity to delete
	 * @param metadata
	 *            HBase table mapping definition
	 * @throws PersistenceException
	 *             in case anything goes wrong
	 */
	private void deleteEntity(HTablePool hbaseTablePool, HBaseEntity entity, HbaseMapping metadata) throws PersistenceException {
		HTableInterface table = null;
		try {
			table = (HTableInterface) hbaseTablePool.getTable(metadata.getHbaseClass().getTable());
			Delete delete = new Delete(constructRowKey(entity, metadata.getHbaseClass().getRowkeyDefinition()));
			table.delete(delete);
		} catch (Exception e) {
			throw new PersistenceException("Failed to delete entry for table " + metadata.getHbaseClass().getTable(), e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					LOGGER.warn("Error returning table to the pool : " + e.getMessage(), e);
				}
			}
		}
	}

	public HBaseEntity findEntity(HTablePool hbaseTablePool, HBaseEntity entity, HbaseMapping metadata) throws PersistenceException {
		HTableInterface table = null;
		try {
			table = (HTableInterface) hbaseTablePool.getTable(metadata.getHbaseClass().getTable());
			byte[] rowKey = constructRowKey(entity, metadata.getHbaseClass().getRowkeyDefinition());
			if (rowKey != null && rowKey.length > 0) {
				// do a get operation
				Get g = constructGetQuery(metadata, entity, rowKey);
				Result result = table.get(g);
				if (!result.isEmpty()) {
					return constructEntityFromResultRow(metadata, result, entity);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred in searchData:", e);
			throw new PersistenceException("Exception occcurred while performing search for table " + metadata.getHbaseClass().getTable(), e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					LOGGER.warn("Error returning table to the pool : " + e.getMessage(), e);
				}
			}
		}
		return null;
	}

	/**
	 * Looks up rows from HBase table and returns PersistentEntity instances
	 * corresponding to them.
	 * 
	 * @param hbaseTablePool
	 *            HBase table pool for HBase access
	 * @param entity
	 *            Type of entity to fetch
	 * @param metadata
	 *            Table mapping details
	 * @return List of PersistentEntity instances
	 * @throws PersistenceException
	 *             in case anything goes wrong
	 */
	public Collection<PersistentEntity> findEntities(HTablePool hbaseTablePool, HBaseCriteria criteria, HbaseMapping metadata) throws PersistenceException {
		List<PersistentEntity> searchResultList = new ArrayList<PersistentEntity>();
		HTableInterface table = null;
		try {
			table = (HTableInterface) hbaseTablePool.getTable(metadata.getHbaseClass().getTable());
			// do a scan operation
			Scan s = constructScanQuery(metadata, criteria);
			ResultScanner scanner = table.getScanner(s);
			// check to see if start and end indices have been specified, for pagination say
			int startIndex = criteria.getFirstResult();
			int maxResults = criteria.getMaxResults();
			int count = 0;
			for (Result resultRow = scanner.next(); resultRow != null; resultRow = scanner.next()) {
				if (!resultRow.isEmpty() && count >= startIndex) {
					searchResultList.add(constructEntityFromResultRow(metadata, resultRow, (HBaseEntity)criteria.getManagedClass().newInstance()));
				}
				count += 1;
				if (count >= maxResults) {
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred in searchData:", e);
			throw new PersistenceException("Exception occcurred while performing search for table " + metadata.getHbaseClass().getTable(), e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					LOGGER.warn("Error returning table to the pool : " + e.getMessage(), e);
				}
			}
		}
		return searchResultList;
	}

	/**
	 * Helper method to construct a HBase get query using the meta data
	 * specified for the specified HBase entity and the row id.
	 * 
	 * @param metadata
	 *            the HBase mapping meta data
	 * @param entity
	 *            the HBase entity to construct the get query for
	 * @param rowId
	 *            the HBase entity row Id
	 * @return the HBase Get query
	 * @throws ConfigurationException
	 *             in case of HBase access errors
	 */
	private Get constructGetQuery(HbaseMapping metadata, HBaseEntity entity, byte[] rowId) throws ConfigurationException {
		Get getRequest = null;
		try {
			getRequest = new Get(rowId);
			HBaseCriteria queryCriteria = (HBaseCriteria) entity.getCriteriaForLoad();
			if (queryCriteria != null) {
				/* add the maximum versions to fetch */
				int versions = queryCriteria.getParameter(HBaseCriteria.VERSIONS) == null ? 1 : Integer.valueOf((String)queryCriteria.getParameter(HBaseCriteria.VERSIONS));
				if (versions > 1) {
					getRequest.setMaxVersions(versions);
				}
				/* add the time range for fetching records */
				long startTimestamp = queryCriteria.getParameter(HBaseCriteria.START_TIMESTAMP) == null ? 0 : Long.valueOf((String)queryCriteria.getParameter(HBaseCriteria.START_TIMESTAMP));
				if (startTimestamp != 0) {
					/*
					 * Verify the end timestamp value if it is empty we will use
					 * the current system time
					 */
					long endTimestamp = queryCriteria.getParameter(HBaseCriteria.END_TIMESTAMP) == null ? 0 : Long.valueOf((String)queryCriteria.getParameter(HBaseCriteria.END_TIMESTAMP));
					if (endTimestamp == 0) {
						queryCriteria.addParameter(HBaseCriteria.END_TIMESTAMP,System.currentTimeMillis());
					}
					getRequest.setTimeRange(startTimestamp, endTimestamp);
				}
			}
			for (ColumnDefinition column : metadata.getHbaseClass().getColumnDefinition()) {
				getRequest.addColumn(getColumnFamilyInBytes(column), getColumnQualifierInBytes(entity, column));
			}
		} catch (IOException e) {
			throw new ConfigurationException("Exception occurred while constructing get query for table " + metadata.getHbaseClass().getTable() + " using entity " + entity.toString(), e);
		}
		return getRequest;
	}

	/**
	 * Construct and return a HBase scan query using the specified HBase mapping
	 * metadata for the specified query criteria
	 * 
	 * @param metadata
	 *            the HBase mapping meta data
	 * @param queryCriteria
	 *            the HBase query criteria
	 * @return the HBase scan query
	 * @throws ConfigurationException
	 *             in case of HBase access exceptions
	 */
	private Scan constructScanQuery(HbaseMapping metadata, HBaseCriteria queryCriteria) throws ConfigurationException {
		Scan scanRequest = null;
		try {
			if (queryCriteria != null) {
				if (queryCriteria.getScan() != null) {
					scanRequest = queryCriteria.getScan(); // use the Scan object passed in through the HBaseCriteria
				} else {
					scanRequest = new Scan();
					/* add the maximum versions to fetch */
					int versions = queryCriteria.getParameter(HBaseCriteria.VERSIONS) == null ? 1 : Integer.valueOf((String)queryCriteria.getParameter(HBaseCriteria.VERSIONS));
					if (versions > 1) {
						scanRequest.setMaxVersions(versions);
					}
					/* add the time range for fetching records */
					long startTimestamp = queryCriteria.getParameter(HBaseCriteria.START_TIMESTAMP) == null ? 0 : Long.valueOf((String)queryCriteria.getParameter(HBaseCriteria.START_TIMESTAMP));
					if (startTimestamp != 0) {
						/**
						 * Verify the end timestamp value if it is empty we will
						 * use the current system time
						 */
						long endTimestamp = queryCriteria.getParameter(HBaseCriteria.END_TIMESTAMP) == null ? 0 : Long.valueOf((String)queryCriteria.getParameter(HBaseCriteria.END_TIMESTAMP));
						if (endTimestamp == 0) {
							queryCriteria.addParameter(HBaseCriteria.END_TIMESTAMP,System.currentTimeMillis());
						}
						scanRequest.setTimeRange(startTimestamp, endTimestamp);
					}
					/* add start and end row keys if specified*/
					byte[] startKey = (byte[])queryCriteria.getParameter(HBaseCriteria.START_KEY);
					byte[] endKey = (byte[])queryCriteria.getParameter(HBaseCriteria.END_KEY);
					if (startKey != null) {
						scanRequest.setStartRow(startKey);
					}
					if (endKey != null) {
						scanRequest.setStopRow(endKey);
					}
				}
			} else {
				scanRequest = new Scan();
			}
		} catch (IOException e) {
			throw new ConfigurationException("Error during constructing scan query, given time range is not valid for table " + metadata.getHbaseClass().getTable(), e);
		}
		return scanRequest;
	}

	/**
	 * Constructs an entity from the HBase query <code>Result</code>
	 * 
	 * @param mapping
	 *            HBase table mapping data
	 * @param resultRow
	 *            Result of search result
	 * @param entity
	 *            Type of entity to create
	 * @return Entity that is populated using Result
	 * @throws ConfigurationException
	 * 
	 *             NOTE: This method cannot handle scenarios wherein a column
	 *             qualifier is not a constant printable text, but instead is a
	 *             a variable value. In those cases, we cannot determine which
	 *             attribute of the entity the value corresponds to. Workaround
	 *             is to use raw HBase APIs for reading data in such cases.
	 */
	private HBaseEntity constructEntityFromResultRow(HbaseMapping metadata, Result resultRow, HBaseEntity resEntity) throws ConfigurationException {
		try {
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
						setAttribute(resEntity, columnDefinition.getValueAttribute(), convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, columnDefinition.getValueAttribute()).getPropertyType(), keyValue.getValue()));

						if (StringUtils.isNotBlank(columnDefinition.getColumnQualifierAttribute())) {
							byte[] columnQualifierAttributeValue = keyValue.getQualifier();

							// If column qualifier has fixed string literal
							// part, then, remove those leading bytes to get the
							// actual
							// value of the column qualifier attribute
							if (StringUtils.isNotBlank(columnDefinition.getColumnQualifier())) {
								columnQualifierAttributeValue = Bytes.tail(columnQualifierAttributeValue, columnQualifierAttributeValue.length - columnDefinition.getColumnQualifier().getBytes().length);
							}

							setAttribute(resEntity, columnDefinition.getColumnQualifierAttribute(), convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, columnDefinition.getColumnQualifierAttribute()).getPropertyType(), columnQualifierAttributeValue));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ConfigurationException("Error while creating entity for table " + metadata.getHbaseClass().getTable(), e);
		}
		return resEntity;
	}

	/**
	 * Helper method to populate row key attributes using the specified data
	 */
	private void populateRowKeyAttributes(HbaseMapping metadata, PersistentEntity resEntity, byte[] rowKey) throws ConfigurationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// If composite row key, loop through all row key members and populate
		// entity accordingly
		if (metadata.getHbaseClass().getRowkeyDefinition().getCompositeRowKey() != null) {
			List<RowKeyMember> rowKeyMembers = metadata.getHbaseClass().getRowkeyDefinition().getCompositeRowKey().getRowKeyMember();

			int startIndex = 0;
			for (RowKeyMember rowKeyMember : rowKeyMembers) {
				// For each constituent of a composite row key, extract the
				// corresponding bytes and store it in entity
				int endIndex = startIndex + rowKeyMember.getValueLength();
				byte[] part = extractBytes(rowKey, startIndex, endIndex);
				setAttribute(resEntity, rowKeyMember.getValueAttribute(), convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, rowKeyMember.getValueAttribute()).getPropertyType(), part));
				startIndex = endIndex;
			}
		} else {
			// Single attribute based row key
			setAttribute(resEntity, metadata.getHbaseClass().getRowkeyDefinition().getValueAttribute(), convertToObject(PropertyUtils.getPropertyDescriptor(resEntity, metadata.getHbaseClass().getRowkeyDefinition().getValueAttribute()).getPropertyType(), rowKey));
		}
	}

	/**
	 * Helper method to extract bytes from the specified row key data
	 */
	private byte[] extractBytes(byte[] rowKey, int startIndex, int endIndex) {
		byte[] extractedBytes = new byte[endIndex - startIndex];
		for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
			extractedBytes[j] = rowKey[i];
		}
		return extractedBytes;
	}

	/**
	 * This method iterates through the HbaseMapping entries and finds the
	 * Column definition for column identified by a given
	 * <code>columnFamily</code> and <code>columnQualifier</code>
	 * 
	 * @param metadata
	 *            HBaseMapping object representing a Hbase table meta data
	 * @param columnFamilyFromHbaseRow
	 *            Column family name
	 * @param columnQualifierFromHbaseRow
	 *            Column qualifier name
	 * @return ColumnDefinition instance for the column
	 */
	private ColumnDefinition findColumnDefinition(HbaseMapping metadata, String columnFamilyFromHbaseRow, String columnQualifierFromHbaseRow) {
		for (ColumnDefinition columnDefinition : metadata.getHbaseClass().getColumnDefinition()) {
			// We are looking for a column definition whose column family
			// matches the one from HBase, and
			// whose column qualifier is a prefix in the column qualifier read
			// from HBase. This is needed
			// to support those columns which are comprised of a fixed part
			// string literal value and a variable
			// value from the entity's attribute. Hence, below code uses
			// "starsWith" for matching column qualifier.
			if (columnFamilyFromHbaseRow != null && columnFamilyFromHbaseRow.equals(columnDefinition.getColumnFamily()) && columnQualifierFromHbaseRow != null && columnQualifierFromHbaseRow.startsWith(columnDefinition.getColumnQualifier())) {
				return columnDefinition;
			}
		}
		return null;
	}

	/**
	 * Sets value for an entity's attribute
	 * 
	 * @param entity
	 *            HBase entity that is being updated
	 * @param attribute
	 *            Attribute whose value is to be updated
	 * @param value
	 *            Value for the attribute
	 * @throws ConfigurationException
	 */
	private void setAttribute(PersistentEntity entity, String attribute, Object value) throws ConfigurationException {
		try {
			if (StringUtils.isNotBlank(attribute)) {
				PropertyUtils.setProperty(entity, attribute, value);
			}
		} catch (Exception e) {
			LOGGER.error("Error setting attribute " + attribute, e);
		}
	}

	/**
	 * This method converts a byte array into a value of type as specified by
	 * HbaseType
	 * 
	 * @param bytes
	 *            Byte array input as read from HBase
	 * @param valueType
	 *            HbaseType that represents the data type to which the
	 *            <code>bytes</code> should be converted to
	 * @return Object whose value is constructed from <code>bytes</code> and is
	 *         of type as specified by <code>valueType</code>
	 * @throws ConfigurationException
	 */
	@SuppressWarnings({ "unchecked" })
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

	/**
	 * Retrieves a column family name as bytes
	 * 
	 * @param column
	 *            Column definition
	 * @return byte array representing column family
	 */
	private byte[] getColumnFamilyInBytes(ColumnDefinition column) {
		return Bytes.toBytes(column.getColumnFamily());
	}

	/**
	 * Retrieves the value of the column specified by <code>column</code> as
	 * byte array
	 * 
	 * @param entity
	 *            PersistentEntity in the context,
	 * @param column
	 *            Column definition of the column in the context
	 * @return byte array representing column value as needed by HBase APIs
	 * @throws ConfigurationException
	 */
	private byte[] getColumnValueInBytes(PersistentEntity entity, ColumnDefinition column) throws ConfigurationException {
		return convertToBytes(getAttribute(entity, column.getValueAttribute()));
	}

	/**
	 * Returns the byte array representation of the column represented by
	 * <code>column</column>
	 * 
	 * @param entity
	 *            PersistentEntity instance in the context. Some columns may
	 *            have variable part in column qualifier whose value may have to
	 *            be retrieved from the PersistentEntity instance.
	 * @param column
	 *            Column definition of the column in the context
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
				LOGGER.error("Error reading column qualifier value for : " + column.getColumnQualifierAttribute(), e);
			}
		}
		if (columnQualifier.length == 0) {
			throw new ConfigurationException("Could not determine the column qualifier value for a column from family: " + column.getColumnFamily());
		}
		return columnQualifier;
	}

	/**
	 * Helper method to construct a row key for the passed in HBase entity using
	 * the row key definition
	 * 
	 * @param obj
	 *            the HBase entity to construct the row key for
	 * @param rowIdMetaData
	 *            the row key definition meta data
	 * @return the row key as a byte array
	 * @throws ConfigurationException
	 *             in case of any HBase access exceptions
	 */
	private final byte[] constructRowKey(PersistentEntity obj, RowKeyDefinition rowIdMetaData) throws ConfigurationException {
		try {
			if (rowIdMetaData.getCompositeRowKey() != null) {
				// Compose row key from multiple attributes
				List<RowKeyMember> rowKeyMembers = rowIdMetaData.getCompositeRowKey().getRowKeyMember();

				byte[] idValue = new byte[0];
				for (RowKeyMember rowKeyMember : rowKeyMembers) {
					// Get the value of each attribute and concatenate bytes to
					// form the composite row key
					idValue = Bytes.add(idValue, convertToBytes(getAttribute(obj, rowKeyMember.getValueAttribute())));
				}
				return idValue;
			} else {
				byte[] idValue = convertToBytes(getAttribute(obj, rowIdMetaData.getValueAttribute()));
				return idValue;
			}
		} catch (Exception e) {
			LOGGER.error("Error reading ID attribute : " + rowIdMetaData.getValueAttribute(), e);
			return new byte[0];
		}
	}

	/**
	 * This method converts given value into a byte array. It applies
	 * appropriate conversion logic based on data type of the <code>value</code>
	 * 
	 * @param value
	 *            Object representing input value
	 * @param valueType
	 *            HbaseType representing data type of <code>value</code>
	 * @return byte array that is byte representation of <code>value</code>
	 * @throws ConfigurationException
	 */
	private byte[] convertToBytes(Object value) throws ConfigurationException {
		if (value != null) {
			if (value instanceof byte[]) {
				return (byte[]) value;
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
	 * Returns the value of an entity's attribute
	 * 
	 * @param entity
	 *            Entity whose attribute's value is being read
	 * @param attribute
	 *            Name of the attribute whose value is being read
	 * @return Object represent the value of the attribute
	 */
	private Object getAttribute(Object entity, String attribute) {
		Object returnValue = null;
		try {
			returnValue = PropertyUtils.getProperty(entity, attribute);
		} catch (Exception e) {
			LOGGER.error("Error reading attribute : '" + attribute + "' in class " + entity.getClass().getName(), e);
		}
		return returnValue;
	}

	/** Getter/Setter methods */
	public void setUseWAL(Boolean useWAL) {
		this.useWAL = useWAL;
	}

	public void setUseAutoFlush(Boolean useAutoFlush) {
		this.useAutoFlush = useAutoFlush;
	}

}
