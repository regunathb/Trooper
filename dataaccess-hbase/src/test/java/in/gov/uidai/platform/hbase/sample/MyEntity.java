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
package in.gov.uidai.platform.hbase.sample;

import java.util.Date;

import org.trpr.platform.core.impl.persistence.AbstractPersistentEntity;
import org.trpr.platform.core.spi.persistence.Criteria;

/**
 * A sample test class to persisnt to HBase for testing
 * 
 * @author Shashikant Soni
 * 
 */
public class MyEntity extends AbstractPersistentEntity {

	private String uid;
	private String name;
	private Long longValue;
	private Integer intValue;
	private byte[] byteArrayValue;
	private Date dateValue;
	
	/*
	 * No argument constructor
	 */
	public MyEntity() {
	}
	
	public MyEntity(Criteria c){
		super(c);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getNameAttr() {
		return "name";
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public byte[] getByteArrayValue() {
		return byteArrayValue;
	}

	public void setByteArrayValue(byte[] byteArrayValue) {
		this.byteArrayValue = byteArrayValue;
	}

	public Date getDateValue() {
		return dateValue;
	}
	
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("MyEntity: ");
		sb.append(" UID='");
		sb.append(this.getUid());
		sb.append("', Name='");
		sb.append(this.getName());
		sb.append("', dateValue = '");
		sb.append(this.dateValue);
		sb.append("', intValue = '");
		sb.append(this.intValue);
		sb.append("', longValue = '");
		sb.append(this.longValue);
		sb.append("', byteArrayValue (as string) = '");
		sb.append(new String(this.byteArrayValue));
		return sb.toString();
	}
}
