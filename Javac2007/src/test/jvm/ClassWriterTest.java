package test.jvm;

/*
public class ClassWriterTest{
	protected static class MemberClass{
		int a;
		int b;
		
		
		protected static int fieldStaticA=10;

		@Deprecated
		static final int fieldStaticB=10;
		static final int fieldStaticC;
		public int fieldA=10;

		static{
			fieldStaticC=10;
		}
	}

	class MemberClassB<T> {
		private T t;
	}
}

*/

/*
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
    //String value2() default "defaultValue";
}

public class ClassWriterTest{
	@MyAnnotation("test")
	@SuppressWarnings("fallthrough")
	int a;
}

*/
/*
import java.lang.annotation.*;

enum EnumA {
	A,B;
}

@interface MyAnnotation {
    int f1() default 10;
	int f2() default 10;
	int f3() default 10;

	EnumA f4() default EnumA.A;
}

public class ClassWriterTest{
	@MyAnnotation(f3=20,f4=EnumA.B)
	int a;
}

*/

/*
class Test<A extends Number, B extends Number, C> {
	//A a;
	//B b;

	Test<? super Integer, ? extends Number, ?> test;
}
*/
/*
class ExceptionA extends Exception {}
class ExceptionB extends Exception {}
class ExceptionC extends Exception {}

class Test<A extends ExceptionA, B extends ExceptionB> {
	void m() throws A,B,ExceptionC{}
}
*/

/*
//测试assembleSig FORALL

class ExceptionA extends Exception {}
class ExceptionB extends Exception {}
class ExceptionC extends Exception {}

class Test{
	<A extends ExceptionA, B extends ExceptionB> void m() throws A,B,ExceptionC{}
}
*/

class Test<A extends Number, B extends Number, C> {
	//A a;
	//B b;

	Test<? super Integer, ? extends Number, ?> test;

	class MemberClass<A extends Number, B extends Number, C>{}

	{
		class LocalClassInInitBlock{}
	}
}