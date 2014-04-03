package test.attr;

class C1{}
class C2 extends C1{}


class F1{}

class CaptureTest {
	void m() {
		C1 a=new C1();
		C2 b=new C2();
		F1 f=new F1();
		a=(C1)null;
		a=(C1)b;
		a=(C1)f;
	}
}

/*
class C1{}
class C2 extends C1{}
class C3 extends C2{}
class C4 extends C3{}

class D1 extends C4{}
class D2 extends D1{}
class D3 extends D2{}

class E1 extends C2{}
class E2 extends E1{}
class E3 extends E2{}

class F1<T>{}
class Test<T1 extends F1<C2>,T2 extends T1,T3 extends C3,T4 extends C4,T5 extends C4> {}
class CaptureTest {

	void m(Test<? extends F1<C3>,? super D2,D3,?,? super C4> ct){
		Object o = ct;
	};
}


class Test<T1 extends E1,T2 extends T1,T3 extends C3,T4 extends C4,T5 extends C4> {}
class CaptureTest {

	void m(Test<? extends D1,? super D2,D3,?,? super C4> ct){
		Object o = ct;
	};
}


class Test<T1 extends C4,T2 extends T1,T3 extends C3,T4 extends C4,T5> {}
class CaptureTest {

	void m(Test<? extends C2,? super D2,D3,?,?> ct){
		Object o = ct;
	};
}
*/