package org.junit.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import org.junit.internal.runners.JUnit38ClassRunner;

/** Runner for use with JUnit 3.8.x-style AllTests classes
 * (those that only implement a static <code>suite()</code>
 * method). For example:
 * <pre>
 * &#064;RunWith(AllTests.class)
 * public class ProductTests {
 *    public static junit.framework.Test suite() {
 *       ...
 *    }
 * }
 * </pre>
 */
public class AllTests extends JUnit38ClassRunner {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	@SuppressWarnings("unchecked")
	public AllTests(Class<?> klass) throws Throwable {
		super(testFromSuiteMethod(klass));
	}

	public static Test testFromSuiteMethod(Class<?> klass) throws Throwable {
		try {//我加上的
		DEBUG.P(AllTests.class,"testFromSuiteMethod(1)");
		DEBUG.P("klass="+klass);

		Method suiteMethod= null;
		Test suite= null;
		try {
			suiteMethod= klass.getMethod("suite");
			DEBUG.P("suiteMethod="+suiteMethod);
			if (! Modifier.isStatic(suiteMethod.getModifiers())) {
				throw new Exception(klass.getName() + ".suite() must be static");
			}
			suite= (Test) suiteMethod.invoke(null); // static method

			DEBUG.P("suite="+suite);
		} catch (InvocationTargetException e) {
			DEBUG.P("e="+e);
			throw e.getCause();
		}
		return suite;

		}finally{//我加上的
		DEBUG.P(0,AllTests.class,"testFromSuiteMethod(1)");
		}
	}
}
