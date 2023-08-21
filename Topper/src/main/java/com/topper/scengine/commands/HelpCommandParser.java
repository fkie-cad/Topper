package com.topper.scengine.commands;

import com.topper.exceptions.scripting.IllegalCommandException;

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
}