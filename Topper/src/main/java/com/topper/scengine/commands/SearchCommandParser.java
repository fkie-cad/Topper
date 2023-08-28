package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.sstate.ExecutionState;

@TopperCommandParser(states = { ExecutionState.class })
public final class SearchCommandParser implements ScriptCommandParser {

	@Override
	@NonNull 
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		
		if (tokens.length <= 0) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		String expression = "";
		if (tokens.length >= 2) {
			
			final StringBuilder b = new StringBuilder();
			for (int i = 1; i < tokens.length; i++) {
				if (i < tokens.length - 1) {
					b.append(tokens[i] + "\\s");
				} else {
					b.append(tokens[i]);
				}
			}
			expression = b.toString();
		}
		
		if (expression == null) {
			throw new IllegalCommandException("Invalid expression.");
		}
		return new SearchCommand(expression);
	}

	@Override
	@NonNull 
	public final String usage() {
		return this.command() + " [expr]";
	}

	@Override
	@NonNull
	public final String command() {
		return "search";
	}

	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return SearchCommand.class;
	}
}