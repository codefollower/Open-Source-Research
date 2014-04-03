package test.attr.error;

class ClassA {
	void m1(){}
}

interface InterfaceA {
	void m2();
}

interface InterfaceB extends InterfaceA {
	//static void m2();//此处不允许使用修饰符 0x8 static
	void m2();
}

public class override_static extends ClassA implements InterfaceA {
	static void m1(){}
	static void m2(){}
}

class ClassB {
	final void m1(){}
	static void m2(){}
	static final void m3(){}
}

class override_meth extends ClassB {
	void m1(){}
	void m2(){}
	void m3(){}
}

class ClassC {
	void m1(){}
	private void m2(){}
	public void m3(){}
	protected void m4(){}
}
class override_weaker_access extends ClassC {
	protected void m1(){}
	void m2(){}
	void m3(){}
	private void m4(){}
}

