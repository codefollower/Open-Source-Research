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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//看此类时要时刻记住一点: startTimestamp是递增的，构造Uncommited实例时也从一个时间戳开始，
//后面commit(long id)时，id也是个时间戳，所以increaseFirstUncommitedBucket()和raiseLargestDeletedTransaction里的代码
//能够按序循环修改firstUncommitedBucket和firstUncommitedAbsolute，这两个变量一开始就在构造函数中赋值，依据一个开始时间戳。
public class Uncommited {
    private static final Logger LOG = LoggerFactory.getLogger(TSOHandler.class);

    private static final int BKT_NUMBER = 1 << 10; // 2 ^ 10 是1024

    //一个Bucket能表示32768个事务id，而这里又有1024个Bucket
    //所以可表示32768 * 1024个事务id
    private Bucket buckets[] = new Bucket[BKT_NUMBER];
    private int firstUncommitedBucket = 0;
    private long firstUncommitedAbsolute = 0;
    private int lastOpenedBucket = 0;

    public Uncommited(long startTimestamp) {
        //用id除以Bucket.getBucketSize()得到倍数，然后用倍数与BKT_NUMBER取模，看这个id放在buckets数组的哪个下标
        lastOpenedBucket = firstUncommitedBucket = getRelativePosition(startTimestamp);
        //不取模
        firstUncommitedAbsolute = getAbsolutePosition(startTimestamp);
        long ts = startTimestamp & ~(Bucket.getBucketSize() - 1); //实际上就是startTimestamp % Bucket.getBucketSize()
        LOG.debug("Start TS : " + startTimestamp + " firstUncom: " + firstUncommitedBucket + " Mask:" + ts);
        LOG.debug("BKT_NUMBER : " + BKT_NUMBER + " BKT_SIZE: " + Bucket.getBucketSize());
        //因为startTimestamp - ts <= Bucket.getBucketSize()
        //所以它想从startTimestamp - startTimestamp % Bucket.getBucketSize()的位置开始到startTimestamp都设成commit
        for (; ts <= startTimestamp; ++ts)
            commit(ts);
    }

    public synchronized void commit(long id) {
        int position = getRelativePosition(id);
        Bucket bucket = buckets[position];
        if (bucket == null) {
            bucket = new Bucket(getAbsolutePosition(id)); //BucketSize的倍数
            buckets[position] = bucket;
            lastOpenedBucket = position;
        }
        bucket.commit(id);
        if (bucket.allCommited()) {
            //因为不会查commit的，只会查Uncommited的，
            //所以在isUncommited(long id)方法里如果buckets[position]为null就当成是Uncommited的
            buckets[position] = null;
            increaseFirstUncommitedBucket();
        }
    }

    public void abort(long id) {
        commit(id);
    }

    public boolean isUncommited(long id) {
        Bucket bucket = buckets[getRelativePosition(id)];
        if (bucket == null) {
            return false;
        }
        return bucket.isUncommited(id);
    }

    public Set<Long> raiseLargestDeletedTransaction(long id) {
        if (firstUncommitedAbsolute > getAbsolutePosition(id))
            return Collections.emptySet();
        int maxBucket = getRelativePosition(id);
        Set<Long> aborted = new TreeSet<Long>();
        for (int i = firstUncommitedBucket; i != maxBucket; i = (i + 1) % BKT_NUMBER) {
            Bucket bucket = buckets[i];
            if (bucket != null) {
                aborted.addAll(bucket.abortAllUncommited());
                buckets[i] = null;
            }
        }

        Bucket bucket = buckets[maxBucket];
        if (bucket != null) {
            aborted.addAll(bucket.abortUncommited(id));
        }

        increaseFirstUncommitedBucket();

        return aborted;
    }

    public synchronized long getFirstUncommitted() {
        return buckets[firstUncommitedBucket].getFirstUncommitted();
    }

    //求下一个空闲桶
    private synchronized void increaseFirstUncommitedBucket() {
        while (firstUncommitedBucket != lastOpenedBucket && buckets[firstUncommitedBucket] == null) {
            firstUncommitedBucket = (firstUncommitedBucket + 1) % BKT_NUMBER;
            firstUncommitedAbsolute++;
        }
    }

    private int getRelativePosition(long id) {
        //用id除以Bucket.getBucketSize()得到倍数，然后用倍数与BKT_NUMBER取模，看这个id放在buckets数组的哪个下标
        return ((int) (id / Bucket.getBucketSize())) % BKT_NUMBER;
    }

    private int getAbsolutePosition(long id) { //不取模
        return (int) (id / Bucket.getBucketSize());
    }
}
