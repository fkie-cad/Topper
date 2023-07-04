package com.topper.sstate;

import com.topper.configuration.TopperConfig;
import com.topper.scengine.ScriptCommand;

public final class ScriptContext {

	private final TopperConfig config;
	
	private CommandState state;
	
	
	public ScriptContext(final TopperConfig config) {
		this.config = config;
	}
	
	public void changeState(final CommandState newState) {
		this.state = newState;
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
}