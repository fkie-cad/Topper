package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.IllegalCommandException;

public final class SearchCommandParser implements ScriptCommandParser {

	@Override
	@NonNull 
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		
		if (tokens.length < 1 || tokens.length > 2) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		final String expression = (tokens.length > 1) ? tokens[1] : "";
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
}