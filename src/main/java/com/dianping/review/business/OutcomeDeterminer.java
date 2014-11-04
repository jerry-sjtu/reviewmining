package com.dianping.review.business;

import java.util.Random;

import org.apache.log4j.Logger;

public class OutcomeDeterminer {

	private static final Logger logger = Logger.getLogger(OutcomeDeterminer.class
			.getName());

	private Random generator;

	public OutcomeDeterminer() {
		generator = new Random();
	}

	public String getOutcome() {
		int randomNumber = generator.nextInt(5);
		if (logger.isDebugEnabled()) {
			logger.debug("Random number is: " + randomNumber);
		}

		if (randomNumber == 0) {
			return "error";
		}
		return "success";
	}
}
