package test.parser;
import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
//@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PackageAnnotations {
    String f1() default "str";
	int [] f2() default {1,2};
	Class f3() default PackageAnnotations.class;
	Target f4() default @Target(ElementType.FIELD);
}
//import java.lang.annotation.*;