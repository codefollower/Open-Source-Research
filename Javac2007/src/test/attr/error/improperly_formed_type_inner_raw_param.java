package test.attr.error;
public class improperly_formed_type_inner_raw_param {
	public class OuterClass<T> {
		public class InnerClass<V> {}

		public void method() {
			//this type is either fully parameterized, or not parameterized at all.
			OuterClass.InnerClass<String> innerClassA=new InnerClass<String>();
			OuterClass<?>.InnerClass<String> innerClassB=new InnerClass<String>();
		}
	}
}

/*
bin\mysrc\my\error\improperly_formed_type_inner_raw_param.java:8: 类型的格式不正
确，给出了普通类型的类型参数
         OuterClass.InnerClass<String> innerClassA=new InnerClass<String>();
                              ^
1 错误
*/