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
package org.trpr.platform.persistence.hbase.test;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.trpr.dataaccess.hbase.persistence.HBaseCriteria;
import org.trpr.dataaccess.hbase.persistence.HBaseProvider;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * Test class for HBase operations
 * 
 * Before running this sample, crate a HBase table, mytable, in your HBase installation.
 * Here are the steps:
 * 	  > $HBASE_HOME/bin/hbase shell
 *    > create "mytable", "demodata"
 *    > exit
 * 
 * @author Shashikant Soni
 * 
 */
public class HBasePersistenceSample {

	public static void main(String[] args) throws PersistenceException {
		
		System.out.println(" **** HBase Persistence Testing ******");
		
		ApplicationContext ac = new ClassPathXmlApplicationContext("hbasesample/spring-config.xml");
		HBaseProvider provider = (HBaseProvider) ac.getBean("hbaseProvider");

		// Add a new entity
		System.out.println("Creating a new entity");
		MyEntity entityForAdd = constructSampleEntity();
		provider.makePersistent(entityForAdd);

		// Read all entries
		System.out.println("Reading All The Entries from Table:");
		PersistentEntity entityForSearch = new MyEntity(new HBaseCriteria());
		ArrayList<PersistentEntity> entityList = (ArrayList<PersistentEntity>) provider.findEntity(entityForSearch);
		System.out.println("Found " + entityList.size() + " entities in the table.");
		for (PersistentEntity entity : entityList) {
			MyEntity e = (MyEntity) entity;
			System.out.println("\t" + entity);
		}
		
		// Delete the newly added entry
		MyEntity entityForDelete = new MyEntity();
		entityForDelete.setUid(entityForAdd.getUid());
		provider.makeTransient(entityForAdd);
		
		System.out.println("Done");
	}

	private static MyEntity constructSampleEntity() {
		MyEntity myEntity = new MyEntity();
		myEntity.setUid("999912312312");
		myEntity.setName("John Doe");
		myEntity.setIntValue(7055);
		myEntity.setLongValue(1007055L);
		myEntity.setDateValue(new Date());
		myEntity.setByteArrayValue("Hello world!".getBytes());
		
		return myEntity;
	}

	private static PersistentEntity getEntityForSearch() {
		MyEntity entity = new MyEntity(getCriteria());
		return entity;
	}

	private static HBaseCriteria getCriteria() {
		HBaseCriteria criteria = new HBaseCriteria();
		// criteria.setTimeRangeStart(1287573491960L);
		// criteria.setDateRange(1287573399785L, 1287561691023L);
		return criteria;
	}


}
