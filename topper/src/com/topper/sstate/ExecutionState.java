package com.topper.sstate;

import com.topper.scengine.ScriptCommand;

public final class ExecutionState extends CommandState {

	public ExecutionState(final ScriptContext context) {
		super(context);
	}

	@Override
	public final void execute(final ScriptCommand command) {
		throw new UnsupportedOperationException("TODO");
	}
}