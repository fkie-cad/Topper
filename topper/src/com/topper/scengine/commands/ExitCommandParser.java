package com.topper.scengine.commands;

import com.topper.exceptions.IllegalCommandException;

public final class ExitCommandParser implements ScriptCommandParser {

	@Override
	public final ScriptCommand parse(final String[] tokens) throws IllegalCommandException {
		return new ExitCommand();
	}

	@Override
	public final String usage() {
		return "exit";
	}
}