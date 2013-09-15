package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assume.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class ClassRoadie {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private RunNotifier fNotifier;
	private TestClass fTestClass;
	private Description fDescription;
	private final Runnable fRunnable;
	
	public ClassRoadie(RunNotifier notifier, TestClass testClass,
			Description description, Runnable runnable) {
		DEBUG.P(this,"ClassRoadie(4)");
		DEBUG.P("testClass="+testClass);
		DEBUG.P("description="+description);

		fNotifier= notifier;
		fTestClass= testClass;
		fDescription= description;
		fRunnable= runnable;

		DEBUG.P(0,this,"ClassRoadie(4)");
	}

	protected void runUnprotected() {
		DEBUG.P(this,"runUnprotected()");

		fRunnable.run();

		DEBUG.P(0,this,"runUnprotected()");
	};

	protected void addFailure(Throwable targetException) {
		try {//我加上的
		DEBUG.P(this,"addFailure(1)");

		fNotifier.fireTestFailure(new Failure(fDescription, targetException));

		}finally{//我加上的
		DEBUG.P(0,this,"addFailure(1)");
		}
	}

	public void runProtected() {
		try {//我加上的
		DEBUG.P(this,"runProtected()");

		try {
			runBefores();
			runUnprotected();
		} catch (FailedBefore e) {
		} finally {
			runAfters();
		}

		}finally{//我加上的
		DEBUG.P(0,this,"runProtected()");
		}
	}

	private void runBefores() throws FailedBefore {
		try {//我加上的
		DEBUG.P(this,"runBefores()");

		try {
			try {
				//运行@BeforeClass方法
				List<Method> befores= fTestClass.getBefores();
				for (Method before : befores) {
					DEBUG.P("before="+before);
					before.invoke(null);
				}
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (AssumptionViolatedException e) {
			throw new FailedBefore();
		} catch (Throwable e) {
			addFailure(e);
			throw new FailedBefore();
		}

		}finally{//我加上的
		DEBUG.P(0,this,"runBefores()");
		}
	}

	private void runAfters() {
		try {//我加上的
		DEBUG.P(this,"runAfters()");

		List<Method> afters= fTestClass.getAfters();
		for (Method after : afters)
			try {
				DEBUG.P("after="+after);

				after.invoke(null);
			} catch (InvocationTargetException e) {
				addFailure(e.getTargetException());
			} catch (Throwable e) {
				addFailure(e); // Untested, but seems impossible
			}

		}finally{//我加上的
		DEBUG.P(0,this,"runAfters()");
		}
	}
}