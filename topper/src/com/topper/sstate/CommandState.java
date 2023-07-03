package com.topper.sstate;

import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.ScriptCommand;

public abstract class CommandState {
	
	private final ExecutionDriver driver;
	
	public CommandState(final ExecutionDriver driver) {
		this.driver = driver;
	}

	public abstract void execute(final ScriptCommand command) throws InvalidStateTransitionException;
	
	public final ExecutionDriver getContext() {
		return this.driver;
	}
}
