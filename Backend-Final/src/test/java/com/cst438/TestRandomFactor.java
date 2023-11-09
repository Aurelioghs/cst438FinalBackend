package com.cst438;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.cst438.service.RandomFactor;

public class TestRandomFactor {
	
	/*
	 * test that random factors are between 11 and 99 inclusive
	 */
	@Test
	public void testRandomFactor() {
		RandomFactor rf = new RandomFactor();
		for (int i=0; i<10000; i++) {
			int factor = rf.getRandomFactor();
			assertTrue(factor>=11 && factor <=99);
		}
	}

}
