package com.topper.exceptions;

public class CommandException extends TopperException {
	
	public CommandException(final String message) {
		super(message);
	}
	
	public CommandException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}