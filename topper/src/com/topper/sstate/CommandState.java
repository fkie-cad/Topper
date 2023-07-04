package com.topper.sstate;

import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.ScriptCommand;

public abstract class CommandState {
	
	private final ScriptContext context;
	
	public CommandState(final ScriptContext context) {
		this.context = context;
	}

	public abstract void execute(final ScriptCommand command) throws InvalidStateTransitionException;
	
	public final ScriptContext getContext() {
		return this.context;
	}
}
