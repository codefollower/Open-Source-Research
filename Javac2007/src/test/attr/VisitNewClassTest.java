package test.attr;

class Test<T>{}
class VisitNewClassTest {
	void m(Class<?>[] pa) {}
	Test<Object> vec = new Test<String>();

	class Cell<E> {

      E value;

      Cell (E v) { value=v; }

      E get() { return value; }

      void set(E v) { value=v; }

    }
	void m2() {
    Cell x = new Cell<String>("abc");

    Object o=x.value;          // OK, has type Object

    x.get();          // OK, has type Object

    x.set("def");     // unchecked warning
	}

}
/*
class Test<TA extends Number,TB extends TA,TC extends TA>{}
class VisitNewClassTest {
	//<T extends VisitNewClassTest>void m1(boolean b) {
	
	void m1(boolean b,Test<? super Integer,?,?> c,Test<? super Float,?,?> d) {
		//Object o = b ? 1 :"dd";
		//Object o = b ? c :d;

		//Test<?> c1 = new Test<Float>();
		//Test<? super Float> d1 = new Test<Float>();

		//Object o = b ? b :d1;

		Object o = b ? c :d;
	}
}



class VisitNewClassTest<TA> {
	void m1() throws VisitNewClassTest {}
	//<TC extends VisitNewClassTest<? extends TB>,TB> TC m1(TC tc) { return tc; }
}

enum EA {
	;
	EA() { super(); }
}

@interface IA {
	boolean equals();
	int hashCode();
	String toString();
}


class ClassA1{}
class ClassA2{}
class VisitNewClassTest<TA> {

	//VsitNewClassTest vct = new <ClassA1>VisitNewClassTest<ClassA2>() {};

	////内部错误；无法将位于
	//VisitNewClassTest vct = new VisitNewClassTest<ClassA2>(this) {};

	
	VisitNewClassTest vct = new VisitNewClassTest<ClassA2>(new ClassA2(),VisitNewClassTest.<Integer>m1(1)) {};
	<TB extends TA>VisitNewClassTest(TA t,int i){}
	<TC> TC m1(TC tc) { return tc; }

	//<TC extends TA> TC m1(TC tc) { return tc; }
	//VisitNewClassTest vct = new VisitNewClassTest<ClassA2>(new ClassA2(),VisitNewClassTest.<ClassA2>m1(new ClassA2())) {};
}


class VisitNewClassTest<T> {

	//VisitNewClassTest vct = new VisitNewClassTest(this) {};

	
	//<V>VisitNewClassTest(int i){}
	//VisitNewClassTest(){}

	VisitNewClassTest vct = new VisitNewClassTest(this);
	VisitNewClassTest(VisitNewClassTest<T> t){}
}


class VisitNewClassTest<T> {
	class ClassA2{}
	class ClassA3<A3T1,A3T2,A3T3,A3T4,A3T5>{}

	class ClassA1<VA,VB,VC> {
		//<V extends ClassA3<?,? extends ClassA2,? super ClassA2>>ClassA1(int i){}
		
		//这里会提早绑定? extends V[ClassA2]   ? super V[ClassA2]
		//<V extends ClassA3<?,? extends V,? super V>>ClassA1(int i){}

		//这里? extends V{ bound=Object }      ? super V2{ bound=ClassA3 }
		<V2 extends ClassA3<?,? extends VA,? extends VC,? super VB,? super V2>>ClassA1(int i){}
		ClassA1(){}

		static <VA,VB,VC> void m1(){}

		void m2() {
			ClassA1.<? extends ClassA2,? super ClassA2,?>m1();
		}
	}

	//class ClassA1_1 extends ClassA1 {}

	//ClassA1_1 c1 = new ClassA1_1() {};

	//class ClassA1_2 extends ClassA1<? extends ClassA2,? super ClassA2,?> {}

	//ClassA1_2 c2 = new ClassA1_2() {};
}


class VisitNewClassTest<T> {
	class ClassA2{}
	class ClassA3<A3T1,A3T2,A3T3>{}

	class ClassA1<V> {
		//<V extends ClassA3<?,? extends ClassA2,? super ClassA2>>ClassA1(int i){}
		
		//这里会提早绑定? extends V[ClassA2]   ? super V[ClassA2]
		//<V extends ClassA3<?,? extends V,? super V>>ClassA1(int i){}

		//这里? extends V{ bound=Object }      ? super V2{ bound=ClassA3 }
		<V2 extends ClassA3<?,? extends V,? super V2>>ClassA1(int i){}
		ClassA1(){}
	}

	class ClassA1_1 extends ClassA1 {}

	ClassA1_1 c1 = new ClassA1_1() {};

	class ClassA1_2 extends ClassA1<ClassA2> {}

	ClassA1_2 c2 = new ClassA1_2() {};
}

class VisitNewClassTest<T> {

	VisitNewClassTest vct = new VisitNewClassTest() {};

	
	<V>VisitNewClassTest(int i){}
	VisitNewClassTest(){}
}


class VisitNewClassTest<T> {
	//interface InterfaceA {}
	//InterfaceA ia = new VisitNewClassTest().new InterfaceA(){};

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
	//ClassA ca = new VisitNewClassTest().new ClassA(){};


	//interface InterfaceB<T> {}
	//InterfaceB<String> ib = new <String>InterfaceB(){};

	//interface InterfaceB {}
	//InterfaceB ib = new InterfaceB(10,20){};

	//
	//class InnerClassA {
	//	InnerClassA() {
	//		this(new test.attr.VisitNewClassTest.InnerClassA() {});
	//	}
	//	InnerClassA(InnerClassA at) {}
	//}
	//
}


//public class VisitNewClassTest<W> {
public class VisitNewClassTest {
	VisitNewClassTest() {
		//不带限制范围的类或接口
		//this(new InnerClassA<? super VisitNewClassTest>(){});
		//this(new <? extends VisitNewClassTest>InnerClassA<VisitNewClassTest>(10){}); //非法的类型开始

		//this(new <InnerClassA>InnerClassA<VisitNewClassTest>(10){}); //111

		this(new InnerClassA<VisitNewClassTest>(10){}); //111

		//如果不为InnerClassA加类型参数，那么会把InnerClassA的构造函数的类
		//型变量<T extends VisitNewClassTest>擦除(参见:Types===>memberType)
		//this(new InnerClassA(10){}); //222
		
		//this(new InnerEnumA(){});//无法实例化枚举类型
		//this(new InnerAbstractClassA());//抽象的；无法对其进行实例化
		//this(new InnerInterfaceA<VisitNewClassTest>(10){});//不带有参数
		//this(new VisitNewClassTest().new InnerInterfaceA<VisitNewClassTest>(){});//不带有参数

		//this(new VisitNewClassTest().new InnerInterfaceA<VisitNewClassTest>());
	}
	VisitNewClassTest(InnerClassA ica) {}

	//VisitNewClassTest(InnerInterfaceA ica) {}

	class InnerClassA<T> {
		//private InnerClassA(int i){}  //111

		//private <V extends W>InnerClassA(int i){}  //111

		//private <V extends T>InnerClassA(int i){}  //111

		private <VA extends T,VB extends VA>InnerClassA(int i){}  //111
	}

	

	//enum InnerEnumA {}
	//abstract class InnerAbstractClassA<T> {}
	//static interface InnerInterfaceA<T> {}
}

com.sun.tools.javac.code.Types===>memberType(Type t, Symbol sym)
-------------------------------------------------------------------------
t=test.attr.VisitNewClassTest.InnerClassA t.tag=CLASS
sym=<T>InnerClassA(int) sym.flags()=0x40000002 private acyclic 
com.sun.tools.javac.code.Types$15===>visitClassType(2)
-------------------------------------------------------------------------
t=test.attr.VisitNewClassTest.InnerClassA t.tag=CLASS
sym=<T>InnerClassA(int) sym.flags()=0x40000002 private acyclic 
owner=test.attr.VisitNewClassTest.InnerClassA owner.flags()=0x0 
owner.type.isParameterized()=true
com.sun.tools.javac.code.Types===>asOuterSuper(Type t, Symbol sym)
-------------------------------------------------------------------------
t=test.attr.VisitNewClassTest.InnerClassA t.tag=CLASS
sym=test.attr.VisitNewClassTest.InnerClassA
com.sun.tools.javac.code.Types===>asSuper(Type t, Symbol sym)
-------------------------------------------------------------------------
t=test.attr.VisitNewClassTest.InnerClassA  t.tag=CLASS
sym=test.attr.VisitNewClassTest.InnerClassA
returnType=test.attr.VisitNewClassTest.InnerClassA
com.sun.tools.javac.code.Types===>asSuper(Type t, Symbol sym)  END
-------------------------------------------------------------------------

com.sun.tools.javac.code.Types===>asOuterSuper(Type t, Symbol sym)  END
-------------------------------------------------------------------------

ownerParams=T
baseParams =
com.sun.tools.javac.code.Types===>erasure(Type t)
-------------------------------------------------------------------------
t=<T>Method(int)void  t.tag=(FORALL)16  lastBaseTag=8
com.sun.tools.javac.code.Type$MethodType===>map(Mapping f)
-------------------------------------------------------------------------
f=erasure
com.sun.tools.javac.code.Types===>erasure(Type t)
-------------------------------------------------------------------------
t=int  t.tag=(INT)4  lastBaseTag=8
t=int  erasureType=int
com.sun.tools.javac.code.Types===>erasure(Type t)  END
-------------------------------------------------------------------------

com.sun.tools.javac.code.Types===>erasure(Type t)
-------------------------------------------------------------------------
t=void  t.tag=(VOID)9  lastBaseTag=8
t=void  erasureType=void
com.sun.tools.javac.code.Types===>erasure(Type t)  END
-------------------------------------------------------------------------

com.sun.tools.javac.code.Type$MethodType===>map(Mapping f)  END
-------------------------------------------------------------------------

t=<T>Method(int)void  erasureType=Method(int)void
com.sun.tools.javac.code.Types===>erasure(Type t)  END
-------------------------------------------------------------------------

com.sun.tools.javac.code.Types$15===>visitClassType(2)  END
-------------------------------------------------------------------------

returnType=Method(int)void
com.sun.tools.javac.code.Types===>memberType(Type t, Symbol sym)  END
*/