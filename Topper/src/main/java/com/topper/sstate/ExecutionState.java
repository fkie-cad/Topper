package com.topper.sstate;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.scengine.commands.ExitCommand;
import com.topper.scengine.commands.ScriptCommand;

public final class ExecutionState extends CommandState {

	public ExecutionState(@NonNull final ScriptContext context) {
		super(context);
	}

	@Override
	public final void executeCommand(@NonNull final ScriptCommand command)
			throws InvalidStateTransitionException, CommandException, IOException {
		command.execute(this.getContext());
		
		// Determine what next state is. Only "exit" can transition to a "new" state
		if (command instanceof ExitCommand) {
			this.getContext().changeState(new TerminationState(this.getContext()));
		}
	}
}