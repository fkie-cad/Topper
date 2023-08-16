package com.topper.exceptions;

public final class DuplicateInfoIdException extends StageException {

	private static final String PREFIX = "Duplicate identifier: ";
	
	public DuplicateInfoIdException(final String message) {
		super(PREFIX + message);
	}
	
	public DuplicateInfoIdException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}