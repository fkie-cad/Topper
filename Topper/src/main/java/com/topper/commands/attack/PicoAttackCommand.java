package com.topper.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.exceptions.commands.CommandException;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "attack", mixinStandardHelpOptions = true, version = "1.0", subcommands = {
		PicoTOPExceptionHandlerAttackCommand.class }, description = "Computes an attack to use on the loaded file given special assumptions.")
@PicoState(states = { ExecutionState.class })
public final class PicoAttackCommand extends PicoCommand {

	@ParentCommand
	private PicoTopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException {
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
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent;
	}
}