package my.test;

class ClassA {}
class ClassB extends ClassA {}
class ClassC extends ClassB {}

//public class GenericsTest<E> {
public class GenericsTest<E extends ClassA> {
//public class GenericsTest<E extends ClassB> {
//public class GenericsTest<E extends ClassC> {
	void add(E e) {}

	public static void main(String... args) {
		//GenericsTest<ClassB> gt=new GenericsTest<ClassB>();

		//GenericsTest<?> gt=new GenericsTest<ClassC>();
		GenericsTest<? extends ClassA> gtA=new GenericsTest<ClassA>();
		GenericsTest<? extends ClassB> gtB=new GenericsTest<ClassB>();
		GenericsTest<? extends ClassC> gtC=new GenericsTest<ClassC>();

		GenericsTest<? super ClassA> gtA2=new GenericsTest<ClassA>();
		GenericsTest<? super ClassB> gtB2=new GenericsTest<ClassB>();
		GenericsTest<? super ClassC> gtC2=new GenericsTest<ClassB>();

		/*
		ClassA a=new ClassA();
		ClassB b=new ClassB();
		ClassC c=new ClassC();
		gt.add(a);
		gt.add(b);
		gt.add(c);*/
	}
}
