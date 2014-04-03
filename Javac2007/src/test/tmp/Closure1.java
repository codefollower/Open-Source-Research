package test.tmp;

public class Closure1 {
    static class S {
        void hi() { throw new Error(); }
        S() { hi(); }
    }
    static class T {
        void greet() { }
        class N extends S {
            void hi() {
                T.this.greet();
            }
        }
    }
    public static void main(String av[]) { new T().new N(); }
}

//class Closure11 {
//	public static void main(String av[]) { new T().new N(); }
//}