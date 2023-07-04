package com.topper.interactive;

import com.topper.configuration.TopperConfig;

public final class InteractiveTopper {

	private final TopperConfig config;
	
	public InteractiveTopper(final TopperConfig config) {
		this.config = config;
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	public final void mainLoop() {
		throw new UnsupportedOperationException();
	}
}