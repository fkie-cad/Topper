package com.topper.scengine.commands.help;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;
import com.topper.scengine.commands.TopperCommandParser;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.SelectionState;

@TopperCommandParser(states = { SelectionState.class, ExecutionState.class })
public final class HelpCommandParser implements ScriptCommandParser {

	@Override
	public final ScriptCommand parse(final String[] tokens) throws IllegalCommandException {
		
		if (tokens.length != 2) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		return new HelpCommand(tokens[1]);
	}

	@Override
	public final String usage() {
		return this.command() + " [command]";
	}

	@Override
	public String command() {
		return "help";
	}

	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return HelpCommand.class;
	}
}