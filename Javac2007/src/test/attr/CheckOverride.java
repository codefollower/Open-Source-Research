package test.attr;

class ClassA {}

class ClassB extends ClassA {
	<T extends ClassA,V> void m1(){}
}
class CheckOverride extends ClassB {
	//<T extends ClassB,V> void m1(){}
	void m1() throws E1,E2,E3{}
}

class E1 extends Error{}
class E2 extends Exception{}
class E3 extends RuntimeException {}