//javac -d classes src\test\Test.java
//java -cp classes test.Test

package test;

public class Test {
	public static void main(String[] args) {
		int a=0, b=1, c=-1;
		System.out.println((a|b|c)<0);
		//testMath();
		//testThrowThrowable();
		//testGetDeclaredFields();
	}

	static void testMath() {
		System.out.println(Math.floor(1.1));
		System.out.println(Math.floor(1.9));
		System.out.println(Math.floor(0.9));

		int base=3;

		for (int attempts = 0; attempts < 20; attempts++) {
			System.out.println((int) Math.floor((Math.random() * base)));
		}
	}


	static void testThrowThrowable() {
		m();

		m2();
	}
	static void m() {
		pointOfOrigin = new Throwable();
	}

	static void m2() {
		pointOfOrigin.printStackTrace();
	}

	static Throwable pointOfOrigin;


	static void testGetDeclaredFields() {
		for(java.lang.reflect.Field f : GetDeclaredFieldsTest.class.getDeclaredFields()) {
			System.out.println(f);
		}
	}

	static interface I {
		int e = 0;
	}

	static class C {
		int d;
	}

	static class GetDeclaredFieldsTest extends C implements I {

		static int a;
		int b;
		private int c;
	}
}