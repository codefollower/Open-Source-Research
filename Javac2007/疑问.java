(1)
为什么
List<JCAnnotation> packageAnnotations = List.nil();
不用在 List.nil() 中上 List.<JCAnnotation>nil().


(2)
这两句
ParserTest(final /** @deprecated */ int i){}
ParserTest(/** @deprecated */ final int i){}
编译结果不一样，前一句提示已过时，后一句没有提示

(3)
LetExpr怎么来的？？？？？？？？

(4)
------------------------------------------------------------
test\enter\EnterTest.java:3: 警告：[deprecation] test.enter 中的 test.enter.Ente
rTestB 已过时
class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTest
B & EnterTestC,TD> {
                           ^
test\enter\EnterTest.java:3: 警告：[deprecation] test.enter 中的 test.enter.Ente
rTestB 已过时
class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTest
B & EnterTestC,TD> {
                                                                       ^
2 警告
------------------------------------------------------------
package test.enter;

class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTestB & EnterTestC,TD> {
	class ClassA{}
	static class ClassB{}
	static void methodA() {
		class LocalClass{}
	}
	void methodB() {
		class LocalClass{}
	}

	/**
     * 
     * @deprecated  //这个东西在读进"}"后没有清除，遗留给了EnterTestB
     */
}
class EnterTestB {
	static class ClassB{}
}
interface EnterTestC {}