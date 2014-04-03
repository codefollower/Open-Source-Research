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
	
	public Test2 main6() {
		return new Test2() {
			String out(){
				return str;
			}
		};
	}
	private String str="dfdf";
	public class Test2{}
	String out(){return "dfd";}
}

//javap -classpath bin\mybin -verbose my.test.Test$4>javapOut.txt
Compiled from "Test.java"
final class my.test.Test$4 extends my.test.Test$Test2
  SourceFile: "Test.java"
  SourceID: length = 0x2
   00 17 
  CompilationID: length = 0x2
   00 19 
  EnclosingMethod: length = 0x4
   00 1B 00 1C 
  InnerClass: 
   final #4; //class my/test/Test$4
   public #34= #5 of #27; //Test2=class my/test/Test$Test2 of class my/test/Test
  minor version: 0
  major version: 50
  Constant pool:
const #1 = Field	#4.#29;	//  my/test/Test$4.this$0:Lmy/test/Test;
const #2 = Method	#5.#30;	//  my/test/Test$Test2."<init>":(Lmy/test/Test;)V
const #3 = Method	#27.#31;	//  my/test/Test.access$000:(Lmy/test/Test;)Ljava/lang/String;
const #4 = class	#32;	//  my/test/Test$4
const #5 = class	#33;	//  my/test/Test$Test2
const #6 = Asciz	this$0;
const #7 = Asciz	Lmy/test/Test;;
const #8 = Asciz	<init>;
const #9 = Asciz	(Lmy/test/Test;)V;
const #10 = Asciz	Code;
const #11 = Asciz	LineNumberTable;
const #12 = Asciz	CharacterRangeTable;
const #13 = Asciz	LocalVariableTable;
const #14 = Asciz	this;
const #15 = Asciz	;
const #16 = Asciz	InnerClasses;
const #17 = Asciz	Lmy/test/Test$4;;
const #18 = Asciz	out;
const #19 = Asciz	()Ljava/lang/String;;
const #20 = Asciz	SourceFile;
const #21 = Asciz	Test.java;
const #22 = Asciz	SourceID;
const #23 = Asciz	1180596986000;
const #24 = Asciz	CompilationID;
const #25 = Asciz	1180597475859;
const #26 = Asciz	EnclosingMethod;
const #27 = class	#35;	//  my/test/Test
const #28 = NameAndType	#36:#37;//  main6:()Lmy/test/Test$Test2;
const #29 = NameAndType	#6:#7;//  this$0:Lmy/test/Test;
const #30 = NameAndType	#8:#9;//  "<init>":(Lmy/test/Test;)V
const #31 = NameAndType	#38:#39;//  access$000:(Lmy/test/Test;)Ljava/lang/String;
const #32 = Asciz	my/test/Test$4;
const #33 = Asciz	my/test/Test$Test2;
const #34 = Asciz	Test2;
const #35 = Asciz	my/test/Test;
const #36 = Asciz	main6;
const #37 = Asciz	()Lmy/test/Test$Test2;;
const #38 = Asciz	access$000;
const #39 = Asciz	(Lmy/test/Test;)Ljava/lang/String;;

{
final my.test.Test this$0;

my.test.Test$4(my.test.Test);
  Code:
   Stack=2, Locals=2, Args_size=2
   0:	aload_0
   1:	aload_1
   2:	putfield	#1; //Field this$0:Lmy/test/Test;
   5:	aload_0
   6:	aload_1
   7:	invokespecial	#2; //Method my/test/Test$Test2."<init>":(Lmy/test/Test;)V
   10:	return
  LineNumberTable: 
   line 68: 0

  CharacterRangeTable: length = 0x2
   00 00 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      11      0    this       Lmy/test/Test$4;


java.lang.String out();
  Code:
   Stack=1, Locals=1, Args_size=1
   0:	aload_0
   1:	getfield	#1; //Field this$0:Lmy/test/Test;
   4:	invokestatic	#3; //Method my/test/Test.access$000:(Lmy/test/Test;)Ljava/lang/String;
   7:	areturn
  LineNumberTable: 
   line 70: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 07 00 01 18 05 00 01 18 10 00 01
   00 00 00 07 00 01 14 10 00 01 1C 04 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      8      0    this       Lmy/test/Test$4;


}





//javap -classpath bin\mybin -verbose my.test.Test>javapOut.txt
Compiled from "Test.java"
public class my.test.Test extends java.lang.Object
  SourceFile: "Test.java"
  SourceID: length = 0x2
   00 37 
  CompilationID: length = 0x2
   00 39 
  InnerClass: 
   public #22= #21 of #19; //Test2=class my/test/Test$Test2 of class my/test/Test
   final #10; //class my/test/Test$1
   final #12; //class my/test/Test$2
   final #14; //class my/test/Test$3
   final #16; //class my/test/Test$4
  minor version: 0
  major version: 50
  Constant pool:
const #1 = Field	#19.#58;	//  my/test/Test.str:Ljava/lang/String;
const #2 = Field	#59.#60;	//  java/lang/System.out:Ljava/io/PrintStream;
const #3 = String	#61;	//  Hello World!
const #4 = Method	#62.#63;	//  java/io/PrintStream.println:(Ljava/lang/String;)V
const #5 = String	#24;	//  str
const #6 = Method	#19.#64;	//  my/test/Test.main2:(Ljava/lang/String;)Lmy/test/Test;
const #7 = Method	#19.#65;	//  my/test/Test.main3:(Ljava/lang/String;)Lmy/test/Test;
const #8 = Method	#20.#66;	//  java/lang/Object."<init>":()V
const #9 = String	#67;	//  dfdf
const #10 = class	#68;	//  my/test/Test$1
const #11 = Method	#10.#70;	//  my/test/Test$1."<init>":(Ljava/lang/String;)V
const #12 = class	#71;	//  my/test/Test$2
const #13 = Method	#12.#72;	//  my/test/Test$2."<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #14 = class	#73;	//  my/test/Test$3
const #15 = Method	#14.#72;	//  my/test/Test$3."<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #16 = class	#74;	//  my/test/Test$4
const #17 = Method	#16.#75;	//  my/test/Test$4."<init>":(Lmy/test/Test;)V
const #18 = String	#76;	//  dfd
const #19 = class	#77;	//  my/test/Test
const #20 = class	#78;	//  java/lang/Object
const #21 = class	#79;	//  my/test/Test$Test2
const #22 = Asciz	Test2;
const #23 = Asciz	InnerClasses;
const #24 = Asciz	str;
const #25 = Asciz	Ljava/lang/String;;
const #26 = Asciz	main;
const #27 = Asciz	([Ljava/lang/String;)V;
const #28 = Asciz	Code;
const #29 = Asciz	LineNumberTable;
const #30 = Asciz	CharacterRangeTable;
const #31 = Asciz	LocalVariableTable;
const #32 = Asciz	args;
const #33 = Asciz	[Ljava/lang/String;;
const #34 = Asciz	<init>;
const #35 = Asciz	(Ljava/lang/String;)V;
const #36 = Asciz	this;
const #37 = Asciz	Lmy/test/Test;;
const #38 = Asciz	main2;
const #39 = Asciz	(Ljava/lang/String;)Lmy/test/Test;;
const #40 = Asciz	main3;
const #41 = Asciz	main5;
const #42 = Asciz	str1;
const #43 = Asciz	str2;
const #44 = Asciz	I;
const #45 = Asciz	main6;
const #46 = Asciz	()Lmy/test/Test$Test2;;
const #47 = Asciz	out;
const #48 = Asciz	()Ljava/lang/String;;
const #49 = Asciz	access$000;
const #50 = Asciz	(Lmy/test/Test;)Ljava/lang/String;;
const #51 = Asciz	x0;
const #52 = Asciz	SourceFile;
const #53 = Asciz	Test.java;
const #54 = Asciz	SourceID;
const #55 = Asciz	1180596986000;
const #56 = Asciz	CompilationID;
const #57 = Asciz	1180597475875;
const #58 = NameAndType	#24:#25;//  str:Ljava/lang/String;
const #59 = class	#80;	//  java/lang/System
const #60 = NameAndType	#47:#81;//  out:Ljava/io/PrintStream;
const #61 = Asciz	Hello World!;
const #62 = class	#82;	//  java/io/PrintStream
const #63 = NameAndType	#83:#35;//  println:(Ljava/lang/String;)V
const #64 = NameAndType	#38:#39;//  main2:(Ljava/lang/String;)Lmy/test/Test;
const #65 = NameAndType	#40:#39;//  main3:(Ljava/lang/String;)Lmy/test/Test;
const #66 = NameAndType	#34:#84;//  "<init>":()V
const #67 = Asciz	dfdf;
const #68 = Asciz	my/test/Test$1;
const #69 = Asciz	;
const #70 = NameAndType	#34:#35;//  "<init>":(Ljava/lang/String;)V
const #71 = Asciz	my/test/Test$2;
const #72 = NameAndType	#34:#85;//  "<init>":(Ljava/lang/String;Ljava/lang/String;)V
const #73 = Asciz	my/test/Test$3;
const #74 = Asciz	my/test/Test$4;
const #75 = NameAndType	#34:#86;//  "<init>":(Lmy/test/Test;)V
const #76 = Asciz	dfd;
const #77 = Asciz	my/test/Test;
const #78 = Asciz	java/lang/Object;
const #79 = Asciz	my/test/Test$Test2;
const #80 = Asciz	java/lang/System;
const #81 = Asciz	Ljava/io/PrintStream;;
const #82 = Asciz	java/io/PrintStream;
const #83 = Asciz	println;
const #84 = Asciz	()V;
const #85 = Asciz	(Ljava/lang/String;Ljava/lang/String;)V;
const #86 = Asciz	(Lmy/test/Test;)V;

{
public static void main(java.lang.String[]);
  Code:
   Stack=2, Locals=1, Args_size=1
   0:	getstatic	#2; //Field java/lang/System.out:Ljava/io/PrintStream;
   3:	ldc	#3; //String Hello World!
   5:	invokevirtual	#4; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
   8:	ldc	#5; //String str
   10:	invokestatic	#6; //Method main2:(Ljava/lang/String;)Lmy/test/Test;
   13:	pop
   14:	ldc	#5; //String str
   16:	invokestatic	#7; //Method main3:(Ljava/lang/String;)Lmy/test/Test;
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
   Stack=2, Locals=2, Args_size=2
   0:	aload_0
   1:	invokespecial	#8; //Method java/lang/Object."<init>":()V
   4:	aload_0
   5:	ldc	#9; //String dfdf
   7:	putfield	#1; //Field str:Ljava/lang/String;
   10:	return
  LineNumberTable: 
   line 9: 0
   line 74: 4
   line 9: 10

  CharacterRangeTable: length = 0x1E
   00 02 00 04 00 09 00 01 28 02 00 01 28 1C 00 01
   00 00 00 0A 00 00 24 13 00 00 24 14 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      11      0    this       Lmy/test/Test;
   0      11      1    str       Ljava/lang/String;


public static my.test.Test main2(java.lang.String);
  Code:
   Stack=3, Locals=1, Args_size=1
   0:	new	#10; //class my/test/Test$1
   3:	dup
   4:	ldc	#9; //String dfdf
   6:	invokespecial	#11; //Method my/test/Test$1."<init>":(Ljava/lang/String;)V
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
   0:	new	#12; //class my/test/Test$2
   3:	dup
   4:	ldc	#9; //String dfdf
   6:	aload_0
   7:	invokespecial	#13; //Method my/test/Test$2."<init>":(Ljava/lang/String;Ljava/lang/String;)V
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
   2:	new	#14; //class my/test/Test$3
   5:	dup
   6:	ldc	#9; //String dfdf
   8:	aload_0
   9:	invokespecial	#15; //Method my/test/Test$3."<init>":(Ljava/lang/String;Ljava/lang/String;)V
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


public my.test.Test$Test2 main6();
  Code:
   Stack=3, Locals=1, Args_size=1
   0:	new	#16; //class my/test/Test$4
   3:	dup
   4:	aload_0
   5:	invokespecial	#17; //Method my/test/Test$4."<init>":(Lmy/test/Test;)V
   8:	areturn
  LineNumberTable: 
   line 68: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 08 00 01 10 03 00 01 20 05 00 01
   00 00 00 08 00 01 0C 17 00 01 24 02 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      9      0    this       Lmy/test/Test;


java.lang.String out();
  Code:
   Stack=1, Locals=1, Args_size=1
   0:	ldc	#18; //String dfd
   2:	areturn
  LineNumberTable: 
   line 76: 0

  CharacterRangeTable: length = 0x1E
   00 02 00 00 00 02 00 01 30 0F 00 01 30 1C 00 01
   00 00 00 02 00 01 30 0E 00 01 30 1C 00 02 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      3      0    this       Lmy/test/Test;


static java.lang.String access$000(my.test.Test);
  Code:
   Stack=1, Locals=1, Args_size=1
   0:	aload_0
   1:	getfield	#1; //Field str:Ljava/lang/String;
   4:	areturn
  LineNumberTable: 
   line 2: 0

  CharacterRangeTable: length = 0x2
   00 00 
  LocalVariableTable: 
   Start  Length  Slot  Name   Signature
   0      5      0    x0       Lmy/test/Test;


}


