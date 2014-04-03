//@SuppressWarnings({"fallthrough","unchecked"})
package test.attr.error;
import java.lang.annotation.*;


@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

//Override
@Deprecated
@MyAnnotation("test")
public class annotation_type_not_applicable {}
