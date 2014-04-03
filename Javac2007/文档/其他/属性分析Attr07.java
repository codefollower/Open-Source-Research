14: 

// Check that a generic class doesn't extend Throwable
编译器会报一个关键字为“generic.throwable”的错误

如下源代码:
--------------------------------------------------------------------
package my.error;
public class generic_throwable<T> extends Exception {}
--------------------------------------------------------------------

编译错误提示信息如下:
--------------------------------------------------------------------
bin\mysrc\my\error\generic_throwable.java:2: 泛型类无法继承 java.lang.Throwable
public class generic_throwable<T> extends Exception {}
                                          ^
1 错误
--------------------------------------------------------------------