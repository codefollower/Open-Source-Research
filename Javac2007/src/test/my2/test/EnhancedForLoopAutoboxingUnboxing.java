package my.test;

import java.util.ArrayList;
public class EnhancedForLoopAutoboxingUnboxing  {
	public static void main(String[] args) {
		int[] intArray={1,2,3,4,5};

		//在Java 1.4下我们用下面的方式打印intArray
		for(int i=0;i<intArray.length;i++)
			System.out.println(intArray[i]);

		//在Java 5.下我们用Enhanced for Loop简化上面的普通for Loop
		for(int i: intArray)
			System.out.println(i);
		/*
		编译器会把这个Enhanced for Loop转换成下面的普通for Loop
		以“$”结尾的局部变量名都是编译器自动命名的

		for (int arr$[] = intArray, len$ = arr$.length, i$ = 0; i$ < len$; ++i$) {
            int i = arr$[i$];
            System.out.println(i);
        }
		*/

		//下面这一行将被编译器转换成: ArrayList al = new ArrayList();
		//(注:编译器采用了一种叫“Type Erasure”的技术实现这个转换，以后再讨论这个问题)
		ArrayList<Integer> al=new ArrayList<Integer>();


		for(int i=0;i<intArray.length;i++)
			//这一行被编译器转换成: al.add(Integer.valueOf(intArray[i]));
			//也就是编译器完成了Autoboxing，把int数值包装成一个Integer实例
			al.add(intArray[i]);



		for(int i: al)
			System.out.println(i);
		/*
		编译器会把这个Enhanced for Loop转换成下面的普通for Loop
		以“$”结尾的局部变量名都是编译器自动命名的

		for (java.util.Iterator i$ = al.iterator(); i$.hasNext(); ) {
			//这里编译器完成了Unboxing，Integer实例转换成了int数值
            int i = ((Integer)i$.next()).intValue();
            System.out.println(i);
        }
		*/
	}
}


/*
public class EnhancedForLoopAutoboxingUnboxing {
    
    public EnhancedForLoopAutoboxingUnboxing() {
        super();
    }
    
    public static void main(String[] args) {
        int[] intArray = {1, 2, 3, 4, 5};
        for (int i = 0; i < intArray.length; i++) System.out.println(intArray[i]);
        for (int arr$[] = intArray, len$ = arr$.length, i$ = 0; i$ < len$; ++i$) {
            int i = arr$[i$];
            System.out.println(i);
        }
        ArrayList al = new ArrayList();
        for (int i =0 ; i < intArray.length; i++) al.add(Integer.valueOf(intArray[i]));
        for (java.util.Iterator i$ = al.iterator(); i$.hasNext(); ) {
            int i = ((Integer)i$.next()).intValue();
            System.out.println(i);
        }
    }
}*/