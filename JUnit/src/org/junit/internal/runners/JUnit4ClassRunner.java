package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;

public class JUnit4ClassRunner extends Runner implements Filterable, Sortable {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final List<Method> fTestMethods;
	private TestClass fTestClass;

	public JUnit4ClassRunner(Class<?> klass) throws InitializationError {
		DEBUG.P(this,"JUnit4ClassRunner(1)");
		DEBUG.P("klass="+klass);
		//DEBUG.e();

		fTestClass= new TestClass(klass);
		fTestMethods= getTestMethods();

		//DEBUG.P("fTestMethods="+fTestMethods);

		for(Method m: fTestMethods)
			DEBUG.P("m="+m);

		validate();

		DEBUG.P(0,this,"JUnit4ClassRunner(1)");
	}
	
	//所有的@Test方法，包括超类中未被子类覆盖的方法
	protected List<Method> getTestMethods() {
		return fTestClass.getTestMethods();
	}
	
	//验证测试方法及测试类的有效性
	protected void validate() throws InitializationError {
		DEBUG.P(this,"validate()");
		MethodValidator methodValidator= new MethodValidator(fTestClass);
		methodValidator.validateMethodsForDefaultRunner();
		methodValidator.assertValid();

		DEBUG.P(0,this,"validate()");
	}

	@Override
	public void run(final RunNotifier notifier) {
		try {//我加上的
		DEBUG.P(this,"run(1)");

		new ClassRoadie(notifier, fTestClass, getDescription(), new Runnable() {
			public void run() {
				runMethods(notifier);
			}
		}).runProtected();

		}finally{//我加上的
		DEBUG.P(0,this,"run(1)");
		}
	}

	protected void runMethods(final RunNotifier notifier) {
		try {//我加上的
		DEBUG.P(this,"runMethods(1)");

		for (Method method : fTestMethods)
			invokeTestMethod(method, notifier);

		}finally{//我加上的
		DEBUG.P(0,this,"runMethods(1)");
		}
	}

	@Override
	public Description getDescription() {
		try {//我加上的
		DEBUG.P(this,"getDescription()");

		Description spec= Description.createSuiteDescription(getName(), classAnnotations());
		List<Method> testMethods= fTestMethods;
		for (Method method : testMethods)
			spec.addChild(methodDescription(method));

		DEBUG.P("spec="+spec);
		return spec;

		}finally{//我加上的
		DEBUG.P(0,this,"getDescription()");
		}
	}

	//测试类带有的注释，比如@Ignore或其他自定义的注释
	protected Annotation[] classAnnotations() {
		return fTestClass.getJavaClass().getAnnotations();
	}

	protected String getName() {
		return getTestClass().getName();
	}
	
	protected Object createTest() throws Exception {
		return getTestClass().getConstructor().newInstance();
	}

	protected void invokeTestMethod(Method method, RunNotifier notifier) {
		try {//我加上的
		DEBUG.P(this,"invokeTestMethod(2)");

		Description description= methodDescription(method);
		Object test;
		try {
			test= createTest();

			DEBUG.P("test="+test);
		} catch (InvocationTargetException e) {
			notifier.testAborted(description, e.getCause());
			return;			
		} catch (Exception e) {
			notifier.testAborted(description, e);
			return;
		}
		TestMethod testMethod= wrapMethod(method);
		new MethodRoadie(test, testMethod, notifier, description).run();

		}finally{//我加上的
		DEBUG.P(0,this,"invokeTestMethod(2)");
		}
	}

	protected TestMethod wrapMethod(Method method) {
		return new TestMethod(method, fTestClass);
	}

	//测试方法名
	protected String testName(Method method) {
		return method.getName();
	}

	protected Description methodDescription(Method method) {
		return Description.createTestDescription(getTestClass().getJavaClass(), testName(method), testAnnotations(method));
	}

	protected Annotation[] testAnnotations(Method method) {
		return method.getAnnotations();
	}

	public void filter(Filter filter) throws NoTestsRemainException {
		for (Iterator<Method> iter= fTestMethods.iterator(); iter.hasNext();) {
			Method method= iter.next();
			if (!filter.shouldRun(methodDescription(method)))
				iter.remove();
		}
		if (fTestMethods.isEmpty())
			throw new NoTestsRemainException();
	}

	public void sort(final Sorter sorter) {
		Collections.sort(fTestMethods, new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				return sorter.compare(methodDescription(o1), methodDescription(o2));
			}
		});
	}

	protected TestClass getTestClass() {
		return fTestClass;
	}
}