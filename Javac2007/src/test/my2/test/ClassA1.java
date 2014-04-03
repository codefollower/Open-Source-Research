package my.test;

public class ClassA1 {
	ClassA1_1 a1_1=new ClassA1_1();
	ClassA1_2 a1_2=new ClassA1_2();

	String str[]=new String[2];
	public void print() {
		System.out.println("ClassA");
		a1_1.print();
		a1_2.print();
	}
}