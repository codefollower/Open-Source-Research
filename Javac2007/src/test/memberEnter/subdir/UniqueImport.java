package test.memberEnter.subdir;

/*
test\memberEnter\ImportTest.java:38: test.memberEnter.subdir.UniqueImport 在 tes
t.memberEnter.subdir 中不是公共的；无法从外部软件包中对其进行访问
import test.memberEnter.subdir.UniqueImport;
                              ^
2 错误
*/
//class UniqueImport {}

public class UniqueImport {
	private class MemberClassA{}

	public class MemberClassB{}

	public static int staticFieldA;

	public static class MemberClassC{}
} 
