package test.attr;
//AttrTests03
//测试resolveImplicitThis(3) 未明白此方法的运得原理，下面的用例没多大用处
class Aclass{
	Aclass(){
		//Aclass.super();
	}

	static class Bclass{

		Bclass(){
		}

		Bclass b;

		Bclass(Bclass b){
		}
	}

	class Cclass extends Bclass {

		Cclass(){
			//this(a);
			//super(this);
			//Bclass b = a.new Bclass();
			Bclass b = Aclass.new Bclass();
		}

		Aclass a;
		Bclass b;

		Cclass(Aclass a){
			//super();
		}
	}
}