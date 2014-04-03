package my.test;
import java.lang.annotation.*;
/*
interface InterfaceTest {
	void interfaceMethod(int arg);
}
*/

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
    //String value2() default "defaultValue";
}

class ExtendsTest{}
class TestA extends ExtendsTest{}
class TestB extends ExtendsTest{}

interface InterfaceA{}
interface InterfaceB{}
@interface AnnotationA{
String value();
}
@interface AnnotationB{
String value() default "str";
}
public class ClassWriterTest<T> extends ExtendsTest implements InterfaceA {
	{
		class LocalClassInInitBlock{}
	}
	protected abstract class C<T> { int this$0 ;abstract T id(T x); }
	class D extends C<String> { String id(String x) { return x; } }
    
	protected static int fieldStatic=10;
	public int fieldA=10;
	
	int fieldB=10;

	@Deprecated
	@MyAnnotation("test")
	@SuppressWarnings("fallthrough")
	private static final int fieldStaticFinal=10;
	
	/*
	public static void main(String args[]) {
		Test t=new Test();
		System.err.println(t.fieldStatic);
		System.err.println(t.fieldA);
		System.err.println(t.fieldB);
		System.err.println(t.fieldStaticFinal);
	}
	*/
	private T t;
	
	class TestC{}
	protected class TestD<V>{}
	
	TestD<? extends TestC> testD;
	/*Test(int i) {
		//boolean myBoolean=false;
		//if((i<0)?i<10:i>10) myBoolean=!myBoolean;
		//if((i<0)?true:false) myBoolean=!myBoolean;
		
		//for(int n=0;n<10;n++) i++;
		
		//while(i<10) i++;
		
		//do i++;while(i<10);
		//myLable:i++;
		
		myLable: {
			i++;
			while(true) {
				if(i>0) continue;
				else break myLable;
			}
		}
	}*/
	
	<T extends Exception & InterfaceA & InterfaceB,V extends InterfaceA & InterfaceB > int method(int i,int... is) throws T {
		class LocalTest{}
		
		T t;
		
		//int[] n=(i<0) ? new int[5] : new int[10];
		//int[] n=(i<0) ? new int[5] : new int[10];
		//ExtendsTest[] n=(i<0) ? new TestA[5] : new TestB[10];
		
		//ExtendsTest et=new TestA();
		
		//int[] in=new int[]{2,3};
		//i=10;
		//i-=-10;
		//i/=10;
		//String s="str";
		//s+=i+"str2";
		String s[]=new String[]{"str","sss"};
		s[0]+=i+"str2";
		
		return 0;
	}
}