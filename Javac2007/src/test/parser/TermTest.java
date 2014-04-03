package test.parser;

public class TermTest {
	TermTest(int a,int b) {
		//int[][] a1;
		//int a=0,b=0;

		//a=10;
		//a+b>0 ? 0 :1;//不是语句
		
		//a=a+(b&a)/b-++a/b > 0 ? 0 :1;
		//a=a+(b&a)/b-++a/b > 0 ? b=10 :1;

		//意外的类型(变成赋值语句了 [ a+(b&a)/b-++a/b > 0 ? b=10 : b ] = 20  ) 
		a=a+(b&a)/b-++a/b > 0 ? b=10 : b=20;



		//a=a|b<<a^b;

		//String str="A"+"B"+'c';
		//str="A"+"B"+"c";
	}
}