package my.test;
import com.sun.tools.javac.util.*;

public class ListTest {
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        List<String> l=List.<String>fill(20,"123");
        System.out.println(l.size());
        System.out.println(l);
        System.out.println(List.convert(CharSequence.class,l));
        //System.out.println(List.convert(Integer.class,l));//java.lang.ClassCastException
        System.out.println(l.toArray(new String[10]).length);
        System.out.println(l.toArray(new String[30]).length);
        
        //Object[] o=l.toArray(new String[30]);
        //for(int i=0;i<o.length;i++)
            //System.out.println("i="+i+" "+o[i]);
        
        List<?> l2=List.from(new Integer[]{1,2,3,4});
        System.out.println(l2.get(0));
        System.out.println(l2.get(1));
        System.out.println(l2.get(2));
        System.out.println(l2.get(3));
        //System.out.println(l2.get(-1));
        //System.out.println(l2.get(4));
        
        java.util.List<?> l3=l2.subList(0,3);
        System.out.println(l3);
        
        com.sun.tools.javac.processing.PrintingProcessor pp=new com.sun.tools.javac.processing.PrintingProcessor();
        //javax.annotation.processing.SupportedOptions so = pp.getClass().getAnnotation(javax.annotation.processing.SupportedOptions.class);
        //System.out.println(so);
        System.out.println(pp.getSupportedOptions());
        System.out.println(pp.getSupportedAnnotationTypes());
        
        List<String> l4=List.<String>nil();
        System.out.println(l4.size());
        l4.append(null);
        System.out.println(l4.size());
        l4.prepend("dfdf");
        System.out.println(l4.size());
        
        ListBuffer<String> l5=ListBuffer.<String>lb();
        System.out.println(l5.size());
        l5.append(null);
        System.out.println(l5.size());
        l5.prepend("dfdf");
        System.out.println(l5.size());
    }
}
/*
interface InterfaceA {
    void m1();
}
class ClassA {
    //下面三种方式都有编译错误
    //void m1() {}
    //private void m1() {}
    //protected void m1() {}
    
    public void m1() {}
}
class ClassB extends ClassA implements InterfaceA {
}
*/
