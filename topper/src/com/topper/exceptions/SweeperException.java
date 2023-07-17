package com.topper.exceptions;

public class SweeperException extends TopperException {

	private static final String PREFIX = "Sweeping error: ";
	
	public SweeperException(final String message) {
		super(PREFIX + message);
	}
	
	public SweeperException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}