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
package org.trpr.example.batch.hbase.test;

import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.trpr.dataaccess.hbase.persistence.HBaseCriteria;
import org.trpr.example.batch.hbase.test.entity.HBaseEarthling;
import org.trpr.platform.core.spi.persistence.PersistenceManager;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.integration.impl.json.JSONTranscoderImpl;

/**
 * Tets batch step which persists data into HBase using the HBaseProvider
 * 
 * @shashikant soni
 */
public class TestHBaseOps implements Tasklet {

	private static final Log LOG = LogFactory.getLog(TestHBaseOps.class);

	private PersistenceManager persistenceManager;
	
	/**
	 * if this flag is set true in the context. the sample record that
	 * is created would be deleted at the end of test run.
	 */
	private boolean delete = false;
	

	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {

		HBaseEarthling testEntity = new HBaseEarthling();
		testEntity.setName("Jone Doe");
		String uid = UUID.randomUUID().toString();
		testEntity.setUid(uid);
		testEntity.setLongValue(new Random().nextLong());
		testEntity.setIntValue(new Random().nextInt(10000));
		testEntity.setDateValue(new Date());
		testEntity.setByteArrayValue(uid.getBytes());

		
		write(testEntity);
		update(testEntity);
		read(testEntity);
		if (delete) {
			delete(testEntity);
		}
		scan();

		return RepeatStatus.FINISHED;
	}

	private void write(HBaseEarthling e) {
		getPersistenceManager().makePersistent(e);
		LOG.info("Persisted record in hbase with UID :: " + e.getUid());
	}

	@SuppressWarnings("unchecked")
	private void read(HBaseEarthling e) {
		PersistentEntity result =  getPersistenceManager().findEntity(e);
		if(result != null) {
			LOG.info("got record with uid from HBase :: " +  new JSONTranscoderImpl().marshal(result));
		} else {
			LOG.info("NOT FOUND record with uid from HBase :: " + e.getUid());
		}
		
	}

	private void update(HBaseEarthling e) {
		e.setName("Updated John Foo 1");
		getPersistenceManager().makePersistent(e);
		LOG.info("Updated record in hbase with UID :: " + e.getUid());
	}
	
	private void delete(HBaseEarthling e) {
		getPersistenceManager().makeTransient(e);
		LOG.info("got record with uid from HBase :: " + e.getUid());
	}

	private void scan() {
		HBaseCriteria criteria = new HBaseCriteria();
		criteria.setManagedClass(HBaseEarthling.class);
		criteria.setMaxResults(100);
		Collection<PersistentEntity> entities = getPersistenceManager().findEntities(criteria);
		LOG.info("******   Scan output for no. of entities : " + entities.size());
		for (PersistentEntity entity : entities) {
			try {
				LOG.info(entity.toString());
			} catch (Exception e) {
				// to catch bad data related records and continue
				LOG.warn("Unable to retrieve records with ID : " + ((HBaseEarthling)entity).getUid());
			}
		}
		LOG.info("******   Scan complete for no. of entities : " + entities.size());
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
