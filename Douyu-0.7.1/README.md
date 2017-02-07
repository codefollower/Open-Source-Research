Douyu
=====

一种新颖的将编译器、HTTP服务器、MVC框架、ORM框架有效结合的Java开发平台

开发测试环境搭建
=====
[Douyu开发测试环境搭建](https://github.com/codefollower/Douyu/wiki/Douyu%E5%BC%80%E5%8F%91%E6%B5%8B%E8%AF%95%E7%8E%AF%E5%A2%83%E6%90%AD%E5%BB%BA)

1. 要生成eclipse工程请运行:
=====
eclipse.bat

在eclipse中所有文件的编码要用UTF-8.



2.打包发布请运行:
=====
package.bat

打包后的文件放在target目录中:
target\douyu-x.y.z.jar

(注: x.y.z是实际的版本号)




3. 每次更新版本时要修改下面这些文件:
=====
package.bat (对应set version=那一行)
pom.xml (对应<douyu.version>那一行)



4. 要想得到所有的依赖jar包，运行:
=====
assembly.bat 或
mvn dependency:copy-dependencies
