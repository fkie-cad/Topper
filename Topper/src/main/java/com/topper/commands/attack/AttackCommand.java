package com.topper.commands.attack;

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

@Command(name = "attack", mixinStandardHelpOptions = true, version = "1.0", subcommands = {
		TOPExceptionHandlerAttackCommand.class }, description = "Computes an attack to use on the loaded file given special assumptions.")
@CommandLink(states = { ExecutionState.class })
public final class AttackCommand extends PicoCommand {

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