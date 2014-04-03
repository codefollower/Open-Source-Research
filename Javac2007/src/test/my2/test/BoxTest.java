package my.test;

class Box<T> {
	void put(T t) {}
}
class Book {}
class Shoe {}
class ComputerBook extends Book{}
public class BoxTest {
	public static void main(String[] args) {
		Box<Book> bookBox=new Box<Book>();
		//Shoe shoe=new Shoe();
		//bookBox.put(shoe);

		ComputerBook computerBook=new ComputerBook();
		bookBox.put(computerBook);
	}
}