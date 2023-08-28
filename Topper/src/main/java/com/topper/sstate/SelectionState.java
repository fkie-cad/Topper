package com.topper.sstate;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.exit.ExitCommand;
import com.topper.scengine.commands.file.FileCommand;

public final class SelectionState extends CommandState {

	public SelectionState(@NonNull final ScriptContext context) {
		super(context);
	}
	
	@Override
	public final void executeCommand(@NonNull final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException {

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