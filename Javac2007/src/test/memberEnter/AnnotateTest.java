
package test.memberEnter;


import java.lang.annotation.*;

import static test.memberEnter.subdir.UniqueImport.*;
import static test.memberEnter.subdir.UniqueImport.MemberClassB;
import static test.memberEnter.subdir.UniqueImport.staticFieldA;
import static test.memberEnter.subdir2.UniqueImport.staticFieldA;
//import static test.memberEnter.subdir2.UniqueImport.*;

import static test.memberEnter.EnumTest.*;
@AnnotationB
@AnnotationC
//@AnnotationA(toString=10,f2=2)
//@AnnotationA(f1=7*staticFieldA,f2=2)
//@AnnotationA(f1=7*9,f2=2)
//@AnnotationA(f1=10,f2=2,f3=MemberEnterTest.class)

//@AnnotationA(f1=1,f2=2,f4=new int[]{1,2})

//@AnnotationA(f1=1,f2=2,f6=90)
@AnnotationA(f1=1,f2=2,f6=A)
@Deprecated
class AnnotateTest{}


//@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@interface AnnotationA{
	int f1();
	int f2();
	//Class f3() default null;
	Class f3() default AnnotationA.class;

	int[] f4() default {0,1,2};

	//AnnotationB f5() default null;

	//EnumTest f6() default A;
	//EnumTest f6() default EnumTest.A;
	EnumTest f6() default this.A;
}
enum EnumTest{
	A,B,C;
}
class AnnotationB{
}

class AnnotationC implements Annotation{
}

/*
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

@MyAnnotation("test")
class annotation_type_not_applicable {}
*/