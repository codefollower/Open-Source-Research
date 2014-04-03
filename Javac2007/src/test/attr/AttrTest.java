package test.attr;


//import java.lang.annotation.*;
//import test.attr.ClassA.*;

//@AnnotationA(f1=10,f2=2)
//@AnnotationA(f1=10,f2=2,f3=AttrTest.class)
//@Deprecated
//class ClassA<T> {
	//static class InnerClassA<V> {}
//	class InnerClassA<V> {}
//}
class AttrTest<T> {
	/*
	int f1;
	int f2=10;
	final int f3=20;
	static int f4=30;
	static final int f5=40;

	<T> void m1(int i1,int i2) throws T{}
	*/

	//int field=10;

	class InnerClassA<V> {
		<T> InnerClassA(){
			//this(new AttrTest<String>().new <String>InnerClassA<AttrTest<String>>() { void methodA(){} });

			this(new <String>InnerClassA<AttrTest<String>>() { void methodA(){} });
		}
		<T> InnerClassA(InnerClassA at){}

		//InnerClassA at = new AttrTest<String>().new <String>InnerClassA<AttrTest<String>>() { void methodA(){} };
	}

	//AttrTest at = new AttrTest() { void methodA(){} };

	//AttrTest at = new AttrTest<AttrTest>() { void methodA(){} };
	//AttrTest at = new <String>AttrTest<AttrTest>() { void methodA(){} };

	//InnerClassA at = new AttrTest<String>().new <String>InnerClassA<AttrTest<String>>() { void methodA(){} };
	

	//<T> AttrTest(){}

	void methodA() {
		//AttrTest<?> at1;
		//AttrTest<? extends Number> at2;
		//AttrTest<? super Integer> at3;

		//InnerClassA<? extends AttrTest> ic;
		//ClassA ca=new ClassA();
		//当ClassA.InnerClassA是非static时，类型的格式不正确，给出了普通类型的类型参数
		//ClassA.InnerClassA<? extends AttrTest> ic2;
		//InnerClassA<? extends AttrTest> ic2;

		//int i[];
		//labelA:while(true) labelA: break;
		
		//AttrTest<String> at = new AttrTest<String>();

		//AttrTest.<AttrTest<String>,String>methodB(new AttrTest<String>(),"str",10);
		//methodB(new AttrTest<String>(),"str",10);

		//at.<AttrTest<String>,String>methodB(at,"str",10);
		//<AttrTest<String>,String>methodB(at,"str",10);
	}

	//AttrTest() {
	//	<AttrTest<String>,String>this(new AttrTest<String>(),"str",10);
	//}

	//static <T,V> void methodB(T t,V v,int i){}
	//<T,V> void methodB(T t,V v,int i){}

	//<T,V> AttrTest(T t,V v,int i){}

	/**
     * @deprecated
     */
	//abstract void myMethod();
	
	//@SuppressWarnings({"fallthrough","unchecked"})
	//@Deprecated
	//public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		//int field=Test.this.field;
		/*
		;
		if(i-4>5) field++;
		else i--;
		
		for(final int dd:ii);
		for(;i<10;i++,i+=2);
		for(int n=0,n2[]={10,20};n<10;n++);
		
		while(i<10);
		
		do i++;
		while(i<10);
		
		try {
			i++;
		}
		catch(RuntimeException e) {}
		catch(Exception e) {}
		finally {
			i=0;
		}
		
		switch(i) {
			case 0:
			i++;
			break;
			default:
			i--;
		}
		
		synchronized (s) {}
		
		assert (i<10): "message";
		
		//枚举类型不能为本地类型
		//enum MyEnum {}
		myLable: i++;
		
		//报错：意外的类型
		//++-10;
		
		//++--myInt;
		
		//注意ExtendsTestBound必须extends TestBound且同时implements MyInterfaceA
		//因为Test类的第一个形式参数S extends TestBound & MyInterfaceA
		//否则报错：类型参数 my.test.ExtendsTestBound 不在其限制范围之内
		//Test<String> test=new Test<String>();
		
		int myIntArray[]=new int[10];
		myIntArray[1]=10;
		
		int[] myIntArray2={1,2};
		i++;
		field<<=2;
		//final @Deprecated class MyLocalClass1{}
		//abstract class MyLocalClass2 {}
		//strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		*/
		//return new int[0][0];
	//}
}
/*
@Target({ElementType.FIELD, ElementType.METHOD})
@interface AnnotationA{
	int f1();
	int f2();
	Class f3();
}

*/
/*
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

@MyAnnotation("test")
class annotation_type_not_applicable {}
*/