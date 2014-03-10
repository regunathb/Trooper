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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.trpr.dataaccess.hbase.HTablePool;
import org.trpr.dataaccess.hbase.auth.AuthenticationProvider;
import org.trpr.dataaccess.hbase.mappings.config.HBaseMappingContainer;
import org.trpr.dataaccess.hbase.model.config.HbaseMapping;
import org.trpr.dataaccess.hbase.persistence.entity.HBaseEntity;
import org.trpr.dataaccess.hbase.serializer.DateSerializer;
import org.trpr.dataaccess.hbase.serializer.IntegerSerializer;
import org.trpr.dataaccess.hbase.serializer.LongSerializer;
import org.trpr.dataaccess.hbase.serializer.StringSerializer;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.impl.persistence.AbstractPersistenceHandler;
import org.trpr.platform.core.impl.persistence.sharding.ShardedEntityContextHolder;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.core.spi.persistence.Serializer;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.trpr.platform.runtime.spi.config.ConfigurationException;

/**
 * This implementation of <code>PersistenceHandler</code> that can be used for
 * persisting and retrieving entities of type <code>PersistentEntity</code> from
 * HBase.
 * 
 * For example on how to use HBase persistence API, refer to test sample,
 * HBasePersistenceSample.java and related files.
 * 
 * @author Srikanth, Aditya Karanth A
 * @author Regunath B
 * 
 * 
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Performance-Metrics,name=HBaseMetrics-", description = "HBase Performance Metrics Logger")
public class HBaseHandler extends AbstractPersistenceHandler implements InitializingBean {

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(HBaseHandler.class);

	/** The default HTablePool size */
	private static final int HTABLE_POOL_SIZE = 10;

	/** The HBaseHandlerDelegate instance */
	private HBaseHandlerDelegate hbaseHandlerDelegate;

	/** Map containing HBase access configurations keyed by shard hints */
	private Map<String, HBaseConfiguration> targetHbaseConfigurations = new HashMap<String, HBaseConfiguration>();

	/** Map containing HTablePool instances keyed by shard hints */
	private Map<String, HTablePool> targetHbaseTablePools = new HashMap<String, HTablePool>();

	/** The HBase configuration to use as default */
	private Configuration hbaseConfiguration;

	/** The HTablePool for the default configuration */
	private HTablePool hbaseTablePool;

	/** The HTable pool size */
	private int htablePoolSize = HTABLE_POOL_SIZE;

	private Boolean useWAL = true;

	private Boolean useAutoFlush = true;

	/** The HBase mapping container instance */
	private HBaseMappingContainer hbaseMappingContainer;

	/** Map for type to serializer mappings*/
	private Map<String, Serializer> classNameToSerializerMap = new HashMap<String, Serializer>();
	
	/** Authentication provider, if any*/
	private AuthenticationProvider authProvider;
	
	/**The connection validity check interval in minutes*/
	private Integer callConnectionValidityCheckMinutes; 
	
	public HBaseHandler() {
		// Default serializers. It can be overridden by setting new values in
		// Spring bean definition
		classNameToSerializerMap.put("java.lang.String", new StringSerializer());
		classNameToSerializerMap.put("java.lang.Long", new LongSerializer());
		classNameToSerializerMap.put("java.lang.Integer", new IntegerSerializer());
		classNameToSerializerMap.put("java.util.Date", new DateSerializer());		
	}

	/**
	 * Initializing bean method implementation. Checks to see if at least one of
	 * targetHbaseConfigurations or hbaseConfiguration is defined.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.targetHbaseConfigurations.size() == 0 && this.hbaseConfiguration == null) {
			throw new IllegalArgumentException("targetHbaseConfigurations (or) hbaseConfiguration is required");
		}
		// initialize the HTablePool(s) for the HBase configurations
		if (this.hbaseConfiguration != null) {
			this.hbaseTablePool = new HTablePool(this.hbaseConfiguration, this.htablePoolSize, this.getCallConnectionValidityCheckMinutes());
			// check if an authentication provider has been set and pass it on to the HTablePool
			if (this.getAuthProvider() != null) {
				this.hbaseTablePool.setAuthenticationProvider(this.getAuthProvider());
			}
			this.targetHbaseTablePools.put(ShardedEntity.DEFAULT_SHARD,this.hbaseTablePool);
		}
		for (String shard : this.targetHbaseConfigurations.keySet()) {
			this.targetHbaseTablePools.put(shard, new HTablePool(this.targetHbaseConfigurations.get(shard), this.htablePoolSize));
		}
		// initialize the delegate with defaults, if not set already
		if (this.hbaseHandlerDelegate == null) {
			this.hbaseHandlerDelegate = new HBaseHandlerDelegate(this.hbaseMappingContainer);
		} else {
			// probably the serializers have been customized, so set only the mapping container
			this.hbaseHandlerDelegate.setHBaseMappingContainer(this.hbaseMappingContainer);
		}
		// now warm up the HBaseTablePool for all configured HBaseEntity instances
		for (HbaseMapping mapping : this.hbaseMappingContainer.getMappingForAllClasses()) {
			LOGGER.info("Warming up HTable pool for : " + mapping.getHbaseClass().getTable());
			// warm up the default pool
			HTableInterface table = (HTableInterface)this.hbaseTablePool.getTable(mapping.getHbaseClass().getTable());
			table.setAutoFlush(useAutoFlush);
			table.close();
			// warm up the sharded pools
			for (HTablePool shardedPool : this.targetHbaseTablePools.values()) {
				HTableInterface shardTable = (HTableInterface)shardedPool.getTable(mapping.getHbaseClass().getTable());
				shardTable.setAutoFlush(useAutoFlush);
				shardTable.close();				
			}
		}
		this.hbaseHandlerDelegate.setUseAutoFlush(useAutoFlush);
		this.hbaseHandlerDelegate.setUseWAL(useWAL);

	}

	public HbaseMapping getMappingForClass(String className) {
		return hbaseMappingContainer.getMappingForClass(className);
	}

	public void setHbaseConfigProps(Properties props) {
		hbaseConfiguration = HBaseConfiguration.create();
		for (Object key : props.keySet()) {
			hbaseConfiguration.set(key.toString(), props.getProperty(key.toString()));
		}
	}

	public void setHbaseMappings(List<String> mappingFileNameList) throws ConfigurationException {
		hbaseMappingContainer = new HBaseMappingContainer();
		hbaseMappingContainer.init(mappingFileNameList);
	}

	public void setClassNameToSerializerMap(Map<String, Serializer> classNameToSerializerMap) {
		// Do a putAll to avoid loosing the default serializer mappings
		this.classNameToSerializerMap.putAll(classNameToSerializerMap);
	}

	public void setHbaseTablePool(HTablePool hbaseTablePool) {
		this.hbaseTablePool = hbaseTablePool;
	}

	/**
	 * Returns the HBaseTable pool to use for persistence of the specified HBase
	 * entity
	 * 
	 * @param entity
	 *            the HBaseEntity for persistence
	 * @return the HBase table pool to use
	 */
	protected HTablePool getHbaseTablePool(HBaseEntity entity) {
		HTablePool tablePool = this.hbaseTablePool;
		// check to see if the entity is a ShardedEntity i.e. has been
		// identified and set in the ShardedEntityContextHolder by the
		// PersistenceManager Note that the passed in HBaseEntity is not used to
		// determine the shard hint. The one set in the
		// ShardedEntityContextHolder SHOULD be the same.
		ShardedEntity shardedEntity = ShardedEntityContextHolder.getShardedEntity();
		if (shardedEntity != null) {
			if (!shardedEntity.getShardHint().equals(entity.getShardHint())) {
				// ideally this should not happen at all
				LOGGER.error("The sharded entity in context does not match the passed in value. Context entity shard is : [" + shardedEntity.getShardHint() + "], passed-in entity shard hint is [" + entity.getShardHint() + "]");
				throw new IllegalStateException("The sharded entity in context does not match the passed in value. Context entity shard is : [" + shardedEntity.getShardHint() + "], passed-in entity shard hint is [" + entity.getShardHint() + "]");
			}
			if (this.targetHbaseTablePools.containsKey(shardedEntity.getShardHint())) {
				tablePool = this.targetHbaseTablePools.get(shardedEntity.getShardHint());
			} else {
				tablePool = this.targetHbaseTablePools.get(ShardedEntity.DEFAULT_SHARD);
			}
			if (tablePool == null) {
				// throw an error if even the default shard configuration is not
				// defined
				LOGGER.error("Cannot determine target HBase Configuration for lookup key [" + shardedEntity.getShardHint() + "]");
				throw new IllegalStateException("Cannot determine target HBase Configuration for lookup key [" + shardedEntity.getShardHint() + "]");
			}
		}
		return tablePool;
	}

	@Override
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		PersistentEntity persistentEntity = this.hbaseHandlerDelegate.makePersistent((HBaseEntity) entity, getHbaseTablePool((HBaseEntity) entity));
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HBaseHandler.makePersistent", entity.toString());		
		return persistentEntity;
	}

	@Override
	public void makeTransient(PersistentEntity entity) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		this.hbaseHandlerDelegate.makeTransient((HBaseEntity) entity, getHbaseTablePool((HBaseEntity) entity));
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HBaseHandler.makeTransient", entity.toString());		
	}

	@Override
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		PersistentEntity persistentEntity = this.hbaseHandlerDelegate.findEntity(getHbaseTablePool((HBaseEntity) entity), (HBaseEntity) entity, getMappingForClass(entity.getClass().getName()));
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HBaseHandler.findEntity", entity.toString());		
		return persistentEntity;
	}

	@Override
	public Collection<PersistentEntity> findEntities(Criteria criteria) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		try {
			Collection<PersistentEntity> persistentEntities = this.hbaseHandlerDelegate.findEntities(getHbaseTablePool((HBaseEntity)criteria.getManagedClass().newInstance()), (HBaseCriteria) criteria, getMappingForClass(criteria.getManagedClass().getName()));
			return persistentEntities;
		} catch (Exception e) {
			LOGGER.error("Error while reading data :: ", e);
			throw new PersistenceException("Error while reading data :: ", e);
		} finally {
			// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
			this.performanceMetricsLogger.logPerformanceMetrics("HBaseHandler.findEntities", criteria.toConciseString());		
		}
	}

	/** Getter/Setter methods */
	public Configuration getHbaseConfiguration() {
		return this.hbaseConfiguration;
	}
	public void setHbaseConfiguration(HBaseConfiguration hbaseConfiguration) {
		this.hbaseConfiguration = hbaseConfiguration;
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
	public void setHtablePoolSize(int htablePoolSize) {
		this.htablePoolSize = htablePoolSize;
	}
	public int getHtablePoolSize() {
		return this.htablePoolSize;
	}
	public AuthenticationProvider getAuthProvider() {
		return this.authProvider;
	}
	public void setAuthProvider(AuthenticationProvider authProvider) {
		this.authProvider = authProvider;
	}
	public void setCallConnectionValidityCheckMinutes(Integer callConnectionValidityCheckMinutes) {
		this.callConnectionValidityCheckMinutes = callConnectionValidityCheckMinutes;
	}
	public Integer getCallConnectionValidityCheckMinutes() {
		return this.callConnectionValidityCheckMinutes;
	}	
	// //////////// UNSUPPORTED operations ////////////////

	@Override
	public int update(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Operation not supported!!!!!");
	}

	@Override
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Operation not supported!!!!!");
	}

}
