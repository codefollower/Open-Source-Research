package test.enter;


class A {
	int a;
	static void m() {
		int b=a;
	}

	static class B {
		int c=a;

		static void m2() {
			int d=c;
		}
	}
}
