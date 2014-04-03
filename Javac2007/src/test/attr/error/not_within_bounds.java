package test.attr.error;
/*class ClassA {}
class ClassB extends ClassA {}
class ClassC extends ClassB {}
class ClassD extends ClassC {}

class ExtendsTest
<A extends ClassC, B extends ClassC, C extends ClassC, D extends ClassC> {}

public class not_within_bounds 
<T extends ExtendsTest<?, ClassB, ? extends ClassA, ? super ClassB>> {}
*/
/*
bin\mysrc\my\error\not_within_bounds.java:11: 类型参数 my.error.ClassB 不在其限
制范围之内
<T extends ExtendsTest<?, ClassB, ? extends ClassA, ? super ClassB>> {}
                          ^
bin\mysrc\my\error\not_within_bounds.java:11: 类型参数 ? super my.error.ClassB
不在其限制范围之内
<T extends ExtendsTest<?, ClassB, ? extends ClassA, ? super ClassB>> {}
                                                            ^
2 错误
*/


class ClassA {}
class ClassB extends ClassA {}
class ClassC extends ClassB {}
class ClassD<T> extends ClassC {}

class ExtendsTest
<
A extends ClassB,		//1
B extends ClassB,		//2
C extends ClassB,		//3
D extends ClassB,		//4
E extends ClassB,		//5
F extends ClassB		//6
> {}

class not_within_bounds2 <T extends ExtendsTest
<
?, 						//1
ClassC,					//2
? extends ClassA,		//3
? extends ClassC,		//4
? extends ClassA,		//5    ExtendsTest 编译错误 ClassA,ClassB,ClassC 正确
? super ClassC			//6    ClassA 编译错误
>> {}

public class not_within_bounds<T extends not_within_bounds2<ExtendsTest
<
ClassC,					//1 ClassA 编译错误,ClassB,ClassC 正确
ClassB,					//2
ClassB,					//3
ClassB,					//4
ClassC,					//5
ClassB					//6
>>> {

	public void myMethod() {
		ClassD<?> myClassD1 = new ClassD<String>();
		ClassD<?> myClassD2 = new ClassD<String>();
		
		aMethod(myClassD2); // 正确
		bMethod(myClassD2, myClassD1); // 非法
		cMethod(myClassD2, myClassD1); // 正确
		
	}
	
	public <T> void aMethod(ClassD<T> s) {}
	
	public <T> void bMethod(ClassD<T> s, T t) {}
	
	public <T> void cMethod(ClassD<? extends T> s, T t) {}
} 