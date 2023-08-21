package com.topper.scengine.commands;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.CommandException;
import com.topper.sstate.ScriptContext;

public final class HelpCommand implements ScriptCommand {

	private final String command;
	
	public HelpCommand(final String command) {
		this.command = command;
	}
	
	@Override
	public final void execute(final ScriptContext context) throws CommandException, IOException {
		
		final ScriptCommandParser parser = context.getParser().findParserByString(this.command);
		if (parser != null) {
			// Print usage based on registered parser
			context.getIO().output("Usage:" + System.lineSeparator() + "    " + parser.usage() + System.lineSeparator());
		} else {
			context.getIO().output("Command " + this.command + " is unknown." + System.lineSeparator());
		}
	}
}