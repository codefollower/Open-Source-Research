package test.attr;


class ClassA<V> {
	void m1() {}
}
class VisitMethodDefTest<T> extends ClassA {
	void m1() { m1(); }
}




/*
class ClassA {
	void m1(){}
	private void m2(){}
	public void m3(){}
	protected void m4(){}
}
class VisitMethodDefTest extends ClassA {
	protected void m1(){}
	void m2(){}
	void m3(){}
	private void m4(){}
}


//test\attr\VisitMethodDefTest.java:5: 对于 @interface，不允许 "extends"
//@interface VisitMethodDefTest extends InterfaceA {
//                                      ^
//1 错误
interface InterfaceA {
	int m1();
}
@interface VisitMethodDefTest extends InterfaceA {
	int m1();
}


class ClassA {
	static void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}


class ClassA {
	final void m1() {}
}
class VisitMethodDefTest extends ClassA {
	void m1() {}
}


interface InterfaceA {
	void m1();
}
class VisitMethodDefTest implements InterfaceA {
	static void m1() {}
}


class ClassA {
	void m1() {}
}
class VisitMethodDefTest extends ClassA {
	static void m1() {}
}



class ClassA {
	private void m1(int i) {} //private方法overrides(4)返回false
	void m1() {}
}
class VisitMethodDefTest extends ClassA {
	
	void m1() {}
}


class VisitMethodDefTest {
	public enum enum_no_finalize {
		;
		protected final void finalize(){}
		public final void finalize(){}
		public final void finalize(int i){}
		public final int finalize(){return 0;}
	}
}


class VisitMethodDefTest {
	class ClassA{}
	interface InterfaceA {}

	<TA,TB extends ClassA & InterfaceA> VisitMethodDefTest() {}
}
*/

//class VisitMethodDefTest {
	/**
     * @deprecated
     */
//	VisitMethodDefTest() {}
//}



/*
class VisitMethodDefTest {

	@SuppressWarnings({"fallthrough","unchecked"})
	@Deprecated
	VisitMethodDefTest() {}
}
*/