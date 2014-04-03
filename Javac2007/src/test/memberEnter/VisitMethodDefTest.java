package test.memberEnter;

class VisitMethodDefTest<T,i1> {
	
	/*
	int f1;
	int f2=10;
	final int f3=20;
	static int f4=30;
	static final int f5=40;
	*/

	//<T,T> void m1(int i1,int i2) throws T{}

	//int i1;

	static <T> void m1(int i1,int i2) throws T{
		//int i1;//测试Check.checkTransparentVar(3)
	}

	//void m2(int[] i1) {}
	//<T> void m2(int... i1) {}
	

	VisitMethodDefTest() {}
}