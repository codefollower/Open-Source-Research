package test.attr;
//AttrTests07
//测试Attr中从visitTypeArray方法开始到结束的代码

//import test.attr.Aclass.Bclass;
import test.attr.Aclass.*;
//class Aclass<T> implements java.io.Serializable{
class Aclass<T> {
	//Aclass<Cclass>.Bclass<Dclass> a;
	//Aclass<?>.Bclass<?> b;
	//Bclass<?> c;
	class Bclass<V>{
		//Bclass<?> d;
	}
	class Cclass{}
	class Dclass{}
}

class Aclass2<T> {
	//Aclass.Bclass<?> b2;
	//Bclass<?> b2;
	Bclass<Aclass2> b2;
}

	//int[] array;
	//@interface Aanno{}
	//void m(int i){
		//while(@Aanno);
	//}

	//Object a=new Aanno(){};
	//static final long serialVersionUID=new Integer(3);
	//int a=10;
	//final int b=20;

	//@interface Aanno{
		//Class<? super Aclass> m();
		//Class<? extends Aclass> m2();
		//int[][] m3();

		//int hashCode();
	//}
	//enum Aenum  implements java.io.Serializable{
	//	;
	//	Aenum() {
			//super();
	//	}
	//}
	//void m(int a) { int b; }

	/*
	int f1;
	//Bclass b = a.new Aclass() {}.new Bclass() {};
	Bclass b = new Aclass().new Bclass() {};
	Aclass<?> a;
	class Bclass{}

	void m() {
		//T t1=T.this;
		//T t2=T.super.toString();
		//T t3=T.class;

		a.m();
		a.f1++;

		a.<Bclass>m2(null).m();
		//Aclass.<Bclass>m2(null).m();
	}

	<T> Aclass<?> m2(T t) {
		return a;
	}
	*/


	/*
	{
		f1=f2;
	}
	int f1,f2;
	*/

	/*
	int outer_f1;
	final protected int outer_f2;
	int outer_f3=outer_f2=0;

	int outer_m1(int outer_f1){
		final int outer_f2;

		class LocalClassA {
			//int f1 = outer_f1;
			//int f2 = outer_f2;
			void m() {
				outer_f2 = 10;
			}
		}

		//outer_f2=this.outer_f2;
		//Aclass.this.outer_f2=this.outer_f2;
		this.outer_f2=this.outer_f2;
		return 10;
	}
	*/
//}