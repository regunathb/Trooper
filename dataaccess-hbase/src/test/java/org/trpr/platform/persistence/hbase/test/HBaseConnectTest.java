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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Test class to verify connectivity and other basic stuff with HBase. In case
 * there are issues while running the complete handler code this class can be
 * used to verify the basic setup and connectivity with the configured HBase.
 * 
 * Before running this sample, crate a HBase table, mytable, in your HBase
 * installation. Here are the steps: <tt>
 * > $HBASE_HOME/bin/hbase shell 
 * > create "mytable", "demodata" 
 * > exit
 * </tt>
 * 
 * @shashikant soni
 */
public class HBaseConnectTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Configuration hbaseConfiguration = HBaseConfiguration.create();
		System.out.println("starting");
//		hbaseConfiguration.set("hbase.zookeeper.quorum", "localhost");
		// hbaseConfiguration.set("hbase.client.write.buffer", "2097152");

		HTable testTable = new HTable(hbaseConfiguration, "mytable");
		System.out.println("connected table");
		for (int i = 0; i < 100; i++) {
			byte[] family = Bytes.toBytes("demodata");
			byte[] qual = Bytes.toBytes("aField");

			Put put = new Put();
			for (int j = 0; j < 20; j++) {
				byte[] value = Bytes.toBytes("test" + j);
				put.add(family, qual, value);
				testTable.put(put);
			}

			Scan scan = new Scan();
			scan.addColumn(family, qual);
			ResultScanner rs = testTable.getScanner(scan);
			for (Result r = rs.next(); r != null; r = rs.next()) {
				byte[] valueObj = r.getValue(family, qual);
				String value = new String(valueObj);
				System.out.println(value);
			}
		}
		System.out.println("*********** END ***********");
	}

}
