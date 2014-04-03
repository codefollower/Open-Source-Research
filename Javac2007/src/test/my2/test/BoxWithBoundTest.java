package my.test;

class Shoe {}
class Book {}
class ComputerBook extends Book{}      //计算机书籍

class BoxWithBound<T extends Book> {}

public class BoxWithBoundTest {
	public static void main(String[] args) {
		BoxWithBound<Shoe> shoeBox;  //“放鞋专用”的标签不符合你最初的本意

		//可以改成下面两种方式之一
		BoxWithBound<Book> bookBox;
		BoxWithBound<ComputerBook> computerBook;
	}
}