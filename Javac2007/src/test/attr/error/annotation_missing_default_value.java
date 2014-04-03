package test.attr.error;
@interface MyAnnotation {
    String valueA();
	String valueB() default "testB";
	String valueC();
	String valueD();
}
@MyAnnotation(valueA="testA")
public class annotation_missing_default_value  {}

