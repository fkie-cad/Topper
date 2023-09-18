package com.topper.commands.exit;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.commands.TopLevelCommand;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.CommandLink;
import com.topper.sstate.CommandContext;
import com.topper.sstate.SelectionState;
import com.topper.sstate.TerminationState;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "exit", mixinStandardHelpOptions = true, version = "1.0", description = "Terminates this process.")
@CommandLink(states = { SelectionState.class, ExecutionState.class })
public final class ExitCommand extends PicoCommand {

	@ParentCommand
	private TopLevelCommand parent;

	@Override
	public void execute(@NonNull CommandContext context) {
		parent.out().println("Thank you for traveling with Deutsche Bahn!\n");
	}

	@Override
	@NonNull 
	public CommandState next() {
		return new TerminationState(this.getContext());
	}
	
	@SuppressWarnings("null")	// cannot be null
	@Override
	@NonNull 
	public final TopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent;
	}
}