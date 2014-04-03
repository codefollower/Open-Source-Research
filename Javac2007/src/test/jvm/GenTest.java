// <editor-fold defaultstate="collapsed">// </editor-fold>
package test.jvm;

public abstract class GenTest<T> {
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
    }
	*/

	//void methodA(){}
	//abstract void methodB();
	/*
	int methodC(boolean b1,boolean b2) {
		//if(b1 && b2) return 0;
		//return 1;
		//return b1 ? 1 : b2 ? 2 : 3;
		//int iii=(b1 ? 1 : b2 ? 2 : 3);
		//return iii;
		//if(b1 ? b1:b2) return 1;
		//if(b1 ? true:false) return 2;
		//if(b1 ? false:true) return 3;return 4;
		while(b1 ? b1:b2) return 1;
		return 2;
	}
	

	void methodD(boolean b1) {
		while(b1) b1=false;

		for(;;) break;

		synchronized (s) {}
	}

	void methodE(String s) {
		synchronized (s) {}
	}

	void methodF(int i) {
		try {
			i++;
		}
		catch(RuntimeException e) {}
		catch(Exception e) {}
		//catch(NoSuchFieldException e) {}
		finally {
			i=0;
		}
	}

	boolean methodG(boolean b1) {
		while(b1) {
			try {
				if(b1) continue;
				return true;
			}
			finally {
				break;
			}
		}
		return false;
	}

	void methodH(int iii) {
		while(true) {
			iii++;
			if(iii<10) continue;
			break;
		}
	}

	static int methodI(boolean b1) {
		try {
			if(b1) return 1;
			return 0;
		}
		finally {
			//b1=true;
			System.out.println("byby");
		}
	}

	void methodJ(int iii) {
		lable1:
		while(true) {
			try {
				iii++;
				if(iii<10) continue;
				//break;
				return;
			} finally {
				continue lable1;
			}
		}
	}

	void methodK(int iii) {
		lable1:
		do {
			try {
				//lable2:
				for(int iii2=0;iii2<5;iii2++) {
					try {
						while(true) {
							try {
								iii++;
								//if(iii2/iii<10) continue;
								if(iii2/iii<10) continue lable1;
								break lable1;
							} catch(ArithmeticException e) {
								iii=0;
								return;
							} finally {
								iii=1;
							}
							//return;
						}
					} finally {
						iii=2;
					}
				}
			} finally {
				iii=3;
				//continue lable1;
			}
		} while(true);
	}

	void methodL(boolean b1,int iii) {
		lable1:
		do {
			try {
				lable2:
				for(;;) {
					try {
						while(true) {
							try {
								if(b1) continue lable2;
								iii=0;
								break lable1;
							} catch(ArithmeticException e) {
								iii=1;
								return;
							} finally {
								iii=2;
							}
							//return;
						}
					} finally {
						iii=3;
					}
				}
			} finally {
				iii=4;
				//continue lable1;
			}
		} while(true);
	}

	void method01() { //visitLiteral
		int i1;
		int [] i2;
		int [][] i3;

		i1=0;
		i2=null;
		i3=null;
	}*/

	//static int f;

	void method02() { //visitSelect
		//Class C=GenTest.class;
		//int i2=GenTest.f;

		//GenTest[][] gt=new GenTest[2][3];
		//int[][] int2=new int[4][5];
		//String[][][] strs=new String[6][7][8];

		
		int[] int3=new int[2];
		String[] strs2=new String[3];
		GenTest[] gt2=new GenTest[4];

	}
		
	
	//public <V> void myMethod(GenTest<V> gt,int i,double d,String s,int... ii) {
		//String str1=s+"string2";
		//if(i>0 && i<3) return;
		/*
		//final @Deprecated class MyLocalClass{}
		final int myMethodInt;
		final int myMethodInt2=100;
		int myMethodInt3=200;
		int myMethodInt4=-1;

		//C c = new D()
    	//c.id(new Object()); // fails with a ClassCastException
    	
		//Flow阶段用于错误测试的例子
		
		{}
		///*
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

		*/
	//}

	/*
    static int i=10;
    static final int i2=10;
    
    int fieldA=10;
    {
            fieldA=20;
    }

    {
            fieldB=20;
    }
    int fieldB=10;

    GenTest() {
    }

	public void methodA() {}
    
    class GenInnerclassTest {
        int fieldA=10;
        {
            int[] arrayA=new int[]{1,2};
            int b,c=0,d;
            fieldA=20;
            d=1;
            //fieldA=arrayA.length;
            //Class cd=int[].class;
            //Class cd=GenTest.class;
            GenTest.i=90;
        }
        
        GenInnerclassTest() {
            fieldA=30;
            
            //GenTest gt=new GenTest() { public void m(){} };
        }
    }
    
    //public void m4(Object o) {}
    //public void m5() {
        
        //m4(new Object(){ class LocalClass{} public void m2(){} });
    //}
	*/
                
}

/*
public abstract class GenTest implements GenTestInterfaceB {
    public abstract void interfaceMethodC();
}

interface GenTestInterfaceA {
    void interfaceMethodA();
}

//接中不能使用implements，但可以使用extends，且extends后可接多个名称
//interface GenTestInterfaceB implements GenTestInterfaceA {
interface GenTestInterfaceB extends GenTestInterfaceA {
    void interfaceMethodB();
    void interfaceMethodC();
    
    //static void interfaceMethodStatic();
}
*/




//import static java.lang;


//import static my.test.TopLevelClass.*;

//import static my.test.EnterTest.InnerInterface;
//import static my.test.EnterTest.InnerInterface;

//import my.test.EnterTest.InnerInterface;
//interface InnerInterface{}

//import static my.*;
//////////////import my.*;
//import my2.*;
//import static my.MyProcessor;
//import my.MyProcessor;
//////////////class EnterTestSupertype<E extends EnterTestSupertype<E>>{}
//final class EnterTestFinalSupertype{}
//////////////interface EnterTestInterfaceA{}
//////////////interface EnterTestInterfaceB{}
//@interface EnterTestAnnotation extends EnterTestInterfaceA,EnterTestInterfaceB{}
//enum EnterTestEnum{}
//public class EnterTest<T,S> extends T implements EnterTestInterfaceA,EnterTestInterfaceB {
//public class EnterTest<T,S> extends EnterTestInterfaceA {
//public class EnterTest<T,S> extends EnterTestFinalSupertype {
//public class EnterTest<T,S> extends EnterTestEnum {
//////////////@Deprecated class EnterTest<T,S extends EnterTestInterfaceA> extends EnterTestSupertype implements EnterTestInterfaceA,EnterTestInterfaceB {
//@Deprecated 

 
//public interface GenTest {    
    /*
    public static interface InnerInterface<T extends EnterTest> {
        void m2();
    }
    public void m1(InnerInterface ii) {
        class LocalClass{}
    }
    
    public void m3() {
        m1(new InnerInterface(){ public void m2(){} });
    }*/
    
    //public void m4(Object o) {}
    
    /**
     * 
     * @deprecated
     */
    
    //@SuppressWarnings({"fallthrough","unchecked"})
    //@Deprecated
    //public <T,V extends EnterTest,E extends V>void m5() {
        
        //m4(new Object(){ class LocalClass{} public void m2(){} });
    //}
//}