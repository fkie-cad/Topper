package com.topper.sstate;

import com.topper.configuration.TopperConfig;
import com.topper.scengine.ScriptCommand;

public final class ExecutionDriver {

	private final TopperConfig config;
	
	private CommandState state;
	
	
	public ExecutionDriver(final TopperConfig config) {
		this.config = config;
	}
	
	public void changeState(final CommandState newState) {
		this.state = newState;
	}
	
	public final void execute(final ScriptCommand command) {
		
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
}
