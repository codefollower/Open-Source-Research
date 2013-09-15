package org.junit.samples;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.*;
import org.junit.Ignore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Some simple tests.
 *
 */

/*
//不能放在这里，因为Junit要求测试类是public的
class JUnit4ClassRunnerTestSuper {
	@Test public void m2() {
		System.err.println("JUnit4ClassRunnerTestSuper: m2()");
	}

	@Test public void m4() {
		System.err.println("JUnit4ClassRunnerTestSuper: m4()");
	}
}
*/

/*
	JUnit4ClassRunnerTest: beforeClass3()
	JUnit4ClassRunnerTest: beforeClass2()
	JUnit4ClassRunnerTest: beforeClass1()
	invoke JUnit4ClassRunnerTest() invokeCount=1
	JUnit4ClassRunnerTest: before3()
	JUnit4ClassRunnerTest: before2()
	JUnit4ClassRunnerTest: before1()
	JUnit4ClassRunnerTest: after1()
	JUnit4ClassRunnerTest: after2()
	JUnit4ClassRunnerTest: after3()
	invoke JUnit4ClassRunnerTest() invokeCount=2
	JUnit4ClassRunnerTest: before3()
	JUnit4ClassRunnerTest: before2()
	JUnit4ClassRunnerTest: before1()
	JUnit4ClassRunnerTest: m1()
	JUnit4ClassRunnerTest: after1()
	JUnit4ClassRunnerTest: after2()
	JUnit4ClassRunnerTest: after3()
	invoke JUnit4ClassRunnerTest() invokeCount=3
	JUnit4ClassRunnerTest: before3()
	JUnit4ClassRunnerTest: before2()
	JUnit4ClassRunnerTest: before1()
	JUnit4ClassRunnerTest: m2()
	JUnit4ClassRunnerTest: after1()
	JUnit4ClassRunnerTest: after2()
	JUnit4ClassRunnerTest: after3()
	invoke JUnit4ClassRunnerTest() invokeCount=4
	JUnit4ClassRunnerTest: before3()
	JUnit4ClassRunnerTest: before2()
	JUnit4ClassRunnerTest: before1()
	JUnit4ClassRunnerTest: m3()
	JUnit4ClassRunnerTest: after1()
	JUnit4ClassRunnerTest: after2()
	JUnit4ClassRunnerTest: after3()
	invoke JUnit4ClassRunnerTest() invokeCount=5
	JUnit4ClassRunnerTest: before3()
	JUnit4ClassRunnerTest: before2()
	JUnit4ClassRunnerTest: before1()
	JUnit4ClassRunnerTestSuper: m4()
	JUnit4ClassRunnerTest: after1()
	JUnit4ClassRunnerTest: after2()
	JUnit4ClassRunnerTest: after3()
	JUnit4ClassRunnerTest: afterClass1()
	JUnit4ClassRunnerTest: afterClass2()
	JUnit4ClassRunnerTest: afterClass3()
*/

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnno {}

//@Ignore
@MyAnno
public class JUnit4ClassRunnerTest extends JUnit4ClassRunnerTestSuper {
	//必须有默认构造函数
	//public JUnit4ClassRunnerTest(int i) {
	//}

	static int invokeCount=0;
	public JUnit4ClassRunnerTest() {
		invokeCount++;
		System.err.println("invoke JUnit4ClassRunnerTest() invokeCount="+invokeCount);
	}
	protected int fValue1;
	protected int fValue2;

	@BeforeClass public static void beforeClass1() {
		System.err.println("JUnit4ClassRunnerTest: beforeClass1()");
	}
	@BeforeClass public static void beforeClass2() {
		System.err.println("JUnit4ClassRunnerTest: beforeClass2()");
	}
	@BeforeClass public static void beforeClass3() {
		System.err.println("JUnit4ClassRunnerTest: beforeClass3()");
	}

	@AfterClass public static void afterClass1() {
		System.err.println("JUnit4ClassRunnerTest: afterClass1()");
	}
	@AfterClass public static void afterClass2() {
		System.err.println("JUnit4ClassRunnerTest: afterClass2()");
	}
	@AfterClass public static void afterClass3() {
		System.err.println("JUnit4ClassRunnerTest: afterClass3()");
	}

	/*
	@Before public void setUp() {
		fValue1= 2;
		fValue2= 3;

		System.err.println("JUnit4ClassRunnerTest: setUp()");
	}
	*/

	@Before public void before1() {
		System.err.println("JUnit4ClassRunnerTest: before1()");
	}

	@Before public void before2() {
		System.err.println("JUnit4ClassRunnerTest: before2()");
	}

	@Before public void before3() {
		System.err.println("JUnit4ClassRunnerTest: before3()");
	}

	@After public void after1() {
		System.err.println("JUnit4ClassRunnerTest: after1()");
	}

	@After public void after2() {
		System.err.println("JUnit4ClassRunnerTest: after2()");
	}

	@After public void after3() {
		System.err.println("JUnit4ClassRunnerTest: after3()");
	}
	
	/*
	@Test public void divideByZero() {
		int zero= 0;
		int result= 8/zero;
		result++; // avoid warning for not using result
	}
	

	@Test(timeout=3000) public void testEquals() {
		assertEquals(12, 12);
		assertEquals(12L, 12L);
		assertEquals(new Long(12), new Long(12));

		//assertEquals("Size", 12, 13);
		//assertEquals("Capacity", 12.0, 11.99, 0.0);
	}
	*/

	@Test(timeout=3000) public void m0() {
		assertEquals(12, 12);
		assertEquals(12L, 12L);
		assertEquals(new Long(12), new Long(12));

		//assertEquals("Size", 12, 13);
		//assertEquals("Capacity", 12.0, 11.99, 0.0);
	}

	@Ignore @Test public void m1() {
		System.err.println("JUnit4ClassRunnerTest: m1()");
	}
	@Test public void m2() {
		System.err.println("JUnit4ClassRunnerTest: m2()");

		//assertEquals(12, 32);
	}
	@Test public void m3() {
		System.err.println("JUnit4ClassRunnerTest: m3()");
	}

}