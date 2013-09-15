package org.junit.internal;

import java.io.PrintStream;
import java.text.NumberFormat;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TextListener extends RunListener {

	private final PrintStream fWriter;

	public TextListener() {
		this(System.out);
	}

	public TextListener(PrintStream writer) {
		this.fWriter= writer;
	}

	@Override
	public void testRunFinished(Result result) {
		printHeader(result.getRunTime());
		printFailures(result);
		printFooter(result);
	}

	@Override
	public void testStarted(Description description) {
		fWriter.append('.');
	}

	@Override
	public void testFailure(Failure failure) {
		fWriter.append('E');
	}
	
	@Override
	public void testIgnored(Description description) {
		fWriter.append('I');
	}
	
	/*
	 * Internal methods
	 */

	private PrintStream getWriter() {
		return fWriter;
	}

	protected void printHeader(long runTime) {
		getWriter().println();
		getWriter().println("Time: " + elapsedTimeAsString(runTime));
	}

	protected void printFailures(Result result) {
		if (result.getFailureCount() == 0)
			return;
		if (result.getFailureCount() == 1)
			getWriter().println("There was " + result.getFailureCount() + " failure:");
		else
			getWriter().println("There were " + result.getFailureCount() + " failures:");
		int i= 1;
		for (Failure each : result.getFailures())
			printFailure(each, i++);
	}

	protected void printFailure(Failure failure, int count) {
		printFailureHeader(failure, count);
		printFailureTrace(failure);
	}

	protected void printFailureHeader(Failure failure, int count) {
		getWriter().println(count + ") " + failure.getTestHeader());
	}

	protected void printFailureTrace(Failure failure) {
		getWriter().print(failure.getTrace());
	}

	protected void printFooter(Result result) {
		if (result.wasSuccessful()) {
			getWriter().println();
			getWriter().print("OK");
			getWriter().println(" (" + result.getRunCount() + " test" + (result.getRunCount() == 1 ? "" : "s") + ")");

		} else {
			getWriter().println();
			getWriter().println("FAILURES!!!");
			getWriter().println("Tests run: " + result.getRunCount() + ",  Failures: " + result.getFailureCount());
		}
		getWriter().println();
	}

	/**
	 * Returns the formatted string of the elapsed time. Duplicated from
	 * BaseTestRunner. Fix it.
	 */
	protected String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000);
	}
}

/*

打印样例:
----------------------------------
JUnit version 4.4
.I...
Time: 0.078

OK (4 tests)


----------------------------------
JUnit version 4.4
.I.E..
Time: 0.078
There was 1 failure:
1) m2(org.junit.samples.JUnit4ClassRunnerTest)
java.lang.AssertionError: expected:<12> but was:<32>
	at org.junit.Assert.fail(Assert.java:74)
	at org.junit.Assert.failNotEquals(Assert.java:448)
	at org.junit.Assert.assertEquals(Assert.java:102)
	at org.junit.Assert.assertEquals(Assert.java:323)
	at org.junit.Assert.assertEquals(Assert.java:319)
	at org.junit.samples.JUnit4ClassRunnerTest.m2(JUnit4ClassRunnerTest.java:186)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at org.junit.internal.runners.TestMethod.invoke(TestMethod.java:59)
	at org.junit.internal.runners.MethodRoadie.runTestMethod(MethodRoadie.java:142)
	at org.junit.internal.runners.MethodRoadie$2.run(MethodRoadie.java:109)
	at org.junit.internal.runners.MethodRoadie.runBeforesThenTestThenAfters(MethodRoadie.java:124)
	at org.junit.internal.runners.MethodRoadie.runTest(MethodRoadie.java:107)
	at org.junit.internal.runners.MethodRoadie.run(MethodRoadie.java:58)
	at org.junit.internal.runners.JUnit4ClassRunner.invokeTestMethod(JUnit4ClassRunner.java:136)
	at org.junit.internal.runners.JUnit4ClassRunner.runMethods(JUnit4ClassRunner.java:80)
	at org.junit.internal.runners.JUnit4ClassRunner$1.run(JUnit4ClassRunner.java:66)
	at org.junit.internal.runners.ClassRoadie.runUnprotected(ClassRoadie.java:37)
	at org.junit.internal.runners.ClassRoadie.runProtected(ClassRoadie.java:59)
	at org.junit.internal.runners.JUnit4ClassRunner.run(JUnit4ClassRunner.java:64)
	at org.junit.internal.runners.CompositeRunner.runChildren(CompositeRunner.java:49)
	at org.junit.internal.runners.CompositeRunner.run(CompositeRunner.java:38)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:178)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:149)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:132)
	at org.junit.runner.JUnitCore.runMain(JUnitCore.java:105)
	at org.junit.runner.JUnitCore.main(JUnitCore.java:56)

FAILURES!!!
Tests run: 4,  Failures: 1

*/