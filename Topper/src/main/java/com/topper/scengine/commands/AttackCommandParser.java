package com.topper.scengine.commands;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.ScriptParser;

public final class AttackCommandParser implements ScriptCommandParser {

	@NonNull
	private static final ScriptParser subcommandParser = new ScriptParser();
	
	@Override
	@NonNull
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		
		// Check sub - command
		if (tokens.length <= 1) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		final ScriptCommandParser parser = subcommandParser.findParserByString(tokens[1]);
		if (parser == null) {
			throw new IllegalCommandException("Subcommand " + tokens[1] + " is unknown.");
		}
		
		// Parse command from sub - command tokens
		final ScriptCommand subcommand = parser.parse(Arrays.copyOfRange(tokens, 1, tokens.length));
		
		return subcommand;
	}

	@Override
	@NonNull 
	public final String usage() {
		return this.command() + " <attack-type> <attack-specific args>";
	}

	@Override
	@NonNull 
	public final String command() {
		return "attack";
	}
	
	public static final void registerSubcommandParser(@NonNull final ScriptCommandParser parser) {
		subcommandParser.registerParser(parser);
	}
}