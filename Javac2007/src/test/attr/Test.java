package test.attr;

import java.lang.reflect.Modifier;

enum TestEnum {
    BAR {
        private final TestEnum self = BAR;
        private final TestEnum other = QUX;
    },
    QUX
}
class A {
    private protected int m() { return -1; }
}

class B extends A
{
    private protected int m() { return 0; }
}
class ConstValInit {

    public final String fnl_str = "Test string";

    public final int fnl_int = 1;

    public static void main(String args[]) throws Exception {

        Class checksClass = Class.forName("ConstValInit");
        ConstValInit checksObject = (ConstValInit)(checksClass.newInstance());
        String reflected_fnl_str = (String)checksClass.getField("fnl_str").get(checksObject);
        if (!checksObject.fnl_str.equals(reflected_fnl_str)) {
            throw new Exception("FAILED: ordinary and reflected field values differ");
        }

    }

    void foo() {
        // Statement below will not compile if fnl_int is not recognized
        // as a constant expression.
        switch (1) {
            case fnl_int: break;
        }
    }

}

enum AbstractEnum1 implements AE1_I {
    toto {
        public void m() {
        }
    }
    ;
    public static void main(String[] args) {
        if (!Modifier.isAbstract(AbstractEnum1.class.getModifiers()))
            throw new Error();
    }
}

interface AE1_I {
    void m();
}
/*
enum AbstractEmptyEnum {
    ;
    abstract void m();
}
*/
class Test {
/*
  static {
	  x++;
    //System.out.println(x);  //illegal forward reference
  }

  static int x = 1;


  {
    System.out.println(x);  //illegal forward reference
  }

  int x = 1;


  void test() {

    //int i = (i = 1) + i++; //legal (i in scope and definitely assigned)
  }
  */
}

/*
class T6676362a {
    //Object o = new Object() {Object m() {return o2;}};

	//Object o = new Object();

	Object o = new Object(){};
    final Object o2 = o;
}




class T6676362b {
    static final int i1 = T6676362b.i2; //legal - usage is not via simple name
    static final int i2 = i1;
}

class UseBeforeDeclaration {
    static {
        x = 100; // ok - assignment
//      int y = ((x)) + 1; // error - read before declaration
        int v = ((x)) = 3; // ok - x at left hand side of assignment
        int z = UseBeforeDeclaration.x * 2; // ok - not accessed via simple name
        Object o = new Object(){
                void foo(){x++;} // ok - occurs in a different class
                {x++;} // ok - occurs in a different class
            };
    }
    {
        j = 200; // ok - assignment
//      j = j + 1; // error - right hand side reads before declaration
//      int k = j = j + 1; // error - right hand side reads before declaration
        int n = j = 300; // ok - j at left hand side of assignment
//      int h = j++; // error - read before declaration
        int l = this.j * 3; // ok - not accessed via simple name
        Object o = new Object(){
                void foo(){j++;} // ok - occurs in a different class
                { j = j + 1;} // ok - occurs in a different class
            };
    }
    int w = x= 3; // ok - x at left hand side of assignment
    int p = x; // ok - instance initializers may access static fields
    static int u = (new Object(){int bar(){return x;}}).bar(); // ok - occurs in a different class
    static int x;
    int m = j = 4; // ok - j at left hand side of assignment
    int o = (new Object(){int bar(){return j;}}).bar(); // ok - occurs in a different class
    int j;
}

*/