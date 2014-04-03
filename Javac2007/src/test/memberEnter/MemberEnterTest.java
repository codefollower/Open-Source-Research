package test.memberEnter;


import java.lang.annotation.*;

//import static test.memberEnter.subdir.UniqueImport.*;
//import static test.memberEnter.subdir2.UniqueImport.*;


//@AnnotationA(f1=10,f2=2)
@AnnotationA(f1=10,f2=2,f3=MemberEnterTest.class)
@Deprecated
//class MemberEnterTest<T,V extends AnnotationA> {
//class MemberEnterTest<T,V extends MemberClassB> {
class GenericsClass<A>{}
//class MemberEnterTest<T,V extends AnnotationA> extends GenericsClass<?>{
//class MemberEnterTest<T,V extends AnnotationA> extends int {
//class MemberEnterTest<T,V extends AnnotationA> extends T {
//final class finalClass{}
//class MemberEnterTest<T,V extends AnnotationA> extends finalClass {
//class CyclicClass extends MemberEnterTest{}
//class MemberEnterTest extends CyclicClass {
//class MemberEnterTest extends MemberEnterTest {
//class MemberEnterTest<V extends T,T extends V> extends finalClass {
//class MemberEnterTest<T,V extends AnnotationA> extends AnnotationA{
//class MemberEnterTest<T,V extends AnnotationA> extends MemberClassC{
//class MemberEnterTest<T,V extends AnnotationA> extends UniqueImport{
//class MemberEnterTest<T,V extends AnnotationA> extends T{
//class MemberEnterTest<T,V extends AnnotationA> extends V{
class MemberEnterTest<T,V extends GenericsClass<?>&GenericsClass<? extends V>> {
	public class MemberClassB{}

	/*
	int f1;
	int f2=10;
	final int f3=20;
	static int f4=30;
	static final int f5=40;

	<T> void m1(int i1,int i2) throws T{}
	*/
}
@Target({ElementType.FIELD, ElementType.METHOD})
@interface AnnotationA{
	int f1();
	int f2();
	Class f3();
}

/*
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value();
}

@MyAnnotation("test")
class annotation_type_not_applicable {}
*/