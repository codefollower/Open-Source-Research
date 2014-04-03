**********************************************************
时间: 2007.05.29 23:00
用途: 说明javac不是一个优化编译器的例子
**********************************************************
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

**********************************************************
时间: 2007.05.29 23:00
用途: ClassWriter测试例子
**********************************************************
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
public class Test<T> extends ExtendsTest implements InterfaceA {
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

Enter测试例子:
package my.test;
import my.*;
public class Test<S,T extends ExtendsTest,E extends ExtendsTest&MyInterfaceA> {
	public class MyInnerClass {}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	public static interface MyInnerInterfaceStatic {}
	public enum MyInnerEnum {}
	public static enum MyInnerEnumStatic {}
	
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
}
class MyTheSamePackageClass {}


public class Test{
/*
	public class MyInnerClass {}
	public interface MyInnerInterface {}
	public enum MyInnerEnum {}
*/	
	public void myMethod() {
		final @Deprecated class MyLocalClass{}
	}
	int field;
}

MenberEnter测试例子:
package my.test;
import static my.StaticImportTest.MyInnerClassStaticPublic;
import static my.StaticImportTest.*;
import my.StaticImportTest.MyInnerClass;
import my.*;
public class Test<S,T extends ExtendsTest,E extends ExtendsTest&MyInterfaceA> {
	//public static class MyInnerClassStaticPublic {}
	
	public class MyInnerClass {}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	//public static interface MyInnerInterfaceStatic {}
	public enum MyInnerEnum {}
	//public static enum MyInnerEnumStatic {}
	
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field;
}
class MyTheSamePackageClass {}


package my.test;
;
import static my.StaticImportTest.*;
import my.*;
import static my.StaticImportTest.MyInnerClassStaticPublic;
import my.ExtendsTest;
//public class Test<S,T extends ExtendsTest,E extends ExtendsTest&MyInterfaceA> extends ExtendsTest {
public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA> extends my.ExtendsTest.MyInnerClassStatic {
	//public static class MyInnerClassStaticPublic {}
	
	public class MyInnerClass {
		//错误提示:
		//bin\mysrc\my\test\Test.java:11: 只有在静态上下文中才允许使用枚举声明
        //        public enum MyInnerEnum2{}
        //               ^
		//public enum MyInnerEnum2{}
	}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	//public static interface MyInnerInterfaceStatic {}
	public enum MyInnerEnum {
		;
		MyInnerEnum() {}
	}
	//public static enum MyInnerEnumStatic {}
	
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		field=Test.super.field;
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field;
	//MyInterfaceA myInterfaceA=new MyInterfaceA() {};
	/*
	{
		field=6;
	}
	*/
}
//class MyTheSamePackageClass {}


MenberEnter Attr测试例子:
package my.test;
import static my.StaticImportTest.*;
import my.*;
import static my.StaticImportTest.MyInnerClassStaticPublic;
import my.ExtendsTest;
public class Test<S,T extends ExtendsTest,E extends ExtendsTest&MyInterfaceA> extends ExtendsTest {
	//public static class MyInnerClassStaticPublic {}
	
	public class MyInnerClass {}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	//public static interface MyInnerInterfaceStatic {}
	public enum MyInnerEnum {}
	//public static enum MyInnerEnumStatic {}
	
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field;
	/*
	{
		field=6;
	}
	*/
}
class MyTheSamePackageClass {}

MenberEnter Attr测试例子:
package my.test;
//import static my.StaticImportTest.*;
//import my.*;
//import static my.StaticImportTest.MyInnerClassStaticPublic;
import my.ExtendsTest;
//public class Test<S,T extends ExtendsTest,E extends ExtendsTest&MyInterfaceA> extends ExtendsTest {
public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA> extends my.ExtendsTest<Test,MyInterfaceA>.MyInnerClassStatic {
	//public static class MyInnerClassStaticPublic {}
	
	public class MyInnerClass {}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	//public static interface MyInnerInterfaceStatic {}
	public enum MyInnerEnum {}
	//public static enum MyInnerEnumStatic {}
	
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field;
	/*
	{
		field=6;
	}
	*/
}
//class MyTheSamePackageClass {}



Attr 语言各类语句:

package my.test;
public class Test<T> {
	//abstract void myMethod();
	@Deprecated
	public <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		//int field=Test.this.field;
		;
		if(i+1/2*3-4>5) i++;
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
		final @Deprecated class MyLocalClass1{}
		abstract class MyLocalClass2 {}
		strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field=10;
}

第一:
javac <选项>的处理:
<选项>类型分三种:
    enum OptionKind {
        NORMAL,  //标准选项
        EXTENDED,//非标准选项(也称扩展选项,用标准选项“-X”来查看所有扩展选项)
        HIDDEN,  //隐藏选项(内部使用，不会显示)
    }
    
    
几个重要的Scope:

JCCompilationUnit
//packge.members_field是一个Scope,这个Scope里的每一个Entry
        //代表了包名目录下的所有除成员类与本地类以外的类
        //每个Entry是在Enter阶段加入的
        public PackageSymbol packge;
        		Scope members_field
        
        //在Env.topLevelEnv(JCCompilationUnit tree)中进行初始化
        //与JCCompilationUnit对应的Env<AttrContext>.info.Scope相同。
        //Scope包括在同一源文件中定义的所有最顶层的类(不包括成员类)
        //以及非*号结尾的import语句导入的类
        public Scope namedImportScope;
        
        //java.lang包中的所有类及所有以*号结尾的import语句导入的类
        public Scope starImportScope;
        
JCClassDecl

//sym.members_field是一个Scope,这个Scope里的每一个Entry
        //代表一个成员类(或成员接口)，但是不包括type parameter。
        //type parameter在与JCClassDecl对应的Env<AttrContext>.info.Scope中。
        
        //在Enter阶断把成员类、接口、枚举类加入members_field
        //在MemberEnter阶断把字段、方法加入members_field
        //在Attr阶断本地类加入members_field
        public ClassSymbol sym;
        	Scope members_field
        	






MemberEnter详细例子:
package my.test;
import static my.StaticImportTest.*;
import my.*;
import static my.StaticImportTest.MyInnerClassStaticPublic;
//import static my.ExtendsTest.MyInnerClassStaticPublic;
//import java.util.Date;
//import java.sql.Date;
			
import my.StaticImportTest.MyInnerClass;
//import my.StaticImportTest.*;

public class Test<S,V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest implements InterfaceTest {
	public int myInterfaceMethod(int i,char c) {
		return field;
	}

	public class MyTestInnerClass {
		MyTestInnerClass(int intInner) {
		}
	}

	//abstract void myMethod();
	@Deprecated
	public <M extends T,S> int[] myMethod(final M t,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
	//无法从静态上下文中引用非静态 类型变量的限制范围 T
	//public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		//int field=Test.this.field;
		;
		if(i+1/2*3-4>5) i++;
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
		final @Deprecated class MyLocalClass1{
			//int field=Test.this.field;
			MyLocalClass1() {
				Test.this.field=10;
			}
		}
		//abstract class MyLocalClass2 {}
		//strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		return new int[0][0];
	}
	int field=10;
	static int staticfield=10;
	{
		field=20;
	}
	
	static{
		staticfield=20;
	}
}







//Flow分析使用的测试例子:
package my.test;

@Deprecated
public class Test<S, T> {
	/*
	;
	public class MyInnerClass {
		MyInnerClass(){}
	}
	public static class MyInnerClassStatic {}
	public interface MyInnerInterface {}
	public static interface MyInnerInterfaceStatic {
		//int myInt=1;
		//String myMethod();
	}
	public enum MyInnerEnum{,}//只有一个逗号
	public static enum MyInnerEnumStatic implements MyInterfaceA,MyInterfaceB{
	    @Deprecated EXTENDS("? extends ") {
	    	 String toString() {
	    	 	return "extends"; 
	    	 }
	    },
	    SUPER("? super "),
	    UNBOUND("?");
	
	    private final String name;
	
	    MyInnerEnumStatic(String name) {
		this.name = name;
	    }
	
	    public String toString() { return name; }
	}
	//{ case; }//错误提示:“单个 case”或“单个 default”
		*/
	{
		int i=0;
	}
	static {
		//myStaticInt2=10;
		int i=2;
		final int i2=2;
		final int i3;
		//final int i4=myStaticMethod();
	}
	
	public static int myStaticMethod() throws Exception{
		return 10;
	}
	
	//Flow阶段用于错误测试的例子
	//Test() {
	//	this(2);//super();
	//}
	
	Test() throws Error, Exception {
		this(2);//super();
	}
	Test(int myInt) throws Error, NoSuchFieldException {
		//this.myInt=myInt;
		//myStaticInt1=1;
	}
	Test(float f) throws  NoSuchFieldException, InterruptedException {
		//myStaticInt1=1;
	}  
	
	<N extends T,S> void myMethod2(final N n,S s) throws Exception{
		int myMethodInt;
	}
	
	public <M extends T,S> int myMethod3(M m,S s)[] throws Exception,RuntimeException {
		int myMethodInt=10;
		return new int[0];
	}

	
	@Deprecated
	//public void myMethod(int i,String s,int... ii) throws Exception{
	public <M extends T,S> int[] myMethod(final M m,S[] s2[],int i,String s,int... ii)[] {//throws Exception,Error{
		//Flow阶段用于错误测试的例子
		/*
		{}
		int bbb;
		if(false) bbb++;
		else bbb--;
		
		int ccc;
		if(true) ccc++;
		else ccc--;
		
		int iii;
		if(iii>5) iii++;
		else iii--;
		
		boolean myBoolean;
		if(i<0) myBoolean=!myBoolean;
		
		if(i+1/2*3-4>5) i++;
		else i--;
		*/
		//int ddd;
		int ddd=10;
		for(final int dd:ii) {
			int eee;
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
		/*
		for(;7<10;) i++;
		ddd++;
		*/
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
		
		final @Deprecated class MyClass{}
		final int myMethodInt;
		
		abstract class MyClass2{}
		strictfp class MyClass3{}
		
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
		myInt<<=(int)2L;
		//myInt<<=(int)2;//警告：[转换] 向 int 转换出现冗余
		
		return new int[0][0];
		//对于condition ? trueExpression : falseExpression语句
        //从这里可以看出falseExpression不能含有赋值运符符AssignmentOperator
        //但是trueExpression可以
		//myInt=(myInt>0) ? myInt=0:myInt=1;
	}
	
	public int myInt='\uuuuu5df2';
	public final int myInt2=10;
	//public final int myInt3;//Flow阶段用于错误测试的例子
	public float myFloat=0x.1p-1f;
	//public float 我的变量=" ";
	
	public static final int myStaticInt1='\377';
	//Flow阶段用于错误测试的例子
	/*public static final int myStaticInt2;
	public static final int myStaticInt3;
	public static final int myStaticInt4;
	public static final int myStaticInt5;
	
	public static final int myStaticInt21;
	public static final int myStaticInt31;
	public static final int myStaticInt41;
	public static final int myStaticInt51;
	
	public static final int myStaticInt211;
	public static final int myStaticInt311;
	public static final int myStaticInt411;
	public static final int myStaticInt511;
	
	public static final int myStaticInt2111;
	public static final int myStaticInt3111;
	public static final int myStaticInt4111;
	public static final int myStaticInt5111;
	
	
	public static final int myStaticInt21111;
	public static final int myStaticInt31111;
	public static final int myStaticInt41111;
	public static final int myStaticInt51111;
	
	public static final int myStaticInt211111;
	public static final int myStaticInt311111;
	public static final int myStaticInt411111;
	public static final int myStaticInt511111;
	
	public static final int myStaticInt2111111;
	public static final int myStaticInt3111111;
	public static final int myStaticInt4111111;
	public static final int myStaticInt5111111;
	
	public static final int myStaticInt21111111;
	public static final int myStaticInt31111111;
	public static final int myStaticInt41111111;
	public static final int myStaticInt51111111;
	
	public static final int myStaticInt211111111;
	public static final int myStaticInt311111111;
	public static final int myStaticInt411111111;
	public static final int myStaticInt511111111;
	
	public static final int myStaticInt2111111111;
	public static final int myStaticInt3111111111;
	public static final int myStaticInt4111111111;
	public static final int myStaticInt5111111111;
	*/
	S myS;
	T myT;
	public void method(){}
	public void method(int i){}
	
}



**********************************************************
时间: 2007.05.06 14:20
用途: Types类的测试用例
**********************************************************

package my.test;
import static my.StaticImportTest.*;
import my.*;
import static my.StaticImportTest.MyInnerClassStaticPublic;
//import static my.ExtendsTest.MyInnerClassStaticPublic;
//import java.util.Date;
//import java.sql.Date;
			
import my.StaticImportTest.MyInnerClass;
//import my.StaticImportTest.*;

import java.util.*;
public class Test<S,V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest implements InterfaceTest {
	public int myInterfaceMethod(int i,char c) {
		return field;
	}

	//public class MyTestInnerClass<Z extends ExtendsTest> implements MyInterfaceA,MyInterfaceB {
	public class MyTestInnerClass<Z extends ExtendsTest<? super ExtendsTest,? super Test>> implements MyInterfaceA,MyInterfaceB {
		MyTestInnerClass(int intInner) {
		}
	}

	//abstract void myMethod();
	@Deprecated
	public <M extends MyTestInnerClass<ExtendsTest<Test,ExtendsTest>>,S> int[] myMethod(final M t,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
	//public <M extends MyTestInnerClass<? extends Test>,S> int[] myMethod(final M t,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
	//无法从静态上下文中引用非静态 类型变量的限制范围 T
	//public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		//int field=Test.this.field;
		;
		if(i+1/2*3-4>5) i++;
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
		//MyTestInnerClass<?> test=new MyTestInnerClass<ExtendsTest>(2);
		
		int myIntArray[]=new int[10];
		myIntArray[1]=10;
		
		int[] myIntArray2={1,2};
		i++;
		field<<=2;
		final @Deprecated class MyLocalClass1{
			//int field=Test.this.field;
			MyLocalClass1() {
				Test.this.field=10;
			}
		}
		//abstract class MyLocalClass2 {}
		//strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许
		
		
		Set<?> unknownSet = new HashSet<String>();
		/** 向 Set s 中添加一个元素*/
		//addToSet(unknownSet, "abc"); // 非法
		addToSet2(unknownSet, "abc"); // 正确
		
		Set<?> ss = Collections.unmodifiableSet(unknownSet); // this works! Why?

		return new int[0][0];
	}
	public static <T> void addToSet(Set<T> s, T t) {}
	
	public static <T> void addToSet2(Set<? extends T> s, T t) {}
	
	int field=10;
	static int staticfield=10;
	{
		field=20;
	}
	
	static{
		staticfield=20;
	}
}




**********************************************************
时间: 2007.05.13 10:30
用途: Attr,Types类的测试用例
**********************************************************

package my.test;
//@SuppressWarnings("fallthrough")
@SuppressWarnings({"fallthrough","unchecked"})
@Deprecated
interface InterfaceTest<T extends Number,S> {}
class ExtendsTest<T,S> {}

/*
enum EnumTest{
	myEnum;
	public abstract void abstractEnumMethod();
}
*/
@SuppressWarnings({"fallthrough","deprecation","unchecked"})
@Deprecated
public class Test<S,P extends V, V extends InterfaceTest<Number,String>&InterfaceTest<? super Float,String>,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest implements InterfaceTest {
	public interface InterfaceTestInner {}
	
	//@Deprecated
	//class ExtendsTest<T,S> {}
	
	//@SuppressWarnings("deprecation")
	//@Deprecated
	public class 
	MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>> 
	implements InterfaceTest,InterfaceTestInner {}
	
	public void myMethod() {
		MyTestInnerClass<?> myTestInnerClass = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,Test>>();
		
		MyTestInnerClass<?> myTestInnerClass2 = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,ExtendsTest>>();
		
		aMethod(myTestInnerClass2); // 正确
		bMethod(myTestInnerClass2, myTestInnerClass); // 非法
		cMethod(myTestInnerClass2, myTestInnerClass); // 正确
		
	}
	
	public <T> void aMethod(MyTestInnerClass<T> s) {}
	
	public <T> void bMethod(MyTestInnerClass<T> s, T t) {}
	
	public <T> void cMethod(MyTestInnerClass<? extends T> s, T t) {}
	
	public abstract void abstractMethod();
}







**********************************************************
时间: 2007.05.14 19:35
用途: Attr,Types类的测试用例
**********************************************************


package my.test;
//@SuppressWarnings("fallthrough")
@SuppressWarnings({"fallthrough","unchecked"})
@Deprecated
interface InterfaceTest<A extends Number,B> {
	int interfaceMethod(int i);
}
abstract class ExtendsTest<C,D> {
	public int interfaceMethod(int i) {
		return 1;
	}
	public int myMethod() {}
}

@SuppressWarnings({"fallthrough","deprecation","unchecked"})
@Deprecated
public class Test<S,P extends V, V extends InterfaceTest<Number,String>&InterfaceTest<? super Float,String>,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest<Number,String> implements InterfaceTest {
	public interface InterfaceTestInner {}
	
	//@Deprecated
	//class ExtendsTest<T,S> {}
	
	//@SuppressWarnings("deprecation")
	//@Deprecated
	public class 
	MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>> 
	implements InterfaceTest,InterfaceTestInner {}
	
	public void myMethod() {
		MyTestInnerClass<?> myTestInnerClass = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,Test>>();
		
		MyTestInnerClass<?> myTestInnerClass2 = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,ExtendsTest>>();
		
		aMethod(myTestInnerClass2); // 正确
		bMethod(myTestInnerClass2, myTestInnerClass); // 非法
		cMethod(myTestInnerClass2, myTestInnerClass); // 正确
		
	}
	
	public <T> void aMethod(MyTestInnerClass<T> s) {}
	
	public <T> void bMethod(MyTestInnerClass<T> s, T t) {}
	
	public <T> void cMethod(MyTestInnerClass<? extends T> s, T t) {}
	
	//public abstract void abstractMethod();
}




**********************************************************
时间: 2007.05.14 19:35
用途: Attr,Types类，注释检查的测试用例
**********************************************************

package my.test;
import java.lang.annotation.*;
/*

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
*/
@SuppressWarnings("unchecked")
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
    String value2() default "defaultValue";
}

//class MyTest<T extends Enum & Cloneable> {}

//@SuppressWarnings("fallthrough")
@SuppressWarnings({"fallthrough","unchecked"})
//@Deprecated
interface InterfaceTest<A extends Number,B> {
	int interfaceMethod(int i);
	//void myOverrideMethod(int i,byte b);
}
abstract class ExtendsTest2 {
	//public void myOverrideMethod(int i,byte b) {}
}
abstract class ExtendsTest<C,D> extends  ExtendsTest2 implements InterfaceTest{
	public int interfaceMethod(int i) {
		return 1;
	}
	public void myOverrideMethod(int i,char c) {}
}

//@MyAnnotation(value="test",value="test")
//@Target({ElementType.TYPE,ElementType.TYPE})
@SuppressWarnings({"fallthrough","deprecation","unchecked"})
@Deprecated
//@Override
public class Test<S,P extends V, V extends InterfaceTest<Number,String>&InterfaceTest<? super Float,String>,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest> extends ExtendsTest<Number,String> implements InterfaceTest {
	public interface InterfaceTestInner {}
	
	@Override
	public void myOverrideMethod(int i,byte b) {}
	
	//@Deprecated
	//class ExtendsTest<T,S> {}
	
	//@SuppressWarnings("deprecation")
	//@Deprecated
	public class 
	MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>> 
	implements InterfaceTest,InterfaceTestInner {}
	
	public void myMethod() {
		MyTestInnerClass<?> myTestInnerClass = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,Test>>();
		
		MyTestInnerClass<?> myTestInnerClass2 = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,ExtendsTest>>();
		
		aMethod(myTestInnerClass2); // 正确
		bMethod(myTestInnerClass2, myTestInnerClass); // 非法
		cMethod(myTestInnerClass2, myTestInnerClass); // 正确
		
	}
	
	public <T> void aMethod(MyTestInnerClass<T> s) {}
	
	public <T> void bMethod(MyTestInnerClass<T> s, T t) {}
	
	public <T> void cMethod(MyTestInnerClass<? extends T> s, T t) {}
	
	//public abstract void abstractMethod();
}


**********************************************************
时间: 2007.05.17 14:58
用途: Attr,Types类，类型参数的测试用例
**********************************************************

package my.test;

@SuppressWarnings({"fallthrough","unchecked"})
//@Deprecated
interface InterfaceTest<A extends Number,B> {
	//int interfaceMethod(int i);
	//void myOverrideMethod(int i,byte b);
	
	interface MyInnerStaticInterface {}
}
abstract class ExtendsTest<C,D> implements InterfaceTest<Integer,String>{

	public int interfaceMethod(int i) {
		return 1;
	}
	//public static class InnerStaticClass {}
	//public class InnerClass<T> {}
	
	public class InnerClass<A,B,C,D extends Number> {}
	
	
	//public int incompatibleMethod(int i) {
	//	return ;
	//}
	//public void myOverrideMethod(int i,char c) {}
}


@SuppressWarnings({"fallthrough","deprecation","unchecked"})
@Deprecated
public class Test<E extends ExtendsTest.InnerClass<Number,?,? extends Long,? super Integer>>{
//public class Test<E extends ExtendsTest<Integer,String>.InnerStaticClass, F extends ExtendsTest<Integer,String>.InnerClass>{
//public class Test<E, F extends ExtendsTest>{
//@Override
//public class Test<E extends ExtendsTest & InterfaceTest<Float,String>> extends ExtendsTest implements InterfaceTest<Long,String> {
//public class Test<E extends ExtendsTest & ExtendsTest[] & my.test.InterfaceTest<Number,String>.MyInnerStaticInterface & InterfaceTest<? super Float,String> & InterfaceTest<Number,String>, F, G extends H, H extends ExtendsTest<Number,String>&InterfaceTest<? super Float,String>,K extends ExtendsTest> extends ExtendsTest<Number,String> implements InterfaceTest {
//public class Test<E extends ExtendsTest&InterfaceTest, F, G extends H, H extends InterfaceTest<Number,String>&InterfaceTest<? super Float,String>,K extends ExtendsTest> extends ExtendsTest<Number,String> implements InterfaceTest {
	public interface InterfaceTestInner {}
	
	//public void incompatibleMethod(int i){}
	
	//@Override
	//public void myOverrideMethod(int i,byte b) {}
	
	//@Deprecated
	//class ExtendsTest<T,S> {}
	
	//@SuppressWarnings("deprecation")
	//@Deprecated
	public class 
	MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>> 
	implements InterfaceTest,InterfaceTestInner {}
	
	public void myMethod() {
		Test.MyTestInnerClass<?> myTestInnerClass = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,Test>>();
		
		MyTestInnerClass<?> myTestInnerClass2 = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,ExtendsTest>>();
		
		aMethod(myTestInnerClass2); // 正确
		bMethod(myTestInnerClass2, myTestInnerClass); // 非法
		cMethod(myTestInnerClass2, myTestInnerClass); // 正确
		
	}
	
	public <T> void aMethod(MyTestInnerClass<T> s) {}
	
	public <T> void bMethod(MyTestInnerClass<T> s, T t) {}
	
	public <T> void cMethod(MyTestInnerClass<? extends T> s, T t) {}
	
	//public abstract void abstractMethod();
}



**********************************************************
时间: 2007.05.20 09:20
用途: Attr,Types类，类型参数的测试用例
**********************************************************

package my.test;

@SuppressWarnings({"fallthrough","unchecked"})
//@Deprecated
interface InterfaceTest<A extends Number,B> {
	int interfaceMethod(int i);
	//void myOverrideMethod(int i,byte b);
	
	//interface MyInnerStaticInterface {}
}
abstract class ExtendsTest<C,D> implements InterfaceTest<Integer,String>{

	//public int interfaceMethod(int i) { return 1; }
	//abstract <T> void extendsTestAbstractMethod();
	//abstract void extendsTestAbstractMethod(char c);
	//abstract int extendsTestAbstractMethod(char c);
	
	//public static class InnerStaticClass {}
	//public class InnerClass<T> {}
	
	//public class InnerClass<A,B,C,D extends Number> {}
	
	
	public int incompatibleMethod(int i) { return 1; }
	//public void myOverrideMethod(int i,char c) {}
}


@SuppressWarnings({"fallthrough","deprecation","unchecked"})
@Deprecated
//@Override
//public class Test<E extends ExtendsTest & InterfaceTest<Float,String>> extends ExtendsTest implements InterfaceTest<Long,String> {
//public class Test<E extends ExtendsTest & ExtendsTest[] & my.test.InterfaceTest<Number,String>.MyInnerStaticInterface & InterfaceTest<? super Float,String> & InterfaceTest<Number,String>, F, G extends H, H extends ExtendsTest<Number,String>&InterfaceTest<? super Float,String>,K extends ExtendsTest> extends ExtendsTest<Number,String> implements InterfaceTest {
//public class Test<E extends ExtendsTest&InterfaceTest, F, G extends H, H extends InterfaceTest<Number,String>&InterfaceTest<? super Float,String>,K extends ExtendsTest> extends ExtendsTest<Number,String> implements InterfaceTest {
//public class Test<E extends ExtendsTest.InnerClass<Number,?,? extends Long,? super Integer>>{
//public class Test<E extends ExtendsTest<Integer,String>.InnerStaticClass, F extends ExtendsTest<Integer,String>.InnerClass>{
//public class Test<E, F> extends ExtendsTest<String,Integer> {
//public class Test extends ExtendsTest<String,Integer> implements InterfaceTest {
//public class Test<T,V> extends Exception {
public class Test<T,V> extends ExtendsTest<String,Integer> implements InterfaceTest<Integer,String> {
	
	public interface InterfaceTestInner {}
	
	//public <T> void extendsTestAbstractMethod(int i) {}
	//public void extendsTestAbstractMethod(int i) {}
	//void extendsTestAbstractMethod(char c){}
	
	public static int interfaceMethod(int i) { return 1; }
	public void incompatibleMethod(int i){}
	
	//@Override
	//public void myOverrideMethod(int i,byte b) {}
	
	//@Deprecated
	//class ExtendsTest<T,S> {}
	
	//@SuppressWarnings("deprecation")
	//@Deprecated
	public class 
	MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>> 
	implements InterfaceTest,InterfaceTestInner {}
	
	public void myMethod() {
		Test.MyTestInnerClass<?> myTestInnerClass = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,Test>>();
		
		MyTestInnerClass<?> myTestInnerClass2 = 
		new MyTestInnerClass<ExtendsTest<ExtendsTest,ExtendsTest>>();
		
		aMethod(myTestInnerClass2); // 正确
		bMethod(myTestInnerClass2, myTestInnerClass); // 非法
		cMethod(myTestInnerClass2, myTestInnerClass); // 正确
		
	}
	
	public <T> void aMethod(MyTestInnerClass<T> s) {}
	
	public <T> void bMethod(MyTestInnerClass<T> s, T t) {}
	
	public <T> void cMethod(MyTestInnerClass<? extends T> s, T t) {}
	
	//public abstract void abstractMethod();
}


/*
class ClassA {}
class ClassB extends ClassA {}
public class Test<T extends ClassA> {
	public void myMethod() {
		Test<?> test = new Test<ClassB>();
		aMethod(test);    // 正确
		bMethod(test, 1); // 非法
		cMethod(test, 1); // 正确
	}
	
	public <T> void aMethod(Test<T> s) {}
	
	public <T> void bMethod(Test<T> s, T t) {}
	
	public <T> void cMethod(Test<? extends T> s, T t) {}
} 
*/
/*
class ClassA {}
class ClassB extends ClassA {}
class ClassC<T,V> extends ClassB {}
class ClassD extends ClassC {}

class ExtendsTest
<A extends ClassC, B extends ClassC, C extends ClassC<ClassA,ClassB>, D extends ClassC<ExtendsTest,ClassB>, E extends ClassC<ExtendsTest,ClassB>, F extends ClassC> {}

//public class Test 
//<T extends ExtendsTest<?, ClassB, ? extends ExtendsTest, ? extends ClassD, ? extends ClassA, ? super ClassB>> {}

public class Test 
<T extends ExtendsTest
<?, ClassD, ? extends ClassA, ? extends ClassD, ? extends ClassA, ? super ClassD>> {}


class Test2 extends Test
<ExtendsTest<ClassA,ClassC,ClassD,ClassC,ClassC,ClassC>>{} 
*/


**********************************************************
时间: 2007.05.20 20:43
用途: com.sun.tools.javac.comp.Check===>checkOverride(4)测试用例
**********************************************************
package my.test;
//@SuppressWarnings("deprecation")
//@Deprecated
interface InterfaceTest {
	void interfaceMethod_A();
	void interfaceMethod_B();
}
abstract class ExtendsTest<C,D> {
	public  void extendsMethod_A(){}
	public static void extendsMethod_B(){}
	public static void extendsMethod_C(){}
	public final void extendsMethod_D(){}
	
	protected void extendsMethod_E(){}
	public void extendsMethod_F(){}
	
	public Integer extendsMethod_G(){ return 1; }
	public void extendsMethod_H(){}
	
	public void extendsMethod_I() throws Throwable {}
	
	public void extendsMethod_J(int[] i){}
	
	public void extendsMethod_K(int... i){}
	
	@Deprecated
	public void extendsMethod_L(){}
}

public class Test extends ExtendsTest implements InterfaceTest {
	public static void extendsMethod_A(){}
	public static void interfaceMethod_A(){}
	
	public void extendsMethod_B(){}
	public static void extendsMethod_C(){}
	public void extendsMethod_D(){}
	
	private void interfaceMethod_B(){}
	void extendsMethod_E(){}
	protected void extendsMethod_F(){}
	
	public int extendsMethod_G(){ return 1; }
	public int extendsMethod_H(){ return 1; }
	
	public void extendsMethod_I() throws Error{}
	
	public void extendsMethod_J(int... i){}
	
	public void extendsMethod_K(int[] i){}
	
	public void extendsMethod_L(){}
}



**********************************************************
时间: 2007.05.21 16:23
用途: com.sun.tools.javac.comp.Check===>checkOverride(4)测试用例,多加序列号测试
**********************************************************
package my.test;
//@SuppressWarnings("deprecation")
//@Deprecated
interface InterfaceTest {
	void interfaceMethod_A();
	void interfaceMethod_B();
}
abstract class ExtendsTest<C,D> {
	public  void extendsMethod_A(){}
	public static void extendsMethod_B(){}
	public static void extendsMethod_C(){}
	public final void extendsMethod_D(){}
	
	protected void extendsMethod_E(){}
	public void extendsMethod_F(){}
	
	public Integer extendsMethod_G(){ return 1; }
	public void extendsMethod_H(){}
	
	public void extendsMethod_I() throws Throwable {}
	
	public void extendsMethod_J(int[] i){}
	
	public void extendsMethod_K(int... i){}
	
	@Deprecated
	public void extendsMethod_L(){}
}

public class Test<T> extends Throwable implements InterfaceTest {
	//static final long serialVersionUID=9;
	public static void extendsMethod_A(){}
	public static void interfaceMethod_A(){}
	
	public void extendsMethod_B(){}
	public static void extendsMethod_C(){}
	public void extendsMethod_D(){}
	
	private void interfaceMethod_B(){}
	void extendsMethod_E(){}
	protected void extendsMethod_F(){}
	
	public int extendsMethod_G(){ return 1; }
	public int extendsMethod_H(){ return 1; }
	
	public void extendsMethod_I() throws Error{}
	
	public void extendsMethod_J(int... i){}
	
	public void extendsMethod_K(int[] i){}
	
	public void extendsMethod_L(){}
	public void extendsMethod_L(int i){}
}



**********************************************************
时间: 2007.05.23 08:07
用途: Gen测试
**********************************************************

package my.test;
public class Test<T> {
	//protected abstract class C<T> { int this$0 ;abstract T id(T x); }

    //class D extends C<String> { String id(String x) { return x; } }
    
    
    enum MyEnum {
    EXTENDS("? extends "),
    SUPER("? super "),
    UNBOUND("?");

    private final String name;

    MyEnum(String name) {
	this.name = name;
    }

    public String toString() { return name; }
    }

	
	public <V> void myMethod(int i,String s,int... ii) {
		final @Deprecated class MyLocalClass{}
		final int myMethodInt;

		//C c = new D()
    	//c.id(new Object()); // fails with a ClassCastException
    	
		//Flow阶段用于错误测试的例子
		
		{}
		int bbb=10;
		if(false) bbb++;
		else bbb--;
		
		int ccc=10;
		if(true) ccc++;
		else ccc--;
		
		int iii=10;
		if(iii>5) iii++;
		else iii--;
		
		boolean myBoolean=false;
		if(i<0) myBoolean=!myBoolean;
		
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
		
		while(i<10);
		
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
	}
}
