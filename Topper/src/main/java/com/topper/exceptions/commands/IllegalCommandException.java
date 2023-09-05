package com.topper.exceptions.commands;

@SuppressWarnings("serial")
public class IllegalCommandException extends CommandException {
	
	private static final String PREFIX = "Illegal command: ";
	
	public IllegalCommandException(final String message) {
		super(PREFIX + message);
	}
	
	public IllegalCommandException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}
