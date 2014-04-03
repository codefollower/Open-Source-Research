package my.test;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@interface ClassAnnotation {
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@interface MethodAnnotation {
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@interface FieldAnnotation {
}

 

//@ClassAnnotation @NotFoundAnnotation
@ClassAnnotation
public class ComputeAnnotationSetTest {
    @FieldAnnotation
    int fieldA;
    
    @MethodAnnotation
    void methodA() {}
}




