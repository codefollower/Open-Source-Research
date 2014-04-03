package test.attr.error;
class ExtendsTest<T> {
	class InnerClass<V> {}
}
public class improperly_formed_type_param_missing 
             <T extends ExtendsTest<String>.InnerClass> {}
