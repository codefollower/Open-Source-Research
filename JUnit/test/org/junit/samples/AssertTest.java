package org.junit.samples;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A sample test case, testing {@link java.util.Vector}.
 *
 */
public class AssertTest {
	@Test(expected= IndexOutOfBoundsException.class) public void m0() {
		assertEquals(12, 12);
		assertEquals(12L, 12L);
		assertEquals(new Long(12), new Long(12));

		//assertEquals("Size", 12, 13);
		//assertEquals("Capacity", 12.0, 11.99, 0.0);

		 new ArrayList<Object>().get(0); 
	}
}