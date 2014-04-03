// <editor-fold defaultstate="collapsed">// </editor-fold>
package my.test;
import java.lang.annotation.*;
import my.test.InnerAnnotation.*;
import my.test.TopLevelClass.MemberClass;
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

class InnerAnnotation {
    MemberClass mc;
    //enum InnerEnum {
    //    EnumA,EnumB,EnumC
    //}
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface MyInnerAnnotation {
        byte BYTE();
        char CHAR();
        short SHORT();
        int INT();
        long LONG();
        float FLOAT();
        double DOUBLE();
        boolean BOOLEAN();
        String StringCLASS();
        //InnerEnum ENUM();
        
        ElementType ENUM();
        Class<?> Class();
        int[] ARRAY();
        
        AnnotationA ANNO();
        //MyInnerAnnotation ANNO();//不能有循环注释
        
    }}
public class Test<T> extends ExtendsTest implements InterfaceA {
	{
            //测试com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
            class LocalClassInInitBlock{}
	}
	protected abstract class C<T> { int this$0 ;abstract T id(T x); }
	class D extends C<String> { String id(String x) { return x; } }
    
	protected static int fieldStatic=10;
	public int fieldA=10;
	
	int fieldB=10;
        
        @InnerAnnotation.MyInnerAnnotation (
            BYTE=1,
            CHAR=2,
            SHORT=3,
            INT=3,
            LONG=5,
            FLOAT=6.0F,
            DOUBLE=7.0D,
            BOOLEAN=true,
            StringCLASS="8",
            //ENUM=InnerAnnotation.InnerEnum.EnumA,
            ENUM=ElementType.FIELD,
            Class=InnerAnnotation.MyInnerAnnotation.class,
            ARRAY={1,2,3},
            ANNO=@AnnotationA("9")
        )
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
        
        int[][] myArray;
	
	TestD<? extends TestC> testD;
        
        //测试com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
        Object o=new Object() { public void m(){}};
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
            //测试com.sun.tools.javac.jvm.ClassWriter===>writeEnclosingMethodAttribute(1)
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
        
// <editor-fold defaultstate="collapsed">     
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
// </editor-fold>   
}