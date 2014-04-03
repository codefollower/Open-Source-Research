//package my.test k
package my.test;
import java.util.ArrayList;
public class Test3<S,T extends java.util.ArrayList> {
	/*
	public class MyInnerClass{
		public int a=0;
		//public static int b=0;//内部类不能有静态声明
	}
	*/
	{
		int a=0;
	}
	static{
		int b=0;
	}
	
	Test3() {
		this(2);
	}
	
	Test3(int e) {
		this.e=e;
	}
	
	Test3(String s) {
	}
	
	public final int c=04;
	public static int d=0xfffffffe;
	public int e=0x8ffffffe;
	public static final String f="STR";
	public final float g=0x.1p-1f;//0.0f;
		
	public int[] myInt=new int[256];
	S myS;
	T myT;
	public Object result;
    @SuppressWarnings("unchecked")
    public <M extends Object> M myMethod(M m,int... i) {
    	Test3<String,ArrayList> TTT=new Test3<String,ArrayList>();
    	ArrayList<String> AL=new ArrayList<String>();
    	AL.add("STR");
    	String STR=AL.get(0);
    	Object result = AL.get(0);
	    //Object result = this.result;
	    return (M)result;
	}
	/*
	public static void main(String[] args) {
		System.out.println(-444444444444444L);
		int a=2;
		int b=0xffffffff & (~2+1); //-2
		int c=0;
		int d=-0;
		System.out.println(a);
		System.out.println(Integer.toHexString(b));
		System.out.println(c);
		System.out.println(d);
	}*/
}



