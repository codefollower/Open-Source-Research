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
public class Test<T,V extends ExtendsTest> extends ExtendsTest implements InterfaceA {
	{
		class LocalClassInInitBlock{}
	}
	public abstract class C<T> { int this$0 ;abstract T id(T x); }
	public class D<S extends Test<String,ExtendsTest>> extends C<String> { String id(String x) { return x; } }
    
	protected static int fieldStatic=10;
	public int fieldA=10;
	
	int fieldB=10;

	@Deprecated
	@MyAnnotation("test")
	@SuppressWarnings("fallthrough")
	private static final int fieldStaticFinal=10;

	public Test() {}

	public Test(T t) {
		this.t=t;
	}
	
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
	TestD<?> testE;
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
	
	<T extends Exception & InterfaceA & InterfaceB,V extends InterfaceA & InterfaceB > int method(int i,int... is) throws T,java.io.IOException {
		class LocalTest{}

		LocalTest lt;
		
		//T t;
		
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
//public class Test<T> implements InterfaceTest {
	/*
	static int fieldStatic=10;
	static {
		fieldStatic=20;
	}
	
	int field=10;
	{
		field=20;
	}
	
	static final int fieldStaticFinal=10;
	
	Test() {
		InterfaceTest interfaceTest=this;
		interfaceTest.interfaceMethod(10);
		myStaticMethod();
		myPrivateMethod();
		myMethod(12,"str",100,101,102);
	}
	
	private void myPrivateMethod(){}
	
	private static void myStaticMethod(){}
	
	public void interfaceMethod(int arg){}
	*/
	
	/*
	public static void main(String args[]) {
		Test t=new Test();
		System.err.println(t.field);
		System.err.println(t.fieldStatic);
	}*/
	
	//protected abstract class C<T> { int this$0 ;abstract T id(T x); }
    //class D extends C<String> { String id(String x) { return x; } }
    
    /*
    enum MyEnum {
    EXTENDS("? extends "),
    SUPER("? super "),
    UNBOUND("?");

    private final String name;

    MyEnum(String name) {
	this.name = name;
    }

    public String toString() { return name; }
    }*/
    
    /*
	class MyInnerClass{
		MyInnerClass(){
			this("str");
		}
		MyInnerClass(String str){}
	}*/
	
	/*
	class MyInnerClass2 extends MyInnerClass{
		MyInnerClass2(String SSSSS){
			super("dfdfdf");
		}
		
		MyInnerClass2(){
			this("dfdfdf");
		}
	}
	*/
	
	/*	
	public <V> int myMethod(int i,String s,double d,int... ii) {
		
		//final @Deprecated class MyLocalClass{
		//	int fieldLocal=10;
		//	{
		//		fieldLocal=20;
		//	}
		//}
		
		{}
		{final double myMethodDouble=1.3F;}

		//C c = new D()
    	//c.id(new Object()); // fails with a ClassCastException
    	
		//Flow阶段用于错误测试的例子
		
		//对于这样的if语句，编译器把整个if语句替换成--bbb
		int bbb=10;
		if(false) bbb++;
		else bbb--;
		//对于这样的if语句，编译器把整个if语句替换成++ccc
		int ccc=10;
		if(true) ccc++;
		else ccc--;
		//对于这样的if语句，编译器也不能优化，即使(iii=10)>5这一结果很明显
		int iii=10;
		if(iii>5) iii++;
		//else iii--;
		
		int array[][]=null;
		//if(s==null) s=null;
		
		boolean myBoolean=false;
		if((i<0)?i<10:i>10) myBoolean=!myBoolean;
		
		if(i+1/2*3-4>5) i++;
		else i--;
		
		//int ddd;
		int ddd=10;
		for(final int dd:ii) {
			int eee=3*6*7;
			if (dd>0) {
				continue;
				//;
				//ddd++;
			}
			else ddd--;

			//else myStaticMethod();
			if (dd>10) {
				break;
				//;
				//ddd++;
			}
			ddd--;
			int fff;
		}
		
		//for(;7<10;) i++;
		//ddd++;
		
		for(;i<10;i++,i+=2);
		for(int n=0,n2[]={10,20};n<10;n++);
		
		while(i<10) return 100;
		
		do i++;
		while(i<10);
		
		try {
			i++;
		}
		catch(RuntimeException e) {}
		catch(Exception e) {}
		//catch(NoSuchFieldException e) {}
		finally {
			i=0;
		}
		
		switch(i) {
			case 0:
			i++;
			int fff;
			break;
			default:
			i--;
		}
		
		synchronized (s) {}
		
		assert (i<10): "message";
		
		
		
		//abstract class MyClass2{}
		//strictfp class MyClass3{}
		
		//枚举类型不能为本地类型
		//enum MyEnum {}
		myLable:{
			int bb;// i++;
			//break;
		}
		
		//报错：意外的类型
		//++-10;
		
		//++--myInt;
		
		//注意ExtendsTestBound必须extends TestBound且同时implements MyInterfaceA
		//因为Test类的第一个形式参数S extends TestBound & MyInterfaceA
		//否则报错：类型参数 my.test.ExtendsTestBound 不在其限制范围之内
		//Test<ExtendsTestBound,String> test=new Test<ExtendsTestBound,String>();
		
		int myIntArray[]=new int[10];
		myIntArray[1]=10;
		
		int[] myIntArray2={1,2};
		i++;
		int myInt=0;
		myInt<<=(int)2L;
		//myInt<<=(int)2;//警告：[转换] 向 int 转换出现冗余
		
		//return new int[0][0];
		//对于condition ? trueExpression : falseExpression语句
        //从这里可以看出falseExpression不能含有赋值运符符AssignmentOperator
        //但是trueExpression可以
		//myInt=(myInt>0) ? myInt=0:myInt=1;
		
		return 1000;
	}
	*/
}


/*
import java.util.*;
import javax.lang.model.element.*;
import javax.annotation.processing.*;

import java.lang.annotation.*;
 import static java.lang.annotation.RetentionPolicy.*;
 import javax.lang.model.element.*;
 import static javax.lang.model.element.NestingKind.*;
 //import static my.test.TopLevelClass.MemberClass;
 //import static my.test.TopLevelClass.MemberClass_static;
 //import static my.test.TopLevelClass.MemberInterface;
 //import static my.test.TopLevelClass.MemberInterface_static;
 //import static my.test.TopLevelClass.MemberEnum;
 //import static my.test.TopLevelClass.MemberEnum_static;
 
 
 @Nesting(TOP_LEVEL)
class NestingExamples {
     @Nesting(MEMBER)
     static class MemberClass1{}
 
     @Nesting(MEMBER)
     class MemberClass2{}
 
     public static void main(String... argv) {
         @Nesting(LOCAL)
         class LocalClass{};
 
         Class<?>[] classes = {
             NestingExamples.class,
             MemberClass1.class,
             MemberClass2.class,
             LocalClass.class
         };     
 
         for(Class<?> clazz : classes) {
             System.out.format("%s is %s%n",
                               clazz.getName(),
                               clazz.getAnnotation(Nesting.class).value());
         }
     }
 }
 
 @Retention(RUNTIME)
 @interface Nesting {
     NestingKind value();
 }
@Nesting(TOP_LEVEL)

interface Inter {
	void my();
}

class Test2 implements Inter {
	public void my(){
		System.out.println("dfdf");
	}
}
	
public class Test extends AbstractProcessor{
	
public  boolean process(Set<? extends TypeElement> annotations,
				    RoundEnvironment roundEnv) {
				    	return true;
				    }
				    
	
	public static void main(String args[]) {
		Set<Inter> inter = new LinkedHashSet<Inter>();
		inter.add(new Test2());
		inter.add(new Test2());		
		inter.add(new Test2());
		
		for(Inter a  : inter) a.my();
	}		
				 
	public Test() {
	int leng=100;
	//1
	for(int i=0;i<=100;i++);
	//2
	for(int i=leng;i<=0;i++);
*/



	/*
	第2种方式比第1种方式更快

	因为第1种方式使用的是“if_icmpgt”指令
	还多了一条“bipush	100”指令用于把常量100压入堆栈，
	当要与i比较时还得把常量100弹出，

	第2种方式使用的是“ifgt”指令，并没有“iconst_0”，

	所以第2种方式更快一些，因为它比第1种方式少了常量操作数的出入栈
	*/
//	}
//}
/*
public class Test<T> {
	public static void main(String args[]) {
		Test<String> t=new Test<String>();
		//t.method(new Object(),new Integer(8));
		//t.method("ss",new Integer(8));
		t.method(1.5f,9L);
	}
    
    <T extends Number> void method(T t1,T t2) {
        
    }
}
*/
/*
//class 我的类 {}
public enum Test {
	enum1("enum1"),
	enum2("enum1"),
	enum3,
	enum4;

    public String name;

    Test(String name) {
        this.name = name;
    }
    
    int _$我=2;
    
    Test() {
        this.name = "empty";
    }
}
*/
/*
public class Test {
	public static void main(String args[]) {
		System.out.println("Hello World!");
		main2("str");
		main3("str");
	}
	
	Test(String str) {}
	
	public static Test main2(String str) {
		return new Test("dfdf") {
			String out(){
				int i=0;
				return "dfdf";
			}
		};
	}
	
	public static Test main3(final String str) {
		return new Test("dfdf") {
			String out(){
				int i=0;
				return str;
			}
		};
	}
*/
	/*
	bin\mysrc\my\test\Test.java:53: 从内部类中访问局部变量 str1；需要被声明为最终类型
	                                return str1+str2;
	                                       ^
	bin\mysrc\my\test\Test.java:53: 从内部类中访问局部变量 str2；需要被声明为最终类型
	                                return str1+str2;
	                                            ^
	2 错误
	*/
	
	/*
	如果在匿名内部类中访问方法中的参数(str1)或方法中定义的局部变量(str2)
	那么str1与str2都得声明为最终(final)类型。
	如果这个匿名内部类是通过new Test("dfdf")这样的方式产生的，
	那么编译器会把这个匿名内部类重新命名为Test$n，Test$n继承自Test，
	被Test$n引用的方法中的参数(str1)或方法中定义的局部变量(str2)必需是
	最终(final)类型，编译器把str1与str2都当成Test$n自己的最终(final)类型字段
	并将Test(String)改成Test$1(String,String,String)
	(注：按照Test$n引用的方法中的参数或方法中定义的局部变量的个数扩展构造方法)
	*/
	/*public static Test main4(String str1) {
		String str2="dfdf";
		return new Test("dfdf") {
			String out(){
				int i=0;
				return str1+str2;
			}
		};
	}*/
	
	/*
	public static Test main5(final String str1) {
		final int str2=3;
		return new Test("dfdf") {
			String out(){
				int i=str2;
				return str1+str2;
			}
		};
	}
	
	public Test2 main6() {
		return new Test2() {
			String out(){
				return str;
			}
		};
	}
	private String str="dfdf";
	public class Test2{}
	String out(){return "dfd";}
}*/

/*
public class Test {
	public static void main(String args[]) {
		System.out.println("Hello World!");
		main2();
		main2("str");
		main2("str","str");
	}
	
	public static void main2(String... str) {
		System.out.println("Hello World!");
	}
}


package my.test;
public class Test {
	Test() {
		int v1=1;
		while(v1<10000) {
			int v2=5;
			v1=v1+v2*2;
		}
	}
}

package my.test;
public class Test {
	public static void main(String args[]) {
		System.out.println("Hello World!");
	}
}
*/