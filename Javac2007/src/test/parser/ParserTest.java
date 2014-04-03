/** ***********************
 ** 这里是package的JAVADOC
 ** @@deprecated(去掉一个@就可以测试checkNoMods(mods.flags);)
 ** ***********************
 */
//@PackageAnnotations //测试“软件包注释应在文件 package-info.java 中”
package test.parser;
//import static test.parser.ImportStaticTest.*;
//import my.* void abc catch//测试skip(checkForImports, false, false, false);
//import test.parser.ImportAllTest;
//; //在import语句后可以只有单个分号
//; //在import语句后可以只有单个分号
/** ***********************
 ** 这里是类的JAVADOC
 ** ***********************
 */
//strictfp只是为了测试modifiersOpt方法时能多看到一个modifier，可省略
//public strictfp ParserTest { //缺少class或interface或enum的情况

//缺少class或interface或enum的情况并测试errs = List.<JCTree>of(mods);
//public strictfp {

//接口定义是不能跟implements的，
//否则会在classOrInterfaceBody方法中报告“需要 '{'”
//interface ParserTest implements InterfaceA,InterfaceB {
@test.parser.PackageAnnotations(f1="str")
public strictfp class ParserTest<T extends ClassA & InterfaceA, V> extends ClassB implements InterfaceA,InterfaceB {
	//;//单个分号也是合法的语句
	/** ***********************
	 ** 这里是成员类的JAVADOC
	 ** ***********************
	 */
	class MemberClassA {}

	//void fieldA;//有错: 需要 '('
	//测试checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
	//ParserTest(/** @deprecated */ final public int i){}

	//注意下面两句的编译结果是不一样的
	//ParserTest(final /** @deprecated */ int i){} //有错
	//ParserTest(/** @deprecated */ final int i){} //无错

	//为什么可以用String s1[]与String[] s1表示同样的功能呢？
	//把[]放在变量的后面，就可以用一条声明语句定义字符串数组和字符串变量
	//public String s1[],s2="123";
	//要是把[]放在String的后面，我想把s2当成是一个字符串变量，
	//则下面的声明语句是错误的(但并不是在语法分析阶段检查该错误
	//public String[] s1,s2="123";

	//测试variableInitializer()与arrayInitializer(2)
	//public int[][] i1={{1,2},{3,4}};

	//ParserTest(String[] s[]){} //相当于二维数组String[][] s
	//虽然String...也可看成是可变数组String[]，但下面是不合法的，
	//这是语言规范所定，至于为什么有这规定，有待研究？
	//ParserTest(String... s[]){}

	//因为方法参数中的数组类型参数是不能指定数组大小的
	//ParserTest(String[99] s[]){}

    //类似下面的语法也可以(返回值是数组的话,[]可以放在右括号')'后面):
	//等价于public int[] myMethod() { return new int[0]; }
	//public int myMethod()[] { return new int[0]; }

	//抛出范形类的语法错误
	//public void methodA() throws ParserTest<ClassA,ClassB>{}

	//如果接口中的方法有方法体并不在语法分析时检查
	//interface MemberInterfaceA {
	//	void methodA(){};
	//}

	//测试enumBody(Name enumName)只有逗号和分号的情况
	//enum MemberEnumA {
	//	,
	//		;
	//}

	/*
	enum MemberEnumB {
		S1("s1") {
			public String toString() {
				return "s1";
			}
		},
		S2("s2");
		String s;
		MemberEnumB(String s) { this.s=s; }
	}
	*/

	/*
	class MemberClassB {
		void methodA(){
			final @Deprecated int localVariableA;
			//enum LocalEnumA {} //枚举类型不能为本地类型
		}
	}*/

	//Class<?> c;
	//Class c=int[][].class;
	//Class c=int[][].classA;
	//Class c=int[][].char;
	//Class c=int[][];
	//Class c=int[][].123;
	//Class c=ParserTest[][].class;
	
	//{ int a1[]={1,2}, a2; a1[0]=3; a2=a1[1]; }

	/*
	static class MemberClassB {
		static <R> R methodA(R r) { return r; }
	}
	
	{ MemberClassB.methodA(this); }
	{ MemberClassB.methodA("str"); }
	{ MemberClassB.<ParserTest>methodA(this); }
	{ MemberClassB.<String>methodA("str"); }
	*/

	/*
	class MemberClassC {
		//{ ParserTest.this(); }
		//{ ParserTest pt=ParserTest.this; }
	}
	*/

	/*
	int superField;
	<T> ParserTest(T t){}
	static <T> void methodB(T t){}
	class MemberClassD extends ParserTest {
		MemberClassD() { <String>super("str"); }
		{ int sf=MemberClassD.super.superField; }
		{ MemberClassD.super.<String>methodB("str"); }
	}
	*/

	/*
	class MemberClassE {
		class MemberClassF<T> {
			<T> MemberClassF(T t){}
		}
	}
	{
		MemberClassE me=new MemberClassE();
		MemberClassE.MemberClassF<Long> mf=me.new <String>MemberClassF<Long>("str");
		//类型的格式不正确，缺少某些参数(在Check类中检查)
		//MemberClassE.MemberClassF mf=me.new <String>MemberClassF<Long>("str");
	}
	*/

	//class MemberClassG<T> {<T> MemberClassG(T t){}}
	//{ MemberClassG[] mg=new <Long>MemberClassG<String>[]{};}
	

	//expr=<?>;

	/*
	class MemberClassH<T> {}
	MemberClassH<?> Mh1;
	MemberClassH<String> Mh2;
	MemberClassH<? extends Number> Mh3;
	//MemberClassH<? mh;
	//MemberClassH<? <;
	//MemberClassH<?>=null;
	*/

	//{ int i=1;i=-90; }

	//class MemberClassI<T> {}
	//MemberClassI<int> mi;

	//float f1=1.1E-33333f;
	//float f2=1.1E+33333f;
	
	
	//{ this.<ParserTest>methodA(this); }

	//int i=1.2E+45w;
	//int a我爱中国;//\\uD800\\u007\\;


		//无法从静态上下文中引用非静态 类型变量的限制范围 T
	//public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,String s2,int... ii)[] throws Exception,Error{
		//int field=Test.this.field;
	@Deprecated
	public <M extends T,S> int[] myMethod(final M t,S[] s[],int i,String s2,ClassB<ClassA> cb,int... ii)[] throws Exception,Error{
		//int a[]=new int[2]{1,2};
		//int b[][]=new int[2][];
		//int c[][][]=new int[2][][3];

		//cannot create array with type arguments
		//ClassB<ClassA>[] pt=new <ClassA>ClassB<ClassA>[0];

		//MemberClassA mca=new ParserTest().new MemberClassA();
		
		//ClassB<?> c=(ClassB<?>)cb;
		//byte b=(byte)++i;
		
		//int i2=i++--++--;
		int[][] ii2={{1,2},{3,4}};
		int i2=ii2[1][2];

		/*
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
			case (0):
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
				ParserTest.this.field=10;
			}
		}
		//abstract class MyLocalClass2 {}
		//strictfp class MyLocalClass3 {}
		//interface MyLocalInterface {}//不允许

		*/
		return new int[0][0];
	}
	/*
	int field=10;
	static int staticfield=10;
	{
		field=20;
	}
	
	static{
		staticfield=20;
	}*/
}

class ClassA {}
class ClassB<T> {
	ClassB(){}
	<V> ClassB(V t){}
}
interface InterfaceA {
	//{}接口不能有语句块(block)
	//int f1; //需要 =
}
interface InterfaceB {}

//'\576'
//\u005Cupo0\;
//'8p



/*
//int \\u00G2\uDC00;
// <editor-fold defaultstate="collapsed">// </editor-fold>
//package my.test k

package my.test;
import my.*;
//public uj8 class ParserTest<T> {
public class ParserTest<T> {
	int \uD800\uDC00;

    int b1[]=new int[2]{1,2};
    int a[]=new int[]{1,2};
    int a[]=new int[];
    int a1[2][3]={1,2,3,4,5,6};
    int a2[][]={{1,2,3},{4,5,6}};
    int a3[][]=new int[2][3];
    int a4[][]=new int[][3]{1,2,3,4,5,6};
    int a5[][]=new int[][]{{1,2,3},{4,5,6}};
    
    int a1[]=new int[2];
    byte a2[][]=new byte[][]{{1,2,3},{4,5,6}};*/
    //ParserTest<String>[] ps=new ParserTest<String>[2];
    /////** @deprecated */
    //ParserTest(/** @deprecated */int i){
//	    \uD800\uDC00=99;
    //}
//}
