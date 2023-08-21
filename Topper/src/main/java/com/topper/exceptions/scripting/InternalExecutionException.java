package com.topper.exceptions.scripting;

public final class InternalExecutionException extends CommandException {

	private static final String PREFIX = "Command execution failed:";
	
	public InternalExecutionException(final String message) {
		super(PREFIX + message);
	}

	public InternalExecutionException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}