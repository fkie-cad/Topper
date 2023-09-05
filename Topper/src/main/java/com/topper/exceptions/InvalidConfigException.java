package com.topper.exceptions;

/**
 * Configuration exception used to indicate that an error related
 * to loading or parsing in e.g. {@link ConfigManager} failed.
 * 
 * @author Pascal Kühnemann
 * @since 21.08.2023
 * */
@SuppressWarnings("serial")
public class InvalidConfigException extends TopperException {
	
	private static final String PREFIX = "Config error: ";
	
	public InvalidConfigException(final String message) {
		super(PREFIX + message);
	}

	public InvalidConfigException(final String message, final Throwable throwable) {
		super(PREFIX + message, throwable);
	}
}