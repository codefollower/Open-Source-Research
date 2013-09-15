package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MethodValidator {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final List<Throwable> fErrors= new ArrayList<Throwable>();

	private TestClass fTestClass;

	public MethodValidator(TestClass testClass) {
		fTestClass = testClass;
	}

	public void validateInstanceMethods() {
		try {//我加上的
		DEBUG.P(this,"validateInstanceMethods()");

		validateTestMethods(After.class, false);
		validateTestMethods(Before.class, false);
		validateTestMethods(Test.class, false);
		
		List<Method> methods= fTestClass.getAnnotatedMethods(Test.class);
		//测试类必须至少包含一个带有@Test的方法

		DEBUG.P("methods.size()="+methods.size());
		if (methods.size() == 0)
			fErrors.add(new Exception("No runnable methods"));

		}finally{//我加上的
		DEBUG.P(0,this,"validateInstanceMethods()");
		}
	}

	public void validateStaticMethods() {
		try {//我加上的
		DEBUG.P(this,"validateStaticMethods()");

		validateTestMethods(BeforeClass.class, true);
		validateTestMethods(AfterClass.class, true);

		}finally{//我加上的
		DEBUG.P(0,this,"validateStaticMethods()");
		}
	}
	
	public List<Throwable> validateMethodsForDefaultRunner() {
		try {//我加上的
		DEBUG.P(this,"validateMethodsForDefaultRunner()");

		validateNoArgConstructor();
		validateStaticMethods();
		validateInstanceMethods();
		return fErrors;

		}finally{//我加上的
		DEBUG.P(0,this,"validateMethodsForDefaultRunner()");
		}
	}
	
	public void assertValid() throws InitializationError {
		try {//我加上的
		DEBUG.P(this,"assertValid()");

		DEBUG.P("fErrors="+fErrors);
		if (!fErrors.isEmpty())
			throw new InitializationError(fErrors);

		}finally{//我加上的
		DEBUG.P(0,this,"assertValid()");
		}
	}

	public void validateNoArgConstructor() {
		try {
			//必须有默认构造函数
			fTestClass.getConstructor();
		} catch (Exception e) {
			fErrors.add(new Exception("Test class should have public zero-argument constructor", e));
		}
	}
	private void validateTestMethods(Class<? extends Annotation> annotation,
			boolean isStatic) {
		List<Method> methods= fTestClass.getAnnotatedMethods(annotation);
		
		for (Method each : methods) {
			//带有@BeforeClass与@AfterClass的方法必须是static
			if (Modifier.isStatic(each.getModifiers()) != isStatic) {
				String state= isStatic ? "should" : "should not";
				fErrors.add(new Exception("Method " + each.getName() + "() "
						+ state + " be static"));
			}
			//测试类必须是public的
			if (!Modifier.isPublic(each.getDeclaringClass().getModifiers()))
				fErrors.add(new Exception("Class " + each.getDeclaringClass().getName()
						+ " should be public"));
			//测试方法必须是public的
			if (!Modifier.isPublic(each.getModifiers()))
				fErrors.add(new Exception("Method " + each.getName()
						+ " should be public"));
			//测试方法返回类型必须是void
			if (each.getReturnType() != Void.TYPE)
				fErrors.add(new Exception("Method " + each.getName()
						+ " should be void"));
			//测试方法不能带参数
			if (each.getParameterTypes().length != 0)
				fErrors.add(new Exception("Method " + each.getName()
						+ " should have no parameters"));
		}
	}
}
