10: 调用com.sun.tools.javac.comp.Check===>validateTypeParams(1)方法
检查泛型类的所有类型参数(type parameters)

10.1: 调用com.sun.tools.javac.comp.Check$Validator===>visitTypeParameter(1)
先检查JCTypeParameter的bounds，JCTypeParameter的bounds有下列两大类:

下面这些是用例源代码
-----------------------------
interface InterfaceA{}
interface InterfaceB{}
class ClassA{}
-----------------------------

第一类:非COMPOUND型的上限绑定

1) 不带extends  如:class TestA<T>{}
2) 带extends，并且extends后面是类名      如:class TestB<T extends ClassA>{}
3) 带extends，并且extends后面是接口名    如:class TestC<T extends InterfaceA>{}
3) 带extends，并且extends后面是类型变量  如:class TestD<T,V extends T>{}


第二类:COMPOUND型的上限绑定(类型变量后面都必须有extends关键字)

1) 格式:  接口A & 接口B & ... & 接口N

   如:class TestE<T extends InterfaceA & InterfaceB>{}

2) 格式:  类名 & 接口A & 接口B & ... & 接口N

   如:class TestF<T extends ClassA & InterfaceA & InterfaceB>{}

COMPOUND型的上限绑定列表中，第一个绑定可以是类和接口，
从第二个绑定开始，必须是接口，类型变量不能出现在COMPOUND型的上限绑定列表中。


如下源代码:
--------------------------------------------------------------------
package my.error;
public class UpperBoundTest {
	interface InterfaceA{}
	interface InterfaceB{}
	class ClassA{}
	class ClassB{}

	class TestA<T>{}
	class TestB<T extends ClassA>{}
	class TestC<T extends InterfaceA>{}
	class TestD<T,V extends T>{}

	class TestE<T extends InterfaceA & InterfaceB>{}
	class TestF<T extends ClassA & InterfaceA & InterfaceB>{}

	//下面四个类无法编译通过
	class TestG<T extends ClassA & ClassB & InterfaceB>{}
	class TestH<T,V extends T & ClassA & InterfaceA>{}
	class TestI<T,V extends ClassA & T & InterfaceA>{}
	class TestJ<T,V extends ClassA & InterfaceA & T>{}
}
--------------------------------------------------------------------


编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\UpperBoundTest.java:17: 此处需要接口
        class TestG<T extends ClassA & ClassB & InterfaceB>{}
                                       ^
bin\mysrc\my\error\UpperBoundTest.java:18: 类型变量后面不能带有其他限制范围
        class TestH<T,V extends T & ClassA & InterfaceA>{}
                                    ^
bin\mysrc\my\error\UpperBoundTest.java:19: 意外的类型
找到： 类型参数 T26867996
需要： 类
        class TestI<T,V extends ClassA & T & InterfaceA>{}
                                         ^
bin\mysrc\my\error\UpperBoundTest.java:20: 意外的类型
找到： 类型参数 T20918341
需要： 类
        class TestJ<T,V extends ClassA & InterfaceA & T>{}
                                                      ^
4 错误
--------------------------------------------------------------------


每一个JCTypeParameter的bounds字段都是List<JCExpression>类型的，
如果一个JCTypeParameter没有绑定(如 class Test<T>)，那么bounds字段
是一个元素个数为0的List<JCExpression>，而不是bounds=null。
在com.sun.tools.javac.comp.Check===>visitTypeParameter(1)方法中先
调用com.sun.tools.javac.comp.Check===>validate(List<? extends JCTree> trees)方法
对List<JCExpression> bounds中的每一个JCExpression进行检查，
把每一个JCExpression都传递到validate(JCTree tree)方法中，
validate(JCTree tree)方法里再根据JCTree的不同子类调用不同的方法:

JCArrayTypeTree:
对应com.sun.tools.javac.comp.Check$Validator===>visitTypeArray(1)

JCWildcard:
对应com.sun.tools.javac.comp.Check$Validator===>visitWildcard(1)

JCFieldAccess:
对应com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)

JCTypeApply:
对应com.sun.tools.javac.comp.Check$Validator===>visitTypeApply(1)

其他JCTree的子类:
对应com.sun.tools.javac.comp.Check$Validator===>visitTree(1)(不做任何事的方法)


如果JCTypeParameter的其中一个绑定是JCFieldAccess型的JCTree，那么在
com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)方法中得检查
这个绑定不是在参数化的类型中选择静态类，
否则，编译器会报一个关键字为“cant.select.static.class.from.param.type”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
class ExtendsTest<T> {
	static class InnerStaticClass {}
}
public class cant_select_static_class_from_param_type
             <T extends ExtendsTest<String>.InnerStaticClass> {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_select_static_class_from_param_type.java:6: 无法从参数化
的类型中选择静态类
             <T extends ExtendsTest<String>.InnerStaticClass> {}
                                           ^
1 错误
--------------------------------------------------------------------


如果JCTypeParameter的其中一个绑定是JCFieldAccess型的JCTree，那么在
com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)方法中还得检查
这个绑定不是在参数化的类型中选择非参数化类(假定这个类是一个泛型类)，
否则，编译器会报一个关键字为“improperly.formed.type.param.missing”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
class ExtendsTest<T> {
	class InnerClass<V> {}
}
public class improperly_formed_type_param_missing 
             <T extends ExtendsTest<String>.InnerClass> {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\improperly_formed_type_param_missing.java:6: 类型的格式不正确
，缺少某些参数
             <T extends ExtendsTest<String>.InnerClass> {}
                                           ^
1 错误
--------------------------------------------------------------------






