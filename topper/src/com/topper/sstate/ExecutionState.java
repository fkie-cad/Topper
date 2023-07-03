package com.topper.sstate;

import com.topper.scengine.ScriptCommand;

public final class ExecutionState extends CommandState {

	public ExecutionState(final ExecutionDriver driver) {
		super(driver);
	}

	@Override
	public final void execute(final ScriptCommand command) {
		throw new UnsupportedOperationException("TODO");
	}
}