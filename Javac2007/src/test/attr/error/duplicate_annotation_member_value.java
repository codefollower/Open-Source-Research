package test.attr.error;
@interface MyAnnotation {
    String value();
}
@MyAnnotation(value="testA",value="testB")
public class duplicate_annotation_member_value  {}

