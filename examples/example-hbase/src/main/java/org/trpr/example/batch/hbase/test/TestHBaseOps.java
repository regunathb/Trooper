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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.trpr.dataaccess.hbase.persistence.HBaseProvider;
import org.trpr.example.batch.hbase.test.entity.MyEntity;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * Tets batch step which persists data into HBase using the HBaseProvider
 * 
 * @shashikant soni
 */
public class TestHBaseOps implements Tasklet {

	private static final Log LOG = LogFactory.getLog(TestHBaseOps.class);

	private HBaseProvider hbaseProvider;
	
	/**
	 * if this flag is set true in the context. the sample record that
	 * is created would be deleted at the end of run.
	 */
	private boolean delete = false;
	

	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {

		MyEntity testEntity = new MyEntity();
		testEntity.setName("Jone Doe");
		String uid = UUID.randomUUID().toString();
		testEntity.setUid(uid);
		testEntity.setLongValue(new Long(1));
		testEntity.setIntValue(1);
		testEntity.setDateValue(new Date());
		testEntity.setByteArrayValue(uid.getBytes());

		write(testEntity);
		read(testEntity);
		update(testEntity);
		read(testEntity);
		if (delete)
			delete(testEntity);

		return RepeatStatus.FINISHED;
	}

	private void write(MyEntity e) {
		getHbaseProvider().makePersistent(e);
		LOG.info("Persisted record in hbase with UID :: " + e.getUid());
	}

	private void read(MyEntity e) {
		List<PersistentEntity> result = (List<PersistentEntity>) getHbaseProvider().findObject(e);
		
		if(CollectionUtils.isNotEmpty(result)) {
		for (PersistentEntity persistentEntity : result) {
			MyEntity aRec = (MyEntity) persistentEntity;
			LOG.info("got record with uid from HBase :: " + aRec.toString());
		}
		} else {
			LOG.info("NO FOUND record with uid from HBase :: " + e.toString());
		}
		
	}

	private void update(MyEntity e) {
		e.setName("Updated John Foo");
		getHbaseProvider().makePersistent(e);
		LOG.info("Updated record in hbase with UID :: " + e.getUid());
	}
	
	private void delete(MyEntity e) {
		getHbaseProvider().makeTransient(e);
		LOG.info("got record with uid from HBase :: " + e.getUid());
	}

	public HBaseProvider getHbaseProvider() {
		return hbaseProvider;
	}

	public void setHbaseProvider(HBaseProvider hbaseProvider) {
		this.hbaseProvider = hbaseProvider;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
