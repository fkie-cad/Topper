package com.topper.exceptions.pipeline;

import com.topper.exceptions.TopperException;

public class StageException extends TopperException {

	private static final String PREFIX = "Pipeline stage error: ";
	
	public StageException(final String message) {
		super(PREFIX + message);
	}
	
	public StageException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}