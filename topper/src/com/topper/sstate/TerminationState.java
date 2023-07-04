package com.topper.sstate;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;

public final class TerminationState extends CommandState {

	public TerminationState(final ScriptContext context) {
		super(context);
	}

	@Override
	public final ImmutableList<Class<? extends ScriptCommand>> getAvailableCommands() {
		return ImmutableList.of();
	}

	@Override
	public final void executeCommand(ScriptCommand command)
			throws InvalidStateTransitionException, CommandException, IOException {
		// Should be unreachable...
		throw new InvalidStateTransitionException("Cannot execute command in termination state.");
	}
}