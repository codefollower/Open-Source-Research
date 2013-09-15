package org.junit.samples;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
/**
 * Some simple tests.
 *
 */

@Ignore
public class JUnit4ClassRunnerTestSuper {
	@Test public void m2() {
		System.err.println("JUnit4ClassRunnerTestSuper: m2()");
	}

	@Test public void m4() {
		System.err.println("JUnit4ClassRunnerTestSuper: m4()");
	}
}
