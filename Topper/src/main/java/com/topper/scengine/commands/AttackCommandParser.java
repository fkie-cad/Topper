package com.topper.scengine.commands;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;

public final class AttackCommandParser implements ScriptCommandParser {
	
	static {
		CommandManager.get().registerCommandParser(new AttackCommandParser());
	}

	private final CommandManager manager;
	
	public AttackCommandParser() {
		this.manager = CommandManager.get();
	}
	
	@Override
	@NonNull
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		
		// TODO: CONTINUE HERE: IMPLEMENT SUB COMMANDS AND WRITE TESTS FOR COMMANDS
		
		// Check sub - command
		if (tokens.length <= 1) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		final Set<@NonNull ScriptCommandParser> parsers = this.manager.findParserByName(tokens[1]);
		if (parsers.size() == 0) {
			throw new IllegalCommandException("Subcommand " + tokens[1] + " is unknown.");
		} else if (parsers.size() >= 2) {
			throw new IllegalCommandException("Subcommand " + tokens[1] + " is ambiguous.");
		}
		final ScriptCommandParser parser = parsers.stream().findFirst().get();
		
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
}