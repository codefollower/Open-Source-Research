package my.test;

public class NonStaticImport {
	public static void main(String[] args) {
		//凡是引用到TopLevelClass的“静态字段”或“静态方法”时
		//都得加上这一串字符：“TopLevelClass.”
		//这显得很冗长
		System.out.println(TopLevelClass.field_static);
		TopLevelClass.method_static();
	}
}

