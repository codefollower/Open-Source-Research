class tmp {
	int outer_f1=1;
		int outer_m1(){
			//f1=10;
			return outer_f1*10;
		}

	public static void main(String[] args) {
		tmp.InnerClassA a = new tmp().new InnerClassA(1,2);
	}
	class InnerClassA{
		int f1=1;
		int m1(){
			//f1=10;
			return f1*10;
		}

		InnerClassA(int i1,int i2) {
			this(
				new InnerClassA(10) {
					int Anonf1 = m1();
					int Anonf2 = outer_m1();
					int Anonf3 = outer_f1;

					int out() {
						System.out.println(Anonf1);
						System.out.println(Anonf2);
						System.out.println(Anonf3);
						return Anonf1;
					}
				}.out()
			);
		}
		//InnerClassA(InnerClassA at) {}
		InnerClassA(int i1) {}
	}
}

class Aclass<T> {
	int f1;
	Aclass<?> a;

	void m() {
		T t1=T.this;
		T t2=T.super.toString();
		T t3=T.class;

		a.m();
		a.f1++;
	}
}
class Test {

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

}


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