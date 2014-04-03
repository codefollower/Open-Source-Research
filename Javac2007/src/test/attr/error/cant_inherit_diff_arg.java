package test.attr.error;
interface InterfaceTest<A extends Number> {}
class ExtendsTest implements InterfaceTest<Integer>{}
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>{}
/*
bin\mysrc\my\error\cant_inherit_diff_arg.java:4: 无法使用以下不同的参数继承 my.error.InterfaceTest：<java.lang.Float> 和 <java.lang.Integer>
public class cant_inherit_diff_arg<T extends ExtendsTest & InterfaceTest<Float>>
{}
                                   ^
1 错误
*/