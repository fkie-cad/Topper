package com.topper.exceptions.pipeline;

public class SweeperException extends StageException {

	private static final String PREFIX = "Sweeping error: ";
	
	public SweeperException(final String message) {
		super(PREFIX + message);
	}
	
	public SweeperException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}