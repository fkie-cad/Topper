package com.topper.exceptions;

import org.eclipse.jdt.annotation.NonNull;

/**
 * {@link RuntimeException} that will be thrown, if a situation occurs
 * that is expected to be impossible.
 * */
public final class UnreachableException extends RuntimeException {

	private static final String PREFIX = "Unreachable situation occurred: ";
	
	public UnreachableException(@NonNull final String message) {
		super(PREFIX + message);
	}
	
	public UnreachableException(@NonNull final String message, @NonNull final Throwable t) {
		super(PREFIX + message, t);
	}
}