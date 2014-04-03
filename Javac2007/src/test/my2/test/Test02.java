package my.test;
public class Test2<S,T> extends TestOhter2 implements MyInterfaceA,MyInterfaceB {
	public int myInt;
	
	S myS;
	T myT;
	
	Test2() {
		myInt=10;
	}
	
	
	private class TestInner {
		TestInner(){}
	}
	
}