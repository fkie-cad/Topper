package com.topper.exceptions.scripting;

import com.topper.exceptions.TopperException;

public class StateException extends TopperException {
	
	private static final String PREFIX = "State error: ";

	public StateException(final String message) {
		super(PREFIX + message);
	}
	
	public StateException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}

}
