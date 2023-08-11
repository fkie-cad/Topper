package com.topper.sstate;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.scengine.commands.ExitCommand;
import com.topper.scengine.commands.FileCommand;
import com.topper.scengine.commands.HelpCommand;
import com.topper.scengine.commands.ScriptCommand;

public final class SelectionState extends CommandState {

	public SelectionState(final ScriptContext context) {
		super(context);
	}

	@Override
	public ImmutableList<Class<? extends ScriptCommand>> getAvailableCommands() {
		return ImmutableList.of(
				FileCommand.class,
				ExitCommand.class,
				HelpCommand.class
		);
	}
	
	@Override
	public final void executeCommand(final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException {

		// Only available commands are "file", "exit" and "help"
		command.execute(this.getContext());
		
		// Only "file" triggers transition that enables further commands.
		if (command instanceof FileCommand) {
			this.getContext().changeState(new ExecutionState(this.getContext()));
		} else if(command instanceof ExitCommand) {
			this.getContext().changeState(new TerminationState(this.getContext()));
		}
	}
}