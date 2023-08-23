package com.topper.scengine.commands;

import com.topper.exceptions.scripting.IllegalCommandException;

public final class ExitCommandParser implements ScriptCommandParser {

	static {
		CommandManager.get().registerCommandParser(new ExitCommandParser());
	}
	
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
}