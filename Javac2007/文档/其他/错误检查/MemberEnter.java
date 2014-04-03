错误Key : pkg.clashes.with.class.of.same.name
错误提示: 
比如:如果包名是my.test,然后在my目录下有个test.java文件那么就会出现错误提示:
bin\mysrc\my\test\Test.java:1: 软件包 my.test 与带有相同名称的类冲突
package my.test;
^
错误理由: 子包名与类名不能相同(也就是子包名对应的子目录与类名所对应的文件不能在同一目录下)


错误Key : fatal.err.no.java.lang
错误提示: 致命错误：在类路径或引导类路径中找不到软件包 java.lang
错误理由: 在类路径或引导类路径中找不到软件包 java.lang


错误Key : doesnt.exist
错误提示: 
bin\mysrc\my\test\Test.java:3: 软件包 my2 不存在
import my2.*;
^
错误理由: 导入的软件包名必须能在类路径中找得到
