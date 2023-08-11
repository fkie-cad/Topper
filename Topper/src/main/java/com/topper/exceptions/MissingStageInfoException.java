package com.topper.exceptions;

public class MissingStageInfoException extends StageException {

	private static final String PREFIX = "Missing Stage Information: ";
	
	public MissingStageInfoException(final String message) {
		super(PREFIX + message);
	}
	
	public MissingStageInfoException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}

}
