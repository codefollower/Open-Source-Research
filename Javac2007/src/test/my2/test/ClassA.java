package my.test;

public class ClassA<T> {
	ClassA1 a1=new ClassA1();
	ClassA2 a2=new ClassA2();

	public class ClassAInnerClass<V> {
		public void print() {}
	}

	

	public void print() {
		ClassAInnerClass<ClassA> cic=new ClassAInnerClass<ClassA>();
		cic.print();
		System.out.println("ClassA");
		a1.print();
		a2.print();
	}

	public static void main(String[] args) {
		ClassA a=new ClassA();
		a.print();

		for(int i=1;i<100;i=i*2+1) {
			System.out.println(Integer.toBinaryString(916));
			System.out.println(Integer.toBinaryString(highestOneBit(916)));
			System.out.println();
		}
	}

	public static int highestOneBit(int i) {
        // HD, Figure 3-1
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }
}