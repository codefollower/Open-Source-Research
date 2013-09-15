/**
 * 
 */
package org.junit.internal.requests;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class IgnoredClassRunner extends Runner {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final Class<?> fTestClass;

	public IgnoredClassRunner(Class<?> testClass) {
		DEBUG.P(this,"IgnoredClassRunner(1)");
		DEBUG.P("testClass="+testClass);

		fTestClass= testClass;

		DEBUG.P(0,this,"IgnoredClassRunner(1)");
	}

	@Override
	public void run(RunNotifier notifier) {
		DEBUG.P(this,"run(1)");
		DEBUG.P("fTestClass="+fTestClass);

		notifier.fireTestIgnored(getDescription());

		DEBUG.P(0,this,"run(1)");
	}

	@Override
	public Description getDescription() {
		return Description.createSuiteDescription(fTestClass);
	}
}