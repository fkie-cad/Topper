package com.topper.sstate;

import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.ScriptCommand;

public final class TerminationState extends CommandState {

	public TerminationState(final ScriptContext context) {
		super(context);
	}

	@Override
	public final void execute(final ScriptCommand command) throws InvalidStateTransitionException {
		throw new InvalidStateTransitionException("Cannot execute command in termination state.");
	}
}