package test.flow;
/*
class Test {
	static final int a;
	static final int b=10;
	static int c;
}
*/

/*
//测试errorUncaught()
class Test {
	static final int a = m();
	static final int b;

	static int m() throws Exception {
		return 10;
	}
}
*/
/*
//测试errorUncaught()
class Test {
	static {
		m();
	}

	static void m() throws Exception {}
}
*/
/*
class Test {
	Test() {
		this(2);
	}
	//Test(int myInt) throws Error, Exception {}
	//Test(float f) throws Exception {}

	Test(int myInt) throws NoSuchFieldException, NoSuchMethodException {}
	Test(float f) throws Exception {}
}
*/

/*
//测试方法参数的赋值情况
class Test {
	void m(int i) {
		i++;
	}
}
*/

/*
class Test {
	final int a;
}
*/
/*
class Test {
	int a = m();
	{
		m();
	}

	int m() throws Exception {return 0;}
}
*/

/*
class Test {
	final int a;
	Test() {
		this(2);
	}
	//Test(int myInt) throws Error, Exception {}
	//Test(float f) throws Exception {}

	Test(int i) throws NoSuchFieldException, NoSuchMethodException {
		if(i<0) return;
		if(i>0) throw new NoSuchFieldException();

		a = 10;
	}
	Test(float f) throws Exception {}
}
*/
/*
//测试unreported.exception.default.constructor
class Test {
	class A {
		A() throws Exception {}
	}
	class B extends A {}
}
*/
/*
//测试redundant.cast
class Test {
	int i = (int)10;
}
*/

class Test {
	Test(int i) {
		if(i<0) {
			return;
		}
		else {
			i++;
			throw new Error();
		}
		i++;
	}
}


/*
class Test {
	{
		int a,b,c;

		boolean b1,b2;

		//if(false) c++;
		//else c--;

		//if(true) c++;
		//else c--;

		//if(a>b) c++;
		//else c--;

		if(b1 && b2) c++;
		else c--;

		//int a,b,c;
		//if(false) if(false) c++;
		//else c--;


		//int iii;
		//if(iii>5) iii++;
		//else iii--;
	}
}
*/
/*
class Test {
	{
		int a,b;

		int c=10;

		do {
			a=10;
			int d=10;
			c++;
			if(c>10) {
				a=10;
				continue;
			}
			a++;

			if(c>20) break;
		} while(true);
		//} while(d>10); //这里不能使用循环内定义的变量
	}
}
*/
/*
class Test {
	{
		int a,b;

		int c=10;
		

		outerLoop:
		do {
			innerLoop:
			do {
				if(c>10) {
					a=10;
					continue innerLoop;
				}

				if(c>20) {
					a=20;
					continue outerLoop;
				}

				if(c>10) break innerLoop;
				if(c>20) break outerLoop;

			} while(true);
		} while(true);
	}
}
*/
/*
class Test {
	
	{
		int a,b;

		int c=10;

		//while(false) {
		while(c>10) {
		//while(true) {
			a=10;
			int d=10;
			c++;
			if(c>10) {
				a=10;
				continue;
			}
			a++;

			if(c>20) break;

			a++;
		}

		while(true) {}
		a++;
	}
}
*/
/*
class Test {
	
	{
		int a,b;

		int c=10;
		switch(c) {
			case 1:
			case 2:
				int v1;
				//break;
			case 3:
				int v2=10;
				a=20;
				break;
			default:
				int v3=30;
		}
	}
}
*/

/*
class Test {
	
	{
		try {
			int a,b;

			int c=10;

			boolean b1=false,b2;

			if(b1) throw new RuntimeException();
			if(b1) throw new NoSuchMethodException();
			//if(b1) throw new NoSuchFieldException();

			//if(b1) throw new Exception();
		}
		catch(NoSuchFieldException e) {}
		//catch(Exception e) {}
		catch(RuntimeException e) {}
		catch(Exception e) {}
		catch(Error e) {}
		//catch(Throwable e) {}
	}
}
*/

/*
class Test {
	
	{
		try {
			int a,b;

			int c=10;

			boolean b1=false,b2;

			if(b1) throw new RuntimeException();
			if(b1) throw new NoSuchMethodException();
			if(b1) throw new NoSuchFieldException();

			//if(b1) throw new Exception();
		}
		catch(NoSuchFieldException e) {}
		//catch(Exception e) {}
		catch(RuntimeException e) {}
		catch(Exception e) {}
		catch(Error e) {}
		//catch(Throwable e) {}
		finally {
			//警告：[finally] 无法正常完成 finally 子句
			throw new RuntimeException();
		}
	}

	class MyInnerClass{}
}
*/







//测试visitIf与visitConditional
//class Test<T> {
	/*
	<V>Test(char a,Integer... b) throws Exception{}

	Test<?> t = new <Number>Test<Integer>('c',new Integer[]{1,2});

	void m() {
		Test<?> t = new <Number>Test<Integer>('c',new Integer[]{1,2});
	}
	*/

	//void m1() {
	//	m2();
	//	throw new Exception();
	//}
	//void m2() throws Exception{}
//}