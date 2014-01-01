package my.test;

import java.util.BitSet;

import com.yahoo.omid.tso.Bucket;
import com.yahoo.omid.tso.Uncommited;

public class BucketTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i <= 12; i++)
            System.out.println(i % 12);

        Bucket b = new Bucket(10);

        for (int i = 0; i <= 12; i++)
            b.commit(i);

        for (int i = 0; i <= 24; i++)
            System.out.println(!b.isUncommited(i));

        BitSet transactions = new BitSet(12);

        for (int i = 0; i <= 10; i++)
            transactions.set(i);

        System.out.println(transactions.nextClearBit(0));

        transactions.clear(0);

        System.out.println(transactions.nextClearBit(0));

        //Uncommited u = 
            new Uncommited(32768 + 100);

        //Uncommited u = new Uncommited(-5);

        long startTimestamp = 2000;

        startTimestamp = Bucket.getBucketSize() - 1;

        startTimestamp = Bucket.getBucketSize();

        startTimestamp = Bucket.getBucketSize() + 100;

        startTimestamp = Bucket.getBucketSize() + 101;

        startTimestamp = Bucket.getBucketSize() * 2 + 101;

        long ts = startTimestamp & ~(Bucket.getBucketSize() - 1);

        long ts2 = startTimestamp % (Bucket.getBucketSize());
        System.out.println(startTimestamp - ts);
        System.out.println(ts2);

        System.out.println(startTimestamp - startTimestamp / 32767 * 32767);

        System.out.println(Integer.toBinaryString(~(4)));

        System.out.println(Integer.toBinaryString(~(32768 - 1)));

        System.out.println(Integer.toBinaryString(1000));

        System.out.println(Integer.toBinaryString((~(32768 - 1)) & 1000));

        int n = (32768 + 100);
        n = -5;
        System.out.println(Integer.toBinaryString(~(32768 - 1)));
        System.out.println(Integer.toBinaryString(n));
        System.out.println(Integer.toBinaryString((~(32768 - 1)) & n));
        System.out.println(Integer.toBinaryString(n - (~(32768 - 1)) & n));

        System.out.println((~(32768 - 1)) & n);
        System.out.println(n - (~(32768 - 1)) & n);

        //System.out.println(-5% 90);

        System.out.println(-5 & (32768 - 1));
    }
}
