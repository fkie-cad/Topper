package com.topper.exceptions.pipeline;

public class ViolatedAssumptionException extends StageException {

	private static final String PREFIX = "Violated assumption: ";
	
	public ViolatedAssumptionException(final String message) {
		super(PREFIX + message);
	}
	
	public ViolatedAssumptionException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}