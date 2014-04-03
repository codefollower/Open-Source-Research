package my.test;

public class Varargs {
	public static void main(String[] args) {
		print("str0");
		print("str1",1);
		print("str2",1,2);
	}
	
	//把省略符号“...”加在最后一个方法参数的类型名称(这里是“int”)后面，
	//这就等于告诉编译器:print是一个可变参数的方法，除了第一个方法参数是String
	//类型外，剩下的方法参数的类型都是int类型
	public static void print(String str, int... args) {
		System.out.println("str="+str+" args.length="+args.length );
	}

	/*
	输出结果是:
	str=str0 args.length=0
	str=str1 args.length=1
	str=str2 args.length=2
	
	经过编译器转换后的代码像下面这样:

	public static void main(String[] args) {
		print("str0", new int[]{});
		print("str1", new int[]{1});
		print("str2", new int[]{1, 2});
	}
    
    public static void print(String str, int[] args) {
        System.out.println("str=" + str + " args.length=" + args.length);
    }
	*/
}
