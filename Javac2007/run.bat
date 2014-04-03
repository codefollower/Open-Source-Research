@echo off

rem java -classpath src;classes com.sun.tools.javac.Main @args.txt > myout.java 2>&1 

java -classpath src;classes com.sun.tools.javac.Main @args.txt > myout.java

rem java -Xbootclasspath/p:src;classes;test/jar/JarUnnamedPackage.jar -Xbootclasspath/a:src;classes -classpath src;classes com.sun.tools.javac.Main @args.txt > myout.txt

rem java -Xbootclasspath/p:src;classes -Xbootclasspath/a:src;classes -classpath src;classes com.sun.tools.javac.Main @args.txt > myout.txt

rem javac @args.txt > myout.txt
rem javac @args.txt

rem java -classpath src;classes com.sun.tools.javac.Main @args.txt > myout.java 2>&1

rem java -Xbootclasspath/p:src;classes -Xbootclasspath/a:src;classes -classpath src;classes com.sun.tools.javac.Main -help -J