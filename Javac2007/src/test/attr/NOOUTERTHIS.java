package test.attr;
/*
class ClassA {}
	class ClassB extends ClassA{}
	class ClassC<T extends ClassA> {}

	class Test{
		void m222(ClassC<?>c,ClassC<? extends ClassB> c1,ClassC<? super ClassB> c2) {}
	}
*/
public class NOOUTERTHIS {
	//{this();} static {this();} //对 this 的调用必须是构造函数中的第一个语句

	int f1;
	class InnerClassA {
		int f2;
		InnerClassA(int i1,int i2) {
			//this(new InnerClassA(f1,f2) { void methodA(){}});

			<ClassA,ClassB>this(new InnerClassA(i1,i1) { int i=10;int ii=i;void methodA(){}});
			//this(new InnerClassA(i1,i1) { int i=10;int ii=i;void methodA(){}});
		}

		//InnerClassA(InnerClassA at) {}

		//<T extends ClassA & InterfaceA>InnerClassA(InnerClassA at) {}

		<TA extends ClassA & InterfaceA, TB extends TA>InnerClassA(InnerClassA at) {}
	}

	class ClassA implements InterfaceA {}
	interface InterfaceA {}
	//class ClassB extends ClassA implements InterfaceA {}

	//这个也是正确的，虽然 TB extends TA，并且TA extends ClassA & InterfaceA，
	//但是不要求与TB对应的实参extends ClassA & InterfaceA，
	//只要TB对应的实参extends自TA对应的实参就可以了，
	//像上面的例子:TA=ClassA，因为ClassB extends ClassA，所以TB=ClassB是合法的
	//通过Types.subst([TA],[TA,TB],[ClassA,ClassB]).visitTypeVar(TA)得到[ClassA]
	//然后再Types.isSubtypeUnchecked(ClassB,ClassA)得到true
	class ClassB extends ClassA{}

	/*
	test\attr\NOOUTERTHIS.java:20: 找不到符号
	符号： 构造函数 <test.attr.NOOUTERTHIS.ClassA,test.attr.NOOUTERTHIS.ClassB>InnerClassA(<匿名 test.attr.NOOUTERTHIS.InnerClassA>)
	位置： 类 test.attr.NOOUTERTHIS.InnerClassA
							<ClassA,ClassB>this(new InnerClassA(i1,i1) { int i=10;int ii=i;void methodA(){}});
							^
	1 错误
	class ClassB implements InterfaceA {}
	*/



	/*
	NOOUTERTHIS() {
		//无法在调用父类型构造函数之前引用 this
		this(new InnerClassA() { int i=10;});
	}

	NOOUTERTHIS(InnerClassA at) {}
	*/

	//NOOUTERTHIS() { NOOUTERTHIS.super(); }
	//NOOUTERTHIS(int i) {}
}

/*
test\attr\NOOUTERTHIS.java:30: 非法限定符；java.lang.Object 不是内部类
class ClassA { ClassA() { ClassA.super(); } }
                                ^
1 错误
class ClassA { ClassA() { ClassA.super(); } } 
*/
