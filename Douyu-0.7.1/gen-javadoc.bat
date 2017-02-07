@echo off

javadoc -windowtitle "Douyu 0.7.1 API" -doctitle "Douyu 0.7.1 API Specification" -charset utf-8 -encoding utf-8 -classpath douyu-api\src\main\java;douyu-api\target\classes;E:\Tomcat7-SVN\target\classes -d apidocs -use -author -version -sourcepath douyu-api\src\main\java -subpackages douyu

