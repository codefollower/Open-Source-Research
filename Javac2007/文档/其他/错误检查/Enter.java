错误Key : pkg.annotations.sb.in.package-info.java
错误提示: 软件包注释应在文件 package-info.java 中
错误理由: 只有以“package-info.java”命名的源文件才能有包注释，其他文件不允许

警告Key : pkg-info.already.seen
警告提示: 未知
警告理由: 未知


错误Key : class.public.should.be.in.file
错误提示: 
bin\mysrc\my\test\Test22.java:7: 类 Test 是公共的，应在名为 Test.java 的文件中声明
public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
extends my.ExtendsTest.MyInnerClassStatic {
       ^
错误理由: 如果一个类是public的，则源文件名需和类名一样


错误Key : duplicate.class
错误提示: 未知
错误理由: 未知

