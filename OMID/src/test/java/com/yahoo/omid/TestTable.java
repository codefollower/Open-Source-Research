/**
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */

package com.yahoo.omid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import com.yahoo.omid.client.TransactionManager;
import com.yahoo.omid.client.TransactionState;
import com.yahoo.omid.client.TransactionalTable;

public class TestTable {
    private static final Log LOG = LogFactory.getLog(TestTable.class);

    private static final String TEST_TABLE = "usertable";
    private static final String TEST_TABLE2 = "usertable2";
    private static final String TEST_FAMILY = "Teste";

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        //        admin.disableTable(TEST_TABLE);
        //        admin.disableTable(TEST_TABLE2);
        //        admin.deleteTable(TEST_TABLE);
        //        admin.deleteTable(TEST_TABLE2);
        if (!admin.tableExists(TEST_TABLE)) {
            HTableDescriptor desc = new HTableDescriptor(TEST_TABLE);
            HColumnDescriptor column = new HColumnDescriptor(TEST_FAMILY);
            desc.addFamily(column);

            //         HColumnDescriptor delfam = new HColumnDescriptor(TransactionalTable.DELETE_STATUS_FAMILY);
            //         delfam.setMaxVersions(10);
            //         desc.addFamily(delfam);
            //admin.createTable(desc);
            admin.createTable(desc, new byte[][]{Bytes.toBytes("3000")});
            LOG.info("created table");
        }

        if (!admin.tableExists(TEST_TABLE2)) {
            HTableDescriptor desc = new HTableDescriptor(TEST_TABLE2);
            HColumnDescriptor column = new HColumnDescriptor(TEST_FAMILY);
            desc.addFamily(column);
            //admin.createTable(desc);
            admin.createTable(desc, new byte[][]{Bytes.toBytes("3000")});
            LOG.info("created table");
        }

        TransactionManager tm = new TransactionManager(conf);
        TransactionalTable tt = new TransactionalTable(conf, TEST_TABLE);

        TransactionalTable tt2 = new TransactionalTable(conf, TEST_TABLE2);

        TransactionState t1 = tm.beginTransaction();

        Put put = new Put(Bytes.toBytes("2002"));
        put.add(Bytes.toBytes(TEST_FAMILY), Bytes.toBytes("c"), Bytes.toBytes("2002"));
        tt.put(t1, put);

        tt2.put(t1, put);

        ResultScanner rs = tt.getScanner(t1, new Scan());
        Result r = rs.next();
        while (r != null) {
            System.out.println(r);
            r = rs.next();
        }
        tm.tryCommit(t1);

        System.out.println();

        t1 = tm.beginTransaction();

        put = new Put(Bytes.toBytes("2003"));
        put.add(Bytes.toBytes(TEST_FAMILY), Bytes.toBytes("c"), Bytes.toBytes("2003"));
        tt.put(t1, put);
        tt2.put(t1, put);
        tm.abort(t1);

        rs = tt.getScanner(t1, new Scan());
        r = rs.next();
        while (r != null) {
            System.out.print(r);
            r = rs.next();
        }

        System.out.println();

        rs = tt2.getScanner(t1, new Scan());
        r = rs.next();
        while (r != null) {
            System.out.print(r);
            r = rs.next();
        }
        
        System.out.println();
        
        
        put = new Put(Bytes.toBytes("2004"));
        put.add(Bytes.toBytes(TEST_FAMILY), Bytes.toBytes("c"), Bytes.toBytes("2004"));
        tt.put(t1, put);
        tt2.put(t1, put);
        put = new Put(Bytes.toBytes("3001"));
        put.add(Bytes.toBytes(TEST_FAMILY), Bytes.toBytes("c"), Bytes.toBytes("3002"));
        tt.put(t1, put);
        tt2.put(t1, put);
        tm.abort(t1);

        rs = tt.getScanner(t1, new Scan());
        r = rs.next();
        while (r != null) {
            System.out.print(r);
            r = rs.next();
        }

        System.out.println();

        rs = tt2.getScanner(t1, new Scan());
        r = rs.next();
        while (r != null) {
            System.out.print(r);
            r = rs.next();
        }
        
        TransactionManager.close(); //我加上的
    }
}