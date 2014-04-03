package test.attr;
//AttrTests06
//测试NOOUTERTHIS

class Aclass{
	int outer_f1;
	protected int outer_f2;

	int outer_m1(){
		return 10;
	}

	Aclass(int i1,int i2) {
			this(
				new Aclass(outer_f1,20) {
					int Anonf3 = outer_f1;
					int Anonf4 = outer_f2;

					int Anonf6 = outer_m1();
				}
			);
	}
	Aclass(Aclass at) {}

	class InnerClassA{
	//class InnerClassA extends Aclass{
		int f1 = outer_f1;
		int f2 = outer_f2;

		int m1(){
			return 10;
		}

		
		InnerClassA(int i1,int i2) {
			this(
				new InnerClassA(f1,f2) {
					int Anonf1 = f1;
					int Anonf2 = f2;
					int Anonf3 = outer_f1;
					int Anonf4 = outer_f2;

					int Anonf5 = m1();
					int Anonf6 = outer_m1();
					//void methodA(){}
				}
			);

			//<ClassA,ClassB>this(new InnerClassA(i1,i1) { int i=10;int ii=i;void methodA(){}});
			//this(new InnerClassA(i1,i1) { int i=10;int ii=i;void methodA(){}});
		}
		InnerClassA(InnerClassA at) {}
/*
		//InnerClassA(InnerClassA at) {}

		//<T extends ClassA & InterfaceA>InnerClassA(InnerClassA at) {}

		<TA extends ClassA & InterfaceA, TB extends TA>InnerClassA(InnerClassA at) {}
		*/
	}
}