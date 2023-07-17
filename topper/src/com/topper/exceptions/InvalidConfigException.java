package com.topper.exceptions;

public class InvalidConfigException extends TopperException {
	
	private static final String PREFIX = "Config error: ";
	
	public InvalidConfigException(final String message) {
		super(PREFIX + message);
	}

	public InvalidConfigException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}