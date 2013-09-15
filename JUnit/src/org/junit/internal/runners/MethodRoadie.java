package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assume.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

//@BeforeClass与@AfterClass方法都只执行一次，
//如果有@Before和@After方法，那么每次执行一个@Test方法时都先执行所有的
//@Before方法，再执行@Test方法，然后执行所有的@After方法
//每次执行一个@Test方法时都要生成一个测试类的新实例
public class MethodRoadie {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final Object fTest;
	private final RunNotifier fNotifier;
	private final Description fDescription;
	private TestMethod fTestMethod;

	public MethodRoadie(Object test, TestMethod method, RunNotifier notifier, Description description) {
		DEBUG.P(this,"MethodRoadie(4)");
		DEBUG.P("test="+test); //方法所属类的对像
		DEBUG.P("method="+method);
		DEBUG.P("description="+description);

		fTest= test;
		fNotifier= notifier;
		fDescription= description;
		fTestMethod= method;

		DEBUG.P(0,this,"MethodRoadie(4)");
	}

	public void run() {
		try {//我加上的
		DEBUG.P(this,"run()");

		if (fTestMethod.isIgnored()) {
			fNotifier.fireTestIgnored(fDescription);
			return;
		}
		fNotifier.fireTestStarted(fDescription);
		try {
			long timeout= fTestMethod.getTimeout();
			if (timeout > 0)
				runWithTimeout(timeout);
			else
				runTest();
		} finally {
			fNotifier.fireTestFinished(fDescription);
		}

		}finally{//我加上的
		DEBUG.P(0,this,"run()");
		}
	}

	private void runWithTimeout(final long timeout) {
		try {//我加上的
		DEBUG.P(this,"runWithTimeout(1)");

		runBeforesThenTestThenAfters(new Runnable() {
		
			public void run() {
				ExecutorService service= Executors.newSingleThreadExecutor();
				Callable<Object> callable= new Callable<Object>() {
					public Object call() throws Exception {
						runTestMethod();
						return null;
					}
				};
				Future<Object> result= service.submit(callable);
				service.shutdown();
				try {
					boolean terminated= service.awaitTermination(timeout,
							TimeUnit.MILLISECONDS);
					if (!terminated)
						service.shutdownNow();
					result.get(0, TimeUnit.MILLISECONDS); // throws the exception if one occurred during the invocation
				} catch (TimeoutException e) {
					addFailure(new Exception(String.format("test timed out after %d milliseconds", timeout)));
				} catch (Exception e) {
					addFailure(e);
				}				
			}
		});

		}finally{//我加上的
		DEBUG.P(0,this,"runWithTimeout(1)");
		}
	}
	
	public void runTest() {
		try {//我加上的
		DEBUG.P(this,"runTest()");

		runBeforesThenTestThenAfters(new Runnable() {
			public void run() {
				runTestMethod();
			}
		});

		}finally{//我加上的
		DEBUG.P(0,this,"runTest()");
		}
	}

	public void runBeforesThenTestThenAfters(Runnable test) {
		try {//我加上的
		DEBUG.P(this,"runBeforesThenTestThenAfters(1)");

		try {
			runBefores();
			test.run();
		} catch (FailedBefore e) {
		} catch (Exception e) {
			throw new RuntimeException("test should never throw an exception to this level");
		} finally {
			runAfters();
		}
		
		}finally{//我加上的
		DEBUG.P(0,this,"runBeforesThenTestThenAfters(1)");
		}
	}
	
	protected void runTestMethod() {
		try {//我加上的
		DEBUG.P(this,"runTestMethod()");

		try {
			fTestMethod.invoke(fTest);
			if (fTestMethod.expectsException())
				addFailure(new AssertionError("Expected exception: " + fTestMethod.getExpectedException().getName()));
		} catch (InvocationTargetException e) {
			Throwable actual= e.getTargetException();
			if (actual instanceof AssumptionViolatedException)
				return;
			else if (!fTestMethod.expectsException())
				addFailure(actual);
			else if (fTestMethod.isUnexpected(actual)) {
				String message= "Unexpected exception, expected<" + fTestMethod.getExpectedException().getName() + "> but was<"
					+ actual.getClass().getName() + ">";
				addFailure(new Exception(message, actual));
			}
		} catch (Throwable e) {
			addFailure(e);
		}

		}finally{//我加上的
		DEBUG.P(0,this,"runTestMethod()");
		}
	}
	
	private void runBefores() throws FailedBefore {
		try {//我加上的
		DEBUG.P(this,"runBefores()");

		try {
			try {
				List<Method> befores= fTestMethod.getBefores();
				for (Method before : befores)
					before.invoke(fTest);
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

		List<Method> afters= fTestMethod.getAfters();
		for (Method after : afters)
			try {
				after.invoke(fTest);
			} catch (InvocationTargetException e) {
				addFailure(e.getTargetException());
			} catch (Throwable e) {
				addFailure(e); // Untested, but seems impossible
			}

		}finally{//我加上的
		DEBUG.P(0,this,"runAfters()");
		}
	}

	protected void addFailure(Throwable e) {
		try {//我加上的
		DEBUG.P(this,"addFailure(1)");

		fNotifier.fireTestFailure(new Failure(fDescription, e));

		}finally{//我加上的
		DEBUG.P(0,this,"addFailure(1)");
		}
	}
}

