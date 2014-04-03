package test.attr;
/*
test\attr\VisitApplyTest.java:9: 需要包含 test.attr.ClassA.InnerClass 的封闭实例
                InnerClassA() { super(); }
                                ^
1 错误
*/
public class VisitApplyTest {
	//VisitApplyTest() { super(); }

	class InnerClassB{}
	//class InnerClassA extends InnerClassB {
	class InnerClassA extends ClassA.InnerClass {
		InnerClassA() { super(); }
	}
}

class ClassA {
	class InnerClass{}
}
