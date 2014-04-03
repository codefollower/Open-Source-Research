package test.attr.error;
interface InterfaceTest {
	//int myOverrideMethodA=10;
	void myOverrideMethodA(int i,char c);
}

abstract class superClassTestA implements InterfaceTest {
	public void myOverrideMethodB(int i,char c) {}
}

abstract class superClassTestB extends superClassTestA {
	//int myOverrideMethodA;
	public void myOverrideMethodC(int i,char c) {}

	public void method_does_not_override_superclass(int i,char c) {}
}

public class method_does_not_override_superclass extends superClassTestB {
	//下面三个方法的第二个参数与超类型中对应的三个方法的第二个参数不同，
	//所以使用“@Override”注释标记并不恰当，并没有真正达到覆盖的目的。

	@Override
	public static void myOverrideMethodC(int i,char c) {}

	@Override
	public method_does_not_override_superclass(int i,char c) {}

	@Override
	public void myOverrideMethodA(int i,byte b) {}

	@Override
	public void myOverrideMethodB(int i,byte b) {}

	@Override
	public void myOverrideMethodC(int i,byte b) {}
}
