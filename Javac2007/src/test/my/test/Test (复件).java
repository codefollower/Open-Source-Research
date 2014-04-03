package my.test;
/*
class Supertype1 {
	static void m222() {}
	int f222;
	
	void m333() {}
}

class Supertype2 extends Supertype1 {
	static void m222() {}
	private int f222;
	
	void m333() {}
}

public class Test extends Supertype2 {
	static void m222() {}
	
	int f222;
	
	void m333() {}
}

class Supertype{
	static void m222() {}
	private void m333() {}
}

public class Test extends my.test.isMemberOf.Supertype {
	protected int f111;
}*/
//public class Test<T extends ClassA & MyInterfaceA>{}

/*


class ClassA {}
class ClassB extends ClassA{}
class ClassC extends ClassB{}
class ClassD<T extends ClassB> {}

public class Test<V extends ClassA>{
	void m222(ClassD<? extends ClassC> c) {}
	
	void m333() {
		ClassD<ClassB> c=new ClassD<ClassB>();
		m222(c);
	}
}

class ClassA {}
class ClassB extends ClassA{}
class ClassC<T extends ClassA> {}
class ClassC1<T extends Test> {}
class ClassD extends ClassC1{}

public class Test{
Test() {
	ClassC1 c=(ClassC1)d;
}
ClassD d=new ClassD();
	void m222(ClassC<?>c,ClassC<? extends ClassB> c1,ClassC<? super ClassA> c2) {}
}


class ClassA<T> {}
//class ClassB extends ClassA{}
//class ClassC<T extends ClassA> extends ClassB {}
public class Test<T extends ClassA<? super String>,E extends T>{
//Test<? super ClassC<ClassB>> t=new Test<ClassB>();
}
*/
/*
@com.sun.tools.javac.util.Version("@(#)List.java	1.39 07/03/21")
public class Test<T extends Number>{
    class MyInnerClass<T extends Integer> {
        private Object data;
        
        public void setData(Test<? super T> data) {
            //assert !(data instanceof Test<?>) : this;
            this.data = data;
        }
        
        public void setData2(Test<? extends T> data) {
            //assert !(data instanceof Test<?>) : this;
            this.data = data;
        }
        
        //MyInnerClass() { this("str",123); }
        //MyInnerClass(String str,int i){}
    }
}
*/

public class Test {
    /*这段代码在javac1.6下编译没有错，但是在javac1.7下报错：
    src/my/test/Test.java:108: 无法访问 java.lang.Class
    错误的类文件： /home/zhh/java/jdk1.6.0_04/lib/ct.sym(META-INF/sym/rt.jar/java/lang/ClassLoader.class)
    错误的签名： <init>
    请删除该文件或确保该文件位于正确的类路径子目录中。
            ClassLoader loader = Test.class.getClassLoader();
            ^
    1 错误
    
    static {
	//getClassLoader()在java.lang.Class类中定义
	ClassLoader loader = Test.class.getClassLoader();
	//DEBUG.P("loader="+loader);
	if (loader != null)
		//loader.setDefaultAssertionStatus(false);//我加上的，没用
	    loader.setPackageAssertionStatus("my.test", true);//没用
    }*/
    
    public static void main(String... args) {
        assert args.length>2:args.length;
    }
}


