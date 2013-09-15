package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestClass {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final Class<?> fClass;
	
	public TestClass(Class<?> klass) {
		DEBUG.P(this,"TestClass(1)");
		DEBUG.P("klass="+klass);

		fClass= klass;

		DEBUG.P(0,this,"TestClass(1)");
	}

	List<Method> getTestMethods() {
		try {//我加上的
		DEBUG.P(this,"getTestMethods()");

		return getAnnotatedMethods(Test.class);

		}finally{//我加上的
		DEBUG.P(0,this,"getTestMethods()");
		}
	}

	//注意和TestMethod类中定义的getBefores()的区别，
	//这里是指用@BeforeClass标注的方法
	List<Method> getBefores() {
		return getAnnotatedMethods(BeforeClass.class);
	}

	List<Method> getAfters() {
		return getAnnotatedMethods(AfterClass.class);
	}
	
	public List<Method> getAnnotatedMethods(Class<? extends Annotation> annotationClass) {
		try {//我加上的
		DEBUG.P(this,"getAnnotatedMethods(1)");
		DEBUG.P("annotationClass="+annotationClass);

		List<Method> results= new ArrayList<Method>();
		for (Class<?> eachClass : getSuperClasses(fClass)) {

			DEBUG.P("eachClass="+eachClass);

			Method[] methods= eachClass.getDeclaredMethods();
			for (Method eachMethod : methods) {
				Annotation annotation= eachMethod.getAnnotation(annotationClass);

				//DEBUG.P("eachMethod="+eachMethod);
				//DEBUG.P("annotation="+annotation);

				//DEBUG.P("(annotation != null && ! isShadowed(eachMethod, results)="+(annotation != null && ! isShadowed(eachMethod, results)));

				//超类带有@Test的方法被子类覆盖了
				if (annotation != null && ! isShadowed(eachMethod, results)) 
					results.add(eachMethod);
			}
		}

		DEBUG.P("results="+results);

		//一个测试类中可以出现多个带有@Before或@BeforeClass的方法，
		//这些方法的执行顺序是从下往上执行，也就是与方法的声明顺序刚好相反。

		if (runsTopToBottom(annotationClass))
			Collections.reverse(results);

		DEBUG.P("results="+results);

		return results;

		}finally{//我加上的
		DEBUG.P(0,this,"getAnnotatedMethods(1)");
		}
	}

	private boolean runsTopToBottom(Class< ? extends Annotation> annotation) {
		return annotation.equals(Before.class) || annotation.equals(BeforeClass.class);
	}
	
	private boolean isShadowed(Method method, List<Method> results) {
		for (Method each : results) {
			if (isShadowed(method, each))
				return true;
		}
		return false;
	}

	private boolean isShadowed(Method current, Method previous) {
		if (! previous.getName().equals(current.getName()))
			return false;
		if (previous.getParameterTypes().length != current.getParameterTypes().length)
			return false;
		for (int i= 0; i < previous.getParameterTypes().length; i++) {
			if (! previous.getParameterTypes()[i].equals(current.getParameterTypes()[i]))
				return false;
		}
		return true;
	}

	private List<Class<?>> getSuperClasses(Class< ?> testClass) {
		ArrayList<Class<?>> results= new ArrayList<Class<?>>();
		Class<?> current= testClass;
		while (current != null) {
			results.add(current);
			current= current.getSuperclass();
		}
		return results;
	}

	public Constructor<?> getConstructor() throws SecurityException, NoSuchMethodException {
		return fClass.getConstructor();
	}

	public Class<?> getJavaClass() {
		return fClass;
	}

	public String getName() {
		return fClass.getName();
	}

}
