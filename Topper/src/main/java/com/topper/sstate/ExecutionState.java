package com.topper.sstate;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.scengine.commands.ExitCommand;
import com.topper.scengine.commands.FileCommand;
import com.topper.scengine.commands.HelpCommand;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.SearchCommand;

public final class ExecutionState extends CommandState {

	public ExecutionState(final ScriptContext context) {
		super(context);
	}

	@Override
	public final ImmutableList<Class<? extends ScriptCommand>> getAvailableCommands() {
		return ImmutableList.of(
				FileCommand.class,
				SearchCommand.class,
				HelpCommand.class,
				ExitCommand.class
		);
	}

	@Override
	public final void executeCommand(final ScriptCommand command)
			throws InvalidStateTransitionException, CommandException, IOException {
		command.execute(this.getContext());
		
		// Determine what next state is. Only "exit" can transition to a "new" state
		if (command instanceof ExitCommand) {
			this.getContext().changeState(new TerminationState(this.getContext()));
		}
	}
}