package com.topper.scengine.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.scengine.commands.PicoCommand;
import com.topper.scengine.commands.PicoTopLevelCommand;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "ctop", mixinStandardHelpOptions = true, version = "1.0", description = "Computes a list of patches to apply to the loaded file to achieve gadget chain execution.")
@PicoState(states = { ExecutionState.class })
public final class PicoTOPExceptionHandlerAttackCommand extends PicoCommand {

	@ParentCommand
	private PicoAttackCommand parent;
	
	@Override
	public void execute(@NonNull ScriptContext context) throws CommandException {
		this.getTopLevel().out().println("Performing amazing attack.");
	}

	@Override
	@NonNull 
	public CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@Override
	@NonNull 
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent.getTopLevel();
	}
}