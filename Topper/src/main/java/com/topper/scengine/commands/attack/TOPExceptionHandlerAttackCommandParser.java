package com.topper.scengine.commands.attack;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.AttackCommandParser;
import com.topper.scengine.commands.CommandManager;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;

public final class TOPExceptionHandlerAttackCommandParser implements ScriptCommandParser {

	public TOPExceptionHandlerAttackCommandParser() {
		CommandManager.get().registerCommandParser(AttackCommandParser.class, this);
	}
	
	@Override
	@NonNull 
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {
		return new TOPExceptionHandlerAttackCommand();
	}

	@Override
	@NonNull 
	public final String usage() {
		return this.command() + "";
	}

	@Override
	public @NonNull String command() {
		return "ctop";
	}
}