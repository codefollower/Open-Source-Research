package test.attr;
//AttrTests05
//测试selectSym(5) site.tag = TYPEVAR

class Aclass{
	class A{
		C c;
		static class C{
			static int i;
		}
	}
	class D<T extends A> {
		Class<?> c = T.class;
		int i = T.C.i; //isType(sym)=true
		A.C c = T.c; //无法从静态上下文中引用非静态 变量 c
	}
	/*
					class B {
						//int i;
						B b;
						B b(){ return new B(); }
						class b{}

						class C{}
					}
					public class VisitSelectTest<T extends B> extends A<T.b> {
						//A<T.i> al;

						//A<T.b> al;
						B b=T.b;
						B b2=T.b();
						B b3=T.C;
					}
	//class B<T> extends A<T.foo> {}
	//int a=10;
	//static int b = Aclass.this.a;
	
	Aclass(){
		//Aclass a = Aclass.this;
			//a = Bclass.this;
			//a = Bclass.super.getClass();
	}

	<T> Aclass(T t){
		int i = t.toString();
		//Aclass a = Aclass.this;
			//a = Bclass.this;
			//a = Bclass.super.getClass();
	}

	class Bclass{
		//Aclass a;
		Bclass(){
			Bclass.super();
			//Aclass a = Aclass.this;
			//a = Bclass.this;
			
			//a = Bclass.super();//.getClass();
		}
	}
	*/
/*
	static class Cclass{

		Cclass(){
			Aclass a = Aclass.this;
			a = Bclass.this;
			a = Bclass.super.getClass();
		}
	}
	*/
}

/*
//class Bclass{}
//class Cclass{}
class Aclass {
	int i;

	class Bclass{
		int i;
	}
	
	Aclass()
	{
		//Aclass.this();//不是语句
		Aclass.super();
		//Ainterface.this.i=10;
		Bclass.this.i=10;
	}
}

interface Ainterface{
	int a=10;
	int b = Ainterface.this.a;
}
*/