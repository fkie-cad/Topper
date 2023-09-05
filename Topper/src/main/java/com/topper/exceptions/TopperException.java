package com.topper.exceptions;

/**
 * Top - level Topper exception used to group all Topper
 * related exceptions.
 * 
 * @author Pascal Kühnemann
 * @since 21.08.2023
 * */
@SuppressWarnings("serial")
public class TopperException extends Exception {

	public TopperException(final String message) {
		super(message);
	}
	
	public TopperException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}