package test.attr;
//AttrTests04
//测试resolveQualifiedMethod  findMethod (带有限制的方法调用)
class Bclass{}
class Cclass{}
class Aclass<T>{
	void m1(long i){}
	void m1(int i){}

	//int m2(Aclass<?> a) { return 10; }

	//static <V> void m3(){}

	<V> void m2(long i,V v){}
	//<M extends Bclass,N extends M> void m2(int i,V v){}
	<M extends Bclass,N extends M> void m2(int i,N n){}
	void m2(int i){}

	//static <V> void m3(long i,V v){}
	//static <V> void m3(int i,V v){}


	Aclass<Bclass> a;
	Cclass c;
	{
		
		//a.m1(20);
		//a.m1(m2(a));
		//a.m1(Aclass.<Aclass>m3());
		//a.m2(a);
		//a.<Cclass>m2(20,c);
		a.m2(20,a);

		//Aclass.<Cclass>m3(20,c);
	}
}