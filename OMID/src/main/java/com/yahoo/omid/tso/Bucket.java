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

package com.yahoo.omid.tso;

import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bucket { //是一个桶，用来记录未提交的事务，一个bit代表一个事务

    private static final Log LOG = LogFactory.getLog(Bucket.class);

    private static final long BUCKET_SIZE = 32768; // 2 ^ 15 //是32K，相当于有32768个桶，下标从0到32767

    private BitSet transactions = new BitSet((int) BUCKET_SIZE);

    private int transactionsCommited = 0;
    private int firstUncommited = 0;
    private boolean closed = false;
    private int position;

    public Bucket(int position) {
        //是BUCKET_SIZE的倍数，在abortUncommited中用来算实际值
        //如position是0,那么(((long) position) * BUCKET_SIZE + i)就是0*BUCKET_SIZE+i也就是i
        //如position是1,那么(((long) position) * BUCKET_SIZE + i)就是1*BUCKET_SIZE+i也就是
        this.position = position;
    }

    //这里是判断未提交用了取反运算，说明BitSet transactions中标记为1的是用来表示已提交的事务
    public boolean isUncommited(long id) {
        return !transactions.get((int) (id % BUCKET_SIZE)); //取模后值总是会落在0到BUCKET_SIZE-1之间
    }

    public Set<Long> abortAllUncommited() {
        Set<Long> result = abortUncommited(BUCKET_SIZE - 1); //相当于从firstUncommited到最后一个
        closed = true;
        return result;
    }

    //从firstUncommited的位置找到lastCommited，把firstUncommited <= i <= lastCommited这个闭区间里头未提交的事务id设成true
    public synchronized Set<Long> abortUncommited(long id) { //id要取模
        int lastCommited = (int) (id % BUCKET_SIZE);

        Set<Long> aborted = new TreeSet<Long>();
        if (allCommited()) {
            return aborted;
        }

        LOG.trace("Performing scanning...");

        //transactions.nextClearBit(firstUncommited)的意思是从firstUncommited的位置开始寻找第一个bit为false的下标
        //如果firstUncommited是0，firstUncommited所在bit是false，那么下标也是0
        for (int i = transactions.nextClearBit(firstUncommited); i >= 0 && i <= lastCommited; i = transactions
                .nextClearBit(i + 1)) { //这里得到的i不一定是按1的间隙递增，比如可能1、2、3、4、5中只有2和5这两个下标中的bit为false

            //position是BUCKET_SIZE的倍数，在abortUncommited中用来算实际值
            //如position是0,那么(((long) position) * BUCKET_SIZE + i)就是0*BUCKET_SIZE+i也就是i
            //如position是1,那么(((long) position) * BUCKET_SIZE + i)就是1*BUCKET_SIZE+i也就是
            aborted.add(((long) position) * BUCKET_SIZE + i);
            commit(i);
        }

        firstUncommited = lastCommited + 1; //指向下一个

        return aborted;
    }

    public synchronized void commit(long id) {
        transactions.set((int) (id % BUCKET_SIZE));
        ++transactionsCommited;
    }

    public boolean allCommited() {
        //所有的位已满了或者已关闭，这里有点小问题，
        //用transactionsCommited>=BUCKET_SIZE更好些，因为重复调用多次commit时transactionsCommited变量会一直增加
        return BUCKET_SIZE == transactionsCommited || closed;
    }

    public static long getBucketSize() {
        return BUCKET_SIZE;
    }

    public long getFirstUncommitted() {
        return position * BUCKET_SIZE + firstUncommited;
    }

}
