package test.flow;

class MyException1 extends Exception{}
class MyException2 extends Exception{}
class MyException3 extends Exception{}
class MyException4 extends Exception{}
class Test {
	static int f1_static=10;
	final static int f2_static_final;
	final static int f3_static_final=10;
	static {
		int f4_static_block=10;

		//f3_static_final=20;//(不是在Flow阶段报错)无法为最终变量 f3_static_final 指定值
	}
	int m1(int i) {
		int localVar=10;
		return i;
	}
	void m2(int i) throws MyException4{}


					Test() {
						this(2);
					}
					int myInt;
					Test(int myInt) throws MyException1, MyException2,MyException3 {
						this.myInt=myInt;
					}
					Test(float f) throws MyException1, MyException4 {
					}

					//Test(long f){}

	//static class StaticClass{}
	/*
	Test aTest = new Test(){};

	void m() {
		class LocalClassA{}
		//LocalClassA aLocalClassA = new LocalClassA();
	}
	*/
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
// </editor-fold>  