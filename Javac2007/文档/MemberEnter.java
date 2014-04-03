以下说到的“类型”是指:
类(Class)、接口(Interface)、注释(Annotation)、枚举类(Enum)
1.从com.sun.tools.javac.comp.MemberEnter===>complete(Symbol sym)开始，
如果sym是一个最外层的类，转到1.2

1.2 com.sun.tools.javac.comp.MemberEnter===>visitTopLevel(1)
如果在同一源文件中，在最外层同时声明了N个(N>=2)类型:
-----------------------------
package test.memberEnter;

public class ClassA{}
class ClassB{}
interface InterfaceA{}
...........
-----------------------------
那么在visitTopLevel(1)中只处理ClassA，对于ClassB，InterfaceA直接返回

visitTopLevel(1)处理流程:
1) 包名与类名冲突检查，
例:
-----------------------------
package test.memberEnter.clash1.clash2;
public class ClashTest {}
-----------------------------
假设-classpath是E:\javac，并有下列文件:
E:\javac\test.java
E:\javac\test\memberEnter.java
E:\javac\test\memberEnter\clash1.java
E:\javac\test\memberEnter\clash1\clash2.java
不管上面的文件内容是什么，
当编译ClashTest.java时，会报错:
------------------------------------
test\memberEnter\clash1\clash2\ClashTest.java:1: 软件包 test.memberEnter.clash1.
clash2 与带有相同名称的类冲突
package test.memberEnter.clash1.clash2;
^
1 错误
------------------------------------
对于clash1.java、memberEnter.java也产生包名与类名冲突，但由于
“package test.memberEnter.clash1.clash2;”对于的JCTree的开始位置(pos)都是同一个，
所以在log.error(...)时只报告一次，但是对于E:\javac\test.java却是合法的，
虽然它与包名中的“test”同在E:\javac这个目录下，
但是编译器只检查包名中第一个“.”号之后的子包名是否在对应的子目录与类名冲突。
如果包名没有“.”号，如“package test”或者一个类没有指定package，
那么都不检查目录与类名冲突,对于目面的例子:
package test.memberEnter.clash1.clash2;
除了test外，memberEnter、clash1、clash2都要检查

2) 调用com.sun.tools.javac.comp.MemberEnter===>annotateLater(3)
如果编译的文件是package-info.java，并且它有包注释， 
则在Annotate的“ListBuffer<Annotator> q = new ListBuffer<Annotator>();”中
保存下来先，留待以后处理，
另外请注意，非package-info.java文件是不能有包注释的，在Enter中已检查了

3) 调用com.sun.tools.javac.comp.MemberEnter===>importAll(3)
将"java.lang"包中的所有类放入JCCompilationUnit toplevel.starImportScope，
注意:starImportScope是一个ImportScope。

4) 如果没有import语句，则visitTopLevel(1)方法结束，
否则处理处理import语句，转到1.3