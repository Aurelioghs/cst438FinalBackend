package com.cst438.service;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class RandomFactor {
	
	private final int LOWER=11; // bounds for factors
	private final int UPPER=99;
	
	private Random generator = new Random();
	
	/*
	 * generate random integer between LOWER and UPPER inclusive
	 */
	public int getRandomFactor() {
		return generator.nextInt(UPPER-LOWER+1)+LOWER;
	}
	

}
