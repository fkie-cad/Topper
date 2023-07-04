package com.topper.exceptions;

public class InputException extends TopperException {

	private static final String PREFIX = "Input error: ";
	
	public InputException(final String message) {
		super(PREFIX + message);
	}
	
	public InputException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}