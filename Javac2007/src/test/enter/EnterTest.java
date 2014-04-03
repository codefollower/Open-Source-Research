package test.enter;


class EnterTest2 {
			void methodA() {
				class EnterTest22{}
				class EnterTest22{}
			}
		}

class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTestB & EnterTestC,TD> {
	class ClassA{}
	static class ClassB{
		class ClassB11{}

		void methodA() {
			methodB(new ClassA(){});
			class LocalClass11{}
		}

		void methodB(ClassA a){}
	}
	interface InterfaceA {}
	static interface InterfaceB {}

	static void methodA(ClassA a) {
		class LocalClass{}
	}
	void methodB() {
		methodA(new ClassA(){});
		class LocalClass{}

		//class EnterTest{}
	}
}
class EnterTestB {
	static class ClassB{}
}
interface EnterTestC {
	class ClassA{}
	static class ClassB{}
	interface InterfaceA {}
	static interface InterfaceB {}
}












/*
package my.test;
class EnterTest {
	void methodA() {
		class EnterTest{}
	}
}
*/
// <editor-fold defaultstate="collapsed">// </editor-fold>
//package test.enter;
//import java.util;
//import CurrentPathClass.*;
//import doesnt.exist.*;
//import my.*;
//import my.*;
//import my.*;
//import my.test.*;
//import my.test.*;
//import static java.lang;

//import static my.test.*; //用来测试importStaticAll(3)，当tsym.kind=ERR
//下面两条语句测试importStaticAll(3)与staticImportAccessible(2)
//import static my.test.ImportStaticTest.*;
//import static my.ImportStaticTest.*;

//import static my.test.ClassC.*;
//import static my.test.ClassE.*;
//import static my.test.ClassF.*;
//import static my.test.ClassG.Class1;//错误:找不到符号
//import my.test.ClassG.Class1;

//import my.test.EnterTestB;
//import static my.test.EnterTestB.ClassB;

//import static my.test.EnterTest.InnerInterface;
//import static my.test.EnterTest.InnerInterface;

//import static my.test.EnterTest.ClassB;
//import static my.test.EnterTestB.ClassB;

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
/*
class EnterTest<TA extends EnterTestB,TB extends EnterTestC,TC extends EnterTestB & EnterTestC,TD> {
	class ClassA{}
	static class ClassB{}
	static void methodA() {
		class LocalClass{}
	}
	void methodB() {
		class LocalClass{}
	}

	*/


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
    
    //public void m4(Object o) {
	//class LocalClass{};
    //}
    
    /**
     * 
     * @deprecated
     */
    
    //@SuppressWarnings({"fallthrough","unchecked"})
    //@Deprecated
    //public <T,V extends EnterTest,E extends V>void m5() {
        
        //m4(new Object(){ class EnterTest{} public void m2(){} }); //已在 my.test 中定义 my.test.EnterTest
    //}
	
	//interface InterfaceA {
	//private class InterfaceClassA{} //非法的修饰符组合：0x1 public 和 0x4 protected
	//protected class InterfaceClassA{} //非法的修饰符组合：0x1 public 和 0x4 protected
	//enum InterfaceEnumA{}
	//interface InterfaceInterfaceA{}
		
	//}
//}

/*
class EnterTestB {
	static class ClassB{}
}
interface EnterTestC {}
//class EnterTest {}
//interface EnterTest {}

*/