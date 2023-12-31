package com.topper.exceptions.commands;

@SuppressWarnings("serial")
public class InvalidStateTransitionException extends StateException {
	
	private static final String PREFIX = "Invalid state transition: ";

	public InvalidStateTransitionException(String message) {
		super(PREFIX + message);
	}

	public InvalidStateTransitionException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}
