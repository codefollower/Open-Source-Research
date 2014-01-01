package my.test;

import com.yahoo.omid.tso.TSOServer;

public class TSOServerTest {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        args = new String[] {"-zk", "127.0.0.1:2181", "-port", "1234"};
        TSOServer.main(args);
    }

}
