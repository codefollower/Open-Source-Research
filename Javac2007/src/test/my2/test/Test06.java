//@SuppressWarnings("package annotations")  		
package my.test;
import my.*;
import com.sun.tools.javac.util.Version;
;
@Version("@(#)Test.java	1.3 07/01/31")
@Deprecated
public class Test<S extends TestBound & MyInterfaceA, T> extends TestOhter<Integer,?,? super Object,? extends TestBound > implements my.test.MyInterfaceA,MyInterfaceB {
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
	
	{
		int i=0;
	}
	static {
		int i=2;
	}
	
	//Flow阶段用于错误测试的例子
	//Test() {
	//	this(2);//super();
	//}
	
	Test() throws Error, Exception {
		this(2);//super();
	}
	Test(int myInt) throws Error, Exception {
		//this.myInt=myInt;
		//myStaticInt1=1;
	}
	Test(float f) throws Exception {
		//myStaticInt1=1;
	}  
	
	<N extends T,S> void myMethod2(final N n,S s) throws Exception{
		int myMethodInt;
	}
	
	public <M extends T,S> int myMethod3(M m,S s)[] throws Exception,RuntimeException {
		int myMethodInt=10;
		return new int[0];
	}
	*/
	
	@Deprecated
	//public void myMethod(int i,String s,int... ii) throws Exception{
	public <M extends T,S> int[] myMethod(final M m,S[]<?> s[],int i,String s,int... ii)[] throws Exception,Error{
		{}
		/*//Flow阶段用于错误测试的例子
		if(true) ;
		if(false) ;
		int iii;
		if(iii+1/2*3-4>5) iii++;
		else iii--;
		
		boolean myBoolean;
		if(i<0) myBoolean=!myBoolean;
		*/
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
		
		final @Deprecated class MyClass{}
		final int myMethodInt;
		
		abstract class MyClass2{}
		strictfp class MyClass3{}
		
		//枚举类型不能为本地类型
		//enum MyEnum {}
		myLable: i++;
		
		//报错：意外的类型
		//++-10;
		
		//++--myInt;
		
		//注意ExtendsTestBound必须extends TestBound且同时implements MyInterfaceA
		//因为Test类的第一个形式参数S extends TestBound & MyInterfaceA
		//否则报错：类型参数 my.test.ExtendsTestBound 不在其限制范围之内
		Test<ExtendsTestBound,String> test=new Test<ExtendsTestBound,String>();
		
		int myIntArray[]=new int[10];
		myIntArray[1]=10;
		
		int[] myIntArray2={1,2};
		i++;
		myInt<<=2;
		
		return new int[0][0];
		//对于condition ? trueExpression : falseExpression语句
        //从这里可以看出falseExpression不能含有赋值运符符AssignmentOperator
        //但是trueExpression可以
		//myInt=(myInt>0) ? myInt=0:myInt=1;
	}
	/*
	public int myInt='\uuuuu5df2';
	public final int myInt2=10;
	//public final int myInt3;//Flow阶段用于错误测试的例子
	public float myFloat=0x.1p-1f;
	//public float 我的变量=" ";
	
	public static final int myStaticInt1='\377';
	//Flow阶段用于错误测试的例子
	//public static final int myStaticInt2;
	//public static final int myStaticInt3;
	//public static final int myStaticInt4;
	//public static final int myStaticInt5;
	
	S myS;
	T myT;
	public void method(){}
	public void method(int i){}
	*/
}
class MyTheSamePackageClass {
}
/*
//错误:此处不允许使用修饰符 private
private class MyTheSamePackageClass {
}
*/
