package test.attr;
//AttrTests08
//测试Attr中的几个Loop
import java.util.Iterator;
class C{}
class D{}
//class Aclass<T> implements Iterable<Aclass>{
class Aclass<T> {

	 //public Iterator<Aclass> iterator() { return null;}

	 class Bclass<V> implements Iterable<Aclass<C>.Bclass<D>>{
		Aclass<C>.Bclass<D> a;
		{
			/*
			test\attr\AttrTests.java:15: 不兼容的类型
			找到： test.attr.Aclass<test.attr.C>.Bclass<test.attr.D>
			需要： test.attr.Aclass<T {bound=Object}>.Bclass<?>
									for(Bclass<?> b: a);
													 ^
			1 错误
			*/
			//for(Bclass<?> b: a);
			for(Aclass<?>.Bclass<?> b: a);
		}
		public Iterator<Aclass<C>.Bclass<D>> iterator() { return null;}
	 }
	 
	 

	 /*
	 class Bclass<V> implements Iterable<Aclass<Aclass>.Bclass<Bclass>>{
		Aclass<Aclass>.Bclass<Bclass> a;
		{
			for(Bclass<?> b: a);
		}

		//public Iterator<Bclass> iterator() { return null;}
		public Iterator<Aclass<Aclass>.Bclass<Bclass>> iterator() { return null;}
	 }
	 */

	/*
	{
		int i=10;
		for(int i=10,j=20;;);
	}
	void m(int i) {
		for(int i=10,j=20;;);
	}
	*/
}
