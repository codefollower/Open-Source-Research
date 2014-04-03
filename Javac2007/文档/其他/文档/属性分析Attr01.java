源文件:com.sun.tools.javac.comp.Attr.java

属性分析阶段的起点:
com.sun.tools.javac.comp.Attr===>attribClass(2)


属性分析阶段的详细流程

com.sun.tools.javac.comp.Attr===>attribClass(2)开始
--------------------------------------------------------------------
1: 调用com.sun.tools.javac.comp.Annotate===>flush()
这一步有可能在Enter阶段调用annotate.enterDone()完成，
annotate.flush()与MemberEnter中的几个匿名Annotate.Annotator()相关连


com.sun.tools.javac.comp.Attr===>attribClass(1)开始
--------------------------------------------------------------------
2: 调用com.sun.tools.javac.comp.Check===>checkNonCyclic(2)
检查当前类与当前类所有实现的接口、所有超类、owner之间是否
存在循环继承。存在循环继承时，
编译器会报一个关键字为“cyclic.inheritance”的编译错误
(备注：错误信息在com\sun\tools\javac\resources\compiler.properties中按
关键字设置，下文如果说到其他编译错误时，与这里所述类同)

如下源代码:
--------------------------------------------------------------------
package my.error;
public class cyclic_inheritance extends cyclic_inheritance2 {}
class cyclic_inheritance2 extends cyclic_inheritance {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\cyclic_inheritance.java:2: 循环继承涉及 my.error.cyclic_inheritance
public class cyclic_inheritance extends cyclic_inheritance2 {}
       ^
--------------------------------------------------------------------

如下源代码:
--------------------------------------------------------------------
package my.error;
public class cyclic_inheritance extends cyclic_inheritance {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\cyclic_inheritance.java:2: 循环继承涉及 my.error.cyclic_inheritance
public class cyclic_inheritance extends cyclic_inheritance {}
       ^
--------------------------------------------------------------------


如果当前类所有实现的接口、所有超类、owner在checkNonCyclic(2)前
已完成属性分析，那么在checkNonCyclic(2)后确认没有存在循环继承的
情况下，把ACYCLIC标志加进当前类所有实现的接口、所有超类、Owner
的flags_field字段中。


3: 调用com.sun.tools.javac.code.Types===>supertype(Type t)
找出当前类的直接超类，如果当前类所对应的ClassSymbol的flags_field字段中
没有COMPOUND标志，当前类的直接超类的tag是CLASS，那么先对当前类的直接超类
进行属性分析(由调用com.sun.tools.javac.comp.Attr===>attribClass(1)开始)，
同样的，如果当前类的owner的tag是CLASS，那么在紧接着对它进行同样的属性分析。


4: 判断当前类所对应的ClassSymbol的flags_field字段中是否有UNATTRIBUTED标志，
如果没有，说明当前类可能是其他类的超类或owner，在之前可能已进行过属性分析，
所以就不再对当前类进行属性分析，否则继续。


5: 执行flags_field &= ~UNATTRIBUTED，
去掉UNATTRIBUTED标志，注明已开始对当前类进行属性分析。


6: 从Enter的typeEnvs(一个HashMap)中提取当前类对应的env，并根据
类定义前的注释类型来设置env.info.lint，例如在当前类的类定义前有：
--------------------------------------------------------------------
@SuppressWarnings({"fallthrough","unchecked"})
@Deprecated
public class Test {...}
--------------------------------------------------------------------
那么当前类Test对应的env.info.lint如下:
env.info.lint=Lint:[values(8)[CAST, DEP_ANN, DIVZERO, EMPTY, FINALLY, OVERRIDES, PATH, SERIAL] suppressedValues(3)[DEPRECATION, FALLTHROUGH, UNCHECKED]]

上面的lint分两部分，values(8)部分表示编译器对这8种类型的警告不会屏蔽，一但发现就
向用户发出警告，suppressedValues(3)部分表示编译器不会对用户发出这三种警告。


7: 与枚举类相关的检查
1)在定义枚举类时不能使用“extends”关键字，也就是说不能人为去指定
一个枚举类的超类(见Parser.enumDeclaration(2)方法)

2)任何显示声明的类都不能从 java.lang.Enum 继承，
否则报一个关键字为“enum.no.subclassing”的编译错误

如下源代码:
--------------------------------------------------------------------
package my.error;
public class enum_no_subclassing extends Enum {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\enum_no_subclassing.java:2: 类无法直接继承 java.lang.Enum
public class enum_no_subclassing extends Enum {}
       ^
1 错误
--------------------------------------------------------------------

但是像如下的泛型类定义是合法的:
class MyTestA<T extends Enum> {}
class MyTestB<T extends Enum & Cloneable> {}

泛型类 MyTestA 的泛型变量T的上限绑定(upper bound)是Enum，
它并不是COMPOUND型的上限绑定，所以编译器不会为它单独生成一个ClassSymbol，
同样也不会单独对上限绑定进行属性分析；

但对于泛型类 MyTestB 来说，它的泛型变量T的上限绑定是“Enum & Cloneable”，
这是一个COMPOUND型的上限绑定，编译器会为它单独生成一个ClassSymbol，
而且这个ClassSymbol的flags_field＝ABSTRACT|PUBLIC|SYNTHETIC|COMPOUND|ACYCLIC，
这个ClassSymbol对应的ClassType的supertype_field＝java.lang.Enum,
interfaces_field=java.lang.Cloneable,编译器还会对这个COMPOUND型的上限绑定
对应的ClassSymbol进生属性分析，虽然它的超类是 java.lang.Enum ，但编译器允许
这种情况。
(有关COMPOUND型的上限绑定见com.sun.tools.javac.code.Types类的makeCompoundType(2)方法)



3)如果当前类不是枚举类型，但当前类的直接超类是枚举类型，
那么编译器会报一个关键字为“enum.types.not.extensible”的错误
(同时还有另一个错误)

如下源代码:
--------------------------------------------------------------------
package my.error;
enum MyEnum {}
public class enum_types_not_extensible extends MyEnum {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\enum_types_not_extensible.java:3: 无法从最终 my.error.MyEnum
进行继承
public class enum_types_not_extensible extends MyEnum {}
                                               ^
bin\mysrc\my\error\enum_types_not_extensible.java:3: 枚举类型不可继承
public class enum_types_not_extensible extends MyEnum {}
       ^
2 错误
--------------------------------------------------------------------


8: 开始对类体进行属性分析
com.sun.tools.javac.comp.Attr===>attribClassBody(2)开始
--------------------------------------------------------------------

8.1: 调用com.sun.tools.javac.comp.Check===>validateAnnotations(2)检查类注释

8.1.1: 调用com.sun.tools.javac.comp.Check===>validateAnnotation(2)

调用com.sun.tools.javac.comp.Check===>validateAnnotation(1)开始
--------------------------------------------------------------------

先检查注释成员值是否有重复，有重复，
则编译器会报一个关键字为“duplicate.annotation.member.value”的错误。

如下源代码:
--------------------------------------------------------------------
package my.error;
@interface MyAnnotation {
    String value();
}
@MyAnnotation(value="testA",value="testB")
public class duplicate_annotation_member_value  {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\duplicate_annotation_member_value.java:5: my.error.MyAnnotation 中的注释成员值 value 重复
@MyAnnotation(value="testA",value="testB")
                                  ^
1 错误
--------------------------------------------------------------------

然后检查是否为所有没有默认值的注释成员指定相应值，
否则，编译器会报一个关键字为“annotation.missing.default.value”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
@interface MyAnnotation {
    String valueA();
	String valueB() default "testB";
	String valueC();
}
@MyAnnotation(valueA="testA")
public class annotation_missing_default_value  {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\annotation_missing_default_value.java:7: 
注释 my.error.MyAnnotation 缺少 valueC
@MyAnnotation(valueA="testA")
^
1 错误
--------------------------------------------------------------------


最后，如果注释类型是 java.lang.annotation.Target 那么检查目标值是否重复，
否则，编译器会报一个关键字为“repeated.annotation.target”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.TYPE})
public @interface repeated_annotation_target {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\repeated_annotation_target.java:4: 注释目标重复
@Target({ElementType.TYPE,ElementType.TYPE})
                                     ^
1 错误
--------------------------------------------------------------------

调用com.sun.tools.javac.comp.Check===>validateAnnotation(1)结束
--------------------------------------------------------------------


8.1.2: 调用com.sun.tools.javac.comp.Check===>annotationApplicable(2)

调用com.sun.tools.javac.comp.Check===>annotationApplicable(2)开始
--------------------------------------------------------------------
先调用com.sun.tools.javac.code.Symbol$ClassSymbol===>attribute(Symbol anno)
来获得注释类型的java.lang.annotation.Target, 如果注释类型在定义时没有指
定 Target 那么attribute(Symbol anno)方法将返回null，否则返回指定的 Target.

例如对于注释类型“ java.lang.Deprecated ”
------------------------------------------
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Deprecated {
}
------------------------------------------
那么attribute(Symbol anno)方法将返回它的 Target 为null，
这就表示注释类型 Deprecated 可以用在源代码中的
任何程序元素(any program element)前(如:类、接口、方法、字段声明前面)


再例如对于注释类型“ java.lang.SuppressWarnings ”
----------------------------------------------------------------
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    String[] value();
}
----------------------------------------------------------------
attribute(Symbol anno)方法返回的 Target 就是:
Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
也就是说注释类型 SuppressWarnings 可以用在:
TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE这几种
程序元素声明前面，但它不能用在PACKAGE声明前面，

如下源代码:
--------------------------------------------------------------------
@SuppressWarnings({"fallthrough","unchecked"})
package my.error;
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
软件包注释应在文件 package-info.java 中
@SuppressWarnings({"fallthrough","unchecked"})
^
1 错误
--------------------------------------------------------------------
(备注:注释类型 SuppressWarnings 还可以用在ANNOTATION_TYPE声明前面，
虽然 Target 中没有显示指定ANNOTATION_TYPE，但显示指定了TYPE，指定了
TYPE，也就相当于指定了ANNOTATION_TYPE，
因为TYPE代表的是：类、接口、注释、枚举这四种声明)

备注:@SuppressWarnings({"fallthrough","unchecked"})不起作用，因为package-info不参加Attr

在annotationApplicable(2)方法中利用attribute(Symbol anno)方法返回的 Target
与当前Symbol的相关字段(kind、owner、flags_field)进行比较，如果存在不匹配的
情况，就返回false，否则返回true。

调用com.sun.tools.javac.comp.Check===>annotationApplicable(2)结束
--------------------------------------------------------------------


8.1.3: 在com.sun.tools.javac.comp.Check===>validateAnnotation(2)方法中
接收com.sun.tools.javac.comp.Check===>annotationApplicable(2)方法的返回值，
如果为false，编译器会报一个关键字为“annotation.type.not.applicable”的错误。

如下源代码:
--------------------------------------------------------------------
package my.test;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

@MyAnnotation("test")
public class annotation_type_not_applicable {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\annotation_type_not_applicable.java:9: 注释类型不适用于该类型的声明
@MyAnnotation("test")
^
1 错误
--------------------------------------------------------------------

因为在定义注释类型 MyAnnotation 的时候指定它的Target为FIELD与METHOD，
也就是说注释类型 MyAnnotation 只能用在字段和方法的声明前面，但是在上
面的例子中却用在了类定义前面了。


8.1.4: 如果在一个方法前使用了“@Override”注释标记，那么调用
com.sun.tools.javac.comp.Check===>isOverrider(Symbol s)检查该方法是否
覆盖或实现超类型的方法，如果该方法没有覆盖或实现超类型的方法，
那么isOverrider(Symbol s)返回 false ，
同时编译器会报一个关键字为“method.does.not.override.superclass”的错误。

(
备注:
这里的超类型是特指类继承树与实现树上关联的所有类与接口组成的一个闭包类型集合,
如下例中“method_does_not_override_superclass”类的超类型就是如下的类型集合:
[method_does_not_override_superclass、superClassTestB、superClassTestA、
InterfaceTest、java.lang.Object]
)

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest {
	void myOverrideMethodA(int i,char c);
}

abstract class superClassTestA implements InterfaceTest {
	public void myOverrideMethodB(int i,char c) {}
}

abstract class superClassTestB extends superClassTestA {
	public void myOverrideMethodC(int i,char c) {}
}

public class method_does_not_override_superclass extends superClassTestB {
	//下面三个方法的第二个参数与超类型中对应的三个方法的第二个参数不同，
	//所以使用“@Override”注释标记并不恰当，并没有真正达到覆盖的目的。
	@Override
	public void myOverrideMethodA(int i,byte b) {}

	@Override
	public void myOverrideMethodB(int i,byte b) {}

	@Override
	public void myOverrideMethodC(int i,byte b) {}
}
--------------------------------------------------------------------


编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\method_does_not_override_superclass.java:17: 方法不会覆盖或实现超类型的方法
        @Override
        ^
bin\mysrc\my\error\method_does_not_override_superclass.java:20: 方法不会覆盖或实现超类型的方法
        @Override
        ^
bin\mysrc\my\error\method_does_not_override_superclass.java:23: 方法不会覆盖或实现超类型的方法
        @Override
        ^
3 错误
--------------------------------------------------------------------

调用com.sun.tools.javac.comp.Check===>validateAnnotation(2) 结束
-------------------------------------------------------------------------

如果当前类还其他注释要检查，继续转到 8.1.1

否则，转到 9

调用com.sun.tools.javac.comp.Check===>validateAnnotations(2)结束
-------------------------------------------------------------------------


9: 调用com.sun.tools.javac.comp.Attr===>attribBounds(1)方法
如果当前类是一个泛型类，并且这个泛型类的泛型变量有COMPOUND型的上限绑定，
因编译器在属性分析阶段之前(MemberEnter阶段)已为COMPOUND型的上限绑定单独生成
一个ClassSymbol，现在得对这个ClassSymbol开始进行属性分析。






















