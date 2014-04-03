package test.attr.error;
class ExtendsTest<T> {
	static class InnerStaticClass {}
}
public class cant_select_static_class_from_param_type
             <T extends ExtendsTest<String>.InnerStaticClass> {}
