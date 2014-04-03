package test.attr;
//AttrTests10
//测试Attr中的visitApply

class VisitApplyTest {
	//VisitApplyTest() { super(); }

	class InnerClassB{}
	//class InnerClassA extends InnerClassB {
	class InnerClassA extends ClassA.InnerClass {
		InnerClassA() {
			//test\attr\AttrTests.java:17: 不兼容的类型
			//找到： test.attr.VisitApplyTest
			//需要： test.attr.ClassA
			//						b.super();
			//						^
			//b.super();
			
			//a.super();

			super();
		}
	}

	ClassA a;
	VisitApplyTest b;

	VisitApplyTest() {
		//b.super();
		this.<ClassA>m1(a);
	}
	<T> void m1(T t) {}
}

class ClassA {
	class InnerClass{}
}