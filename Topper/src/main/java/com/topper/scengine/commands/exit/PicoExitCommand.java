package com.topper.scengine.commands.exit;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.scengine.commands.PicoCommand;
import com.topper.scengine.commands.PicoTopLevelCommand;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;
import com.topper.sstate.SelectionState;
import com.topper.sstate.TerminationState;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "exit", description = "Terminates this process.")
@PicoState(states = { SelectionState.class, ExecutionState.class })
public final class PicoExitCommand extends PicoCommand {

	@ParentCommand
	private PicoTopLevelCommand parent;

	@Override
	public void execute(@NonNull ScriptContext context) {
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
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent;
	}
}