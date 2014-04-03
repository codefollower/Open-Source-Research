11: 注释类型定义不能使用extends，也不能有类型参数，否则
编译器就会报一个关键字为“cant.extend.intf.annotation”的错误，
以及一个关键字为“intf.annotation.cant.have.type.params”的错误。

如下源代码:
--------------------------------------------------------------------
package my.error;
interface InterfaceTest {}
public @interface cant_extend_intf_annotation extends InterfaceTest {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\cant_extend_intf_annotation.java:3: 对于 @interface，不允许 "extends"
public @interface cant_extend_intf_annotation extends InterfaceTest {}
                                                      ^
1 错误
--------------------------------------------------------------------

如下源代码:
--------------------------------------------------------------------
package my.error;
public @interface intf_annotation_cant_have_type_params<T> {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\intf_annotation_cant_have_type_params.java:2: @interface 不能
带有类型参数
public @interface intf_annotation_cant_have_type_params<T> {}
                                                        ^
1 错误
--------------------------------------------------------------------