package test.attr;
class A{
	@Deprecated
	A(){}

	class B extends A{
		//sym.flags()=0x20000 deprecated 
		//env.info.scope.owner=B()
		//env.info.scope.owner.flags()=0x0 
		//env.info.scope.owner.outermostClass()=test.attr.A
		//sym.outermostClass()=test.attr.A
		B(){
			super(); //不会警告，因为B是A的成员
		}
	}
}
class B extends A{
	//sym.flags()=0x40020000 deprecated acyclic 
	//env.info.scope.owner=B()
	//env.info.scope.owner.flags()=0x0 
	//env.info.scope.owner.outermostClass()=test.attr.B
	//sym.outermostClass()=test.attr.A
	B(){
		super(); //警告：[deprecation] test.attr.A 中的 A() 已过时
	}
}

class C extends A{
	//sym.flags()=0x40020000 deprecated acyclic 
	//env.info.scope.owner=C()
	//env.info.scope.owner.flags()=0x20000 deprecated 
	//env.info.scope.owner.outermostClass()=test.attr.C
	//sym.outermostClass()=test.attr.A
	@Deprecated
	C(){
		super(); //不会警告，因为C()已有@Deprecated
	}
}

/*
class ClassA{}
class ClassC{}
@Deprecated
class ClassB<T> extends ClassA {
	//ClassB cb;
	//ClassB<? extends ClassC> cb;
	//ClassB<? extends ClassC> cb;
	ClassB<ClassC> cb;
	<T extends ClassA> T m(T t){ return t; }
	<T extends ClassB> T m(T t){ return t; }

	//void m2(ClassB<? extends ClassA> cb){}
	//void m2(ClassB<? extends ClassC> cb){}

	//<T extends ClassB> T m(ClassB<? extends ClassB> t){ return t; }

	{
		//cb.<ClassA>m(cb);
		cb.<ClassB>m(cb);
		//cb.<ClassC>m(cb);
		cb.m(cb);
		//cb.m2(cb);
		//m(cb);
	}

	ClassC c;
	
	ClassB() {
		<ClassC>this(null);
		//<ClassB>m(cb);//非法的表达式开始
		//<ClassC>this(c);  //无法在调用父类型构造函数之前引用 c
		this.<ClassB>m(cb);
		m(cb);
	}
	@Deprecated
	<T> ClassB(T t) {
	}

	//ClassB<? super ClassB> cb;

	class ClassDD extends ClassB {

		ClassDD(){
			<ClassC>super(null);
		}
	}
}

class ClassDD extends ClassB {
	ClassDD(){
		<ClassC>super(null);
	}
}
*/

