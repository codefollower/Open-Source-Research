package my.test;
import java.lang.annotation.*;

/**
 * Used to provide version info for a class.
 */
//@Version("@(#)Version.java	1.4 07/03/21")
@Version(name="@(#)Version.java	1.4 07/03/21")
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
//
//@Version
public @interface Version{
	String name();
    //String value() default "Unknow";//
	//String value2(int i);
}
/*
@Version("@(#)Version.java	1.4 07/03/21")
interface Version2{
    String value();
}
@Version("@(#)Version.java	1.4 07/03/21")
enum V {}
@Version("@(#)Version.java	1.4 07/03/21")
class Tss
{
}*/