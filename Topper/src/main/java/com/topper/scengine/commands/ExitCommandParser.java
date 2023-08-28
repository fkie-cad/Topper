package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.SelectionState;

@TopperCommandParser(states = { SelectionState.class, ExecutionState.class })
public final class ExitCommandParser implements ScriptCommandParser {

	@Override
	public final ScriptCommand parse(final String[] tokens) throws IllegalCommandException {
		return new ExitCommand();
	}

	@Override
	public final String usage() {
		return this.command();
	}

	@Override
	public String command() {
		return "exit";
	}

	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return ExitCommand.class;
	}
}