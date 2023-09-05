package com.topper.exceptions.commands;

public final class IllegalSessionState extends CommandException {

	private static final String PREFIX = "Session state is illegal: ";
	
	public IllegalSessionState(final String message) {
		super(PREFIX + message);
	}
	
	public IllegalSessionState(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}