package test.attr;

class C1<T>{}
class C2 extends C1{}
class F1{}
interface interfaceA{}
interface interfaceB{}

class ClassA<T>{}
class ClassB<T> extends ClassA<T>{}
class ClassC<T> extends ClassB<F1>{}
interface interfaceC<T> {}
interface interfaceD<T> extends interfaceC<F1>{}
//class IsCastableTest<T> extends C2 {
class IsCastableTest<T,V extends T,M extends interfaceA,N extends F1&interfaceA,L extends interfaceB&interfaceA,O extends F1> extends C2 {
	//IsCastableTest tt1;
	//IsCastableTest<C1> tt2=(IsCastableTest<?>)tt1;
	
	//IsCastableTest<T> tt3=(IsCastableTest<T>)tt1;
	//IsCastableTest<C1> tt4=(IsCastableTest<F1>)tt1;
	//IsCastableTest tt5=(IsCastableTest<F1>)tt1;
	//IsCastableTest<C2> tt6=(C1<F1>)tt1;

	//C1<F1> tt7=(IsCastableTest<C2>)tt1;

	//C1<C1<F1>> tt7=(IsCastableTest<IsCastableTest<C2>>)tt1;
	{
		Object aObject=null;

		/*
		int[] aIntArray;
		Object[] aObjectArray;
		C2[] aC2Array;
		aObject = (int[])aIntArray;
		aObject = (Object)aObjectArray;
		aObject = (Object)aC2Array;
		aObject = (Object)new Integer[0];
		*/

		T t=null;
		V v=null;
		M m=null;
		N n=null;
		L l=null;
		O o=null;
		ClassB<C2> cb=null;
		ClassC<C2> cc=null;
		interfaceD<C2> id=null;
		interfaceD<N> id2=null;
		ClassB<N> cb2=null;
		ClassC cc2=null;
		//aObject = (Object)t;
		//aObject = (Object)v;
		aObject = (Object)m;
		aObject = (Object)n;
		aObject = (Object)l;
		aObject = (Object)o;
		aObject = (Object)cb;
		aObject = (Object)cc;
		aObject = (Object)id;
		aObject = (Object)id2;
		aObject = (Object)cb2;
		aObject = (Object)cc2;
	}



	//int[] aIntArray2 = (int[])aIntArray;
	//C1<?> tt8=(IsCastableTest<C2>)tt1;
	//C1<? super F1> tt9=(IsCastableTest<C2>)tt1;
	/*
	<T> void m() {
		C1 a=new C1();
		C2 b=new C2();
		F1 f=new F1();
		byte inta=10;
		Integer aInteger = 10;

		Number aNumber=10;
		aNumber=aInteger;
		aInteger=aNumber;

		aNumber=(Number)aInteger;
		aInteger=(Integer)aNumber;

		int intb=(int)inta;
		//int intb=(int)m();
		double doublea=10.2;
		intb=(int)doublea;
		//intb=(int)null;
		//Integer inta = new Integer(10);
		//int intb=(int)inta;

		//int inta=10;
		//Integer intb = (Integer)inta;
		


		a=(C1)null;
		a=(C1)b;
		a=(C1)f;

		T t;
		a=(C1)t;
	}
	*/
}

/*
//class ClassA<T>{}
class ClassA{}
class ClassB extends ClassA {}
class ClassC extends ClassB {
	//ClassC c;
	//ClassB b=(ClassB)c;
	//ClassB b2=(ClassB)null;
}
//interface ClassD<T extends ClassA<T>>{}
interface ClassD<T extends ClassA>{}
//class Test<V extends ClassD<ClassB>&ClassD<? super ClassC>>{}
//class Test<V extends ClassD<?>&ClassD<? super ClassC>>{}
//class Test<V extends ClassD<? extends Test>&ClassD<? super ClassC>>{}

//class Test<V extends ClassD<? extends ClassB>&ClassD<? super ClassC>>{}
class Test<V extends ClassD<? extends V>&ClassD<? super ClassC>>{}
*/



/*
//com.sun.tools.javac.comp.Attr===>attribBounds(1)
class A {
	private void m1(int i,char c) {}
}
interface I {}

class B<T extends A&I> extends A {
	@Override
	public void m1(int i,char c) {}
}
*/
/*
//子类不能覆盖超类中同名的private方法
class A {
	private void m1(int i,char c) {}
}

class B extends A {
	@Override
	public void m1(int i,char c) {}
}
*/