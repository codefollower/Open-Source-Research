package my.test;

class Box<T> {}
class Shoe {}

class Book {}
class ComputerBook extends Book{}      //计算机书籍
class JavaBook extends ComputerBook{}  //与Java语言相关的计算机书籍

public class Storage {
	//第一种方式：把所有的箱子都放到小库房
	//“Box<?>”中的问号“？”是一个无限制的通配符(Wildcards)
	//在这里它表示贴有任何标签的箱子
	public static void putBox1(Box<?> box){}


	//第二种方式：只把某一类别的箱子放到小库房
	//“<? extends ComputerBook>”是一个有限制的通配符
	//在这里它表示所有贴有计算机书籍相关标签的箱子都可以放入小库房
	public static void putBox2(Box<? extends ComputerBook> box){}


	/*
	public static void putBox3(Box<? super ComputerBook> box){}
	可以认为是第三种方式，不过这无法用生活常识来解释，
	我们可以通过以下JavaBook类的继承关系图同时说明第二种方式与第三种方式的一些问题:
	java.lang.Object
	|_Book
	  |_ComputerBook
	    |_JavaBook
	  
	“<? extends ComputerBook>”是一个“Upper Bound”的通配符，
	它匹配继承关系图中的以下结点(从结点ComputerBook开始由上往下遍历):

	ComputerBook
	|_JavaBook

	ComputerBook是这个子继承关系图中的最高结点，
	这样对“Upper Bound”就比较好理解。
	  

	而“<? super ComputerBook>”是一个“Lower Bound”的通配符
	它匹配继承关系图中的以下结点(从结点ComputerBook开始由下往上遍历):

	java.lang.Object
	|_Book
	  |_ComputerBook

	ComputerBook是这个子继承关系图中的最低结点，
	这样对“Lower Bound”就比较好理解。

	(“Upper Bound”与“Lower Bound”是Javac1.7源码文档注释里经常出现的两个术语)
	*/
	public static void putBox3(Box<? super ComputerBook> box){}


	public static void main(String[] args) {
		Box<Shoe> shoeBox=new Box<Shoe>();
		Box<Book> bookBox=new Box<Book>();
		Box<ComputerBook> computerBookBox=new Box<ComputerBook>();
		Box<JavaBook> javaBookBox=new Box<JavaBook>();
		
		//第一种方式: putBox1(Box<?> box) 无限制的通配符(Wildcards)
		putBox1(shoeBox);
		putBox1(bookBox);
		putBox1(computerBookBox);
		putBox1(javaBookBox);

		//第二种方式: putBox2(Box<? extends ComputerBook> box) 有限制的通配符
		//在这里它表示所有贴有计算机书籍相关标签的箱子都可以放入小库房
		putBox2(computerBookBox);
		putBox2(javaBookBox);
		
		/*
		也可以用下面的继承关系图来理解编译错误：
		ComputerBook
		|_JavaBook

		*/
		//shoeBox是贴有Shoe标签的箱子，Shoe类不在上面的继承关系图中
		putBox2(shoeBox);   //编译错误，因为鞋箱不能放入专用计算机书箱库房

		//bookBox是贴有Book标签的箱子，Book类不在上面的继承关系图中
		putBox2(bookBox);   //编译错误，因为书箱太笼统，不能放入专用计算机书箱库房

		/*
		第三种方式: putBox3(Box<? super ComputerBook> box) 有限制的通配符
		这种方式无法用生活常识来解释，用下面的继承关系图来理解编译错误：
		java.lang.Object
		|_Book
		  |_ComputerBook
		*/
		putBox3(new Box<Object>());
		putBox3(bookBox);
		putBox3(computerBookBox);
		putBox3(javaBookBox); //编译错误，javaBookBox是贴有JavaBook标签的箱子，JavaBook类不在继承关系图中
		putBox3(shoeBox);     //编译错误，shoeBox是贴有Shoe标签的箱子，Shoe类不在继承关系图中
	}
}