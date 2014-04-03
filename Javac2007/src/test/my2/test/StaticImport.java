package my.test;
//多了一条import语句，在import后面多加了“static”
import static my.test.TopLevelClass.*;

public class StaticImport {
	public static void main(String[] args) {
		//串冗长字符串：“TopLevelClass.” 已削除
		System.out.println(field_static);//TopLevelClass的“静态字段”
		method_static();                 //TopLevelClass的“静态方法”
		MemberClass_static c;            //TopLevelClass的“静态成员类”
	}
}