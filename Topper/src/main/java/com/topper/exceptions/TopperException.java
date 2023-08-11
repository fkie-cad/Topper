package com.topper.exceptions;

public class TopperException extends Exception {

	public TopperException(final String message) {
		super(message);
	}
	
	public TopperException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}