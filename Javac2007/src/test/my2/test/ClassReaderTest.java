package my.test;
//import my.test.Test.*;
class ClassReaderTest2 extends ExtendsTest{
	
	Test<ClassReaderTest2,ClassReaderTest2> test=new Test<ClassReaderTest2,ClassReaderTest2>(this);
	Test<ClassReaderTest2,ClassReaderTest2>.D<Test<String,ExtendsTest>> d = test.new D<Test<String,ExtendsTest>>();

}
public class ClassReaderTest {
	ClassReaderTest2 test=new ClassReaderTest2();
	//Test<ClassReaderTest> test=new Test<ClassReaderTest>(this);
	public ClassReaderTest() {
		//test.method(12,12);
	}

	//public int myInt='\uuuuu5df2';
	public final int myInt2='\666';
	public final String str="\btr";
	//public final int myInt3;//\\uFFFlow阶段用于错误测试的例子
	public float myFloat=0x.1-1f;
}