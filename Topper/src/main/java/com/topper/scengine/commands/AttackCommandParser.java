package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.attack.TOPExceptionHandlerAttackCommandParser;

public final class AttackCommandParser implements ScriptCommandParser {

	
	@NonNull
	private final ScriptParser subcommandParser;
	
	public AttackCommandParser() {
		
		// Register sub - commands
		this.subcommandParser = new ScriptParser();
		this.subcommandParser.registerParser(new TOPExceptionHandlerAttackCommandParser());
	}
	
	@Override
	@NonNull
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		
		// Check sub - command
		if (tokens.length <= 1) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}
		
		
		
		return new AttackCommand();
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