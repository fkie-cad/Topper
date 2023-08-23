package com.topper.scengine.commands;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.sstate.ScriptContext;

public final class HelpCommand implements ScriptCommand {

	@NonNull
	private final String command;
	
	public HelpCommand(@NonNull final String command) {
		this.command = command;
	}
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException, IOException {
		
		final Set<@NonNull ScriptCommandParser> parsers = CommandManager.get().findParserByName(this.command);
		if (parsers.size() == 1) {
			// Print usage based on registered parser
			context.getIO().output("Usage:" + System.lineSeparator() + "    " + parsers.stream().findFirst().get().usage() + System.lineSeparator());
		} else if (parsers.size() >= 2) {
			context.getIO().output("Command " + this.command + " is ambiguous." + System.lineSeparator());
		} else {
			context.getIO().output("Command " + this.command + " is unknown." + System.lineSeparator());
		}
	}
}