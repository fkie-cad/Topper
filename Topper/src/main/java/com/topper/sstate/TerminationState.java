package com.topper.sstate;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;

public final class TerminationState extends CommandState {

	public TerminationState(@NonNull final ScriptContext context) {
		super(context);
	}

	@Override
	public final void executeCommand(@NonNull final ScriptCommand command)
			throws InvalidStateTransitionException, CommandException, IOException {
		// Should be unreachable...
		throw new InvalidStateTransitionException("Cannot execute command in termination state.");
	}
}