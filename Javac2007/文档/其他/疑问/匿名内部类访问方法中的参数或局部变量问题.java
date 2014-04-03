//例子
package my.test;
public class Test {
	public static void main(String args[]) {
		System.out.println("Hello World!");
		main2("str");
		main3("str");
	}
	
	Test(String str) {}
	
	public static Test main2(String str) {
		return new Test("dfdf") {
			String out(){
				int i=0;
				return "dfdf";
			}
		};
	}
	
	public static Test main3(final String str) {
		return new Test("dfdf") {
			String out(){
				int i=0;
				return str;
			}
		};
	}
	/*
	bin\mysrc\my\test\Test.java:53: 从内部类中访问局部变量 str1；需要被声明为最终类型
	                                return str1+str2;
	                                       ^
	bin\mysrc\my\test\Test.java:53: 从内部类中访问局部变量 str2；需要被声明为最终类型
	                                return str1+str2;
	                                            ^
	2 错误
	*/
	
	/*
	如果在匿名内部类中访问方法中的参数(str1)或方法中定义的局部变量(str2)
	那么str1与str2都得声明为最终(final)类型。
	如果这个匿名内部类是通过new Test("dfdf")这样的方式产生的，
	那么编译器会把这个匿名内部类重新命名为Test$n，Test$n继承自Test，
	被Test$n引用的方法中的参数(str1)或方法中定义的局部变量(str2)必需是
	最终(final)类型，编译器把str1与str2都当成Test$n自己的最终(final)类型字段
	并将Test(String)改成Test$1(String,String,String)
	(注：按照Test$n引用的方法中的参数或方法中定义的局部变量的个数扩展构造方法)
	*/
	/*public static Test main4(String str1) {
		String str2="dfdf";
		return new Test("dfdf") {
			String out(){
				int i=0;
				return str1+str2;
			}
		};
	}*/
	public static Test main5(final String str1) {
		final int str2=3;
		return new Test("dfdf") {
			String out(){
				int i=str2;
				return str1+str2;
			}
		};
	}
	
	String out(){return "dfd";}
}

//javap -classpath bin\mybin -verbose my.test.Test$3>javapOut.txt
Compiled from "Test.java"
final class my.test.Test$3 extends my.test.Test
  SourceFile: "Test.java"
  SourceID: length = 0x2
   00 1E 
  CompilationID: length = 0x2
   00 20 
  EnclosingMethod: length = 0x4
   00 09 00 22 
  InnerClass: 
   final #8; //class my/test/Test$3
  minor version: 0
  major version: 50
  Constant pool:
const #1 = Field	#8.#35;	//  my/test/Test$3.val$str1:Ljava/lang/String;
const #2 = Method	#9.#36;	//  my/test/Test."<init>":(Ljava/lang/String;)V
const #3 = class	#37;	//  java/lang/StringBuilder
const #4 = Method	#3.#38;	//  java/lang/StringBuilder."<init>":()V
const #5 = Method	#3.#39;	//  java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
const #6 = Method	#3.#40;	//  java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
const #7 = Method	#3.#41;	//  java/lang/StringBuilder.toString:()Ljava/lang/String;
const #8 = class	#42;	//  my/test/Test$3
const #9 = class	#43;	//  my/test/Test
const #10 = Asciz	val$str1;
const #11 = Asciz	Ljava/lang/String;;
const #12 = Asciz	<init>;
const #13 = Asciz	(Ljava/lang/String;Ljava/lang/String;)V;
const #14 = Asciz	Code;
const #15 = Asciz	LineNumberTable;
const #16 = Asciz	CharacterRangeTable;
const #17 = Asciz	LocalVariableTable;
const #18 = Asciz	this;
const #19 = Asciz	;
const #20 = Asciz	InnerClasses;
const #21 = Asciz	Lmy/test/Test$3;;
const #22 = Asciz	x0;
const #23 = Asciz	out;
const #24 = Asciz	()Ljava/lang/String;;
const #25 = Asciz	i;
const #26 = Asciz	I;
const #27 = Asciz	SourceFile;
const #28 = Asciz	Test.java;
const #29 = Asciz	SourceID;
const #30 = Asciz	1180575812000;
const #31 = Asciz	CompilationID;
const #32 = Asciz	1180575813546;
const #33 = Asciz	EnclosingMethod;
const #34 = NameAndType	#44:#45;//  main5:(Ljava/lang/String;)Lmy/test/Test;
const #35 = NameAndType	#10:#11;//  val$str1:Ljava/lang/String;
const #36 = NameAndType	#12:#46;//  "<init>":(Ljava/lang/String;)V
const #37 = Asciz	java/lang/StringBuilder;
const #38 = NameAndType	#12:#47;//  "<init>":()V
const #39 = NameAndType	#48:#49;//  append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
const #40 = NameAndType	#48:#50;//  append:(I)Ljava/lang/StringBuilder;
const #41 = NameAndType	#51:#24;//  toString:()Ljava/lang/String;
const #42 = Asciz	my/test/Test$3;
const #43 = Asciz	my/test/Test;
const #44 = Asciz	main5;
const #45 = Asciz	(Ljava/lang/String;)Lmy/test/Test;;
const #46 = Asciz	(Ljava/lang/String;)V;
const #47 = Asciz	()V;
const #48 = Asciz	append;
const #49 = Asciz	(Ljava/lang/String;)Ljava/lang/StringBuilder;;
const #50 = Asciz	(I)Ljava/lang/StringBuilder;;
const #51 = Asciz	toString;

{
final java.lang.String val$str1;

my.test.Test$3(java.lang.String, java.lang.String);
  Code:
   Stack=2, Locals=3, Args_size=3
   0:	aload_0
   1:	aload_2
   2:	putfield	#1; //Field val$str1:Ljava/lang/String;
   5:	aload_0
   6:	aload_1
   7:	invokespecial	#2; //Method my/test/Test."<init>":(Ljava/lang/String;)V
   10:	return
  LineNumberTable: 
   line 59: 0

  CharacterRangeTable: length = 0x2
   00 00 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      11      0    this       Lmy/test/Test$3;
   0      11      1    x0       Ljava/lang/String;


java.lang.String out();
  Code:
   Stack=2, Locals=2, Args_size=1
   0:	iconst_3
   1:	istore_1
   2:	new	#3; //class java/lang/StringBuilder
   5:	dup
   6:	invokespecial	#4; //Method java/lang/StringBuilder."<init>":()V
   9:	aload_0
   10:	getfield	#1; //Field val$str1:Ljava/lang/String;
   13:	invokevirtual	#5; //Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
   16:	iconst_3
   17:	invokevirtual	#6; //Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
   20:	invokevirtual	#7; //Method java/lang/StringBuilder.toString:()Ljava/lang/String;
   23:	areturn
  LineNumberTable: 
   line 61: 0
   line 62: 2

  CharacterRangeTable: length = 0x2C
   00 03 00 00 00 01 00 00 FFFFFFF4 09 00 00 FFFFFFF4 10 00 01
   00 02 00 17 00 00 FFFFFFF8 05 00 00 FFFFFFF8 16 00 01 00 00
   00 17 00 00 FFFFFFF0 10 00 00 FFFFFFFC 04 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      24      0    this       Lmy/test/Test$3;
   2      22      1    i       I


}


//javap -classpath bin\mybin -verbose my.test.Test>javapOut.txt
Compiled from "Test.java"
public class my.test.Test extends java.lang.Object
  SourceFile: "Test.java"
  SourceID: length = 0x2
   00 2C 
  CompilationID: length = 0x2
   00 2E 
  InnerClass: 
   final #8; //class my/test/Test$1
   final #11; //class my/test/Test$2
   final #13; //class my/test/Test$3
  minor version: 0
  major version: 50
  Constant pool:
const #1 = Field	#47.#48;	//  java/lang/System.out:Ljava/io/PrintStream;
const #2 = String	#49;	//  Hello World!
const #3 = Method	#50.#51;	//  java/io/PrintStream.println:(Ljava/lang/String;)V
const #4 = String	#30;	//  str
const #5 = Method	#16.#52;	//  my/test/Test.main2:(Ljava/lang/String;)Lmy/test/Test;
const #6 = Method	#16.#53;	//  my/test/Test.main3:(Ljava/lang/String;)Lmy/test/Test;
const #7 = Method	#17.#54;	//  java/lang/Object."<init>":()V
const #8 = class	#55;	//  my/test/Test$1
const #9 = String	#58;	//  dfdf
const #10 = Method	#8.#59;	//  my/test/Test$1."<init>":(Ljava/lang/String;)V
const #11 = class	#60;	//  my/test/Test$2
const #12 = Method	#11.#61;	//  my/test/Test$2."<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #13 = class	#62;	//  my/test/Test$3
const #14 = Method	#13.#61;	//  my/test/Test$3."<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #15 = String	#63;	//  dfd
const #16 = class	#64;	//  my/test/Test
const #17 = class	#65;	//  java/lang/Object
const #18 = Asciz	main;
const #19 = Asciz	([Ljava/lang/String;)V;
const #20 = Asciz	Code;
const #21 = Asciz	LineNumberTable;
const #22 = Asciz	CharacterRangeTable;
const #23 = Asciz	LocalVariableTable;
const #24 = Asciz	args;
const #25 = Asciz	[Ljava/lang/String;;
const #26 = Asciz	<init>;
const #27 = Asciz	(Ljava/lang/String;)V;
const #28 = Asciz	this;
const #29 = Asciz	Lmy/test/Test;;
const #30 = Asciz	str;
const #31 = Asciz	Ljava/lang/String;;
const #32 = Asciz	main2;
const #33 = Asciz	(Ljava/lang/String;)Lmy/test/Test;;
const #34 = Asciz	main3;
const #35 = Asciz	main5;
const #36 = Asciz	str1;
const #37 = Asciz	str2;
const #38 = Asciz	I;
const #39 = Asciz	out;
const #40 = Asciz	()Ljava/lang/String;;
const #41 = Asciz	SourceFile;
const #42 = Asciz	Test.java;
const #43 = Asciz	SourceID;
const #44 = Asciz	1180575812000;
const #45 = Asciz	CompilationID;
const #46 = Asciz	1180575813562;
const #47 = class	#66;	//  java/lang/System
const #48 = NameAndType	#39:#67;//  out:Ljava/io/PrintStream;
const #49 = Asciz	Hello World!;
const #50 = class	#68;	//  java/io/PrintStream
const #51 = NameAndType	#69:#27;//  println:(Ljava/lang/String;)V
const #52 = NameAndType	#32:#33;//  main2:(Ljava/lang/String;)Lmy/test/Test;
const #53 = NameAndType	#34:#33;//  main3:(Ljava/lang/String;)Lmy/test/Test;
const #54 = NameAndType	#26:#70;//  "<init>":()V
const #55 = Asciz	my/test/Test$1;
const #56 = Asciz	;
const #57 = Asciz	InnerClasses;
const #58 = Asciz	dfdf;
const #59 = NameAndType	#26:#27;//  "<init>":(Ljava/lang/String;)V
const #60 = Asciz	my/test/Test$2;
const #61 = NameAndType	#26:#71;//  "<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #62 = Asciz	my/test/Test$3;
const #63 = Asciz	dfd;
const #64 = Asciz	my/test/Test;
const #65 = Asciz	java/lang/Object;
const #66 = Asciz	java/lang/System;
const #67 = Asciz	Ljava/io/PrintStream;;
const #68 = Asciz	java/io/PrintStream;
const #69 = Asciz	println;
const #70 = Asciz	()V;
const #71 = Asciz	(Ljava/lang/String;Ljava/lang/String;)V;

{
public static void main(java.lang.String[]);
  Code:
   Stack=2, Locals=1, Args_size=1
   0:	getstatic	#1; //Field java/lang/System.out:Ljava/io/PrintStream;
   3:	ldc	#2; //String Hello World!
   5:	invokevirtual	#3; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
   8:	ldc	#4; //String str
   10:	invokestatic	#5; //Method main2:(Ljava/lang/String;)Lmy/test/Test;
   13:	pop
   14:	ldc	#4; //String str
   16:	invokestatic	#6; //Method main3:(Ljava/lang/String;)Lmy/test/Test;
   19:	pop
   20:	return
  LineNumberTable: 
   line 4: 0
   line 5: 8
   line 6: 14
   line 7: 20

  CharacterRangeTable: length = 0x3A
   00 04 00 00 00 07 00 00 10 03 00 00 10 26 00 01
   00 08 00 0D 00 00 14 03 00 00 14 10 00 01 00 0E
   00 13 00 00 18 03 00 00 18 10 00 01 00 00 00 14
   00 00 0C 29 00 00 1C 02 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      21      0    args       [Ljava/lang/String;


my.test.Test(java.lang.String);
  Code:
   Stack=1, Locals=2, Args_size=2
   0:	aload_0
   1:	invokespecial	#7; //Method java/lang/Object."<init>":()V
   4:	return
  LineNumberTable: 
   line 9: 0

  CharacterRangeTable: length = 0x10
   00 01 00 00 00 04 00 00 24 13 00 00 24 14 00 02
   
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      5      0    this       Lmy/test/Test;
   0      5      1    str       Ljava/lang/String;


public static my.test.Test main2(java.lang.String);
  Code:
   Stack=3, Locals=1, Args_size=1
   0:	new	#8; //class my/test/Test$1
   3:	dup
   4:	ldc	#9; //String dfdf
   6:	invokespecial	#10; //Method my/test/Test$1."<init>":(Ljava/lang/String;)V
   9:	areturn
  LineNumberTable: 
   line 12: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 09 00 00 30 03 00 00 44 05 00 01
   00 00 00 09 00 00 2C 27 00 00 48 02 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      10      0    str       Ljava/lang/String;


public static my.test.Test main3(java.lang.String);
  Code:
   Stack=4, Locals=1, Args_size=1
   0:	new	#11; //class my/test/Test$2
   3:	dup
   4:	ldc	#9; //String dfdf
   6:	aload_0
   7:	invokespecial	#12; //Method my/test/Test$2."<init>":(Ljava/lang/String;Ljava/lang/String;)V
   10:	areturn
  LineNumberTable: 
   line 21: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 0A 00 00 54 03 00 00 68 05 00 01
   00 00 00 0A 00 00 50 2D 00 00 6C 02 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      11      0    str       Ljava/lang/String;


public static my.test.Test main5(java.lang.String);
  Code:
   Stack=4, Locals=2, Args_size=1
   0:	iconst_3
   1:	istore_1
   2:	new	#13; //class my/test/Test$3
   5:	dup
   6:	ldc	#9; //String dfdf
   8:	aload_0
   9:	invokespecial	#14; //Method my/test/Test$3."<init>":(Ljava/lang/String;Ljava/lang/String;)V
   12:	areturn
  LineNumberTable: 
   line 58: 0
   line 59: 2

  CharacterRangeTable: length = 0x2C
   00 03 00 00 00 01 00 00 FFFFFFE8 0D 00 00 FFFFFFE8 14 00 01
   00 02 00 0C 00 00 FFFFFFEC 03 00 01 00 05 00 01 00 00
   00 0C 00 00 FFFFFFE4 2E 00 01 04 02 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      13      0    str1       Ljava/lang/String;
   2      11      1    str2       I


java.lang.String out();
  Code:
   Stack=1, Locals=1, Args_size=1
   0:	ldc	#15; //String dfd
   2:	areturn
  LineNumberTable: 
   line 67: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 02 00 01 0C 0F 00 01 0C 1C 00 01
   00 00 00 02 00 01 0C 0E 00 01 0C 1C 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      3      0    this       Lmy/test/Test;


}

