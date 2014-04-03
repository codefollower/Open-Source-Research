package test.jvm;

public strictfp class LoadClassTest<T extends ExtendsClass & InterfaceA, V> extends ExtendsClass implements InterfaceA {
	public int f1;
	public void m1(){}

	public static class ClassA<M, N extends InterfaceA> {
		public int f2;
		public void m2(){}
	}

	public class ClassB {
		public int f3;
		public void m3(){}
	}
}
