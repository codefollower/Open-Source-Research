13: 
调用com.sun.tools.javac.comp.Check===>checkClassBounds(2)方法，
// Check that class does not import the same parameterized interface
        // with two different argument lists.

编译器会报一个关键字为“cant.inherit.diff.arg”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest<A extends Number> {}
class ExtendsTest implements InterfaceTest<Integer>{}
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>{}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_inherit_diff_arg.java:4: 无法使用以下不同的参数继承 my.error.InterfaceTest：<java.lang.Float> 和 <java.lang.Integer>
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>{}
                                   ^
1 错误
*/
--------------------------------------------------------------------