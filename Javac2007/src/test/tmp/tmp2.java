import java.io.*;
//import my.test.ParserTest.*;
//import java.lang.annotation.*;
import java.util.zip.*;
import java.util.*;
import java.text.MessageFormat;
public class tmp<V> {
	class InnerClassA{
		int f1=1;
		int m1(){
			f1=10;
			return f1*10;
		}

		InnerClassA(int i1,int i2) {
			this(
				new InnerClassA(10,20) {
					int Anonf1 = m1();

					int out() {
						System.out.println(Anonf1);
						return Anonf1;
					}
				}.out()
			);
		}
		//InnerClassA(InnerClassA at) {}
		InnerClassA(int i1) {}
	}

	A a=new A();
	A a222=new A();
	B b=(B)a;
	A a2=(A)b;

	/*
	static void fromArrayToCollection(Object[] a, Collection<?> c) {

		for (Object o : a) {

			c.add(o); // 编译期错误

		}

	}
	*/

	static <T extends V> void fromArrayToCollection(T[] a, Collection<T> c){

       for (T o : a) {

           c.add(o); // correct

       }

    }

	static {
		Object[] oa = new Object[100];

      Collection<Object> co = new ArrayList<Object>();

      fromArrayToCollection(oa, co);// T 指Object

      String[] sa = new String[100];

      Collection<String> cs = new ArrayList<String>();

      fromArrayToCollection(sa, cs);// T inferred to be String

      fromArrayToCollection(sa, co);// T inferred to be Object

      Integer[] ia = new Integer[100];

      Float[] fa = new Float[100];

      Number[] na = new Number[100];

      Collection<Number> cn = new ArrayList<Number>();

      fromArrayToCollection(ia, cn);// T inferred to be Number

      fromArrayToCollection(fa, cn);// T inferred to be Number

      fromArrayToCollection(na, cn);// T inferred to be Number

      fromArrayToCollection(na, co);// T inferred to be Object

      fromArrayToCollection(na, cs);// compile-time error


	}





	void test(List<? extends Number> list)
	{
		//list.set(0,new Integer(0));
		//list.add(0,new Integer(0));
		list.add(new Integer(0));
	}

	void test()
	{
		//Collection<?> c = new ArrayList<String>();
		List<?> c = new ArrayList<String>();
		//Collection<? extends Object> c = new ArrayList<String>();

		//c.add(null);
		//c.add(new Object()); // 编译时错误
		//String s = c.get(0); // 编译时错误
		Object o = c.get(0);


		a2.f=10;
		
		int i = ((B)a222).m();

		System.err.println(i);
		//a2.m();
	}
	A a3=(A)new B();
	
	static class A {
		int f;
	}
	static class B extends A {
		int m() {
			return f;
		}
	}

	static class C extends A {
		int m() {
			return f;
		}
	}

	final static class D extends A {
	}

	static void cast() {
		A a=new A();
		B b=new B();
		a=b;
		//C c=(C)a;
		D d=new D();
		a=(A)d;
	}

	public void getMethod(Class<?>[] parameterTypes) {}

	
    private static int i=10;
    public static void main(String[] args) throws Exception {
		boolean bb=true,cc=false;
		boolean dd = bb&cc;
		System.out.println("dd: "+dd);
		          // An example of overflow:

          double d = 1e308;

          System.out.print("overflow produces infinity: ");

          System.out.println(d + "*10==" + d*10);

          // An example of gradual underflow:

          d = 1e-305 * Math.PI;

          System.out.print("gradual underflow: " + d + "\n   ");

          for (int i = 0; i < 4; i++)

             System.out.print(" " + (d /= 100000));

          System.out.println();

          // An example of NaN:

          System.out.print("0.0/0.0 is Not-a-Number: ");

          d = 0.0/0.0;

          System.out.println(d);

          // An example of inexact results and rounding:

          System.out.print("inexact results with float:");

          for (int i = 0; i < 100; i++) {

             float z = 1.0f / i;

             if (z * i != 1.0f)

               System.out.print(" " + i);

          }

          System.out.println();

          // Another example of inexact results and rounding:

          System.out.print("inexact results with double:");

          for (int i = 0; i < 100; i++) {

             double z = 1.0 / i;

             if (z * i != 1.0)

               System.out.print(" " + i);

          }

          System.out.println();

          // An example of cast to integer rounding:

          System.out.print("cast to int rounds toward 0: ");

          d = 12345.6;

          System.out.println((int)d + " " + (int)(-d));



	  /*
		int i = 1000000;

          System.out.println(i * i);
		  System.out.println((int)(i * (long)i));
		  System.out.println(i * (long)i);

          long l = i;

          System.out.println(l * l);

		  //System.out.println((boolean)i);

          System.out.println(20296 / (l - i));
	  */

		new tmp().test();
		cast();
	    /*
        System.out.println(i);
        System.out.println(new String("中文").length());
        
        System.out.println("false!=false => "+(false!=false));
        System.out.println("false!=true  => "+(false!=true));
        System.out.println("true!=false  => "+(true!=false));
        System.out.println("true!=true   => "+(true!=true));
        
        byte[] b={(byte)1,(byte)3,(byte)255};
       
   			System.out.println(Integer.toBinaryString(((b[0] & 0xFF) << 8)));
        System.out.println((char)(((b[0] & 0xFF) << 8) + (b[1] & 0xFF)));
         System.out.println((char)(((b[0]) << 8) + (b[1])));
	 
	 int \uD800\uDC00;
	 \uD800\uDC00=99;
	 for(int j=0;j<15;j++) System.out.println(j & (i-1));
	 System.out.println(Character.isHighSurrogate('\uD800'));
	 System.out.println(Character.isHighSurrogate('a'));*/
	 //System.out.println(Integer.toHexString('\\'));
	 //String defaultEncodingName =
         //       new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();
	 //System.out.println("defaultEncodingName="+defaultEncodingName);
/*
	 System.out.println(!true);
	 //System.out.println(!'2');
	 System.out.println(-16>>>2);
	 System.out.println(-16>>2);

	 {
		MemberClassE me=new MemberClassE();
		MemberClassE.MemberClassF<Long> mf=me.new <String>MemberClassF<Long>("str");
		//类型的格式不正确，缺少某些参数(在Check类中检查)
		//MemberClassE.MemberClassF mf=me.new <String>MemberClassF<Long>("str");
	}


		for (int i = 0; i < args.length; i++) {
			System.out.print(i == 0 ? args[i] : " " + args[i]);
			System.out.println();
		}*/


/*
		Float n;
		String proper="1.1754943508222875E-33333f";
		proper="1.1754943508222875E+33333f";
		//n=1.1754943508222875E+33333f;
		//proper="1.1754943508r222875E-33333f";
		//n = Float.NaN;
		//n = Float.MIN_NORMAL-Float.MIN_NORMAL;
		//n=1.1754943r508222875E-33333f;
		//System.out.println(n.floatValue());

		try {
			n = Float.valueOf(proper);
			System.out.println(n);
			System.out.println(n.floatValue());
			System.out.println();
		} catch (NumberFormatException ex) {
			n = Float.NaN;
		}

		System.out.println(n);
		System.out.println(n.floatValue());

		System.out.println('c'+0);
		System.out.println(Literal('c'));
		System.out.println(Literal('c'+0));
*/
		/*
		tmp.java:76: 对 method 的引用不明确，tmp 中的 方法 method(int,java.lang.Integer)
		 和 tmp 中的 方法 method(int,java.lang.Long) 都匹配
						method(12,null);
						^
		1 错误
		*/
		//method(12,null);

		//method(12,(Integer)null);

		//System.out.println("java.class.path="+System.getProperty("java.class.path"));
		//method2(12);
		
		/*
		for (Enumeration<? extends ZipEntry> e = new ZipFile("test/jar/JarUnnamedPackage.jar").entries(); e.hasMoreElements(); ) {
                ZipEntry entry;
                try {
                    entry = e.nextElement();
					System.out.println("entry="+entry);
                } catch (InternalError ex) {
                    IOException io = new IOException();
                    io.initCause(ex); // convenience constructors added in Mustang :-(
                    throw io;
                }
            String name = entry.getName();
            System.out.println("name="+name);
            int i = name.lastIndexOf('/');
			System.out.println("name.lastIndexOf('/')="+i);
            String dirname = name.substring(0, i+1);
            System.out.println("dirname="+dirname);
            String basename = name.substring(i+1);
			System.out.println("basename="+basename);
            System.out.println("basename.length()="+basename.length());

        }
		

		System.out.println("isPosZero(1.0)="+isPosZero(1.0));
		System.out.println("isPosZero(0.0)="+isPosZero(0.0));
		System.out.println("isPosZero(-0.0)="+isPosZero(-0.0));

		System.out.println("isZero(1.0)="+isZero(1.0));
		System.out.println("isZero(0.0)="+isZero(0.0));
		System.out.println("isZero(-0.0)="+isZero(-0.0));

		System.out.println("(0.0f==-0.0f)="+(0.0f==-0.0f));
		System.out.println("(1.0f/0.0f)="+(1.0f/0.0f));
		System.out.println("(1.0f/-0.0f)="+(1.0f/-0.0f));
		System.out.println("(0.0d==-0.0d)="+(0.0d==-0.0d));
		System.out.println("(1.0d/0.0d)="+(1.0d/0.0d));
		System.out.println("(1.0d/-0.0d)="+(1.0d/-0.0d));

		*/
		//boolean b1=true;
		//while(b1) b1=false;

		//for(;;) break;

		//methodJ(1);

		//int i=~3;

		//new String();
		//b1?1:2;

		//枚举类型不能为本地类型
		//enum MyEnum {}
		//final enum MyEnum {}
		//@MyAnnotation enum MyEnum {
		//	//m1,m2;
		//}
		//MyEnum me=MyEnum.m1;
		//MyEnum me;
		//MyEnum me=new MyEnum();
		//int b[][]=new int[2][];
		//int c[][]=new int[2][][3];
		//int d[][]=new int[][];
		//int e[]=new int[];
		//b[1][0]=10;

		//int a=2,b=2;
		//a=a+(b&a)/b-++a/b > 0 ? b=10 :1;
		//System.out.println("a="+a);
		//System.out.println(MessageFormat.format("参数{0},参数{0},参数{1},参数{3},参数{2}",1,2,3,4));
	}
/*
 interface I<X> {   
      public void query(Map <String, Object> map, int count);   
	  public void query2(String map, int count); 
}
//class C implements I<C> {  
 class C implements I {   
      public void query(Map <String, Object> map, int count) {}   
	  public void query2(String map, int count) {}
}   
*/
//@interface MyAnnotation {
//}
	static void methodJ(int iii) throws Exception {
		//lable1:
		while(true) {
			try {
				iii++;
				if(iii<10) continue;
				//break;
				System.out.println("iii="+iii);
				return;
			} finally {
				System.out.println("finally iii="+iii);
				Thread.sleep(2000);
				//continue lable1;
				continue;
			}
		}
		//System.out.println("iii="+iii);
	}

	static Object Literal(Object value) { return value;}


	static void method(int i,Integer in){}
	static void method(int i,Long lo){}

	//static void method2(int i) throws CompletionFailure{}

	//RuntimeException及其子类无需事先catch或throws
	//public static class CompletionFailure extends RuntimeException {}

	//Exception需事先catch或throws
	//public static class CompletionFailure extends Exception {}


			private static boolean isPosZero(float x) {
				return x == 0.0f && 1.0f / x > 0.0f;
			}
			/** Return true iff double number is positive 0.
			 */
			private static boolean isPosZero(double x) {
				return x == 0.0d && 1.0d / x > 0.0d;
			}

			private static boolean isZero(float x) {
				return x == 0.0f;
			}
			/** Return true iff double number is positive 0.
			 */
			private static boolean isZero(double x) {
				return x == 0.0d;
			}
}
