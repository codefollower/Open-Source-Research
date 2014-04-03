package test.jvm;

//java.lang.ArrayIndexOutOfBoundsException: 65520
//at com.sun.tools.javac.jvm.ClassReader.sigToTypeParam(ClassReader.java:971)
//import test.jvm.LoadClassTest.ClassA;

import static test.jvm.LoadClassTest.ClassA;

public class ClassReaderTest extends ExtendsClass implements InterfaceA{
	ClassReaderTest(int a,int b) {
		ClassA<LoadClassTest.ClassB,ClassReaderTest> lct =new ClassA<LoadClassTest.ClassB,ClassReaderTest>();
		lct.m2();

		LoadClassTest.ClassB lctb=new LoadClassTest().new ClassB();
		lctb.m3();
		//int[][] a1;
		//int a=0,b=0;

		//a=10;
		//a+b>0 ? 0 :1;//不是语句
		
		//a=a+(b&a)/b-++a/b > 0 ? 0 :1;
		//a=a+(b&a)/b-++a/b > 0 ? b=10 :1;

		//意外的类型(变成赋值语句了 [ a+(b&a)/b-++a/b > 0 ? b=10 : b ] = 20  ) 
		//a=a+(b&a)/b-++a/b > 0 ? b=10 : b=20;



		//a=a|b<<a^b;

		//String str="A"+"B"+'c';
		//str="A"+"B"+"c";
	}
}