11: 如果当前类是一个非抽象类(flags_field字段不含有ABSTRACT | INTERFACE标志)，
调用com.sun.tools.javac.comp.Check===>checkAllDefined(2)方法检查这个非抽象类
是否含有抽象方法，以及实否实现了超类型中的所有抽象方法。
只要找到第一个没有实现的抽象方法，
编译器就会报一个关键字为“does.not.override.abstract”的错误，
后面如果即使还有未实现的抽象方法，也不再继续检查。

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract 不是抽象的，并且未覆盖 my.error.does_not_override_abstract 中的抽象方
法 innerAbstractMethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 错误
--------------------------------------------------------------------

如果把“abstract void innerAbstractMethod();”这一行注释掉，
编译器会接着检查超类“AbstractClass”，发现在超类“AbstractClass”中有一个
“abstractClassMethod()”的抽象方法，也同样报错

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract 不是抽象的，并且未覆盖 my.error.AbstractClass 中的抽象方法 abstractCl
assMethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 错误
--------------------------------------------------------------------

如果再把“abstract void abstractClassMethod();”这一行注释掉，
因为“AbstractClass”类实现了“InterfaceTest”接口，所以
编译器会接着检查“InterfaceTest”接口，发现在“InterfaceTest”接口中有一个
interfaceMethod()方法没被实现，也同样报错

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	//abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract 不是抽象的，并且未覆盖 my.error.InterfaceTest 中的抽象方法 interfaceM
ethod()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 错误
--------------------------------------------------------------------

如果再把“void interfaceMethod();”这一行注释掉，
因为“does_not_override_abstract”类实现了“InterfaceTest2”接口，所以
编译器会接着检查“InterfaceTest2”接口，发现在“InterfaceTest2”接口中有一个
interfaceMethod2()方法没被实现，也同样报错

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest{
	//void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	//abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\does_not_override_abstract.java:12: my.error.does_not_overrid
e_abstract 不是抽象的，并且未覆盖 my.error.InterfaceTest2 中的抽象方法 interface
Method2()
public class does_not_override_abstract extends AbstractClass implements Interfa
ceTest2 {
       ^
1 错误
--------------------------------------------------------------------