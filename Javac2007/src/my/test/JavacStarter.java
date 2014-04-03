package my.test;

public class JavacStarter {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        com.sun.tools.javac.Main.main(new String[] { "@args.txt" });
    }

}
