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
package org.trpr.dataaccess.hbase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.RowLock;
import org.apache.hadoop.hbase.client.RowMutations;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.CoprocessorProtocol;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.PoolMap;
import org.apache.hadoop.hbase.util.PoolMap.PoolType;
import org.trpr.dataaccess.hbase.auth.AuthenticationProvider;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;


/**
 * The <code>HTablePool</code> is a simple pool of HTable instances. This code is based on the {@link org.apache.hadoop.hbase.client.HTablePool} implementation
 * and is therefore a drop-in replacement for existing users (version hbase-0.94.2-cdh4.2.0). Refer to the original implementation's class summary for expected 
 * behavior of methods - only deviations from original behavior is explicitly documented in this class.
 * 
 * This implementation has the following additional capabilities:
 * <pre><ul>
 * <li>Cycles through all existing connections in the pool using a background thread and keeps them active by executing a 'ping' query</li>
 * <li>Authenticates using the specified {@link AuthenticationProvider}, if one exists, when new connections are created</li>
 * </ul><pre>
 * @author Regunath B
 * @version 1.0, 08 Jan 2014
 * 
 * @see org.apache.hadoop.hbase.client.HTablePool
 */
public class HTablePool implements Closeable {
	
	/** The Log instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(HTablePool.class);
	
	/** Protected non-final member variables providing access to modify implementation by sub-types, if needed*/
	protected PoolMap<String, HTableInterface> tables;
	protected int maxSize;
	protected PoolType poolType;
	protected Configuration config;
	protected HTableInterfaceFactory tableFactory;
	
	/**The connection validity check interval in minutes*/
	protected Integer callConnectionValidityCheckMinutes; 
	
	/** The AuthenticationProvider to use prior to creating connections*/
	protected AuthenticationProvider authenticationProvider;

	/** Constructors from the original implementation*/
	public HTablePool() {
	    this(HBaseConfiguration.create(), Integer.MAX_VALUE);
	}
	public HTablePool(final Configuration config, final int maxSize) {
		this(config, maxSize, null, null, null);
	}
	public HTablePool(final Configuration config, final int maxSize, Integer callConnectionValidityCheckMinutes) {
		this(config, maxSize, null, null, callConnectionValidityCheckMinutes);
	}
	public HTablePool(final Configuration config, final int maxSize,final HTableInterfaceFactory tableFactory) {
		this(config, maxSize, tableFactory, PoolType.Reusable, null);
	}
	public HTablePool(final Configuration config, final int maxSize,final PoolType poolType) {
		this(config, maxSize, null, poolType, null);
	}
	public HTablePool(final Configuration config, final int maxSize,final HTableInterfaceFactory tableFactory, 
			PoolType poolType,  Integer callConnectionValidityCheckMinutes) {
		this.config = config == null ? HBaseConfiguration.create() : config;
		this.maxSize = maxSize;
		this.tableFactory = tableFactory == null ? new HTableFactory() : tableFactory;
		this.callConnectionValidityCheckMinutes = callConnectionValidityCheckMinutes;
		if (poolType == null) {
			this.poolType = PoolType.Reusable;
		} else {
			switch (poolType) {
			case Reusable:
			case ThreadLocal:
				this.poolType = poolType;
				break;
			default:
				this.poolType = PoolType.Reusable;
				break;
			}
		}
		this.tables = new PoolMap<String, HTableInterface>(this.poolType,this.maxSize);
		// create and start the connection pool validator thread if connection validity check is turned on i.e. is not null
		if (this.getCallConnectionValidityCheckMinutes() != null) {
			new ConnectionValidator(this);
		}
	}

	/** Methods copied from original implementation, private methods are made proptected to permit overriding by sub-types*/
	public HTableInterface getTable(String tableName) {
		HTableInterface table = findOrCreateTable(tableName);
		return new PooledHTable(table);
	}	
	public HTableInterface getTable(byte[] tableName) {
		return getTable(Bytes.toString(tableName));
	}
	public void putTable(HTableInterface table) throws IOException {
		if (table instanceof PooledHTable) {
			returnTable(((PooledHTable) table).getWrappedTable());
		} else {
			throw new IllegalArgumentException("not a pooled table: " + table);
		}
	}
	public void closeTablePool(final String tableName) throws IOException {
		Collection<HTableInterface> tables = this.tables.values(tableName);
		if (tables != null) {
			for (HTableInterface table : tables) {
				this.tableFactory.releaseHTableInterface(table);
			}
		}
		this.tables.remove(tableName);
	}
	public void closeTablePool(final byte[] tableName) throws IOException {
		closeTablePool(Bytes.toString(tableName));
	}
	public void close() throws IOException {
		for (String tableName : tables.keySet()) {
			closeTablePool(tableName);
		}
		this.tables.clear();
	}
	int getCurrentPoolSize(String tableName) {
		return tables.size(tableName);
	}	
	protected HTableInterface findOrCreateTable(String tableName) {
		HTableInterface table = tables.get(tableName);
		if (table == null) {
			table = createHTable(tableName);
		}
		return table;
	}
	protected void returnTable(HTableInterface table) throws IOException {
		String tableName = Bytes.toString(table.getTableName());
		if (tables.size(tableName) >= maxSize) {
			this.tables.remove(tableName, table);
			this.tableFactory.releaseHTableInterface(table);
			return;
		}
		tables.put(tableName, table);
	}
	/**
	 * Creates a new HTableInterface using the factory for the specified table name.
	 * Authenticates the principal if an {@link AuthenticationProvider} has been specified
	 * @param tableName the table name for which to create a new HTableInterface
	 * @return the newly created HTableInterface instance
	 */
	protected HTableInterface createHTable(String tableName) {
		if (this.getAuthenticationProvider() != null) {
			this.getAuthenticationProvider().authenticatePrincipal(this.config);
		}		
		return this.tableFactory.createHTableInterface(config,Bytes.toBytes(tableName));
	}
	
	/** Getter/Setter methods*/
	public Integer getCallConnectionValidityCheckMinutes() {
		return this.callConnectionValidityCheckMinutes;
	}
	public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}
	public AuthenticationProvider getAuthenticationProvider() {
		return this.authenticationProvider;
	}
	
	/**
	 * Connection validator thread. Checks for connection validity periodically with period specified by the {@link HTablePool}. Closes broken
	 * or otherwise invalid connections.
	 */
	class ConnectionValidator extends Thread {
		/** The pool to validate connections for*/
		HTablePool tablePool;
		ConnectionValidator(HTablePool tablePool) {
			this.tablePool = tablePool;
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
			setName("HTablePoolValidator_" + this.tablePool.hashCode());
			start();			
		}
		public void run() {
			while(true) {
				try {
					Thread.sleep((long)(this.tablePool.getCallConnectionValidityCheckMinutes() * 60000));
					long start = System.currentTimeMillis();
					LOGGER.debug("Starting to validate connections for tablepool with size : " + this.tablePool.tables.keySet().size());
					for (String tableName : this.tablePool.tables.keySet()) {
						HTableInterface table = this.tablePool.tables.get(tableName); // pick up the first available table from the pool. Over time all connections will get picked up and validated
						try {
							// Validate the table/connection by calling a Get with row key as System.currentTimeMillis(). This call will exercise the 
							// network components of the table's connection
							if (table != null) { // we can get null if all connections have already been borrowed i.e. are being used
								table.exists(new Get(Bytes.toBytes( System.currentTimeMillis())));
								this.tablePool.returnTable(table); // return the table back to the pool
							}
						} catch (Exception ex) {
							LOGGER.info("Error validating connection for : " + tableName + " : " + ex.getMessage() + " . Invalidating all connections for this table.");
							this.tablePool.closeTablePool(tableName);
							LOGGER.info("Recreating a single connection for : " + tableName);
							table = this.tablePool.createHTable(tableName);
							table.close(); // calling close will simply return the valid connection to the pool
						}
					}
					LOGGER.debug("Completed validating connections. Connections size : " + tables.keySet().size() + 
							". Completed in : " + (System.currentTimeMillis() - start) + " ms");
				} catch (Exception e) {
					LOGGER.error("Error validating pool connections : " + e.getMessage() + ". Will retry in : " + 
							this.tablePool.getCallConnectionValidityCheckMinutes() + " minutes.", e);
				}
			}
		}
	}

	/** Pooled HTable class copied from original implementation*/
	class PooledHTable implements HTableInterface {

		private HTableInterface table;

		public PooledHTable(HTableInterface table) {
			this.table = table;
		}
		public byte[] getTableName() {
			return table.getTableName();
		}
		public Configuration getConfiguration() {
			return table.getConfiguration();
		}
		public HTableDescriptor getTableDescriptor() throws IOException {
			return table.getTableDescriptor();
		}
		public boolean exists(Get get) throws IOException {
			return table.exists(get);
		}
		public void batch(List<? extends Row> actions, Object[] results)throws IOException, InterruptedException {
			table.batch(actions, results);
		}
		public Object[] batch(List<? extends Row> actions) throws IOException,InterruptedException {
			return table.batch(actions);
		}
		public Result get(Get get) throws IOException {
			return table.get(get);
		}
		public Result[] get(List<Get> gets) throws IOException {
			return table.get(gets);
		}
		@SuppressWarnings("deprecation")
		public Result getRowOrBefore(byte[] row, byte[] family)
				throws IOException {
			return table.getRowOrBefore(row, family);
		}
		public ResultScanner getScanner(Scan scan) throws IOException {
			return table.getScanner(scan);
		}
		public ResultScanner getScanner(byte[] family) throws IOException {
			return table.getScanner(family);
		}
		public ResultScanner getScanner(byte[] family, byte[] qualifier)throws IOException {
			return table.getScanner(family, qualifier);
		}
		public void put(Put put) throws IOException {
			table.put(put);
		}
		public void put(List<Put> puts) throws IOException {
			table.put(puts);
		}
		public boolean checkAndPut(byte[] row, byte[] family, byte[] qualifier,byte[] value, Put put) throws IOException {
			return table.checkAndPut(row, family, qualifier, value, put);
		}
		public void delete(Delete delete) throws IOException {
			table.delete(delete);
		}
		public void delete(List<Delete> deletes) throws IOException {
			table.delete(deletes);
		}
		public boolean checkAndDelete(byte[] row, byte[] family,byte[] qualifier, byte[] value, Delete delete) throws IOException {
			return table.checkAndDelete(row, family, qualifier, value, delete);
		}
		public Result increment(Increment increment) throws IOException {
			return table.increment(increment);
		}
		public long incrementColumnValue(byte[] row, byte[] family,byte[] qualifier, long amount) throws IOException {
			return table.incrementColumnValue(row, family, qualifier, amount);
		}
		public long incrementColumnValue(byte[] row, byte[] family,byte[] qualifier, long amount, boolean writeToWAL)throws IOException {
			return table.incrementColumnValue(row, family, qualifier, amount,writeToWAL);
		}
		public boolean isAutoFlush() {
			return table.isAutoFlush();
		}
		public void flushCommits() throws IOException {
			table.flushCommits();
		}
		public void close() throws IOException {
			returnTable(table);
		}
		public RowLock lockRow(byte[] row) throws IOException {
			return table.lockRow(row);
		}
		public void unlockRow(RowLock rl) throws IOException {
			table.unlockRow(rl);
		}
		public <T extends CoprocessorProtocol> T coprocessorProxy(Class<T> protocol, byte[] row) {
			return table.coprocessorProxy(protocol, row);
		}
		public <T extends CoprocessorProtocol, R> Map<byte[], R> coprocessorExec(Class<T> protocol, byte[] startKey, byte[] endKey,
				Batch.Call<T, R> callable) throws IOException, Throwable {
			return table.coprocessorExec(protocol, startKey, endKey, callable);
		}
		public <T extends CoprocessorProtocol, R> void coprocessorExec(Class<T> protocol, byte[] startKey, byte[] endKey,
				Batch.Call<T, R> callable, Batch.Callback<R> callback)throws IOException, Throwable {
			table.coprocessorExec(protocol, startKey, endKey, callable,callback);
		}
		public String toString() {
			return "PooledHTable{" + ", table=" + table + '}';
		}
		HTableInterface getWrappedTable() {
			return table;
		}
		public void mutateRow(RowMutations rm) throws IOException {
			table.mutateRow(rm);
		}
		public Result append(Append append) throws IOException {
			return table.append(append);
		}
		public void setAutoFlush(boolean autoFlush) {
			table.setAutoFlush(autoFlush);
		}
		public void setAutoFlush(boolean autoFlush, boolean clearBufferOnFail) {
			table.setAutoFlush(autoFlush, clearBufferOnFail);
		}
		public long getWriteBufferSize() {
			return table.getWriteBufferSize();
		}
		public void setWriteBufferSize(long writeBufferSize) throws IOException {
			table.setWriteBufferSize(writeBufferSize);
		}
	}	
	
}
