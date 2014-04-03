// <editor-fold defaultstate="collapsed">// </editor-fold>
package my.test;


public class GenTest {
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