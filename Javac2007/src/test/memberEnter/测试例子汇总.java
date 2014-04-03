测试例子汇总:
1:测试:visitTopLevel各4个import语句
--------------------------------------------
package test.memberEnter;


//import static test.memberEnter2.ClassE.*;
//import static test.enter.ClassE.*;
//import static test.enter.ClassE.*;//会重复加入starImportScope

//import static test.memberEnter.ClassE.*;

//import test.enter.ClassE.*;
//import test.memberEnter.ClassE.*;

//import test.enter.ClassE;
//import test.memberEnter.ClassE;
//import static test.memberEnter.ClassD.Class1;
//import test.memberEnter.*;
//import test.memberEnter.ClassF;

//import static test.memberEnter.ClassE.Class1;


//非静态导入一个private类，会转到AccessError-->report->report.access报错
//import test.memberEnter.ClassE.Class2;//private

//静态导入一个private类在importNamedStatic(4)不报错，也不加入namedImportScope
//但是在MemberEnter完之后在importNamedStatic(4)的enterAnnotation()报错
//import static test.memberEnter.ClassE.Class2;


class MemberEnterTest{}
--------------------------------------------



2:member enter超类、接口、类型变量
--------------------------------------------
package test.memberEnter;

@AnnotationA @Deprecated("args")
class MemberEnterTest<TA extends SuperClassA,TB extends InterfaceA,TC extends SuperClassA & InterfaceA,TD extends TA,TE extends InterfaceA&InterfaceB,TF extends TA&InterfaceA,TG extends SuperClassA & InterfaceA & TB, TH extends TH, TI> extends SuperClassA implements InterfaceA{
	/*
	class ClassA{}
	static class ClassB{}
	interface InterfaceA {}
	static interface InterfaceB {}

	static void methodA() {
		class LocalClass{}
	}
	void methodB() {
		class LocalClass{}
	}
	*/
}
class SuperClassA {
	//static class ClassB{}
}
interface InterfaceA {
	/*
	class ClassA{}
	static class ClassB{}
	interface InterfaceA {}
	static interface InterfaceB {}*/
}
interface InterfaceB{}
//@interface InterfaceB extends InterfaceA {}

@interface AnnotationA{}
