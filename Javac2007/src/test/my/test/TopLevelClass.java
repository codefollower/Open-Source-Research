package my.test;

class TopLevelInterface {}             //TopLevelInterface是一个“顶层接口”
class TopLevelEnum {}                  //TopLevelEnum是一个“顶层枚举类”

public class TopLevelClass {           //TopLevelClass是一个“顶层类”
	private int field;                 //field是一个“非静态字段”
	public static int field_static;   //field_static是一个“静态字段”
	
	public class MemberClass {}        //MemberClass是一个“非静态成员类”

	//MemberClass_static是一个“静态成员类”
	public static class MemberClass_static {}
	
	public interface MemberInterface {}//MemberInterface是一个“成员接口”

	public enum MemberEnum {}          //MemberEnum是一个“成员枚举类”

	public TopLevelClass() {}          //TopLevelClass()是一个“构造方法”
	
	//method是一个“非静态方法”
	public void method(int arg) {      //arg是一个“方法参数”
		int variable;                  //variable是一个“局部变量”
		class LocalClass {}            //LocalClass是一个“本地类”
	}
	//method_static是一个“静态方法”
	public static void method_static() {}
}