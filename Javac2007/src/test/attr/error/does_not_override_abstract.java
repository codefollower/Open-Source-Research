package test.attr.error;
interface InterfaceTest{
	//void interfaceMethod();
}
interface InterfaceTest2{
	void interfaceMethod2();
}
abstract class AbstractClass implements InterfaceTest {
	//abstract void abstractClassMethod();
}

public class does_not_override_abstract extends AbstractClass implements InterfaceTest2 {
	//abstract void innerAbstractMethod();
}