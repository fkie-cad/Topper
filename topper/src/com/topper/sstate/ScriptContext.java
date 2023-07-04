package com.topper.sstate;

import com.topper.configuration.TopperConfig;
import com.topper.interactive.IOManager;
import com.topper.scengine.ScriptParser;

public final class ScriptContext {

	private final TopperConfig config;
	
	private CommandState state;
	
	private final IOManager io;
	private final ScriptParser parser;
	
	
	public ScriptContext(final TopperConfig config, final IOManager io, final ScriptParser parser) {
		this.config = config;
		this.io = io;
		this.parser = parser;
		this.state = new SelectionState(this);
	}
	
	public final void changeState(final CommandState newState) {
		this.state = newState;
	}
	
	public final CommandState getCurrentState() {
		return this.state;
	}
	
	public final boolean isTerminationState() {
		return this.state instanceof TerminationState;
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	public final IOManager getIO() {
		return this.io;
	}
	
	public final ScriptParser getParser() {
		return this.parser;
	}
}