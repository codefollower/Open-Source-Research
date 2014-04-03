package test.attr;


class VisitSelectTest<T> {
	static int i=12;
	VisitSelectTest<?> v=new VisitSelectTest<T>();
	int i2=v.i;
}
/*
class VisitSelectTest<T> {
	//interface InterfaceA {}
	//InterfaceA ia = new VisitSelectTest().new InterfaceA(){};

	//abstract class ClassA {}
	//ClassA ca = new ClassA();

	enum EnumA {}
	int ea = new EnumA();
	EnumA eb = new EnumA();

	class ClassA {
		void m1(EnumA e) {}
		void m2() {
			m1(new EnumA());
		}
	}

	//static class ClassA {}
	//ClassA ca = new VisitSelectTest().new ClassA(){};


	//interface InterfaceB<T> {}
	//InterfaceB<String> ib = new <String>InterfaceB(){};

	//interface InterfaceB {}
	//InterfaceB ib = new InterfaceB(10,20){};

	
	//class InnerClassA {
	//	InnerClassA() {
	//		this(new test.attr.VisitSelectTest.InnerClassA() {});
	//	}
	//	InnerClassA(InnerClassA at) {}
	//}
}


import test.attr.ClassA.ClassC;
class VisitSelectTest<T> {
	T t=null;
	//VisitSelectTest<T> t=null;

	class ClassB{}
	class ClassA<V>{
		//VisitSelectTest.ClassA<ClassB> vc=null;
		//ClassA<ClassB> vc=null;

		//test.attr.ClassA.ClassC<ClassB> vc=null;

		ClassC<ClassB> vc=null;
	}

	class InnerClassA {
		InnerClassA() {
			this(new InnerClassA() {});
		}
		InnerClassA(InnerClassA at) {}
	}
}

class ClassA{
	class ClassC<V>{
	}
}

class ClassA{}
class VisitSelectTest<T> {
	static T a2;

	void m(VisitSelectTest<T> a1){
		VisitSelectTest<ClassA> a2;
	}
}



class VisitSelectTest<T> {
	static T a2;

	void m(T a1){
		T a2;
	}

	{
		T a3;
	}

	static {
		T a4;
	}
}



class VisitSelectTest<T> {
	static T a2;
}


test\attr\VisitSelectTest.java:13: 对 a1 的引用不明确，test.attr.ClassA 中的 变
量 a1 和 test.attr.ClassB 中的 变量 a1 都匹配
        int a2=a1;
               ^
1 错误

import static test.attr.ClassA.*;
import static test.attr.ClassB.*;
class ClassA{
	//protected static int a1=10;
	//private static int a1=10;
	static int a1=10;
}
class ClassB{
	static int a1=10;
}
class VisitSelectTest {
	int a2=a1;
}
*/

/*
class VisitSelectTest {
	int a1;
	static class C1_1 {
		int a2=a1; //无法从静态上下文中引用非静态 变量 a1
		class C1_1_1 {
			//static int a2=a1; //内部类不能有静态声明
			int a2=a1; //无法从静态上下文中引用非静态 变量 a1
		}
	}
}



interface VisitSelectTest1 {
	int a1=10;
}

interface VisitSelectTest2 extends VisitSelectTest1 {
	int a1=10;
	static int a2=a1;
}


public class VisitSelectTest {
	int a1;
	static int a2=a1;
}



import test.attr.ClassA;
class ClassA<T>{
	void m(){};
}
public class VisitSelectTest extends ClassA {
	void m() {super.m();}
}


abstract class ClassA{
	abstract void m();
}
public class VisitSelectTest extends ClassA {
	void m() {super.m();}
}



//import test.attr.PointTree.Visitor;//导入需要 test.attr.Tree.Visitor 的规范名称
import test.attr.Tree.Visitor;//

class ClassA{}
class Tree<A> { class Visitor {  } }
class PointTree extends Tree<ClassA> {}

class VisitSelectTest {
	PointTree.Visitor pv;

	Tree<ClassA>.Visitor pv2;
	Visitor pv3;
}



// ...PointTree.Visitor...
                    //
                    // Then the type of the last expression above is
                    // Tree<Point>.Visitor.


class ClassA{
	@Deprecated
	int i=10;

	@Deprecated
	ClassA(int i) {}

	@Deprecated
	ClassA() {}
}

public class VisitSelectTest extends ClassA {

	int i=super.i;

	ClassA ca=new ClassA(10);

}




public class VisitSelectTest<T> {

	void m(int t){
		int c = t.t;
	}

}


class A<T>{}
class B {
	//int i;
	B b;
	B b(){ return new B(); }

	//static B b;   //正确
	//static B b(){ return new B(); } //正确

	class b{}
}
public class VisitSelectTest<T extends B> extends A<T.b> {
	//A<T.i> al;

	//A<T.b> al;
	B b=T.b;
	B b2=T.b();
}


class ClassA{
	int i=10;
}

public class VisitSelectTest extends ClassA {

	int i=super.i;

}

public class VisitSelectTest<T> {

	//Class c = T.class;
	//static Class c2 = T.class;

	Class c3 = T[].class;

	//static void m(T t){}

}
*/
