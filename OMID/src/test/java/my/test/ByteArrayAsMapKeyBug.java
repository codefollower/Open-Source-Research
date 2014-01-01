package my.test;

import java.util.HashMap;
import java.util.Map;

public class ByteArrayAsMapKeyBug {

    /**
     * @param args
     */
    public static void main(String[] args) {
        byte[] a = { 1, 2 };
        byte[] b = a;
        byte[] c = a;
        Map<byte[], String> map = new HashMap<byte[], String>();
        map.put(b, "b");
        map.put(c, "c");
        System.out.println(map.size());
        
         
        
        map = new HashMap<byte[], String>();
        map.put(new RowKey(a).getTable(), "b");
        map.put(new RowKey(a).getTable(), "c");
        System.out.println(map.size());
    }
}

class RowKey {
    private byte[] tableId;

    public RowKey(byte[] t) {
        tableId = t;
    }

    public byte[] getTable() {
        return tableId;
    }

}