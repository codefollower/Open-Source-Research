package test.attr;
//AttrTests02
//测试resolveSelf(4)
class Aclass{
	Aclass(){
		Aclass a = Aclass.this;
			a = Bclass.this;
			a = Bclass.super.getClass();
	}

	class Bclass{

		Bclass(){
			Aclass a = Aclass.this;
			a = Bclass.this;
			a = Bclass.super.getClass();
		}
	}

	static class Cclass{

		Cclass(){
			Aclass a = Aclass.this;
			a = Bclass.this;
			a = Bclass.super.getClass();
		}
	}
}