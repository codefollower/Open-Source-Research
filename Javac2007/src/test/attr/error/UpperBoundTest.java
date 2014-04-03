package test.attr.error;
public class UpperBoundTest {
	interface InterfaceA{}
	interface InterfaceB{}
	class ClassA{}
	class ClassB{}

	class TestA<T>{}
	class TestB<T extends ClassA>{}
	class TestC<T extends InterfaceA>{}
	class TestD<T,V extends T>{}

	class TestE<T extends InterfaceA & InterfaceB>{}
	class TestF<T extends ClassA & InterfaceA & InterfaceB>{}

	//下面四个类无法编译通过
	class TestG<T extends ClassA & ClassB & InterfaceB>{}
	class TestH<T,V extends T & ClassA & InterfaceA>{}
	class TestI<T,V extends ClassA & T & InterfaceA>{}
	class TestJ<T,V extends ClassA & InterfaceA & T>{}
	class TestK<T,V extends ClassA & int>{}
}
