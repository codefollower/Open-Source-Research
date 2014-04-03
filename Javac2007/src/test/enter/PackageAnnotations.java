package my.test;
import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
//@Target({ElementType.FIELD, ElementType.METHOD})
@interface PackageAnnotations {
    String value();
}