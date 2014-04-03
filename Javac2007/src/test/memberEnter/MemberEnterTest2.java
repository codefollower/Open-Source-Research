package test.memberEnter;

//import static test.memberEnter.*; //用来测试importStaticAll(3)，当tsym.kind=ERR
//下面两条语句测试importStaticAll(3)与staticImportAccessible(2)
//import static test.memberEnter.ImportStaticTest.*;
//import static test.memberEnter.ImportStaticTest.*;

//import static test.memberEnter.ClassC.*;
//import static test.memberEnter.ClassE.*;
//import static test.memberEnter.ClassF.*;
//import static test.memberEnter.ClassG.Class1;//错误:找不到符号
//import test.memberEnter.ClassG.Class1;


//import test.memberEnter.ClassG;
//import test.enter.ClassG;
//import test.enter.*;
//import test.*;
//import test.memberEnter.*;

//import test.memberEnter.ClassH.ClassHMemberClassA;

/*
test\memberEnter\MemberEnterTest.java:14: 找不到符号
符号： 类 ClassB
public class MemberEnterTest extends ClassB {
                                     ^
1 错误
*/
//public class MemberEnterTest extends ClassB { //看不到内部类
	
public abstract strictfp class MemberEnterTest<A,B extends BClass,C extends BInterfaceA,D extends BClass & BInterfaceA & BInterfaceB> extends BClass implements BInterfaceA,BInterfaceB {
	class ClassA{
		//class MemberEnterTest{}//已在 test.memberEnter 中定义 test.memberEnter.EnterTest
		//class ClassA{}//已在 test.memberEnter.EnterTest 中定义 test.memberEnter.EnterTest.ClassA
	}
	//static class ClassB{}
	//class ClassC<T,V>{}
	/*
	//static <Y extends FindGlobalTypeInStar,Z extends ClassG, D extends MemberClassC, E extends MemberClassA,F extends MemberClassB,G extends ClassA,H extends A,L,M extends L,N extends BClass> N methodA(M m,N n,int i,MemberEnterTest... met) throws ExceptionA,ErrorA{
	static <Y extends FindGlobalTypeInStar,Z extends ClassG, D extends MemberClassC, E extends MemberClassA,F extends MemberClassB,G extends ClassA,H extends A,L,M extends L,N extends BClass> N methodA(M m,N n,int i,MemberEnterTest... met) throws ExceptionA,ErrorA{
		class LocalClass{}
		//return new BClass();
		return n;
	}
	*/

	///*
	static <L,M extends L,N extends BClass> N methodA(M m,N n,int i,MemberEnterTest... met) throws ExceptionA,ErrorA,L{
		final int i2=10;
		class LocalClass{}
		//return new BClass();
		return n;
	}
	//*/

	//strictfp public void methodB() {
	//	class LocalClass{}
	//}
	//static abstract void methodC();//非法的修饰符组合 abstract 和 static
	//abstract <T extends ClassC<BClass,BClass>, V extends MemberClassD, W extends ClassHMemberClassA> void methodC();
	

	//void methodD(int i) { int i; } //已在 methodD(int) 中定义 i
}
//private class BClass {} //此处不允许使用修饰符 private
class BClass {
	//class MemberClassA {}
	//private class MemberClassB {}
	//private class MemberClassC {}
	//class MemberClassD {}
}
strictfp interface BInterfaceA {
	//class MemberClassA {}
	//public class MemberClassB {}
	//strictfp void methodB();
}
interface BInterfaceB {}
class ExceptionA extends Exception{}
class ErrorA extends Error{}
//class ClassG{}
//class ClassH{
//	class ClassHMemberClassA {}
//}





/*
package test.memberEnter;
class EnterTest {
	void methodA() {
		class EnterTest{}
	}
}
*/
// <editor-fold defaultstate="collapsed">// </editor-fold>

//import java.util;
//import CurrentPathClass.*;
//import doesnt.exist.*;
//import my.*;
//import my.*;
//import my.*;
//import test.memberEnter.*;
//import test.memberEnter.*;
//import static java.lang;



//import test.memberEnter.EnterTestB;
//import static test.memberEnter.EnterTestB.ClassB;

//import static test.memberEnter.EnterTest.InnerInterface;
//import static test.memberEnter.EnterTest.InnerInterface;

//import static test.memberEnter.EnterTest.ClassB;
//import static test.memberEnter.EnterTestB.ClassB;

//import test.memberEnter.EnterTest.InnerInterface;
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
    /*
    @SuppressWarnings({"fallthrough","unchecked"})
    //@Deprecated
    public <T,V extends EnterTest,E extends V>void m5() {
        
        //m4(new Object(){ class EnterTest{} public void m2(){} }); //已在 test.memberEnter 中定义 test.memberEnter.EnterTest
    }
	*/
	//interface InterfaceA {
	//private class InterfaceClassA{} //非法的修饰符组合：0x1 public 和 0x4 protected
	//protected class InterfaceClassA{} //非法的修饰符组合：0x1 public 和 0x4 protected
	//enum InterfaceEnumA{}
	//interface InterfaceInterfaceA{}
		
	//}
//}
//class EnterTestB {
//	static class ClassB{}
//}
//interface EnterTestC {}
//class EnterTest {}
//interface EnterTest {}