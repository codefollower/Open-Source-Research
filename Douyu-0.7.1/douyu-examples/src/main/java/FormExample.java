@douyu.mvc.Controller
public class FormExample {
	public void save(String name, int age, java.io.PrintWriter out) {
		out.println("name = " + name);
		out.println("age  = " + age);
	}
}