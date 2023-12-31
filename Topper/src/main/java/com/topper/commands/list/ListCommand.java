package com.topper.commands.list;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.commands.TopLevelCommand;
import com.topper.exceptions.commands.CommandException;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.CommandLink;
import com.topper.sstate.CommandContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", mixinStandardHelpOptions = true, version = "1.0", subcommands = { ListMethodsCommand.class, ListTypesCommand.class }, description = "Lists requested, file-related resources.")
@CommandLink(states =  { ExecutionState.class })
public final class ListCommand extends PicoCommand {

	@ParentCommand
	private TopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final CommandContext context) throws CommandException {
		this.getTopLevel().out().println(new CommandLine(this).getUsageMessage());
	}

	@Override
	@NonNull 
	public final CommandState next() {
		return new ExecutionState(this.getContext());
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